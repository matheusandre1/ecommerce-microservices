-- Create sequence for users
CREATE SEQUENCE users_seq START WITH 1 INCREMENT BY 50;

-- Create users table
CREATE TABLE users (
    id BIGINT NOT NULL DEFAULT nextval('users_seq'),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'CUSTOMER',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

-- Link sequence to table
ALTER SEQUENCE users_seq OWNED BY users.id;
