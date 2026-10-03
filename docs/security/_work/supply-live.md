# Supply chain, secrets and live deployment (working notes, 2026-09-28)

## Tools
- gitleaks 8.30.1 (git history of both repos plus working trees, `--redact`).
- OSV-Scanner 2.6.0 on `package-lock.json`; Maven runtime tree resolved with `mvn dependency:list -pl app -am` (137 third-party artefacts) and checked against the osv.dev batch API.
- `npm audit` on the frontend.
- curl, DNS-over-HTTPS and a TCP connect test for the live checks (GET/HEAD/OPTIONS only, no credentials).

## Findings

### SUP-01 Committed default JWT signing key (HIGH)
- `app/src/main/resources/application.yml:114` sets `secret: ${JWT_SECRET:X7kP…}`. The same 32-character value is in `.env.railway.example:27`, and it has been in git history since commit `98af17e` (2026-07-10).
- `JwtKeyLoader` uses this string as the HS256 HMAC key for every access token. Any environment that forgets `JWT_SECRET` silently runs with a public key, so anyone who has read the repo can mint tokens with any `role`/`oosmUser` claim.
- `application.yml:129` also falls back to the JWT secret for `APP_SETTINGS_ENCRYPTION_KEY`, so the same public value would protect encrypted settings.
- Evidence that production is currently safe: the production token saved in `token-smoke.json` (issuer `https://oosm-production.up.railway.app`) does **not** verify with the default key. A local token in `scripts/_day_import_token.txt` (issuer `http://localhost:8085`) **does**. This was checked offline, without sending any request.
- Fix: remove the default (`${JWT_SECRET}`), fail at start-up when it is missing or shorter than 32 bytes, use a separate required `APP_SETTINGS_ENCRYPTION_KEY`, and replace the value in the example file with a placeholder. Treat the old value as burned. Effort S.

### SUP-02 Outdated Spring Boot 3.4.4 stack with known critical/high CVEs (HIGH)
- 30 of 137 runtime artefacts have OSV advisories. Nearly all come from the Spring Boot 3.4.4 BOM:
  - tomcat-embed-core 10.1.39: critical security-constraint and HTTP/2 header-validation advisories, plus request smuggling and multipart DoS;
  - spring-security 6.4.4: CVE-2025-41232 (method-security bypass on private methods), CVE-2025-41248, and CVE-2026-22732 (headers not written);
  - spring-boot-actuator: CVE-2026-22731/22733 (authentication bypass under health-group paths);
  - jackson-databind 2.18.3, spring-webmvc/web 6.2.5, spring-data-commons 3.4.4 (Sort/property-path DoS), and postgresql 42.7.5 (channel-binding downgrade);
  - logback 1.5.18, micrometer, nimbus-jose-jwt 9.37.3/9.47.
- Fix: move to the latest Spring Boot 3.5.x (or at least the latest 3.4.x patch). This pulls fixed Tomcat, Spring Framework, Security, Jackson and pgjdbc versions. Re-run the test suite. Effort M.

### SUP-03 iText 2.1.7 and Bouncy Castle 1.38 in the conditioning module (MEDIUM)
- `conditioning -> com.lowagie:itext:2.1.7 -> bctsp-jdk14 -> bcprov-jdk14:1.38` (2009-era). This brings CVE-2017-9096 (XXE in iText XML parsing) and many Bouncy Castle signature and timing issues.
- The exploitability depends on whether untrusted XML/HTML reaches iText, which the input-handling review checks.
- Fix: replace with OpenPDF (a drop-in `com.lowagie` fork) or exclude `bctsp-jdk14` if signing is not used. Effort S–M.

### SUP-04 Apache POI 5.2.3, commons-compress 1.21 (MEDIUM)
- CVE-2025-31672 (OOXML input validation) and commons-compress DoS advisories on the day-import upload path.
- Fix: POI 5.4.x and commons-compress 1.27+. Effort S.

