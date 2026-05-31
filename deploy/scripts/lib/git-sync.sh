#!/usr/bin/env bash

# 生产部署前同步代码到指定版本。
# 调用方需要先加载 deploy/env/prod-*.env，并提供：
# - FOREST_CODE_DIR：生产服务器上的仓库目录，例如 /home/code/forest。
# - FOREST_GIT_REPO_URL：Git 仓库地址。
# - DEPLOY_GIT_REF：本次部署指定的分支、tag 或 commit。
sync_prod_repo() {
    local script_relative_path="$1"
    local resolved_ref=""

    [[ -n "${FOREST_CODE_DIR:-}" ]] || die "Missing FOREST_CODE_DIR in prod env."
    [[ -n "${FOREST_GIT_REPO_URL:-}" ]] || die "Missing FOREST_GIT_REPO_URL in prod env."
    [[ -n "${DEPLOY_GIT_REF:-}" ]] || die "Missing --ref <git-ref> for prod up."

    if [[ ! -d "$FOREST_CODE_DIR/.git" ]]; then
        if [[ -e "$FOREST_CODE_DIR" ]]; then
            die "FOREST_CODE_DIR exists but is not a Git repository: $FOREST_CODE_DIR"
        fi
        mkdir -p "$(dirname "$FOREST_CODE_DIR")"
        git clone "$FOREST_GIT_REPO_URL" "$FOREST_CODE_DIR"
    fi

    (
        cd "$FOREST_CODE_DIR"
        git fetch --all --tags --prune
        git reset --hard
        git clean -fd

        if git show-ref --verify --quiet "refs/remotes/origin/${DEPLOY_GIT_REF}"; then
            resolved_ref="origin/${DEPLOY_GIT_REF}"
        else
            resolved_ref="$DEPLOY_GIT_REF"
        fi

        git checkout --force --detach "$resolved_ref"
        git reset --hard HEAD
        git clean -fd
    )

    # 切到同步后的仓库脚本继续执行，确保后续 build 使用最新 compose、Dockerfile 和源码。
    exec "${FOREST_CODE_DIR}/${script_relative_path}" "${ORIGINAL_ARGS[@]}" --skip-git-sync
}
