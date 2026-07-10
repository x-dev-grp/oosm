#!/bin/sh
# Parse Railway/Render DATABASE_URL into a clean JDBC URL (host only, no credentials in URL).
set -eu

configure_from_url() {
  url="$1"
  url="${url#postgres://}"
  url="${url#postgresql://}"
  url="${url#jdbc:postgresql://}"

  if [ -z "$url" ]; then
    echo "ERROR: empty database URL" >&2
    exit 1
  fi

  case "$url" in
    *@*)
      creds="${url%%@*}"
      rest="${url#*@}"
      if [ -z "${DB_USER:-}" ] && [ -z "${PGUSER:-}" ]; then
        export DB_USER="${creds%%:*}"
      fi
      if [ -z "${DB_PASS:-}" ] && [ -z "${PGPASSWORD:-}" ]; then
        export DB_PASS="${creds#*:}"
      fi
      ;;
    *)
      rest="$url"
      ;;
  esac

  hostport="${rest%%/*}"
  dbpath="${rest#*/}"
  dbpath="${dbpath%%\?*}"

  export PGHOST="${hostport%%:*}"
  export PGPORT="${hostport#*:}"
  if [ "$PGPORT" = "$hostport" ]; then
    export PGPORT=5432
  fi
  if [ -z "${PGDATABASE:-}" ]; then
    export PGDATABASE="${dbpath:-railway}"
  fi
  export DB_URL="jdbc:postgresql://${PGHOST}:${PGPORT}/${PGDATABASE}"
  # Keep SSL for Render public hosts (configure_from_url strips ?query).
  case "${PGHOST}" in
    *.render.com|dpg-*)
      export DB_URL="${DB_URL}?sslmode=require"
      ;;
  esac
}

parse_jdbc_url() {
  url="$1"
  url="${url#jdbc:postgresql://}"
  hostport="${url%%/*}"
  dbpath="${url#*/}"
  dbpath="${dbpath%%\?*}"

  export PGHOST="${hostport%%:*}"
  export PGPORT="${hostport#*:}"
  if [ "$PGPORT" = "$hostport" ]; then
    export PGPORT=5432
  fi
  if [ -z "${PGDATABASE:-}" ]; then
    export PGDATABASE="${dbpath:-postgres}"
  fi
}

if [ -n "${DATABASE_URL:-}" ]; then
  configure_from_url "$DATABASE_URL"
elif [ -n "${DB_URL:-}" ]; then
  case "$DB_URL" in
    jdbc:postgresql://*@*|postgresql://*@*|postgres://*@*)
      configure_from_url "$DB_URL"
      ;;
    jdbc:postgresql://*)
      parse_jdbc_url "$DB_URL"
      ;;
    *)
      echo "ERROR: DB_URL must start with jdbc:postgresql:// (got: ${DB_URL%%:*}:...)" >&2
      exit 1
      ;;
  esac
fi

export DB_USER="${DB_USER:-${PGUSER:-postgres}}"
export DB_PASS="${DB_PASS:-${PGPASSWORD:-}}"

# Fail fast — Spring defaults to localhost:5432 when DB_URL is missing.
if [ -z "${DB_URL:-}" ]; then
  echo "ERROR: DB_URL (or DATABASE_URL) is not set. Paste oosm/.env.railway.render-db on the backend service." >&2
  echo "ERROR: Do not rely on localhost — there is no Postgres inside this container." >&2
  exit 1
fi

case "${DB_URL}" in
  *localhost*|*127.0.0.1*)
    echo "ERROR: DB_URL points at localhost. For Railway + Render DB use the external host:" >&2
    echo "ERROR: jdbc:postgresql://dpg-….frankfurt-postgres.render.com:5432/oosm_fawv?sslmode=require" >&2
    exit 1
    ;;
esac

# PGHOST must be hostname only — credentials belong in DB_USER / DB_PASS.
case "${PGHOST:-}" in
  *@*)
    echo "ERROR: PGHOST contains '@' (likely copied from DATABASE_URL). Use Postgres.PGHOST reference or unset PGHOST." >&2
    exit 1
    ;;
  *:*)
    echo "ERROR: PGHOST contains ':' (likely user:pass@host). Set PGHOST to postgres.railway.internal only." >&2
    exit 1
    ;;
esac

echo "Database: ${PGHOST:-from DB_URL}:${PGPORT:-5432}/${PGDATABASE:-?} user=${DB_USER}"
echo "JDBC URL host: $(echo "$DB_URL" | sed -E 's#jdbc:postgresql://([^:/?]+).*#\1#')"

listen_port="${PORT:-${SERVER_PORT:-8084}}"
export SERVER_PORT="$listen_port"
echo "Starting oosm-monolith on 0.0.0.0:${listen_port} (PORT=${PORT:-unset}, SERVER_PORT=${SERVER_PORT})"

exec java ${JAVA_OPTS:-} -Dserver.port="${listen_port}" -jar /app/oosm-monolith.jar
