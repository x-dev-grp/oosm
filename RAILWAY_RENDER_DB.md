# Railway app + Render PostgreSQL

Deploy **backend** and **frontend** on Railway while keeping the existing **Render Postgres** (`oosm_fawv`) as the only database.

For the usual all-on-Railway stack (including Railway Postgres), see [RAILWAY_TESTING.md](RAILWAY_TESTING.md).

## Architecture

```text
Browser → Railway frontend (nginx) → Railway backend (Spring) → Render Postgres (external SSL)
```

- Railway: `oosm` + `osm-ms-fe` only — **no** Postgres plugin
- Render: keep Postgres **Available**; API/web services on Render may be suspended
- Railway cannot resolve Render’s **internal** host (`dpg-…-a`). Always use the **external** hostname

## Paste-ready env files

| Service | File |
|---------|------|
| Backend (secrets — local / Railway only) | `.env.railway.render-db` (gitignored) |
| Backend (template in repo) | [`.env.railway.render-db.example`](.env.railway.render-db.example) |
| Frontend (template) | [`../osm-ms-fe/.env.railway.render-db.example`](../osm-ms-fe/.env.railway.render-db.example) |

## Do not

- Add a Railway PostgreSQL plugin for this project
- Run `scripts/railway-link-postgres.sh` (it removes `DB_URL` and wires Railway Postgres references)
- Set `DATABASE_URL`, `PGHOST`, `PGPORT`, `PGDATABASE`, `PGUSER`, or `PGPASSWORD`
- Use the internal Render host without `.frankfurt-postgres.render.com`
- Omit `?sslmode=require` on `DB_URL`
- Suspend Render Postgres while Railway is live

## Troubleshooting: `Connection to localhost:5432 refused`

Spring fell back to the default URL because **`DB_URL` was not set** on the Railway **backend** service (or variables were added to the wrong service).

1. Railway → **backend** service (not frontend) → **Variables**
2. Confirm these exist (Raw Editor / paste from [`.env.railway.render-db`](.env.railway.render-db)):

```text
DB_URL=jdbc:postgresql://dpg-d9576om7r5hc73e40l7g-a.frankfurt-postgres.render.com:5432/oosm_fawv?sslmode=require
DB_USER=oosm_fawv_user
DB_PASS=sBwBnDDBCF4vShgKgLe9nHPeKc3Nr5KY
SPRING_SQL_INIT_MODE=never
HIBERNATE_DDL_AUTO=none
```

3. Delete `DATABASE_URL` / `PGHOST` / `PG*` if present
4. Redeploy backend
5. Deploy logs must show `Database: dpg-….frankfurt-postgres.render.com:5432/oosm_fawv` — **not** `localhost`

If SQL init still runs (`dataSourceScriptDatabaseInitializer`), `SPRING_SQL_INIT_MODE` is missing; set it to `never`.

## Database connection

Render external URL (from dashboard):

```text
postgresql://oosm_fawv_user:***@dpg-d9576om7r5hc73e40l7g-a.frankfurt-postgres.render.com/oosm_fawv
```

Set on Railway backend as separate vars (not `DATABASE_URL`):

```text
DB_URL=jdbc:postgresql://dpg-d9576om7r5hc73e40l7g-a.frankfurt-postgres.render.com:5432/oosm_fawv?sslmode=require
DB_USER=oosm_fawv_user
DB_PASS=<from Render dashboard>
SPRING_SQL_INIT_MODE=never
```

## Railway dashboard checklist

### 1. Project

1. New Railway account/project → **Empty project** (e.g. `oosm-railway`).
2. **Do not** add PostgreSQL.

### 2. Backend service

1. New service → GitHub → `x-dev-grp/oosm`, branch `pfe-v2-final`.
2. Root `/` (Dockerfile + `railway.toml` / `railway.json`).
3. Health check: `/actuator/health/liveness` (timeout 300s in `railway.toml`).
4. Settings → Networking → **Generate domain** → copy `https://<backend>.up.railway.app`.
5. Variables → paste [`.env.railway.render-db`](.env.railway.render-db).
6. Replace every `CHANGE_ME_BACKEND` / `CHANGE_ME_FRONTEND` after the frontend domain exists (step 3–4), then redeploy once.

### 3. Frontend service

1. New service → GitHub → `x-dev-grp/osm-ms-fe`, branch `pfe-v2-final`.
2. Health check: `/health` (`railway.json`).
3. Generate domain → copy `https://<frontend>.up.railway.app`.
4. Variables → paste [`../osm-ms-fe/.env.railway.render-db`](../osm-ms-fe/.env.railway.render-db).
5. Set `BACKEND_URL=https://<backend>.up.railway.app` (same value as backend public URL).
6. Redeploy frontend.

### 4. Wire URLs on backend (second pass)

On the backend service, set:

```text
JWT_ISSUER_URI=https://<backend>.up.railway.app
JWK_SET_URI=https://<backend>.up.railway.app/oauth2/jwks
INTERNAL_BASE_URL=https://<backend>.up.railway.app
FRONTEND_ENTRY_POINT=https://<frontend>.up.railway.app
APP_CORS_ALLOWED_ORIGIN_PATTERNS=https://<frontend>.up.railway.app,https://*.up.railway.app
```

Redeploy the backend.

### 5. Verify

1. `https://<backend>.up.railway.app/actuator/health/liveness` → `{"status":"UP"}`
2. Optional: `/actuator/health` shows DB up (SSL to Render)
3. Open `https://<frontend>.up.railway.app/` → login with existing Render DB users
4. Render Postgres metrics should show connections from Railway

## Memory notes

Backend env uses SerialGC and `-Xmx384m` (Railway Hobby typically has more RAM than Render free). Keep `HIBERNATE_DDL_AUTO=none`, bootstraps off, and a small Hikari pool (`DB_POOL_MAX_SIZE=3`).

## Related

- Full Railway + Railway Postgres: [RAILWAY_TESTING.md](RAILWAY_TESTING.md)
- Reset / wrong-host troubleshooting: [RAILWAY_RESET.md](RAILWAY_RESET.md)
- Frontend Railway notes: [`../osm-ms-fe/RAILWAY_DEPLOYMENT.md`](../osm-ms-fe/RAILWAY_DEPLOYMENT.md)
