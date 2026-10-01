# Backend Authentication & Sessions — Security Review (read-only)

Scope: `modules/security` (Spring Authorization Server setup, custom `TOKEN` grant, refresh, session refresh, password reset / OTP, bootstrap), `shared-kernel` tenant/identity helpers, `app/src/main/resources/application.yml`, deployment descriptors (`render.yaml`, `.env.*.example`, `Dockerfile`, `docker/entrypoint.sh`) and local helper scripts in `scripts/`.

Method: static reading only. No app run, no network. Secrets are masked as first 4 chars + `…`.

Architecture recap (as implemented):

- Custom grant `grant_type=TOKEN` (username/email/phone + password) on `/oauth2/token`, public client `oosm-client` (`ClientAuthenticationMethod.NONE`, no secret).
- Access token = self-contained JWT, **HS256** with a single symmetric secret (`JwtKeyLoader`), TTL 5 min; opaque refresh token TTL 24 h (`ClientRegistry`).
- Authorizations (access + refresh token values) persisted in the DB by `JpaOAuth2AuthorizationService`.
- Resource server validates the JWT signature and expiry only; `OosmJwtAuthenticationConverter` reloads authorities from the DB by `sub`, but `role`/`tenantId` claims are still trusted in several places.
- Extra endpoint `/api/security/user/me/refresh-session` re-issues an access token from a stored access token.
- Password reset: 6-digit code emailed, BCrypt-hashed in `confirmation_code`, 3 attempts, 10 min expiry.

---

## Findings

### AUTHN-01 — Hard-coded default JWT signing secret; deployment does not force an override
**Severity: Critical**. If the default is used anywhere (and nothing stops that), anyone who has the repo can forge an HS256 token for any user, including the platform admin.

Evidence:
- `app/src/main/resources/application.yml:114`
  ```yaml
  secret: ${JWT_SECRET:X7kP…}        # 32-char literal default
  ```
- The **same literal** is committed in `.env.railway.example:27` (`JWT_SECRET=X7kP…`), so people who copy the example file end up using the default.
- `render.yaml` (web service `oosm-api`, lines 13–120) declares no `JWT_SECRET` key at all, so a Blueprint deploy silently falls back to the default unless someone sets it by hand in the dashboard.
- `modules/security/.../securityConfig/JwtKeyLoader.java:27-33`: the only check is `StringUtils.hasText(jwtSecret)`. There is no rejection of the known default, no minimum entropy check, no profile guard.
- `SecurityConfig.java:70-75`: the decoder is `NimbusJwtDecoder.withSecretKey(...)` with HS256. The same key signs and verifies. There is no issuer or audience validation.
- Key reuse: `application.yml:129`, `encryption-key: ${APP_SETTINGS_ENCRYPTION_KEY:${app.security.jwt.secret:…}}`. The AES-GCM key for secrets stored in `app_setting` (SMTP/Resend/FCM credentials) is derived from the JWT secret whenever `APP_SETTINGS_ENCRYPTION_KEY` is unset (`render.yaml` marks that key `sync: false`, so it may be empty).

Impact: forge `{"sub":"oosmAdmin","role":"OOSMADMIN","oosmUser":{"tenantId":…}}`. `OosmJwtAuthenticationConverter` loads that user's DB authorities from `sub`, `TenantFilter` trusts `oosmUser.tenantId`, and `@PreAuthorize("authentication.tokenAttributes['role'] == 'OOSMADMIN' …")` trusts the claim. The result is full cross-tenant platform-admin access and decryption of stored integration secrets. **Needs verification:** whether the production Render/Railway env actually sets `JWT_SECRET` and `APP_SETTINGS_ENCRYPTION_KEY`. The code defect exists either way.

Fix:
1. Remove the default (`${JWT_SECRET}`, no fallback). Fail startup if the value is missing, shorter than 32 random bytes, or equal to any known/example value.
2. Add `JWT_SECRET` and `APP_SETTINGS_ENCRYPTION_KEY` to `render.yaml` with `generateValue: true` (or `sync: false`). Scrub the literal from `.env.railway.example`.
3. **Rotate the secret in every environment now.** Assume it is compromised, since it is in git history.
4. Never derive the settings encryption key from the JWT key.
5. Medium term: move to RS256/ES256 with a keystore-backed `JWKSource` and a `kid`-based rotation set, and add issuer/audience validators.

