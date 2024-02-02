#! /usr/bin/env bash

set -euxo pipefail

docker compose -f ./docker-compose.yml -f ../../e2e-tests/cypress/docker-compose.yml $@
