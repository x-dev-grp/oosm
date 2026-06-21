# VPS deployment (Docker Compose)

Deploy the full OSM stack on a Linux VPS: PostgreSQL, Spring Boot backend, Angular/nginx frontend.

## Prerequisites

- Ubuntu 22.04+ (or similar) with Docker Engine and Compose plugin
- DNS A/AAAA record pointing to the VPS
- GitHub Container Registry access to `ghcr.io/x-dev-grp/oosm-backend` and `osm-frontend`

## One-time VPS setup

```bash
# On the VPS (as root)
curl -fsSL https://raw.githubusercontent.com/x-dev-grp/oosm/main/deploy/vps/setup-vps.sh | bash

mkdir -p /opt/osm
# Copy these files from the oosm repo into /opt/osm:
#   deploy/vps/docker-compose.yml
#   deploy/vps/.env.example
#   deploy/vps/deploy.sh

cd /opt/osm
cp .env.example .env
nano .env

# Login so the VPS can pull private GHCR images (if packages are private)
docker login ghcr.io
chmod +x deploy.sh
./deploy.sh
```

Put TLS in front with Caddy, nginx, or Traefik on the host, or terminate HTTPS at a reverse proxy and forward to `WEB_PORT` (default 80).

## Environment variables

| Variable | Description |
|----------|-------------|
| `PUBLIC_APP_URL` | Public app URL, e.g. `https://osm.example.com` |
| `DB_PASS` | PostgreSQL password |
| `JWT_SECRET` | Long random string (32+ chars) for token signing |
| `BACKEND_IMAGE` / `FRONTEND_IMAGE` | GHCR image references |

See `.env.example` for the full list.

## GitHub Actions

### Backend repo (`oosm`)

| Workflow | Trigger | Purpose |
|----------|---------|---------|
| `ci.yml` | PR / push | Maven verify |
| `docker-publish.yml` | push `main` / `release`, tags `v*` | Push `ghcr.io/x-dev-grp/oosm-backend` |
| `deploy-vps.yml` | Manual | SSH deploy to VPS |

### Frontend repo (`osm-ms-fe`)

| Workflow | Trigger | Purpose |
|----------|---------|---------|
| `ci.yml` | PR / push | npm ci + production build |
| `docker-publish.yml` | push `main` / `release` | Push `ghcr.io/x-dev-grp/osm-frontend` |

### GitHub secrets (backend repo, environment `vps`)

| Secret | Description |
|--------|-------------|
| `VPS_HOST` | VPS IP or hostname |
| `VPS_USER` | SSH user (e.g. `deploy`) |
| `VPS_SSH_PRIVATE_KEY` | Private key for SSH |
| `VPS_DEPLOY_PATH` | e.g. `/opt/osm` |
| `VPS_SSH_PORT` | Optional, default 22 |
| `GHCR_DEPLOY_USER` | GitHub user or bot for GHCR pull |
| `GHCR_DEPLOY_TOKEN` | PAT with `read:packages` |

### Deploy from GitHub

1. Publish images (push to `main` in both repos, or run publish workflows manually).
2. In `oosm` → Actions → **Deploy to VPS** → Run workflow.
3. Optionally set image tags (default: `release-latest` for both).

## Local production-like test

```bash
cd deploy/vps
cp .env.example .env
# Edit PUBLIC_APP_URL, secrets, etc.
docker compose --env-file .env up -d
```

## Updating

```bash
cd /opt/osm
docker compose pull
./deploy.sh
```

Or trigger **Deploy to VPS** from GitHub Actions.
