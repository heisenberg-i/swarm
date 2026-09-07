# Control Center modules (scale)

Control Center is how nanoFarm **sells and provisions Swarm**: tenants, plants, who may log in. The Swarm app is what the customer **operates**.

## Why the new test user was missing

The Tenants table only shows a **user count per company**. It is not a user directory.

Users appear on a tenant only if `company_id` is set (created by that tenant’s Super Admin, or approved here).

Public **Sign up** currently created an Operator with **no company**, so they never showed on a tenant. They could still log into the plant app with an empty plant list (alerts spinner). That is the gap this approval queue closes.

## Module map

| Module | Status | What it is | What it is not |
|--------|--------|------------|----------------|
| Overview | Shipped | Pipeline counts, pending approvals | Plant health |
| Tenants | Shipped | Company lifecycle, Super Admin, assign plants, sensor access view | Live HMI / readings |
| Plant inventory | Shipped | Create sites, assign later | Sensor pairing |
| **User approvals** | **Now** | Queue: signup / no-tenant users → assign tenant + role + plants → activate | Day-to-day staff edits |
| **HMI access** | **Now** | Per-plant enable/disable. Swarm app shows Plant HMI only when CP turns it on | Live HMI / readings |
| User directory | Next | Search all users across tenants | Mixing live telemetry |
| Audit log | Next | Who approved, assigned plants, disabled tenants | |
| Entitlements / seats | Later | Plan limits when selling at volume | emPOWER billing SoT |
| Pagination / API search | Later | When tenant count is large | |

**Never in Control Center:** Dashboard, Plant HMI, Connect Device, Alerts, NOVA OP, AI, Maintenance, Reports. Those stay in the Swarm app so tenant data stays isolated. An Operator (e.g. Vulcan Mozambique) will not see Control Center at all.

## User approval flow

1. Person signs up on the Swarm login page → status `PENDING`, **cannot log in**.
2. Control Center **Approvals** lists them (also lists existing ACTIVE users with no company).
3. Operator picks **tenant**, **role** (Super Admin only if that tenant has none, else Plant Admin / Operator), optional **plants**.
4. **Approve** → `ACTIVE`, linked to company, plants assigned. They log in at the Swarm app.
5. **Reject** → `DISABLED`.

Tenant Super Admins still add Plant Admins / Operators inside the Swarm app **Users** screen. Those users already have a company and do **not** wait in this queue.

## Suggested nav (this release)

1. Overview  
2. Tenants  
3. Plant inventory  
4. **Approvals**
5. **HMI** (`/hmi-access`) — enable/disable Plant HMI per plant. Do not use `/hmi` in Control Center; that URL is the static PFD folder. Tenant users see the sidebar item only when at least one of their plants is enabled.
