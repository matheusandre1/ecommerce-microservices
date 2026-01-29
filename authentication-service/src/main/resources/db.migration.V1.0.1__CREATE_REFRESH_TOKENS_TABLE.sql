CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    hashed_token VARCHAR(255) UNIQUE NOT NULL,
    encrypted_token TEXT NOT NULL,
    user_id BIGINT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE
);
