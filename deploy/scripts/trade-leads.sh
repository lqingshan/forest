#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
POSTGRES_SCRIPT="${SCRIPT_DIR}/postgres.sh"
GATEWAY_SCRIPT="${SCRIPT_DIR}/gateway.sh"
GIT_SYNC_SCRIPT="${SCRIPT_DIR}/lib/git-sync.sh"
ORIGINAL_ARGS=("$@")

usage() {
    cat <<'EOF'
Usage:
  ./deploy/scripts/trade-leads.sh <target> <action> [component] [--ref <git-ref>]

Targets:
  local      local backend target
  prod-core  production core backend target

Actions:
  up | restart | down | logs | ps

Components:
  local: backend | gateway | all (default: all)
  prod-core: backend | postgres | all (default: backend)

Notes:
  - down only stops the trade-leads backend target.
  - restart stops/removes the selected service container, then runs the matching up flow.
  - shared postgres/gateway ownership belongs to postgres.sh and gateway.sh.
EOF
}

die() {
    echo "$*" >&2
    usage >&2
    exit 1
}

TARGET="${1:-}"
ACTION="${2:-}"
COMPONENT="${3:-}"
if [[ "${COMPONENT:-}" == --* ]]; then
    COMPONENT=""
fi

if [[ -n "$COMPONENT" ]]; then
    shift 3 || true
else
    shift 2 || true
fi

DEPLOY_GIT_REF=""
SKIP_GIT_SYNC=false
while [[ $# -gt 0 ]]; do
    case "$1" in
        --ref)
            shift
            [[ $# -gt 0 ]] || die "Missing value after --ref."
            DEPLOY_GIT_REF="$1"
            shift
            ;;
        --skip-git-sync)
            SKIP_GIT_SYNC=true
            shift
            ;;
        *)
            die "Unsupported argument: $1"
            ;;
    esac
done

case "$TARGET" in
    local|prod-core) ;;
    *) die "Unsupported target: ${TARGET:-<empty>}" ;;
esac

case "$ACTION" in
    up|restart|down|logs|ps) ;;
    *) die "Unsupported action: ${ACTION:-<empty>}" ;;
esac

SHARED_ENV_FILE="${REPO_ROOT}/deploy/env/${TARGET}.env"
case "$TARGET" in
    local) APP_ENV_FILE="${REPO_ROOT}/apps/trade-leads/env/local.env" ;;
    prod-core) APP_ENV_FILE="${REPO_ROOT}/apps/trade-leads/env/prod.core.env" ;;
esac

[[ -f "$SHARED_ENV_FILE" ]] || die "Missing shared env file: $SHARED_ENV_FILE"
[[ -f "$APP_ENV_FILE" ]] || die "Missing app env file: $APP_ENV_FILE"

declare -a BACKEND_COMPOSE_FILES=()
declare -a COMBINED_COMPOSE_FILES=()
declare -a COMBINED_SERVICES=()

resolve_component() {
    if [[ -z "$COMPONENT" ]]; then
        if [[ "$TARGET" == "local" ]]; then
            COMPONENT="all"
        else
            COMPONENT="backend"
        fi
    fi

    case "$TARGET:$COMPONENT" in
        local:backend|local:gateway|local:all) ;;
        prod-core:backend|prod-core:postgres|prod-core:all) ;;
        *)
            die "Unsupported component for ${TARGET}: ${COMPONENT}"
            ;;
    esac

    BACKEND_COMPOSE_FILES=(
        "${REPO_ROOT}/deploy/components/trade-leads.backend.yml"
        "${REPO_ROOT}/deploy/targets/trade-leads.${TARGET}.yml"
    )

    case "$TARGET" in
        local)
            COMBINED_COMPOSE_FILES=(
                "${REPO_ROOT}/deploy/components/gateway.yml"
                "${REPO_ROOT}/deploy/components/trade-leads.backend.yml"
                "${REPO_ROOT}/deploy/targets/gateway.local.yml"
                "${REPO_ROOT}/deploy/targets/trade-leads.local.yml"
            )
            COMBINED_SERVICES=(gateway trade-leads-backend)
            ;;
        prod-core)
            COMBINED_COMPOSE_FILES=(
                "${REPO_ROOT}/deploy/components/postgres.yml"
                "${REPO_ROOT}/deploy/components/trade-leads.backend.yml"
                "${REPO_ROOT}/deploy/targets/postgres.prod-core.yml"
                "${REPO_ROOT}/deploy/targets/trade-leads.prod-core.yml"
            )
            COMBINED_SERVICES=(postgres trade-leads-backend)
            ;;
    esac
}

load_envs() {
    set -a
    # shellcheck disable=SC1090
    source "$SHARED_ENV_FILE"
    # shellcheck disable=SC1090
    source "$APP_ENV_FILE"
    set +a
}

run_compose() {
    local compose_ref="$1"
    shift
    local subcommand="$1"
    shift
    local -a cmd=(docker compose)
    local -a compose_files=()
    local compose_file
    case "$compose_ref" in
        BACKEND_COMPOSE_FILES)
            compose_files=("${BACKEND_COMPOSE_FILES[@]}")
            ;;
        COMBINED_COMPOSE_FILES)
            compose_files=("${COMBINED_COMPOSE_FILES[@]}")
            ;;
        *)
            die "Unsupported compose file group: ${compose_ref}"
            ;;
    esac
    for compose_file in "${compose_files[@]}"; do
        cmd+=(-f "$compose_file")
    done
    cmd+=("$subcommand" "$@")
    (
        cd "$REPO_ROOT"
        "${cmd[@]}"
    )
}

