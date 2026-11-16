# PURPOSE: Operator/developer documentation for SWARM.
# 17 — Alerts and notifications

## Exists today

`AlertService.evaluateReading` uses **process YAML**:

- pH min/max 6.5 / 8.5
- temp 25 / 45 °C (also applied to TEMPERATURE_TRANSMITTER)
- pressure max 2.5 bar
- gas flow / methane special cases (MQ5 bands)

Timeout job 5 minutes → node OFFLINE + alert.  
Ack/resolve in UI.  
`system_settings` not used for thresholds.  
No email despite mail starter.

## Problems

- One threshold for every plant and every temperature tag (digester vs ambient DHT11).
- DHT11 humidity/methane hub temperature can alarm as if it were digester T.
- No escalation, no notification, no audit of threshold edits.

## Target

### Policy object

`alert_policies`: plant_id, instrument_id (nullable = type default), metric, min, max, severity, enabled, version, approved_by, approved_at.

Resolution order: instrument → plant metric → platform template.

Templates ship for mesophilic AD; **inactive until Plant Admin accepts**.

### Lifecycle

`ACTIVE → ACKNOWLEDGED → RESOLVED` (keep).  
Add `SUPPRESSED` (maintenance window).  
Re-alert if still out of band after `rearm_minutes`.

### Notifications (phase)

1. In-app (exists).
2. Email via JavaMail to user emails on assigned plant, respect `notification_preferences`.
3. SMS/WhatsApp **not** in 2.0 unless a provider is chosen — README claim is retired.

### Escalation

If CRITICAL unacked 15 min → Plant Admins; 60 min → Super Admin. Configurable. Night quiet hours skip WARNING, not CRITICAL.

### Stale / offline

Keep timeout job. Quality STALE on latest reading. Do not spam every 5 min: one OFFLINE alert per node until it revives (today may recreate — verify and debounce).

### Acceptance

- Changing a threshold writes audit + policy version.
- Hub ambient temperature can be unbound from digester temperature policy.
- Operator can ack; cannot edit policy.
