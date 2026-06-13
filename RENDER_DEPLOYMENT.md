# Render Test Deployment

This blueprint deploys:

```text
oosm-api  Spring Boot modular monolith
oosm-web  Angular frontend served by Nginx
```

Use [render.yaml](render.yaml) from this repo as the Blueprint. Both services
use the Frankfurt region, free instances, and manual deploys.

## Backend

The backend runs from:

```text
https://github.com/x-dev-grp/oosm
```

The database is Supabase PostgreSQL. Render does not create a database.

Import the ignored `.env.render` file into the `oosm-api` environment using
Render's **Add from .env** control. It contains the current Render PostgreSQL
internal connection values. The tracked [.env.render.example](.env.render.example)
remains a secret-free template.

Supabase pooler format is also valid if the host, port, user, and password match the Supabase connection string.

Schema mode stays:

```text
HIBERNATE_DDL_AUTO=update
```

Do not use:

```text
create
create-drop
```

## Frontend

The frontend runs from:

```text
https://github.com/x-dev-grp/osm-ms-fe
```

It is deployed as a Docker web service, not a Render static site. The Blueprint
injects the backend private `hostport`. Nginx proxies:

```text
/api/*    -> oosm-api
/oauth2/* -> oosm-api
/.well-known/* -> oosm-api
/jwks -> oosm-api
/actuator/* -> oosm-api
```

The frontend template is
`osm-ms-fe/.env.render.example`. No frontend secret is required at runtime.

## Deployment Order

1. Push both repositories and branches referenced by `render.yaml`.
   The backend deploys from `main`; the frontend deploys from `pfe-v2-final`.
2. Create a Render Blueprint from the backend repository.
3. Import the backend `.env.render.example` values into `oosm-api`.
4. Confirm the generated service URLs match `oosm-api.onrender.com` and
   `oosm-web.onrender.com`. Update the URL variables when Render changes a name.
5. Deploy `oosm-api`.
6. Deploy `oosm-web` after the backend health check passes.
7. Test `/actuator/health/liveness` through both service URLs.

## Database Scripts

After the first backend deploy creates/updates the base schema in Supabase:

1. Run the SQL bootstrap from [RAILWAY_DATABASE_BOOTSTRAP.md](RAILWAY_DATABASE_BOOTSTRAP.md).
2. Restart `oosm-api`.

The same SQL bootstrap applies to Supabase.

## Secrets

Do not commit the populated `.env.render` files. Do not expose Supabase
service-role keys to the frontend. The backend needs PostgreSQL credentials,
not Supabase browser API keys.
