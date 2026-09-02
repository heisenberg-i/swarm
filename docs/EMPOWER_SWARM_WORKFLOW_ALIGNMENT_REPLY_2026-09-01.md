# Swarm reply — emPOWER ↔ Swarm workflow alignment

**Audience:** emPOWER Manufacturing Hub / integrations + Swarm Cursor team  
**From:** Swarm Cursor team (docs-only; no product change in this note)  
**Date:** 2026-09-01  
**Status:** Reply to coordinator handoff (paste cut off at §3.1). Grounded in this repo (`docs/PARTNER_API.md`, Partner `/partner/v1` controller, Control Center + `companies` vs `partner_organizations`).  
**Related:** [PARTNER_API.md](PARTNER_API.md), [EMPOWER_HANDOFF.md](EMPOWER_HANDOFF.md), [CONTROL_CENTER.md](CONTROL_CENTER.md), [EMPOWER_SHARED_LOGIN_CONNECTIVITY.md](EMPOWER_SHARED_LOGIN_CONNECTIVITY.md)

Do **not** copy demo/local passwords from the workflow PDF into tickets, Slack, or git.

---

## 0. Swarm agrees with coordinator verdict

| Question | Swarm confirmation |
|----------|-------------------|
| Does emPOWER have Swarm **product** pages beyond Settings API connection? | **Agree.** Swarm UI stays on `app.swarm.co.in` / `control.swarm.co.in`. ERP `/manufacturing/*` is not Swarm. |
| Is Swarm gated by emPOWER Control Centre? | **Agree — not directly.** `manufacturing.hub` on SaaS may unlock MH + vault. **Swarm Control Center** (`control.swarm.co.in`) is nanoFarm onboarding only. Different product from `control.empowerapp.in`. |
| Enough emPOWER module pages for **all** Swarm data? | **Agree — by design.** Live sensors, HMI, alerts, AI health, RUL, ESP pairing, MQTT stay in Swarm. Partner API is **read-only pull** of telemetry (+ optional alerts/health). |

**North star (unchanged):** two standalone apps; Swarm owns IoT SoT; emPOWER owns money/BOM/WO SoT; Partner Bearer keys (server→server); no Swarm UI embed in ERP.

---

## 1. One Swarm correction to emPOWER §2.1

| emPOWER handoff said | Swarm ground truth |
|----------------------|-------------------|
| Consume Swarm `/aggregates/daily` — **Planned** / not called yet | **Swarm already ships it.** `GET /api/partner/v1/aggregates/daily?from=&to=` (required dates). See `PartnerTelemetryController` + [PARTNER_API.md](PARTNER_API.md). emPOWER cron can switch from local `MeterDailyAggregate` rollup to this when ready. |
| Live OpenAPI against real Swarm Partner — **Blocked on Swarm** | **Contract is live in repo.** Production: `https://api.swarm.co.in/api/partner/v1`. Staging keys are an **ops handoff** (Super Admin issues `swk_…`), not a missing endpoint. Fixture on emPOWER is fine until vault has a real key. |
| Webhooks / OAuth / shared login — not wired | **Webhooks:** still deferred (agreed). **Shared human login:** Swarm SSO **consumer is implemented** (`IDENTITY_MODE=saas`, `accounts.empowerapp.in`); awaiting SaaS staging `SAAS_CLIENT_SECRET`. **Orthogonal** to Partner keys. |

---

## 3. Gaps & mismatches (completes coordinator §3)

### 3.1 Naming collisions (critical)

