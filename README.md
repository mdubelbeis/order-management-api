# Order Management API

A production-style RESTful backend built with Spring Boot for managing users, orders, products, and inventory with relational modeling and business validation.

---

## 🎯 Project Goals

This project is designed to:

- Demonstrate clean relational modeling using JPA
- Practice layered backend architecture
- Implement real-world business rules (inventory validation, order lifecycle)
- Avoid exposing entities directly via REST
- Incrementally evolve toward production-ready structure

## 🧠 Design Decisions

- OrderItem is modeled as a join entity rather than a direct many-to-many relationship to allow additional attributes (quantity, priceAtPurchase).
- BigDecimal is used for monetary values to avoid floating-point precision errors.
- Foreign key constraints enforce referential integrity.
- Docker is used to isolate database configuration from local machine dependencies.

## 🚀 Current Status (Week 2-DTO Layer Implemented)

✅ Dockerized PostgreSQL  
✅ Spring Boot application running  
✅ JPA domain model implemented  
✅ Relational mappings (User → Order → OrderItem → Product)  
✅ Basic CRUD endpoints (Users, Products, Orders, OrderItems)  
✅ Global exception handling
✅ Standardized API error responses
✅ Bean validation using `jakarta.validation`
✅ Request/Response DTO pattern implemented
✅ Database schema auto-generated and verified  
✅ Health endpoint available

### 🔄 In Progress

- Service-layer business logic (inventory enforcement)
- Order lifecycle rules
- Pagination
- Integration testing

---

## 🧱 Architecture Overview

This project follows a layered architecture:

- **Controller Layer** – Thin REST endpoints
- **Service Layer** – Business logic and validation
- **Repository Layer** – Spring Data JPA persistence
- **Domain Layer** – Entity modeling and relationships
- **DTO Layer** – Controlled request/response models
- **Exception Layer** – Centralized API error handling
- **PostgreSQL** – Relational data store
- **Docker** – Containerized local development database

---

## 🗄 Data Model

```mermaid
erDiagram
  USERS {
    bigint id PK
    varchar email "unique"
    varchar name
    timestamp created_at
  }

  ORDERS {
    bigint id PK
    bigint user_id FK
    varchar status
    timestamp created_at
  }

  ORDER_ITEMS {
    bigint id PK
    bigint order_id FK
    bigint product_id FK
    int quantity
    decimal price_at_purchase
  }

  PRODUCTS {
    bigint id PK
    varchar sku "unique"
    varchar name
    decimal price
    int inventory_qty
  }

  USERS ||--o{ ORDERS : places
  ORDERS ||--o{ ORDER_ITEMS : contains
  PRODUCTS ||--o{ ORDER_ITEMS : referenced_by
```

## 🛠 Tech Stac
k
- Java 21
- Spring Boot
- Spring Data JPA
- PostgreSQL
- Docker
- Maven

## 🧪 Running Locally

1️⃣ Start database
```bash
docker compose up -d
```
2️⃣ Run application
```bash 
./mvnw spring-boot:run
```
3️⃣ Health check
```bash
curl http://localhost:8080/health
```


## 📌 Example Endpoints

- POST  /users
- GET   /users  
- POST  /products
- GET   /products
- POST /orders/user/{userId}
- POST /order-items?orderId=&productId=&quantity=
- GET /orders

## 📦 DTO Example

Create User Request
```json 
{
"email": "user@example.com",
"name": "John Doe"
}
```

User Response
```json
{
  "id": 1,
  "email": "user@example.com",
  "name": "John Doe",
  "createdAt": "2026-02-26T18:10:00Z"
}
```

##  ❗Example Error Response
```json
{
"timestamp": "2026-02-26T18:15:00Z",
"status": 400,
"error": "Bad Request",
"message": "Email already exists: user@example.com",
"path": "/users"
}
```

## 🔜 Planned Enhancements

- Inventory validation rules
- Order lifecycle enforcement
- Pagination support
- Integration testing
- Swagger/OpenAPI documentation