#!/usr/bin/env sh
# Link backend service to Railway Postgres via reference variables.
# Run once after linking the project: railway link
#
# Usage:
#   RAILWAY_POSTGRES_SERVICE=Postgres ./scripts/railway-link-postgres.sh
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

echo "Linking Postgres service '${POSTGRES_SERVICE}' to backend..."

# Remove stale Render/Supabase DB_URL if present (it overrides DATABASE_URL and breaks Railway DNS).
# shellcheck disable=SC2086
railway variable delete DB_URL ${SERVICE_ARGS} ${ENV_ARGS} --skip-deploys 2>/dev/null || true

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
  ${SERVICE_ARGS} \
  ${ENV_ARGS} \
  --skip-deploys

echo "Done. Redeploy the backend if it was already running."
