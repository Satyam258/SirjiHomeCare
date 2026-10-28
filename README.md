# Sirji Homecare

A single-tenant homecare catalogue and order-management application. The monorepo contains a React/Vite frontend and a Java 21/Spring Boot API backed by PostgreSQL.

## Included foundation

- Public product catalogue with category filters, search, and order enquiries.
- Customer order requests with branch-radius validation and mandatory admin approval.
- Bulk-order tagging based on a configurable quantity threshold (default: 50).
- Admin JWT sign-in and protected order-management APIs.
- Product, feedback, authentication, tracking, and administration API foundations.
- Flyway schema, OpenAPI UI, ERD, and deployment notes.

## Prerequisites

- Node.js 20.19+ or 22.12+ and npm.
- Java 21 and Maven 3.9+.
- Docker Desktop for local PostgreSQL, or a PostgreSQL 16 database.

## Local development

1. Start PostgreSQL: `docker compose up -d db`.
2. Start the API: `cd backend` then `mvn spring-boot:run`.
3. In another terminal, start the frontend: `cd frontend`, `npm install`, then `npm run dev`.
4. Open the Vite URL shown in the terminal (normally `http://localhost:5173`).
5. API health: `http://localhost:8080/actuator/health`.
6. API documentation: `http://localhost:8080/swagger-ui.html`.

The frontend reads `VITE_API_BASE_URL` (defaults to `http://localhost:8080`); use `frontend/.env.example` as its local environment template. The backend reads its database and JWT settings from environment variables; defaults are set for local Compose database access. `.env.example` is a reference file and is not loaded automatically by Spring Boot. Replace the `JWT_SECRET` placeholder with a random base64 key of at least 32 bytes before starting the API (PowerShell: `$bytes = New-Object byte[] 32; [Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes); [Convert]::ToBase64String($bytes)`). Set `APP_BOOTSTRAP_ADMIN_EMAIL` and `APP_BOOTSTRAP_ADMIN_PASSWORD` in the API process environment to create the first administrator; the password must be at least 12 characters. Never use development credentials in production.

## Useful commands

- Frontend checks: `cd frontend && npm run build && npm run lint`
- Backend tests: `cd backend && mvn test`
- Start database only: `docker compose up -d db`
- Stop local database: `docker compose down`

See [docs/erd.md](docs/erd.md) for the initial data model and [docs/deployment.md](docs/deployment.md) for environment and deployment guidance.
Import [postman/Sirji Homecare API.postman_collection.json](postman/Sirji%20Homecare%20API.postman_collection.json) into Postman for the API workflow examples.
