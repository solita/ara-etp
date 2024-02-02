#! /usr/bin/env bash

set -euxo pipefail

docker compose -f ./docker-compose.yml -f ../../etp-core/docker/docker-compose.yml up
