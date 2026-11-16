# PURPOSE: Operator/developer documentation for SWARM.
# Installation Guide

SWARM’s supported install path is Docker Compose with PostgreSQL 16 + TimescaleDB. See [DOCKER.md](./DOCKER.md) for Compose services, volumes, and retiring the old host PostgreSQL.

## Prerequisites

- Docker and Docker Compose
- A `.env` file copied from `.env.example` (never commit `.env`)

## Docker (recommended)

```bash
cp .env.example .env
# Set DB_PASSWORD, JWT_SECRET, SWARM_BOOTSTRAP_ADMIN_EMAIL, SWARM_BOOTSTRAP_ADMIN_PASSWORD
docker compose up -d --build
```

| Service | Published port | Notes |
|---------|----------------|-------|
| Frontend | 80 | nginx serving the React app |
| Backend | 8080 | Spring Boot API at `/api` |
| PostgreSQL | none | Internal `postgres:5432` only |
| MQTT | none | Internal `mosquitto:1883`; app ingest stays off unless `MQTT_ENABLED=true` |

Open http://localhost (or `http://<this-host-ip>/` on the LAN). A backup dump is optional: an empty database is initialized automatically.

### First Control Center admin

Migrations do **not** insert a usable administrator. On a fresh install, set both:

- `SWARM_BOOTSTRAP_ADMIN_EMAIL`
- `SWARM_BOOTSTRAP_ADMIN_PASSWORD` (12+ characters)

The backend creates that `CONTROL_CENTER_ADMIN` once, the first time no Control Center admin exists. Restarts do not change existing users or passwords. If a Control Center admin is already present, the environment variables are ignored.

### Demo accounts (opt-in)

Production stays empty of demo logins. To seed the abc / Rebuild Demo Co `SUPER_ADMIN` accounts:

```bash
SWARM_SEED_DEMO_ACCOUNTS=true
SWARM_SEED_DEMO_PASSWORD=choose-a-12-plus-char-password
```

Demo seeding never creates a Control Center admin and never overwrites an existing email.

See [DOCKER.md](./DOCKER.md).

## Manual backend (optional)

Java 17, Maven 3.8+, Node.js 18+, and a reachable PostgreSQL 16 + TimescaleDB. Point `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`, and `JWT_SECRET` at that database. Flyway applies `backend/src/main/resources/db/migration`. Do not recreate `tenants`, `devices`, `sensor_messages`, or `sensor_readings`.

The Compose database is internal. Host `192.168.29.229:5433` is the previous live instance and should be retired after soak — see [DOCKER.md](./DOCKER.md).

## Environment variables

Use placeholders only. Real values belong in `.env`.

| Variable | Description |
|----------|-------------|
| `DB_HOST` / `DB_PORT` / `DB_NAME` / `DB_USER` / `DB_PASSWORD` | PostgreSQL connection |
| `JWT_SECRET` | JWT signing key (required in production) |
| `SWARM_BOOTSTRAP_ADMIN_EMAIL` / `SWARM_BOOTSTRAP_ADMIN_PASSWORD` | First Control Center admin (12+ character password). Used only when none exists. |
| `SWARM_SEED_DEMO_ACCOUNTS` / `SWARM_SEED_DEMO_PASSWORD` | Opt-in demo `SUPER_ADMIN` logins; default off |
| `MQTT_ENABLED` | Optional MQTT ingest; default `false` |
| `CORS_ORIGINS` | Extra browser origins allowed to call the API |
| `SWARM_CORS_ALLOW_ANY_HTTP` | Default `true` so LAN IPs such as `http://192.168.x.x` can sign in. Set `false` to allow only `CORS_ORIGINS`. |
| `VITE_API_URL` | Frontend Docker build arg. Compose sets `/api` so the SPA uses nginx on the same origin. |
| `SERVER_PORT` | API port (default 8080) |

## HTTPS

Place nginx or another reverse proxy with TLS in front of the frontend (port 80) and API (port 8080). Do not publish PostgreSQL.

## API routing

- Backend servlet context is `/api`. Health: `GET /api/health`. The backend root `/` is expected to be 404.
- Docker frontend nginx proxies `/api/` to `http://backend:8080/api/` and serves the SPA for every other path.
- Compose builds the UI with `VITE_API_URL=/api` so the browser calls the same host it loaded.

## Authentication troubleshooting

| Symptom | Cause | What to do |
|---------|--------|------------|
| Login returns **403** `Invalid CORS request` | Browser `Origin` (LAN IP or hostname) was not allowed | Default `SWARM_CORS_ALLOW_ANY_HTTP=true` allows `http://` / `https://` origins. If you locked CORS down, add the exact origin to `CORS_ORIGINS` (include the scheme; port if not 80/443). |
| Login form says the backend is down, but `/api/health` is 200 | The UI used to treat any non-401 as a dead backend | Current UI shows the API message, CORS guidance, or “API is reachable but reported a server error”. |
| Login 401 `Invalid email or password` | Unknown email, wrong password, or disabled account | Use the bootstrap admin email. Passwords are bcrypt; they are not reset on restart. |
| No admin after a fresh install | V2 no longer inserts an administrator | Set bootstrap env vars **before** the first backend start. If a Control Center admin already exists, env vars are not applied. |
| Flyway checksum mismatch after upgrade | Applied V1/V2 bytes changed earlier | Backend aligns **only** the known legacy checksums, then validates. Existing rows are not replayed. |

## Troubleshooting

| Issue | Solution |
|-----------------|
| Backend unhealthy | `docker compose logs backend` — wait for `postgres` healthy and `postgres-bootstrap` completed |
| CORS 403 on login | See Authentication troubleshooting. Do not disable authentication to work around this. |
| MQTT connection failed | Expected when `MQTT_ENABLED=false`. Do not publish host 1883 (used by other projects) |
| Need a clean database | `docker compose down` then `docker volume rm swarm_webapp_swarm_pg_data` — this is destructive |
