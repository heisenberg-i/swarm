# PURPOSE: Operator/developer documentation for SWARM.
# 18 — Operations and maintenance workflows

## Exists today

- Alerts ack/resolve.
- `predictive_maintenance` regenerated from sensor heartbeat/battery — not a CMMS.
- Maintenance page is read-only.
- No shift log, feedstock, downtime, incidents, checklists.

## Target (module I) — still SWARM, not SAP PM

Keep the audience: plant operators and site engineers on biogas sites.

| Record | Who writes | Fields (min) |
|---|---|---|
| Shift log | Operator | plant, shift, author, notes, created_at |
| Feedstock ticket | Operator / admin | plant, time, type, tonnes (OPERATOR source), optional lab |
| Downtime | Operator | equipment, start/end, reason (power, choke, planned) |
| Incident | Plant Admin | severity, description, related alert ids |
| Checklist | Operator | template (daily walkdown), ticks, exceptions |
| Work order | Plant Admin | equipment, title, status OPEN/DONE, due |

Predictive page: show **condition** (online, last seen, battery, open work orders). Drop “remaining useful life days” until a model card exists. Label current heuristic “sensor health index” if retained.

## Workflows

### Daily walkdown

Checklist template per plant type → operator completes on Android → exceptions create work orders.

### Feeding

Feedstock ticket → optional calculated yield later (gas / tonne) in analytics **only if** both GAS_FLOW mapping and tickets exist.

### Breakdown

Downtime on mixer → HMI still simulation or live independently → alert SUPPRESSED optional.

## Permissions

Operators write logs/tickets/checklists. Plant Admin closes work orders. Super Admin sees all company plants.

## What not to build

Full spare-parts inventory, purchasing, contractor portals. That is emPOWER/ERP. SWARM stores **operational truth** on site.
