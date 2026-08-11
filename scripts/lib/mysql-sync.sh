#!/usr/bin/env bash
# Shared mysqldump / restore helpers for Pi -> Railway sync scripts.

mysql_sync_log() {
  echo "[$(date -Iseconds)] $*"
}

mysql_sync_init_paths() {
  local repo_root="$1"
  LOCAL_DB_HOST="${LOCAL_DB_HOST:-127.0.0.1}"
  LOCAL_DB_PORT="${LOCAL_DB_PORT:-3306}"
  DB_NAME="${DB_NAME:-biopower_db}"
  DB_USER="${DB_USER:-biopower}"
  SYNC_BACKUP_DIR="${SYNC_BACKUP_DIR:-${repo_root}/data/backups}"
  TIMESTAMP="$(date +%Y%m%d%H%M)"
  DUMP_FILE="${SYNC_BACKUP_DIR}/swarm-${TIMESTAMP}.sql.gz"
}

mysql_sync_require_local_vars() {
  if [[ -z "${DB_PASSWORD:-}" ]]; then
    echo "Missing required variable: DB_PASSWORD" >&2
    exit 1
  fi
}

mysql_sync_require_railway_vars() {
  local missing=()
  for var in RAILWAY_MYSQL_HOST RAILWAY_MYSQL_PASSWORD RAILWAY_MYSQL_DATABASE; do
    if [[ -z "${!var:-}" ]]; then
      missing+=("$var")
    fi
  done
  if [[ ${#missing[@]} -gt 0 ]]; then
    echo "Missing required Railway variables: ${missing[*]}" >&2
    exit 1
  fi
  RAILWAY_MYSQL_PORT="${RAILWAY_MYSQL_PORT:-3306}"
  RAILWAY_MYSQL_USER="${RAILWAY_MYSQL_USER:-root}"
}

mysql_sync_dump_local() {
  local dry_run="${1:-false}"
  mkdir -p "${SYNC_BACKUP_DIR}"
  mysql_sync_log "Dumping ${DB_NAME} from ${LOCAL_DB_HOST}:${LOCAL_DB_PORT}"
  if [[ "${dry_run}" == "true" ]]; then
    mysql_sync_log "DRY-RUN: mysqldump ... | gzip > ${DUMP_FILE}"
    return 0
  fi
  mysqldump \
    --host="${LOCAL_DB_HOST}" \
    --port="${LOCAL_DB_PORT}" \
    --user="${DB_USER}" \
    --password="${DB_PASSWORD}" \
    --single-transaction \
    --no-tablespaces \
    --routines \
    --triggers \
    "${DB_NAME}" | gzip > "${DUMP_FILE}"
  mysql_sync_log "Dump written: ${DUMP_FILE} ($(du -h "${DUMP_FILE}" | cut -f1))"
}

mysql_sync_restore_railway() {
  local dry_run="${1:-false}"
  mysql_sync_log "Restoring to Railway MySQL ${RAILWAY_MYSQL_HOST}:${RAILWAY_MYSQL_PORT}/${RAILWAY_MYSQL_DATABASE}"
  if [[ "${dry_run}" == "true" ]]; then
    mysql_sync_log "DRY-RUN: gunzip -c ${DUMP_FILE} | mysql ... ${RAILWAY_MYSQL_DATABASE}"
    return 0
  fi
  gunzip -c "${DUMP_FILE}" | mysql \
    --host="${RAILWAY_MYSQL_HOST}" \
    --port="${RAILWAY_MYSQL_PORT}" \
    --user="${RAILWAY_MYSQL_USER}" \
    --password="${RAILWAY_MYSQL_PASSWORD}" \
    --skip-ssl \
    "${RAILWAY_MYSQL_DATABASE}"
  mysql_sync_log "Railway restore complete"
}

mysql_sync_prune_backups() {
  local keep="${1:-14}"
  local dry_run="${2:-false}"
  if [[ "${dry_run}" == "true" ]]; then
    mysql_sync_log "DRY-RUN: prune backups keeping latest ${keep}"
    return 0
  fi
  find "${SYNC_BACKUP_DIR}" -name 'swarm-*.sql.gz' -type f | sort -r | tail -n +"$((keep + 1))" | xargs -r rm -f
  mysql_sync_log "Pruned old backups (keeping latest ${keep})"
}

mysql_sync_wait_tcp() {
  local host="${RAILWAY_MYSQL_HOST}"
  local port="${RAILWAY_MYSQL_PORT:-3306}"
  local max_attempts="${1:-60}"
  local sleep_secs="${2:-5}"
  mysql_sync_log "Waiting for MySQL TCP ${host}:${port} (max ${max_attempts} attempts)"
  local attempt=1
  while [[ "${attempt}" -le "${max_attempts}" ]]; do
    if (echo >"/dev/tcp/${host}/${port}") >/dev/null 2>&1; then
      mysql_sync_log "MySQL TCP ready after ${attempt} attempt(s)"
      return 0
    fi
    sleep "${sleep_secs}"
    attempt=$((attempt + 1))
  done
  mysql_sync_log "ERROR: MySQL TCP not ready at ${host}:${port}"
  return 1
}
