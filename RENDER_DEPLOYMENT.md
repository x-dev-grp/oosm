# Backend Deployment on Render

The Blueprint deploys only the `oosm-api` Spring Boot service.

## Repository

```text
Repository: https://github.com/x-dev-grp/oosm
Branch: pfe-v2-final
Blueprint: render.yaml
Runtime: Docker
Region: Frankfurt
Health check: /actuator/health
```

## Port binding (critical)

Render injects `PORT` (typically `10000`). The app **must** listen on `0.0.0.0:$PORT`.

- `docker/entrypoint.sh` exports `SERVER_PORT` from `PORT` and passes `-Dserver.port`.
- `application.yml` uses `server.port: ${PORT:8084}` (local default `8084` only when `PORT` is unset).
- **Do not set `SERVER_PORT` in the Render dashboard.** If it is set to `8084`, remove it and redeploy.
- **Do not set `PORT` manually** — Render supplies it.

A failed deploy that mentions `:8084` in the health-check URL usually means a stale `SERVER_PORT=8084` dashboard variable or an old image; the live service must bind to Render's `PORT`.

## Database Values

Provide:

```text
DB_URL=jdbc:postgresql://<host>:<port>/<database>?sslmode=require
DB_USER=<database-user>
DB_PASS=<database-password>
```

For an existing Render Postgres database in the same workspace and Frankfurt
region, use its internal hostname:

```text
DB_URL=jdbc:postgresql://<render-internal-hostname>:5432/<database>?sslmode=require
DB_USER=<render-database-user>
DB_PASS=<render-database-password>
```

For the current database, the non-secret values are:

```text
DB_URL=jdbc:postgresql://dpg-d9576om7r5hc73e40l7g-a:5432/oosm_fawv
DB_USER=oosm_fawv_user
```

Use the database's current password from Render as `DB_PASS`. Do not use the
external hostname from a Render-hosted backend.

## Required Placeholders

Replace these before deployment:

```text
FRONTEND_ENTRY_POINT=https://CHANGE_ME_FRONTEND_HOST
APP_CORS_ALLOWED_ORIGIN_PATTERNS=https://CHANGE_ME_FRONTEND_HOST
OAUTH2_CLIENT_ID=CHANGE_ME_OAUTH_CLIENT_ID
OAUTH2_CLIENT_SECRET=CHANGE_ME_OAUTH_CLIENT_SECRET
SECURITY_BOOTSTRAP_USERNAME=CHANGE_ME_ADMIN_USERNAME
SECURITY_BOOTSTRAP_PASSWORD=CHANGE_ME_ADMIN_PASSWORD
SECURITY_BOOTSTRAP_EMAIL=CHANGE_ME_ADMIN_EMAIL
SECURITY_BOOTSTRAP_PHONE=CHANGE_ME_ADMIN_PHONE
QR_BASE_URL=https://CHANGE_ME_PUBLIC_QR_HOST/q/v1
```

`FRONTEND_ENTRY_POINT` and `APP_CORS_ALLOWED_ORIGIN_PATTERNS` may temporarily
use `http://localhost:4200` while no deployed frontend exists.

## Optional Placeholders

Resend (transactional email):

```text
RESEND_API_KEY=re_xxxxxxxx
MAIL_FROM_ADDRESS=noreply@your-verified-domain.com
MAIL_FROM_NAME=OOSM
MAIL_SUPPORT_EMAIL=support@your-verified-domain.com
MAIL_ENABLED=true
```

`MAIL_FROM_ADDRESS` must use a domain verified in Resend.

```text
FCM_PROJECT_ID=CHANGE_ME_FCM_PROJECT_ID
FCM_CLIENT_EMAIL=CHANGE_ME_FCM_CLIENT_EMAIL
FCM_PRIVATE_KEY=
```

Mail and FCM credentials may remain empty when those features are unused. FCM private key is a service-account PEM (use `\n` for newlines in env vars, or set via Admin Settings).

Render supplies `PORT` and `RENDER_EXTERNAL_URL`. Do not define them manually.
Do not define `JWK_SET_URI`; the application derives it from
`RENDER_EXTERNAL_URL`.

## Deployment