Effort: S (config and guard) / M (asymmetric keys and rotation).

---

### AUTHN-02 — Well-known bootstrap admin with default password, reset (and unlocked) on every boot by default
**Severity: High** (Critical if `SECURITY_BOOTSTRAP_PASSWORD` is unset anywhere). A predictable platform-admin credential gets re-applied on every restart.

Evidence:
- `application.yml:118-123`
  ```yaml
  enabled:  ${SECURITY_BOOTSTRAP_ENABLED:true}
  username: ${SECURITY_BOOTSTRAP_USERNAME:oosmAdmin}
  password: ${SECURITY_BOOTSTRAP_PASSWORD:oosm…}
  reset-existing-admin-password: ${SECURITY_BOOTSTRAP_RESET_PASSWORD:true}
  ```
- `SecurityBootstrap.java:52` also defaults `reset-existing-admin-password` to `true`. At `:139-147`, on every startup:
  ```java
  if (resetExistingAdminPassword) {
      user.setPassword(passwordEncoder.encode(adminPassword));
      user.setLocked(false); user.setEnabled(true); user.setNewUser(false); ...
  ```
- `render.yaml:81-90` sets `SECURITY_BOOTSTRAP_ENABLED=true` and does not set `SECURITY_BOOTSTRAP_RESET_PASSWORD`, so the default `true` applies. `.env.render.example:87` and `.env.railway.example:33` also set it to `true`. `.env.railway.example:32` uses a weak placeholder (`chan…`).
- `LegacyOsmUserMigrationRunner.java:58-62` forces the admin username to the well-known `oosmAdmin`.

Impact:
- Admin password changes made in the UI are silently reverted on every deploy or restart (Render free tier restarts often).
- If an admin account is locked because of compromise, a restart unlocks it.
- If the env var is missing, the account comes back with the public default password.
- The username is predictable, which pairs badly with AUTHN-06 (no lockout).

Fix:
- Default `reset-existing-admin-password` to `false` and `bootstrap.enabled` to `false`.
- Remove the password default and require a strong value only for first creation. Force `isNewUser=true` so the admin must change the password on first login.
- Never auto-unlock.
- Log a loud warning if the bootstrap password equals a known default.

Effort: S.

---

### AUTHN-03 — Unauthenticated password-reset endpoint returns the full user object (PII leak + enumeration)
**Severity: High**. An anonymous caller who knows an email or phone number gets that user's ID, tenant ID, username, contact data, role, permissions, lock state and photo.

Evidence:
- `UserController.java:53-66` (public via `/api/security/user/auth/**`):
  ```java
  OOSMUserOUTDTO user = userService.resetPassword(identifier);
  return ResponseEntity.ok(user);
  ```
- `UserService.java:599-644` returns `modelMapper.map(user, OOSMUserOUTDTO.class)` for any matching email **or phone** (the phone branch sends nothing: `//TODO send to phone number`, around line 633).
- `OOSMUserOUTDTO` + `BaseDto` fields include: `id`, `tenantId`, `username`, `firstName`, `lastName`, `email`, `phoneNumber`, `isLocked`, `isNewUser`, `role` (a `RoleDTO` with its `permissions` set), `photoData` (base64), `qrHex`/`qrImageBase64`, `createdBy`, and more.
- Different outcomes: 200 + DTO (exists), 400 `"Invalid input"` (unknown), 403 `"Account is locked"` (locked; `UserController.java:68-71`, `UserService.java:606-609`).

Impact:
- Mass harvesting of usernames, phones, tenant IDs and role structure for any known email or phone.
- It also hands the attacker the `userId` that `/auth/validateResetCode/{userId}` and `/auth/updatePassword/{userId}` need. That removes the "must read the email" barrier from the ID half of the reset flow (see AUTHN-05).
- Enables targeted login attacks (usernames) and phishing.

