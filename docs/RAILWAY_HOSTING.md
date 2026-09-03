# SWARM Production Hosting (Railway)

**Last verified:** 2026-07-30

This is the **active production** Railway project. Do not use the old `modest-nurturing` / `rare-passion` project (expired trial).

---

## Railway project

| Item | Value |
|------|-------|
| **Project name** | SWARM_SaaS |
| **Project ID** | `b950f324-280f-4cce-aee6-147f0dfd08e5` |
| **Workspace** | arunbiopower's Projects |
| **Environment** | production |
| **Dashboard** | https://railway.com/project/b950f324-280f-4cce-aee6-147f0dfd08e5 |

### Link CLI to this project

```powershell
railway link -p b950f324-280f-4cce-aee6-147f0dfd08e5 -e production
```

---

## Services (Pi primary — Railway standby)

| Service | Root dir | Public URL | Standby role |
|---------|----------|------------|--------------|
| **MySQL** | (template) | internal: `mysql.railway.internal` | DB replica (Pi sync) — **keep running** |
| **backend** | `backend` | https://backend-production-a841.up.railway.app | Failover API — **Serverless on** |
| **swarm_webapp** | `frontend` | was `app.swarm.co.in` | **Stop/remove** — Pi serves the app |

```
Pi (live)     →  api.swarm.co.in + app.swarm.co.in
Railway       →  MySQL + backend only (idle until Pi fails)
```

---

## Production URLs

| Purpose | URL |
|---------|-----|
| Web app | https://app.swarm.co.in |
| Plant HMI | https://app.swarm.co.in/plant-hmi |
| Backend API | https://backend-production-a841.up.railway.app/api |
| Marketing site | https://swarm.co.in |

---

## Environment variables (reference)

### backend service

```env
DB_HOST=mysql.railway.internal
DB_PORT=3306
DB_NAME=railway
DB_USER=root
DB_PASSWORD=<from Railway MySQL service>
JWT_SECRET=<set in Railway>
MQTT_ENABLED=false
CORS_ORIGINS=https://app.swarm.co.in,https://control.swarm.co.in,https://localhost,capacitor://localhost
IDENTITY_MODE=local
SAAS_ACCOUNTS_URL=https://accounts.empowerapp.in
SAAS_CLIENT_ID=swarm_webapp
SAAS_APP_URL=https://app.swarm.co.in
SWARM_PUBLIC_API_URL=https://api.swarm.co.in/api

# Standby — minimal RAM (see application-standby.yml + docs/RAILWAY_STANDBY_OPTIMIZATION.md)
JAVA_TOOL_OPTIONS=-XX:+UseContainerSupport -Xms64m -Xmx256m -XX:+UseSerialGC -XX:MaxMetaspaceSize=128m
SPRING_PROFILES_ACTIVE=prod,standby
SWARM_DEPLOYMENT_ROLE=standby
SWARM_PARTNER_SANDBOX_ENABLED=false
RAILWAY_TELEMETRY_DISABLED=true
NOVA_SPACE_OP_ENABLED=false
NOVA_THINK_ENABLED=false
NOVA_NARRATE_ENABLED=false
SPRING_JPA_HIBERNATE_DDL_AUTO=validate
SENSOR_RETENTION_DAYS=30
AI_RECOMMENDATION_RETENTION_DAYS=30
AI_RECOMMENDATION_MAX_PER_PLANT=100
NOVA_MESSAGE_RETENTION_DAYS=90
```

Copy-paste reference: [`.env.railway.standby.example`](../.env.railway.standby.example)

### swarm_webapp service — **stop on Railway**

Pi serves `https://app.swarm.co.in`. Remove or pause **swarm_webapp** on Railway to save ~100–150 MB idle RAM.

If you keep it for UI failover only, use Serverless +:

```env
VITE_API_URL=https://api.swarm.co.in/api
```

### Recommended memory limits (Railway standby)

| Service | Limit |
|---------|-------|
| **backend** | 384 MB (see `backend/railway.toml`) |
| **MySQL** | 512 MB |
| **swarm_webapp** | **stopped** (recommended) |

With `JAVA_TOOL_OPTIONS` above, standby JVM heap is capped at **256 MB**.

### Railway backend crashed?

If deploy logs show `Schema-validation` or Hibernate errors after enabling `prod` profile:

1. Set `SPRING_JPA_HIBERNATE_DDL_AUTO=update` on the **backend** service (not only shared vars).
2. Confirm **DB_HOST**, **DB_PASSWORD**, **JWT_SECRET** exist on backend.
3. Restart backend deployment.

Required backend DB variables (from MySQL service):

```env
DB_HOST=mysql.railway.internal
DB_PORT=3306
DB_NAME=railway
DB_USER=root
DB_PASSWORD=<MYSQLPASSWORD from MySQL service>
JWT_SECRET=<same as Pi>
```

---

When RPi is the live primary, Railway runs as **standby** (MySQL + backend **stopped** between daily syncs). See:

- [RAILWAY_STANDBY_OPTIMIZATION.md](RAILWAY_STANDBY_OPTIMIZATION.md) — daily MySQL sleep (~0 MB idle)
- [RAILWAY_MANUAL_FAILOVER.md](RAILWAY_MANUAL_FAILOVER.md) — manual wake when Pi fails
- [RAILWAY_COST_OPTIMIZATION.md](RAILWAY_COST_OPTIMIZATION.md) — light standby (Serverless + sync)
- [RPi_PRIMARY_RUNBOOK.md](RPi_PRIMARY_RUNBOOK.md) — Pi setup, tunnel, daily sync
- [CLOUDFLARE_FAILOVER.md](CLOUDFLARE_FAILOVER.md) — health checks and failover pools
- `docker-compose.rpi.yml` — arm64 primary stack
- `scripts/daily-railway-sync.sh` — daily mysqldump + Railway sleep
- `scripts/sync-to-railway.sh` — on-demand sync or local backup only

---

## Deploy workflow

**Railway auto-deploys from:** `swarmnfi-svg/swarm_webapp` (not `Srinivas0724/swarm_webapp`).

Push to **both** remotes when releasing:

```powershell
cd C:\Users\seena\swarm_webapp
git push origin main    # Srinivas0724 fork
git push swarm main     # Railway production (required)
```

Manual deploy:

```powershell
railway up ./backend --path-as-root -s backend -d -y
railway up ./frontend --path-as-root -s swarm_webapp -d -y
```

---

## Demo logins

| Role | Email | Password |
|------|-------|----------|
| Control Center Admin | swarm.nfi@gmail.com | Swarm@2026 |

---

## APK notes

- APK is built with `VITE_API_URL` from `frontend/.env.production` (copy from `.env.production.example`)
- Build via `docs/ANDROID_APK.md` — do not commit debug APKs to the repo
- CORS must include `capacitor://localhost` and `https://localhost` on **backend**

---

## Old project (deprecated)

| Item | Value |
|------|-------|
| Project | modest-nurturing |
| Backend | rare-passion-production-fc1a.up.railway.app |
| Status | Trial expired — do not use |
