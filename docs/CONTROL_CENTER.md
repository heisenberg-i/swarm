# Control Center

Control Center is **Swarm multi-tenant onboarding** for nanoFarm sales/ops. It is not the plant dashboard.

At scale you sell **one Swarm tenant per customer** (company). That tenant gets a Super Admin and one or more plants. The customer then runs IoT, HMI, and users inside **app.swarm.co.in**.

| Site | Who | Purpose |
|------|-----|---------|
| https://control.swarm.co.in | `CONTROL_CENTER_ADMIN` | Provision tenants: company, Super Admin, plant assignment |
| https://app.swarm.co.in | Company Super Admin, Plant Admin, Operator | Operate and monitor plants |

## Onboarding pipeline

1. Create tenant (company).
2. Create that tenant’s Super Admin.
3. Create plants in the unassigned pool (or reuse existing).
4. Assign plants to the tenant.
5. Status becomes **Ready** — Super Admin logs in at the Swarm app and only sees those plants.
6. Super Admin adds Plant Admin / Operator in the app **Users** screen.

Public sign-up waits in **Approvals** until Control Center assigns a tenant. See [CONTROL_CENTER_MODULES.md](CONTROL_CENTER_MODULES.md).

Onboarding states: `NEEDS_ADMIN` → `NEEDS_PLANTS` → `READY`. Disable a tenant to cut off access without deleting history.

The tenant page also lists **sensor nodes** (paired vs not, last reading flag) and **who has access** (plant-wide vs this node). Live values, HMI, and pairing stay in the Swarm app.

## What stays out of Control Center

Live sensors, HMI, alerts, AI, ESP pairing, MQTT, and Partner API keys belong to the **tenant Swarm app**. Control Center does not embed those modules so one operator console cannot mix customer plant data.

## Cloudflare Tunnel

On the Pi, add a public hostname to the **same frontend** as the app:

- `control.swarm.co.in` → `http://localhost:80` (same as `app.swarm.co.in`)

Update `.env.rpi`:

```env
CORS_ORIGINS=https://app.swarm.co.in,https://control.swarm.co.in,https://api.swarm.co.in,capacitor://localhost,https://localhost
```

Restart backend after CORS change.

## Local development

Log in as Control Center Admin on localhost (same origin shows Control Center). Home: `/overview`.