Fix:
- Always return `202 Accepted` with an empty or generic body, whether or not the account exists or is locked.
- Deliver the `userId` (or better, an opaque reset token) only inside the email link.
- Switch the reset endpoints to a random opaque token instead of `{userId}` in the path.

Effort: S (backend) + S (frontend adjustment).

---

### AUTHN-04 — `/me/refresh-session` mints new access tokens from any stored access token, even expired, bypassing refresh-token lifetime and logout
**Severity: High**. A single leaked access token (for example via XSS from `localStorage`) can be turned into indefinite access. It is not bounded by the 5-minute or 24-hour TTLs.

Evidence:
- The endpoint is permit-all and bearer parsing is skipped: `AuthServerConfig.java:84` (public chain) and `:145`.
- `UserController.java:387-410` takes the raw `Authorization` header value and calls `userSessionService.refreshSession(accessToken)`.
- `UserSessionService.java:62-127`:
  ```java
  OAuth2Authorization authorization = authorizationService.findByToken(accessTokenValue, ACCESS_TOKEN);
  ... validateActiveUser(user);          // lock / tenant active only
  ... tokenGenerator.generate(...)       // new 5-min token
  authorizationService.save(...)         // becomes the new "current" token
  ```
  There is no check of `authorization.getAccessToken().isActive()` (expiry), no check that the refresh token is still valid, and no JWT signature/expiry validation. The token is only matched by value in the DB.
- The `oauth2_authorization` rows are never cleaned up. The only bulk delete is at tenant purge (`TenantLifecycleService.java:131`). Logout is a no-op (AUTHN-07).
- `validateActiveUser` (`:144-157`) checks `companyProfile.isActive()` but not `getDeleted()`. It also treats only `OOSMADMIN` as platform admin, while the login providers also accept the legacy `OSMADMIN`.

Impact: as long as the victim does not refresh again (for example after they log out, or when "remember me" leaves a session idle), an attacker holding the last-issued access token can chain `refresh-session` calls forever. This defeats the 24 h refresh TTL, logout, and password changes. Only account lock or delete, or a tenant deactivation, stops it.

Fix:
- Require a valid, unexpired JWT on this endpoint: move it out of the permit-all lists so the resource server validates it.
- Also require `authorization.getAccessToken().isActive()` **and** `authorization.getRefreshToken().isActive()`, and cap the issued token's `exp` at the refresh token's expiry.
- Alternatively, drop the endpoint and use the standard refresh-token grant.
- Add a scheduled purge of expired authorizations.

Effort: S–M.

---

### AUTHN-05 — Password-reset code can be brute-forced online (attempt counter resets on every re-request, no rate limit)
**Severity: Medium**. The 3-attempt limit applies per code, not per account or time window, and nothing throttles re-requests.

Evidence:
- 6-digit numeric code, `SecureRandom` (`UserService.java:990`): 900 000 possibilities, about 19.8 bits.
- Re-requesting a reset resets the counter: `UserService.java:618` (new code) and `:668` (`existedCode.setFailedAttempts(0); existedCode.setConsumedAt(null);`).
- Limit of 3: `UserService.java:707`, `:765`. `userId` is obtained from AUTHN-03.
- Sliding expiry: `ConfirmationCode.java:23` uses `getLastModifiedDate().plusMinutes(10)`, and `BaseEntity.onUpdate()` (`BaseEntity.java:132-138`) bumps `lastModifiedDate` on every failed-attempt update. Each wrong guess therefore extends the code's life.
- The configured `otp.*` block in `application.yml:91-100` (15 min expiry, 2 min resend delay, 3 attempts) is **not used** anywhere in the code.
- No rate limiting library or filter exists for auth endpoints (see AUTHN-06).

Impact: expected success is about 1 in 300 000 per 4 requests (reset + 3 guesses). That is roughly 1.2 M requests, which a scripted attacker can do in hours or days with no server-side limit. It is noisy (one email per round), but it leads to full account takeover, including of admins.

