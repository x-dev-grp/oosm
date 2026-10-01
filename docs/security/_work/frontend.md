# Frontend security review — `osm-ms-fe` (Angular 19)

- Scope: `F:/osm-ms-fe` working tree (read-only), plus targeted cross-checks in the backend `F:/oosm` where a frontend behaviour depends on it.
- Method: static review only, with no network requests and no builds.
- Secret handling: no real secret values were found. Where values would have been shown, only the first 4 characters are given.

## Summary

| ID | Severity | Title |
|----|----------|-------|
| FE-01 | High | Stored XSS in the QR "print" popups (`document.write` of unescaped entity names and codes into a same-origin window) |
| FE-02 | High | No Content-Security-Policy or other security headers in nginx |
| FE-03 | Medium | Access and refresh tokens are readable by JavaScript (localStorage for 30 days with "remember me", otherwise sessionStorage) |
| FE-04 | Medium | Logout is client-side only: the refresh token is not revoked (the backend `/auth/logout` is a no-op and the frontend never calls it) and the FCM device is not unregistered |
| FE-05 | Medium (needs verification) | ApexCharts tooltip title is set with `innerHTML` from user-controlled category labels |
| FE-06 | Low | The auth interceptor attaches the Bearer token and `X-Tenant-Id` to every `HttpClient` request, whatever the host |
| FE-07 | Low | nginx proxy: `X-Forwarded-Proto $scheme` overwrites Render's `https`; client-supplied `X-Forwarded-For` is appended; the backend uses `forward-headers-strategy: framework` |
| FE-08 | Low | Push-notification click navigates to a server-supplied `webRoute` without scheme/origin validation (`window.location.href`, `clients.openWindow`) |
| FE-09 | Low | Tenant data stays in localStorage after logout (app parameters, cash-register opening balances, selected operation, saved username) |
| FE-10 | Low | Swagger UI (admin) uses `persistAuthorization: true` and adds the token to every request it makes |
| FE-11 | Low | nginx: `server_tokens` not disabled, no dotfile deny, `/actuator/` publicly proxied |
| FE-12 | Low | The Firebase messaging service worker loads unpinned-integrity third-party scripts from gstatic (11.6.0, while the app bundles firebase ^12) |
| FE-13 | Info | Route guards are UI-only. List of admin and permission-gated API paths the backend must enforce |
| FE-14 | Info | Service worker caches no API data (`dataGroups: []`), but it is not reset on logout |
| FE-15 | Info | Repo hygiene: stray files (`docs.html`, `build_log.txt`, `.idea/shelf/*.patch`, `WhatsApp Image…jpeg`), unused dependencies, `npm ci --force` |
| FE-16 | Info | Firebase config: the committed files are empty; no service-account or FCM server keys in the repo |
| FE-17 | Info | Dev server `disableHostCheck: true` + `host: 0.0.0.0` |

---

## FE-01 — Stored XSS in QR "print" popups

- **Severity: High.** Any user who can edit an article or product name can run script in the app origin of another user who clicks "Print QR". That script can read their tokens (FE-03).
- **Evidence:**
  - `src/app/stock/components/article/article-detail/article-detail.component.ts:332-346`
    ```ts
    <h2>Article ${this.article.nom}</h2> ...
    const printWindow = window.open('', '_blank', 'width=600,height=600');
    printWindow?.document.write(`... <title>QR Code - Article ${this.article.nom}</title> ... <body>${printContent}</body>`)
    ```
  - `src/app/stock/components/sku/sku-detail/sku-detail.component.ts:273-290`: `<h2>Produit ${productName}</h2>`, where `productName = finalProductDisplayName(this.sku)` is free text.
  - `src/app/projet/pages/projets/projet-detail/projet-detail.component.ts:259-292`: `${projectCode}`, `${manualCode}`, and `<img src="${qrImage}">` (attribute injection is possible if `qrImage` is not a data URL).
  - `src/app/OF/components/of/of-detail/of-detail.component.ts:168-189`: `${this.of.code}`, `${this.of.publicCode}`.
  - `src/app/shared/components/qr-panel/qr-panel.component.ts:228-249`: `${this.publicCode}`.
  - `src/app/shared/components/qr-dialog/qr-dialog.component.ts:219-242`: `${this.data.qrText}`, `${this.data.payloadType}`.
