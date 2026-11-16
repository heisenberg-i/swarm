# PURPOSE: Operator/developer documentation for SWARM.
# Railway Manual Failover (Pi down)

Use when the Raspberry Pi is unhealthy and you need Railway to serve `api.swarm.co.in`.

**Prerequisites:** Daily sync has run at least once (`scripts/daily-railway-sync.sh`) so Railway MySQL volume has a recent snapshot.

**Data staleness:** Up to **24 hours** behind Pi (last daily sync).

---

## 1. Confirm Pi is down

```bash
curl -s https://api.swarm.co.in/api/health
# expect: connection error or timeout
```

---

## 2. Start Railway MySQL

1. Open [SWARM_SaaS](https://railway.com/project/b950f324-280f-4cce-aee6-147f0dfd08e5)
2. Click **MySQL** service
3. **Deployments** → **Deploy** / **Redeploy** latest (or Restart)
4. Wait **2–3 minutes** for TCP proxy to accept connections

Optional check from any machine (use credentials from Railway MySQL → Connect):

```bash
mysqladmin ping -h <RAILWAY_MYSQL_HOST> -P <PORT> -u root -p
```

---

## 3. Start Railway backend

1. Click **backend** service
2. **Deployments** → **Deploy** / **Redeploy** latest
3. Wait **1–2 minutes** for JVM startup

Verify:

```bash
curl -s https://backend-production-a841.up.railway.app/api/health
# expect: {"status":"UP",...,"deploymentRole":"standby"}
```

---

## 4. Confirm public API routes to Railway

```bash
curl -s https://api.swarm.co.in/api/health
# expect: deploymentRole standby (may take ~30s after Cloudflare detects healthy standby)
```

Login at https://app.swarm.co.in — if Pi frontend is down, start **swarm_webapp** on Railway optionally.

---

## 5. ESP / devices

Devices using `https://api.swarm.co.in/api` resume posting once step 4 returns healthy. No re-pairing required.

---

## 6. When Pi recovers (failback)

On the Pi:

```bash
cd ~/swarm_webapp
docker compose -f docker-compose.rpi.yml --env-file .env.rpi up -d
curl -s http://localhost:8080/api/health   # primary
./scripts/daily-railway-sync.sh            # refresh Railway + sleep again
```

Confirm public API:

```bash
curl -s https://api.swarm.co.in/api/health
# expect: deploymentRole primary
```

Railway MySQL and backend will be stopped again by the daily sync script.

---

## Failover drill checklist

| Step | Action | Pass |
|------|--------|------|
| 1 | Note current `api.swarm.co.in` health = primary | ☐ |
| 2 | On Pi: `docker compose -f docker-compose.rpi.yml down` | ☐ |
| 3 | Start Railway MySQL, wait for TCP | ☐ |
| 4 | Start Railway backend | ☐ |
| 5 | `curl` standby health on Railway URL | ☐ |
| 6 | `curl` `api.swarm.co.in` → standby (after ~30s) | ☐ |
| 7 | Login / smoke test dashboard | ☐ |
| 8 | Restart Pi stack + run `daily-railway-sync.sh` | ☐ |
| 9 | `api.swarm.co.in` → primary again | ☐ |

---

## Related

- [RPi_PRIMARY_RUNBOOK.md](RPi_PRIMARY_RUNBOOK.md)
- [RAILWAY_STANDBY_OPTIMIZATION.md](RAILWAY_STANDBY_OPTIMIZATION.md)
- [CLOUDFLARE_FAILOVER.md](CLOUDFLARE_FAILOVER.md)
