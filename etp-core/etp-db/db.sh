#!/usr/bin/env bash
set -e

package='target/etp-db.jar';

if [ "$2" == 'test' ]
then
  cp="$package:target/test/sql"
elif [ "$2" == 'dev' ]
then
  cp="$package:target/test/dev-sql"
else
  cp=$package
fi

java -cp $cp clojure.main -m solita.etp.db.flywaydb "$1"
