# Authorisation & tenant isolation review (static, read-only)

Scope: all modules under `modules/*/src/main/java`. Method: source reading only (app not run, no network).
Companion file: `docs/security/_work/endpoint-inventory.md` (1426 endpoints: 330 declared + 1096 inherited from
`BaseController`, 103 controllers, 20 with `@PreAuthorize`).

Severity scale: **Critical** = any authenticated user (any tenant) can take over the platform or read/modify all tenants'
data; **High** = cross-tenant data access or privilege gain with preconditions / limited scope; **Medium** = limited
disclosure, defence-in-depth gap with realistic abuse; **Low/Info** = hardening.

---

## Summary of findings

| ID | Severity | Title |
|---|---|---|
| AUTHZ-01 | Critical | Client-controlled `filterTenant=false` disables tenant scoping on every `advanced/search` and export endpoint |
| AUTHZ-02 | Critical | Generic `BaseController` CRUD (`fetch/{id}`, `PUT`, `remove`, `delete`, `qr`) is not tenant-scoped and has no authorisation (IDOR across 77 controllers) |
| AUTHZ-03 | High | `TenantModuleApiAccessFilter` runs before `TenantFilter` → module gating is a no-op (needs verification) |
| AUTHZ-04 | Critical | Any authenticated user can rename/re-permission any role (incl. the shared `ADMIN` role) → OOSMADMIN escalation |
| AUTHZ-05 | High | Mass assignment of `tenantId`, `isDeleted`, audit fields (and nested relations) via ModelMapper on create/update |
| AUTHZ-06 | Critical | User management bypass: base `POST/PUT/DELETE /api/security/user` have no checks (set any user's password hash → account takeover); `addUser`/`updateUser` accept arbitrary role/tenant |
| AUTHZ-07 | High | Company profile (tenant) update and listing are open to every user: edit any tenant's legal/bank data, enumerate all tenants |
| AUTHZ-08 | High | Document-generation PDFs (`/api/documents/**`) load entities by id without tenant check |
| AUTHZ-09 | High | Payslip PDF (and payslip fetch) cross-tenant: permission is checked but the lookup is not tenant-scoped |
| AUTHZ-10 | Medium | QR / code resolution falls back to a global (all-tenant) lookup |
| AUTHZ-11 | Medium | Assorted unscoped queries: filtration by status, deliveries for quality, delivery numbers, role user counts |
| AUTHZ-12 | High | Global permission catalogue CRUD open to all users (non-admin endpoints of `PermissionController`) |
| AUTHZ-13 | Medium | JWT converter keeps deleted/locked users and deactivated tenants authenticated for the token lifetime |
| AUTHZ-14 | Medium | Anonymous `refresh-session` re-issues access tokens from expired access tokens (needs verification) |
| AUTHZ-15 | Medium | Anonymous `auth/resetPassword` returns the full user DTO (id, tenant, role, email, phone) → enumeration + id disclosure |
| AUTHZ-16 | Low | Async work (`CompletableFuture.runAsync`) and event listeners do not propagate `TenantContext` |
| AUTHZ-17 | Info | Only ~20 `@PreAuthorize` in the codebase; authorisation relies on per-handler code that most endpoints lack |

---

## 1. Tenant isolation model (answer to "where does the tenant come from")

* **Source:** `TenantFilter` (shared-kernel) reads `oosmUser.tenantId` (legacy `osmUser.tenantId`) from the validated JWT
  and puts it in a plain `ThreadLocal<UUID>` (`TenantContext`).
  `modules/shared-kernel/src/main/java/com/xdev/ooms/sharedkernel/config/TenantFilter.java:30-50`.
  The claim is written at token issue time by `SecurityConfig.jwtCustomizer` (`SecurityConfig.java:~79-101`).
* **Switching:** the `X-Tenant-ID` header override is commented out (`TenantFilter.java:53-57`). No other request input
  sets the tenant. A user therefore cannot *switch* tenant directly — but see AUTHZ-01/02/05/06: the tenant is simply
  not applied by most data paths, and a user's `tenantId` can be changed through mass assignment (AUTHZ-05/06), which
  becomes effective at the next token issue / refresh.
* **Cleanup:** `TenantContext.clear()` in `finally` (`TenantFilter.java:61-63`) — correct for request threads.
  `DayImportDriveService.scheduledSync` and `GoogleDriveOAuthService.persistCredential` set/clear in try/finally (good).
  `CompletableFuture.runAsync` in `UserService.java:469,483` runs mail dispatch on the common pool without the tenant
  (AUTHZ-16). No `TaskDecorator`/context propagation exists. `SeuilAlerteService` scheduling is fully commented out.
* **Enforcement mechanism:** none central. No Hibernate `@Filter`/`@TenantId`, no multi-tenant connection provider, no
  RLS. Scoping is manual: `BaseServiceImpl.findAll*` use `findAllByTenantIdAndIsDeletedFalse`; everything keyed by id
  uses unscoped `findById` / `findByIdAndIsDeletedFalse`. `BaseEntity.@PrePersist/@PreUpdate` set `tenantId` from the
  context **only if null** (`BaseEntity.java:123-136`), so a client-supplied value wins.
* **Filter order:** `TenantFilter` is a `@Component` servlet filter with no `@Order` (lowest precedence);
  `TenantModuleApiAccessFilter` is `@Order(50)`; Spring Security's proxy is `-100`. Order is therefore
  Security → module filter (tenant still null) → `TenantFilter` (AUTHZ-03).

---

## 2. Findings

### AUTHZ-01 — `filterTenant=false` disables tenant scoping on search and exports
**Severity: Critical** — one request by any authenticated user of any tenant returns every tenant's rows for any entity
(employees, payslips, sales, deliveries, users…).

**Evidence**
* `modules/shared-kernel/src/main/java/com/xdev/ooms/sharedkernel/models/SearchData.java:11,26-27`
  ```java
  private boolean filterTenant =true;
  public void setFilterTenant(boolean filterTenant) { this.filterTenant = filterTenant; }
  ```
  `SearchData` is the `@RequestBody` of `POST {base}/advanced/search` and (nested in `ExportDetails`) of
  `POST {base}/export/{pdf,csv,excel}`.
* `BaseServiceImpl.java:439-447, 469-479`
  ```java
  if (searchData.isFilterTenant()) { ... put("tenantId", details); }
  ...
  } else {
      result = repository.findAllByIsDeletedFalse(pageable);   // all tenants
  ```
  Same pattern in exports at `BaseServiceImpl.java:526, 827, 1167`.
* Overrides that keep the hole: `CompanyProfileService.search` calls `super.search` (`CompanyProfileService.java:230-236`).
  `UserService` overrides `search` with a permission + tenant check (good, `UserService.java:~97-122`).

**Impact:** full cross-tenant read of every entity exposed through a `BaseControllerImpl` subclass (77 controllers,
~320 search/export rows flagged `cross-tenant via filterTenant=false` in the inventory). Also used to harvest ids for
AUTHZ-02/04/06 (e.g. the OOSMADMIN role id via `POST /api/security/role/advanced/search`).

**Fix:** remove `filterTenant` from the request model (make it `@JsonIgnore`/server-only). Always add the tenant
predicate server-side unless `SecurityUtils.isOosmAdmin()`; also force-override any client `tenantId` key in the
search map. Longer term, a Hibernate `@Filter`/`@TenantId` enabled per request. **Effort: S** (flag) / **M** (central filter).

---

### AUTHZ-02 — Generic CRUD endpoints are IDOR-prone and unauthorised
**Severity: Critical** — any authenticated user can read, overwrite, soft-delete or hard-delete any row of any tenant
by UUID (UUIDs are obtainable via AUTHZ-01).

**Evidence** (`modules/shared-kernel/src/main/java/com/xdev/ooms/sharedkernel/services/impl/BaseServiceImpl.java`)
* `findById` (`GET {base}/fetch/{id}`): line 171 `repository.findByIdAndIsDeletedFalse(id)` — no tenant.
* `update` (`PUT {base}`): lines 303-310
  ```java
  Optional<E> existedOptEntity = this.repository.findById(request.getId());
  ...
  this.modelMapper.map(request, existedEntity);
  ```
* `remove` (`DELETE {base}/remove/{id}`): line 356 `repository.deleteById(id);` — **hard delete**, no tenant.
* `delete` (`DELETE {base}/delete/{id}`): line 380 `repository.findById(id)` then `setDeleted(true)`.
* `generateQrInfo` (`GET {base}/qr/{entityType}/{entityId}`): line 1640 `repository.findById(entityId)` (and persists QR data).
* `BaseControllerImpl` exposes all of them with no `@PreAuthorize`; `genQr` only checks `REGENERATE_QR` when
  `regenerate=true`.
* Module overrides repeat the pattern, e.g. HR `EmployeeService.findById` → `super.findById`, `EmployeeService.update`,
  `EmployeeLoanService.findById`, `PayrollPeriodService.update`, `PayslipService.update` all use `repository.findById`.
* Correct counter-example: `UnifiedDeliveryService.processPayment` (`~1292`) is tenant-scoped; `update-payment-pricing`
  (`~1085`) and `update-exchange-pricing` (`~1209`) are not.

**Concrete IDOR chains**
| Endpoint | Service | Repository call |
|---|---|---|
| `GET /api/hr/employees/fetch/{id}` | `EmployeeService.findById` → `BaseServiceImpl.findById` | `findByIdAndIsDeletedFalse(id)` |
| `PUT /api/hr/employees` | `EmployeeService.update` | `repository.findById(dto.getId())` |
| `DELETE /api/finance/<any>/remove/{id}` | `BaseServiceImpl.remove` | `deleteById(id)` |
| `DELETE /api/production/deliveries/delete/{id}` | `BaseServiceImpl.delete` | `findById(id)` |
| `PUT /api/production/deliveries/{id}/update-payment-pricing` | `UnifiedDeliveryService` (~1085) | `findById` |
| `GET /api/production/filtration/{id}` | `FiltrationService.getById` | `findByIdAndIsDeletedFalse` |
| `GET /api/<any>/qr/{type}/{id}?regenerate=false` | `BaseServiceImpl.generateQrInfo` | `findById(entityId)` |

**Impact:** cross-tenant confidentiality and integrity loss for all business data; irreversible hard deletes.

**Fix:** add tenant-aware finders to `BaseRepository` (`findByIdAndTenantIdAndIsDeletedFalse`) and use them in every
id-based base method (OOSMADMIN exception explicit); return 404 on mismatch. Add a default permission check in
`BaseControllerImpl` (e.g. `PermissionSupport.requireAction(resource, READ/CREATE/UPDATE/DELETE)` driven by the
controller's entity) so subclasses are secure by default. Consider disabling `remove` (hard delete) globally.
**Effort: M.**

---

### AUTHZ-03 — Module gating filter runs before the tenant is known
**Severity: High** — the "tenant has module X enabled" control is effectively disabled for all tenants; needs runtime
verification (static confidence high).

**Evidence**
* `modules/security/src/main/java/com/xdev/ooms/security/tenantmodule/TenantModuleApiAccessFilter.java:22,35-44`
  ```java
  @Order(50)
  ...
  if (SecurityUtils.isOosmAdmin()) { filterChain.doFilter(request, response); return; }
  UUID tenantId = TenantContext.getCurrentTenant();
  if (tenantId == null) { filterChain.doFilter(request, response); return; }
  ```
* `TenantFilter` is `@Component` without `@Order` → `Ordered.LOWEST_PRECEDENCE`, i.e. after order 50.
* Path rules only cover `/api/hr/`, `/api/finance/`, `/api/inventaire/`, `/api/ordreConditionement/`, `/api/production/`
  and three `/api/security/*` prefixes (`TenantModulePathResolver.java:21-36`); `/api/documents/`,
  `/api/security/company-profile/`, `/api/notifications/` are exempt (`:59-70`) and 68 rows (e.g. `/api/certifications`,
  `/api/expeditions`, `/api/types`, `/api/search`) match no rule.
* Authorities are filtered by enabled modules in the converter (`OosmJwtAuthenticationConverter.java:50-51`), but that
  only matters for handlers that actually check authorities (a small minority).

**Impact:** tenants use APIs of modules they have not subscribed to; the licensing / least-privilege boundary is absent.

**Fix:** give `TenantFilter` an explicit order lower than 50 (e.g. `@Order(10)`), or resolve the tenant from the JWT
inside the module filter; fail closed (403) when an authenticated non-OOSMADMIN request has no tenant; add rules for
unmatched prefixes. **Effort: S.**

---

### AUTHZ-04 — Role modification → OOSMADMIN privilege escalation
**Severity: Critical** — any authenticated user of any tenant can make themselves (or every tenant admin) platform
super-admin with one request.

**Evidence**
* `RoleService.update` (reached by inherited `PUT /api/security/role`) —
  `modules/security/src/main/java/com/xdev/ooms/security/role/service/RoleService.java:103-127`
  ```java
  Role role = roleRepository.findById(dto.getId())...;     // no tenant, no permission check
  if (dto.getRoleName() != null) role.setRoleName(dto.getRoleName());
  role.getPermissions().clear(); ...
  Set<Permission> target = resolvePermissionsFromDTOs(dto.getPermissions());
  ```
* Role name becomes an authority, upper-cased —
  `OosmJwtAuthenticationConverter.java:52-54`
  ```java
  authorities.add(new SimpleGrantedAuthority(roleName.toUpperCase()));
  ```
  and is reloaded from DB on every request (`getByUsernameWithFreshPermissions`, line 45).
* OOSMADMIN guard accepts that authority —
  `AdminUserController.java:21`, `AdminDashboardController.java:17`, `AdminSettingsController.java:43`,
  `CompanyProfileController.java:45,72,91,107,123`, `PermissionController.java:66,76,92`:
  `authentication.tokenAttributes['role'] == 'OOSMADMIN' or hasAnyAuthority('OOSMADMIN', 'ROLE_OOSMADMIN')`.
* In-method `SecurityUtils.isOosmAdmin()` normalises the JWT `role` claim (`SecurityUtils.java:100-104,129-131`:
  `toUpperCase().replace("ROLE_", "")`); the claim is refreshed from the DB role name by
  `POST /api/security/user/me/refresh-session` (`SecurityConfig.java:99` `.claim("role", user.getRole().getRoleName())`).
* `Role.roleName` is `unique=true` but case-sensitive, so `"oosmadmin"`, `"OosmAdmin"` or `"ROLE_OOSMADMIN"` do not
  collide with an existing `OOSMADMIN` role.
* The `ADMIN` role is **one global row shared by all tenants** (`CompanyProfileService.java:93-101`,
  `RoleService.findAll` adds it for every tenant at `:43-47`). Renaming it, or replacing its permissions, affects every
  tenant administrator at once.
* `POST /api/security/role` (inherited `save`) is also open and resolves any permission ids (`RoleService.java:74-77`).

**Attack:** user U (tenant A, any role R) → `PUT /api/security/role {"id":"<R>","roleName":"oosmadmin","permissions":[...all ids...]}`
→ next request carries authority `OOSMADMIN` → `GET /api/admin/users`, `/api/security/company-profile/{tenantId}/purge`, etc.
After `refresh-session` the JWT claim also normalises to `OOSMADMIN`, satisfying `isOosmAdmin()` double checks.

**Impact:** full platform compromise (all tenants, tenant purge, settings/secret rotation, user admin).

**Fix:** override `save/update/remove/delete` in `RoleService` with (a) `HABILITATION:ROLE:*` permission check,
(b) tenant ownership check (`role.tenantId == current`), (c) deny any change to system roles (`ADMIN`, `OOSMADMIN`) except
by OOSMADMIN, (d) reserve role names case-insensitively (`OOSMADMIN`, `ADMIN`, `ROLE_*`). Stop deriving privileged
authorities from free-text role names — use a separate immutable `systemRole` enum/flag. Make `ADMIN` per tenant or
immutable. **Effort: M.**

---

### AUTHZ-05 — Mass assignment through ModelMapper
**Severity: High** — lets users plant records into other tenants, move records between tenants, un-delete records and
forge audit fields; combined with AUTHZ-02 it amplifies every IDOR.

**Evidence**
* `BaseServiceImpl.save` `:242` `E entity = this.modelMapper.map(request, this.entityClass);` and `update` `:310`
  `this.modelMapper.map(request, existedEntity);` — the full DTO is copied, including `BaseDto` fields
  `tenantId`, `isDeleted`, `createdBy`, `createdDate`, `qrHex`, …
* `BaseEntity.java:123-136` only fills `tenantId` from context when null → client value is kept on create and update.
* Default `ModelMapper` (no `setSkipNullEnabled`, no field whitelist): nested DTOs (e.g. `role`, `supplier`) are mapped
  into the *managed* associated entity on update — the related row may be modified via dirty checking (needs verification).
* `UserService.addUser` `:230` `modelMapper.map(userDTO, OOSMUser.class)` takes `tenantId` and `role` from the request.

**Impact:** cross-tenant data injection (e.g. create an invoice/delivery in tenant B), record theft
(`PUT` with `tenantId` = own tenant pulls tenant B's record into A), restoring deleted data, audit falsification.

**Fix:** separate request DTOs without `tenantId/isDeleted/audit/id-on-create`; or configure ModelMapper
`typeMap.addMappings(m -> m.skip(BaseEntity::setTenantId) ...)`; always set `tenantId` server-side on create and assert
it unchanged on update. **Effort: M.**

---

### AUTHZ-06 — User management bypass and account takeover
**Severity: Critical** — any authenticated user can take over any account (including OOSMADMIN) or create users in
other tenants.

**Evidence**
* `UserController extends BaseControllerImpl<OOSMUser, OOSMUserDTO, OOSMUserOUTDTO>` (`UserController.java:33`);
  `UserService` does **not** override `save`, `update` or `remove` → inherited `POST /api/security/user`,
  `PUT /api/security/user`, `DELETE /api/security/user/remove/{id}` run the generic code (AUTHZ-02/05).
* `OOSMUserDTO` (the input DTO) contains `password`, `role`, `isLocked`, `email`, `username`, `tenantId`
  (`OOSMUserDTO.java:9-19` + `BaseDto`). `update` copies `password` verbatim onto the entity; the encoder is
  `BCryptPasswordEncoder` (`SecurityConfig.java:105-106`), so an attacker who sends a bcrypt hash they generated knows
  the new password.
* `UserService.delete` (`DELETE /api/security/user/delete/{id}`) `:371-390`: `userRepository.findById(id)` then locks and
  soft-deletes — no permission, no tenant check.
* `addUser` (`POST /api/security/user/addUser`) `:222,230`: only `assertCanCreateUser()`; `role` and `tenantId` come from
  the DTO → a tenant admin can create a user in tenant B, or with the OOSMADMIN role id.
* `updateUser` (`POST /api/security/user/updateUser/{id}`) `:271,277`: target is same-tenant-checked (good) but
  `roleRepository.findByIdAndIsDeletedFalse(userDTO.getRole().getId())` accepts any role (OOSMADMIN, other tenants' roles).

**Attack:** `PUT /api/security/user {"id":"<oosmadmin-user-id>","username":"root","password":"$2a$10$<attacker bcrypt>","email":"root@x","role":{"id":"<oosmadmin role>"} ...}`
→ log in as that user. User ids leak through AUTHZ-01, AUTHZ-15 or `GET /api/security/user/role/{roleName}`.
Note: default ModelMapper also writes nulls for omitted fields, so a partial body can blank fields (needs verification).

**Fix:** override `save`, `update`, `remove`, `delete` in `UserService` (throw 405 or route to `addUser/updateUser`);
never map `password`, `tenantId`, `isLocked` from client DTOs; in `addUser/updateUser` force `tenantId = current tenant`
and validate the role belongs to the tenant and is not a system role unless caller is OOSMADMIN. **Effort: S–M.**

---

### AUTHZ-07 — Company profile (tenant) endpoints open to all users
**Severity: High** — any user can modify any tenant's company profile (legal name, tax ids, IBAN/SWIFT printed on
invoices, logo) and list all tenants.

**Evidence**
* `PUT /api/security/company-profile/update` (`CompanyProfileController.java:60`) and inherited `PUT` →
  `CompanyProfileService.update` `:293-315`: `repository.findById(dto.getId())` then `applyDtoToEntity` — no
  `isOosmAdmin`, no "own tenant" check. (Creation `save` at `:80-83` *is* protected.)
* `findAll` `:187` and pageable `findAll` `:212` use `repository.findAllByIsDeletedFalse` (all tenants) →
  `GET /api/security/company-profile/fetchAll`, `/fetchAllPageable`.
* `GET /api/security/company-profile/by-tenant/{tenantId}` (`CompanyProfileController.java:155`) and `fetch/{id}`.
* Inherited `POST /api/security/company-profile` → `BaseServiceImpl.save` (unguarded create of a tenant row).
* All paths are module-gate exempt (`TenantModulePathResolver.java:66`).

**Impact:** payment fraud (swap the bank details that appear on other tenants' invoices), impersonation, full tenant
enumeration (names, tax ids, contacts).

**Fix:** `update`: allow OOSMADMIN or (tenant ADMIN and `dto.getId() == current tenant`); list endpoints: OOSMADMIN
only, otherwise return only the caller's profile; override/disable inherited `save`. **Effort: S.**

---

### AUTHZ-08 — Cross-tenant document generation
**Severity: High** — any user can download invoices, delivery notes, purchase orders and traceability reports of any
tenant given an id (ids available via AUTHZ-01).

**Evidence** — repository queries only filter by id:
* `OilSaleRepository.java:20-22`, `DeliveryRepository.java:28-30`, `OilTransactionRepository.java:26-28`,
  `ExpeditionRepository.java:21-23`, `ProjetRepository.java:18-20`:
  ```java
  WHERE s.id = :id AND s.isDeleted = false
  Optional<OilSale> findByIdForPdf(@Param("id") UUID id);
  ```
* Called by `OilSaleDocumentService.java:41,56`, `OilSaleInvoicePdfService.java:48`, `DeliveryCommercialPdfService.java:30`,
  `DeliveryInvoicePdfService.java:157`, `PaymentNotePdfGeneratorService.java:64`, `FormDeliveryDocumentService.java:46`,
  `OilTransactionDocumentService.java:37`, `ExpeditionDocumentService.java:57,68`, and `TransactionBillMapper.java:118` (finance).
* Controllers (`/api/documents/oil-sales/{id}/invoice|bon-commande|bon-livraison`, `/api/documents/deliveries/{id}/{type}`,
  `/api/documents/oil-transactions/{id}/{type}`, `/api/documents/expeditions/{id}`, `/api/documents/projects/{id}/traceability`)
  have no authorisation and are module-gate exempt.

**Impact:** disclosure of commercial documents (customers, prices, quantities) across tenants.

**Fix:** add `AND x.tenantId = :tenantId` to every `findByIdForPdf` and pass `TenantContext`; add a per-document
permission check. **Effort: S.**

---

### AUTHZ-09 — Payslip PDF / fetch across tenants
**Severity: High** — salary data (names, CNSS, pay) of other tenants' employees is retrievable by anyone holding the
`PAYSLIP:GEN_PDF` permission in their own tenant; the plain `fetch/{id}` requires nothing.

**Evidence**
* `PayslipController.java:42` `downloadPdf` → `HrPermissionSupport.requireAction("PAYSLIP", GEN_PDF)` (permission only) →
  `HrPayRollReadAdapter.java:25` `payslipRepository.findWithDetailsByIdAndIsDeletedFalse(payrollId)` →
  `PayslipRepository.java:37-39` `WHERE p.id = :id AND p.isDeleted = false`.
* `GET /api/hr/payslips/fetch/{id}` inherited (AUTHZ-02).

**Fix:** tenant-scope the repository query; for employees, also restrict to own payslip. **Effort: S.**

---

### AUTHZ-10 — Global fallback in QR/code resolution
**Severity: Medium** — reveals the existence and summary of other tenants' records by code; requires knowing/guessing
a QR hex code.

**Evidence** — `BaseServiceImpl.java:1825-1836`
```java
Optional<E> tenantMatch = repository.findByQrHexIgnoreCaseAndTenantIdAndIsDeletedFalse(normalizedCode, tenantId);
...
// 2) Try global case-insensitive search (as fallback or if no tenant)
Optional<E> globalMatch = repository.findByQrHexIgnoreCaseAndIsDeletedFalse(normalizedCode);
```
Reached by `GET {base}/resolve/{publicCode}`, `GET {base}/search/by-code` and `GET /api/search/by-code`
(`GlobalCodeSearchController.java:31-42`, iterates every contributor).

**Fix:** drop the global fallback for tenant users (keep only for OOSMADMIN / explicitly public codes). **Effort: S.**

---

### AUTHZ-11 — Other unscoped queries
**Severity: Medium** — cross-tenant listing through specific endpoints; narrower than AUTHZ-01.

**Evidence**
* `GET /api/production/filtration/status/{status}` → `FiltrationService.getFiltrationsByStatus` (`:442`) →
  `FiltrationOperationRepo.java:27` native `SELECT * FROM filtration_operation WHERE status = :status AND is_deleted = false`.
* `GET /api/production/deliveries/findForQuality` → `UnifiedDeliveryService.java:302-305` →
  `DeliveryRepository.java:84-91` (no tenant predicate).
* `DeliveryRepository.findAllDeliveryNumbers` (`:173`, used at `UnifiedDeliveryService.java:839`) — number allocation
  across all tenants (information leak + numbering collisions between tenants).
* `GET /api/security/role/all-with-user-count` → `RoleController.java:46` `userRepository.countUsersGroupedByRole()`
  (global counts, including the shared `ADMIN` role).
* Supplier / lot-number lookups in `UnifiedDeliveryService` (global, needs verification of each call site).

**Fix:** add tenant predicates; add a repository-level test that fails for any `@Query` lacking `tenant`. **Effort: S–M.**

---

### AUTHZ-12 — Global permission catalogue writable by anyone
**Severity: High** — permissions are global rows shared by all tenants; any user can create, rename or delete them,
breaking authorisation platform-wide or crafting names that match other checks.

**Evidence** — `PermissionController` protects only `catalog-status`, `catalog-spec`, `sync-catalog`
(`PermissionController.java:66,76,92`). Inherited `POST/PUT /api/security/permission`, `DELETE .../remove/{id}`,
`.../delete/{id}` go to `BaseServiceImpl` unguarded. `PermissionService.findAll` returns the full catalogue.

**Impact:** e.g. rename a permission held by the attacker's role to `HR:PAYSLIP:GEN_PDF` / `HABILITATION:OOSMUSER:CREATE`,
or delete permissions to deny service to all tenants.

**Fix:** make all permission writes OOSMADMIN-only (override in controller/service to 403). **Effort: S.**

---

### AUTHZ-13 — Converter does not revoke deleted/locked users or inactive tenants
**Severity: Medium** — access continues up to the 5-minute access-token TTL after lock/delete/tenant deactivation.

**Evidence** — `OosmJwtAuthenticationConverter.java:45-48`
```java
OOSMUser user = userService.getByUsernameWithFreshPermissions(username);
if (user == null || user.getRole() == null) {
    return token;           // still authenticated with raw token authorities
}
```
No `isLocked` / `enabled` / `isDeleted` / tenant-active check (these exist only in `UserSessionService.validateActiveUser`).

**Fix:** throw `BadCredentialsException`/`DisabledException` when the user is missing, locked, disabled, deleted or the
tenant is inactive. **Effort: S.**

---

### AUTHZ-14 — `refresh-session` accepts expired access tokens (needs verification)
**Severity: Medium** — a leaked access token can be turned into fresh tokens indefinitely (until the authorization row is
removed), defeating the 5-minute TTL; endpoint is anonymous.

**Evidence** — `/api/security/user/me/refresh-session` is `permitAll` (`AuthServerConfig.java:140-146`).
`UserSessionService.refreshSession` `:66-72` only does `authorizationService.findByToken(accessTokenValue, ACCESS_TOKEN)`
— no `authorization.getAccessToken().isExpired()` / `isInvalidated()` check before generating a new token (`:95-106`).
Locked users and inactive tenants *are* rejected (`validateActiveUser`, `:79`).

**Fix:** require a non-expired, non-invalidated access token (or use the refresh-token grant); move the endpoint to the
authenticated chain. **Effort: S.**

---

### AUTHZ-15 — Anonymous password-reset returns user data
**Severity: Medium** — unauthenticated enumeration of users by e-mail/phone, disclosing user id, tenant, role, names.

**Evidence** — `UserController.java:53-66` (`POST /api/security/user/auth/resetPassword`, anonymous) returns
`ResponseEntity.ok(user)`; `UserService.resetPassword` `:604,644` returns `modelMapper.map(user, OOSMUserOUTDTO.class)`
(`username, firstName, lastName, email, phoneNumber, role, tenantName, isLocked, …`). Locked accounts return a distinct 403.
The returned `id` feeds `/auth/validateResetCode/{userId}` and the IDORs above.

**Fix:** always return `202 {}` regardless of existence; rate-limit. **Effort: S.**

---

### AUTHZ-16 — No tenant propagation to async work
**Severity: Low** — current async tasks only send mail with explicit data, but any future repository call in these
paths would run with `TenantContext == null` (which in `findAll*` means "tenant IS NULL" or global, depending on caller).

**Evidence** — `UserService.java:469,483` `CompletableFuture.runAsync(() -> …)` on the common pool; no
`TaskDecorator`; `TenantCreatedEventListener` receives an explicit tenant id but `BaseEntity.@PrePersist` would take
`null` from context if provisioning code omits `setTenantId` (needs verification).

**Fix:** a `TaskDecorator`/wrapper that copies and clears `TenantContext`; pass tenant explicitly in listeners.
**Effort: S.**

---

### AUTHZ-17 — Authorisation is opt-in per handler
**Severity: Info** — root cause of AUTHZ-02/06/07/08/12. Only 20 `@PreAuthorize` occurrences; ~15 service files call
`PermissionSupport`/`SecurityUtils`. 1362 of 1426 endpoints have no guard beyond "authenticated" per the inventory.
Swagger UI is reachable by any authenticated user (api-docs JSON is OOSMADMIN-only).

**Fix:** deny-by-default: a `BaseControllerImpl`-level permission check derived from entity name + verb, and an
ArchUnit test that every `@RequestMapping` handler is either annotated or explicitly marked public. **Effort: L.**

---

## 3. Mass assignment summary
* Every `BaseControllerImpl` `POST ""` / `PUT ""` (AUTHZ-05): `tenantId`, `isDeleted`, audit fields, nested entities.
* Users (AUTHZ-06): `password`, `role`, `tenantId`, `isLocked`, `email`, `username`.
* Roles (AUTHZ-04): `roleName`, `permissions`.
* Company profile `update` uses an explicit field copy (`applyDtoToEntity`) and preserves `tenantId`/`createdBy` — good
  pattern, but unauthorised (AUTHZ-07).

## 4. Privilege escalation summary
1. Role rename/permission edit → OOSMADMIN (AUTHZ-04).
2. Global permission rename (AUTHZ-12).
3. User update with chosen password hash / role (AUTHZ-06).
4. `addUser`/`updateUser` with OOSMADMIN role id or foreign `tenantId` (AUTHZ-06, needs `HABILITATION:OOSMUSER:CREATE|UPDATE`).
5. OOSMADMIN-only endpoints themselves are correctly double-guarded (annotation + in-method), but the guard trusts a
   role *name* that users can edit.

---

## 5. Things to verify with active testing (local, two tenants)

Setup: tenant **A** with user `a_user` (role with no HABILITATION permissions) and `a_admin` (ADMIN); tenant **B** with
`b_admin` and some data (employee, payslip, oil sale, delivery); platform user `root` (OOSMADMIN). Obtain tokens via the
normal login. `<X-id>` = ids read as `root` or via test 1.

| # | Request (as) | Expected (secure) | Suspected (per code) |
|---|---|---|---|
| 1 | `POST /api/hr/employees/advanced/search` body `{"page":0,"size":50,"filterTenant":false}` (a_user) | only A rows / 403 | B employees returned (AUTHZ-01) |
| 2 | `POST /api/hr/employees/export/csv` body `{"searchData":{"filterTenant":false}}` (a_user) | A only / 403 | CSV with B rows |
| 3 | `GET /api/hr/employees/fetch/<B-employee-id>` (a_user) | 404 | 200 with B employee (AUTHZ-02) |
| 4 | `PUT /api/hr/employees` body `{"id":"<B-employee-id>","firstName":"x",...}` (a_user) | 404/403 | B employee modified |
| 5 | `DELETE /api/production/deliveries/remove/<B-delivery-id>` (a_user) | 404/403 | row hard-deleted in B |
| 6 | `POST /api/hr/employees` body with `"tenantId":"<B>"` (a_user) | created in A or 400 | created in B (AUTHZ-05) |
| 7 | `PUT /api/security/role` body `{"id":"<a_user role id>","roleName":"oosmadmin","permissions":[]}` (a_user) then `GET /api/admin/users` | 403 / 403 | 200, then admin endpoints 200 (AUTHZ-04) |
| 8 | `PUT /api/security/role` body `{"id":"<ADMIN role id>","roleName":"ADMIN","permissions":[]}` (a_user) | 403 | all tenant admins lose permissions |
| 9 | `PUT /api/security/user` body `{"id":"<root id>","username":"root","email":"root@x","password":"<bcrypt of 'Pwn3d!'>","role":{"id":"<OOSMADMIN role id>"}}` (a_user), then login as root/`Pwn3d!` | 403 | login succeeds (AUTHZ-06) |
| 10 | `DELETE /api/security/user/delete/<b_admin id>` (a_user) | 403 | b_admin locked + soft-deleted |
| 11 | `POST /api/security/user/addUser` body with `"tenantId":"<B>"` and `"role":{"id":"<ADMIN id>"}` (a_admin with CREATE) | 403/created in A | admin user created in B |
| 12 | `POST /api/security/user/updateUser/<a_user id>` body with `"role":{"id":"<OOSMADMIN role id>"}` (a_admin) | 400/403 | a_user becomes OOSMADMIN |
| 13 | `PUT /api/security/company-profile/update` body `{"id":"<B tenant id>","legalName":"x","iban":"ATTACKER"...}` (a_user) | 403 | B profile updated (AUTHZ-07) |
| 14 | `GET /api/security/company-profile/fetchAll` (a_user) | own only / 403 | all tenants |
| 15 | `GET /api/documents/oil-sales/<B-sale-id>/invoice` (a_user) | 404 | B invoice PDF (AUTHZ-08) |
| 16 | `GET /api/hr/payslips/<B-payslip-id>/pdf` (a_admin with GEN_PDF) | 404 | B payslip PDF (AUTHZ-09) |
| 17 | Disable HR for tenant A (as root), then `GET /api/hr/employees/fetchAll` (a_admin) | 403 module disabled | 200 (AUTHZ-03) |
| 18 | `GET /api/search/by-code?code=<B QR hex>` (a_user) | empty | B record summary (AUTHZ-10) |
| 19 | `GET /api/production/filtration/status/IN_PROGRESS` (a_user) | A only | A + B (AUTHZ-11) |
| 20 | `POST /api/security/permission` / `PUT /api/security/permission {"id":"<perm>","name":"HR:PAYSLIP:GEN_PDF"}` (a_user) | 403 | 200 (AUTHZ-12) |
| 21 | Lock a_user as a_admin, then immediately `GET /api/hr/employees/fetchAll` with a_user's existing token | 401 | 200 until expiry (AUTHZ-13) |
| 22 | Wait > 5 min, `POST /api/security/user/me/refresh-session` with a_user's **expired** access token, no other auth | 401 | new token issued (AUTHZ-14) |
| 23 | `POST /api/security/user/auth/resetPassword?identifier=b_admin@b.test` (anonymous) | 202 empty | 200 with user JSON (AUTHZ-15) |
| 24 | Send `X-Tenant-ID: <B>` on any request (a_user) | ignored | ignored (confirm header override stays disabled) |

---

## 6. What is done well
* Tenant id comes only from the signed JWT; the `X-Tenant-ID` override is disabled; `TenantContext.clear()` in `finally`.
* Authorities are rebuilt from the database on every request (permission revocation is immediate) and filtered by the
  tenant's enabled modules.
* OOSMADMIN endpoints (`/api/admin/**`, company create/modules/deactivate/reactivate/purge, permission catalogue sync)
  combine `@PreAuthorize` with an in-method `isOosmAdmin()` check.
* `UserService` overrides `findAll`/`findById`/`search` with permission + same-tenant checks; `updateUser` rejects
  self-edit and cross-tenant targets; `updateFcmToken` has a self check; `/me*` endpoints are self-scoped.
* Company profile `remove`/`delete` are disabled (405); `update` copies fields explicitly and preserves
  `tenantId`/`createdBy`.
* `UserNotificationService.markRead` checks ownership; `SupportTicketService` scopes by role.
* Day-import feature: `DayImportAccess` enforces full authorities, requires a tenant, and native queries use
  `tenant_id = ?`; Google Drive OAuth state is HMAC-signed with a 15-minute window; scheduled sync sets/clears the tenant.
* Password-reset codes are bcrypt-hashed with a 3-attempt limit; `refresh-session` re-validates locked users and
  inactive tenants.
* Actuator exposure is limited to `health`/`info`; OpenAPI JSON is OOSMADMIN-only.
