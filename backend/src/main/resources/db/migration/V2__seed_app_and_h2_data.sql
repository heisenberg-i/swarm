-- Seed tenant profiles and attach Timescale devices to plants.
-- Does not insert into sensor_readings / sensor_messages (telemetry already present).
-- Does not create login accounts. The first Control Center admin is provisioned from
-- SWARM_BOOTSTRAP_ADMIN_EMAIL / SWARM_BOOTSTRAP_ADMIN_PASSWORD when none exists.
-- Demo tenants and demo logins are opt-in (SWARM_SEED_DEMO_ACCOUNTS=true).

INSERT INTO tenant_profiles (tenant_id, status, updated_at)
SELECT t.tenant_id, 'ACTIVE', NOW()
FROM tenants t
WHERE NOT EXISTS (
    SELECT 1 FROM tenant_profiles p WHERE p.tenant_id = t.tenant_id
);

-- One plant per existing Timescale tenant so live methane readings are queryable.
INSERT INTO plants (plant_name, plant_type, location, status, hmi_enabled, tenant_id, created_at, updated_at)
SELECT t.tenant_name, 'BIOGAS', 'Timescale telemetry', 'ACTIVE', FALSE, t.tenant_id, NOW(), NOW()
FROM tenants t
WHERE NOT EXISTS (SELECT 1 FROM plants p WHERE p.tenant_id = t.tenant_id);

INSERT INTO device_registry (
    device_pk, plant_id, device_name, device_kind, lifecycle, legacy_compat, config_version, updated_at
)
SELECT d.id, p.plant_id, d.device_id, 'ESP_HUB', 'ACTIVE', TRUE, 1, NOW()
FROM devices d
JOIN plants p ON p.tenant_id = d.tenant_id
WHERE NOT EXISTS (SELECT 1 FROM device_registry r WHERE r.device_pk = d.id);

INSERT INTO sensor_nodes (plant_id, node_name, sensor_type, status, created_at, updated_at)
SELECT DISTINCT p.plant_id, 'Methane analyser', 'METHANE', 'ACTIVE', NOW(), NOW()
FROM plants p
JOIN devices d ON d.tenant_id = p.tenant_id
WHERE d.device_id = 'swarm_esp32_001'
  AND NOT EXISTS (
      SELECT 1 FROM sensor_nodes n WHERE n.plant_id = p.plant_id AND n.sensor_type = 'METHANE'
  );

INSERT INTO device_channels (
    device_id, channel_id, sensor_node_id, measurement_type, engineering_unit,
    instrument_tag, slope, intercept, hard_min, hard_max, sampling_interval_sec, active
)
SELECT d.id,
       'plant_01_swarm_esp32_001_gas_01',
       n.node_id,
       'METHANE',
       'ppm',
       'AIT-HUB-GAS',
       1, 0, 0, 100000, 30, TRUE
FROM devices d
JOIN plants p ON p.tenant_id = d.tenant_id
JOIN sensor_nodes n ON n.plant_id = p.plant_id AND n.sensor_type = 'METHANE'
WHERE d.device_id = 'swarm_esp32_001'
  AND NOT EXISTS (
      SELECT 1 FROM device_channels c
      WHERE c.device_id = d.id AND c.channel_id = 'plant_01_swarm_esp32_001_gas_01'
  );

INSERT INTO partner_organizations (id, name, external_org_id, active, created_at)
OVERRIDING SYSTEM VALUE VALUES
    (1, 'BIOPOWER Pilot', 'org_biopower_pilot', TRUE, TIMESTAMP '2026-11-07 13:57:51.38169')
ON CONFLICT (id) DO NOTHING;

INSERT INTO partner_api_keys (id, org_id, key_hash, key_prefix, name, active, created_at)
OVERRIDING SYSTEM VALUE VALUES
    (1, 1, '85814b28f4894a45f9942704b68fbc3e7a474e457ed7130188f5be8da754aa74',
     'swk_sandbox_', 'emPOWER sandbox', TRUE, TIMESTAMP '2026-11-07 13:57:51.384153'),
    (2, 1, 'ba42da33df169a6a173d63275445c8d01324fe95f165f2b705260293b762c2e7',
     'swk_k_0803fc', 'k', TRUE, TIMESTAMP '2026-11-07 17:23:50.264257')
ON CONFLICT (id) DO NOTHING;

INSERT INTO system_settings (setting_key, setting_value, category, description, updated_at)
VALUES
    ('production.cleanup.v1.done', 'true', 'SYSTEM', 'Imported from local H2', NOW()),
    ('alert.ph.min', '6.5', 'ALERT', NULL, NOW()),
    ('alert.ph.max', '8.5', 'ALERT', NULL, NOW()),
    ('alert.temp.min', '25', 'ALERT', NULL, NOW()),
    ('alert.temp.max', '45', 'ALERT', NULL, NOW()),
    ('alert.pressure.max', '2.5', 'ALERT', NULL, NOW()),
    ('mqtt.broker.url', 'tcp://localhost:1883', 'MQTT', NULL, NOW()),
    ('mqtt.topic', 'biopower/sensors/#', 'MQTT', NULL, NOW()),
    ('mail.smtp.host', 'smtp.gmail.com', 'EMAIL', NULL, NOW()),
    ('ai.analysis.interval', '300', 'AI', NULL, NOW()),
    ('report.schedule.daily', '06:00', 'REPORT', NULL, NOW())
ON CONFLICT (setting_key) DO NOTHING;

SELECT setval(pg_get_serial_sequence('plants', 'plant_id'), GREATEST((SELECT MAX(plant_id) FROM plants), 1));
SELECT setval(pg_get_serial_sequence('users', 'id'), GREATEST(COALESCE((SELECT MAX(id) FROM users), 1), 1));
SELECT setval(pg_get_serial_sequence('partner_organizations', 'id'), GREATEST((SELECT MAX(id) FROM partner_organizations), 1));
SELECT setval(pg_get_serial_sequence('partner_api_keys', 'id'), GREATEST((SELECT MAX(id) FROM partner_api_keys), 1));
SELECT setval(pg_get_serial_sequence('sensor_nodes', 'node_id'), GREATEST(COALESCE((SELECT MAX(node_id) FROM sensor_nodes), 1), 1));
