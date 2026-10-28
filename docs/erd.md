# Initial ERD

```mermaid
erDiagram
    CATEGORY ||--o{ PRODUCT : groups
    SERVICE_BRANCH ||--o{ CUSTOMER_ORDER : serves
    CUSTOMER_ORDER ||--|{ ORDER_ITEM : contains
    PRODUCT ||--o{ ORDER_ITEM : selected_as
    ADMIN_USER ||--o{ CUSTOMER_ORDER : reviews

    CATEGORY {
        uuid id PK
        string name UK
        string slug UK
        boolean active
        timestamptz created_at
        timestamptz updated_at
    }
    PRODUCT {
        uuid id PK
        uuid category_id FK
        string name
        string slug UK
        text description
        numeric price
        string unit
        string image_url
        boolean active
        timestamptz created_at
        timestamptz updated_at
    }
    SERVICE_BRANCH {
        uuid id PK
        string name
        string address
        numeric latitude
        numeric longitude
        numeric radius_km
        int priority
        boolean active
        timestamptz created_at
        timestamptz updated_at
    }
    CUSTOMER_ORDER {
        uuid id PK
        string order_number UK
        string customer_name
        string customer_phone
        string delivery_address
        numeric latitude
        numeric longitude
        uuid branch_id FK
        string status
        boolean bulk_order
        uuid reviewed_by FK
        text admin_notes
        bigint version
        timestamptz created_at
        timestamptz updated_at
    }
    ORDER_ITEM {
        uuid id PK
        uuid order_id FK
        uuid product_id FK
        int quantity
        numeric unit_price
        timestamptz created_at
        timestamptz updated_at
    }
    ADMIN_USER {
        uuid id PK
        string email UK
        string password_hash
        string role
        boolean active
        timestamptz created_at
        timestamptz updated_at
    }
    SITE_SETTINGS {
        uuid id PK
        string setting_key UK
        string setting_value
        timestamptz created_at
        timestamptz updated_at
    }
    FEEDBACK {
        uuid id PK
        string customer_name
        string email
        int rating
        text message
        boolean approved
        timestamptz created_at
        timestamptz updated_at
    }
```

The initial catalog and a sample service branch are inserted by Flyway. The first admin account is bootstrapped from environment variables and its password is BCrypt-hashed at startup.
