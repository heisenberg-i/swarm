# PURPOSE: Operator/developer documentation for SWARM.
# 24 — Data migration and compatibility

## Principle

Expand/contract. Live MySQL on Pi is the production store. Cursor must **not** run irreversible migrations against production.

## Phase A — Baseline (compatible)

1. Flyway `V1__baseline` from a dumped schema (including Hibernate-added columns).
2. `ddl-auto: validate`.
3. No data rewrite.

## Phase B — Additive (compatible)

- `sensor_readings.quality` default `MEASURED`
- `sensor_readings.source_kind` default `MEASURED`
- `sensor_nodes.device_id` nullable FK
- `devices` backfill: group nodes by `device_chip_id`
- `plants.timezone` default
- `process_areas` seeded from plant_type template (enabled flags)
- `alert_policies` copied from YAML templates, **disabled** until accept
- `users.token_version` default 0
- `audit_events` empty

SPA ignores unknown JSON fields; backend ignores unknown columns.

## Phase C — Ingest auth compat

- New pairs issue `swd_` secrets.
- Old hubs: `CREDENTIAL_MISSING`, compat unauthenticated if `SWARM_IOT_COMPAT_LEGACY=true`.
- Firmware update ships; Field Devices shows “upgrade hub”.
- After N days, compat off per plant.

## Phase D — Partner

- Additive fields only.
- Empty allow-list: **data fix first** (ensure every production org has plantIds) **then** enforce deny. Order matters.

## Phase E — Reports

Old rows with fake `file_path`: status `INVALID`; regenerate.

## Phase F — Rename BIOPOWER (optional, late)

Package `com.biopower` → `com.swarm` is cosmetic. Do not couple to security work. Docker service names can wait.

## Rollback

Every Flyway version has a documented down **or** is additive-only so rollback is “don’t run next”. Destructive downs are not used.

## Irreversible list (human approval)

- Dropping `sensor_nodes` in favour of `instruments`
- Changing Partner `device_id` format
- Forcing all hubs to new firmware
- Hashing algorithm change for API keys (need re-issue)

## Seeded credentials

On first 2.0 deploy: disable documented CC password if still present; force bootstrap. Do not print the old password in migration notes.
