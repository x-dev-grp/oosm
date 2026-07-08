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
  esac
fi

export DB_USER="${DB_USER:-${PGUSER:-postgres}}"
export DB_PASS="${DB_PASS:-${PGPASSWORD:-}}"

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

# Render free/starter instances are capped at 512MiB.
apply_compact_jvm() {
  if [ "${OOSM_SKIP_JVM_TUNING:-false}" = "true" ]; then
    return 0
  fi
  export JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:+UseSerialGC -Xms48m -Xmx192m -Xss384k -XX:MaxMetaspaceSize=96m -XX:ReservedCodeCacheSize=32m -XX:MaxDirectMemorySize=16m -XX:+ExitOnOutOfMemoryError"
  echo "JVM: compact profile for 512MiB containers (heap=192m, metaspace=96m)"
}

apply_compact_jvm

listen_port="${PORT:-${SERVER_PORT:-8084}}"
echo "Starting oosm-monolith on 0.0.0.0:${listen_port} (PORT=${PORT:-unset}, SERVER_PORT=${SERVER_PORT:-unset})"

exec java ${JAVA_OPTS:-} -jar /app/oosm-monolith.jar
