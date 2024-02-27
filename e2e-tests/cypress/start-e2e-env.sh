#!/usr/bin/env bash
set -euxo pipefail

docker compose up --build migration-runner
docker compose exec db dropdb -U postgres cypress_test --if-exists --force
docker compose exec db createdb -U postgres -T etp_dev cypress_test
echo -e "\e[1;33m Waiting for frontend to be healthy. Can take ~20s. \e[0m"
docker compose up --build --wait frontend
