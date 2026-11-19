-- CloudShare Database Schema - V1
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    display_name VARCHAR(100),
    password_hash VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE files (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    original_name VARCHAR(255) NOT NULL,
    storage_key VARCHAR(512) UNIQUE NOT NULL,
    content_type VARCHAR(150),
    size_bytes BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL, -- PENDING (URL issued) | READY (upload confirmed) | DELETED
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE share_links (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    file_id UUID NOT NULL REFERENCES files(id) ON DELETE CASCADE,
    token VARCHAR(64) UNIQUE NOT NULL,
    password_hash VARCHAR(100) NULL,
    expires_at TIMESTAMPTZ NULL,
    max_downloads INT NULL,
    download_count INT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE download_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    share_link_id UUID NOT NULL REFERENCES share_links(id) ON DELETE CASCADE,
    file_id UUID NOT NULL REFERENCES files(id) ON DELETE CASCADE,
    downloaded_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    ip_hash VARCHAR(64),
    country_code VARCHAR(2),
    country_name VARCHAR(100),
    city VARCHAR(100),
    browser VARCHAR(80),
    os VARCHAR(80),
    device_type VARCHAR(20),
    referrer VARCHAR(512)
);

-- Indexes
CREATE INDEX idx_files_owner ON files(owner_id);
CREATE INDEX idx_links_file ON share_links(file_id);
CREATE INDEX idx_events_file_time ON download_events(file_id, downloaded_at);
CREATE INDEX idx_events_link_time ON download_events(share_link_id, downloaded_at);
