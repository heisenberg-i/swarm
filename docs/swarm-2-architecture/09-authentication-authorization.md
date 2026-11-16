# PURPOSE: Operator/developer documentation for SWARM.
# 09 — Authentication and authorization

Assume the browser is hostile. Hidden routes (`/settings`) are not controls. `ProtectedRoute` is UX.

## Exists today

- Local email/password, BCrypt **strength 12** (aligned with emPOWER).
- JWT HS256, 24 h, secret from env (committed default exists — treat as compromised in any repo clone).
- SSO consumer: OAuth code → `EmpowerSaaSAuthClient` → local user upsert → **same JWT**.
- Partner: Bearer API key, hashed at rest, prefix displayed.
- Roles in JWT; `PlantAccessService` for plant lists.
- Logout does not revoke.
- `/auth/**` permitAll.
- Default CC admin seeded on primary.

## Target identity lifecycle

### 1. Bootstrap (replace documented defaults)

First install:

1. If no `CONTROL_CENTER_ADMIN` exists, process prints a **one-time** setup URL/token to stdout / local file with 0600 perms — not a constant in git.
2. Operator sets email + password meeting policy + enrols MFA.
3. Token expires in 1 hour. `ProductionDataCleanup` seeding of known passwords is removed.

Break-glass: second CC admin stored as a hashed recovery code in env `SWARM_BREAKGLASS_HASH`, used only with MFA and audited.

### 2. People

| Path | Rule |
|---|---|
| Local signup | PENDING, no JWT until approval (already true for login). |
| CC/Super create user | Invite link or one-time password; force change + MFA if privileged. |
| SSO | Upsert by `identity_user_id` then email. **Status PENDING** until company+plants assigned, unless an existing ACTIVE user is linking. |
| Disable | Immediate: session revocation + JWT denylist/version bump. |
| Password | BCrypt 12+; no change-password without auth. |

### 3. Sessions (replace immortal 24 h JWT)

- **Access token:** 15 minutes, JWT, `uid`, `ver`, `roles`, **no giant ACL required** if server loads plants from DB (cache 30 s).
- **Refresh token:** opaque, hashed in `refresh_tokens`, rotated on use, family revoke on reuse.
- Logout deletes refresh + increments `users.token_version`.
- Android uses the same pair; store refresh in encrypted prefs (Capacitor Preferences is not enough long-term — use platform Keystore via a plugin in a later slice).

### 4. MFA

- Mandatory for `CONTROL_CENTER_ADMIN` and `SUPER_ADMIN`.
- TOTP first (no SMS as the only factor).
- SSO: if emPOWER asserts `mfaVerified`, accept for login; still require SWARM step-up for `hmi.command.live` and Partner key create.

### 5. Authorization

Replace ad-hoc `hasRole` with:

1. Authentication filter (JWT **or** API key **or** device credential) — mutually exclusive by path.
2. Method security: `@PreAuthorize("@authz.can(#id, 'plant.read')")`.
3. Plant/company checks **inside** every service that loads an entity by id.

Fix known holes:

- `/auth/me`, `/auth/change-password` authenticated.
- `GET /sensor-nodes/{id}` plant ACL.
- Reports require `plantId` and ACL.
- AI acknowledge ACL.
- Partner empty allow-list = deny all.
- `/admin/partner` Super Admin only (not CC).

### 6. Device identity

At pair:

1. Server generates `device_id` + `device_secret` (shown once).
2. Hub stores secret in EEPROM (firmware change).
3. Batch ingest: `Authorization: Bearer swd_…` or HMAC `X-SWARM-Device` + timestamp.
4. Rotate from Field Devices; old secret grace 24 h.

Until firmware is updated, a **compat** mode accepts legacy unauthenticated batch **only from known chip+node tuple with rate limit** and flags the device `CREDENTIAL_MISSING`. Compat off by env on new installs.

### 7. API keys (Partner)

Keep hash + prefix + revoke. Add:

- `scopes` (`health`, `catalog`, `telemetry`, `aggregates`).
- `expires_at` required for production keys (max 90 days, rotatable).
- Rate limit per key.
- Sandbox default **false** except `dev` profile.

### 8. Secrets management

- No secrets in firmware source, README, or `application.yml` defaults that work in production.
- Pi: env files 0600, not world-readable.
- JWT secret ≥ 256 bit, generated at bootstrap.
- LLM keys stay env (already).

### 9. Data protection

- TLS at Cloudflare / nginx. No cleartext to the public API.
- Android cleartext **only** for RFC1918/link-local (pairing).
- PII: email, phone in `users`; Partner responses must not include user emails.
- Backup dumps treated as secret.

### 10. Vulnerability / incident

- Security.md later: report address.
- Compromised Partner key: Super Admin revoke (already).
- Compromised JWT secret: rotate + all sessions invalid.
- Compromised hub: rotate device secret + watch ingest IP.

## Path security matrix (target)

| Path | Auth |
|---|---|
| `GET /health` | Public liveness |
| `POST /auth/login`, signup, sso | Public, rate limited |
| `GET /auth/me`, change-password | User JWT |
| `POST /iot/*` | Device credential |
| `/partner/v1/**` | Partner API key |
| `/control-center/**` | CC JWT + MFA |
| `/hmi/**/commands` | User JWT + permission + commissioning |
| Everything else plant | User JWT + plant ACL |
