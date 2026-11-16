# PURPOSE: Operator/developer documentation for SWARM.
# 05 — Business domain model

SWARM monitors **plants**, not abstract devices. The domain follows how a biogas / Bio-CNG engineering company commissions a site.

## Plant types (already in code)

`BIOGAS`, `BIO_CNG`, `SANITATION`, `STP`, `ORGANIC_WASTE`, `WASTE_TO_ENERGY`

These stay. Process **areas** are configurable per plant. A sanitation plant does not inherit Bio-CNG compression because the enum says so.

## Configurable process areas

A **plant template** lists areas. Enabling an area exposes equipment templates and typical instruments. Disabling hides them from HMI packs and dashboards. It does not delete history.

| Area code | Typical plants | Example equipment | Example instruments |
|---|---|---|---|
| `FEED_RECEIVING` | All | Weighbridge, hopper | Weight (operator or scale) |
| `PRETREATMENT` | Biogas, CBG, organic | Shredder, sorter | Motor run, temp |
| `SLURRY_PREP` | Biogas, CBG | Mix tank, feed pump | Level, flow |
| `DIGESTER` | All AD | Digester, mixer, heating | T, pH, level, mixer run |
| `BIOGAS_STORAGE` | Biogas, CBG | Holder, blower | Pressure, CH₄, H₂S, flow |
| `GAS_CONDITIONING` | CBG, some biogas | Chiller, knockout, H₂S media | T, dew point, H₂S |
| `UPGRADING` | BIO_CNG | PSA/membrane | CH₄, CO₂, flow |
| `COMPRESSION_STORAGE` | BIO_CNG | Compressor, cascade | Discharge P, T |
| `DIGESTATE` | AD plants | Screw press, tank | Level, runtime |
| `EFFLUENT` | Sanitation, STP, AD | Equalization, aeration | pH, DO, flow |
| `UTILITIES` | All | Boiler, flare, transformer | Status, kW |
| `SAFETY` | All | Gas detector, ESD display | LEL, status (indication only) |

**Do not** put ESD/trip logic in SWARM. Safety instruments may be **displayed** if wired through a gateway. Trips stay in the PLC / hardwired chain (document 16).

## Entity language (target)

```
Company
  └ Plant (type, timezone, template)
       ├ ProcessArea (enabled)
       ├ Equipment (tag, area, kind, template)
       ├ Instrument (tag, equipment, measurement, source)
       ├ Gateway / Device (hub, PLC, MQTT)
       ├ Channel (device + instrument mapping)
       ├ Person assignment (role, optional instrument set)
       └ OperatingParameter (name, unit, source: MEASURED|CALCULATED|OPERATOR|AI_ESTIMATE)
```

### Mapping to today

| Today | Becomes |
|---|---|
| `plants` | Plant + timezone + template |
| `enabled_sensor_types` CSV | Area/instrument catalog enablement |
| `sensor_nodes` | Instrument **or** hub channel (keep table, add `equipment_id`, `area_id`, `source_kind`) |
| `device_chip_id` shared across 3 nodes | `devices` row (hub) 1:N channels |
| `hmi_equipment` | Equipment + diagram hotspot |
| HMI `tag_no` | ISA-style tag (TIT-103) |

## Value sources (mandatory discriminator)

| `source_kind` | Meaning | Example |
|---|---|---|
| `MEASURED` | Came from a device ingest with quality | DHT11 temperature |
| `CALCULATED` | Derived in SWARM from measured inputs | CH₄ Nm³/d from flow + time |
| `OPERATOR` | Typed in a log | Feedstock tonnes |
| `AI_ESTIMATE` | Model or rule extrapolation | Health score, heuristic RUL |

NOVA and reports must print `source_kind`. Health score is `AI_ESTIMATE` even when rules are simple.

## Operating parameters vs instruments

- **Instrument:** a tag that can have telemetry (TIT-103).
- **Parameter:** a named setpoint or KPI (digester T target 37 °C) — may be operator-entered.
- Thresholds attach to instruments (or parameters), not to a global YAML forever.

Plant-specific limits require **qualified approval** (Plant Admin + recorded reason). Shipping 25–45 °C as a default **template** for mesophilic digestion is fine; applying it silently to a thermophilic CBG plant is not.

## Relationships the product needs

- Hub `SWARM MODEL` **produces** channels T/RH/CH₄ (MQ5 is a **proxy** for combustible gas, not a calibrated %LEL or %CH₄ analyser). UI and Partner `metric_type=METHANE` today overstate chemistry. 2.0: keep the metric for compatibility, document `measurement_basis=MQ5_ADC` / optional calibrated CH₄.
- Transmitter (4–20 mA via future gateway) **is** the instrument.
- PLC register **maps** to an instrument; SWARM does not own the register map as safety.

## Edge cases

| Case | Rule |
|---|---|
| Two digestors | Two equipment tags, same area type, separate instruments |
| Shared flare | Utilities area, plant-level equipment |
| Lab GC CH₄ | Laboratory import, `OPERATOR` or `LAB`, not hub methane |
| MQ5 vs analyser | Different instruments; do not average them |
| Plant without HMI pack | Field Devices + dashboard only |
| Sanitation plant | Effluent areas enabled; upgrading hidden |
