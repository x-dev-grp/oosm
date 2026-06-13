# Render Deployment

This blueprint deploys:

```text
oosm-api  Spring Boot modular monolith
oosm-web  Angular frontend served by Nginx
```

Use [render.yaml](render.yaml) from this repo as the Blueprint.

## Backend

The backend runs from:

```text
https://github.com/x-dev-grp/oosm
```

The database is Supabase PostgreSQL. Render does not create a database.

Set these backend environment variables in Render:

```text
DB_URL=jdbc:postgresql://<supabase-host>:5432/postgres?sslmode=require
DB_USER=<supabase-db-user>
DB_PASS=<supabase-db-password>
```

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

It is deployed as a Docker web service, not a Render static site. Nginx proxies:

```text
/api/*    -> oosm-api
/oauth2/* -> oosm-api
```

## Database Scripts

After the first backend deploy creates/updates the base schema in Supabase:

1. Run the SQL bootstrap from [RAILWAY_DATABASE_BOOTSTRAP.md](RAILWAY_DATABASE_BOOTSTRAP.md).
2. Restart `oosm-api`.

The same SQL bootstrap applies to Supabase.

## Important

Do not expose Supabase service-role API keys to the frontend. The backend needs only PostgreSQL connection credentials.
