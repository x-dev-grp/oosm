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

load_app_setting_value() {
  setting_key="$1"
  if ! command -v psql >/dev/null 2>&1; then
    return 0
  fi
  if [ -z "${PGHOST:-}" ] || [ -z "${PGDATABASE:-}" ]; then
    return 0
  fi
  PGPASSWORD="${DB_PASS}" psql \
    -h "${PGHOST}" \
    -p "${PGPORT:-5432}" \
    -U "${DB_USER}" \
    -d "${PGDATABASE}" \
    -tAc "SELECT value FROM app_setting WHERE setting_key = '${setting_key}' AND value IS NOT NULL AND trim(value) <> '' LIMIT 1" 2>/dev/null \
    | tr -d '\r\n' \
    | sed 's/^[[:space:]]*//;s/[[:space:]]*$//'
}

bootstrap_new_relic_from_db() {
  if ! command -v psql >/dev/null 2>&1; then
    echo "New Relic DB bootstrap skipped: psql not installed"
    return 0
  fi

  if [ -z "${NEW_RELIC_APM_ENABLED:-}" ]; then
    db_enabled="$(load_app_setting_value "NEW_RELIC_APM_ENABLED")"
    if [ -n "$db_enabled" ]; then
      export NEW_RELIC_APM_ENABLED="$db_enabled"
      echo "New Relic: NEW_RELIC_APM_ENABLED loaded from app_setting ($db_enabled)"
    fi
  fi

  if [ -z "${NEW_RELIC_APP_NAME:-}" ]; then
    db_app_name="$(load_app_setting_value "NEW_RELIC_APP_NAME")"
    if [ -n "$db_app_name" ]; then
      export NEW_RELIC_APP_NAME="$db_app_name"
      echo "New Relic: NEW_RELIC_APP_NAME loaded from app_setting ($db_app_name)"
    fi
  fi

  if [ -z "${NEW_RELIC_APPLICATION_LOGGING_FORWARDING_ENABLED:-}" ]; then
    db_forwarding="$(load_app_setting_value "NEW_RELIC_LOG_FORWARDING_ENABLED")"
    if [ -n "$db_forwarding" ]; then
      export NEW_RELIC_APPLICATION_LOGGING_FORWARDING_ENABLED="$db_forwarding"
      echo "New Relic: log forwarding loaded from app_setting ($db_forwarding)"
    fi
  fi

  if [ -z "${NEW_RELIC_REGION:-}" ]; then
    db_region="$(load_app_setting_value "NEW_RELIC_REGION")"
    if [ -n "$db_region" ]; then
      export NEW_RELIC_REGION="$db_region"
      echo "New Relic: region loaded from app_setting ($db_region)"
    fi
  fi
}

bootstrap_new_relic_from_db

export NEW_RELIC_REGION="${NEW_RELIC_REGION:-EU}"

# New Relic Java agent (APM + Logback log forwarding).
# NEW_RELIC_LICENSE_KEY must be set on the host (Render env). Other flags may come from app_setting above.
if [ "${NEW_RELIC_APM_ENABLED:-false}" = "true" ] && [ -f /app/newrelic/newrelic.jar ]; then
  if [ -z "${NEW_RELIC_LICENSE_KEY:-}" ]; then
    echo "WARN: NEW_RELIC_APM_ENABLED=true but NEW_RELIC_LICENSE_KEY is not set; skipping Java agent" >&2
    echo "WARN: Set NEW_RELIC_LICENSE_KEY in Render environment variables." >&2
  else
    export NEW_RELIC_LOG="${NEW_RELIC_LOG:-/app/newrelic/newrelic_agent.log}"
    export NEW_RELIC_APP_NAME="${NEW_RELIC_APP_NAME:-oosm-monolith}"
    export NEW_RELIC_APPLICATION_LOGGING_FORWARDING_ENABLED="${NEW_RELIC_APPLICATION_LOGGING_FORWARDING_ENABLED:-true}"
    NR_JAVA_OPTS="-javaagent:/app/newrelic/newrelic.jar -Dnewrelic.config.file=/app/newrelic/newrelic.yml"
    JAVA_OPTS="${JAVA_OPTS:-} ${NR_JAVA_OPTS}"
    export JAVA_OPTS
    echo "New Relic Java agent enabled (app=${NEW_RELIC_APP_NAME}, region=${NEW_RELIC_REGION})"
  fi
fi

exec java ${JAVA_OPTS:-} -jar /app/oosm-monolith.jar
