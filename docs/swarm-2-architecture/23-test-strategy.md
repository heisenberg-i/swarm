# PURPOSE: Operator/developer documentation for SWARM.
# 23 — Test strategy

## Exists today

Six backend unit tests (Nova guard/slots/permissions, Swarm URL, org ids, deployment role). **Zero** frontend tests. **Zero** security/ingest integration tests.

## Pyramid for this repo

1. **Unit** — rules, AnswerGuard, policy resolution, quality validation, ArchUnit module deps.
2. **API integration** (Spring `@SpringBootTest` + H2/Testcontainers MySQL) — authz, ingest auth, Partner isolation, CC approve.
3. **Contract** — Partner JSON fixtures frozen for emPOWER.
4. **Frontend** — Vitest for ACL-sensitive helpers; Playwright for Field Devices + login + CC approve smoke.
5. **Firmware** — not automated here; pairing tested on hardware in commissioning.

## Must-add tests before claiming “2.0 security”

| Test | Fails today |
|---|---|
| Unauthenticated `/iot/batch` rejected when compat off | permitAll |
| Disabled user refresh denied | JWT still valid |
| Partner empty allow-list reads no plants | reads all |
| User A cannot GET user B `sensor-nodes/{id}` | no ACL |
| SSO new user cannot GET `/dashboard` | ACTIVE empty |
| CC admin 403 on `/admin/partner/api-keys` | allowed |
| `/auth/change-password` without token 401 | permitAll |
| Report generate without plantId 400 | findAll |
| HMI command without plant admin 403 | |
| NOVA answer without evidence for a numeric claim | |

## Browser verification

Keep the existing rule: UI slices verified in browser. Playwright later encodes that.

## Load

Not first. Hub is 30 s × nodes. A few hundred plants is MySQL-fine if indexes stay.

## Simulation

Tests that send HMI commands assert `simulationMode=true` unless a commissioning fixture is present.
