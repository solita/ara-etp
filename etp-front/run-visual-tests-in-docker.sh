#!/usr/bin/env bash

if [ "$1" == 'update' ]
then
  update=true
else
  update=false
fi
docker build -t etp-front-visual-tests -f visual-tests.Dockerfile .

docker run --name etp-front-visual-tests-container etp-front-visual-tests ./run-visual-tests.sh "$update"

if [ "$update" == true ]
then
  docker cp etp-front-visual-tests-container:/visual-tests/__snapshots__ .
fi

docker rm etp-front-visual-tests-container