wait_for_postgres() {
    local attempts=60
    local status
    for ((i = 1; i <= attempts; i++)); do
        status="$(docker inspect -f '{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' forest-postgres 2>/dev/null || true)"
        case "$status" in
            healthy)
                return 0
                ;;
            unhealthy|exited|dead)
                die "forest-postgres entered an unhealthy state: ${status}"
                ;;
        esac
        sleep 1
    done
    die "Timed out waiting for forest-postgres to become healthy."
}

delegate_postgres() {
    "$POSTGRES_SCRIPT" "$@"
}

delegate_gateway() {
    "$GATEWAY_SCRIPT" "$@"
}

restart_backend() {
    run_compose BACKEND_COMPOSE_FILES stop trade-leads-backend || true
    run_compose BACKEND_COMPOSE_FILES rm -f trade-leads-backend || true
    run_compose BACKEND_COMPOSE_FILES up --build -d trade-leads-backend
}

resolve_component
load_envs

if [[ "$TARGET" == "local" && -n "$DEPLOY_GIT_REF" ]]; then
    die "local target does not accept --ref."
fi

if [[ "$ACTION" != "up" && "$ACTION" != "restart" && -n "$DEPLOY_GIT_REF" ]]; then
    die "--ref is only supported for prod-core up/restart backend/all."
fi

if [[ "$TARGET" == "prod-core" && ( "$ACTION" == "up" || "$ACTION" == "restart" ) && "$COMPONENT" == "postgres" && -n "$DEPLOY_GIT_REF" ]]; then
    die "prod-core postgres component does not accept --ref."
fi

if [[ "$TARGET" == "prod-core" && ( "$ACTION" == "up" || "$ACTION" == "restart" ) && "$COMPONENT" != "postgres" && -z "$DEPLOY_GIT_REF" ]]; then
    die "prod-core up/restart requires --ref <git-ref>."
fi

if [[ "$TARGET" == "prod-core" && ( "$ACTION" == "up" || "$ACTION" == "restart" ) && "$COMPONENT" != "postgres" && "$SKIP_GIT_SYNC" == "false" ]]; then
    # shellcheck disable=SC1090
    source "$GIT_SYNC_SCRIPT"
    sync_prod_repo "deploy/scripts/trade-leads.sh"
fi

echo "[trade-leads] target=${TARGET} action=${ACTION} component=${COMPONENT}"

case "$ACTION" in
    up)
        case "$COMPONENT" in
            backend)
                run_compose BACKEND_COMPOSE_FILES up --build -d trade-leads-backend
                ;;
            postgres)
                delegate_postgres "$TARGET" up
                ;;
            gateway)
                [[ "$TARGET" == "local" ]] || die "gateway is only available on the local target."
                delegate_gateway local up
                ;;
            all)
                if [[ "$TARGET" == "local" ]]; then
                    run_compose BACKEND_COMPOSE_FILES up --build -d trade-leads-backend
                    delegate_gateway local up
                else
                    delegate_postgres prod-core up
                    wait_for_postgres
                    run_compose BACKEND_COMPOSE_FILES up --build -d trade-leads-backend
                fi
                ;;
        esac
        ;;
    restart)
        case "$COMPONENT" in
            backend)
                restart_backend
                ;;
            postgres)
                delegate_postgres "$TARGET" restart
                ;;
            gateway)
                [[ "$TARGET" == "local" ]] || die "gateway is only available on the local target."
                delegate_gateway local restart
                ;;
            all)
                if [[ "$TARGET" == "local" ]]; then
                    restart_backend
                    delegate_gateway local restart
                else
                    run_compose BACKEND_COMPOSE_FILES stop trade-leads-backend || true
                    run_compose BACKEND_COMPOSE_FILES rm -f trade-leads-backend || true
                    delegate_postgres prod-core restart
                    wait_for_postgres
                    run_compose BACKEND_COMPOSE_FILES up --build -d trade-leads-backend
                fi
                ;;
        esac
        ;;
    logs)
        case "$COMPONENT" in
            backend)
                run_compose BACKEND_COMPOSE_FILES logs -f trade-leads-backend
                ;;
            postgres)
                delegate_postgres "$TARGET" logs
                ;;
            gateway)
                [[ "$TARGET" == "local" ]] || die "gateway is only available on the local target."
                delegate_gateway local logs
                ;;
            all)
                run_compose COMBINED_COMPOSE_FILES logs -f "${COMBINED_SERVICES[@]}"
                ;;
        esac
        ;;
    ps)
        case "$COMPONENT" in
            backend)
                run_compose BACKEND_COMPOSE_FILES ps trade-leads-backend
                ;;
            postgres)
                delegate_postgres "$TARGET" ps
                ;;
            gateway)
                [[ "$TARGET" == "local" ]] || die "gateway is only available on the local target."
                delegate_gateway local ps
                ;;
            all)
                run_compose COMBINED_COMPOSE_FILES ps "${COMBINED_SERVICES[@]}"
                ;;
        esac
        ;;
    down)
        if [[ "$COMPONENT" != "backend" && "$COMPONENT" != "all" ]]; then
            die "trade-leads down only supports the backend target. Use postgres.sh or gateway.sh for shared components."
        fi
        run_compose BACKEND_COMPOSE_FILES stop trade-leads-backend || true
        run_compose BACKEND_COMPOSE_FILES rm -f trade-leads-backend || true
        ;;
esac
