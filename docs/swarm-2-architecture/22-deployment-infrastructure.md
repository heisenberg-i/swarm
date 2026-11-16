# PURPOSE: Operator/developer documentation for SWARM.
# 22 — Deployment and infrastructure

## Exists today

| Piece | Role |
|---|---|
| `docker-compose.rpi.yml` | Pi primary: mysql, backend, frontend |
| Cloudflare Tunnel | `api.swarm.co.in`, `app.swarm.co.in` |
| Railway | MySQL + standby API, Nova off |
| `docker-compose.yml` | Dev names `biopower-*`, Mosquitto unused |
| Vite 8081 vs Spring 8080 | Local proxy mismatch (Python/socat) |

MQTT is not in the Pi compose. That is correct until a plant needs it.

## Target topology (same)

```
[ESP on plant LAN] --HTTPS--> api.swarm.co.in --Tunnel--> Pi nginx -- Spring -- MySQL
[Browsers / APK] ----------> app.swarm.co.in --Tunnel--> Pi nginx (SPA + /api proxy)
[emPOWER] -----------------> api.swarm.co.in /partner/v1
[Railway standby] <--- replica/sync --- Pi MySQL
```

Failover: Cloudflare health on `/api/health` (docs/CLOUDFLARE_FAILOVER.md). Standby must not enable sandbox keys or HMI auto-sequence.

## Secure deployment

- No default passwords in images.
- `JWT_SECRET`, DB, SaaS secret, Partner sandbox disabled, LLM keys via env.
- nginx TLS at edge (Cloudflare). Pi may speak HTTP to tunnel origin — lock tunnel.
- `ddl-auto: validate` in prod after Flyway.
- `SWARM_DEPLOYMENT_ROLE=primary|standby` gates jobs (extend current Nova gate).

## Backup

- Existing sync scripts to Railway: keep, document RPO/RTO honestly (hours, not seconds).
- Nightly `mysqldump` to `SYNC_BACKUP_DIR` with rotation.
- Report files included in backup.
- Restore drill: quarterly, on a copy — not in Cursor autonomy.

## Observability

- Pi: docker logs + `/health/ready`.
- Alert nanoFarm if ingest silent company-wide 30 min (CC system health).

## Local development

- Recommend `docker-compose.local.yml` + documented 8080/8081.
- H2 `dev` profile for UI work without MySQL.
- Identity local.

## What Cursor must not do

- Deploy to Pi/Railway.
- Change live Cloudflare/Railway tokens.
- Enable `MQTT_ENABLED` on production without a plant request.
- Point `SWARM_PUBLIC_API_URL` at a personal tunnel.

## Disaster recovery

1. Pi down: Cloudflare → Railway API (read/write if promoted — **manual**, see RAILWAY_MANUAL_FAILOVER).
2. ESP continues posting if DNS still hits a live API.
3. Pairing (LAN) still needs a technician on site regardless of cloud.
