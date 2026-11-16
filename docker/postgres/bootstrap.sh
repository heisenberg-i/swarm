#!/bin/bash
# PURPOSE: Restores the SWARM PostgreSQL backup into the Docker Timescale volume, then clears non-NOVA rows once.
set -euo pipefail

MARKER="${BOOTSTRAP_MARKER:-/state/swarm_app_bootstrapped}"
export PGPASSWORD="${POSTGRES_PASSWORD:?POSTGRES_PASSWORD is required}"

until pg_isready -h postgres -U "${POSTGRES_USER}" -d "${POSTGRES_DB}" >/dev/null 2>&1; do
  echo "Waiting for PostgreSQL..."
  sleep 2
done

if [ -f "$MARKER" ]; then
  echo "SWARM database already bootstrapped; skipping restore/reset."
  exit 0
fi

DUMP="$(ls -1t /backups/swarm_pg_*.dump 2>/dev/null | head -1 || true)"
if [ -z "${DUMP}" ]; then
  echo "ERROR: no backups/swarm_pg_*.dump found to restore." >&2
  exit 1
fi

echo "Restoring ${DUMP} into ${POSTGRES_DB}..."
# Timescale catalogs/extension may already exist in the image; ignore duplicate-object errors.
set +e
pg_restore -h postgres -U "${POSTGRES_USER}" -d "${POSTGRES_DB}" \
  --no-owner --no-acl --disable-triggers \
  "${DUMP}"
RESTORE_RC=$?
set -e
echo "pg_restore exit code: ${RESTORE_RC}"

if ! psql -h postgres -U "${POSTGRES_USER}" -d "${POSTGRES_DB}" -tAc \
    "SELECT 1 FROM information_schema.tables WHERE table_schema='public' AND table_name='flyway_schema_history'" | grep -q 1; then
  echo "ERROR: restore did not produce flyway_schema_history" >&2
  exit 1
fi

echo "Clearing non-NOVA application data..."
psql -h postgres -U "${POSTGRES_USER}" -d "${POSTGRES_DB}" -v ON_ERROR_STOP=1 -f /reset/reset-non-nova.sql

touch "$MARKER"
echo "Bootstrap complete."
