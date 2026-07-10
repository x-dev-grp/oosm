# Railway backend + Render frontend + Render Postgres

Deploy **only the Spring backend** on Railway. Keep **frontend** and **Postgres** on Render.

For all-on-Railway (including Railway Postgres), see [RAILWAY_TESTING.md](RAILWAY_TESTING.md).

## Architecture

```text
Browser → Render frontend (oosm-web / nginx)
              │  BACKEND_URL proxy (/api, /oauth2, …)
              ▼
         Railway backend (Spring Boot)
              │  JDBC SSL (external host)
              ▼
         Render Postgres (oosm_fawv)
```

- **Railway:** backend service only — **no** Postgres plugin, **no** frontend service
- **Render:** Postgres + `oosm-web` (frontend) stay live; old Render `oosm-api` can be suspended
- Railway cannot resolve Render’s **internal** host (`dpg-…-a`). Use the **external** hostname

## Paste-ready env files

| Where | File |
|-------|------|
| Railway backend (secrets, local) | `.env.railway.render-db` (gitignored) |
| Railway backend (template) | [`.env.railway.render-db.example`](.env.railway.render-db.example) |
| Render frontend → Railway API | [`../osm-ms-fe/.env.render.railway-api.example`](../osm-ms-fe/.env.render.railway-api.example) |

## Do not

- Add a Railway PostgreSQL plugin
- Deploy the Angular frontend on Railway for this setup
- Run `scripts/railway-link-postgres.sh`
- Set `DATABASE_URL` / `PGHOST` / `PG*` on Railway
- Use the internal Render DB host without `.frankfurt-postgres.render.com`
- Omit `?sslmode=require` on `DB_URL`
- Suspend Render Postgres (or the Render frontend) while this stack is live

## Database connection

```text
DB_URL=jdbc:postgresql://dpg-d9576om7r5hc73e40l7g-a.frankfurt-postgres.render.com:5432/oosm_fawv?sslmode=require
DB_USER=oosm_fawv_user
DB_PASS=<from Render>
SPRING_SQL_INIT_MODE=never
HIBERNATE_DDL_AUTO=none
```

## Checklist

### 1. Railway — backend only

1. Empty project (e.g. `oosm-railway`) — **no** Postgres, **no** frontend service
2. Service from `x-dev-grp/oosm` / branch `pfe-v2-final`
3. Health check: `/actuator/health/liveness`
4. Generate domain → note `https://<backend>.up.railway.app`
5. Paste `.env.railway.render-db` (or the example + secrets)
6. Set JWT / internal URLs to the Railway domain:

```text
JWT_ISSUER_URI=https://<backend>.up.railway.app
JWK_SET_URI=https://<backend>.up.railway.app/oauth2/jwks
INTERNAL_BASE_URL=https://<backend>.up.railway.app
FRONTEND_ENTRY_POINT=https://oosm-web.onrender.com
APP_CORS_ALLOWED_ORIGIN_PATTERNS=https://oosm-web.onrender.com,https://*.onrender.com,https://oosm.x-dev.pro
```

7. Redeploy backend

### 2. Render — frontend points at Railway

On **oosm-web** (or your Render frontend service):

```text
BACKEND_URL=https://<backend>.up.railway.app
```

Redeploy the frontend so nginx proxies `/api` and `/oauth2` to Railway.

You can suspend the old Render **oosm-api** service; keep **Postgres** and **oosm-web**.

### 3. Verify

1. `https://<backend>.up.railway.app/actuator/health/liveness` → `UP`
2. Open `https://oosm-web.onrender.com/` → login (existing Render DB users)
3. Browser network: API calls go to the Render frontend origin and are proxied to Railway

## Troubleshooting: `Connection to localhost:5432 refused`

`DB_URL` is missing on the Railway backend. Paste the DB vars above, delete `DATABASE_URL` / `PG*`, redeploy. Logs must show the Render external host, not `localhost`.

## Memory notes

Railway backend has **no hard `-Xmx` / metaspace caps**. `JAVA_TOOL_OPTIONS` uses `-XX:MaxRAMPercentage=75.0` so the heap scales with the service plan. Keep `HIBERNATE_DDL_AUTO=none` and a modest pool (`DB_POOL_MAX_SIZE=3`) unless you raise the Railway memory plan.

## Related

- Full Railway stack: [RAILWAY_TESTING.md](RAILWAY_TESTING.md)
- Reset / wrong-host: [RAILWAY_RESET.md](RAILWAY_RESET.md)
- Render frontend deploy: [`../osm-ms-fe/RENDER_DEPLOYMENT.md`](../osm-ms-fe/RENDER_DEPLOYMENT.md)
