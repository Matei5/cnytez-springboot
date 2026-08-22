#!/usr/bin/env bash

set -Eeuo pipefail
umask 077

HEALTH_ATTEMPTS=24
HEALTH_INTERVAL_SECONDS=5

wait_for_backend() {
  local allow_legacy_health=${1:-false}
  local attempt

  for ((attempt = 1; attempt <= HEALTH_ATTEMPTS; attempt++)); do
    if curl --fail --silent --show-error \
      http://localhost:8080/actuator/health/readiness >/dev/null 2>&1; then
      return 0
    fi

    if [[ "$allow_legacy_health" == "true" ]]; then
      if curl --fail --silent --show-error \
        http://localhost:8080/ >/dev/null 2>&1; then
        return 0
      fi
    fi

    sleep "$HEALTH_INTERVAL_SECONDS"
  done

  return 1
}

start_backend() {
  local app_dir=$1
  local backend_image=$2

  cd "$app_dir/backend"
  BACKEND_IMAGE="$backend_image" docker compose \
    -f docker-compose.yml \
    -f docker-compose.deploy.yml \
    config --quiet
  BACKEND_IMAGE="$backend_image" docker compose \
    -f docker-compose.yml \
    -f docker-compose.deploy.yml \
    up --detach --no-build --no-deps backend
}

rollback_to() {
  local app_dir=$1
  local target_image=$2
  local target_sha=$3

  echo "Rolling backend back to $target_image ($target_sha)"
  start_backend "$app_dir" "$target_image"

  if ! wait_for_backend true; then
    BACKEND_IMAGE="$target_image" docker compose \
      -f "$app_dir/backend/docker-compose.yml" \
      -f "$app_dir/backend/docker-compose.deploy.yml" \
      logs --tail=200 backend
    echo "Rollback target did not become healthy" >&2
    return 1
  fi

  cd "$app_dir"
  git checkout --detach "$target_sha"
  printf '%s\n' "$target_sha" > "$app_dir/.last-successful-backend-sha"
  printf '%s\n' "$target_image" > "$app_dir/.last-successful-backend-image"
  echo "Rollback completed successfully"
}

if [[ ${1:-} == "--rollback" ]]; then
  app_dir=${2:?"Application directory is required"}
  previous_image_file="$app_dir/.previous-backend-image"
  previous_sha_file="$app_dir/.previous-backend-sha"

  if [[ ! -f "$previous_image_file" || ! -f "$previous_sha_file" ]]; then
    echo "No previous backend release has been recorded" >&2
    exit 1
  fi

  rollback_to \
    "$app_dir" \
    "$(<"$previous_image_file")" \
    "$(<"$previous_sha_file")"
  exit 0
fi

app_dir=${1:?"Application directory is required"}
deploy_sha=${2:?"Deployment commit SHA is required"}
backend_image=${3:?"Backend image is required"}
backup_dir="$app_dir/.deployment-backups"
ecr_registry=386807259133.dkr.ecr.us-east-1.amazonaws.com
ecr_repository=cnytez-backend
expected_image="$ecr_registry/$ecr_repository:$deploy_sha"

if [[ ! "$deploy_sha" =~ ^[0-9a-f]{40}$ ]]; then
  echo "Deployment commit SHA is invalid" >&2
  exit 1
fi

if [[ "$backend_image" != "$expected_image" ]]; then
  echo "Backend image does not match the CI-approved commit" >&2
  exit 1
fi

cd "$app_dir"

if [[ -n $(git status --porcelain --untracked-files=no) ]]; then
  echo "Tracked files on the deployment host have local changes; refusing to overwrite them" >&2
  exit 1
fi

previous_sha=$(git rev-parse HEAD)
mkdir -p "$backup_dir"

if [[ -f "$app_dir/.last-successful-backend-image" ]]; then
  previous_image=$(<"$app_dir/.last-successful-backend-image")
else
  current_image_id=$(docker inspect reddit-backend --format '{{.Image}}')
  previous_image="cnytez-backend:rollback-${previous_sha:0:12}"
  docker image tag "$current_image_id" "$previous_image"
fi

if docker compose -f "$app_dir/backend/docker-compose.yml" ps --status running --quiet db | grep -q .; then
  backup_file="$backup_dir/postgres-$(date -u +%Y%m%dT%H%M%SZ)-before-${deploy_sha:0:12}.dump"
  echo "Creating pre-deployment database backup at $backup_file"
  docker compose -f "$app_dir/backend/docker-compose.yml" exec -T db \
    pg_dump --username postgres --dbname cnytez_db --format custom > "$backup_file"
fi

git fetch --quiet origin main

if ! git cat-file -e "${deploy_sha}^{commit}"; then
  echo "CI-approved commit $deploy_sha is not available on the deployment host" >&2
  exit 1
fi

printf '%s\n' "$previous_image" > "$app_dir/.previous-backend-image"
printf '%s\n' "$previous_sha" > "$app_dir/.previous-backend-sha"
git checkout --detach "$deploy_sha"

aws ecr get-login-password --region us-east-1 |
  docker login --username AWS --password-stdin "$ecr_registry"
docker pull "$backend_image"

if ! start_backend "$app_dir" "$backend_image" || ! wait_for_backend false; then
  echo "Deployment did not become healthy; collecting logs and rolling back" >&2
  BACKEND_IMAGE="$backend_image" docker compose \
    -f "$app_dir/backend/docker-compose.yml" \
    -f "$app_dir/backend/docker-compose.deploy.yml" \
    logs --tail=200 backend || true
  rollback_to "$app_dir" "$previous_image" "$previous_sha"
  exit 1
fi

printf '%s\n' "$deploy_sha" > "$app_dir/.last-successful-backend-sha"
printf '%s\n' "$backend_image" > "$app_dir/.last-successful-backend-image"
docker image prune --force
echo "Backend deployment $deploy_sha is healthy"
