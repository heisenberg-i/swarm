-- PURPOSE: Clears non-NOVA SWARM application rows while keeping schema, Timescale, Flyway, and NOVA tables intact.
-- Used by Docker first-boot after restoring the PostgreSQL backup.

BEGIN;

-- Child / telemetry first. Never TRUNCATE CASCADE (nova_threads.user_id ON DELETE CASCADE).
DELETE FROM ingest_rejected;
DELETE FROM sensor_readings;
DELETE FROM sensor_messages;
DELETE FROM device_channels;
DELETE FROM device_registry;
DELETE FROM devices;
DELETE FROM user_sensor_nodes;
DELETE FROM user_plants;
DELETE FROM hmi_equipment_state;
DELETE FROM hmi_plant_state;
DELETE FROM hmi_equipment;
DELETE FROM alerts;
DELETE FROM ai_recommendations;
DELETE FROM predictive_maintenance;
DELETE FROM reports;
DELETE FROM partner_api_keys;
DELETE FROM partner_org_plants;
DELETE FROM partner_organizations;
DELETE FROM sensor_nodes;
DELETE FROM equipment;
DELETE FROM process_areas;
DELETE FROM plants;
DELETE FROM tenant_profiles;
DELETE FROM system_settings;

-- Keep any user still referenced by NOVA (none expected on a fresh NOVA table).
DELETE FROM users u
 WHERE NOT EXISTS (SELECT 1 FROM nova_threads t WHERE t.user_id = u.id);

DELETE FROM tenants t
 WHERE NOT EXISTS (SELECT 1 FROM nova_threads nt
                   JOIN users u ON u.id = nt.user_id
                   WHERE u.tenant_id = t.tenant_id)
   AND NOT EXISTS (SELECT 1 FROM plants p WHERE p.tenant_id = t.tenant_id)
   AND NOT EXISTS (SELECT 1 FROM users u WHERE u.tenant_id = t.tenant_id)
   AND NOT EXISTS (SELECT 1 FROM devices d WHERE d.tenant_id = t.tenant_id);

-- Restart owned identity sequences (skip tables we do not own / do not exist).
DO $$
DECLARE
    seq text;
BEGIN
    FOREACH seq IN ARRAY ARRAY[
        'users_id_seq',
        'plants_plant_id_seq',
        'sensor_nodes_node_id_seq',
        'device_channels_id_seq',
        'ingest_rejected_id_seq',
        'alerts_id_seq',
        'ai_recommendations_id_seq',
        'predictive_maintenance_id_seq',
        'reports_id_seq',
        'system_settings_id_seq',
        'partner_organizations_id_seq',
        'partner_api_keys_id_seq',
        'hmi_equipment_id_seq',
        'hmi_equipment_state_id_seq',
        'process_areas_id_seq',
        'equipment_id_seq'
    ]
    LOOP
        IF EXISTS (SELECT 1 FROM pg_class WHERE relname = seq AND relkind = 'S') THEN
            EXECUTE format('ALTER SEQUENCE %I RESTART WITH 1', seq);
        END IF;
    END LOOP;
END $$;

COMMIT;