Fix:
- Enforce a per-account and per-IP resend cooldown (use the existing `otp.resend.delay`) and a daily cap on reset requests.
- Keep a cumulative failure counter per account/window that is not reset by re-issuing.
- Use a fixed expiry timestamp column set at issue time.
- Prefer a 128-bit opaque link token over a 6-digit code for email resets.

Effort: M.

---

### AUTHN-06 — No brute-force protection or rate limiting on login or other public auth endpoints
**Severity: Medium**. Unlimited online guessing against a predictable admin username, plus a cheap CPU/thread-exhaustion DoS.

Evidence:
- No throttling library in the build, and no attempt counter or lockout logic. The only rate limiter is `administration/settings/AdminSettingsRateLimiter.java`, which covers admin mail tests and secret rotation only.
- `CustomTokenGrantAuthenticationProvider.java:91-115` does lookup, then `isLocked` check, then `authenticationManager.authenticate(...)`. Failures are only logged; nothing increments or locks.
- Other public endpoints without limits:
  - `/auth/resetPassword`: BCrypt hash plus a synchronous email per call. The method is `@Transactional` and the mail is sent inline.
  - `/auth/validateResetCode/{userId}`
  - `/auth/updatePassword/{userId}`
  - `/auth/initial-password/{userId}` (`UserController.java:251`)
- `BCryptPasswordEncoder()` at strength 10 (`SecurityConfig.java:106`) combined with `TOMCAT_MAX_THREADS` 20 (`application.yml:9`, `render.yaml:63`). A few parallel login floods saturate the server.

Impact: credential stuffing or password spraying (8-char minimum policy, see AUTHN-11), email bombing of users via reset, and a trivial DoS on the free-tier instance.

Fix:
- Add per-IP and per-identifier throttling (Bucket4j or a reverse-proxy rule) on `/oauth2/token` and `/api/security/user/auth/**`.
- Add progressive delay or temporary lockout after N failures per account, with admin notification.
- Consider CAPTCHA after repeated failures.

Effort: M.

---

### AUTHN-07 — No server-side session revocation; refresh token is not rotated and has no reuse detection
**Severity: Medium**. A stolen refresh token stays valid for 24 h regardless of logout or password change.

Evidence:
- Logout is a no-op: `UserController.java:46-51`
  ```java
  @PostMapping("/auth/logout")
  public ResponseEntity<Void> logout() { return ResponseEntity.noContent().build(); }
  ```
- Password change (`UserService.changeOwnPassword`, around lines 842-861), password reset (`updatePassword`, around 741-793), initial password (`updateInitialPassword`, around 910-928) and admin temp password (`AdminUserService.issueTemporaryPassword`, lines 44-79) never delete the user's `oauth2_authorization` rows.
- Refresh is not rotated: `CustomRefreshTokenAuthenticationProvider.java:218-224` returns `null` as the refresh token, so the same refresh token is reused for its full 24 h. The `reuseRefreshTokens(false)` setting (`ClientRegistry.java:71`) is therefore ineffective, and there is no reuse or replay detection.
- Tokens are stored in plaintext (AUTHN-12). The frontend persists both tokens in `localStorage` for "remember me" (`osm-ms-fe/src/app/auth/services/tokenService.service.ts:38-39`).

What works: lock, soft-delete, and tenant deactivation or deletion do stop the **refresh grant** (`CustomRefreshTokenAuthenticationProvider.java:151-162`). Tenant purge deletes authorizations (`TenantLifecycleService.java:131`).

Impact: after a victim notices compromise and changes their password or logs out, the attacker keeps minting access tokens for up to 24 h (indefinitely with AUTHN-04).

Fix:
- Implement logout as revocation: delete the authorization identified by the presented refresh or access token.
- Revoke all authorizations for the principal on any password change/reset, admin temp-password issuance, role change, or lock.
- Rotate the refresh token on every use. Keep a token family; on reuse of an old refresh token, revoke the whole family.

Effort: M.

---

### AUTHN-08 — User enumeration via login, reset and reset-validation responses
**Severity: Medium**. Account existence and lock state can be learned without credentials.

