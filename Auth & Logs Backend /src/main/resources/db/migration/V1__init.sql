-- Flyway baseline for Auth & Logs schema

-- Optional extensions (no-op if missing permissions)
-- create extension if not exists "pgcrypto";
-- create extension if not exists "uuid-ossp";

create table if not exists inspectors (
    id uuid primary key,
    inspector_id varchar(64) not null unique,
    name varchar(128) not null,
    email varchar(255) not null unique,
    password_hash varchar(255) not null,
    totp_secret varchar(64),
    mfa_enabled boolean not null default true,
    created_at timestamptz,
    updated_at timestamptz
);

create table if not exists incidents (
    id uuid primary key,
    inspector_id uuid not null references inspectors(id) on delete cascade,
    title varchar(140) not null,
    description text,
    severity varchar(32),
    status varchar(32),
    latitude double precision,
    longitude double precision,
    occurred_at timestamptz,
    created_by varchar(255),
    created_at timestamptz,
    updated_at timestamptz
);
create index if not exists idx_incident_inspector on incidents(inspector_id);

create table if not exists shifts (
    id uuid primary key,
    inspector_id uuid not null references inspectors(id) on delete cascade,
    start_time timestamptz,
    end_time timestamptz,
    status varchar(64),
    location varchar(255),
    notes text,
    created_by varchar(255),
    created_at timestamptz,
    updated_at timestamptz
);
create index if not exists idx_shift_inspector on shifts(inspector_id);

create table if not exists audit_logs (
    id uuid primary key,
    actor_inspector_id varchar(64) not null,
    action varchar(64) not null,
    entity_type varchar(64),
    entity_id varchar(255),
    timestamp timestamptz not null,
    ip_address varchar(64),
    user_agent varchar(255),
    metadata text
);

