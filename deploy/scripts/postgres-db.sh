#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"

usage() {
    cat <<'EOF'
Usage:
  ./deploy/scripts/postgres-db.sh <target> <action> <app>

Targets:
  local      local database target
  prod-core  production core database target

Actions:
  create       create the app database if it does not exist
  copy-legacy  copy legacy trade-leads data into the new app database
  verify       verify the app database exists and is connectable

Apps:
  trade-leads
  cxc-commerce

Examples:
  ./deploy/scripts/postgres-db.sh local create trade-leads
  ./deploy/scripts/postgres-db.sh local copy-legacy trade-leads
  ./deploy/scripts/postgres-db.sh prod-core verify cxc-commerce

Notes:
  - copy-legacy only supports trade-leads.
  - copy-legacy keeps forest_dev / forest_prod unchanged.
  - The target database must be empty before copy-legacy.
EOF
}

die() {
    echo "$*" >&2
    usage >&2
    exit 1
}

TARGET="${1:-}"
ACTION="${2:-}"
APP="${3:-}"

case "$TARGET" in
    local|prod-core) ;;
    *) die "Unsupported target: ${TARGET:-<empty>}" ;;
esac

case "$ACTION" in
    create|copy-legacy|verify) ;;
    *) die "Unsupported action: ${ACTION:-<empty>}" ;;
esac

case "$APP" in
    trade-leads|cxc-commerce) ;;
    *) die "Unsupported app: ${APP:-<empty>}" ;;
esac

ENV_FILE="${FOREST_POSTGRES_ENV_FILE:-${REPO_ROOT}/deploy/env/${TARGET}.env}"
if [[ "$TARGET" == "local" && ! -f "$ENV_FILE" ]]; then
    ENV_FILE="${REPO_ROOT}/deploy/env/local.env.example"
fi
[[ -f "$ENV_FILE" ]] || die "Missing env file: $ENV_FILE"

load_env() {
    set -a
    # shellcheck disable=SC1090
    source "$ENV_FILE"
    set +a
}

apply_defaults() {
    : "${POSTGRES_IMAGE:=crpi-4wiolv963xmb1wvm.cn-hangzhou.personal.cr.aliyuncs.com/hangzhoulingjian/postgres:18.3-alpine}"
    : "${POSTGRES_CLIENT_IMAGE:=$POSTGRES_IMAGE}"
    : "${POSTGRES_HOST:=127.0.0.1}"
    : "${POSTGRES_PORT:=5432}"
    : "${POSTGRES_USER:=forest}"
    : "${POSTGRES_PASSWORD:=lingzhi!@#2026}"
    : "${POSTGRES_DB:=postgres}"
    export POSTGRES_IMAGE POSTGRES_CLIENT_IMAGE POSTGRES_HOST POSTGRES_PORT POSTGRES_USER POSTGRES_PASSWORD POSTGRES_DB
}

resolve_database_names() {
    LEGACY_DATABASE=""
    case "$TARGET:$APP" in
        local:trade-leads)
            TARGET_DATABASE="trade_leads_local"
            LEGACY_DATABASE="forest_dev"
            ;;
        prod-core:trade-leads)
            TARGET_DATABASE="trade_leads_prod"
            LEGACY_DATABASE="forest_prod"
            ;;
        local:cxc-commerce)
            TARGET_DATABASE="cxc_commerce_local"
            ;;
        prod-core:cxc-commerce)
            TARGET_DATABASE="cxc_commerce_prod"
            ;;
        *)
            die "Unsupported database mapping: $TARGET $APP"
            ;;
    esac
}

validate_database_name() {
    local database="$1"
    if [[ ! "$database" =~ ^[a-z][a-z0-9_]*$ ]]; then
        die "Invalid database name: $database"
    fi
}

psql_cmd() {
    local database="$1"
    shift
    if command -v psql >/dev/null 2>&1; then
        PGPASSWORD="$POSTGRES_PASSWORD" psql \
            -h "$POSTGRES_HOST" \
            -p "$POSTGRES_PORT" \
            -U "$POSTGRES_USER" \
            -d "$database" \
            "$@"
    else
        docker run --rm -i \
            -e PGPASSWORD="$POSTGRES_PASSWORD" \
            "$POSTGRES_CLIENT_IMAGE" \
            psql \
            -h "$POSTGRES_HOST" \
            -p "$POSTGRES_PORT" \
            -U "$POSTGRES_USER" \
            -d "$database" \
            "$@"
    fi
}

