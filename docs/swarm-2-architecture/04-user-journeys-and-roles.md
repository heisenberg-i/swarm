# PURPOSE: Operator/developer documentation for SWARM.
# 04 — User journeys and role matrix

## Roles that exist today

| Role | Where | Job |
|---|---|---|
| `CONTROL_CENTER_ADMIN` | Control Center shell only | nanoFarm provisions tenants |
| `SUPER_ADMIN` | Plant app | Company owner: plants, users, Field Devices, Partner keys, settings |
| `PLANT_ADMIN` | Plant app | Site engineer: plants they are assigned, pair hubs, HMI commands, users (operators) |
| `OPERATOR` | Plant app | Run the shift: dashboard, pair hub, view HMI, ack alerts |
| `ROLE_PARTNER` | API only | emPOWER backend pull |

`PlantAccessService.assertPlantAppUser` already blocks Control Center admins from the plant app.

## Target permission families

Permissions are **server-side**. The React sidebar is a hint.

| Permission | CC | Super | Plant Admin | Operator | Partner |
|---|---|---|---|---|---|
| `cc.tenant.write` | ✓ | | | | |
| `cc.user.approve` | ✓ | | | | |
| `cc.hmi.flag` | ✓ | | | | |
| `plant.read` | | assigned | assigned | assigned | allow-list |
| `plant.write` | | ✓ | assigned | | |
| `device.pair` | | ✓ | ✓ | ✓ | |
| `instrument.write` | | ✓ | optional later | | |
| `hmi.view` | | if enabled | if enabled | if enabled | |
| `hmi.command.sim` | | ✓ | ✓ | | |
| `hmi.command.live` | | commissioning + policy | commissioning + policy | | |
| `alert.ack` | | ✓ | ✓ | ✓ | |
| `alert.threshold.write` | | ✓ | ✓ | | |
| `user.manage` | | company | plant operators | | |
| `partner.keys` | | ✓ | | | |
| `partner.telemetry.read` | | | | | ✓ |
| `nova.chat` | | ✓ | ✓ | ✓ | |
| `ops.log.write` | | ✓ | ✓ | ✓ | |
| `report.download` | | ✓ | ✓ | view if assigned | |

Empty Partner allow-list → **no plants** (breaking change vs today; required).

Empty Operator `user_sensor_nodes` → all instruments on assigned plants (keep; document it). Non-empty → those nodes only.

## Journeys

### J1 — nanoFarm sells a site

1. Control Center: create company “Vulcan CBG”.
2. Create Super Admin (invite / one-time password, MFA enrolment).
3. Create plant “Line 1”, type `BIO_CNG`, assign to company.
4. Enable HMI only if a P&ID pack exists and the customer bought it.
5. Super Admin logs into the plant app (never Control Center).

**Today:** Super Admin created with password in the CC form; documented CC admin is seeded.  
**2.0:** No git-seeded passwords; invite token; audit.

### J2 — Super Admin staffs the plant

1. Users: add Plant Admin (site engineer) and Operators.
2. Assign plants. Optionally assign sensor groups to an operator.
3. Hardware is **not** registered on the Users form (already moved to Field Devices).

### J3 — Commissioning on plant Wi-Fi

1. Engineer joins plant LAN / setup AP `SWARM-Setup-{chipId}`.
2. Field Devices → Pair Sensor Hub: IP + device password.
3. SWARM creates temperature, humidity, methane points.
4. Hub POSTs `/iot/batch` with device credential (2.0) every 30s.
5. Add instrument for TIT/PIT/FIT/pH that are not on the hub.
6. Map instruments to equipment tags / process areas.

**Today:** Batch is unauthenticated.  
**2.0:** Device credential issued at pair time.

### J4 — Shift

1. Operator opens Dashboard on phone.
2. Sees stale banner if hub > timeout.
3. Acknowledges methane high.
4. Writes shift note “feeding skipped, monsoon”.
5. Asks NOVA “latest digester temperature” → measured value + time + node.

### J5 — emPOWER costing

1. Super Admin: emPOWER Partners → org id from emPOWER Settings + plant allow-list → issue `swk_…` once.
2. emPOWER vaults the key.
3. Cron GET `/partner/v1/health` then plants, devices, aggregates.
4. Maps `node_12` → meter on their hub. Unmapped devices ignored.

### J6 — SSO (production identity)

1. `IDENTITY_MODE=saas` with client secret.
2. Login redirects to accounts.empowerapp.in.
3. Callback upserts `identity_user_id`.
4. If the user has no company/plants, they see a pending screen — **not** an empty dashboard as a live operator.

**Today:** New SSO users are ACTIVE OPERATOR with no plants.  
**2.0:** PENDING until CC or Super Admin assigns.

## Journey gaps to close

| Journey | Gap |
|---|---|
| J1 | Seeded CC credentials; no MFA |
| J3 | Unauthenticated ingest; firmware defaults |
| J4 | No shift log; fake reports |
| J5 | Empty allow-list = all plants |
| J6 | SSO skips approval |
