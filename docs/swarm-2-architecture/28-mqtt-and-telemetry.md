# PURPOSE: Operator/developer documentation for SWARM.
# 28 — MQTT and telemetry (implemented)

**Status:** Implemented locally (2026-11-07). Broker optional. No live equipment connected.

## Pipeline

```
HTTP /iot/batch  ──┐
MQTT swarm/v1…   ──┼──► Device auth (optional headers / topic UID)
                   └──► TelemetryIngestService
                            ├── resolve device (never trust payload plant alone)
                            ├── resolve channel map (device-specific)
                            ├── calibrate (slope/intercept)
                            ├── quality + hard/soft range
                            ├── idempotency (device_id + message_id)
                            ├── persist sensor_readings
                            ├── alert evaluate
                            └── AI health (existing)
```

Rejected messages land in `ingest_rejected` (dead-letter style audit).

## Device identity

| Concept | Storage |
|---------|---------|
| Physical hub | `devices` (`device_uid`, `chip_id`, `secret_hash`, `legacy_compat`) |
| Logical channel | `device_channels.channel_id` (engineer-assigned, **not** GPIO) |
| Compat inventory | `sensor_nodes` linked via `device_channels.sensor_node_id` |

Pairing (`POST /devices/pair`) provisions the device + three channels and returns `deviceUid` + one-time `deviceSecret`.

## HTTP auth migration

Headers (preferred):

- `X-SWARM-Device-Id: <deviceUid>`
- `X-SWARM-Device-Secret: <secret>`

Or `Authorization: Device <deviceUid>:<secret>`.

Env:

- `SWARM_IOT_COMPAT_LEGACY=true` (default): hubs with `legacy_compat=true` may still POST batch with `chipId` only (current ESP firmware).
- Set `false` after firmware ships credentials.
- Rotating a secret sets `legacy_compat=false`.

## MQTT

Config (`mqtt.*`):

| Property | Default |
|----------|---------|
| `mqtt.enabled` | `false` |
| `mqtt.broker-url` | `tcp://localhost:1883` (Mosquitto / EMQX) |
| `mqtt.swarm-topic` | `swarm/v1/+/+/+/telemetry` |
| `mqtt.topic` | `biopower/sensors/#` (legacy) |

### Topic convention

```
swarm/v1/{tenantId}/{plantId}/{deviceUid}/telemetry
swarm/v1/{tenantId}/{plantId}/{deviceUid}/status      # reserved
swarm/v1/{tenantId}/{plantId}/{deviceUid}/heartbeat   # reserved
```

Devices must not publish to other plants’ topics (broker ACL + server validation).

### Payload contract `schemaVersion: "1.0"`

```json
{
  "schemaVersion": "1.0",
  "deviceId": "SWARM-HUB-001",
  "messageId": "unique-message-id",
  "timestamp": "2026-11-07T10:30:00Z",
  "readings": [
    { "channelId": 1, "value": 38.5 },
    { "channelId": 2, "value": 7.2 }
  ]
}
```

Server resolves measurement type, units, instrument tag, equipment, and area from `device_channels`.

### Simulated testing

Unit tests drive `MqttTelemetryHandler` without a broker. To exercise a real broker locally:

1. Run Mosquitto or EMQX on `1883`.
2. `MQTT_ENABLED=true MQTT_BROKER=tcp://127.0.0.1:1883`
3. Publish to `swarm/v1/_/{plantId}/{deviceUid}/telemetry` with a paired device’s channels.
4. Check `/api/health` → `mqtt.messagesReceived`.

## Rate limits

Process-local fixed window:

- Ingest: `SWARM_RATE_INGEST_PER_MIN` (default 120)
- Login: `SWARM_RATE_LOGIN_PER_MIN` (default 20)

## APIs

- `GET /field-devices?plantId=`
- `GET /field-devices/{id}`
- `PUT /field-devices/{id}/channels`
- `POST /field-devices/{id}/rotate-secret` (Super Admin)

## Explicitly not done here

- Live PLC actuation
- Broker ACL provisioning automation
- Firmware binary that sends device headers (compat path kept)
- Timescale / Kafka
