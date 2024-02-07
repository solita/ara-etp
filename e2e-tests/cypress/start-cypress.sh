#! /usr/bin/env bash

set -euxo pipefail

# TODO: Change to normal
docker compose down
docker compose up --build --remove-orphans

