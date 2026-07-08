# Backend Deployment on Render

The Blueprint deploys only the `oosm-api` Spring Boot service.

## Repository

```text
Repository: https://github.com/x-dev-grp/oosm
Branch: pfe-v2-final
Blueprint: render.yaml
Runtime: Docker
Region: Frankfurt
Health check: /actuator/health/liveness
```

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
ONESIGNAL_APP_ID=CHANGE_ME_ONESIGNAL_APP_ID
ONESIGNAL_API_KEY=CHANGE_ME_ONESIGNAL_API_KEY
ONESIGNAL_ENDPOINT=https://onesignal.com/api/v1/notifications
```

Mail and OneSignal credentials may remain empty when those features are unused.

## New Relic (APM + browser logs)

### 1. Provision in New Relic (CLI)

On your machine:

```powershell
cd F:\oosm
$env:NEWRELIC_API_KEY = "YOUR_USER_API_KEY"
powershell -File scripts\Setup-NewRelic.ps1 -AccountId YOUR_ACCOUNT_ID -Region EU -ApplyToDatabase
```

This creates the browser SPA app, an ingest license key, writes `newrelic-oosm-config.json`, and optionally upserts `app_setting` rows.

### 2. Render backend env (`oosm-api`)

Set these in Render (or `.env.render`):

```text
NEW_RELIC_APM_ENABLED=true
NEW_RELIC_LICENSE_KEY=<ingest-license-key>
NEW_RELIC_APP_NAME=oosm-monolith
NEW_RELIC_REGION=EU
NEW_RELIC_LOG_FORWARDING_ENABLED=true
```

`NEW_RELIC_LICENSE_KEY` is required on the host — the JVM agent starts before Spring and cannot read encrypted DB secrets.

APM enabled/app name/region/log forwarding can also be toggled in **Administration → Settings → Integrations**; the Docker entrypoint reads those from `app_setting` on startup when Render env vars are not set.

### 3. OOSM admin (browser monitoring)

**Administration → Settings → Integrations**:

```text
NEW_RELIC_BROWSER_ENABLED=true
NEW_RELIC_BROWSER_ACCOUNT_ID=<from Setup-NewRelic.ps1 output>
NEW_RELIC_BROWSER_APPLICATION_ID=<from output>
NEW_RELIC_BROWSER_LICENSE_KEY=<from output>
```

Reload settings cache. Redeploy the frontend (`osm-ms-fe`) so the browser agent loads `/api/public/observability-config`.

### 4. Verify

```text
# Backend logs on deploy:
New Relic Java agent enabled (app=oosm-monolith, region=EU)

# Public config (no auth):
https://<backend-host>/api/public/observability-config

# New Relic UI:
APM → oosm-monolith → Logs
Browser → oosm-frontend → Page views
```

Render supplies `PORT` and `RENDER_EXTERNAL_URL`. Do not define them manually.
Do not define `JWK_SET_URI`; the application derives it from
`RENDER_EXTERNAL_URL`.

## Deployment

1. Commit and push the backend changes to `main`.
2. In Render, select **New > Blueprint**.
3. Select `x-dev-grp/oosm`.
4. Keep the Blueprint path as `render.yaml`.
5. Enter every value requested by Render.
6. Create the Blueprint.
7. Open `oosm-api` and monitor the deploy logs.
8. Wait for the service health check to pass.

The first startup creates the OAuth registered-client table, updates the
Hibernate business schema, registers the OAuth client, and creates the bootstrap
administrator.

## Validation

```text
https://<backend-host>/actuator/health/liveness
https://<backend-host>/oauth2/jwks
```

The liveness endpoint must return `UP`. The JWK endpoint must return a JSON key
set.

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
