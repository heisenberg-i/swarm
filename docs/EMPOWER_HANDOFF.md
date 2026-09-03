# emPOWER Integration Handoff Package

**From:** SWARM team  
**To:** emPOWER ERP team  
**When:** After SWARM Super Admin creates a partner org + API key  
**SWARM API contract:** [PARTNER_API.md](PARTNER_API.md)

---

## What SWARM provides

SWARM is the **system of record** for sensor readings, devices, plant health, and alerts. emPOWER consumes this data via a **read-only Partner API** using an org-scoped API key issued by SWARM.

emPOWER does **not** get SWARM user accounts, JWT login, or UI embed access.

---

## Credentials SWARM will send you (once)

| Field | Example | Notes |
|-------|---------|-------|
| Base URL | `https://api.swarm.co.in/api/partner/v1` | Production |
| API key | `swk_…` | Store server-side only; never in browser or NOVA client |
| Organization ID | `001` (Vulcan) or the Settings helper id | Must match SWARM `externalOrgId` / `/health.organizationId`. Never `org_vulcan_mozambique`. |
| Allowed plant IDs | `[1]` | SWARM-side allow-list; key cannot read other plants |

---

## emPOWER implementation checklist (your repo)

### 1. Org vault (server-only)

Store per emPOWER organization:

```json
{
  "provider": "swarm",
  "baseUrl": "https://api.swarm.co.in/api/partner/v1",
  "apiKey": "<from SWARM>",
  "externalOrgId": "001",
  "plantIds": [1]
}
```

- Encrypt at rest
- Never expose to frontend, mobile, or NOVA browser context

### 2. HTTP client

All requests:

```
Authorization: Bearer <api_key>
Content-Type: application/json
```

Recommended pull order (cron ~15 min):

1. `GET /health` — verify connectivity + org binding
2. `GET /plants` — catalog sync
3. `GET /devices` — `device_id` = `node_{nodeId}`
4. `GET /telemetry/latest` — optional incremental
5. `GET /telemetry/history?updated_since=…` — backfill / delta
6. `GET /aggregates/daily?from=…&to=…` — preferred for costing/contribution
7. `GET /telemetry/alerts` — optional
8. `GET /telemetry/health/{plantId}` — plant health cards

### 3. Local persistence

Normalize into emPOWER tables with quality flags:

| SWARM quality | emPOWER handling |
|---------------|------------------|
| `MEASURED` | Store as measured |
| `INCOMPLETE` | Do not zero-fill gaps |
| Totalizer resets | Mark `RESET_DETECTED` if detected |

Store timestamps in **UTC**. Apply hub timezone only when aggregating for display.

### 4. MeterDeviceMap

Map SWARM `device_id` (`node_5`) to emPOWER manufacturing hub/line/meter:

```
swarm_device_id → hubId, lineId, metricType
```

Unmapped devices are **ignored** (do not surface in NOVA).

### 5. NOVA skills (read-only)

- Read from **local** normalized/agggregated tables (not live SWARM on every chat turn)
- Soft-fail if SWARM data is stale or pull failed
- Use contribution/completeness language — not raw “profit” from sensor data

### 6. Optional thin proxy

`GET /api/integrations/telemetry/*` on emPOWER can mirror SWARM paths for internal tools. Primary MH/NOVA path should still use local aggs after cron pull.

---

## APIs emPOWER must NOT call

- `POST /api/iot/batch` — device ingest (SWARM devices only)
- Connect Device / pairing APIs
- SWARM user admin or mutating config APIs

---

## Verification (emPOWER side)

After vault setup, your cron should log successful pulls. Manual test:

```bash
curl -H "Authorization: Bearer <api_key>" \
  https://api.swarm.co.in/api/partner/v1/health

curl -H "Authorization: Bearer <api_key>" \
  https://api.swarm.co.in/api/partner/v1/plants

curl -H "Authorization: Bearer <api_key>" \
  "https://api.swarm.co.in/api/partner/v1/telemetry/health/1"
```

---

## Partner organization setup (SWARM operator)

1. Log in to `https://app.swarm.co.in` as SUPER_ADMIN
2. Open **emPOWER Partners** (`/partners`)
3. Create organization:
   - Name: customer site name
   - emPOWER org ID: *(get from emPOWER when ready; use placeholder until then)*
   - Plants: select that tenant’s plant(s)
4. Create API key named `emPOWER production`
5. Copy key → secure handoff to emPOWER admin
6. Run verification curls above before closing the ticket

---

## Architecture reference

Full design decisions: `NOVA_DNA_source_for_SWARM/docs/EMPOWER_SWARM_INTEGRATION_ARCHITECTURE.md`

**Truth model:**

| Domain | System of record |
|--------|------------------|
| Sensor readings / devices / plant health | SWARM |
| BOM, batches, stock, labour, invoices | emPOWER |
| Contribution / cost per unit | emPOWER join |

---

## Support contacts

- SWARM API issues: SWARM Super Admin / `docs/PARTNER_API.md`
- Key rotation: revoke in SWARM UI → issue new key → update emPOWER vault
