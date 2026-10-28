# Deployment guide

## Required configuration

| Variable | Purpose |
| --- | --- |
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC URL |
| `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD` | Database credentials |
| `JWT_SECRET` | Base64-encoded secret; use at least 256 bits of random key material |
| `JWT_EXPIRATION_MS` | Access-token lifetime in milliseconds |
| `APP_CORS_ALLOWED_ORIGIN` | Exact deployed frontend origin |
| `APP_BULK_QUANTITY_THRESHOLD` | Fallback bulk quantity when no database setting exists |
| `APP_BOOTSTRAP_ADMIN_EMAIL` / `APP_BOOTSTRAP_ADMIN_PASSWORD` | Optional first-admin bootstrap; use a 12+ character password and remove after initial setup |
| `VITE_API_BASE_URL` | Public API origin used during frontend build |

Never commit production credentials. Prefer the hosting provider's secret manager and a managed PostgreSQL instance with TLS and automated backups.

## Backend

Build with `cd backend && mvn clean package`. Deploy the generated JAR or use the included Dockerfile. Configure the database and secrets in the hosting environment. Flyway applies schema changes at startup; keep `spring.jpa.hibernate.ddl-auto=validate` enabled.

Provide a production CORS origin, strong randomly generated JWT secret, and non-default database credentials. Restrict database ingress to the API service. Enable HTTPS at the platform edge and configure structured logs/alerts before accepting orders.

The initial migration includes a demo service branch at Bengaluru coordinates (12.971600, 77.594600) with a 25 km radius. Replace or deactivate it and configure real branch addresses, coordinates, radius, and priority before accepting customer orders.

## Frontend

Build with `cd frontend && npm ci && npm run build`. Deploy `frontend/dist` to Vercel, Cloudflare Pages, or equivalent. Set `VITE_API_BASE_URL` at build time and configure the host to serve `index.html` for SPA routes.

## Release checks

- Confirm Flyway completes against an empty production-like database.
- Create the first administrator through the one-time bootstrap variables, then unset them.
- Verify `/actuator/health`, Swagger access policy, CORS, JWT expiry, and admin-only endpoints.
- Test an order inside and outside configured branch radii and exercise both approval outcomes.
- Verify PostgreSQL backups and restore procedures before production launch.
