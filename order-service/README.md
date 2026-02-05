# Order Service

This is the order-service microservice for the e-commerce platform, built with Quarkus. It manages the lifecycle of customer orders, including creation, status updates, and cancellations. **The service implements the Transactional Outbox Pattern** using Debezium CDC for guaranteed event delivery to Kafka.

## 🎯 Key Features

- ✅ **Transactional Outbox Pattern** - Guaranteed event delivery with Debezium CDC
- ✅ **PostgreSQL** with logical replication for Change Data Capture
- ✅ **Flyway** database migrations
- ✅ **Automatic outbox cleanup** - Scheduled job to remove old events
- ✅ **Observability** - Health checks, metrics, distributed tracing
- ✅ **At-least-once delivery** - No event loss even if Kafka is unavailable

## Tech Stack

- **Framework**: Quarkus 3.30.6
- **Database**: PostgreSQL with Hibernate ORM Panache
- **Messaging**: Kafka via Debezium Outbox Pattern (CDC)
- **Migrations**: Flyway
- **Validation**: Hibernate Validator
- **Observability**: SmallRye Health, Micrometer Prometheus, OpenTelemetry
- **Build**: Maven

## Prerequisites

- Java 21+
- Maven 3.6+
- Docker & Docker Compose (for Kafka Connect + Debezium)

## Architecture

```
Order Service (Quarkus)
    ↓ (Transactional Write)
PostgreSQL (orderdb)
    ├─→ orders table
    ├─→ order_items table
    └─→ outbox table
         ↓ (CDC - Change Data Capture)
Debezium Connector (Kafka Connect - Port 8084)
    ↓ (EventRouter SMT)
Kafka Topic: Order.events
    ↓ (Consume)
├─→ Notification Service (Email/Discord)
└─→ Product Service (Stock management)
```

## Business Rules

### Order Lifecycle

- Orders start in **PENDING** status upon creation.
- Statuses: PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED.
- Total amount is calculated as the sum of item subtotals plus shipping cost.
- Orders can be updated to any status, but cancellations are restricted:
  - Cannot cancel a **DELIVERED** order.
  - Cannot cancel an already **CANCELLED** order.

### Validation

- Customer name: 3-100 characters, required.
- Customer email: Valid email format, required.
- Items: At least one item required.
- Product name: 3-200 characters.
- Quantity: Minimum 1.
- Unit price: Greater than 0.

## API Endpoints

All endpoints return JSON responses. Use `Content-Type: application/json` for POST/PUT requests.

### Create Order

- **POST** `/orders`
- Body: `CreateOrderRequest`
- Creates a new order, calculates total, and **persists OrderCreated event in outbox table**
- Event is captured by Debezium and published to `Order.events` Kafka topic

### List All Orders

- **GET** `/orders`
- Returns a list of orders without items.

### Get Order by ID

- **GET** `/orders/{id}`
- Returns full order details including items.

### Get Orders by Status

- **GET** `/orders/status/{status}`
- Status: PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED

### Get Orders by Customer Email

- **GET** `/orders/customer/{email}`

### Update Order Status

- **PUT** `/orders/{id}/status`
- Body: `{"status": "CONFIRMED"}`
- **Persists OrderStatusChanged event in outbox table**
- Event is captured by Debezium and published to `Order.events` Kafka topic

### Cancel Order

- **PATCH** `/orders/{id}/cancel`
- **Persists OrderStatusChanged event in outbox table**
- Event is captured by Debezium and published to `Order.events` Kafka topic

## Event-Driven Architecture

### Outbox Pattern Implementation

This service uses the **Transactional Outbox Pattern** for reliable event publishing:

1. **Event Persistence**: Events are written to the `outbox` table in the same transaction as business data
2. **CDC (Change Data Capture)**: Debezium connector monitors the outbox table for changes
3. **Event Publishing**: Debezium publishes events to Kafka topics via EventRouter SMT
4. **Automatic Cleanup**: Scheduled job removes events older than 7 days

### Kafka Topic

**Topic**: `Order.events` (unified topic for all Order events)

**Event Types**:
- `OrderCreated` - Published when a new order is created
- `OrderStatusChanged` - Published on status updates or cancellations

### OrderCreated Event

Published when a new order is created.

```json
{
  "orderId": 1,
  "customerName": "João Silva",
  "customerEmail": "joao@example.com",
  "status": "PENDING",
  "totalAmount": 100.00,
  "shippingCost": 10.00,
  "items": [
    {
      "productId": "prod-123",
      "productName": "Produto Exemplo",
      "quantity": 2,
      "unitPrice": 50.00,
      "subtotal": 100.00
    }
  ],
  "createdAt": "2023-10-01T10:00:00"
}
```

### OrderStatusChanged Event

