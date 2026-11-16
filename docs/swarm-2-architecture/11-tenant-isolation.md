# PURPOSE: Operator/developer documentation for SWARM.
# 11 — Tenant isolation strategy

## Exists today

- `companies` ↔ `plants.company_id` ↔ `users.company_id`.
- Users see plants via `user_plants`, not “all plants in company” automatically for Plant Admin/Operator.
- Super Admin is also assigned plants (CC `assignPlants` / Super Admin create).
- Control Center sees all companies (intentional).
- Partner isolation via `partner_org_plants` — **broken if empty**.
- `PlantAccessService` is the kernel.

## Isolation dimensions

| Dimension | Key | Enforced by |
|---|---|---|
| Tenant | `company_id` | Every plant-scoped query |
| Plant | `plant_id` | `assertCanAccessPlant` |
| Operator node set | `user_sensor_nodes` | Optional filter |
| Partner | allow-list | `PartnerPrincipal` — empty deny |
| Control Center | role | Path matcher; no telemetry |

## Rules

1. **No query without a tenant context** except CC and health.
2. Super Admin: all plants **assigned to that user**, which CC should set to the company set. If a plant is added later, CC or Super Admin must assign it (do not silently expose).
3. IDs are not authorization. `GET /plants/7` checks ACL even if the user guessed 7.
4. Nova skills receive `principal` and must use the same ACL (partially true today).
5. Analytics/reports with null plantId are forbidden.
6. File download looks up `plant_id` on the report row then ACL.

## Database enforcement (phased)

Phase 1: service-layer (today, fixed holes).  
Phase 2: optional MySQL views per tenant — skip, high cost.  
Phase 3: row-level security — not in MySQL 8 community as Postgres RLS; stay in app + tests.

Add integration tests: user A cannot read user B plant readings, Partner A cannot read plant 2.

## Cross-tenant features that must never exist

- Global “all plants” dashboard for Super Admin of company A that accidentally includes unassigned company B (CC overview is nanoFarm-wide **counts**, not readings — keep it that way).
- Shared `system_settings` that leak SMTP to tenants: settings are **platform** or **per company**. Today they are global key-value — treat as platform-only, Super Admin of a tenant should not change MQTT broker for the Pi.

## Partner

`external_org_id` is the emPOWER tenant helper (e.g. numeric `001`), not a SWARM company id. Mapping:

`emPOWER org` → `partner_organizations` → allow-listed `plants` ⊆ one SWARM `company`.

Do not allow a Partner org to span two SWARM companies.

## Standby / replica

Railway standby must use the same ACLs. Never run sandbox Partner keys on standby (already documented false; enforce in `application-standby.yml`).
