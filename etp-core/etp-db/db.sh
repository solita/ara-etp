#!/usr/bin/env bash
set -e

package='target/etp-db';

if [[ ! -d $package ]]; then
  echo "Database migration tool directory $package does not exists.";
  exit 1;
fi

if [ "$2" == 'test' ]
then
  cp="$package:target/test/sql"
else
  cp=$package
fi

java -cp $cp clojure.main -m solita.etp.db.flywaydb $1
