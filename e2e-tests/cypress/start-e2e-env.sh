#!/usr/bin/env bash
set -euxo pipefail

# Usage:
#   ./start-e2e-env.sh        # local dev (builds images, uses docker-compose.yml)
#   ./start-e2e-env.sh --ci   # CI mode (pull-only, uses docker-compose.ci.yml)

CI_MODE=false

if [[ "${1:-}" == "--ci" ]]; then
  CI_MODE=true
fi

# Choose compose file:
# - local: docker-compose.yml
# - ci:    docker-compose.ci.yml
if $CI_MODE; then
  COMPOSE_FILES=(-f docker-compose.ci.yml)
else
  COMPOSE_FILES=(-f docker-compose.yml)
fi

dc() {
  docker compose "${COMPOSE_FILES[@]}" "$@"
}

echo "==> Starting e2e environment"
if $CI_MODE; then
  echo "==> CI mode enabled (no builds, using docker-compose.ci.yml)"
else
  echo "==> Local mode (builds enabled, using docker-compose.yml)"
fi

# -------------------------------------------------------------------
# 1. Start frontend early (parallelises webpack startup)
# -------------------------------------------------------------------
if $CI_MODE; then
  dc up -d frontend
else
  dc up --build -d frontend
fi

# -------------------------------------------------------------------
# 2. Run DB migrations (short-lived container)
# -------------------------------------------------------------------
if $CI_MODE; then
  dc up etp-db-for-etp_dev --exit-code-from etp-db-for-etp_dev
else
  dc up --build etp-db-for-etp_dev --exit-code-from etp-db-for-etp_dev
fi

# -------------------------------------------------------------------
# 3. Recreate cypress_test database from etp_dev template
# -------------------------------------------------------------------
dc exec db dropdb -U postgres cypress_test --if-exists --force
dc exec db createdb -U postgres -T etp_dev cypress_test

# -------------------------------------------------------------------
# 4. OCSP workaround
#    In CI we assume images are already pulled and ready.
# -------------------------------------------------------------------
if ! $CI_MODE; then
  # Local dev only: ensure OCSP images are built
  dc build ocsp-responder-root ocsp-responder-int
fi

# -------------------------------------------------------------------
# 5. Start remaining services and wait for health
# -------------------------------------------------------------------
echo -e "\e[1;33m Waiting for services to be healthy. Can take ~20s. \e[0m"

if $CI_MODE; then
  dc up --wait frontend frontend-public backend
else
  dc up --build --wait frontend frontend-public backend
fi