Evidence:
- Login: an unknown user raises `OAuth2AuthenticationException(ACCESS_DENIED)` (`CustomTokenGrantAuthenticationProvider.java:93-96`), which yields HTTP 400 `{"error":"access_denied"}`. A wrong password raises `BadCredentialsException` from `authenticationManager.authenticate` (`:113`). That is not an `OAuth2AuthenticationException`, so it is expected to bypass the token endpoint's OAuth2 error handler and reach `CustomAuthenticationEntryPoint` (`:44-47`), producing HTTP 401 `INVALID_CREDENTIALS`. **Needs verification** by active test.
- Timing: no BCrypt comparison when the user does not exist (`:91-96`), so the response is measurably faster.
- Locked and inactive-tenant users are rejected *before* password verification (`:99-110`), so the same 400 is returned without knowing the password.
- Reset: 200 / 400 / 403 differ (AUTHN-03). `validateResetCode` distinguishes "Code is expired" from "Invalid input" (`UserController.java:101-109`).

Fix:
- Return one generic error (same status, body and approximate timing) for unknown user, wrong password, locked account and inactive tenant. Run a dummy BCrypt match when the user is missing.
- Check the password before revealing lock or tenant state.
- Map all `AuthenticationException`s in the grant provider to a single `OAuth2AuthenticationException(INVALID_GRANT)`.

Effort: S.

---

### AUTHN-09 — Sensitive data in logs (half of the reset code, identifiers, token prefixes)
**Severity: Low**. Only exploitable by someone with log access, but it halves reset-code entropy for them.

Evidence:
- The first 3 of 6 digits of the reset code are logged twice: `UserController.java:89` and `UserService.java:696`
  ```java
  "Code: " + code.substring(0, Math.min(3, code.length())) + "..."
  ```
- The first 10 chars of the refresh token are logged: `CustomRefreshTokenAuthenticationProvider.java:115`.
- Every login, reset and lookup logs the raw identifier (email/phone/username), for example `UserController.java:56`, `UserService.java:601`, and `CustomTokenGrantAuthenticationProvider.java:87,95`.
- `CustomAuthenticationEntryPoint.getClientIP` trusts client-supplied `X-Forwarded-For` for logged IPs (`:101-112`), so audit IPs are spoofable.

Fix:
- Never log any part of OTPs or tokens.
- Hash or mask identifiers in logs.
- Take the client IP from `request.getRemoteAddr()`, which is already proxy-corrected via `server.forward-headers-strategy: framework`.

Effort: S.

---

### AUTHN-10 — Authorization and tenant scoping trust token claims instead of the DB state
**Severity: Low** (with a strong secret; it becomes part of AUTHN-01 otherwise). Role or tenant changes, locks and tenant deactivation take up to 5 min to take effect, and the role claim can disagree with DB authorities.

Evidence:
- `TenantFilter.java:34-49` sets `TenantContext` from the `oosmUser.tenantId` claim.
- `SecurityUtils.getCurrentRole()` (`:94-113`) prefers the `role` claim. `isOosmAdmin()`, `hasElevatedAdminAccess()` and `assertSameTenant` build on it.
- 9 `@PreAuthorize("authentication.tokenAttributes['role'] == 'OOSMADMIN' or …")` sites, for example `AdminUserController.java:21`, `CompanyProfileController.java:45,72,91,107,123`, `PermissionController.java:32` and `AdminSettingsController.java:43`.
- `OosmJwtAuthenticationConverter.java:41-48`: if the `sub` user is not found (renamed or deleted), it returns the token **still authenticated** (with `scope` authorities), and it does not check `isLocked` or tenant status. Claim-based role checks then continue to pass for that token.

Fix:
- In the converter, reject (throw `BadCredentialsException`) when the user is missing, deleted or locked, or the tenant is inactive.
- Derive tenant and role from the loaded DB user and expose them via a custom principal. Stop reading `role` and `tenantId` from the raw JWT.
- Use `hasAuthority('OOSMADMIN')` only.
- Optionally add a per-user `tokenVersion` claim checked against the DB.

Effort: M.

---

### AUTHN-11 — Weak password policy and temporary-password handling
**Severity: Low**. Minimal policy, and credentials travel by email.

