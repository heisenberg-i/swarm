# PURPOSE: Operator/developer documentation for SWARM.
# 10 — Threat model

Scope: this repository’s deployed shape (Pi + Cloudflare + Railway standby + ESP on plant LAN + emPOWER pull + Capacitor app).

Method: STRIDE. Not a pentest. Not IEC 62443 certification.

## Assets

- Telemetry integrity (gas, pH, temperature) used for operations and ERP costing.
- Ability to **display** plant state; in future, to **request** commands.
- Tenant isolation (Company A must not see Company B).
- Partner API keys, JWT secret, hub secrets, LLM keys.
- Control Center (provisioning).

## Actors

| Actor | Trust |
|---|---|
| Plant operator | Low — phone stolen, curious |
| Super Admin | Medium — insider |
| CC admin | High privilege, small set |
| emPOWER backend | Separate org; key theft |
| Hub on LAN | Physically reachable |
| Internet attacker | Untrusted |
| Malicious firmware clone | Untrusted |

## STRIDE against **current** code

| Threat | Example in this repo | 2.0 mitigation |
|---|---|---|
| Spoofing | Anyone POSTs `/iot/batch` for a guessed nodeId | Device credentials, replay window |
| Spoofing | Stolen 24 h JWT after disable | Token version + refresh revoke |
| Tampering | Injected methane spike → false alert + Partner agg | Device auth + quality + anomaly caps |
| Tampering | Hibernate ddl-auto surprise columns | Flyway validate |
| Repudiation | HMI START with no server audit (UI sessionStorage only) | `hmi_commands` table |
| Repudiation | CC approve with no audit | `audit_events` |
| Information disclosure | Empty Partner allow-list = all plants | Deny by default |
| Information disclosure | `/auth/me` permitAll NPE/leak | Authenticate |
| Information disclosure | Reports `findAll()` | plantId + ACL |
| Information disclosure | Firmware defaults in git | Provisioning-time secrets |
| Denial of service | Unauthenticated ingest flood | Auth + rate limit per IP/device |
| Elevation | Hidden `/settings` only in React | Server already SUPER_ADMIN — keep and test |
| Elevation | CC admin on `/admin/partner` | Matcher fix |
| Elevation | SSO user ACTIVE with no approval | PENDING |
| Confused deputy | Browser ESP proxy to non-LAN IP | Keep IP allowlist; prefer direct LAN from phone |
| Safety | Operator believes simulated STOP is a trip | Simulation banner; no live until commissioning |
| Supply | Documented CC password in README | Remove; bootstrap |

## Trust boundaries

1. Internet ↔ nginx/Cloudflare
2. SPA ↔ `/api`
3. Hub LAN ↔ hub HTTP (cleartext on site — accepted for pairing; production ingest should be HTTPS to `api.swarm.co.in`)
4. emPOWER ↔ `/partner/v1`
5. Railway replica ↔ Pi (sync scripts; protect tokens)
6. Cloud SWARM ↔ **must not** be a trust boundary for machine safety

## Abuse cases

- Replay old batch: timestamp + nonce / ingest_id.
- Partner key in a mobile app: keys are server-only; SPA Partners page tests the key — **stop sending the raw key to the browser after create** (show once, never again; test from backend).
- Android backup extracts localStorage JWT: shorten TTL, Keystore.
- HMI auto-sequence on standby: already primary-only; keep it that way.

## Rate limiting (minimum)

| Endpoint | Limit |
|---|---|
| `/auth/login` | 5 / 15 min / IP+email |
| `/auth/sso/*` | 20 / min / IP |
| `/iot/batch` | 6 / min / device (hub is 30 s) |
| `/partner/v1/*` | 60 / min / key |
| `/nova-space-op/chat` | 20 / min / user |

Return 429 with Retry-After.

## Logging / detection

- Alert on ingest from new source IP for a chip.
- Alert on Partner 401 burst (stolen key guessing).
- Alert on CC login from new country (best-effort).

Do not log secrets.

## Residual risk (accepted)

- Plant LAN hub HTTP can be sniffed on site. Physical LAN trust.
- Cloudflare Tunnel dependency for Pi reachability.
- Simulation HMI can still confuse operators if banner is ignored — training + watermark.
