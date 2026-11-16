# PURPOSE: Operator/developer documentation for SWARM.
# 01 — Existing repository audit

Investigation of `/home/lavender/swarm_webapp`. Documentation was treated as a claim until code confirmed it.

## Stack that actually runs

| Layer | Reality |
|---|---|
| Frontend | React 18 + MUI + Vite 7 SPA. Two shells: Control Center vs plant app, **role-split in `App.jsx`**, not separate hosts. |
| Backend | Spring Boot 3.2.5, Java 17, Maven artifact `com.biopower:plant-monitoring`, context path `/api`. |
| Database | MySQL 8 in Docker/Pi; local H2 `dev` profile. Hibernate `ddl-auto: update`. **No Flyway/Liquibase.** |
| Firmware | One sketch: `firmware/swarm_esp8266_hub/swarm_esp8266_hub.ino` (ESP8266, DHT11, MQ5). No ESP32 sketch. |
| Android | Capacitor wrapper of the same SPA (`com.nanofarm.swarm`). |
| NOVA DNA folder | Slice of emPOWER engines for porting patterns. **Not compiled into SWARM.** Live NOVA is `com.biopower.novaspace`. |

Servlet base: `{host}/api/...`. Vite proxies `/api` to `127.0.0.1:8081` in development.

## What the README claims vs what the code does

| README / docs claim | Code |
|---|---|
| ESP32, PLC, MQTT gateway as live monitoring | ESP8266 HTTP `POST /iot/batch`. MQTT class exists, **`MQTT_ENABLED` defaults false**. No Modbus/PLC. |
| Email/SMS/WhatsApp-ready alerts | Thresholds + MySQL `alerts`. No notifier implementation. |
| AI plant health engine | Deterministic `AiHealthService` rules, not a trained model. |
| Predictive maintenance / RUL | Regenerated heuristic on `GET /maintenance/{plantId}`. Always `equipment_type=SENSOR`. |
| PDF reports | `ReportService` writes a row with a synthetic `file_path`. iText/POI unused. Download button has no handler. |
| Control Center at `control.swarm.co.in` | Same SPA; `homeRoute.js` notes DNS is not the live split. |
| HMI SVG overlay (`PidDiagram`) | Live path is iframe `biogas-pfd.html` + `postMessage`. React Tata overlays are unused. |
| Complete ER diagram | `docs/ER_DIAGRAM.md` omits companies, partner, HMI, Nova, chip/IP, `user_sensor_nodes`. |

## Controllers that exist

| Class | Base | Status |
|---|---|---|
| `HealthController` | `GET /health` | Live |
| `AuthController` | `/auth` | Live; `/auth/**` is **permitAll** |
| `IoTController` | `/iot` | Live, **unauthenticated** |
| `DeviceController` | `/devices` | Pairing + ESP proxy |
| `PlantController` | `/plants` | Live |
| `SensorNodeController` | `/sensor-nodes` | Live |
| `DashboardController` | dashboard, analytics, alerts, AI | Live |
| `MaintenanceController` | `/maintenance` | Heuristic |
| `ReportSettingsController` | reports + settings | Reports stub |
| `NotificationController` | `/notifications` | Alert counts, not push |
| `UserController` | `/users` | Live |
| `HmiController` | `/hmi` | Simulated |
| `ControlCenterController` | `/control-center` | Live |
| `PartnerTelemetryController` | `/partner/v1` | Matches `docs/PARTNER_API.md` |
| `PartnerAdminController` | `/admin/partner` | Live |
| `NovaSpaceOpController` | `/nova-space-op` | Live, skills incomplete |

## Security findings (no secrets reproduced)