- **Impact:** `window.open('')` creates an `about:blank` document that inherits the opener's origin. HTML written with `document.write` runs with full access to `window.opener` and the app origin's `localStorage`/`sessionStorage`. A payload such as `<img src=x onerror=...>` in an article name therefore steals access and refresh tokens (session hijack for up to 30 days) and can call any API as the victim. Angular's template sanitization does not apply here. This is the only real DOM-XSS sink found: there are no `[innerHTML]`, `bypassSecurityTrust*` or `insertAdjacentHTML` usages in the app.
- **Recommended fix:**
  - Build the print document with DOM APIs (`createElement` + `textContent`), or HTML-escape every interpolated value with a shared `escapeHtml()` helper.
  - Validate that `qrImageBase64` matches `^[A-Za-z0-9+/=]+$` and `qrImage` starts with `data:image/png;base64,`.
  - Better still, reuse one print component or `ngx-print` (already a dependency), which prints Angular-rendered, sanitized DOM.
  - Keep a CSP (FE-02) as defence in depth. Note that an `about:blank` popup inherits the CSP of its opener.
- **Effort:** S (6 call sites, one shared helper).

## FE-02 — No Content-Security-Policy or other security headers

- **Severity: High.** With no CSP, any XSS (FE-01, FE-05) leads straight to token theft. With no framing protection, clickjacking of admin actions is possible.
- **Evidence:** `nginx.conf.template` (the runtime config used by `Dockerfile`, lines 51-59) and `nginx.conf` (used by `Dockerfile.nginx`) contain no `add_header` for `Content-Security-Policy`, `X-Frame-Options`, `X-Content-Type-Options`, `Referrer-Policy`, `Permissions-Policy` or `Strict-Transport-Security`. Their only `add_header` directives set `Cache-Control`/`Content-Type`. There is no `<meta http-equiv="Content-Security-Policy">` in `src/index.html`.
- **Impact:**
  - XSS payloads can exfiltrate data to any host.
  - The app can be framed, allowing clickjacking of admin actions such as issuing a temporary password or rotating a secret.
  - MIME sniffing is possible on user downloads.
  - Full URLs, including IDs, can leak via `Referer` to Google Fonts and gstatic.
- **HSTS:** Render terminates TLS. Whether Render adds HSTS by default **needs verification** (check the response headers in production).
- **Recommended fix:** Add these at `server` level. Nginx does not inherit `add_header` into a `location` that defines its own `add_header`, so repeat them there or use an `include` snippet. Use `always` so the headers are also sent on errors.
  ```nginx
  server_tokens off;
  add_header Content-Security-Policy "default-src 'self'; script-src 'self' 'sha256-<inline-lang-script>' 'sha256-<window.global-script>' https://www.gstatic.com; style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; font-src 'self' https://fonts.gstatic.com data:; img-src 'self' data: blob:; connect-src 'self' https://*.googleapis.com https://fcmregistrations.googleapis.com https://firebaseinstallations.googleapis.com; worker-src 'self'; frame-ancestors 'none'; base-uri 'self'; form-action 'self'; object-src 'none'" always;
  add_header X-Frame-Options "DENY" always;
  add_header X-Content-Type-Options "nosniff" always;
  add_header Referrer-Policy "strict-origin-when-cross-origin" always;
  add_header Permissions-Policy "camera=(self), microphone=(), geolocation=(), payment=()" always;
  add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
  ```
  Notes on the CSP:
  - Allow `camera=(self)` only if QR scanning uses the camera.
  - The two inline scripts in `index.html` (lines 129-146) need hashes, or move them to a file.
  - ApexCharts and Angular Material need `style-src 'unsafe-inline'`.
  - Start with `Content-Security-Policy-Report-Only` and tighten from the reports.
- **Effort:** M (the headers themselves are S; tuning the CSP for Firebase, Google Fonts and Swagger UI takes testing).

## FE-03 — Tokens readable by JavaScript

- **Severity: Medium.** This is standard for SPAs, but combined with FE-01/FE-02 a single XSS yields a 30-day refresh token.
- **Evidence:** `src/app/auth/services/tokenService.service.ts`
  - Lines 30-42: with remember-me, `localStorage.setItem('auth_token_remember', accessToken)` and `localStorage.setItem('auth_refresh_token_remember', refreshToken)`.
  - Lines 46-48: without remember-me, the same tokens go to `sessionStorage`.
  - Line 25: `REMEMBER_ME_TTL_MS = 30 * 24 * 60 * 60 * 1000`. This expiry is enforced **only client-side**.
  - Login is a password grant from the browser with a public client: `authentication.service.ts:489-499` sends `grant_type=TOKEN`, `client_id=oosm-client`, `username`, `password`.