| Name | Swarm PDF / product meaning | emPOWER meaning | Rule |
|------|-----------------------------|-----------------|------|
| **Control Center / Centre** | `control.swarm.co.in` — `CONTROL_CENTER_ADMIN` onboards **companies + plants** | `control.empowerapp.in` — SaaS commercial / module grants | Never say “Control Center” without the host. Swarm spelling: **Center**. |
| **Company** | Swarm tenant (`companies` table). Created in Swarm CC. Owns plants and Super Admin. | ERP/SaaS **Organization** | Do not treat Swarm Company id as emPOWER org UUID. |
| **Partner organization** | Separate table `partner_organizations`. `externalOrgId` **= emPOWER org UUID**. Allow-list of plant ids for the API key. | Vault “org/site IDs” on `/settings/integrations/swarm` | **Join key** for Partner API. Not the same row as Swarm Company. |
| **Plant** | IoT site (`plants`). Assigned to a Swarm Company in CC. | **Hub** on Manufacturing Hub (`/manufacturing`) — ERP plant/ops | Map hub ↔ Swarm plant id on emPOWER (`MeterDeviceMap` / hub mapping). Swarm does not store emPOWER hub ids. |
| **Super Admin** | Company tenant admin on `app.swarm.co.in`. Issues Partner keys at **emPOWER Partners** (`/partners`). | SaaS / ERP Super Admin | Different people unless coincidentally the same human. |
| **Control Center Admin** | nanoFarm operator only. **Does not** issue Partner keys; **does not** see plant dashboard. | — | Do not ask CC Admin to paste keys into ERP. |
| **NOVA** | Swarm **NOVA OP** — plant-side chat over Swarm data | emPOWER NOVA `mfg_*` skills over **local MH aggregates** | Two products. ERP NOVA must not query Swarm DB. |
| **Dashboard** | Live IoT health / sensors | MH hub home: WO / batches | Different URLs and SoT. |
| **Settings** | Swarm plant Settings (thresholds, MQTT, SMTP) **and** Partners | ERP `/settings/integrations/swarm` (vault) | Operator confusion: “Settings” is not one page. |
| **API key** | Swarm-issued `swk_…` Bearer for `/partner/v1` | Stored in `OrgIntegrationCredential` (AES-GCM) | Same secret; Swarm is issuer, emPOWER is vault. |
| **User JWT** | Human operators on Swarm UI | emPOWER session / hub ACL | Never send operator JWT to Partner API. |

### 3.2 Two Swarm “org” objects (easy to merge by mistake)

```text
Swarm Control Center
  Company (tenant)
    └── Super Admin + Plant Admins + Operators
    └── Plants (IoT sites)

Swarm Partner API (emPOWER)
  PartnerOrganization
    └── externalOrgId  ==  emPOWER Organization UUID
    └── allowed Plants  ⊆  some Swarm plants (usually that company’s)
    └── API keys (Bearer)
```

Onboarding join:

1. nanoFarm CC Admin creates **Company** + assigns **Plants**.  
2. Company **Super Admin** (plant app) creates **Partner organization** with `externalOrgId` = emPOWER org UUID, selects plant allow-list, issues key.  
3. emPOWER admin pastes base URL + key + org/site ids into `/settings/integrations/swarm`.  
4. emPOWER maps hub ↔ device ↔ `metricType`.  
5. Cron pulls telemetry (prefer `/aggregates/daily` for contribution).

### 3.3 What Partner API covers vs what stays in Swarm UI

| Swarm workflow object | On Partner API? | emPOWER should |
|-----------------------|-----------------|----------------|
| Company | No | Ignore for MH; use Partner org + plants |
| Plant | `GET /plants` | Map to hub |
| Sensor node / device | `GET /devices` (`device_id` = `node_{id}`) | Map to meter / `metricType` |
| Reading | `/telemetry/latest`, `/telemetry/history` | Optional raw; quality `MEASURED` |
| Daily agg | **`GET /aggregates/daily`** | **Preferred** for contribution |
| Alert | `GET /telemetry/alerts` | Optional; do not build Swarm alert UX in ERP |
| Plant health score | `GET /telemetry/health/{plantId}` | Optional card; SoT remains Swarm |
| HMI state / bus / blocks | **No** | Stay in Swarm `/plant-hmi` |
| AI recommendation / RUL | **No** | Stay in Swarm |
| ESP pairing / MQTT ingest | **No** | Stay in Swarm; ingest is `POST /api/iot/data` (not Partner) |
| Reports PDF/Excel | **No** | Stay in Swarm |
| Human users / RBAC | **No** | Shared login = SaaS track, not Partner |

