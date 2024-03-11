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
echo -e "\e[1;33m Waiting for frontend to be healthy. Can take ~20s. \e[0m"
docker compose up --build --wait frontend --wait backend
