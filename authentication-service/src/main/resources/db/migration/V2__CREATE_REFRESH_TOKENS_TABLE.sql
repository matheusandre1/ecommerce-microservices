-- Create sequence for refresh_tokens
CREATE SEQUENCE refresh_tokens_seq START WITH 1 INCREMENT BY 50;

-- Create refresh_tokens table
CREATE TABLE refresh_tokens (
    id BIGINT NOT NULL DEFAULT nextval('refresh_tokens_seq'),
    hashed_token VARCHAR(255) UNIQUE NOT NULL,
    encrypted_token TEXT NOT NULL,
    user_id BIGINT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id)
);

-- Link sequence to table
ALTER SEQUENCE refresh_tokens_seq OWNED BY refresh_tokens.id;