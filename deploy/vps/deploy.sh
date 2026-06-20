#!/usr/bin/env bash
set -euo pipefail

DEPLOY_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$DEPLOY_DIR"

if [ ! -f .env ]; then
  echo "Missing .env in $DEPLOY_DIR" >&2
  echo "Copy .env.example to .env and edit values first." >&2
  exit 1
fi

set -a
# shellcheck disable=SC1091
source .env
set +a

echo "Pulling images..."
docker compose --env-file .env pull

echo "Starting stack..."
docker compose --env-file .env up -d --remove-orphans

echo "Status:"
docker compose ps

echo "Done. Open ${PUBLIC_APP_URL:-http://localhost} when health checks pass."
