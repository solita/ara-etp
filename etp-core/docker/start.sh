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

restore_seaweedfs_volume() {
  echo "Restoring SeaweedFS bucket data..."
  mkdir -p seaweedfs/files

  docker_or_podman compose wait minio_create_default_bucket || true

  minio_container_id=$(docker_or_podman compose ps -q minio)
  seaweedfs_files_dir=$(cd seaweedfs/files && pwd)

  docker_or_podman run \
    --rm \
    --network "container:$minio_container_id" \
    -e AWS_ACCESS_KEY_ID=minio \
    -e AWS_SECRET_ACCESS_KEY=minio123 \
    -e AWS_DEFAULT_REGION=us-east-1 \
    -e HOME=/tmp \
    -v "$seaweedfs_files_dir:/seaweedfs/files:ro" \
    amazon/aws-cli:2.37.6 \
    --endpoint-url http://127.0.0.1:9000 \
    s3 sync /seaweedfs/files s3://files --delete
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

is_first_seaweedfs_start=false

if ! docker_or_podman volume inspect etp_data > /dev/null 2>&1; then
  is_first_seaweedfs_start=true
fi

docker_or_podman compose up -d

if [ "$is_first_seaweedfs_start" = true ]; then
  restore_seaweedfs_volume
fi

echo "Waiting for etp-db-for-etp_dev to run database migrations..."
docker_or_podman compose wait etp-db-for-etp_dev || true

echo "Latest logs for related to database migrations:"
docker_or_podman compose logs --tail=10 etp-db-for-etp_dev
