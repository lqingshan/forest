#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"

usage() {
    cat <<'EOF'
Usage:
  ./deploy/scripts/postgres.sh <target> <action>

Targets:
  prod-core  production core PostgreSQL

Actions:
  up | restart | down | logs | ps
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
    prod-core) ;;
    *) die "Unsupported target: ${TARGET:-<empty>}" ;;
esac

case "$ACTION" in
    up|restart|down|logs|ps) ;;
    *) die "Unsupported action: ${ACTION:-<empty>}" ;;
esac

SHARED_ENV_FILE="${REPO_ROOT}/deploy/env/${TARGET}.env"
[[ -f "$SHARED_ENV_FILE" ]] || die "Missing shared env file: $SHARED_ENV_FILE"

COMPOSE_TARGET_FILE="${REPO_ROOT}/deploy/targets/postgres.${TARGET}.yml"
declare -a COMPOSE_FILES=("${REPO_ROOT}/deploy/components/postgres.yml")
[[ -f "$COMPOSE_TARGET_FILE" ]] && COMPOSE_FILES+=("$COMPOSE_TARGET_FILE")

load_env() {
    set -a
    # shellcheck disable=SC1090
    source "$SHARED_ENV_FILE"
    set +a
}

run_compose() {
    local subcommand="$1"
    shift
    local -a cmd=(docker compose)
    local compose_file
    for compose_file in "${COMPOSE_FILES[@]}"; do
        cmd+=(-f "$compose_file")
    done
    cmd+=("$subcommand" "$@")
    (
        cd "$REPO_ROOT"
        "${cmd[@]}"
    )
}

load_env

echo "[postgres] target=${TARGET} action=${ACTION}"

case "$ACTION" in
    up)
        run_compose up -d postgres
        ;;
    restart)
        run_compose stop postgres || true
        run_compose rm -f postgres || true
        run_compose up -d postgres
        ;;
    logs)
        run_compose logs -f postgres
        ;;
    ps)
        run_compose ps postgres
        ;;
    down)
        run_compose stop postgres || true
        run_compose rm -f postgres || true
        ;;
esac
