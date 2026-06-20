# OSM 2.0 — Railway testing deployment

Deploy the **backend**, **frontend**, and **PostgreSQL** to [Railway](https://railway.com) for integration/testing. Production VPS flow remains in [deploy/vps/README.md](deploy/vps/README.md).

## Architecture on Railway

```text
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────┐
│  osm-ms-fe      │────▶│  oosm (backend)  │────▶│  PostgreSQL     │
│  nginx + Angular│     │  Spring Boot     │     │  (Railway plugin)│
└─────────────────┘     └──────────────────┘     └─────────────────┘
   *.up.railway.app         *.up.railway.app
```

Users hit the **frontend** URL. Nginx proxies API, OAuth, WebSocket (`/ws`), and actuator paths to the backend.

## 1. Create Railway project

1. New project → **Empty project** (name e.g. `osm-testing`).
2. Create environment **`testing`** (or use default).
3. Add **PostgreSQL** (Railway plugin).

## 2. Backend service (`oosm` repo)

1. **New service** → **GitHub repo** → select `x-dev-grp/oosm` (or your fork).
2. Root directory: `/` (contains `Dockerfile`, `railway.json`).
3. Settings → **Build**: Dockerfile (`railway.json` already sets this).
4. Settings → **Deploy** → health check: `/actuator/health/liveness`.

### Backend variables

Copy from [`.env.railway.example`](.env.railway.example). Minimum:

| Variable | Value |
|----------|--------|
| `DATABASE_URL` or `DB_URL` | `${{Postgres.DATABASE_URL}}` or JDBC URL |
| `DB_USER` / `DB_PASS` | From Postgres reference if not using `DATABASE_URL` alone |
| `HIBERNATE_DDL_AUTO` | `update` (testing) |
| `JWT_ISSUER_URI` | `https://<backend-public-domain>` |
| `JWK_SET_URI` | `https://<backend-public-domain>/oauth2/jwks` |
| `INTERNAL_BASE_URL` | Same as public backend URL |
| `FRONTEND_ENTRY_POINT` | Frontend public URL (set after frontend deploy) |
| `APP_CORS_ALLOWED_ORIGIN_PATTERNS` | `https://<frontend-domain>,https://*.up.railway.app` |
| `OAUTH2_CLIENT_SECRET` | Strong secret (match frontend client config) |
| `SECURITY_BOOTSTRAP_*` | Optional first admin (disable after login) |

`Dockerfile` converts Railway `DATABASE_URL` (`postgres://…`) to JDBC when `DB_URL` is unset.

After first deploy, run optional SQL from [RAILWAY_DATABASE_BOOTSTRAP.md](RAILWAY_DATABASE_BOOTSTRAP.md) if you need seeds beyond Hibernate `update`.

## 3. Frontend service (`osm-ms-fe` repo)

1. **New service** → **GitHub repo** → select frontend repo.
2. Uses `Dockerfile` + `railway.json` (health check `/`).

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
3. Chat: WebSocket via `/ws` (proxied by frontend nginx)

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

- Frontend: `../osm-ms-fe/RAILWAY_DEPLOYMENT.md`
- Database scripts: [RAILWAY_DATABASE_BOOTSTRAP.md](RAILWAY_DATABASE_BOOTSTRAP.md)
- VPS production: [deploy/vps/README.md](deploy/vps/README.md)
