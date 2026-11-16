# PURPOSE: Operator/developer documentation for SWARM.
# Protected modules — SWARM 2.0 rebuild

**Last verified:** 2026-11-07

## NOVA (absolute restriction)

Do **not** modify, relocate, delete, rename, or reconfigure anything in these trees:

### Backend
- `backend/src/main/java/com/biopower/novaspace/**`
- `backend/src/test/java/com/biopower/novaspace/**`
- `biopower.nova-space-op.*` keys in `application*.yml` (read-only; do not change behaviour)
- Retention key `biopower.retention.nova-messages-days` (leave as-is)
- Flyway tables `nova_threads`, `nova_messages` (no ALTER / DROP)

### Frontend
- `frontend/src/pages/NovaSpaceOp.jsx`
- `frontend/src/components/nova/**`
- `frontend/src/lib/novaBubbleState.js`
- NOVA routes / FAB wiring in `App.jsx` and `MainLayout.jsx` — preserve; do not change NOVA behaviour when editing adjacent nav

### Reference / DNA
- `NOVA_DNA_source_for_SWARM/**` (reference only)

### Verification
```bash
# After any rebuild session — must show no local edits under these paths
git status --short -- backend/src/main/java/com/biopower/novaspace \
  backend/src/test/java/com/biopower/novaspace \
  frontend/src/components/nova frontend/src/pages/NovaSpaceOp.jsx \
  frontend/src/lib/novaBubbleState.js NOVA_DNA_source_for_SWARM
```

Integration boundary (inspect only): `NovaSpaceOpController`, skill registry calling plant/telemetry services. Prefer extending plant/telemetry APIs over changing NOVA callers.

## Other hard stops (operational)

- No Git operations (commit/push/pull/merge/rebase/reset/branch).
- No production deploy, live DB, credential rotation, or PLC actuation.
- HMI remains simulation by default.
