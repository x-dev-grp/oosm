# OOSM 2.0 — Railway testing deployment

Deploy the **backend**, **frontend**, and **PostgreSQL** to [Railway](https://railway.com) for integration/testing. Production VPS flow remains in [deploy/vps/README.md](deploy/vps/README.md).

**Hybrid (Railway backend only + Render frontend + Render Postgres):** see [RAILWAY_RENDER_DB.md](RAILWAY_RENDER_DB.md). Do not add a Railway Postgres plugin or run `scripts/railway-link-postgres.sh` for that setup.

## Architecture on Railway

```text
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────┐
│  osm-ms-fe      │────▶│  oosm (backend)  │────▶│  PostgreSQL     │
│  nginx + Angular│     │  Spring Boot     │     │  (Railway plugin)│
└─────────────────┘     └──────────────────┘     └─────────────────┘
   *.up.railway.app         *.up.railway.app
```

Users hit the **frontend** URL. Nginx proxies API, OAuth, and actuator paths to the backend.

## 1. Create Railway project

1. New project → **Empty project** (name e.g. `osm-testing`).
2. Create environment **`testing`** (or use default).
3. Add **PostgreSQL** (Railway plugin).

## 2. Backend service (`oosm` repo)

1. **New service** → **GitHub repo** → select `x-dev-grp/oosm` (or your fork).
2. Root directory: `/` (contains `Dockerfile`, `railway.json`).
3. Settings → **Build**: Dockerfile (`railway.json` already sets this).
4. Settings → **Deploy** → health check: `/actuator/health/liveness`.
5. Ensure the PostgreSQL plugin service is named **`Postgres`** (Railway default). If you renamed it, edit [`railway.toml`](railway.toml) references (`${{YourDbName.DATABASE_URL}}`, etc.).

### Backend variables (automatic Postgres link)

On each deploy, GitHub Actions runs [`scripts/railway-link-postgres.sh`](scripts/railway-link-postgres.sh) to set reference variables on the backend service:

| Variable | Reference |
|----------|-----------|
| `DATABASE_URL` | `${{Postgres.DATABASE_URL}}` (internal — **not** `DATABASE_PUBLIC_URL`) |
| `DB_USER` / `DB_PASS` | `${{Postgres.PGUSER}}` / `${{Postgres.PGPASSWORD}}` |
| `PGHOST`, `PGPORT`, `PGDATABASE` | `${{Postgres.PGHOST}}` etc. — **hostname only**, never paste `DATABASE_URL` into `PGHOST` |

**Requirements**

1. Postgres plugin service must be named **`Postgres`** (Railway default), or set repo variable `RAILWAY_POSTGRES_SERVICE_NAME` to your DB service name (case-sensitive).
2. GitHub secret `RAILWAY_SERVICE_ID` = backend service ID (not Postgres).

**One-time manual setup** (without CI): from `oosm/` with `railway link`:

```bash
chmod +x scripts/railway-link-postgres.sh
RAILWAY_SERVICE_ID=<backend-service-id> ./scripts/railway-link-postgres.sh
```

Or paste lines from [`railway-db.references`](railway-db.references) in the dashboard: **Variables** → **Add variable** → **Add reference**.

Also set on the backend service (copy from [`.env.railway.example`](.env.railway.example)):
| `JWT_ISSUER_URI` | `https://<backend-public-domain>` |
| `JWK_SET_URI` | `https://<backend-public-domain>/oauth2/jwks` |
| `INTERNAL_BASE_URL` | Same as public backend URL |
| `FRONTEND_ENTRY_POINT` | Frontend public URL (set after frontend deploy) |
| `APP_CORS_ALLOWED_ORIGIN_PATTERNS` | `https://<frontend-domain>,https://*.up.railway.app` |
| `JWT_SECRET` | Long random string (32+ chars) — same value on every redeploy |
| `SECURITY_BOOTSTRAP_*` | Optional first admin (disable after login) |

`Dockerfile` converts Railway `DATABASE_URL` (`postgres://…`) to JDBC. **`DATABASE_URL` always wins** — do not set `DB_URL` on Railway.

JWT keys use a single **`JWT_SECRET`** environment variable (symmetric HS256). No volume or key files required. Local dev uses the default in `application.yml` unless you override `JWT_SECRET`.

After first deploy, run optional SQL from [RAILWAY_DATABASE_BOOTSTRAP.md](RAILWAY_DATABASE_BOOTSTRAP.md) if you need seeds beyond Hibernate `update`.

### Troubleshooting: `UnknownHostException: dpg-...`

The backend is using an **old Render database URL**, not Railway Postgres.

1. Railway → **backend service** → **Variables**
2. **Delete** `DB_URL` if it contains `dpg-` or `render.com`
3. Ensure `DATABASE_URL` = `${{Postgres.DATABASE_URL}}` (reference, not a pasted public URL)
4. Redeploy

Or run `./scripts/railway-link-postgres.sh` — it removes `DB_URL` and sets Postgres references.

### Troubleshooting: `UnknownHostException: postgres:password@postgres.railway.internal`

`PGHOST` was set to `user:password@host` instead of just `postgres.railway.internal`.

1. Delete `PGHOST` if its value contains `@` or `:`
2. Re-add **reference**: `PGHOST` → `Postgres.PGHOST` (value must be `postgres.railway.internal`)
3. Ensure `DATABASE_URL` → `Postgres.DATABASE_URL` (reference)
4. Redeploy (new `docker/entrypoint.sh` parses `DATABASE_URL` correctly)

## 3. Frontend service (`osm-ms-fe` repo)

1. **New service** → **GitHub repo** → select frontend repo.
2. Uses `Dockerfile` + `railway.json` (health check `/health`).

| Variable | Value |
|----------|--------|
| `BACKEND_URL` | `https://<backend-public-domain>` |

Redeploy backend after setting `FRONTEND_ENTRY_POINT` and CORS to the frontend URL.

## 4. GitHub Actions (optional CI deploy)

Each repo has `.github/workflows/deploy-railway.yml`:

- Triggers on push to **`develop`** or **`testing`**, or manual **Run workflow**.
- Uses Railway CLI: `railway up --detach --ci`.

### GitHub environment: `railway-test`

Create environment **`railway-test`** in each repository and add secrets:

| Secret | Backend repo | Frontend repo |
|--------|--------------|---------------|
| `RAILWAY_TOKEN` | Project token from Railway → Settings → Tokens | Same project token |
| `RAILWAY_SERVICE_ID` | Backend service UUID | Frontend service UUID |
| `RAILWAY_ENVIRONMENT_NAME` | Optional, default `testing` | Optional |

Get service ID: Railway service → Settings → General → **Service ID**.

### Branch strategy

| Branch | Railway |
|--------|---------|
| `develop` / `testing` | Auto-deploy via GitHub Actions |
| `main` | GHCR images + VPS (see deploy/vps) |

You can also enable **Railway native GitHub deploy** on each service (Settings → Source) and skip Actions.

## 5. Verify

1. Backend: `https://<backend>/actuator/health/liveness` → `UP`
2. Frontend: open `https://<frontend>/` → login page

## 6. Local parity

```powershell
# Backend
cd oosm
docker compose up -d postgres
mvn -pl app spring-boot:run

# Frontend
cd osm-ms-fe
npm start
```

## Related docs

- **Railway app + Render DB (no Railway Postgres):** [RAILWAY_RENDER_DB.md](RAILWAY_RENDER_DB.md)
- **Clean reset / fix dpg- errors:** [RAILWAY_RESET.md](RAILWAY_RESET.md)
- Frontend: `../osm-ms-fe/RAILWAY_DEPLOYMENT.md`
- Database scripts: [RAILWAY_DATABASE_BOOTSTRAP.md](RAILWAY_DATABASE_BOOTSTRAP.md)
- VPS production: [deploy/vps/README.md](deploy/vps/README.md)
