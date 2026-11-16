# PURPOSE: Operator/developer documentation for SWARM.
# 20 — emPOWER and Partner API compatibility

Two **independent** contracts. Do not merge them.

## A. Telemetry pull (Manufacturing Hub P2)

**Exists:** `docs/PARTNER_API.md`, `PartnerTelemetryController`, hashed keys, plant allow-list, sandbox seeder.

**Retain:** Read-only pull, no UI embed, no user sync, `device_id=node_{id}`, dual JSON naming, 15 min cron on emPOWER side, mapping on emPOWER (`MeterDeviceMap`).

**Improve:**

| Issue | Fix |
|---|---|
| Empty allow-list = all plants | Empty = none |
| Sandbox on by default | `dev` only |
| CC can hit `/admin/partner` | Super Admin only |
| SPA can paste key to test from browser | Test endpoint on backend using stored hash, never echo raw key |
| Health score default 85 | Use last rule engine output or `null` + `INCOMPLETE` |
| On-the-fly daily aggregates | Materialized table |
| No scopes / rate limit | Add |

**Do not break** emPOWER clients: keep v1 URLs and fields. Additive `quality`, `sourceKind` only.

**emPOWER will not call:** `/iot/batch`, `/devices/pair`, `/users`, HMI.

Webhooks remain deferred.

## B. Shared login (SaaS identity)

**Exists:** `IDENTITY_MODE`, `EmpowerSaaSAuthClient`, `/auth/sso/*`, Android redirect `com.nanofarm.swarm://auth/callback`.

**Retain:** emPOWER SaaS is identity SoT when configured; SWARM JWT still issued; plants stay in `biopower_db`.

**Improve:** PENDING for new SSO users; step-up MFA for privileged actions; local CC break-glass when SaaS is down.

**Local/dev:** keep `IDENTITY_MODE=local` so engineers are not blocked on client secrets.

BIOPOWER ERP (`erp.empowerbpg.com`) is **not** this login plane.

## C. NOVA DNA folder

`NOVA_DNA_source_for_SWARM/` is a reference slice from emPOWER. SWARM’s live assistant is `com.biopower.novaspace`. Do not run ERP NOVA against SWARM DB.

## Compatibility tests (required)

- Contract tests for Partner JSON fixtures (health, devices, history cursor).
- Isolation: key for plant 1 cannot read plant 2.
- SSO callback upsert does not change role of an existing Super Admin.