### SUP-05 Frontend Angular 19.2 runtime advisories (MEDIUM)
- `npm audit` reports 89 advisories (5 critical, 47 high). Most are build- and dev-only tooling: tar, basic-ftp, shell-quote, websocket-driver, vite, esbuild, webpack-dev-server, puppeteer via pwa-asset-generator. None of these ship to browsers.
- The shipped runtime is affected by:
  - `@angular/core`/`compiler` <=19.2.25: several sanitizer-bypass XSS advisories;
  - `@angular/common`: XSRF token leakage via protocol-relative URLs;
  - `@angular/service-worker`: header leakage on cross-origin redirects;
  - `dompurify` (transitive, several XSS bypasses);
  - `exceljs` (via `uuid`).
- Fix: update to the latest Angular 19.2.x patch first (non-breaking), run `npm audit fix` for transitive runtime packages, then plan the Angular major upgrade. Effort S (patch) / L (major).

### SUP-06 Real tokens in untracked, non-ignored files (LOW)
- `F:/oosm/token-smoke.json` (production access and refresh token, expired 2026-09-03) and `scripts/_day_import_token.txt` (local token) are untracked, but `git check-ignore` shows no rule covers them. A `git add .` would commit them.
- Fix: delete them and add `token-smoke.json`, `scripts/_*.txt`, `scripts/_*.json` to `.gitignore`. Effort S.

### SUP-07 Secret-like strings in docs (INFO)
- gitleaks hits in `RAILWAY_DEPLOYMENT.md` and `SUPABASE_RENDER_DEPLOYMENT.md` (`OAUTH2_CLIENT_SECRET=<oau…>`) are placeholders.
- `FcmPushService.java:222` is PEM-marker handling, not a key.
- The frontend hit is a Google reCAPTCHA **site** key in a demo page; those keys are public by design.

### SUP-08 Containers (INFO, mostly good)
- The backend image runs as uid 10001 (`USER oosm`), uses a JRE-only runtime and excludes `.env*` and `scripts` via `.dockerignore`. Good.
- The frontend image is stock `nginx:1.27-alpine`: the master process runs as root and there's no `.dockerignore` review of `dist/`.
- Stray tracked files (`build_log.txt`, `docs.html`, `build_output.txt`, a WhatsApp image) are not copied into the image, and live requests for them fall back to `index.html`.
- Suggest `nginxinc/nginx-unprivileged`. Effort S.

### LIVE-01 Frontend serves no security headers (MEDIUM)
- `https://zitflow.x-dev.pro/` responds without `Content-Security-Policy`, `Strict-Transport-Security`, `X-Frame-Options`/`frame-ancestors`, `X-Content-Type-Options`, `Referrer-Policy` or `Permissions-Policy`.
- It also exposes `x-render-origin-server: nginx/1.27.5`.
- Because tokens are held in `localStorage`, a CSP is the main defence-in-depth against token theft through XSS, and the missing frame protection allows clickjacking of the SPA.
- The API responses (proxied from Railway) do carry HSTS, nosniff, `X-Frame-Options: DENY` and no-store.
- Fix: add the headers in `nginx.conf.template` (`always`, repeated in locations that use `add_header`), plus `server_tokens off`. Effort S.

### LIVE-02 Render Postgres reachable from the internet (MEDIUM)
- `dpg-dacp1rfqj5pc738k9ung-a.oregon-postgres.render.com:5432` accepts TCP connections from an arbitrary client IP. Access still requires the password, and `sslmode=require` is used by the app.
- The database is exposed to credential stuffing and to any leak of the connection string (it appears in local IDE files such as `.run/oosm-render.datasource.xml`).
- Fix: in Render set the database's IP allow list to Railway's static egress IPs (enable static outbound IPs on the Railway service) plus admin IPs, or move the API next to the database and use the internal URL. Effort S–M.

### LIVE-03 Email spoofing protection is monitor-only (MEDIUM)
- `_dmarc.x-dev.pro` is `v=DMARC1; p=none;` with no `rua` reporting.
- SPF is `include:mx.ovh.com ~all` (soft fail), and `send.x-dev.pro` covers SES.
- DKIM exists for OVH (`ovhmo-selector-1`) and Resend.
- Anyone can send mail as `@x-dev.pro`, for example fake password-reset emails, and receivers will still deliver it.
- Fix: add `rua=mailto:…` and watch the reports for 2–4 weeks, then move to `p=quarantine` and later `p=reject`; tighten SPF to `-all` once every sender is listed. Effort S.

