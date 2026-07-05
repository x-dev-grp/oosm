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
DB_URL=jdbc:postgresql://dpg-d8mm0t3tqb8s73c8v73g-a:5432/oosm?sslmode=require
DB_USER=oosm_user
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

Mail:

```text
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USER=CHANGE_ME_SMTP_USER
MAIL_PASS=CHANGE_ME_SMTP_PASSWORD
MAIL_SMTP_AUTH=true
MAIL_STARTTLS=true
```

OneSignal:

```text
ONESIGNAL_APP_ID=CHANGE_ME_ONESIGNAL_APP_ID
ONESIGNAL_API_KEY=CHANGE_ME_ONESIGNAL_API_KEY
ONESIGNAL_ENDPOINT=https://onesignal.com/api/v1/notifications
```

Mail and OneSignal credentials may remain empty when those features are unused.

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
