#!/usr/bin/env bash
set -euxo pipefail

ETP_2026=
if [[ " $* " == *" --etp2026 "* ]]; then
  export ETP_2026='true'   # set and export only when flag is used
else
  unset ETP_2026           # completely unset when flag is not used
fi

# Optimised start order
# Start building frontend
# Start etp-db, which requires database to be ready
# When migrations are ready, create cypress_test database
# Start backend and wait for it and frontend to be ready

docker compose up --build -d frontend
docker compose up --build etp-db-for-etp_dev --exit-code-from etp-db-for-etp_dev
docker compose exec db dropdb -U postgres cypress_test --if-exists --force
docker compose exec db createdb -U postgres -T etp_dev cypress_test

echo -e "\e[1;33m Waiting for services to be healthy. Can take ~20s. \e[0m"
docker compose up --build --wait frontend --wait frontend-public --wait backend
