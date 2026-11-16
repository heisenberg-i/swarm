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

Open http://localhost. A backup dump is optional: an empty database is initialized automatically. The first Control Center admin is created from `SWARM_BOOTSTRAP_ADMIN_EMAIL` and `SWARM_BOOTSTRAP_ADMIN_PASSWORD` when none exists. See [DOCKER.md](./DOCKER.md).

## Manual backend (optional)

Java 17, Maven 3.8+, Node.js 18+, and a reachable PostgreSQL 16 + TimescaleDB. Point `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`, and `JWT_SECRET` at that database. Flyway applies `backend/src/main/resources/db/migration`. Do not recreate `tenants`, `devices`, `sensor_messages`, or `sensor_readings`.

The Compose database is internal. Host `192.168.29.229:5433` is the previous live instance and should be retired after soak — see [DOCKER.md](./DOCKER.md).

## Environment variables

Use placeholders only. Real values belong in `.env`.

| Variable | Description |
|----------|-------------|
| `DB_HOST` / `DB_PORT` / `DB_NAME` / `DB_USER` / `DB_PASSWORD` | PostgreSQL connection |
| `JWT_SECRET` | JWT signing key (required in production) |
| `SWARM_BOOTSTRAP_ADMIN_EMAIL` / `SWARM_BOOTSTRAP_ADMIN_PASSWORD` | First Control Center admin (12+ character password) |
| `MQTT_ENABLED` | Optional MQTT ingest; default `false` |
| `CORS_ORIGINS` | Browser origins allowed to call the API |
| `SERVER_PORT` | API port (default 8080) |

## HTTPS

Place nginx or another reverse proxy with TLS in front of the frontend (port 80) and API (port 8080). Do not publish PostgreSQL.

## Troubleshooting

| Issue | Solution |
|-----------------|
| Backend unhealthy | `docker compose logs backend` — wait for `postgres` healthy and `postgres-bootstrap` completed |
| CORS errors | Add the frontend origin to `CORS_ORIGINS` |
| MQTT connection failed | Expected when `MQTT_ENABLED=false`. Do not publish host 1883 (used by other projects) |
| Need a clean database | `docker compose down` then `docker volume rm swarm_webapp_swarm_pg_data` — this is destructive |
