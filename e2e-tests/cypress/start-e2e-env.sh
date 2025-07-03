#!/usr/bin/env bash
set -euxo pipefail

# Optimised start order
# Start building frontend
# Start migration runner, which requires database to be ready
# When migrations are ready, create cypress_test database
# Start backend and wait for it and frontend to be ready

docker compose up --build -d frontend
docker compose up --build migration-runner --exit-code-from migration-runner
docker compose exec db dropdb -U postgres cypress_test --if-exists --force
docker compose exec db createdb -U postgres -T etp_dev cypress_test

# Workaround for an apparently random issue when running under GitHub Actions
# The dependencies of the backend service do not always seem to be built before
# compose attempts to start the backend service, and that will cause the final
# `docker compose up` command to fail.
docker compose build ocsp-responder-root

echo -e "\e[1;33m Waiting for services to be healthy. Can take ~20s. \e[0m"
docker compose up --build --wait frontend --wait frontend-public --wait backend