Evidence:
- `UpdatePasswordDTO.java:9-11`: `@NotBlank @Size(min = 8, max = 128)` is the only rule. There is no breached-password or common-password check and no complexity rule.
- `BCryptPasswordEncoder()` uses the default cost 10 (`SecurityConfig.java:106`). This is acceptable today; 12 is the more usual current choice.
- Temporary passwords are 8 alphanumeric chars (about 47 bits; `UserService.generateSecureCode`, around 958-983) and are **emailed in cleartext** (`dispatchConfirmationAsync`, `sendAdminPasswordReset`).
- When mail delivery is disabled, `addUser` returns the plaintext initial password in the API response (`UserService.java` around 243-246, `initialPassword` field).
- Username, email or phone changes by an admin silently reset the password and email a new one (`updateUser`, around 297-303).

Fix:
- Enforce 12+ chars or a passphrase, and a breached-password check (k-anonymity HIBP or a local list).
- Replace emailed passwords with one-time set-password links (reuse the reset-token mechanism).
- Consider `DelegatingPasswordEncoder` with bcrypt at cost 12 so the hash can be upgraded on login.

Effort: M.

---

### AUTHN-12 — OAuth2 tokens stored in plaintext; Java native deserialization of DB metadata
**Severity: Low**. Requires DB read (or write) access. It amplifies any SQL injection or backup leak.

Evidence:
- `JpaOAuth2AuthorizationService.java:164-221` stores `accessTokenValue` and `refreshTokenValue` verbatim. `AuthorizationRepository.findByRefreshTokenValue` looks up by plaintext value.
- `JpaOAuth2AuthorizationService.java:238-247` uses `new ObjectInputStream(byteIn).readObject()` on `*_metadata` / `attributes` columns. That is arbitrary-class deserialization of DB content. The Jackson `objectMapper` configured with the Spring Security modules (`:50-54`) is never used.

Fix:
- Store a SHA-256 hash of refresh tokens and look up by hash.
- Serialize metadata as JSON using the already-configured `ObjectMapper` (as `JdbcOAuth2AuthorizationService` does), or at least apply an `ObjectInputFilter` allow-list.

Effort: M.

---

### AUTHN-13 — CORS trusts shared-hosting wildcards with `allowCredentials=true`
**Severity: Low**. Auth uses bearer headers, not cookies, so an evil origin cannot steal tokens this way. But any site on `*.onrender.com` / `*.railway.app` is a trusted origin for credentialed requests.

Evidence: `DynamicCorsConfigurationSource.java:26-35` (`http://localhost:*`, `https://*.up.railway.app`, `https://*.railway.app`, `https://*.onrender.com`), `:63` `setAllowCredentials(true)`. `.env.render.example:35,78` and `.env.railway.render-db.example:45` add `https://*.onrender.com` again.

Fix: restrict to exact production origins, keep localhost patterns only in a dev profile, and drop `allowCredentials` if no cookies are used.

Effort: S.

---

### AUTHN-14 — Login identifier collisions can break login (DoS) — *needs verification*
**Severity: Low**. A tenant admin could lock another user out of login.

Evidence: `UserRepository.java:30-39` `findActiveByLoginIdentifier` returns `Optional<OOSMUser>` for `username = :input OR lower(email) = lower(:input) OR phoneNumber = :input`. `checkExistUser` / `checkUserToUpdate` (`UserService.java` around 320-363) only check same-field uniqueness. Creating a user whose **username** equals another user's **email** (or phone) should make that query return 2 rows and throw `IncorrectResultSizeDataAccessException` on the victim's login and refresh.

Fix: enforce cross-field uniqueness (username must not match any email or phone and vice versa, and restrict the username charset, e.g. no `@`, not all digits), or resolve the login identifier by type.

Effort: S.

---

### AUTHN-15 — Local helper scripts embed default credentials; stray token/script files are not ignored
**Severity: Low**. These are local-dev only, but they normalize weak known passwords that can leak into shared or prod DBs.