- **Impact:** Any script running in the origin can read and exfiltrate the refresh token and mint new access tokens until the server-side refresh-token lifetime ends. The 30-day remember-me window is not a server control.
- **Recommended fix:**
  - Short term: fix FE-01/FE-02, keep access tokens short-lived, and make sure the backend rotates refresh tokens with reuse detection, with a server-side absolute lifetime that matches the UI's 30 days. **Needs verification** in the Spring Authorization Server config.
  - Medium term: move the refresh token to an `HttpOnly; Secure; SameSite=Strict` cookie scoped to `/oauth2/token` (a backend-for-frontend or cookie-based refresh), and keep only the access token in memory.
- **Effort:** S for server TTL/rotation; L for cookie-based refresh.

## FE-04 — Logout does not revoke tokens or unregister push

- **Severity: Medium.** On shared factory or office devices, and after a token leak, "logout" gives a false sense of security.
- **Evidence:**
  - `src/app/auth/services/authentication.service.ts:533-568`: `logout()` clears storage and navigates away. It makes no HTTP call and no FCM `deleteToken`.
  - A search for `auth/logout` in `src/app` finds nothing.
  - Backend `modules/security/.../UserController.java:46-51`:
    ```java
    @PostMapping("/auth/logout")
    public ResponseEntity<Void> logout() {
        // ... Revocation of refresh tokens is optional and best-effort elsewhere.
        return ResponseEntity.noContent().build();
    }
    ```
  - `push-notification.service.ts:112-132` registers the FCM token with the user, and nothing ever clears it.
- **Impact:**
  - A copied refresh token stays valid after logout.
  - The browser keeps receiving the previous user's push notifications (title and recap text) after logout or when another person logs in on the same device, until someone else overwrites the FCM token.
- **Recommended fix:**
  - Backend: implement revocation, either `/oauth2/revoke` (Spring Authorization Server supports RFC 7009) or have `/auth/logout` remove the `OAuth2Authorization`, and clear or detach the FCM token for that device.
  - Frontend: in `logout()`, call revocation with the refresh token first (fire-and-forget), then `deleteToken(messaging)`, then clear storage.
- **Effort:** M.

## FE-05 — ApexCharts tooltip title rendered via `innerHTML` (needs verification)

- **Severity: Medium.** A stored-XSS path through a third-party library. It is partly mitigated by label truncation on the BOM report.
- **Evidence:**
  - `node_modules/apexcharts/src/modules/tooltip/Labels.js:224`: `ttCtx.tooltipTitle.innerHTML = xVal`. It is not sanitized, whereas `seriesName` on line 234 does go through `Utilities.sanitizeDom`.
  - `src/app/analytics/components/Rapport-bom/rapport-bom.component.ts:148`: `categories: sortedData.map((item) => this.chartLabel(item.materialName))`. `chartLabel` truncates to 18 characters (lines 158-161), which blocks most payloads.
  - `src/app/analytics/components/Rendement-OF/Rendement-OF.component.ts:129`: `categories: sortedData.map((item) => item.ofCode)`, with no truncation.
  - `src/app/analytics/components/Rapport-Filtration/Rapport-Filtration.component.ts:137`: falls back to `operationLabel`, with no truncation.
  - `apexcharts` is pinned to `3.24.0` (`package.json:56`).
- **Impact:** If `ofCode`, `operationLabel` or a similar category label can hold user-supplied HTML, hovering the chart runs script in the app origin. Whether those fields are user-editable **needs verification**. `ofCode` may be server-generated.
- **Recommended fix:** HTML-escape category labels before passing them to ApexCharts (`xaxis.categories`), or set `tooltip.x.formatter` to return escaped text. Upgrade ApexCharts to a current 3.x/4.x release and re-check.
- **Effort:** S.

## FE-06 — Token attached to any host

