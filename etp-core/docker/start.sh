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

wait_for_service_health() {
  service_name="$1"
  timeout_seconds="${2:-60}"
  start_time=$(date +%s)

  while true; do
    container_id=$(docker_or_podman compose ps -q "$service_name")

    if [ -n "$container_id" ]; then
      health_status=$(docker_or_podman inspect -f '{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' "$container_id")

      if [ "$health_status" = "healthy" ]; then
        return 0
      fi

      if [ "$health_status" = "exited" ] || [ "$health_status" = "dead" ]; then
        echo "$service_name stopped before becoming healthy" >&2
        docker_or_podman compose logs --tail=50 "$service_name" >&2 || true
        return 1
      fi
    fi

    if [ $(( $(date +%s) - start_time )) -ge "$timeout_seconds" ]; then
      echo "Timed out waiting for $service_name to become healthy" >&2
      docker_or_podman compose logs --tail=50 "$service_name" >&2 || true
      return 1
    fi

    sleep 1
  done
}

restore_seaweedfs_volume() {
  echo "Restoring SeaweedFS bucket data..."
  mkdir -p seaweedfs/files

  wait_for_service_health seaweedfs

  seaweedfs_container_id=$(docker_or_podman compose ps -q seaweedfs)
  seaweedfs_files_dir=$(cd seaweedfs/files && pwd)

  docker_or_podman run \
    --rm \
    --network "container:$seaweedfs_container_id" \
    -e AWS_ACCESS_KEY_ID=seaweedfs \
    -e AWS_SECRET_ACCESS_KEY=seaweedfs123 \
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
