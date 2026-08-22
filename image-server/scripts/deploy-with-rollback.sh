#!/usr/bin/env bash

set -Eeuo pipefail
umask 077

HEALTH_ATTEMPTS=24
HEALTH_INTERVAL_SECONDS=5

wait_for_image_server() {
  local allow_legacy_health=${1:-false}
  local attempt

  for attempt in $(seq 1 "$HEALTH_ATTEMPTS"); do
    if curl --fail --silent --show-error \
      http://localhost:8123/health/ready >/dev/null 2>&1; then
      return 0
    fi

    if [[ "$allow_legacy_health" == "true" ]]; then
      if curl --fail --silent --show-error \
        http://localhost:8123/ >/dev/null 2>&1; then
        return 0
      fi
    fi

    sleep "$HEALTH_INTERVAL_SECONDS"
  done

  return 1
}

start_image_server() {
  local app_dir=$1

  cd "$app_dir/image-server"
  docker compose -f docker-compose.yml up --build --detach
}

rollback_to() {
  local app_dir=$1
  local target_sha=$2

  echo "Rolling image server back to $target_sha"
  cd "$app_dir"
  git checkout --detach "$target_sha"
  start_image_server "$app_dir"

  if ! wait_for_image_server true; then
    docker compose -f "$app_dir/image-server/docker-compose.yml" \
      logs --tail=200 image-server
    echo "Image-server rollback target did not become healthy" >&2
    return 1
  fi

  printf '%s\n' "$target_sha" > "$app_dir/.last-successful-image-server-sha"
  echo "Image-server rollback completed successfully"
}

app_dir=${1:?"Application directory is required"}
deploy_sha=${2:?"Deployment commit SHA is required"}

cd "$app_dir"

if [[ -n $(git status --porcelain --untracked-files=no) ]]; then
  echo "Tracked files on the image-server host have local changes; refusing to overwrite them" >&2
  exit 1
fi

previous_sha=$(git rev-parse HEAD)
git fetch --quiet origin main

if ! git cat-file -e "${deploy_sha}^{commit}"; then
  echo "CI-approved commit $deploy_sha is not available on the image-server host" >&2
  exit 1
fi

printf '%s\n' "$previous_sha" > "$app_dir/.previous-image-server-sha"
git checkout --detach "$deploy_sha"

if ! start_image_server "$app_dir" || ! wait_for_image_server false; then
  echo "Image-server deployment did not become healthy; collecting logs and rolling back" >&2
  docker compose -f "$app_dir/image-server/docker-compose.yml" \
    logs --tail=200 image-server || true
  rollback_to "$app_dir" "$previous_sha"
  exit 1
fi

printf '%s\n' "$deploy_sha" > "$app_dir/.last-successful-image-server-sha"
echo "Image-server deployment $deploy_sha is healthy"