Evidence:
- `scripts/reset-local-user-password.cjs:5,9`: default admin password `osmA…`, DB `postgres` / `root…`. It also force-unlocks and clears `is_new_user`.
- `scripts/set-spring-test-password.cjs:4,7`: sets `oosmAdmin` to the public Spring Security test BCrypt vector (raw password `pass…`), DB `root…`.
- `scripts/setup-local-login.ps1:18-19,35-39` prints and sets `osmA…`.
- `scripts/verify-local-admin.cjs:5,13`: same defaults.
- Untracked and **not git-ignored**:
  - `scripts/_reset_sam_password.js`: copies the admin hash onto user `sam`; its comment reveals the admin default `oosm…`.
  - `scripts/_day_import_token.txt`: a JWT (`eyJr…`, 627 chars) that could be committed by accident.
- Other defaults: `docker-compose.yml:8`, `application.yml:33` (`DB_PASS` default `root…`), `AppSettingsEnvironmentPostProcessor.java:232` (falls back to `root…`), legacy `modules/*/resources/legacy/*/application.y*ml` (`MAIL_PASS:chan…`, not loaded at runtime).

Fix: read passwords from env or prompt (no defaults), refuse to run against non-localhost hosts, add `scripts/_*` and `*.token.txt` to `.gitignore`, and delete the stray token file.

Effort: S.

---

### AUTHN-16 — Registered-client token settings are only applied on first creation — *needs verification*
**Severity: Info**. Production TTLs may not match the code.

Evidence: `ClientRegistry.java:60-89`. When the client already exists, the `UPDATE` only resets secret, auth methods and grant types. `token_settings` (5 min / 24 h / reuse flag) are never updated, so older databases keep whatever was stored earlier.

Fix: also update `token_settings` on startup, or manage them via migration. Verify the current `oauth2_registered_client.token_settings` in prod.

Effort: S.

---

### AUTHN-17 — Miscellaneous hardening notes
**Severity: Info**.
- `OOSMUser.isEnabled()` always returns `true` (`OOSMUser.java:106-109`). The persisted `enabled` flag is ignored by Spring Security, so disabling relies solely on `isLocked` / `isDeleted`.
- The `/api/security/**` chain uses `SessionCreationPolicy.IF_REQUIRED` (`AuthServerConfig.java:151`) while everything else is `STATELESS`. No login form exists, so risk is low; make it `STATELESS` for consistency. CSRF is disabled on all chains, which is acceptable **only** because authentication is header-based. Keep it that way (never move tokens to cookies without re-enabling CSRF).
- The JWT carries PII (`email`, `firstName`, `lastName`) in `oosmUser` (`SecurityConfig.java:87-99`), and tokens live in `localStorage`. Minimize claims.
- New-user login rejection puts `username` and `userId` in the OAuth2 error (`CustomTokenGrantAuthenticationProvider.java:124`). This only happens after a correct password, so it is acceptable, but a dedicated `password_change_required` error code would be cleaner.
- No super-admin impersonation or "switch tenant" feature exists. The `X-Tenant-ID` header override in `TenantFilter.java:53-57` is commented out. Keep it that way.

---

## What is done well

- The resource server re-reads **authorities from the DB** on each request (`OosmJwtAuthenticationConverter` → `getByUsernameWithFreshPermissions`) instead of trusting a permissions claim. Tenant-module filtering is applied there too.
- Short access-token TTL (5 min). The refresh grant re-checks lock, soft-delete, tenant active and tenant deleted before issuing (`CustomRefreshTokenAuthenticationProvider.java:143-162`).
- Passwords use BCrypt. Reset codes are **stored BCrypt-hashed**, are single-use (`consumedAt`), have an attempt cap and use `SecureRandom`. Temp passwords also use `SecureRandom`.
- Changing your own password requires the old password. The initial-password flow requires the temporary password and the `isNewUser` state.
- `JwtKeyLoader` refuses to start with an empty secret. The symmetric key is not exposed through `/oauth2/jwks` (Nimbus `JWKSet.toString()` emits public keys only; confirm with the test below).
- Tenant purge deletes all authorizations for the tenant's users. Soft-deleted users have their identifiers mangled, so old tokens no longer resolve.
- `server.error.include-message/binding-errors: never`. Actuator exposure is limited to `health,info` with details `never`. OpenAPI docs are restricted to `OOSMADMIN`, and Render disables springdoc.
- Tenant ID is taken from the signed token, not from a client header (the header override is disabled).
- The Google Drive OAuth public callback uses an HMAC-signed, time-limited `state`.
- User-management actions enforce same-tenant checks and forbid self-management via the admin endpoint.

