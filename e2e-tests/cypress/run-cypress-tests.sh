#!/usr/bin/env bash


echo -e "\e[1;33m Waiting for frontend to be healthy. Can take ~20s. \e[0m"
docker compose up --build --wait frontend
npm run cypress:run
# Will also stop the stack if it was already manually stareted.
# If this turns out to be a problem could set the project name (-p)
# for `docker compose` in this script and make the mapped frontend's port
# (3009 by default) configurable.
docker compose stop
