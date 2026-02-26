# ORDER MANAGEMENT API (Spring Boot + PostgreSQL)
A production-style RESTful backend built with Spring Boot for managing users, orders, products, and inventory with relational modeling and business validation.

## Tech Stack
- Java 21
- Spring Boot
- Spring Data JPA
- PostgreSQL
- Docker

## Data Model
```markdown
This ER diagram represents the relational data model for the Order Management API.

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