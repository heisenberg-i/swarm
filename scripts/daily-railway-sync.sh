#!/usr/bin/env bash
# Daily Pi -> Railway sync: wake MySQL, restore dump, then stop MySQL + backend.
# Run on Raspberry Pi via cron. Requires Railway API token and jq.
#
# Usage:
#   ./scripts/daily-railway-sync.sh
#   ./scripts/daily-railway-sync.sh --dry-run
#   ./scripts/daily-railway-sync.sh --dump-only
#   ./scripts/daily-railway-sync.sh --no-stop

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
ENV_FILE="${ENV_FILE:-${REPO_ROOT}/.env.rpi}"

# shellcheck source=lib/mysql-sync.sh
source "${SCRIPT_DIR}/lib/mysql-sync.sh"
# shellcheck source=railway-api.sh
source "${SCRIPT_DIR}/railway-api.sh"

DRY_RUN=false
DUMP_ONLY=false
NO_STOP=false

usage() {
  cat <<'EOF'
daily-railway-sync.sh — daily Railway standby sync + sleep

Options:
  --dry-run     Print actions without executing
  --dump-only   Local gzip dump only (no Railway contact)
  --no-stop     Sync but leave Railway services running (debug)
  -h, --help    Show this help

Required env (.env.rpi):
  DB_* / LOCAL_DB_* — Pi MySQL
  RAILWAY_MYSQL_* — Railway MySQL TCP proxy
  RAILWAY_TOKEN, RAILWAY_PROJECT_ID, RAILWAY_ENVIRONMENT_ID
  RAILWAY_MYSQL_SERVICE_ID, RAILWAY_BACKEND_SERVICE_ID

Cron (daily 02:00):
  0 2 * * * cd /home/swarm/swarm_webapp && ./scripts/daily-railway-sync.sh >> /var/log/swarm-sync.log 2>&1
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --dry-run) DRY_RUN=true ;;
    --dump-only) DUMP_ONLY=true ;;
    --no-stop) NO_STOP=true ;;
    -h|--help) usage; exit 0 ;;
    *) echo "Unknown option: $1" >&2; usage; exit 1 ;;
  esac
  shift
done

if [[ -f "${ENV_FILE}" ]]; then
  # shellcheck disable=SC1090
  set -a
  source "${ENV_FILE}"
  set +a
  mysql_sync_log "Loaded ${ENV_FILE}"
fi

mysql_sync_init_paths "${REPO_ROOT}"
mysql_sync_require_local_vars
mysql_sync_dump_local "${DRY_RUN}"
mysql_sync_prune_backups 14 "${DRY_RUN}"

if [[ "${DUMP_ONLY}" == "true" ]]; then
  mysql_sync_log "Dump-only mode; skipping Railway wake/sync/sleep"
  exit 0
fi

mysql_sync_require_railway_vars
railway_api_require_token
railway_api_require_ids

mysql_sync_log "=== Daily Railway sync starting ==="

railway_api_wake_service "${RAILWAY_MYSQL_SERVICE_ID}" "${RAILWAY_ENVIRONMENT_ID}" "${DRY_RUN}"

if [[ "${DRY_RUN}" != "true" ]]; then
  mysql_sync_wait_tcp 60 5
fi

mysql_sync_restore_railway "${DRY_RUN}"
mysql_sync_log "Daily Railway restore complete"

if [[ "${NO_STOP}" == "true" ]]; then
  mysql_sync_log "--no-stop: leaving Railway MySQL and backend running"
  mysql_sync_log "=== Daily Railway sync finished (no stop) ==="
  exit 0
fi

railway_api_sleep_service "${RAILWAY_MYSQL_SERVICE_ID}" "${RAILWAY_ENVIRONMENT_ID}" "${DRY_RUN}"
railway_api_sleep_service "${RAILWAY_BACKEND_SERVICE_ID}" "${RAILWAY_ENVIRONMENT_ID}" "${DRY_RUN}"

mysql_sync_log "=== Daily Railway sync finished; MySQL + backend stopped ==="
