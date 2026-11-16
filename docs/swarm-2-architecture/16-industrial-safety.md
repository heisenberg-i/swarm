# PURPOSE: Operator/developer documentation for SWARM.
# 16 — Industrial safety boundaries

Cloud monitoring is not the safety system.

## Layers (keep separate)

| Layer | Owner | Examples |
|---|---|---|
| 0 Hardwired | Site electrical | E-stop, gas detector trip to flare/blower, compressor high-pressure switch |
| 1 PLC / local panel | Site integrator | Interlocks, permissives, sequences |
| 2 SWARM on-site gateway (future) | nanoFarm + integrator | Read registers, **forward command requests** |
| 3 SWARM cloud | This product | Visualize, alarm, simulate, request |

A command from layer 3 that arrives at layer 2 is a **request**. Layer 1 may ignore it.

## Rules

1. SWARM 2.0 default HMI is **simulation**. `simulationMode` cannot be turned off in software alone.
2. `LIVE_APPROVED` requires a commissioning record: plant, diagram pack, gateway identity, named engineer, date, statement that PLC interlocks were tested independently.
3. No SWARM feature may advertise “ESD from the app”.
4. Safety instrument values in the dashboard are **indication**. Loss of communication ≠ safe; it is STALE.
5. Simulation must be visually obvious (banner, watermark on PFD).
6. Training plants stay simulation forever.

## Commissioning record (minimum)

- Plant id, pack id, gateway serial
- Interlock test evidence stored as files (ops), not a checkbox only
- Dual approval: nanoFarm commissioning engineer + customer Plant Admin
- Expiry / re-approval after P&ID change

Until that row exists, API returns `SIMULATION_ONLY` for live-mode requests.

## PLC integration (later)

- Poll/subscribe **reads** through gateway.
- Writes: allowlist of tags marked `controllable` **and** `live_writable` on equipment.
- Rate limit commands (e.g. 1 / 2 s / tag).
- No bulk “stop all” from cloud without PLC-side confirmation.

## Legal / claims

Do not claim SIL, IEC 61511, or 62443 compliance in UI copy. Current Help/HMI diagnostics text that implies EtherCAT I/O is **false** — remove in a copy slice (`HmiDiagnosticsPanel` fake channels).