### 3.4 Auth layers (keep orthogonal)

| Layer | Swarm | emPOWER |
|-------|-------|---------|
| Human UI | Local JWT (`IDENTITY_MODE=local`) or SaaS SSO consumer (`saas`) | Own session + hub ACL |
| Partner telemetry | `ApiKeyAuthenticationFilter` on `/partner/v1/**` | Vault Bearer; client-api proxy so keys never on device |
| Commercial gate | None on Swarm Partner path | `manufacturing.hub` module |
| Shared login DB | Swarm does **not** attach to SaaS Postgres; OAuth token exchange only | SaaS owns identity schema |

### 3.5 Host / port nits

| Surface | Production | Local note |
|---------|------------|------------|
| Partner API | `https://api.swarm.co.in/api/partner/v1` | Compose override often publishes API on **8081**; [PARTNER_API.md](PARTNER_API.md) examples still say **8080**. Vite proxies `/api` → 8081. |
| Plant UI | `https://app.swarm.co.in` | `http://localhost:3000/dashboard` |
| Swarm CC | `https://control.swarm.co.in` | Same origin locally: `/companies` when role is `CONTROL_CENTER_ADMIN` |

### 3.6 Workflow PDF vs Partner contract

The workflow PDF is an **operator** document (CC + plant app + IoT ingest). It correctly mentions **emPOWER Partners** as Super Admin settings. It does **not** define the Partner envelope (`device_id`, `metric_type`, `quality`, daily aggs). Use [PARTNER_API.md](PARTNER_API.md) as integration SoT, not the PDF.

---

## 4. Object mapping (PDF vocabulary → Partner fields)

| PDF object | Partner field / path |
|------------|----------------------|
| Plant | `GET /plants` — numeric plant id in path/query |
| Sensor node | `device_id` = `node_{nodeId}` |
| Reading type/value | `metric_type`, `unit`, `reading_type` (`GAUGE` \| `TOTALIZER`), UTC timestamps |
| Quality | `MEASURED` on raw; aggregates may be `INCOMPLETE` (do not zero-fill) |
| emPOWER org | `externalOrgId` on partner org; `GET /health` returns `organizationId` matching that |
| Partner API key | `Authorization: Bearer swk_…` |

---

## 5. What Swarm will / will not build (locked)

**Will (already or ops):**

- Keep `/partner/v1` read-only pull: health, plants, devices, latest, history, alerts, plant health, **daily aggregates**.  
- Super Admin UI `/partners` to create org + plant allow-list + issue/revoke keys.  
- Handoff package: [EMPOWER_HANDOFF.md](EMPOWER_HANDOFF.md).  
- SSO consumer when SaaS issues `swarm_webapp` client secret.

**Will not:**

- Embed Swarm dashboard/HMI in ERP.  
- Let ERP browser or NOVA call Swarm with a user JWT.  
- Push webhooks for P2.  
- Mirror BOM/WO/₹ into Swarm.  
- Put Partner key issuance on Swarm Control Center.

---

## 6. Suggested next actions (no code in this note)

| Owner | Action |
|-------|--------|
| Swarm Super Admin | For a real tenant: Partners → org with emPOWER UUID → plant allow-list → key → secure handoff (not Slack). |
| emPOWER MH | Point vault at `https://api.swarm.co.in/api/partner/v1`; ping `/health`; map hub↔device↔metric. |
| emPOWER MH | Call **`/aggregates/daily`** when contribution should use Swarm SoT instead of only locally rolled aggs. |
| emPOWER SaaS | Staging OAuth client for `swarm_webapp` if shared human login is the next track. |
| Both | In tickets, write **Swarm Control Center** vs **emPOWER Control Centre**; **Company** vs **Organization** vs **Partner organization**. |

---

## 7. Coordinator paste — sections not in the inbound note

Inbound message ended at the empty §3.1 table. Swarm filled §3–7 from this repo. If emPOWER has further §3.x (conflict matrix, open questions), send the rest; we will not assume unstated gaps.
