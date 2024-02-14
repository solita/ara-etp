#!/usr/bin/env bash
set -e

containername=${1:-'etp-db'}

docker build . --tag $containername
