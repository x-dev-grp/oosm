# Supabase + Render Deployment

Deploy only the frontend and backend on Render. Use Supabase for PostgreSQL.

## Render Services

Use [render.yaml](render.yaml). It creates:

```text
oosm-api
oosm-web
```

It does not create a Render database.

## Backend Variables

Set these on `oosm-api`. The same values are listed in `.env.render`:

```text
DB_URL=jdbc:postgresql://<supabase-host>:5432/postgres?sslmode=require
DB_USER=<supabase-db-user>
DB_PASS=<supabase-db-password>
HIBERNATE_DDL_AUTO=update
SHOW_SQL=false
APP_CORS_ALLOWED_ORIGIN_PATTERNS=https://*.onrender.com
OAUTH2_CLIENT_SECRET=<oauth2-client-secret>
RESEND_API_KEY=<resend-api-key>
MAIL_FROM_ADDRESS=<verified-sender@your-domain.com>
FCM_PROJECT_ID=<fcm-project-id>
FCM_CLIENT_EMAIL=<fcm-service-account-email>
FCM_PRIVATE_KEY=<fcm-service-account-private-key-pem>
```

Do not use:

```text
HIBERNATE_DDL_AUTO=create
HIBERNATE_DDL_AUTO=create-drop
```

## Frontend Variables

`render.yaml` injects the backend private URL:

```text
BACKEND_HOSTPORT=<oosm-api host:port>
```

No hardcoded frontend backend URL is needed.

## Database Bootstrap

1. Deploy `oosm-api` once.
2. Let Hibernate update the Supabase schema.
3. Run the SQL scripts from [RAILWAY_DATABASE_BOOTSTRAP.md](RAILWAY_DATABASE_BOOTSTRAP.md).
4. Restart `oosm-api`.

Run idempotent scripts:

```powershell
.\scripts\Run-RailwayDatabaseScripts.ps1
```

On a fresh database only:

```powershell
.\scripts\Run-RailwayDatabaseScripts.ps1 -IncludeOneTimeSeeds
```