- **Severity: Low.** Currently latent: every `HttpClient` URL in `src/app` is relative (`environment.apiUrl = ''`). The Firebase SDK uses `fetch`, so it is not affected.
- **Evidence:** `src/app/interceptors/auth.interceptor.ts:18-34`: for every non-excluded request, `headers = request.headers.set('Authorization', \`Bearer ${token}\`)` plus `X-Tenant-Id`. There is no check that `request.url` is same-origin or under `/api`. `error.interceptor.ts:131-141` (`addToken`) does the same on retry.
- **Impact:** The first `HttpClient` call to an absolute third-party URL, or to a URL taken from data (file URLs, webhooks, a configurable `apiUrl`), would leak the Bearer token. The exclusion check `url.includes('assets/')` also strips the token from any API path that contains `assets/`.
- **Recommended fix:** Attach the token only when `new URL(req.url, location.origin).origin === location.origin` (or matches the configured API base) and the path starts with `/api/`, `/oauth2/`, or `/v3/api-docs`. Match exclusions on the path prefix, not with `includes`.
- **Effort:** S.

## FE-07 — nginx proxy forwarding headers

- **Severity: Low.** The backend trusts forwarded headers, and the frontend proxy forwards partly wrong or client-controllable values.
- **Evidence:** `nginx.conf.template:24-68`, repeated for `/api/`, `/oauth2/`, `/.well-known/`, `/jwks`, `/actuator/`:
  ```nginx
  proxy_set_header Host $proxy_host;
  proxy_set_header X-Real-IP $remote_addr;
  proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
  proxy_set_header X-Forwarded-Proto $scheme;
  ```
  The backend has `app/src/main/resources/application.yml:4` `forward-headers-strategy: framework`.
- **Impact:**
  - Inside the Render container, `$scheme` is `http`, because TLS ends at Render's edge. This overwrites the edge's `X-Forwarded-Proto: https`, so the backend may build `http://` URLs for OAuth issuer, redirect `Location`s and links, or refuse "secure only" logic.
  - `$proxy_add_x_forwarded_for` appends to whatever the client sent. If the backend uses XFF for rate limiting, lockout or audit IPs, the client can spoof it.
  - `$remote_addr` is Render's proxy, not the user.
  - `Host` is the backend hostname, and no `X-Forwarded-Host` is sent.
- **Recommended fix:**
  - Forward `X-Forwarded-Proto $http_x_forwarded_proto` (Render sets it), or hard-code `https`.
  - Use `set_real_ip_from` with Render ranges plus `real_ip_header X-Forwarded-For`, then send `X-Forwarded-For $remote_addr`.
  - Add `X-Forwarded-Host $host` if the backend must know the public host.
  - Backend: make rate limiting use a trusted client-IP resolution. **Needs verification**.
- **Effort:** S.

## FE-08 — Unvalidated navigation target from notifications

- **Severity: Low.** The route comes from backend-generated notifications or FCM data, not directly from users, but there is no defence in depth.
- **Evidence:**
  - `src/app/shared/services/push-notification.service.ts:103-107`: `notification.onclick = () => { ... window.location.href = route; }`
  - `src/firebase-messaging-sw.js:60-73`: `client.navigate(route)` and `clients.openWindow(route || '/')`.
  - Safer: `notification.service.ts:163-165` and `toolbar-right.component.ts:170` use `router.navigateByUrl(...)`, which cannot leave the app.
  - `reception-import-wizard.component.ts:273`: `window.location.href = url` with the Drive OAuth URL returned by the backend (acceptable).
- **Impact:** If a notification's `webRoute` can be influenced (for example, built from user-entered data or sent by anyone with FCM send rights), a `javascript:` route in `window.location.href` becomes XSS, and an absolute URL becomes an open redirect or phishing.
- **Recommended fix:** Accept only same-origin relative paths: `if (typeof route === 'string' && route.startsWith('/') && !route.startsWith('//'))`. Otherwise fall back to `/`. In the app, use `router.navigateByUrl`.
- **Effort:** S.

## FE-09 — Tenant data left in localStorage after logout

- **Severity: Low.** Business data persists on shared devices.
- **Evidence:** `logout()` only calls `permissionService.clearCache()`, `CompanyProfileService.clearCache()` and `tokenService.clearTokens()`. These keys remain:
  - `osm_app_parameters`: tenant parameters (`AppParameterService.ts:12,92,119`).
  - Cash-register opening balances (`finance/cash-register/cash-register.component.ts:238-259`).
  - `OSM_RECEPTION_SELECTED_OP` (`reception/olive-reception/olive-reception.component.ts:27-32`).
  - Import-rules acceptance (`reception-import-wizard.component.ts:73`).
  - `savedUsername`, which is kept by design (`tokenService.service.ts:21-22`).
  - The Swagger UI `authorized` key (FE-10).
