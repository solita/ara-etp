#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"

docker_or_podman() {
  if [ "$use_podman" = true ]; then
    podman "$@"
  else
    docker "$@"
  fi
}

if [ "$1" == "--podman" ]; then
  use_podman=true
fi

mkdir -p smtp/received-emails
find sftp/ssh -iname "*_key" -exec chmod 600 {} \;

# If migrations have changed, rebuild etp-db
existing_checksum=$(cat migrations.checksum 2> /dev/null || echo "")
new_checksum=$(find ../etp-db/src/ -iname "*.sql" -type f -print0 | sort -z | xargs --null sha256sum | sha256sum | awk '{print $1}')

if [ "$existing_checksum" != "$new_checksum" ]; then
  echo "Database migrations have changed, stopping etp-db containers and deleting etp-db image resulting in a rebuild"
  docker_or_podman compose down etp-db-for-etp_dev etp-db-for-postgres || true
  docker_or_podman image rm docker.io/library/etp-db || true
  echo "Writing new database migrations checksum to migrations.checksum"
  echo "$new_checksum" > migrations.checksum
else
  echo "Database migrations have not changed, keeping possible existing etp-db containers and image"
fi

docker_or_podman compose up -d

docker_or_podman compose cp minio/files minio:/files

echo "Waiting for etp-db-for-etp_dev to run database migrations..."
docker_or_podman compose wait etp-db-for-etp_dev || true

echo "Latest logs for related to database migrations:"
docker_or_podman compose logs --tail=10 etp-db-for-etp_dev
