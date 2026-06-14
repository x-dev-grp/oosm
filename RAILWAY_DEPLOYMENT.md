# OSM 2.0 Railway Deployment

OSM 2.0 is the deployment target for the backend. It is a modular monolith: one Spring Boot service, one deployable jar, one PostgreSQL database.

## Service

Deploy this folder as the backend service:

```text
C:\oosm
```

Railway will use `Dockerfile`, build `app`, and run `osm-monolith.jar`.

## Database

Create one PostgreSQL service and connect the backend with these variables:

```text
DB_URL=jdbc:postgresql://<host>:<port>/<database>
DB_USER=<user>
DB_PASS=<password>
HIBERNATE_DDL_AUTO=update
```

Keep `HIBERNATE_DDL_AUTO=update` for Railway. Do not use `create` or `create-drop` on a persistent database.

Railway PostgreSQL variables can also be used directly:

```text
PGHOST=<railway-postgres-host>
PGPORT=<railway-postgres-port>
PGDATABASE=<railway-postgres-database>
PGUSER=<railway-postgres-user>
PGPASSWORD=<railway-postgres-password>
```

`DB_URL`, `DB_USER`, and `DB_PASS` override the `PG*` variables when both are present.

## Required Runtime Variables

```text
FRONTEND_ENTRY_POINT=https://<frontend-domain>
APP_CORS_ALLOWED_ORIGIN_PATTERNS=https://<frontend-domain>,https://*.up.railway.app
JWT_ISSUER_URI=https://<backend-domain>
JWK_SET_URI=https://<backend-domain>/oauth2/jwks
OAUTH2_CLIENT_SECRET=<oauth2-client-secret>
MAIL_USER=<smtp-user>
MAIL_PASS=<smtp-password>
ONESIGNAL_APP_ID=<onesignal-app-id>
ONESIGNAL_API_KEY=<onesignal-api-key>
```

Railway injects `PORT`; `application.yml` now reads it automatically.

Keep `OAUTH2_CLIENT_SECRET` equal to the frontend Basic auth header unless the frontend is rebuilt with a matching header.

Railway healthcheck uses `/actuator/health/liveness`; this confirms the service process is alive without failing deployment because PostgreSQL readiness is still warming up.

After the first backend deploy creates the base schema, run the manual database scripts in `RAILWAY_DATABASE_BOOTSTRAP.md`, then restart the backend service.

## Local Build

```powershell
mvn -B -DskipTests clean package -pl app -am
```

## Local Run

```powershell
docker compose up -d
mvn -pl app spring-boot:run
```
