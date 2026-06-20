#!/usr/bin/env sh
# Clean stale DB variables and link backend to Railway Postgres via references.
#
# Usage:
#   railway link
#   RAILWAY_SERVICE_ID=<backend-id> RAILWAY_ENVIRONMENT=testing ./scripts/railway-link-postgres.sh

set -eu

POSTGRES_SERVICE="${RAILWAY_POSTGRES_SERVICE:-Postgres}"

SERVICE_ARGS=""
if [ -n "${RAILWAY_SERVICE_ID:-}" ]; then
  SERVICE_ARGS="-s ${RAILWAY_SERVICE_ID}"
fi

ENV_ARGS=""
if [ -n "${RAILWAY_ENVIRONMENT:-}" ]; then
  ENV_ARGS="-e ${RAILWAY_ENVIRONMENT}"
fi

delete_var() {
  name="$1"
  # shellcheck disable=SC2086
  railway variable delete "$name" ${SERVICE_ARGS} ${ENV_ARGS} --skip-deploys 2>/dev/null || true
}

echo "=== Railway DB reset: removing stale variables ==="
for name in \
  DB_URL DB_USER DB_PASS \
  PGHOST PGPORT PGDATABASE PGUSER PGPASSWORD \
  DATABASE_PUBLIC_URL POSTGRES_URL POSTGRES_HOST POSTGRES_PORT \
  POSTGRES_USER POSTGRES_PASSWORD POSTGRES_DB
do
  delete_var "$name"
done

echo "=== Linking Postgres service '${POSTGRES_SERVICE}' ==="
# shellcheck disable=SC2086
railway variable set \
  "DATABASE_URL=\${{${POSTGRES_SERVICE}.DATABASE_URL}}" \
  "DB_USER=\${{${POSTGRES_SERVICE}.PGUSER}}" \
  "DB_PASS=\${{${POSTGRES_SERVICE}.PGPASSWORD}}" \
  "PGHOST=\${{${POSTGRES_SERVICE}.PGHOST}}" \
  "PGPORT=\${{${POSTGRES_SERVICE}.PGPORT}}" \
  "PGDATABASE=\${{${POSTGRES_SERVICE}.PGDATABASE}}" \
  "PGUSER=\${{${POSTGRES_SERVICE}.PGUSER}}" \
  "PGPASSWORD=\${{${POSTGRES_SERVICE}.PGPASSWORD}}" \
  "HIBERNATE_DDL_AUTO=update" \
  "SHOW_SQL=false" \
  ${SERVICE_ARGS} \
  ${ENV_ARGS} \
  --skip-deploys

echo ""
echo "=== Current backend variables (check for dpg- or DB_URL) ==="
# shellcheck disable=SC2086
railway variable list ${SERVICE_ARGS} ${ENV_ARGS} --kv 2>/dev/null | grep -E '^(DATABASE_URL|DB_|PG|POSTGRES)' || true

echo ""
echo "Done. Redeploy the backend:"
echo "  railway redeploy ${SERVICE_ARGS} ${ENV_ARGS}"
