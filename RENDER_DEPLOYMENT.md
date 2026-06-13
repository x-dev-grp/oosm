# Backend Deployment on Render

The Blueprint deploys only the `oosm-api` Spring Boot service.

## Repository

```text
Repository: https://github.com/x-dev-grp/oosm
Branch: main
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

For Supabase, use the Session Pooler connection on port `5432`.

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
