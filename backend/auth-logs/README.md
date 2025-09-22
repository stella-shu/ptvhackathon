Myki Inspector — Auth & Logs Backend

Overview
- Java 17 + Spring Boot 3
- PostgreSQL storage with JPA/Hibernate
- JWT auth with password (OTP/TOTP removed)
- Incident and Shift REST APIs (create, update, fetch)
- Audit trail persisted for key actions

Quick Start
1) Configure the database via env vars (optional defaults shown):
   - `DB_URL=jdbc:postgresql://localhost:5432/inspector`
   - `DB_USERNAME=postgres`
   - `DB_PASSWORD=postgres`
   - `JWT_SECRET` (base64-encoded 256-bit key; change for prod)
2) Build and run:
   - `./mvnw spring-boot:run` or `mvn spring-boot:run`
3) On first boot, a bootstrap inspector is created:
   - Inspector ID: `INSPECTOR1`
   - Password: `ChangeMe123!`
   - OTP is disabled; sign in using inspectorId + password only.

Docker Postgres
- Bring up Postgres locally: `docker compose up -d db`
- Connection defaults match `application.yml` (`inspector / postgres / postgres`).

Database Migrations (Flyway)
- Flyway runs automatically on startup.
- Initial schema: `src/main/resources/db/migration/V1__init.sql`.
- JPA DDL is set to `validate` to ensure schema consistency.

API Docs (Swagger/OpenAPI)
- Visit `/swagger-ui.html` (or `/swagger-ui/index.html`).
- Click “Authorize” and enter `Bearer <JWT>` to try secured endpoints.

Auth
- POST `/api/auth/login` { inspectorId, password }
  - Returns: `{ token, inspectorId, name, email }`
  - Use the `token` as `Authorization: Bearer <token>` for subsequent requests.

Incidents
- POST `/api/incidents` (auth required): create
- PUT `/api/incidents/{id}`: update
- GET `/api/incidents/{id}`: fetch by id
- GET `/api/incidents?mine=true` or no param for all

Shifts
- POST `/api/shifts` (auth required): create
- PUT `/api/shifts/{id}`: update
- GET `/api/shifts/{id}`: fetch by id
- GET `/api/shifts?mine=true` or no param for all

Audit Trail
- Actions recorded: login success/fail, incident create/update, shift create/update.
- Stored in table `audit_logs` with actor, action, target, timestamp, and metadata.

Notes
- For production: configure a strong `JWT_SECRET`, TLS termination, CSRF strategy (if needed), and least-privilege DB credentials. Consider Flyway for schema migrations and finer-grained roles if admins are added.
