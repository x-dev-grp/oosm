#!/usr/bin/env bash
set -euo pipefail

# One-time VPS bootstrap for Ubuntu 22.04+ / Debian 12+
# Run as root or with sudo: curl -fsSL ... | bash

if ! command -v docker >/dev/null 2>&1; then
  apt-get update
  apt-get install -y ca-certificates curl gnupg
  install -m 0755 -d /etc/apt/keyrings
  curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
  chmod a+r /etc/apt/keyrings/docker.gpg
  echo \
    "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
    $(. /etc/os-release && echo "$VERSION_CODENAME") stable" \
    > /etc/apt/sources.list.d/docker.list
  apt-get update
  apt-get install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
fi

DEPLOY_PATH="${DEPLOY_PATH:-/opt/oosm}"
mkdir -p "$DEPLOY_PATH"
echo "Deploy directory: $DEPLOY_PATH"
echo "Copy deploy/vps/* from the oosm repo into $DEPLOY_PATH, then:"
echo "  cp .env.example .env && nano .env"
echo "  docker login ghcr.io"
echo "  ./deploy.sh"
