# Railway — clean reset (fix `UnknownHostException: dpg-...`)

The `dpg-*` hostname is from **Render**, not Railway. Something on the backend still points at the old database.

Use this guide to wipe bad config and wire Railway Postgres correctly.

---

## Option A — Reset variables only (keep Postgres data)

### 1. Open the right place in Railway

1. Project → select environment (**testing** or **production**)
2. Click the **backend** service (Spring Boot / `oosm`), **not** Postgres
3. Go to **Variables**

Also check **Project → Shared Variables** — delete `DB_URL`, `PGHOST`, or any `dpg-*` value there.

### 2. Delete these variables on the backend

Remove **every** database variable that is a plain text value (not a reference). Especially:

| Delete if present | Why |
|-------------------|-----|
| `DB_URL` | Old Render JDBC URL (`dpg-...`) |
| `PGHOST` | May be set to `dpg-...` |
| `DB_USER`, `DB_PASS` | Old Render credentials |
| `PGUSER`, `PGPASSWORD`, `PGPORT`, `PGDATABASE` | Only if plain text, not references |
| `DATABASE_PUBLIC_URL` | External proxy — wrong for in-Railway app |

**Keep nothing** that contains `dpg-`, `render.com`, or `onrender.com`.

### 3. Add Postgres references (backend service)

**Variables → + New Variable → Add Reference** → pick your Postgres service.

Add these (service name must match exactly — default is **`Postgres`**):

```
DATABASE_URL  →  Postgres.DATABASE_URL
DB_USER       →  Postgres.PGUSER
DB_PASS       →  Postgres.PGPASSWORD
PGHOST        →  Postgres.PGHOST
PGPORT        →  Postgres.PGPORT
PGDATABASE    →  Postgres.PGDATABASE
```

Or paste from [`railway-db.references`](railway-db.references).

**Correct internal host:** `postgres.railway.internal`  
**Wrong hosts:** `dpg-*`, `*.proxy.rlwy.net` (public URL)

### 4. Set non-DB variables (backend)

Minimum after DB reset:

```
JWT_SECRET=<your-secret>
JWT_ISSUER_URI=https://<backend>.up.railway.app
JWK_SET_URI=https://<backend>.up.railway.app/oauth2/jwks
INTERNAL_BASE_URL=https://<backend>.up.railway.app
FRONTEND_ENTRY_POINT=https://<frontend>.up.railway.app
APP_CORS_ALLOWED_ORIGIN_PATTERNS=https://<frontend>.up.railway.app,https://*.up.railway.app
HIBERNATE_DDL_AUTO=update
SECURITY_BOOTSTRAP_ENABLED=true
SECURITY_BOOTSTRAP_USERNAME=osmAdmin
SECURITY_BOOTSTRAP_PASSWORD=<pick-a-password>
SECURITY_BOOTSTRAP_EMAIL=osmAdmin@example.com
```

Copy full list from [`.env.railway.example`](.env.railway.example).

### 5. Redeploy

Backend service → **Deployments** → **Redeploy** (or push to `develop` / `testing`).

### 6. Verify

In deploy logs, connection should **not** mention `dpg-`. Health check:

```
https://<backend>.up.railway.app/actuator/health/liveness
```

---

## Option B — Full reset (new Postgres + backend)

Use when Option A still fails or you want an empty database.

### 1. Delete services

In Railway project:

1. **Delete** the backend service
2. **Delete** the Postgres service (destroys all data)
3. Optionally delete the frontend service too

### 2. Recreate Postgres

**+ New → Database → PostgreSQL**

Note the service name (default: **Postgres**).

### 3. Recreate backend

**+ New → GitHub Repo → `oosm`**

- Root: `/`
- Build: Dockerfile
- Health check: `/actuator/health/liveness`

Add variables from step 3–4 in Option A (references + JWT/URLs).

### 4. Recreate frontend

**+ New → GitHub Repo → `osm-ms-fe`**

```
BACKEND_URL=https://<backend-public-url>
```

Update backend `FRONTEND_ENTRY_POINT` and CORS, then redeploy backend.

---

## Option C — CLI reset (from your machine)

```bash
cd oosm
npm install -g @railway/cli
railway login
railway link          # pick project + backend service

# Clean stale vars and set Postgres references
RAILWAY_SERVICE_ID=<backend-service-id> \
RAILWAY_ENVIRONMENT=testing \
RAILWAY_POSTGRES_SERVICE=Postgres \
  sh scripts/railway-link-postgres.sh

railway redeploy --service <backend-service-id>
```

List variables to confirm:

```bash
railway variable list --service <backend-service-id> --kv
```

You should see `${{Postgres.DATABASE_URL}}` style values for DB vars, and **no** `DB_URL`.

---

## Option D — New Railway project (nuclear)

1. Create **Empty project** (e.g. `osm-testing-v2`)
2. Add Postgres → backend → frontend (Option B steps 2–4)
3. Update GitHub secrets `RAILWAY_TOKEN`, `RAILWAY_SERVICE_ID` to new service IDs
4. Disable or delete the old project when the new one works

---

## Common mistakes

| Mistake | Result |
|---------|--------|
| `DB_URL` still set to Render URL | `UnknownHostException: dpg-...` |
| Using `DATABASE_PUBLIC_URL` | Wrong host / SSL issues |
| Postgres service renamed but refs say `Postgres` | Empty / broken references |
| Fixed **production** env but app runs in **testing** | Looks unchanged |
| Old deploy running (no redeploy after var change) | Same error in logs |

---

## Local `.env` (not deployed)

Your repo has `oosm/.env` with Render `DB_URL` for **local dev only**. It is gitignored and not in the Docker image. Railway never reads it unless you paste those values into the dashboard.

For local Postgres:

```
DB_URL=jdbc:postgresql://localhost:5432/osm
DB_USER=postgres
DB_PASS=root
```
