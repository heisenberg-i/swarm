# PURPOSE: Operator/developer documentation for SWARM.
# 25 — Implementation roadmap

Incremental slices. Each slice: tests, progress note in `docs/swarm-2-architecture/progress.md` (created when implementation starts), no production deploy.

## Slice 0 — Guardrails (week 1)

- Flyway baseline + `ddl-auto: validate` on prod profile
- Stop documenting default passwords; remove seeder of known CC password
- `/auth/me` and change-password authenticated
- Partner empty allow-list deny **after** data check
- `/admin/partner` Super Admin only
- ACL on sensor-node get, reports, AI ack
- UI: remove fake 85/GOOD; HMI SIMULATION banner (already true in API)
- Quarantine unused HMI React
- ArchUnit + first security integration tests

**Stop:** no live JWT_SECRET rotation without operator.

## Slice 1 — Field Devices + ingest integrity (week 2–3)

- `devices` table backfill
- Device secret on pair; firmware header support
- Compat flag for old hubs
- Quality + idempotency on ingest
- Rate limit login + ingest
- Firmware: stop compiling default Wi-Fi secrets (setup-only)

## Slice 2 — Sessions (week 3–4)

- Access/refresh + logout revoke + disable user
- MFA TOTP for CC + Super Admin
- SSO PENDING for new users
- Android Keystore if plugin available; else refresh in memory + https-only cookie for web

## Slice 3 — Alerts + reports (week 4–6)

- `alert_policies` per plant/instrument
- Unbind hub DHT from digester templates
- Email notifications if MAIL_* present; else in-app only
- Real PDF/XLSX generate + download
- Materialize daily aggregates

## Slice 4 — Domain + ops (week 6–8)

- Process areas + equipment mapping UI (simple)
- Shift log + downtime + feedstock
- Maintenance page = condition + work orders (minimal)
- Server `hmi_commands` audit
- Commissioning table (still simulation)

## Slice 5 — NOVA + Partner polish (week 8–9)

- Skills `telemetry.history`, `plant.maintenance` / ops
- Evidence array on chat
- Partner scopes, rate limit, backend key test
- Contract tests

## Slice 6 — Android + copy (week 9–10)

- Cleartext RFC1918 only
- Help screenshots / pairing copy already Field Devices
- Diagnostics panel: remove fake EtherCAT

## Later (not 2.0 MVP)

- Live PLC commands
- SSE
- MQTT on Pi
- Webhooks
- Timescale
- Package rename
- SMS/WhatsApp
- Entitlements billing

## Progress documentation

When implementation starts, maintain:

`docs/swarm-2-architecture/progress.md` — slice, PR, tests run, leftover risks.

Each slice commit message: `swarm-2: slice N — <why>`.
