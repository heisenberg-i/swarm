#!/usr/bin/env bash
# Push primary MySQL snapshot to Railway standby (on-demand sync).
# For daily sync + sleep cycle, use scripts/daily-railway-sync.sh instead.
#
# Usage:
#   ./scripts/sync-to-railway.sh              # dump + restore
#   ./scripts/sync-to-railway.sh --dry-run    # show commands only
#   ./scripts/sync-to-railway.sh --dump-only  # backup locally, skip Railway restore

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
ENV_FILE="${ENV_FILE:-${REPO_ROOT}/.env.rpi}"

# shellcheck source=lib/mysql-sync.sh
source "${SCRIPT_DIR}/lib/mysql-sync.sh"

DRY_RUN=false
DUMP_ONLY=false

usage() {
  cat <<'EOF'
sync-to-railway.sh — push primary MySQL snapshot to Railway standby

Options:
  --dry-run     Print actions without executing
  --dump-only   Create local gzip dump only (no Railway restore)
  -h, --help    Show this help

Required env (set in .env.rpi or environment):
  LOCAL_DB_HOST, LOCAL_DB_PORT, DB_NAME, DB_USER, DB_PASSWORD
  RAILWAY_MYSQL_HOST, RAILWAY_MYSQL_PORT, RAILWAY_MYSQL_USER,
  RAILWAY_MYSQL_PASSWORD, RAILWAY_MYSQL_DATABASE
  SYNC_BACKUP_DIR (optional, default: ./data/backups)

Cron example (local backup only, weekly):
  0 3 * * 0 cd /home/swarm/swarm_webapp && ./scripts/sync-to-railway.sh --dump-only >> /var/log/swarm-sync.log 2>&1
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --dry-run) DRY_RUN=true ;;
    --dump-only) DUMP_ONLY=true ;;
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
if [[ "${DUMP_ONLY}" != "true" ]]; then
  mysql_sync_require_railway_vars
fi

mysql_sync_dump_local "${DRY_RUN}"

if [[ "${DUMP_ONLY}" == "true" ]]; then
  mysql_sync_log "Dump-only mode; skipping Railway restore"
  mysql_sync_prune_backups 14 "${DRY_RUN}"
  exit 0
fi

mysql_sync_restore_railway "${DRY_RUN}"
mysql_sync_prune_backups 14 "${DRY_RUN}"
mysql_sync_log "Sync finished"
