# PURPOSE: Operator/developer documentation for SWARM.
# 00 — Executive vision and product scope

## The product we are evolving

SWARM is nanoFarm’s plant-health product for anaerobic digestion and waste-to-energy sites. Operators, plant engineers and company Super Admins already use it to:

- Provision a company from Control Center.
- Assign plants and people.
- Pair a SWARM MODEL hub (ESP8266, DHT11 + MQ5) on plant Wi-Fi.
- Keep an instrument inventory (hub points and standalone P&ID transmitters).
- Watch live readings, alerts, a simulated biomethanation HMI, and a rule-based health score.
- Let emPOWER ERP **pull** telemetry through an org-scoped Partner API.
- Optionally sign in through emPOWER SaaS (`accounts.empowerapp.in`) while plants stay in SWARM’s database.

That is the product. SWARM 2.0 is **not** a new IoT platform with the SWARM logo attached. It is this same workflow, made reliable enough for industrial sale.

Java packages are still `com.biopower`. Docker services are still named `biopower-*` in the generic compose file. The user-facing name is SWARM. The architecture keeps the product name and retires the dual branding in code over time (ADR-003).

## What “better” means here

| Dimension | Today | SWARM 2.0 |
|---|---|---|
| Product identity | Plant health for biogas / Bio-CNG / sanitation / WtE | Unchanged |
| Architecture | Single Spring Boot app + React SPA, mixed concerns | **Modular monolith** with package boundaries |
| Security | JWT + Partner keys; unauthenticated ingest; public `/auth/**` | Device identity, scoped keys, MFA for privileged roles, server-side RBAC |
| Telemetry | MySQL rows, no quality flags on ingest | Validated envelope, quality, timestamps UTC, retention jobs that already exist |
| HMI | Simulated iframe PFD, `simulationMode: true` | Explicit simulation; real PLC only after commissioning approval |
| Intelligence | Rule-based `AiHealthService` + NOVA skills over plant data | Same grounding rule: never fabricate readings |
| Operations | Alerts + heuristic “maintenance”; no shift/feedstock logs | Operator logbook, downtime, feedstock, work orders |
| Reports | DB row with a fake `file_path`; Download does nothing | Real PDF/XLSX using libraries already on the classpath |
| Scale | Pi primary + Railway MySQL/API standby | Same topology first; extract services only when justified |

## Scope in

- Biogas, Bio-CNG/CBG, sanitation, organic waste, STP, waste-to-energy.
- Configurable process areas (not one hard-coded plant).
- Control Center (nanoFarm sells and provisions) vs company plant app (customer operates).
- ESP hubs, industrial transmitters, future PLC/Modbus/MQTT gateways.
- emPOWER SSO (people) and Partner API (telemetry) as **separate** contracts.
- Android as the same SPA in Capacitor, not a second product.
- NOVA as a read-only plant assistant over SWARM data.

## Scope out (for 2.0)

- Building emPOWER ERP, manufacturing costing, or GST inside this repo.
- Embedding SWARM UI in ERP.
- Cloud commands that bypass local PLC interlocks.
- A microservices mesh, Kubernetes, or Timescale “because industrial IoT”.
- Claiming ISO 27001 / IEC 62443 certification without a formal assessment.
- Universal device adapter / EMQX as a platform product (hub MQTT is a later SWARM increment, not an emPOWER deliverable).

## Success criteria

A biogas engineering company can:

1. Create a tenant in Control Center and a Super Admin without shipping default passwords.
2. Commission a SWARM hub on site from **Field Devices → Pair Sensor Hub**.
3. See live methane, temperature, humidity and later P&ID tags in one inventory.
4. Acknowledge alerts with an audit trail.
5. Open HMI labelled **SIMULATION** until a commissioned plant is approved.
6. Hand emPOWER a Partner key that can only read allow-listed plants.
7. Ask NOVA “what is digester temperature?” and get a timestamped measured value or an honest “no reading”.

## Non-negotiables

- Same SWARM product workflows stay.
- Frontend routes are not security.
- Cloud monitoring ≠ machine safety.
- AI must cite evidence; estimates must be labelled estimates.
- Implementation is incremental; this folder is the design source, not a rewrite mandate.