1. `POST /iot/data` and `POST /iot/batch` are permitAll. Payload is `plantId` + `nodeId` + `sensorType` + `value`. No device HMAC.
2. Entire `/auth/**` tree is public, including `/me` and `/change-password`.
3. JWT filter does not re-check `User.status == ACTIVE` after disable.
4. Logout is a no-op (stateless JWT, no denylist).
5. Empty Partner plant allow-list is treated as **all plants**.
6. `/admin/**` allows `CONTROL_CENTER_ADMIN` as well as Super Admin (Partner keys).
7. Several GETs skip plant ACL (`sensor-nodes/{id}`, reports without plantId, AI acknowledge).
8. ESP device password travels as query parameter on backend proxy routes (frontend currently talks to the hub on LAN instead).
9. Hibernate `ddl-auto: update` plus ad-hoc `*Migration` `@Component`s is the schema story.
10. Documented default Control Center credentials are seeded on primary (`ProductionDataCleanup`). Treat as compromised.
11. Firmware sketch contains hardcoded Wi-Fi and device-password defaults. Treat as compromised.
12. Partner sandbox key is enabled by default on primary (`SWARM_PARTNER_SANDBOX_ENABLED` default true).

## Schema vs JPA

`database/schema.sql` initializes Docker MySQL. Hibernate then adds columns the SQL file does not list, notably:

- `sensor_nodes.device_chip_id`, `sensor_nodes.device_ip`
- `user_sensor_nodes`

Boot migrators: `UserRoleColumnMigration`, `UserStatusColumnMigration`, `SensorTypeColumnMigration`, `HmiEnabledColumnMigration`, `HmiEquipmentKindColumnMigration`, `HmiSchemaMigration`, `CompanyAccessMigrator`.

## Tests that exist

Backend unit tests only (no frontend tests):

- `AnswerGuardTest`, `NovaToolPermissionServiceTest`, `SwarmSearchSlotsTest`
- `DeploymentRolePropertiesTest`, `SwarmUrlServiceTest`, `ExternalOrgIdsTest`

No controller/security/ingest integration tests.

## Firmware and ingest path that is real

1. Browser on plant LAN: `espClient.js` → hub `/info`, `/api/status`.
2. JWT `POST /api/devices/pair` creates/claims TEMPERATURE, HUMIDITY, METHANE nodes.
3. Browser `POST http://{ip}/swarm/configure` with `swarmUrl`, plantId, node IDs.
4. Hub every 30s: `POST {swarmUrl}/iot/batch`.
5. Dashboard may poll hub `/api/status` if session-stored ESP credentials exist.

Backend `EspProxyService` exists; the live pairing UI does not use it.

## HMI that is real vs unused

**Live:** `PlantHmi.jsx` → `BiogasPlantDiagram.jsx` iframe `/hmi/biogas-pfd.html` + `swarm-hmi.js`. State polled every 4s. Commands hit `POST /hmi/{id}/commands` and `/master`. `HmiService` always returns `simulationMode: true`.

**Unused:** `PidDiagram`, `PidSchematic`, Tata Steel equipment data, `PlantMasterControls` (auto-sequence UI), `hmiAPI.getDiagram`.

HMI is shown only if Control Center set `plants.hmi_enabled` **and** feedstock contains `P&ID` or name contains `biomethanation`.

## Deployment that is real

- **Primary:** Raspberry Pi Docker (`docker-compose.rpi.yml`): MySQL + backend + frontend. MQTT off. Cloudflare Tunnel → `api.swarm.co.in` / `app.swarm.co.in`.
- **Standby:** Railway MySQL + backend, `SWARM_DEPLOYMENT_ROLE=standby`, Nova forced off.
- Generic `docker-compose.yml` still starts Mosquitto but does not set `MQTT_ENABLED=true`.

## Honest summary

The product is a working multi-tenant plant monitor with real pairing, real ingest, real Control Center, a real Partner pull API, and a simulated HMI. It is **not** yet an enterprise industrial platform: ingest is unauthenticated, reports are fake files, maintenance is a score, MQTT/PLC are absent in production, and schema is evolved by Hibernate.
