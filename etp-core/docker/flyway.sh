#!/usr/bin/env bash
set -e

if [ -z "$1" ]
then
  echo "Command is missing!"
  echo "Usage: $0 [migrate or clean]"
  exit 1
fi

cd ../etp-db
# Don't run test migrations to template db
clojure -M -m solita.etp.db.flywaydb $1

# TODO: This needs to go to env also. Or maybe can just run this inside of a container.
# Run test migrations to etp_dev
DB_URL="jdbc:postgresql://localhost:5432/etp_dev" clojure -M:test -m solita.etp.db.flywaydb $1
