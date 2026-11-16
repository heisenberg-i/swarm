# PURPOSE: Operator/developer documentation for SWARM.
# 13 — Sensor and device data model

## Exists today

`SensorType` enum: PH, TEMPERATURE, PRESSURE, GAS_FLOW, METHANE, CARBON_DIOXIDE, HYDROGEN_SULFIDE, AMMONIA, HUMIDITY, LIQUID_LEVEL, PRESSURE_TRANSMITTER, FLOW_TRANSMITTER, TEMPERATURE_TRANSMITTER.

Hub pairing always creates TEMPERATURE, HUMIDITY, METHANE (MQ5 ADC stored as methane).

`HARDWARE_PRESETS.ESP_HUB` and `TRANSMITTERS` in the SPA.

Nodes carry `firmware_version`, `battery_level`, `signal_strength`, `device_chip_id`, `device_ip`.

No calibration table. No validity range per instrument. Global alert YAML.

## Vendor-neutral model

```
Device        — physical (ESP hub, PLC, MQTT gateway, analyser)
Channel       — one stream from a device (ADC, register, topic)
Instrument    — plant tag (TIT-103) engineers care about
Measurement   — quantity + unit + metric_type
```

A SWARM MODEL hub is **one Device**, three Channels, three Instruments (or three nodes in compat).

## Device kinds

| kind | How it talks | 2.0 support |
|---|---|---|
| `ESP_HUB` | HTTP batch | Now |
| `MQTT_GATEWAY` | `biopower/sensors/#` | Code exists, enable per plant |
| `REST_GATEWAY` | `/iot/data` | Now |
| `PLC_MODBUS` | Via on-site gateway, not cloud poll of RFC1918 | Later |
| `MANUAL` | Operator/lab | Ops module |

## Per measurement (required metadata)

| Field | Rule |
|---|---|
| quantity | e.g. temperature |
| unit | °C, bar, m³/h, %, ppm, pH |
| metric_type | Existing enum + future types without breaking Partner |
| mapping | device, channel, equipment, area, plant |
| calibration | offset/span/date/by whom; null = factory |
| sampling_interval | Hub 30 s upload / 5 s local; transmitters per spec |
| validity_range | Soft (quality BAD) vs hard (reject) |
| quality | MEASURED, BAD, STALE, INCOMPLETE |
| stale_after | Default 10 min (`sensor-timeout-minutes`) |
| thresholds | From `alert_policies`, not only YAML |
| retention | Plant setting |

## Catalogue (engineering defaults — templates only)

Qualified person must approve plant limits. These are **not** universal trips.

| metric | unit | typical validity | notes |
|---|---|---|---|
| TEMPERATURE | °C | -20–150 | Digester vs gas cooler are different policies |
| HUMIDITY | % | 0–100 | DHT11 |
| METHANE | % or ADC | 0–100 % if calibrated; ADC 0–1024 if MQ5 | **Declare basis** |
| PH | — | 0–14 | |
| PRESSURE | bar | 0–16 typical biogas | Compressors higher — plant policy |
| GAS_FLOW | m³/h | ≥0 | Totalizer vs gauge: `reading_type` |
| H2S | ppm | ≥0 | Analyser, not MQ5 |
| CO2 | % | 0–100 | Upgrading |
| LIQUID_LEVEL | % | 0–100 | |
| AMMONIA | ppm | ≥0 | |
| *_TRANSMITTER | as PIT/FIT/TIT | plant | P&ID tags |

Safety gas detectors (LEL) if ever mapped: indication only.

## Quality state machine

```
ingest → validate range → MEASURED
       → out of hard range → reject or BAD (plant policy)
       → timeout job → STALE (do not overwrite last MEASURED value)
daily agg missing hours → INCOMPLETE
```

Never display STALE as a live green number without age.

## Calibration

```
instrument_id, valid_from, valid_to,
slope, intercept, unit,
reference_standard, performed_by, notes
```

Apply on ingest if present: `eng = slope * raw + intercept`. Store **engineering value** in `sensor_readings.value`; raw optional JSON.

MQ5: default slope/intercept identity on ADC; UI label “combustible gas (raw)” until calibrated.

## Lifecycle

`INVENTORY → COMMISSIONING → ACTIVE → MAINTENANCE → RETIRED`

Pairing moves COMMISSIONING → ACTIVE. Unpair does not delete history.

## Edge cases

- Two hubs one plant: two `devices`, six instruments.
- Re-pair same chip: claim existing nodes (already in `DeviceService`).
- Chip moves plants: Super Admin only, audit, Partner device_id stays `node_{id}` (stable).
- Battery/RSSI on ESP: device health, not process values.
