# PURPOSE: Explains how to run SWARM with Docker Compose and how to retire the old host PostgreSQL.

## Start

```bash
cp .env.example .env
# set DB_PASSWORD, JWT_SECRET, SWARM_BOOTSTRAP_ADMIN_EMAIL, SWARM_BOOTSTRAP_ADMIN_PASSWORD
docker compose up -d --build
```

- Frontend: http://localhost
- API: http://localhost:8080/api
- PostgreSQL is **not** published on the host. It lives on the `swarm` Docker network as `postgres:5432`.
- First start of an **empty** database creates the four core tables (`tenants`, `devices`, `sensor_messages`, `sensor_readings` hypertable) from `database/swarm-core-init.sql`. Flyway then applies `V1`/`V2`. V2 does **not** insert login accounts.
- Set `SWARM_BOOTSTRAP_ADMIN_EMAIL` and `SWARM_BOOTSTRAP_ADMIN_PASSWORD` so the first Control Center admin is created when none exists.
- If `backups/swarm_pg_*.dump` is present and the database is empty, that dump is restored and `database/reset-non-nova.sql` runs once. Existing administrators in that dump are kept.
- Later starts skip schema init (marker file in the `swarm_bootstrap_state` volume). Checksum alignment still runs and does not modify user rows.

## Persistent storage

Docker volume `swarm_pg_data` holds PostgreSQL + TimescaleDB data. Removing containers does not delete it. To wipe the application database:

```bash
docker compose down
docker volume rm swarm_webapp_swarm_pg_data
```

That also discards the bootstrap marker. The next `up` initializes an empty core schema, or restores a dump if `backups/swarm_pg_*.dump` exists.

## Database bootstrap

`postgres-bootstrap` runs once after Postgres is healthy and before the backend starts.

| Database state | Backup dump | Result |
|----------------|-------------|--------|
| Empty (no public tables) | none | Create core Timescale schema from `database/swarm-core-init.sql`. |
| Empty | `backups/swarm_pg_*.dump` | Restore the newest dump, then run `database/reset-non-nova.sql`. |
| Core tables already present | ignored | Skip. Marker is recorded if missing. |
| Partial core schema | any | Fail. Backend does not start. |
| Other public tables, no core schema | none | Fail. Nothing is overwritten. |
| Marker present, core tables missing | any | Fail. |

`sensor_readings` is a hypertable on `recorded_at` with a 7-day chunk interval and compression off. NOVA tables are not dropped or truncated.

Restarting Compose does not re-initialize. Do not run SQL by hand.

## Retiring host PostgreSQL (`192.168.29.229:5433`)

Do this only after Compose is healthy and you have a verified dump in `backups/`.

1. Confirm the app uses Docker: `docker compose ps` shows `swarm-postgres` healthy, login works at http://localhost.
2. Stop application processes that still point at `192.168.29.229:5433`.
3. Keep the host instance in read-only / stopped state for a soak period.
4. Do **not** drop Timescale or the `swarm` database until that soak is complete.
5. The IoT platform container `swarm-iot-postgres` on `127.0.0.1:5432` is a **different** project (`swarm-iot-platform`). Leave it alone.

SWARM Compose does not start a second host-published PostgreSQL port, so it does not compete with `swarm-iot-postgres`.

## MQTT

Compose project name is `swarm_webapp`. Mosquitto is `swarm-mqtt` on the internal network at `mosquitto:1883`. Host `1883` stays with `swarm-iot-mosquitto`. SWARM publishes **loopback only** `127.0.0.1:1884:1883` so a test client on this host can publish without colliding.

Set `MQTT_ENABLED=true` and `MQTT_BROKER=tcp://mosquitto:1883` for the backend. HTTP ingest (`POST /api/iot/data` and `/api/iot/batch`) remains available.

`GET /api/health` MQTT fields:

| Field | Meaning |
|-------|---------|
| `enabled` | `MQTT_ENABLED` |
| `configured` | Adapter bean was created (not proof of a live socket) |
| `connected` | Broker subscribe succeeded (use this, not `configured`) |
| `messagesReceived` / `failures` | Ingest counters |
| `lastError` | Last ingest or connection error (no credentials) |

### End-to-end verification

Do **not** publish fabricated telemetry into the live database. Use a **paired** device, or an isolated broker/database.

1. Broker from the backend container: `docker exec swarm-backend getent hosts mosquitto`
2. Health: `curl -sS http://127.0.0.1:8080/api/health` — `mqtt.connected` must be `true`.
3. Mosquitto log: `docker logs swarm-mqtt` shows client `biopower-server-inbound`.
4. A registered hub publishes to `swarm/v1/{tenantId}/{plantId}/{deviceUid}/telemetry` on `127.0.0.1:1884` (or `mosquitto:1883` from Docker). Payload example (replace ids):

```json
{
  "schemaVersion": "1.0",
  "deviceId": "<deviceUid>",
  "messageId": "<unique-id>",
  "timestamp": "2026-11-16T10:00:00Z",
  "readings": [{"channelId": "<mapped-channel>", "value": 42.0}]
}
```

5. Counters: `messagesReceived` increases. Invalid device or payload increases `failures` and writes `ingest_rejected` (not `sensor_readings`).
6. API (authenticated): `GET /api/dashboard/{plantId}` and `GET /api/analytics/{plantId}?sensorType=METHANE&start=<ISO-Z>&end=<ISO-Z>`. Optional `nodeId` and `deviceUid`.
7. UI: Dashboard and Analytics poll the same APIs. Inventory shows `lastValue` / Live vs Stale.

Rebuild/restart of `swarm-backend` restarts NOVA in the same JVM. Recreating only `swarm-mqtt` does not.

## Flyway

`SPRING_FLYWAY_VALIDATE_ON_MIGRATE` is `true`. V1 and V2 are already applied on restored and current databases. The files were later corrected (invalid `# PURPOSE` header, then removal of hardcoded administrator inserts). On startup the backend updates **only** these known legacy checksums, then validates:

| Version | Legacy checksums | Current file |
|---------|------------------|--------------|
| V1 | `841724909` | `-729282543` |
| V2 | `-301239876`, `1381351303` | `2127360042` |

Applied migrations are not re-executed. Existing users and passwords are unchanged. Do not run a blanket `flyway repair`.

## Authentication and CORS

The API context path is `/api`. Compose publishes the backend on host `8080` and the frontend nginx on host `80`, which proxies `/api/` to the backend. Sign-in is `POST /api/auth/login`.

A previous HTTP **403** on that path with body `Invalid CORS request` happened when the UI was opened at a LAN origin such as `http://192.168.29.229` while `CORS_ORIGINS` listed only localhost. `SWARM_CORS_ALLOW_ANY_HTTP` defaults to `true` so those origins work. Set it `false` and list exact origins in `CORS_ORIGINS` to lock down.

The frontend production image is built with `VITE_API_URL=/api` so the browser stays same-origin through nginx. Do not invent a backend `/` route; a 404 there is expected.

## Restart

`docker compose restart` and `docker restart swarm-postgres` keep `swarm_pg_data`. Never use `docker compose down -v` on a system that should retain data.
