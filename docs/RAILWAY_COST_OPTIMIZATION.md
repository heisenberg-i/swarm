# Railway Light Standby — Cost Optimization

**When to use:** Raspberry Pi is primary (`SWARM_DEPLOYMENT_ROLE=primary`). Railway is warm backup but should cost less than full 24/7 traffic serving.

**Goal:** Cut Railway spend ~40–50% while keeping automatic failover (with ~1–2 min cold-start delay on first request after idle).

**Architecture unchanged:**

```text
Pi healthy   →  Cloudflare routes users to Pi
Pi down      →  Cloudflare fails over to Railway (services wake from Serverless sleep)
```

---

## Part 1 — Railway dashboard (manual, ~15 min)

Project: **SWARM_SaaS** — [https://railway.com/project/b950f324-280f-4cce-aee6-147f0dfd08e5](https://railway.com/project/b950f324-280f-4cce-aee6-147f0dfd08e5)

### 1.1 Enable Serverless on **backend** only

1. Open **backend** → **Settings** → **Deploy** → **Enable Serverless** → **Deploy**
2. **Stop or remove `swarm_webapp`** on Railway (Pi serves the app — saves ~150 MB)

**backend** variables — use [`.env.railway.standby.example`](../.env.railway.standby.example) or section 1.5 below.

**Do not enable Serverless on MySQL** — Pi sync needs it reachable. MySQL is the main remaining baseline cost.

### 1.2 Reduce memory limits


| Service          | Recommended limit                  |
| ---------------- | ---------------------------------- |
| **backend**      | 384 MB (`backend/railway.toml`) |
| **swarm_webapp** | **stopped** (Pi serves app)     |
| **MySQL**        | 512 MB                          |


Settings → **Resources** (or service resource slider) → save → redeploy if prompted.

### 1.3 Disable auto-deploy (optional)

For each service → **Settings** → **Deploy** → disable **Auto Deploy** from GitHub.

Primary deploys go to Pi via `./scripts/deploy-rpi.sh`. Railway only needs redeploy when standby env vars change.

### 1.4 Set usage hard limit

Workspace → **Usage** → **Set usage limits**:


| Setting     | Value         |
| ----------- | ------------- |
| Email alert | $8 (optional) |
| Hard limit  | **$10–12**    |


If the limit is hit, Railway stops workloads until the next cycle or you raise the limit.

### 1.5 Confirm standby env vars (no change needed if already set)

**backend:**

```env
SPRING_PROFILES_ACTIVE=prod,standby
SWARM_DEPLOYMENT_ROLE=standby
SWARM_PARTNER_SANDBOX_ENABLED=false
JWT_SECRET=<same as Pi .env.rpi>
SWARM_PUBLIC_API_URL=https://api.swarm.co.in/api
RAILWAY_TELEMETRY_DISABLED=true
JAVA_TOOL_OPTIONS=-XX:+UseContainerSupport -Xms64m -Xmx256m -XX:+UseSerialGC -XX:MaxMetaspaceSize=128m
NOVA_SPACE_OP_ENABLED=false
NOVA_THINK_ENABLED=false
NOVA_NARRATE_ENABLED=false
```

**Stop `swarm_webapp` on Railway** — Pi serves the app (saves ~150 MB). See [RAILWAY_STANDBY_OPTIMIZATION.md](RAILWAY_STANDBY_OPTIMIZATION.md).

---



## Part 2 — Raspberry Pi (swarm_pi)



### 2.1 Change sync from hourly to every 6 hours

```bash
crontab -e
```

Replace hourly sync with (every 6 hours):

```cron
0 */6 * * * cd /home/swarm/swarm_webapp && ./scripts/sync-to-railway.sh >> /var/log/swarm-sync.log 2>&1
```

**Trade-off:** On failover, Railway DB may be up to **4 hours** behind Pi.

### 2.2 Optional — extra local-only backup (weekly)

```cron
0 3 * * 0 cd /home/swarm/swarm_webapp && ./scripts/sync-to-railway.sh --dump-only >> /var/log/swarm-sync.log 2>&1
```

Keeps a gzip dump on Pi SSD (`SYNC_BACKUP_DIR`) without touching Railway.

### 2.3 Test sync

```bash
cd ~/swarm_webapp
./scripts/sync-to-railway.sh --dry-run
./scripts/sync-to-railway.sh
```

---



## Part 3 — Verify



### Railway standby health

```bash
curl -s https://backend-production-a841.up.railway.app/api/health
# expect: "deploymentRole":"standby"
```



### Pi still primary

```bash
curl -s https://api.swarm.co.in/api/health
# expect: "deploymentRole":"primary"

curl -sI https://app.swarm.co.in | grep -i railway
# expect: no x-railway-edge when Pi is healthy
```



### After 24–48 hours

Check Railway **Usage** tab — RAM line should be lower; egress should stay flat or grow slowly (4h sync only).

---



## Part 4 — What to expect on failover


| Event                                   | Behavior                                        |
| --------------------------------------- | ----------------------------------------------- |
| Pi stops                                | Cloudflare routes to Railway (~30s)             |
| First request to slept backend/frontend | Cold start **10–60s**, possible **502** — retry |
| Users                                   | May need to refresh once after Pi outage        |
| Data                                    | Up to 4h stale vs Pi                            |


After Pi recovers:

```bash
cd ~/swarm_webapp
docker compose -f docker-compose.rpi.yml --env-file .env.rpi up -d
./scripts/sync-to-railway.sh
```

---



## Cost comparison (approximate)


| Mode                                | Monthly   |
| ----------------------------------- | --------- |
| Full warm standby (before)          | $10–20    |
| **Light standby (this guide)**      | **$6–15** |
| Cold standby (all services stopped) | $0–5      |


---



## Rollback to full warm standby

1. Disable Serverless on backend + swarm_webapp
2. Restore memory limits (512 MB backend, 256 MB frontend)
3. Change Pi cron back to hourly sync

---



## Related

- [RAILWAY_HOSTING.md](RAILWAY_HOSTING.md) — standby env reference
- [RPi_PRIMARY_RUNBOOK.md](RPi_PRIMARY_RUNBOOK.md) — Pi operations
- [CLOUDFLARE_FAILOVER.md](CLOUDFLARE_FAILOVER.md) — failover pools