- **Impact:** The next person on the device, or malware reading the browser profile, can see the previous tenant's parameters and cash figures. The saved username helps targeted password guessing.
- **Recommended fix:** Namespace tenant or user keys (for example with an `oosm:<tenantId>:` prefix) and remove them all in `logout()`. Store the username only when remember-me is on (current behaviour), and consider masking it.
- **Effort:** S.

## FE-10 — Swagger UI persists authorization and attaches the token everywhere

- **Severity: Low.** The page is admin-only and gated by the `swagger` feature flag.
- **Evidence:** `src/app/administration/admin-api-docs/admin-api-docs.component.ts:99-123`: `persistAuthorization: true`, and the `requestInterceptor` sets `Authorization: Bearer <token>` on **every** Swagger request, including "Try it out" calls against any `servers[]` URL in the spec.
- **Impact:**
  - If an admin uses Swagger's "Authorize" dialog, the value is saved under the localStorage key `authorized` and survives logout.
  - If a spec ever lists a non-app `servers` URL, the admin token goes there.
- **Recommended fix:** Set `persistAuthorization: false`. In `requestInterceptor`, add the header only when `new URL(request.url, location.origin).origin === location.origin`.
- **Effort:** S.

## FE-11 — nginx hardening gaps

- **Severity: Low.**
- **Evidence:** `nginx.conf.template`:
  - No `server_tokens off;`, so the nginx version appears in the `Server` header and error pages.
  - No `location ~ /\. { deny all; }`. The build copies `src/assets/.gitkeep`, so `/assets/.gitkeep` is served.
  - `location /actuator/ { proxy_pass ${BACKEND_PROXY_URL}; ... }` (lines 61-68) exposes backend actuator paths on the public frontend host.
- **Mitigation already in place:** The backend exposes only `health,info` with `show-details: never` (`app/src/main/resources/application.yml:158-171`). The legacy module YAMLs with `include: *` under `modules/*/src/main/resources/legacy/` must stay unused. **Needs verification** that they are not loaded by any profile.
- **Recommended fix:**
  - Add `server_tokens off;` and a dotfile deny.
  - Proxy only `location = /actuator/health` if the frontend needs it (the health check uses nginx `/health` anyway), or drop the `/actuator/` block.
  - Source maps are **off** in production (`angular.json:102`), and CI and Docker build with `-c=production`, so no action there.
- **Effort:** S.

## FE-12 — Third-party scripts in the FCM service worker

- **Severity: Low.** This is a supply-chain and consistency risk.
- **Evidence:** `src/firebase-messaging-sw.js:2-3`: `importScripts('https://www.gstatic.com/firebasejs/11.6.0/firebase-app-compat.js')` plus the `messaging-compat` script. `package.json:64` has `"firebase": "^12.18.0"`.
- **Impact:**
  - `importScripts` cannot use SRI, so whatever gstatic serves runs in a service worker on the app origin.
  - The SW version differs from the app's SDK major version.
- **Recommended fix:** Bundle the SW with the modular SDK (`firebase/messaging/sw`) through a small build step, or self-host pinned copies under `/assets/`. Add `https://www.gstatic.com` to CSP `script-src` only while the CDN is still used.
- **Effort:** M.

## FE-13 — Frontend-only protections and API paths the backend must enforce

- **Severity: Info.** All guards run on decoded JWT claims or session data in the browser and are bypassable. Security relies entirely on the backend.
- **Guards:**
  - `interceptors/guards/admin-auth.guard.ts`: `user?.role === Role.OosmAdmin`.
  - `interceptors/guards/permission.guard.ts`: `allPermissionGuard`, `anyPermissionGuard`, `moduleGuard`. `allPermissionGuard` and `anyPermissionGuard` also pass when `authService.isAdmin()` is true (tenant Admin), even if the tenant module is disabled. `hasPermission` does check the module for Admin, but the guard's `|| isAdmin()` short-circuits it. The backend must enforce tenant module enablement for tenant Admins.
  - `interceptors/guards/role.guard.ts`, `auth.guard.ts`, `auth-login.guard.ts`, `auth-update-password.guard.ts`, `mill-planning-enabled.guard.ts`: UI flow only.