Published on status updates or cancellations.

```json
{
  "orderId": 1,
  "customerEmail": "joao@example.com",
  "oldStatus": "PENDING",
  "newStatus": "CONFIRMED",
  "changedAt": "2023-10-01T10:05:00"
}
```

## Database Schema

### orders
- id (BIGINT, PK)
- customer_name (VARCHAR(255))
- customer_email (VARCHAR(255))
- status (VARCHAR(50))
- total_amount (DECIMAL(10,2))
- shipping_cost (DECIMAL(10,2))
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)

### order_items
- id (BIGINT, PK)
- product_id (VARCHAR(255))
- product_name (VARCHAR(255))
- quantity (INTEGER)
- unit_price (DECIMAL(10,2))
- order_id (BIGINT, FK to orders.id)

### outbox (Transactional Outbox Pattern)
- id (BIGINT, PK)
- aggregate_type (VARCHAR(255)) - Entity type (e.g., "Order")
- aggregate_id (VARCHAR(255)) - Entity ID (used as Kafka message key)
- event_type (VARCHAR(255)) - Event type (e.g., "OrderCreated")
- payload (TEXT) - JSON serialized event data
- created_at (TIMESTAMP) - Event creation timestamp

**Note**: The `outbox` table uses **snake_case** naming via Hibernate's `CamelCaseToUnderscoresNamingStrategy`.

Indexes: customer_email, status, order_id, product_id, created_at (outbox).

## Configuration

### Key Properties

**application.properties** contains:

- **Database**: PostgreSQL with logical replication support (`wal_level=logical`)
- **Hibernate ORM**: Physical naming strategy (camelCase → snake_case)
- **Flyway**: Database migrations including outbox table creation
- **Outbox Pattern**: Events persisted transactionally with business data
- **Observability**: Health checks, Prometheus metrics, OpenTelemetry tracing

### Environment Variables (Production)

```bash
QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://postgres:5432/orderdb
QUARKUS_DATASOURCE_USERNAME=admin
QUARKUS_DATASOURCE_PASSWORD=admin123
KAFKA_BOOTSTRAP_SERVERS=kafka:9092  # Not used directly (Debezium handles Kafka)
```

## Running the Application

### Dev Mode (with Dev Services)

```bash
./mvnw compile quarkus:dev
```

Dev Services automatically starts:
- PostgreSQL (with logical replication)
- Kafka + Zookeeper

### Docker Compose (Recommended)

```bash
# From project root
cd ..
docker-compose up -d order-service
```

**Important**: Code changes in native builds require rebuild:
```bash
docker-compose stop order-service
docker-compose rm -f order-service
docker-compose build order-service
docker-compose up -d order-service
```

### Production

```bash
./mvnw package
java -jar target/quarkus-app/quarkus-run.jar
```

### Native Build

```bash
./mvnw package -Dnative
./target/order-service-1.0.0-SNAPSHOT-runner
```

## Observability

### Health and Metrics

- Health: `GET /q/health` (port 8081)
- Metrics: `GET /q/metrics` (Prometheus format)
- Tracing: Jaeger UI at http://localhost:16686

### Monitoring the Outbox Pattern

```bash
# View outbox events
docker exec -it ecommerce-postgres psql -U admin -d orderdb \
  -c "SELECT id, event_type, aggregate_id, created_at FROM outbox ORDER BY created_at DESC LIMIT 10;"

# Check Debezium connector status
curl http://localhost:8084/connectors/order-service-outbox-connector/status

# View Kafka messages
docker exec -it ecommerce-kafka kafka-console-consumer \
  --bootstrap-server kafka:9092 \
  --topic Order.events \
  --from-beginning
```

## Error Handling

Handled via `GlobalExceptionMapper`:
- **400 Bad Request** - Validation errors, illegal operations
- **404 Not Found** - Order not found
- **500 Internal Server Error** - Unexpected errors

All errors return JSON:
```json
{
  "error": "Order not found with id: 123",
  "timestamp": "2026-02-05T10:00:00"
}
```

## Important Notes

### Consumer Requirements

Consumers of `Order.events` topic must handle **double-encoded JSON**:

```java
// Debezium sends: "{\"orderId\":1,\"customerName\":\"Test\"...}"
// Needs double parse:
JsonNode node = objectMapper.readTree(message);
if (node.isTextual()) {
    node = objectMapper.readTree(node.asText());
}
```

This is automatically handled in:
- ✅ Notification Service (`OrderEventConsumer`)
- ✅ Product Service (`OrderEventConsumer`)

### Debezium Configuration

The `topic.prefix: "outbox"` is **required** by Debezium but **ignored** by the Outbox Event Router SMT, which creates the topic as `Order.events` (without prefix).
