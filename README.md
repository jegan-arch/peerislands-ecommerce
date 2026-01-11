# PeerIslands E-commerce Order Service

A production-ready, modular monolith Order Management System built with **Java 21** and **Spring Boot 4**. This project demonstrates "Senior Architect" patterns including clean architecture, optimistic locking, inventory management, and dockerized deployment.

## 🚀 Key Features

* **Modular Monolith Architecture:** Distinct separation between **Order**, **Catalog**, and **Inventory** domains.
* **Robust Inventory Management:** Atomic stock reservation with "Compensating Transaction" support (rollback on failure).
* **Extensible Validation:** Uses the **Strategy Pattern** to create a pluggable validation pipeline (Inventory checks, etc.).
* **Concurrency Control:** Implements **Optimistic Locking** (`@Version`) to handle race conditions between user cancellations and background jobs.
* **Resilient Scheduling:** Background job auto-processes pending orders with "Time Window" protection.
* **Professional API:**
    * **Pagination:** Custom `PagedResponse` wrapper to hide implementation details.
    * **Error Handling:** Global Exception Handler with standardized `ErrorCode` and JSON error responses.

## 🛠️ Tech Stack

* **Language:** Java 21
* **Framework:** Spring Boot 4+ (Web, Data JPA, Validation)
* **Database:** PostgreSQL (Production/Docker) / H2 (Local Dev)
* **Tooling:** Maven, Docker, Docker Compose, Lombok

---

## 🏃‍♂️ How to Run

### Option 1: Local Development (Fastest)
Uses an **In-Memory H2 Database**. No external dependencies required.

1.  **Build and Run:**
    ```bash
    mvn clean spring-boot:run
    ```

2.  **Access H2 Console:**
    * **URL:** [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
    * **JDBC URL:** `jdbc:h2:mem:testdb`
    * **User:** `sa`
    * **Password:** `password`

### Option 2: Dockerized (Production-Like)
Uses **PostgreSQL** and includes **pgAdmin** for database management.

1.  **Build & Start Containers:**
    ```bash
    docker-compose up --build
    ```
    *(Note: The first run might take a minute to download the Java 21 images)*

2.  **Access Services:**
    * **API:** [http://localhost:8080/api/orders](http://localhost:8080/api/orders)
    * **pgAdmin (DB GUI):** [http://localhost:5050](http://localhost:5050)

3.  **Setup pgAdmin Connection:**
    * **Login:** `admin@admin.com` / `root`
    * **Right Click Servers > Register > Server...**
    * **Host:** `postgres-db` (Important! Do not use localhost)
    * **Port:** `5432`
    * **Username:** `postgres` / **Password:** `password`
    * **Database:** `ecommerce_db`

---

## 🧪 API Documentation

The API enforces `X-User-Id` header for basic multi-tenancy simulation.

### 1. Create Order
**POST** `/api/orders`

**Headers:**
`X-User-Id: customer-1`

**Body:**
```json
{
  "items": [
    {
      "productId": "PROD-3",
      "quantity": 1
    },
    {
      "productId": "PROD-1",
      "quantity": 2
    }
  ]
}
```

### 2. Get All Orders (Paginated)
**GET** `/api/orders?page=0&size=5`

**Headers:**
`X-User-Id: customer-1`

**Response:**
```json
{
    "content": [
        {
            "id": "23a546a7-58ca...",
            "customerId": "customer-1",
            "status": "PENDING",
            "items": [...],
            "createdAt": "2026-01-11T16:06:12"
        }
    ],
    "page": 0,
    "size": 5,
    "totalElements": 1,
    "totalPages": 1,
    "last": true
}
```

### 3. Cancel Order
**POST** `/api/orders/{uuid}/cancel`

**Headers:**
`X-User-Id: customer-1`

*Note: Orders can only be cancelled within **5 minutes** of creation and if they are still `PENDING`.*

---

## 🏗️ Architecture Decisions

### 1. Catalog vs. Inventory Split
Instead of a single "Product Service," we split concerns:
* **Catalog Service:** Handles static data (Names, Prices).
* **Inventory Service:** Handles dynamic state (Stock Counts).
* **Benefit:** Allows the Order Service to orchestrate the transaction—fetching price from Catalog and reserving stock from Inventory atomically.

### 2. Validation Strategy
We use a `List<OrderValidator>` injected into the Service.
* **Current Validator:** `InventoryValidator` (Checks stock).
* **Extensibility:** New rules (e.g., `FraudValidator`, `MaxQuantityValidator`) can be added as new classes without modifying the core `OrderServiceImpl`.

### 3. Data Consistency
* **Stock Reservation:** Done *before* the DB save. If the DB save fails, a `catch` block triggers a "Release Stock" action to maintain consistency.
* **Optimistic Locking:** The `version` field on `OrderEntity` prevents the "Double Update" problem where a user cancels an order at the exact moment the Scheduler tries to ship it.

---

## 📝 Pre-loaded Data

The system starts with the following in-memory data for testing:

| Product ID | Name | Price | Stock |
| :--- | :--- | :--- | :--- |
| `PROD-1` | Wireless Mouse | $25.00 | 100 |
| `PROD-2` | Mechanical Keyboard | $150.00 | 50 |
| `PROD-3` | iPhone 15 | $999.00 | 2 |

---

## ⚠️ Troubleshooting

**"Fatal error compiling: error: release version 21 not supported"**
* Ensure your `Dockerfile` uses `maven:3.9.6-eclipse-temurin-21` and `eclipse-temurin:21-jre-alpine`.
* Ensure your local Maven is running on JDK 21.

**Scheduler not processing orders?**
* The job is configured to process orders **older than 5 minutes**. Wait 5 minutes or modify `OrderProcessingJob.java` for testing.
