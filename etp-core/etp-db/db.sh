#!/usr/bin/env bash
set -e

package='target/etp-db.jar';
mode="$2"

if [ "$mode" == 'test' ]
then
  echo "Test migrations ARE included."
  cp="$package:target/test/sql"
elif [ -z "$mode" ]
then
  echo "Test migrations are NOT included."
  cp=$package
else
  echo "ERROR: Second argument must be 'test' or empty" >&2
  exit 1
fi

java -cp $cp clojure.main -m solita.etp.db.flywaydb "$1"
