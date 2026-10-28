# Sirji Homecare project guidance

- Keep the project single-tenant. Do not add tenant IDs or tenant routing.
- Use Java 21, Spring Boot 3, Maven, PostgreSQL, JPA, and Flyway in `backend/`.
- Use React, TypeScript, Vite, Tailwind CSS, React Router, and Axios in `frontend/`.
- Keep controllers thin; put business rules in services and expose DTOs, not JPA entities.
- All order requests start in `PENDING_APPROVAL`; only an ADMIN can approve or reject them.
- Calculate service eligibility against active branches using the Haversine formula and each branch's radius.
- Bulk order tagging uses the quantity threshold in `site_settings`, defaulting to 50 when unset.
- Validate request DTOs, hash credentials with BCrypt, use JWT for authenticated APIs, and never commit secrets.
- Apply schema changes only through Flyway migrations. Use UTC timestamps and audit fields.
- Add focused unit or integration tests for new business rules and API contracts.