### LIVE-04 No CAA record (LOW)
- `x-dev.pro` has no CAA record, so any public CA may issue certificates for it. DNSSEC is enabled (AD flag set), which is good.
- Fix: add `CAA 0 issue "letsencrypt.org"`, `CAA 0 issue "pki.goog"` (Cloudflare/Render/GitHub use these; verify first) and `CAA 0 iodef "mailto:…"`. Effort S.

### LIVE-05 `oosm.x-dev.pro` points to a suspended Render service (LOW)
- `oosm.x-dev.pro` CNAMEs to `oosm-api-5im4.onrender.com`, which now returns 503. The production API lives on `oosm-production.up.railway.app`, and the frontend Docker default `BACKEND_URL` still names the old Render host.
- Render service subdomains are unique per account, so takeover is unlikely. The record is still misleading and should be repointed to Railway or removed.
- The `ftp` record now CNAMEs to the apex (GitHub Pages), so FTP no longer works there.
- Effort S.

### LIVE-06 Public health endpoint (INFO, good)
- `/actuator/health` returns only `{"status":"UP","groups":[...]}`.
- `/actuator/*` (env, prometheus), `/v3/api-docs`, `/swagger-ui.html` and `/h2-console` return 401 on both the Railway host and through the frontend proxy.
- TLS 1.0/1.1 are refused on both hosts.
- CORS preflights from `https://evil.example` and the look-alike `https://evilx-dev.pro` get 403, while `https://zitflow.x-dev.pro` is allowed with credentials.
- `/oauth2/jwks` returns `{"keys":[]}`, so the HMAC key is not published.

### LIVE-08 CORS trusts every `*.onrender.com` site with credentials (MEDIUM)
- The Railway variable `APP_CORS_ALLOWED_ORIGIN_PATTERNS` includes `https://*.onrender.com`.
- Verified: an OPTIONS preflight from `Origin: https://attacker-demo.onrender.com` gets `200`, `access-control-allow-origin: https://attacker-demo.onrender.com` and `access-control-allow-credentials: true`.
- Anyone can create a free Render static site, so any such page may call the API cross-origin and read responses. Tokens are bearer tokens in `localStorage` (not cookies), so the attacker still needs a token. That limits the impact today, but it removes the browser's same-origin boundary, and it becomes critical the moment any cookie-based auth is added.
- It also contains `https://oosm.x-dev.pro`, which points at a suspended service (LIVE-05).
- Fix: list exact origins only (`https://zitflow.x-dev.pro`, `https://www.x-dev.pro`, `https://osm-ms-fe-1-psnx.onrender.com`). Effort S.

### LIVE-09 Production bootstrap resets the OosmAdmin password on every deploy (MEDIUM)
- Railway has `SECURITY_BOOTSTRAP_ENABLED=true` and `SECURITY_BOOTSTRAP_RESET_PASSWORD=true`. `SecurityBootstrap.java:139-148` therefore rewrites the OosmAdmin password to the Railway variable at every start-up.
- A password change made in the app, including one made after a suspected compromise, is silently reverted at the next deploy or restart. The admin password also lives in plain text in the Railway variables, and it's 13 characters.
- The committed default in `application.yml:120` is not used in production (verified: the Railway value differs, and only lengths and equality were compared, never the values).
- Fix: set `SECURITY_BOOTSTRAP_RESET_PASSWORD=false` (and ideally `SECURITY_BOOTSTRAP_ENABLED=false`) now that the admin exists. Change the code default of `reset-existing-admin-password` to `false`, and refuse to start with the committed default password outside the `local` profile. Effort S.

### LIVE-10 Springdoc enabled by default in production (LOW)
- `SPRINGDOC_ENABLED` is not set on Railway, so the default `true` applies. `/v3/api-docs` currently returns 401 (it's behind authentication), so any logged-in user of any tenant can download the full API map.
- Fix: set `SPRINGDOC_ENABLED=false` in production, or restrict it to OosmAdmin. Effort S.

### LIVE-07 OAuth2 metadata advertises unused grant endpoints (INFO)
- `/.well-known/oauth-authorization-server` advertises `authorization_endpoint` and `device_authorization_endpoint` on the Railway host. These should be disabled if only the custom password/refresh grants are used.
- The authentication review checks whether they are reachable.
