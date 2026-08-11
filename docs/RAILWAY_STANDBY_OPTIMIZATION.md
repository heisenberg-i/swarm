# Railway standby optimization (1.3 GB idle → target &lt;800 MB)

**Problem:** Railway project idles at ~**1.31 GB RAM** (MySQL + backend + frontend) while Pi serves all traffic.

**Target:** **&lt;800 MB** idle with Light standby, or **&lt;$5/mo** with Cold standby.

---

## Why 1.31 GB today

| Service | Typical idle RAM | Notes |
|---------|------------------|-------|
| **MySQL** | ~500–700 MB | Largest piece; Pi sync keeps it warm |
| **backend** | ~350–450 MB | JVM + Spring + DB pool + schedulers |
| **swarm_webapp** | ~50–150 MB | nginx; useless while Pi serves app |
| **Total** | **~1.0–1.3 GB** | Matches your dashboard |

emPOWER uses less because it is not running a full duplicate MySQL + Spring stack 24/7 on Railway.

---

## Immediate dashboard actions (today, no code deploy)

Do these in order — **biggest impact first**.

### 1. Stop `swarm_webapp` on Railway (saves ~100–150 MB)

Pi serves `app.swarm.co.in`. The Railway frontend is redundant while Pi is healthy.

1. Railway → **swarm_webapp** → **Settings** → **Danger** → **Remove service** or **Pause** (if available)
2. **Or** stop deployments and remove custom domain `app.swarm.co.in` from this service (domain is on Pi now)

**Failover trade-off:** If Pi fails, API works on Railway but **web UI may not** until you restart frontend.

### 2. Enable Serverless on `backend` only

Settings → Deploy → **Enable Serverless** → Deploy

Add variables:

```env
RAILWAY_TELEMETRY_DISABLED=true
SPRING_PROFILES_ACTIVE=prod,standby
SWARM_DEPLOYMENT_ROLE=standby
JAVA_TOOL_OPTIONS=-XX:+UseContainerSupport -Xms64m -Xmx256m -XX:+UseSerialGC -XX:MaxMetaspaceSize=128m
NOVA_SPACE_OP_ENABLED=false
NOVA_THINK_ENABLED=false
NOVA_NARRATE_ENABLED=false
MQTT_ENABLED=false
```

### 3. Daily sync + MySQL sleep (recommended)

Pi runs `scripts/daily-railway-sync.sh` once per day: wake MySQL → restore dump → **stop** MySQL + backend. Railway idle RAM drops to **~0 MB** between syncs.

```cron
0 2 * * * cd /home/swarm/swarm_webapp && ./scripts/daily-railway-sync.sh >> /var/log/swarm-sync.log 2>&1
0 3 * * 0 cd /home/swarm/swarm_webapp && ./scripts/sync-to-railway.sh --dump-only >> /var/log/swarm-sync.log 2>&1
```

Requires `RAILWAY_TOKEN` and service IDs in `.env.rpi` — see [RPi_PRIMARY_RUNBOOK.md](RPi_PRIMARY_RUNBOOK.md) §6.

**Failover:** Manual — start MySQL then backend in Railway dashboard (~15–30 min). See [RAILWAY_MANUAL_FAILOVER.md](RAILWAY_MANUAL_FAILOVER.md).

### 4. Hard usage limit

Workspace → Usage → **Hard limit $8–10**

---

## Code deploy (after `git push swarm main`)

The repo now includes **standby optimizations**:

| Change | Effect |
|--------|--------|
| `application-standby.yml` | Lazy init, tiny DB pool, Nova off, `ddl-auto: validate` |
| Scheduling disabled on standby | No 3s/5min background DB polling |
| Migrations skipped on standby | No startup ALTER on Railway |
| `railway.toml` memory caps | backend 384 MB, frontend 128 MB |

**Railway backend must use:**

```env
SPRING_PROFILES_ACTIVE=prod,standby
```

Redeploy backend after push.

---

## Expected RAM after optimization

| Setup | Idle RAM | Monthly cost (approx) |
|-------|----------|------------------------|
| Before (3 services warm) | **~1.31 GB** | $12–20 |
| Light + stop frontend | **~600–800 MB** | $6–10 |
| Cold standby (all stopped) | **~0 MB** | $0–5 |

---

## Daily MySQL sleep (Pi primary)

When daily sync is configured, Railway MySQL and backend are **stopped** after each nightly sync. The MySQL **volume retains** the last dump until the next run.

| Phase | Railway state |
|-------|---------------|
| 23h/day | MySQL + backend **stopped** (~0 MB RAM) |
| ~02:00 IST | Pi wakes MySQL → restores dump → stops MySQL + backend |

Scripts:

- `scripts/daily-railway-sync.sh` — daily wake/sync/sleep cycle
- `scripts/railway-api.sh` — GraphQL `serviceInstanceRedeploy` / `deploymentStop`
- `scripts/setup-daily-railway-cron.sh` — install Pi cron

**Trade-offs vs warm standby:**

| | Warm (6h sync) | Daily sleep |
|---|----------------|-------------|
| Idle RAM | ~600–800 MB | **~0 MB** |
| Monthly cost | $6–10 | **$0–5** |
| Failover | ~30s auto | **Manual ~15–30 min** |
| Data gap | up to 6h | up to **24h** |

---

## Cold standby (if still too expensive)

Stop **all** Railway services. Pi keeps local backups:

```bash
./scripts/sync-to-railway.sh --dump-only
```

Manual recovery when Pi fails (~30–60 min). No automatic failover.

---

## Verify

```bash
curl -s https://backend-production-a841.up.railway.app/api/health
# deploymentRole: standby

curl -s https://api.swarm.co.in/api/health
# deploymentRole: primary
```

Check Railway Usage after 24h — RAM line should drop below 1 GB.

---

## Related

- [RAILWAY_MANUAL_FAILOVER.md](RAILWAY_MANUAL_FAILOVER.md)
- [RAILWAY_HOSTING.md](RAILWAY_HOSTING.md)
- [RPi_PRIMARY_RUNBOOK.md](RPi_PRIMARY_RUNBOOK.md)
