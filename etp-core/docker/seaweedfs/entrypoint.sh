#!/bin/sh
set -eu

weed server \
  -dir=/files \
  -master.port=9333 \
  -volume.port=8080 \
  -volume.max=1000 \
  -filer.port=8888 \
  -s3 \
  -s3.port=9000 \
  -s3.config=/etc/seaweedfs/s3.json &

weed_pid=$!
echo "$weed_pid" > /tmp/weed.pid

cleanup() {
  rm -f /tmp/seaweedfs-ready /tmp/weed.pid
  if kill -0 "$weed_pid" 2>/dev/null; then
    kill "$weed_pid"
    wait "$weed_pid" 2>/dev/null || true
  fi
}

trap cleanup EXIT INT TERM

attempt=0
while :; do
  output=$(echo "s3.bucket.create -name files" | weed shell -master=127.0.0.1:9333 -filer=127.0.0.1:8888 2>&1) && status=0 || status=$?

  if [ "$status" -eq 0 ] || printf '%s' "$output" | grep -qi "already exists"; then
    break
  fi

  attempt=$((attempt + 1))
  if [ "$attempt" -ge 30 ]; then
    echo "Failed to create SeaweedFS default bucket after waiting for startup" >&2
    echo "$output" >&2
    exit 1
  fi

  if ! kill -0 "$weed_pid" 2>/dev/null; then
    wait "$weed_pid"
  fi

  sleep 1
done

touch /tmp/seaweedfs-ready

wait "$weed_pid"


