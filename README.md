# E-Commerce Microservices

A scalable, cloud-native e-commerce backend built with **Java 21** and **Quarkus**, featuring a microservices architecture managed by **Consul** for service discovery and **Kafka** for event-driven communication.

## 🏗 Architecture Overview

The system is composed of loose-coupled microservices that communicate synchronously via REST APIs and asynchronously via Kafka messaging.

### Services

| Service | Technology | Port (Default) | Description |
| :--- | :--- | :--- | :--- |
| **Authentication Service** | Quarkus, PostgreSQL, Redis | `8080` | Manages identity, JWT issuance (ECC), registration, and RBAC. |
| **Order Service** | Quarkus, PostgreSQL | `8082`* | Manages order lifecycle, status updates, and shipping calculations. |
| **Product Service** | Quarkus, MongoDB, Redis | `8083`* | Manages product catalog, stock levels, and category listings. |
| **Notification Service** | Quarkus, Kafka | `8081`* | Consumes events (e.g., `order-created`) to send emails (Welcome, Order Confirmation). |

*(Note: Ports are illustrative based on standard Quarkus assignments; assume conflicts if all run on 8080 without configuration overrides. Check `application.properties` for exact values).*

## 🚀 Getting Started

### Prerequisites

- **Java 21+** (JDK)
- **Maven 3.8+**
- **Docker** & **Docker Compose**
- **OpenSSL** (for generating JWT security keys)

### Setup & Configuration

1. **Clone the repository:**
   ```bash
   git clone <repository-url>
   cd ecommerce-microservices
   ```

2. **Generate Security Keys (Required for Auth):**
   The Authentication Service requires ECC keys for signing JWTs.
   ```bash
   cd authentication-service
   # Run the OpenSSL commands as described in authentication-service/README.md
   cd ..
   ```

3. **Infrastructure:**
   The system relies on a robust infrastructure stack:
   - **PostgreSQL**: Relational data (Auth, Orders).
   - **MongoDB**: Document data (Products).
   - **Redis**: Caching (Products, Auth tokens).
   - **Apache Kafka**: Event bus (`order-created`, `product-updated`, etc.).
   - **Consul**: Service Discovery.

## 🐳 Running with Docker

### Sequential Startup (Recommended for Development)

Building multiple Quarkus services in **native mode** simultaneously is extremely resource-intensive and may cause your system to freeze. To prevent this, use the provided sequential startup script:

```bash
# To build and start everything (Sequential build)
./sequential-up.sh --build

# To just start the containers (Sequential start)
./sequential-up.sh up
```

This script starts infrastructure first, waits for stabilization, and then builds/starts each app one by one.

### Standard Management

```bash
# Stop all services
docker-compose down

# Stop all services and remove data volumes (Clean state)
docker-compose down -v

# View logs for a specific service
docker-compose logs -f order-service
```

### Running Services Individually (Maven)

If you prefer to run services manually for debugging:

**Authentication Service:**
```bash
cd authentication-service
./mvnw quarkus:dev
```

**Product Service:**
```bash
cd product-service
./mvnw quarkus:dev
```

**Order Service:**
```bash
cd order-service
./mvnw quarkus:dev
```

**Notification Service:**
```bash
cd notification-service
./mvnw quarkus:dev
```

## 📡 Event-Driven Architecture (Kafka)

The services exchange messages on several topics:

- **`order-created`**: Triggered by Order Service -> Consumed by Product Service (Stock Update) & Notification Service (Email).
- **`product-created` / `product-updated`**: Triggered by Product Service.
- **`stock-changed`**: Triggered by Product Service.
- **`order-status-changed`**: Triggered by Order Service on updates/cancellations.

## 🛠 Tech Stack

- **Framework:** Quarkus (Super-fast Subatomic Java)
- **Language:** Java 21
- **Databases:** PostgreSQL, MongoDB
- **Caching:** Redis
- **Messaging:** Apache Kafka
- **Service Discovery:** HashiCorp Consul
- **Security:** SmallRye JWT (ECC), Argon2
- **Containerization:** Docker

## 🤝 Contribution

1. Fork the project.
2. Create your feature branch (`git checkout -b feature/AmazingFeature`).
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`).
4. Push to the branch (`git push origin feature/AmazingFeature`).
5. Open a Pull Request.
