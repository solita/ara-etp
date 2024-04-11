#!/usr/bin/env bash
set -euxo pipefail

if [ "$1" == true ]
then
  test_command="npm run test-storybook:update"
else
  test_command="npm run test-storybook"
fi

npx concurrently -k -s first -n "SB,TEST" -c "magenta,blue" \
            "npx http-server storybook-static --port 6006 --silent" \
            "npx wait-on http://127.0.0.1:6006 && $test_command"