- **Admin-only UI (`/dashboard/administration/**`, `AdminAuthGuard`).** The backend must require the platform-admin role (not just authentication) on:
  - `POST /api/security/admin/users/osm-admin`: create platform admin (`administration/services/admin-user.service.ts:14`).
  - `POST /api/security/admin/users/{userId}/issue-temporary-password`: `admin-user.service.ts:18`.
  - `GET /api/security/admin/dashboard/stats`: cross-tenant stats.
  - `POST /api/security/company-profile/save`: create company or tenant (`shared/services/add-company-user.service.ts:15`).
  - `GET /api/security/company-profile/by-tenant/{tenantId}`: cross-tenant read (`admin-company-view.component.ts:97`).
  - `GET|PUT /api/admin/settings[/{key}]`, `POST /api/admin/settings/{key}/rotate-secret`, `POST /api/admin/settings/reload`, `POST /api/admin/settings/mail/test`, `POST /api/admin/settings/notifications/test`, `GET /api/admin/settings/audit`: `admin-settings.service.ts:20-61`.
    - Also verify that `GET` returns **masked** values for `sensitive: true` settings. The model has `value: string | null` plus `sensitive`.
    - `GET /api/admin/settings/status` is also called from `admin-api-docs`.
  - `GET /api/security/permission/catalog-status`, `GET /api/security/permission/catalog-spec`, `POST /api/security/permission/sync-catalog`: `permission-catalog-admin.service.ts`.
  - `GET /v3/api-docs/{group}`: Swagger groups. Must be admin-only or disabled in production.
  - `GET|PATCH /api/security/support-tickets[/{id}]?scope=...`: the `scope` parameter must not let tenants read other tenants' tickets.
- **Checked and OK in the backend:**
  - `POST /api/security/user/register-device` compares the body `userId` with the authenticated user (`UserService.updateFcmToken`, lines 1096-1100).
  - The `X-Tenant-ID` header override is commented out in `TenantFilter.java:53-54`, so the frontend's `X-Tenant-Id` header is ignored.
- **Public backend paths the frontend calls without a token** (`/api/security/user/auth/**`, public in `AuthServerConfig.java:83,144`): `resetPassword`, `validateResetCode/{userId}`, `updatePassword/{userId}`, `initial-password/{userId}`.
  - `initial-password` requires the old password and `isNewUser`, so it is effectively a password-guessing oracle keyed by user UUID.
  - The backend must rate-limit and lock out on these endpoints. **Needs verification**.
- **Effort:** n/a (backend verification).

## FE-14 — Service worker and logout

- **Severity: Info.** No tenant API data is cached.
- **Evidence:**
  - `src/ngsw-config.json:51` has `"dataGroups": []`. This is the file used by `angular.json:72,109`; the root `ngsw-config.json` is an unused duplicate.
  - `navigationUrls` excludes `oauth2/token` and `/api/security/user/auth/`.
  - Asset groups cache `/assets/**`, which contains only static files (i18n, images, user-guide PDFs, empty `firebase-config.json`).
