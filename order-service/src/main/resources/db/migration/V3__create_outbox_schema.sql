-- V3__create_outbox_schema.sql
-- Outbox table for Transactional Outbox Pattern
-- Used by Debezium Connector to capture and publish events to Kafka
CREATE SEQUENCE outbox_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE outbox (
    id BIGINT PRIMARY KEY DEFAULT nextval('outbox_seq'),
    aggregate_type VARCHAR(255) NOT NULL,
    aggregate_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    payload TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL
);

-- Index for efficient queries by creation time (useful for cleanup jobs)
CREATE INDEX idx_outbox_created_at ON outbox(created_at);

