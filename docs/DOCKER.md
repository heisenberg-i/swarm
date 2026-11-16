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
- The first start restores `backups/swarm_pg_*.dump` into the `swarm_pg_data` volume, then runs `database/reset-non-nova.sql`. Later starts skip restore (marker file in the volume).

## Persistent storage

Docker volume `swarm_pg_data` holds PostgreSQL + TimescaleDB data. Removing containers does not delete it. To wipe the application database:

```bash
docker compose down
docker volume rm swarm_webapp_swarm_pg_data
```

That also discards the bootstrap marker, so the next `up` restores the dump and resets non-NOVA rows again.

## Retiring host PostgreSQL (`192.168.29.229:5433`)

Do this only after Compose is healthy and you have a verified dump in `backups/`.

1. Confirm the app uses Docker: `docker compose ps` shows `swarm-postgres` healthy, login works at http://localhost.
2. Stop application processes that still point at `192.168.29.229:5433`.
3. Keep the host instance in read-only / stopped state for a soak period.
4. Do **not** drop Timescale or the `swarm` database until that soak is complete.
5. The IoT platform container `swarm-iot-postgres` on `127.0.0.1:5432` is a **different** project (`swarm-iot-platform`). Leave it alone.

SWARM Compose does not start a second host-published PostgreSQL port, so it does not compete with `swarm-iot-postgres`.

## MQTT

Compose runs Eclipse Mosquitto as `swarm-mqtt` on the internal `swarm` network (`mosquitto:1883`). The port is **not** published: host `1883` is already used by `swarm-iot-mosquitto`.

The backend sets `MQTT_ENABLED=false` by default. That matches current ingest, which is HTTP (`/api/iot/...`) rather than MQTT. The broker is idle until you opt in.

If devices must publish MQTT from outside Docker, add a **non-conflicting** host mapping, for example `127.0.0.1:1884:1883`, and set `MQTT_ENABLED=true` plus `MQTT_BROKER=tcp://mosquitto:1883` for the backend. Do not bind `0.0.0.0:1883` on this host.

## Flyway

`SPRING_FLYWAY_VALIDATE_ON_MIGRATE` is `true`. Applied checksums for `V1` and `V2` were aligned to the current migration files in the JAR. Do not edit applied migration files and do not `flyway repair` unless checksums are verified again.

## Restart

`docker compose restart` and `docker restart swarm-postgres` keep `swarm_pg_data`. Never use `docker compose down -v` on a system that should retain data.
