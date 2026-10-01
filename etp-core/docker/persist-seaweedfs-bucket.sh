#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

use_podman=false
if [ "${1:-}" = "--podman" ]; then
  use_podman=true
fi

docker_or_podman() {
  if [ "$use_podman" = true ]; then
    podman "$@"
  else
    docker "$@"
  fi
}

bucket_name="${S3_BUCKET:-files}"
aws_cli_image="${AWS_CLI_IMAGE:-amazon/aws-cli:2.37.6}"
output_dir="${OUTPUT_DIR:-seaweedfs/files}"
output_parent=$(dirname "$output_dir")
output_basename=$(basename "$output_dir")
tmp_dir="$output_parent/.${output_basename}.tmp.$$"

cleanup() {
  rm -rf "$tmp_dir"
}
trap cleanup EXIT

mkdir -p "$output_parent"
rm -rf "$tmp_dir"
mkdir -p "$tmp_dir"

seaweedfs_container_id=$(docker_or_podman compose ps -q seaweedfs)
if [ -z "$seaweedfs_container_id" ]; then
  echo "Could not find a running seaweedfs container." >&2
  exit 1
fi

tmp_dir_abs=$(cd "$tmp_dir" && pwd)

container_user_args=()
if [ "$use_podman" = false ]; then
  container_user_args=(--user "$(id -u):$(id -g)")
fi

echo "Persisting s3://$bucket_name to $output_dir using $aws_cli_image..."
docker_or_podman run \
  --rm \
  --network "container:$seaweedfs_container_id" \
  "${container_user_args[@]}" \
  -e AWS_ACCESS_KEY_ID=seaweedfs \
  -e AWS_SECRET_ACCESS_KEY=seaweedfs123 \
  -e AWS_DEFAULT_REGION=us-east-1 \
  -e HOME=/tmp \
  -v "$tmp_dir_abs:/seaweedfs/files" \
  "$aws_cli_image" \
  --endpoint-url http://127.0.0.1:9000 \
  s3 sync "s3://$bucket_name" /seaweedfs/files --delete

rm -rf "$output_dir"
mv "$tmp_dir" "$output_dir"
trap - EXIT

echo "Persisted s3://$bucket_name to $output_dir"

