#!/usr/bin/env bash
set -euo pipefail

# Creates a migration of all user data, i.e. data that's not from other migrations such as postinumeros
# Usage: run the script.
# Remember to commit the resulting migration file if the changes are useful later as well.

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
cd "$SCRIPT_DIR" || exit 1

rm -rf minio/files
docker compose cp -a minio:/files/files minio/files/

# Need to insert in replica mode so that audit data is not generated
echo "set session_replication_role = 'replica';" > ../etp-db/src/test/dev-sql/migration/repeatable/r-x-01-test-data.sql

# Save interesting audit data
docker compose exec db pg_dump -U etp etp_dev \
  --data-only \
  --rows-per-insert=1000 \
  --on-conflict-do-nothing \
  `# Henkilo and yritys data is referenced from non-audit tables` \
  -t audit.vk_henkilo \
  -t audit.vk_yritys \
  -t audit.vo_henkilo \
  -t audit.vo_yritys \
  `# Käyttäjä and laatija data is shown on muutoshistoria page` \
  -t audit.kayttaja \
  -t audit.laatija \
  >> ../etp-db/src/test/dev-sql/migration/repeatable/r-x-01-test-data.sql

# Exclude unwanted tables instead of including wanted so that when changes are made,
# either the new data is included or this will fail
docker compose exec db pg_dump -U etp etp_dev \
  --data-only \
  --rows-per-insert=1000 \
  --on-conflict-do-nothing \
  `# Exclude tables that have enumerations, annotations or templates` \
  `# Exclude audit tables other than those that have interesting data` \
  `# Kayttaja and laatija history data is in audit tables` \
  `# Valvonta osapuoli data linked to audit table` \
  -T audit.* \
  -T aineisto \
  -T alakayttotarkoitusluokka \
  -T country \
  -T audit.country \
  -T energiatodistustila \
  -T flyway_schema_history \
  -T hallinto_oikeus \
  -T ilmanvaihtotyyppi \
  -T karajaoikeus \
  -T kayttotarkoitusluokka \
  -T kielisyys \
  -T kunta \
  -T laatija_yritys_tila \
  -T laatimisvaihe \
  -T lammitysmuoto \
  -T lammonjako \
  -T laskutuskieli \
  -T patevyystaso \
  -T postinumero \
  -T postinumerotype \
  -T rooli \
  -T stat_kayttotarkoitusluokka \
  -T stat_ktluokka_alaktluokka \
  -T toimintaalue \
  -T validation_numeric_column \
  -T validation_required_column \
  -T validation_sisainen_kuorma \
  -T vastaanottajaryhma \
  -T verkkolaskuoperaattori \
  -T vk_ilmoituspaikka \
  -T vk_ilmoituspaikka \
  -T vk_rooli \
  -T vk_template \
  -T vk_toimenpidetype \
  -T vk_toimitustapa \
  -T vo_severity \
  -T vo_template \
  -T vo_toimenpidetype \
  -T yritystype \
  | grep -Ev \
  --regex '^\s+\(-\d' `# kayttaja entries with negative id and are added by migration` \
  --regex '^\s+\(0, '\''database' `# Added by migration` \
  >> ../etp-db/src/test/dev-sql/migration/repeatable/r-x-01-test-data.sql
