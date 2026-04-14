# 🛒 Event-Driven CQRS E-Commerce Analytics System

A production-ready, highly-scalable e-commerce analytics system demonstrating the **Command Query Responsibility Segregation (CQRS)** pattern utilizing **Apache Kafka** and **Kafka Streams**.

![Architecture](https://img.shields.io/badge/Architecture-Event--Driven%20CQRS-blue.svg)
![Java](https://img.shields.io/badge/Java-17%2B-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-brightgreen.svg)
![Kafka](https://img.shields.io/badge/Kafka-Streams-black.svg)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14-blue.svg)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED.svg)

---

## 🌟 Architecture Overview

The system is strictly divided into two distinct microservices:

1. **🛠️ Command Service (Write Model) - Port 8080** 
   - Handles state-changing requests (creating products, creating orders, and updating statuses).
   - Persists data to a PostgreSQL database.
   - Publishes domain events (`ProductCreated`, `OrderCreated`, `OrderUpdated`) to Kafka topics.

2. **📊 Query Service (Read Model) - Port 8081** 
   - Optimized for intensive read queries.
   - Subscribes to the Kafka event streams using **Kafka Streams API**.
   - Enriches the data (joining Order Items with Product details).
   - Continuously aggregates sales and revenue in real-time.
   - Aggregations are kept in locally materialized view state stores, accessible with sub-millisecond latency via an Interactive Query API.

### 🗺️ System Architecture Diagram

```mermaid
graph TD
    subgraph Client_Layer [Client Applications]
        Client(fa:fa-user Client)
    end

    subgraph Command_Side [Command Service - Port 8080]
        CS[Command API Controller]
        CR[Repository]
        EP[Event Producer]
        DB[(PostgreSQL)]
    end

    subgraph Messaging_Layer [Apache Kafka]
        PT[[product-events]]
        OT[[order-events]]
    end

    subgraph Query_Side [Query Service - Port 8081]
        AT[Analytics Topology]
        QS[Analytics Query API]
        subgraph Materialized_Views [State Stores]
            SS1[(Product Sales)]
            SS2[(Category Revenue)]
            SS3[(Hourly Sales)]
        end
    end

    %% Command Flow
    Client -- "1. Send Command" --> CS
    CS -- "2. Persist" --> CR
    CR --> DB
    CS -- "3. Produce Events" --> EP
    EP --> PT
    EP --> OT

    %% Query Flow (Streaming)
    PT -- "4. Stream/Process" --> AT
    OT -- "4. Stream/Process" --> AT
    AT -- "5. Materialize View" --> Materialized_Views

    %% Read Flow
    Client -- "6. Query Analytics" --> QS
    QS -- "7. Local Lookups" --> Materialized_Views
```

---

## 🚀 Technologies Used

- **Java 17**
- **Spring Boot 3.x**
- **Apache Kafka** & **Kafka Streams**
- **PostgreSQL**
- **Docker** & **Docker Compose**
- **Maven**
- **JaCoCo** (Test Coverage)

---

## ⚙️ Prerequisites

Before you begin, ensure you have met the following requirements:
- **Docker & Docker Compose** installed
- **Java 17+** (if running locally without Docker)
- **Maven 3.8+** (if running locally without Docker)

---

## 🛠️ Setup & Execution Process

### 1. Clone the Repository
```bash
git clone https://github.com/your-username/EventDrivenCQRS_E-commerce.git
cd EventDrivenCQRS_E-commerce
```

### 2. Configure the Environment
An example environment file exists at `.env.example`. You can simply copy it to `.env` or configure your own values if running outside of Docker.

```bash
cp .env.example .env
```

### 3. Start the Infrastructure & Services
You can spin up the entire cluster (Zookeeper, Kafka, Postgres DB, Command Service, and Query Service) using Docker Compose:

```bash
docker-compose up --build -d
```
> **Note:** The `db` PostgreSQL container is seeded automatically upon startup from `seeds/init.sql`. The `command-service` waits for the database to become healthy before attempting connection.

### 4. Verify Applications
You can check the logs to ensure everything is running smoothly:
```bash
docker-compose logs -f command-service
docker-compose logs -f query-service
```

---

## 📡 API Endpoints

### 🛠️ Command API (`http://localhost:8080/api`)

| Method | Endpoint                    | Description                          | Published Event        |
| ------ | --------------------------- | ------------------------------------ | ---------------------- |
| `POST` | `/products`                 | Create a new product.                | `ProductCreated`       |
| `POST` | `/orders`                   | Create a new composite order.        | `OrderCreated`         |
| `PUT`  | `/orders/{id}/status`       | Update order status (e.g. "PAID").   | `OrderUpdated`         |

### 📊 Analytics Query API (`http://localhost:8081/api/analytics`)

| Method | Endpoint                             | Description                                      | Used Kafka Store           |
| ------ | ------------------------------------ | ------------------------------------------------ | -------------------------- |
| `GET`  | `/products/{productId}/sales`        | Returns aggregate total sales for a given item.  | `product-sales-store`      |
| `GET`  | `/categories/{categoryName}/revenue` | Returns total cumulative revenue by category.    | `category-revenue-store`   |
| `GET`  | `/hourly-sales`                      | Returns tumbling 1-hour window revenue slices.   | `hourly-sales-store`       |
| `GET`  | `/topology`                          | Inspect raw Kafka Streams computational graph.   | N/A                        |

---

## 🧪 Testing Coverage

The project includes an exhaustive test suite targeting the business logic and the Kafka streams topology (using `TopologyTestDriver`). JaCoCo enforces and measures the line coverage.

To execute the test suite locally:
```bash
mvn clean verify
```
*(You will find the JaCoCo coverage reports under `command-service/target/site/jacoco/index.html` and `query-service/target/site/jacoco/index.html`)*

---

## 🛠️ Monitoring & Health Checks

Both services include **Spring Boot Actuator** for real-time monitoring:

- **Command Service Health**: `http://localhost:8080/actuator/health`
- **Query Service Health**: `http://localhost:8081/actuator/health`

**Key Health Indicators:**
- **Database**: PostgreSQL connectivity (Command Service).
- **Kafka**: connectivity to the broker ecosystem.
- **Kafka Streams**: Custom indicator tracking if the topology is in `RUNNING` or `REBALANCING` state (Query Service).

---

## 🏗️ Configuration Details & Reliability

1. **Exactly-Once Semantics (EOS):** Configured via `processing.guarantee=exactly_once_v2` ensuring each message is processed strictly once and only once across the aggregations during partial failures.
2. **PostgreSQL JSONB Types:** The `OrderItems` are highly normalized JSON blocks preventing rigid multi-table joins on the write model.
3. **Interactive Queries:** Uses `ReadOnlyKeyValueStore` and `ReadOnlyWindowStore` avoiding remote network roundtrips.

---

<p align="center">Made with ❤️</p>