pg_dump_cmd() {
    local database="$1"
    shift
    if command -v pg_dump >/dev/null 2>&1; then
        PGPASSWORD="$POSTGRES_PASSWORD" pg_dump \
            -h "$POSTGRES_HOST" \
            -p "$POSTGRES_PORT" \
            -U "$POSTGRES_USER" \
            -d "$database" \
            "$@"
    else
        docker run --rm -i \
            -e PGPASSWORD="$POSTGRES_PASSWORD" \
            "$POSTGRES_CLIENT_IMAGE" \
            pg_dump \
            -h "$POSTGRES_HOST" \
            -p "$POSTGRES_PORT" \
            -U "$POSTGRES_USER" \
            -d "$database" \
            "$@"
    fi
}

psql_scalar() {
    local database="$1"
    local sql="$2"
    psql_cmd "$database" -v ON_ERROR_STOP=1 -tA -c "$sql"
}

database_exists() {
    local database="$1"
    local exists
    if ! exists="$(psql_scalar "$POSTGRES_DB" "SELECT 1 FROM pg_database WHERE datname = '$database';")"; then
        echo "[postgres-db] failed to query database existence: $database" >&2
        exit 1
    fi
    [[ "$exists" == "1" ]]
}

base_table_count() {
    local database="$1"
    psql_scalar "$database" "SELECT count(*) FROM information_schema.tables WHERE table_schema NOT IN ('pg_catalog', 'information_schema') AND table_type = 'BASE TABLE';"
}

create_database() {
    if database_exists "$TARGET_DATABASE"; then
        echo "[postgres-db] database already exists: $TARGET_DATABASE"
        return
    fi

    echo "[postgres-db] creating database: $TARGET_DATABASE owner=$POSTGRES_USER"
    psql_cmd "$POSTGRES_DB" -v ON_ERROR_STOP=1 -c "CREATE DATABASE \"$TARGET_DATABASE\" OWNER \"$POSTGRES_USER\";"
}

assert_database_empty() {
    local database="$1"
    local count
    count="$(base_table_count "$database")"
    if [[ "$count" != "0" ]]; then
        echo "[postgres-db] target database is not empty: $database tables=$count" >&2
        exit 1
    fi
}

copy_legacy() {
    if [[ "$APP" != "trade-leads" ]]; then
        echo "[postgres-db] copy-legacy only supports trade-leads." >&2
        exit 1
    fi

    if ! database_exists "$LEGACY_DATABASE"; then
        echo "[postgres-db] legacy database does not exist: $LEGACY_DATABASE" >&2
        exit 1
    fi

    if ! database_exists "$TARGET_DATABASE"; then
        echo "[postgres-db] target database does not exist: $TARGET_DATABASE" >&2
        echo "[postgres-db] run create first." >&2
        exit 1
    fi

    assert_database_empty "$TARGET_DATABASE"

    echo "[postgres-db] copying legacy data: $LEGACY_DATABASE -> $TARGET_DATABASE"
    pg_dump_cmd "$LEGACY_DATABASE" --no-owner --no-privileges | psql_cmd "$TARGET_DATABASE" -v ON_ERROR_STOP=1

    local source_tables
    local target_tables
    source_tables="$(base_table_count "$LEGACY_DATABASE")"
    target_tables="$(base_table_count "$TARGET_DATABASE")"
    echo "[postgres-db] copy complete. source_tables=$source_tables target_tables=$target_tables"
}

verify_database() {
    if ! database_exists "$TARGET_DATABASE"; then
        echo "[postgres-db] database does not exist: $TARGET_DATABASE" >&2
        exit 1
    fi

    local table_count
    table_count="$(base_table_count "$TARGET_DATABASE")"
    echo "[postgres-db] database verified: $TARGET_DATABASE tables=$table_count"
}

load_env
apply_defaults
resolve_database_names
validate_database_name "$TARGET_DATABASE"
[[ -n "$LEGACY_DATABASE" ]] && validate_database_name "$LEGACY_DATABASE"

echo "[postgres-db] target=${TARGET} action=${ACTION} app=${APP} env=${ENV_FILE} host=${POSTGRES_HOST} port=${POSTGRES_PORT} admin_db=${POSTGRES_DB} target_db=${TARGET_DATABASE}"

case "$ACTION" in
    create)
        create_database
        ;;
    copy-legacy)
        copy_legacy
        ;;
    verify)
        verify_database
        ;;
esac
