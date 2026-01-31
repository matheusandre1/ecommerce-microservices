-- Create sequence for user_action_tokens
CREATE SEQUENCE user_action_tokens_seq START WITH 1 INCREMENT BY 50;

-- Create user_action_tokens table
CREATE TABLE user_action_tokens (
    id BIGINT NOT NULL DEFAULT nextval('user_action_tokens_seq'),
    hashed_token VARCHAR(255) UNIQUE NOT NULL,
    encrypted_token TEXT NOT NULL,
    user_id BIGINT NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id)
);

-- Link sequence to table
ALTER SEQUENCE user_action_tokens_seq OWNED BY user_action_tokens.id;