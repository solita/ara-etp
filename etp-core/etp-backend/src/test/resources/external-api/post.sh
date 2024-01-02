#!/usr/bin/env bash
set -e

local='http://localhost:8080'
dev='https://private.kehitys.energiatodistusrekisteri.fi'
test='https://private.testi.energiatodistusrekisteri.fi'

server=${!1}
file=${2-'complete-2018.json'}
version=${3-'2018'}
user=${4-'laatija@solita.fi:asdfasdf'}

curl -v -d "@$file" \
  -H "Content-Type: application/json" \
  -H 'User-Agent:' \
  -u $user \
  "$server/api/external/energiatodistukset/$version"
