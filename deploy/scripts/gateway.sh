#!/usr/bin/env bash
set -euo pipefail

# 只管理共享 Nginx gateway。
#
# gateway 当前支持两个运行目标：
# - local：本地开发入口，提供本地 HTTPS 访问。
# - prod-edge：生产前端入口机，对外暴露 80/443。
#
# 这个脚本会组合三类配置：
# - deploy/components/gateway.yml：定义 gateway 服务本体。
# - deploy/targets/gateway.<target>.yml：定义目标环境下的端口和 SSL 挂载。
# - deploy/env/<target>.env：提供 FOREST_ENV、证书目录、后端节点等变量。
# 当前脚本所在目录，例如 /path/to/forest/deploy/scripts。
# 这样无论从哪个目录执行脚本，都能稳定找到仓库根目录。
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# 仓库根目录。gateway.sh 位于 deploy/scripts，所以向上两级就是项目根。
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
GIT_SYNC_SCRIPT="${SCRIPT_DIR}/lib/git-sync.sh"
ORIGINAL_ARGS=("$@")

# 打印脚本使用说明。
# cat <<'EOF' 是 heredoc 写法，用来输出多行文本。
usage() {
    cat <<'EOF'
Usage:
  ./deploy/scripts/gateway.sh <target> <action> [--ref <git-ref>]

Targets:
  local      local gateway
  prod-edge  production edge gateway

Actions:
  up | restart | down | logs | ps
EOF
}

# 统一处理错误：先输出错误原因，再输出使用说明，最后以非 0 状态退出。
die() {
    echo "$*" >&2
    usage >&2
    exit 1
}

# 第一个参数是运行目标，例如 local 或 prod-edge。
# ${1:-} 表示如果第一个参数不存在，就使用空字符串，避免 set -u 直接报错。
TARGET="${1:-}"

# 第二个参数是动作，例如 up、down、logs、ps。
ACTION="${2:-}"
shift 2 || true

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

# target 表示机器角色，不是 app 名称。
case "$TARGET" in
    local|prod-edge) ;;
    *) die "Unsupported target: ${TARGET:-<empty>}" ;;
esac

# action 直接映射到 Docker Compose 对 gateway 服务的操作。
case "$ACTION" in
    up|restart|down|logs|ps) ;;
    *) die "Unsupported action: ${ACTION:-<empty>}" ;;
esac

# gateway 是共享入口，所以它读取 deploy/env 下的平台级 env。
# 对 prod-edge 来说，这里包含 SSL 证书目录和后端 upstream 节点。
SHARED_ENV_FILE="${REPO_ROOT}/deploy/env/${TARGET}.env"
[[ -f "$SHARED_ENV_FILE" ]] || die "Missing shared env file: $SHARED_ENV_FILE"

# component 文件定义稳定的服务本体，target 文件补充端口和卷挂载等环境差异。
COMPOSE_TARGET_FILE="${REPO_ROOT}/deploy/targets/gateway.${TARGET}.yml"
declare -a COMPOSE_FILES=(
    "${REPO_ROOT}/deploy/components/gateway.yml"
    "$COMPOSE_TARGET_FILE"
)

load_env() {
    set -a
    # 导出 env 变量，让 Docker Compose 可以用 ${VAR} 插值。
    # shellcheck disable=SC1090
    source "$SHARED_ENV_FILE"
    set +a
}

run_compose() {
    local subcommand="$1"
    shift
    local -a cmd=(docker compose)
    local compose_file
    # Compose 文件按顺序叠加：先服务本体，再叠加目标环境覆盖配置。
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

if [[ "$TARGET" == "local" && -n "$DEPLOY_GIT_REF" ]]; then
    die "local target does not accept --ref."
fi

if [[ "$ACTION" != "up" && "$ACTION" != "restart" && -n "$DEPLOY_GIT_REF" ]]; then
    die "--ref is only supported for prod-edge up/restart."
fi

if [[ "$TARGET" == "prod-edge" && ( "$ACTION" == "up" || "$ACTION" == "restart" ) && -z "$DEPLOY_GIT_REF" ]]; then
    die "prod-edge up/restart requires --ref <git-ref>."
fi

if [[ "$TARGET" == "prod-edge" && ( "$ACTION" == "up" || "$ACTION" == "restart" ) && "$SKIP_GIT_SYNC" == "false" ]]; then
    # shellcheck disable=SC1090
    source "$GIT_SYNC_SCRIPT"
    sync_prod_repo "deploy/scripts/gateway.sh"
fi

echo "[gateway] target=${TARGET} action=${ACTION}"

case "$ACTION" in
    up)
        run_compose up --build -d gateway
        ;;
    restart)
        run_compose stop gateway || true
        run_compose rm -f gateway || true
        run_compose up --build -d gateway
        ;;
    logs)
        run_compose logs -f gateway
        ;;
    ps)
        run_compose ps gateway
        ;;
    down)
        run_compose stop gateway || true
        run_compose rm -f gateway || true
        ;;
esac