- **Impact:** Minimal. No service-worker cache clearing exists on logout, which is fine today. If `dataGroups` are ever added, logout must call `caches.delete(...)` for them.
- **Recommended fix:** Keep `dataGroups` empty for authenticated APIs. Delete the duplicate root `ngsw-config.json` to avoid confusion. Make sure backend API responses send `Cache-Control: no-store` so the browser HTTP cache does not keep them (Spring Security's defaults do this; **needs verification** for file and blob endpoints).
- **Effort:** S.

## FE-15 — Repository and build hygiene

- **Severity: Info.** None of these files is served, because the runtime image copies only `dist/ui/` and `angular.json` assets. They leak internal paths and add supply-chain surface.
- **Evidence:**
  - Stray files in the repo root:
    - `docs.html`: a template redirect to `phoenixcoded.gitbook.io`.
    - `build_log.txt`, `build_output.txt`, `sass-warnings.txt`: contain local paths like `D:\osm-ms-fe\...`.
    - `.idea/shelf/...shelved.patch`, `WhatsApp Image 2026-05-13 at 18.32.30.jpeg`, `test-results/.last-run.json`.
    - `src/assets/images/profile/*.tsx`: served as static files.
  - Template demo pages under `src/app/theme/pages/**` with external links.
  - Dependencies that appear unused (no imports found): `ng-recaptcha`, `angular-uploader`/`uploader`, `ngx-editor`, `ngx-quill`/`quill`. Verify before removing.
  - `Dockerfile:12,23` uses `npm ci --force` and `.npmrc` sets `legacy-peer-deps=true`, which hide dependency conflicts.
- **Recommended fix:** Delete or `.gitignore` the stray files, remove unused dependencies, run `npm audit` regularly, and drop `--force` once peer dependencies are fixed.
- **Effort:** S.

## FE-16 — Firebase configuration and keys

- **Severity: Info.**
- **Evidence:**
  - `src/assets/firebase-config.json`: all fields empty (`"apiKey": ""`, …).
  - `src/environments/environment*.ts`: `firebase` block empty. `apiUrl: ''` means same-origin.
  - The only history of `firebase-config.json` is commit `57913df`. `git log -G "AIza…"` returns nothing.
  - A search for `private_key`, `serviceAccount`, `service_account`, `AAAA…:` (FCM legacy server key), `AIza…`, `client_secret`, `BEGIN PRIVATE KEY` and `ghp_` across tracked files found only SVG false positives.
  - The env examples (`.env.render.example`, `.env.railway.example`, `.env.render.railway-api.example`) hold only backend URLs, including one internal Railway hostname, `charismatic-hope.railway.internal:8084`. That is low-sensitivity but internal topology.
- **Notes:**
  - When the web config is filled in at deploy time, the Firebase web API key is public by design. Restrict it in Google Cloud Console to the production HTTP referrers and to the Firebase Installations and FCM Registration APIs.
  - The app uses only FCM, not Firestore, RTDB or Storage from the browser. Make sure those products are disabled, or their rules deny all, in that Firebase project. **Needs verification** in the console.
  - The FCM service-account key must stay backend-only, which matches the comment in `environment.ts:11`.
- **Effort:** S.

## FE-17 — Dev server exposure

- **Severity: Info.** Development only.
- **Evidence:** `angular.json:137-140`: `"host": "0.0.0.0"`, `"disableHostCheck": true`.
- **Impact:** A developer running `ng serve` on an untrusted network exposes the dev app, and DNS-rebinding protection is disabled.
- **Recommended fix:** Default to `localhost` and use `start` (0.0.0.0) only when needed. The `start:local` script already exists.
- **Effort:** S.

---

## What is done well

- **No Angular sanitization bypasses.** There are no `[innerHTML]`, `bypassSecurityTrust*`, `DomSanitizer`, `insertAdjacentHTML`, `outerHTML`, `srcdoc`, `eval` or `new Function` anywhere in `src/`. `ElementRef` is used only to clear a barcode `<svg>` (`svg.innerHTML = ''`).
- **Safe UI text.** Toasts use `MatSnackBar.open` (text only). ngx-translate strings are rendered through `{{ }}` interpolation. driver.js tour content comes only from static translation keys (`tour.service.ts:182-188`).
- **No open-redirect surface in auth.** There is no `returnUrl`/`redirect` query parameter after login (the old code is commented out in `auth.guard.ts:30`). In-app navigation from data uses `router.navigateByUrl`, and `window.open` targets are same-origin router URLs.
- **Remember-me defaults to sessionStorage.** Tokens go to sessionStorage (tab-scoped) unless remember-me is chosen. The remember-me expiry is purged on start-up.
- **Refresh logic is sound.** It queues concurrent 401s behind one refresh, fails fast on refresh failure, and logs out on `invalid_grant`/`invalid_client`/`access_denied`. It excludes token and public auth endpoints to avoid loops.
- **No token logging.** No `console.log` of tokens or passwords was found (only generic error objects).
- **Clean production build.** Source maps are disabled, output is hashed, and licenses are extracted. The runtime image is `nginx:alpine` with only `dist/` copied. `.dockerignore` excludes `.git`, logs and `node_modules`. `NPM_TOKEN` is written only in the build stage.
- **Service worker caches no API data.** It is configured with `dataGroups: []`, and `ngsw.json` and `ngsw-worker.js` are served `no-cache`.
- **Secrets stay out of the frontend.** Firebase web config is loaded at runtime from an empty committed file, and the environment files have a comment keeping the service-account key backend-only. No secrets were found in the repo or in history for the patterns checked.
- **Backend checks behind frontend assumptions are correct.** The backend enforces self-only FCM device registration and ignores the client `X-Tenant-ID` header.
