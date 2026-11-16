-- PURPOSE: Creates the four SWARM core Timescale tables on a genuinely empty database.
-- Applied by docker/postgres/bootstrap.sh when no backups/swarm_pg_*.dump is present.
-- Does not drop, truncate, or alter NOVA objects. Idempotent via IF NOT EXISTS / if_not_exists.

CREATE EXTENSION IF NOT EXISTS timescaledb;

CREATE TABLE IF NOT EXISTS public.tenants (
    tenant_id   uuid DEFAULT gen_random_uuid() NOT NULL,
    tenant_name text NOT NULL,
    metadata    jsonb DEFAULT '{}'::jsonb,
    created_at  timestamp with time zone DEFAULT now(),
    CONSTRAINT tenants_pkey PRIMARY KEY (tenant_id)
);

CREATE TABLE IF NOT EXISTS public.devices (
    id         bigint NOT NULL,
    tenant_id  uuid NOT NULL,
    device_id  text NOT NULL,
    metadata   jsonb DEFAULT '{}'::jsonb,
    created_at timestamp with time zone DEFAULT now(),
    removed_at timestamp with time zone,
    CONSTRAINT devices_pkey PRIMARY KEY (id),
    CONSTRAINT devices_tenant_id_fkey FOREIGN KEY (tenant_id) REFERENCES public.tenants (tenant_id)
);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_attribute a
        JOIN pg_class c ON c.oid = a.attrelid
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE n.nspname = 'public' AND c.relname = 'devices'
          AND a.attname = 'id' AND a.attidentity <> ''
    ) THEN
        ALTER TABLE public.devices ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY;
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS public.sensor_messages (
    message_id  text NOT NULL,
    tenant_id   uuid NOT NULL,
    device_id   text NOT NULL,
    recorded_at timestamp with time zone,
    received_at timestamp with time zone DEFAULT now() NOT NULL,
    payload     jsonb NOT NULL,
    CONSTRAINT sensor_messages_pkey PRIMARY KEY (message_id),
    CONSTRAINT sensor_messages_tenant_id_fkey FOREIGN KEY (tenant_id) REFERENCES public.tenants (tenant_id)
);

CREATE TABLE IF NOT EXISTS public.sensor_readings (
    id          bigint NOT NULL,
    recorded_at timestamp with time zone NOT NULL,
    message_id  text NOT NULL,
    tenant_id   uuid NOT NULL,
    device_id   text NOT NULL,
    channel_id  text,
    sensor_type text,
    value       jsonb,
    unit        text,
    metadata    jsonb DEFAULT '{}'::jsonb,
    CONSTRAINT sensor_readings_pkey PRIMARY KEY (id, recorded_at)
);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_attribute a
        JOIN pg_class c ON c.oid = a.attrelid
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE n.nspname = 'public' AND c.relname = 'sensor_readings'
          AND a.attname = 'id' AND a.attidentity <> ''
    ) THEN
        ALTER TABLE public.sensor_readings ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY;
    END IF;
END $$;

SELECT create_hypertable(
    'sensor_readings',
    'recorded_at',
    chunk_time_interval => INTERVAL '7 days',
    if_not_exists => TRUE
);

CREATE INDEX IF NOT EXISTS idx_readings_channel_time
    ON public.sensor_readings USING btree (tenant_id, channel_id, recorded_at DESC);

CREATE INDEX IF NOT EXISTS idx_readings_device_time
    ON public.sensor_readings USING btree (tenant_id, device_id, recorded_at DESC);

CREATE INDEX IF NOT EXISTS idx_readings_message
    ON public.sensor_readings USING btree (message_id);

CREATE INDEX IF NOT EXISTS sensor_readings_recorded_at_idx
    ON public.sensor_readings USING btree (recorded_at DESC);
