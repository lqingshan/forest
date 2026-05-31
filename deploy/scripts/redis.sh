#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"

usage() {
    cat <<'EOF'
Usage:
  ./deploy/scripts/redis.sh <target> <action>

Targets:
  local      local shared Redis
  prod-core  production core shared Redis

Actions:
  up | restart | down | logs | ps | ping | ping-local | ping-prod

Environment:
  The script loads deploy/env/<target>.env.
  Override with FOREST_REDIS_ENV_FILE=/path/to/env-file when needed.
EOF
}

die() {
    echo "$*" >&2
    usage >&2
    exit 1
}

TARGET="${1:-}"
ACTION="${2:-}"

case "$TARGET" in
    local|prod-core) ;;
    *) die "Unsupported target: ${TARGET:-<empty>}" ;;
esac

case "$ACTION" in
    up|restart|down|logs|ps|ping|ping-local|ping-prod) ;;
    *) die "Unsupported action: ${ACTION:-<empty>}" ;;
esac

ENV_FILE="${FOREST_REDIS_ENV_FILE:-${REPO_ROOT}/deploy/env/${TARGET}.env}"
if [[ "$TARGET" == "local" && ! -f "$ENV_FILE" ]]; then
    ENV_FILE="${REPO_ROOT}/deploy/env/local.env.example"
fi
[[ -f "$ENV_FILE" ]] || die "Missing env file: $ENV_FILE"

COMPOSE_FILE="${REPO_ROOT}/deploy/components/redis.yml"
[[ -f "$COMPOSE_FILE" ]] || die "Missing compose file: $COMPOSE_FILE"

load_env() {
    set -a
    # shellcheck disable=SC1090
    source "$ENV_FILE"
    set +a
}

apply_defaults() {
    : "${REDIS_IMAGE:=crpi-4wiolv963xmb1wvm.cn-hangzhou.personal.cr.aliyuncs.com/hangzhoulingjian/redis:8.2-alpine}"
    : "${REDIS_CONTAINER_NAME:=forest-redis}"
    : "${REDIS_VOLUME_NAME:=forest_redis_data}"
    : "${REDIS_HOST:=127.0.0.1}"
    : "${REDIS_PORT:=6379}"
    : "${REDIS_PASSWORD:=forest_redis_shared_2026}"
    : "${REDIS_MEM_LIMIT:=1g}"
    : "${REDIS_MEM_RESERVATION:=256m}"
    : "${REDIS_MAXMEMORY:=512mb}"
    : "${REDIS_MAXMEMORY_POLICY:=noeviction}"
    : "${REDIS_DATABASES:=16}"
    : "${REDIS_PROD_DATABASE:=0}"
    : "${REDIS_LOCAL_DATABASE:=1}"
    if [[ "$TARGET" == "prod-core" ]]; then
        : "${REDIS_DATABASE:=$REDIS_PROD_DATABASE}"
    else
        : "${REDIS_DATABASE:=$REDIS_LOCAL_DATABASE}"
    fi
    export REDIS_IMAGE REDIS_CONTAINER_NAME REDIS_VOLUME_NAME REDIS_HOST REDIS_PORT REDIS_PASSWORD REDIS_DATABASE
    export REDIS_MEM_LIMIT REDIS_MEM_RESERVATION REDIS_MAXMEMORY REDIS_MAXMEMORY_POLICY
    export REDIS_DATABASES REDIS_PROD_DATABASE REDIS_LOCAL_DATABASE
}

validate_env() {
    if [[ -z "${REDIS_PASSWORD// }" ]]; then
        die "REDIS_PASSWORD must not be empty."
    fi
    if [[ "$REDIS_LOCAL_DATABASE" == "$REDIS_PROD_DATABASE" ]]; then
        die "REDIS_LOCAL_DATABASE and REDIS_PROD_DATABASE must be different."
    fi
    if (( REDIS_LOCAL_DATABASE < 0 || REDIS_LOCAL_DATABASE >= REDIS_DATABASES )); then
        die "REDIS_LOCAL_DATABASE must be in [0, REDIS_DATABASES)."
    fi
    if (( REDIS_PROD_DATABASE < 0 || REDIS_PROD_DATABASE >= REDIS_DATABASES )); then
        die "REDIS_PROD_DATABASE must be in [0, REDIS_DATABASES)."
    fi
}

run_compose() {
    local subcommand="$1"
    shift
    (
        cd "$REPO_ROOT"
        docker compose -f "$COMPOSE_FILE" "$subcommand" "$@"
    )
}

redis_cli() {
    local db="$1"
    shift
    docker exec \
        -e REDISCLI_AUTH="$REDIS_PASSWORD" \
        "$REDIS_CONTAINER_NAME" \
        redis-cli -n "$db" "$@"
}

load_env
apply_defaults
validate_env

echo "[redis] target=${TARGET} action=${ACTION} env=${ENV_FILE} host=${REDIS_HOST} port=${REDIS_PORT} database=${REDIS_DATABASE} container=${REDIS_CONTAINER_NAME}"

case "$ACTION" in
    up)
        run_compose up -d redis
        ;;
    restart)
        run_compose stop redis || true
        run_compose rm -f redis || true
        run_compose up -d redis
        ;;
    logs)
        run_compose logs -f redis
        ;;
    ps)
        run_compose ps redis
        ;;
    ping)
        redis_cli 0 ping
        ;;
    ping-local)
        redis_cli "$REDIS_LOCAL_DATABASE" ping
        ;;
    ping-prod)
        redis_cli "$REDIS_PROD_DATABASE" ping
        ;;
    down)
        run_compose stop redis || true
        run_compose rm -f redis || true
        ;;
esac
