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


docker_or_podman compose up -d

docker_or_podman compose cp minio/files minio:/files

echo "Waiting for etp-db-for-etp_dev to run database migrations..."
docker_or_podman compose wait etp-db-for-etp_dev
