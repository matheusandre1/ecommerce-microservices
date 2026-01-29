CREATE TABLE user_action_tokens (
    id BIGSERIAL PRIMARY KEY,
    hashed_token VARCHAR(255) UNIQUE NOT NULL,
    encrypted_token TEXT NOT NULL,
    user_id BIGINT NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    expires_at TIMESTAMP NOT NULL
);
