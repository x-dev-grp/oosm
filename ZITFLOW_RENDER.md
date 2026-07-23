# ZitFlow — Render blueprints (FE + Postgres)

Hybrid target:

```text
Browser → zitflow.xdev.pro (Render: zitflow-web)
              │ BACKEND_URL
              ▼
         oosm-api-production.up.railway.app (Railway)
              ▼
         Render Postgres: zitflow-postgres (Frankfurt) — restored from oosm_fawv
```

## Blueprints in git

| File | Repo | Creates |
|------|------|---------|
| [`../osm-ms-fe/render.zitflow.yaml`](../osm-ms-fe/render.zitflow.yaml) | `osm-ms-fe` | **Postgres + frontend** (preferred) |
| [`render.zitflow.yaml`](render.zitflow.yaml) | `oosm` | **Postgres only** |
| [`render.yaml`](render.yaml) | `oosm` | Legacy Render API — **do not use** for ZitFlow |

Branch: **`deploy/zitflow-xdev-pro`**.

## Apply on Render

1. Open [Render Dashboard](https://dashboard.render.com) → **New** → **Blueprint**.
2. Connect **`x-dev-grp/osm-ms-fe`** (or `oosm` for DB-only).
3. Branch: `deploy/zitflow-xdev-pro`.
4. Blueprint path: `render.zitflow.yaml` (if the UI asks; otherwise rename temporarily or paste).
5. Review plan/region (**frankfurt**), then apply.
6. Wait until **zitflow-postgres** is Available and **zitflow-web** has a `.onrender.com` URL.

Do **not** apply the legacy root `render.yaml` for this cutover (it still targets the old Render API).

## After DB is up — dump / restore / Railway

1. Dump current prod:

```bash
pg_dump -Fc --no-owner --no-acl \
  -h <oosm_fawv-external-host>.frankfurt-postgres.render.com \
  -U oosm_fawv_user -d oosm_fawv \
  -f oosm_fawv_YYYYMMDD.dump
```

2. Restore into **zitflow** (External connection from Render → zitflow-postgres):

```bash
pg_restore --verbose --clean --if-exists --no-owner --no-acl \
  -h <zitflow-external-host>.frankfurt-postgres.render.com \
  -U zitflow -d zitflow \
  oosm_fawv_YYYYMMDD.dump
```

3. On Railway backend variables (see `.env.railway.render-db.example`):

```text
DB_URL=jdbc:postgresql://<zitflow-external-host>.frankfurt-postgres.render.com:5432/zitflow?sslmode=require
DB_USER=zitflow
DB_PASS=<from Render>
FRONTEND_ENTRY_POINT=https://zitflow.xdev.pro
APP_CORS_ALLOWED_ORIGIN_PATTERNS=https://zitflow.xdev.pro,https://*.onrender.com
```

Use the **External** Database URL host only (Railway cannot resolve Render internal `dpg-…-a` hosts).

4. Redeploy Railway → check `/actuator/health/liveness`.

## Frontend domain

1. Render → **zitflow-web** → Custom Domains → add `zitflow.xdev.pro`.
2. DNS CNAME `zitflow` (or as required) → Render target.
3. Confirm `BACKEND_URL=https://oosm-api-production.up.railway.app`.

## Notes

- Plan `basic-256mb` is a starting point — raise disk/RAM to match `oosm_fawv` before production cutover.
- Keep old `oosm_fawv` until restore and smoke tests pass, then suspend after retention.
- Do not add Railway Postgres or run `scripts/railway-link-postgres.sh` for this stack.
