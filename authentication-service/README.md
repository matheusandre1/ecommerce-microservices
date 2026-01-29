# Authentication Service

This is a Quarkus-based microservice for user authentication in an e-commerce system, providing secure user registration, login, and token management.

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL (for production deployment)
- OpenSSL (for generating ECC keys)

## Environment Setup

1. Clone the repository and navigate to the project directory.

2. Generate ECC keys for JWT signing and verification:

   ```shell
   # 1. Generate private key ECC (using prime256v1 curve)
   openssl ecparam -name prime256v1 -genkey -noout -out privateKey.pem

   # 2. Convert private key to PKCS#8 format (required for SmallRye to read the private key correctly)
   openssl pkcs8 -topk8 -nocrypt -in privateKey.pem -out privateKey.pem.pkcs8
   mv privateKey.pem.pkcs8 privateKey.pem

   # 3. Extract public key from private key
   openssl ec -in privateKey.pem -pubout -out publicKey.pem
   ```

3. Copy `.env.example` to `.env` and configure the environment variables:

   - `ADMIN_PASSWORD`: Password for the admin user (automatically created on application startup if not exists).
   - `DB_USERNAME`, `DB_PASSWORD`, `DB_URL`: Database credentials for production (e.g., `jdbc:postgresql://localhost:5432/authdb`).
   - `ENCRYPTION_KEY`: 32-character hexadecimal key for AES encryption (used in `CryptoUtil`).
   - `JWT_KEY_LOCATION`: Absolute path to `privateKey.pem`.
   - `JWT_PUBLIC_KEY_LOCATION`: Absolute path to `publicKey.pem`.

   Example `.env`:
   ```
   ADMIN_PASSWORD=secureAdminPass123
   DB_USERNAME=authuser
   DB_PASSWORD=authpass
   DB_URL=jdbc:postgresql://localhost:5432/authdb
   ENCRYPTION_KEY=30e921e913bc06d7b9e2493fef4a93ac
   JWT_KEY_LOCATION=/path/to/privateKey.pem
   JWT_PUBLIC_KEY_LOCATION=/path/to/publicKey.pem
   ```

## Running the Application

### Development Mode

In development mode, Quarkus automatically starts a PostgreSQL database using DevServices (no manual setup required).

```shell
./mvnw quarkus:dev
```

- The application will be available at `http://localhost:8080`.
- Dev UI is accessible at `http://localhost:8080/q/dev/`.
- Database migrations are applied automatically.

### Production Mode

1. Ensure PostgreSQL is running and configured.
2. Set the environment variables from `.env`.
3. Package and run the application:

   ```shell
   ./mvnw package
   java -jar target/quarkus-app/quarkus-run.jar
   ```

For native executable:

```shell
./mvnw package -Dnative
./target/authentication-service-1.0.0-SNAPSHOT-runner
```

## API Endpoints

The service exposes REST endpoints under `/auth`:

- `POST /auth/register`: Register a new user.
  - Body: `{"email": "user@example.com", "password": "password", "fullName": "User Name"}`
  - Response: User details.

- `POST /auth/login`: Authenticate user and return tokens.
  - Body: `{"email": "user@example.com", "password": "password"}`
  - Response: `{"accessToken": "...", "refreshToken": "..."}`

- `POST /auth/refresh`: Refresh the access token using a refresh token.
  - Body: `{"refreshToken": "refresh-token-here"}`
  - Response: New tokens.

All endpoints return JSON responses. Use the access token in the `Authorization: Bearer <token>` header for authenticated requests.

## Security Best Practices Implemented

This service implements several security best practices to protect user data and prevent common vulnerabilities:

- **Password Hashing**: Uses Argon2 (memory-hard function) with salt, iterations (3), memory (64MB), and parallelism (1) for secure password storage, resistant to brute-force and rainbow table attacks.
- **Data Encryption**: Sensitive JWT claims (email, user_id) are encrypted using AES-GCM with a unique IV per encryption, ensuring confidentiality even if JWT is compromised.
- **JWT Security**: Employs ES256 (ECC-based) for signing, which is more efficient and secure than RSA. Tokens have short expiration (15 minutes) and use opaque subjects to prevent user enumeration.
- **Refresh Tokens**: Stored as SHA-256 hashes for lookup, encrypted in the database. Tokens expire in 7 days, can be revoked, and are cleaned up daily by a scheduled service.
- **Secret Management**: All secrets (keys, passwords) are loaded from environment variables, avoiding hardcoded values.
- **Database Security**: Uses parameterized queries via Hibernate ORM, Flyway migrations for schema management, and PostgreSQL for ACID compliance.
- **Error Handling**: Global exception mapper prevents information leakage by returning generic error messages for security-related exceptions.
- **Logging**: Security events (logins, registrations) are logged at appropriate levels without exposing sensitive data.
- **Input Validation**: Uses Jakarta Validation for request DTOs to prevent injection attacks.
- **Admin User**: Created on startup via environment variable, with hashed password.

## Related Guides

- JDBC Driver - PostgreSQL ([guide](https://quarkus.io/guides/datasource)): Connect to the PostgreSQL database via JDBC
- SmallRye JWT Build ([guide](https://quarkus.io/guides/security-jwt-build)): Create JSON Web Token with SmallRye JWT Build API
- SmallRye Health ([guide](https://quarkus.io/guides/smallrye-health)): Monitor service health
- Logging JSON ([guide](https://quarkus.io/guides/logging#json-logging)): Add JSON formatter for console logging
- Hibernate ORM with Panache ([guide](https://quarkus.io/guides/hibernate-orm-panache)): Simplify your persistence code for Hibernate ORM via the active record or the repository pattern
- RESTEasy Classic ([guide](https://quarkus.io/guides/resteasy)): REST endpoint framework implementing Jakarta REST and more
- SmallRye JWT ([guide](https://quarkus.io/guides/security-jwt)): Secure your applications with JSON Web Token
- Hibernate Validator ([guide](https://quarkus.io/guides/validation)): Validate object properties (field, getter) and method parameters for your beans (REST, CDI, Jakarta Persistence)
- SmallRye OpenTracing ([guide](https://quarkus.io/guides/opentracing)): Trace your services with SmallRye OpenTracing

## Provided Code

### Application Configuration

The Quarkus application configuration is located in `src/main/resources/application.properties`.

### Hibernate ORM

Create your first JPA entity using Panache for simplified ORM operations.

### RESTEasy JAX-RS

Easily start your RESTful Web Services with JAX-RS annotations.

### SmallRye Health

Monitor your application's health using SmallRye Health endpoints.