1. Commit and push the backend changes to `pfe-v2-final`.
2. In Render, select **New > Blueprint** (or sync an existing Blueprint).
3. Select `x-dev-grp/oosm`.
4. Keep the Blueprint path as `render.yaml`.
5. Enter every value requested by Render.
6. Create or sync the Blueprint.
7. Open `oosm-api` and trigger **Manual Deploy** if `autoDeployTrigger` is off.
8. Monitor deploy logs until the health check passes (first boot can take 4–5 minutes on the free tier).

The first startup creates the OAuth registered-client table, updates the
Hibernate business schema, registers the OAuth client, and creates the bootstrap
administrator.

### Manual dashboard checklist before redeploy

1. **Environment → remove `SERVER_PORT`** if present (Render uses `PORT` only).
2. **Environment → `JAVA_TOOL_OPTIONS`** — use the values from `render.yaml`; remove stale New Relic-era overrides (`-Xmx192m`, etc.).
3. **Settings → Health Check Path** — must be `/actuator/health` (Blueprint sync updates this from `render.yaml`).
4. **Manual Deploy** → Deploy latest commit.

## Startup tuning (512 MiB free tier)

Render's deploy probe allows roughly five minutes for the container to bind
`$PORT` and return HTTP 200 on the health-check path. On 512 MiB, cold starts
take about four to six minutes.

`render.yaml` already sets:

| Variable | Purpose |
|----------|---------|
| `SPRING_MAIN_LAZY_INITIALIZATION=true` | Opens Tomcat sooner; `RenderActuatorWarmup` pre-warms `/actuator/health` |
| `SPRING_SQL_INIT_MODE=never` | Skips re-running classpath SQL scripts on every redeploy |
| `SPRINGDOC_ENABLED=false` | Disables OpenAPI generation at startup |
| `JPA_REPOSITORY_BOOTSTRAP_MODE=lazy` | Defers JPA repository metadata |

After the **first** successful deploy on a fresh database, keep `SPRING_SQL_INIT_MODE=never`.
Run `scripts/Run-RailwayDatabaseScripts.ps1` locally for one-time seeds instead.

## Validation

```text
https://<backend-host>/actuator/health
https://<backend-host>/oauth2/jwks
```

`/actuator/health` must return `{"status":"UP",...}`. The JWK endpoint must return a JSON key set.

## Database Bootstrap

After the first successful startup:

```powershell
$env:DB_URL="jdbc:postgresql://<host>:<port>/<database>?sslmode=require"
$env:DB_USER="<database-user>"
$env:DB_PASS="<database-password>"

.\scripts\Run-RailwayDatabaseScripts.ps1 -IncludeOneTimeSeeds
```

Run `-IncludeOneTimeSeeds` only once on a fresh database. Restart `oosm-api`
after the scripts finish.

## JVM memory (512 MiB free tier)

Render free instances are capped at 512 MiB. JVM tuning is set via
`JAVA_TOOL_OPTIONS` in `render.yaml` (same profile used before New Relic):

```text
-XX:+UseContainerSupport -XX:+UseSerialGC
-Xms64m -Xmx256m -Xss512k
-XX:MaxMetaspaceSize=128m
-XX:ReservedCodeCacheSize=48m
-XX:MaxDirectMemorySize=32m
-XX:+ExitOnOutOfMemoryError
```

| Region | Limit |
|--------|-------|
| Heap (`-Xmx`) | 256m |
| Metaspace | 128m |
| Code cache | 48m |
| Direct memory | 32m |
| Thread stack | 512k |

If you override `JAVA_TOOL_OPTIONS` in the Render dashboard, remove any stale
compact-profile values from the New Relic era (e.g. `-Xmx192m`,
`-XX:MaxMetaspaceSize=96m`). Redeploy after changing env vars.

## Troubleshooting deploy timeout

| Symptom | Likely cause | Fix |
|---------|--------------|-----|
| Health check URL shows `:8084` | `SERVER_PORT=8084` in dashboard | Remove `SERVER_PORT`, redeploy |
| `No open ports detected` then timeout | Startup > ~5 min (eager init, SQL init, OpenAPI) | Keep lazy init + `SPRING_SQL_INIT_MODE=never` + `SPRINGDOC_ENABLED=false` |
| `Timed out` on `/actuator/health/liveness` | Wrong path or first-request lazy-init delay | Use `/actuator/health`; ensure latest image with `RenderActuatorWarmup` |