---

## Things to verify with active testing (local instance)

Assume `BASE=http://localhost:8084`, client `oosm-client`.

1. **Default JWT secret forgery (AUTHN-01).** Mint an HS256 JWT locally with the default secret from `application.yml`, `sub=oosmAdmin`, `role=OOSMADMIN`, `exp=now+300`. Call `GET $BASE/api/security/admin/...` (any OOSMADMIN endpoint) and `GET $BASE/api/security/user/me`. Expect 200 if the default is active. Repeat against staging/prod only with authorization, checking via the forged token's rejection.
2. **JWKS does not leak the HMAC key.** `GET $BASE/oauth2/jwks` should return `{"keys":[]}` with no `"k"` member. Also check `GET $BASE/.well-known/oauth-authorization-server`.
3. **Bootstrap reset (AUTHN-02).** Change the `oosmAdmin` password via `/me/change-password`, restart the app without `SECURITY_BOOTSTRAP_RESET_PASSWORD`, then log in with the old env/default password. Also lock `oosmAdmin` in the DB, restart, and check whether it is unlocked.
4. **Reset response leak (AUTHN-03).** `POST $BASE/api/security/user/auth/resetPassword?identifier=<known email>`, then `?identifier=<known phone>`, then `?identifier=nobody@x.y`. Compare status and body; confirm `id`, `tenantId`, `role.permissions`, `photoData` are present.
5. **refresh-session with an expired or old token (AUTHN-04).** Log in and save access token A. Wait more than 5 min (A expired). `POST $BASE/api/security/user/me/refresh-session` with `Authorization: Bearer A` should be rejected, but probably returns new token B. Chain again with B. Repeat after the refresh token has expired (set the TTL low in the DB) and after `POST /auth/logout`.
6. **Refresh token reuse and rotation (AUTHN-07).** Call `grant_type=refresh_token&refresh_token=R&client_id=oosm-client` twice, and after `/me/change-password`. Both should succeed with the same R (no rotation, no revocation). Confirm the response contains no new `refresh_token`.
7. **Reset brute-force loop (AUTHN-05).** Script: `resetPassword` → 3× wrong `validateResetCode/{id}?code=` → `resetPassword` again. Confirm the attempt counter resets and that no 429 or cooldown occurs. Also check whether an expired code becomes valid again after a failed attempt (sliding `lastModifiedDate`).
8. **Login throttling (AUTHN-06).** 50 rapid `grant_type=TOKEN&username=oosmAdmin&password=wrong` requests. Expect no lockout or 429. Measure latency and thread saturation with about 40 concurrent requests.
9. **Enumeration (AUTHN-08).** Compare status, body and timing for `username=<nonexistent>` vs `username=<existing>&password=wrong` vs a locked user on `/oauth2/token`.
10. **Stale claims (AUTHN-10).** Log in as a tenant ADMIN. In another session, demote the user or lock them. Within 5 min, call a `hasElevatedAdminAccess`-guarded user-management endpoint with the old token. Also rename the user and retry.
11. **Identifier collision (AUTHN-14).** As tenant admin, create user X with `username` = another user's email, then try to log in as that other user by email.
12. **Registered client TTLs (AUTHN-16).** `SELECT token_settings FROM oauth2_registered_client WHERE client_id='oosm-client';` in each environment.
13. **Logs (AUTHN-09).** Trigger `validateResetCode` and grep logs for `Code: ` to confirm 3-digit prefixes are written.
14. **CORS (AUTHN-13).** `OPTIONS /api/security/user/me` with `Origin: https://evil-attacker.onrender.com` should echo `Access-Control-Allow-Origin` and `Allow-Credentials: true`.
