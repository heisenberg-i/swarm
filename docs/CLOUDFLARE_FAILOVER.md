# Cloudflare Failover Setup

**Goal:** 10-second health checks; automatic cutover from Raspberry Pi (primary) to Railway (standby) when RPi is unhealthy.

**Prerequisites:** Cloudflare account with `swarm.co.in` zone; RPi running Cloudflare Tunnel; Railway standby deployed.

---

## 1. Health endpoints

| Pool | URL | Expected |
|------|-----|----------|
| Primary (RPi) | `GET https://api.swarm.co.in/api/health` | HTTP 200 |
| Standby (Railway) | `GET https://backend-production-a841.up.railway.app/api/health` | HTTP 200 |

---

## 2. Create monitors

In **Cloudflare Dashboard → Traffic → Load Balancing → Monitors**:

### Primary monitor

| Setting | Value |
|---------|--------|
| Type | HTTP |
| Path | `/api/health` |
| Port | 443 |
| Interval | **10 seconds** |
| Timeout | 5 seconds |
| Retries | 2 |
| Expected codes | 200 |

### Standby monitor

Same settings, hostname = `backend-production-a841.up.railway.app`.

---

## 3. Create origin pools

### Pool A — Primary (RPi)

| Setting | Value |
|---------|--------|
| Name | `swarm-rpi-primary` |
| Origin | RPi tunnel hostname or `api.swarm.co.in` origin |
| Monitor | Primary monitor |
| Health check region | Closest to your plant |

### Pool B — Standby (Railway)

| Setting | Value |
|---------|--------|
| Name | `swarm-railway-standby` |
| Origin | `backend-production-a841.up.railway.app` |
| Monitor | Standby monitor |

---

## 4. Load balancer

Create load balancer for `api.swarm.co.in`:

| Setting | Value |
|---------|--------|
| Steering | Failover |
| Default pools | Pool A (primary) first, Pool B (standby) second |
| Fallback | Pool B when A unhealthy |

For `app.swarm.co.in` (frontend):

- **Option A:** Also failover (RPi nginx → Railway `app.swarm.co.in`)
- **Option B:** Keep app on Railway until RPi frontend is ready

Add the same origin for Control Center: `control.swarm.co.in` → same frontend container as `app.swarm.co.in`. See [CONTROL_CENTER.md](CONTROL_CENTER.md).

---

## 5. ESP stable hostname

Configure ESP firmware / pairing to use:

```
https://api.swarm.co.in/api
```

Cloudflare LB routes to healthy backend — ESP does not need re-pairing on failover.

---

## 6. JWT and sessions

Set the **same `JWT_SECRET`** on:

- RPi `.env.rpi`
- Railway backend variables

Users stay logged in across failover if DB sync is recent (daily sync).

---

## 7. Failover timeline

```text
RPi healthy     → Traffic to RPi (primary pool)
RPi stops       → Railway health fails (MySQL/backend stopped) — manual wake required
Manual wake     → Start MySQL + backend in Railway dashboard (~15–30 min)
RPi recovers    → Health OK → Traffic back to RPi; run daily-railway-sync.sh
```

While Railway MySQL and backend are **stopped** between daily syncs, Cloudflare cannot route to Railway until you manually start both services. See [RAILWAY_MANUAL_FAILOVER.md](RAILWAY_MANUAL_FAILOVER.md).

After failback, run `./scripts/daily-railway-sync.sh` on RPi to refresh Railway standby and stop services again.

---

## 8. Testing drill

1. Confirm RPi serves `https://api.swarm.co.in/api/health` → 200, `deploymentRole: primary`
2. Stop RPi: `docker compose -f docker-compose.rpi.yml down`
3. Railway dashboard → start **MySQL**, wait ~2–3 min
4. Railway dashboard → start **backend**
5. Confirm `https://api.swarm.co.in/api/health` → `deploymentRole: standby` (may take ~30s)
6. Login and verify dashboard loads (data may be ≤24h stale)
7. Restart RPi; confirm failback; run `./scripts/daily-railway-sync.sh`

---

## Related

- [RAILWAY_MANUAL_FAILOVER.md](RAILWAY_MANUAL_FAILOVER.md)
- [RPi_PRIMARY_RUNBOOK.md](RPi_PRIMARY_RUNBOOK.md)
- [RAILWAY_HOSTING.md](RAILWAY_HOSTING.md)
