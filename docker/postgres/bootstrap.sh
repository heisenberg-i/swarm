#!/bin/bash
# PURPOSE: Initializes an empty SWARM Timescale database, or restores backups/swarm_pg_*.dump once.
set -euo pipefail

MARKER="${BOOTSTRAP_MARKER:-/state/swarm_app_bootstrapped}"
CORE_SQL="${CORE_INIT_SQL:-/init/swarm-core-init.sql}"
export PGPASSWORD="${POSTGRES_PASSWORD:?POSTGRES_PASSWORD is required}"

psql_q() {
  psql -h postgres -U "${POSTGRES_USER}" -d "${POSTGRES_DB}" -tAc "$1"
}

psql_file() {
  psql -h postgres -U "${POSTGRES_USER}" -d "${POSTGRES_DB}" -v ON_ERROR_STOP=1 -f "$1"
}

until pg_isready -h postgres -U "${POSTGRES_USER}" -d "${POSTGRES_DB}" >/dev/null 2>&1; do
  echo "Waiting for PostgreSQL..."
  sleep 2
done

core_count() {
  psql_q "SELECT COUNT(*) FROM information_schema.tables
          WHERE table_schema = 'public' AND table_type = 'BASE TABLE'
            AND table_name IN ('tenants', 'devices', 'sensor_messages', 'sensor_readings');"
}

public_table_count() {
  psql_q "SELECT COUNT(*) FROM information_schema.tables
          WHERE table_schema = 'public' AND table_type = 'BASE TABLE';"
}

# Align Flyway history after removing the invalid '# PURPOSE' header from V1/V2.
# Those versions were already applied on restored dumps; only rewrite the known old CRCs.
align_flyway_checksums() {
  if [ "$(psql_q "SELECT 1 FROM information_schema.tables WHERE table_schema='public' AND table_name='flyway_schema_history'" | tr -d '[:space:]')" != "1" ]; then
    return 0
  fi
  psql -h postgres -U "${POSTGRES_USER}" -d "${POSTGRES_DB}" -v ON_ERROR_STOP=1 <<'SQL'
UPDATE flyway_schema_history
   SET checksum = -729282543
 WHERE version = '1' AND checksum = 841724909;
UPDATE flyway_schema_history
   SET checksum = 2127360042
 WHERE version = '2' AND checksum IN (-301239876, 1381351303);
SQL
}

verify_core() {
  local n hyper dim interval compression
  n="$(core_count | tr -d '[:space:]')"
  if [ "$n" != "4" ]; then
    echo "ERROR: expected 4 core tables (tenants, devices, sensor_messages, sensor_readings), found ${n}." >&2
    return 1
  fi
  hyper="$(psql_q "SELECT COUNT(*) FROM timescaledb_information.hypertables WHERE hypertable_name = 'sensor_readings'" | tr -d '[:space:]')"
  if [ "$hyper" != "1" ]; then
    echo "ERROR: sensor_readings is not a Timescale hypertable." >&2
    return 1
  fi
  dim="$(psql_q "SELECT column_name FROM timescaledb_information.dimensions WHERE hypertable_name = 'sensor_readings'" | tr -d '[:space:]')"
  if [ "$dim" != "recorded_at" ]; then
    echo "ERROR: sensor_readings time dimension is '${dim}', expected recorded_at." >&2
    return 1
  fi
  interval="$(psql_q "SELECT time_interval::text FROM timescaledb_information.dimensions WHERE hypertable_name = 'sensor_readings'" | tr -d '[:space:]')"
  if [ "$interval" != "7days" ]; then
    echo "ERROR: sensor_readings chunk interval is '${interval}', expected 7 days." >&2
    return 1
  fi
  compression="$(psql_q "SELECT compression_enabled::text FROM timescaledb_information.hypertables WHERE hypertable_name = 'sensor_readings'" | tr -d '[:space:]')"
  if [ "$compression" != "f" ] && [ "$compression" != "false" ]; then
    echo "ERROR: sensor_readings compression is '${compression}', expected off." >&2
    return 1
  fi
}

CORE_COUNT="$(core_count | tr -d '[:space:]')"
PUBLIC_COUNT="$(public_table_count | tr -d '[:space:]')"
DUMP="$(ls -1t /backups/swarm_pg_*.dump 2>/dev/null | head -1 || true)"

if [ -f "$MARKER" ]; then
  if [ "$CORE_COUNT" = "4" ]; then
    echo "SWARM database already bootstrapped; skipping restore/init."
    align_flyway_checksums
    exit 0
  fi
  echo "ERROR: bootstrap marker exists but core tables are missing (${CORE_COUNT}/4). Refusing to continue." >&2
  exit 1
fi

if [ "$CORE_COUNT" = "4" ]; then
  echo "Core SWARM tables already present; recording bootstrap marker."
  verify_core
  align_flyway_checksums
  touch "$MARKER"
  echo "Bootstrap complete."
  exit 0
fi

if [ "$CORE_COUNT" != "0" ]; then
  echo "ERROR: partial core schema (${CORE_COUNT}/4 tables). Refusing to initialize or restore." >&2
  exit 1
fi

if [ -n "${DUMP}" ]; then
  echo "Restoring ${DUMP} into ${POSTGRES_DB}..."
  set +e
  pg_restore -h postgres -U "${POSTGRES_USER}" -d "${POSTGRES_DB}" \
    --no-owner --no-acl --disable-triggers \
    "${DUMP}"
  RESTORE_RC=$?
  set -e
  echo "pg_restore exit code: ${RESTORE_RC}"

  if [ "$(psql_q "SELECT 1 FROM information_schema.tables WHERE table_schema='public' AND table_name='flyway_schema_history'" | tr -d '[:space:]')" != "1" ]; then
    echo "ERROR: restore did not produce flyway_schema_history." >&2
    exit 1
  fi

  echo "Clearing non-NOVA application data..."
  psql_file /reset/reset-non-nova.sql
  verify_core
  align_flyway_checksums
  touch "$MARKER"
  echo "Bootstrap complete."
  exit 0
fi

if [ "$PUBLIC_COUNT" != "0" ]; then
  echo "ERROR: database is not empty (${PUBLIC_COUNT} public table(s)) and no backups/swarm_pg_*.dump was found. Refusing to initialize." >&2
  exit 1
fi

if [ ! -f "$CORE_SQL" ]; then
  echo "ERROR: core init SQL is missing at ${CORE_SQL}." >&2
  exit 1
fi

echo "No backup found; initializing empty SWARM core schema from ${CORE_SQL}..."
psql_file "$CORE_SQL"
verify_core
touch "$MARKER"
echo "Fresh core schema initialized. Flyway will apply application migrations when the backend starts."
echo "Bootstrap complete."
