# API Monitoring Demo (Hackathon)
## Quick Start

- Requirements: Docker and Docker Compose
- Start everything:
  - `docker compose up --build`
- Services brought up:
  - Postgres on `5432`
  - Monitoring Backend on `http://localhost:8080`
  - Demo App on `http://localhost:9090`

When the containers are healthy, open Swagger for the backend: `http://localhost:8080/swagger-ui.html`.

## Generate Some Traffic (Demo App)

Hit a few demo endpoints to create data. Examples:

- `curl -i http://localhost:9090/api/users`
- `curl -i http://localhost:9090/api/products`
- `curl -i http://localhost:9090/api/orders`

Optional create/update to vary statuses and payloads:

- `curl -i -X POST http://localhost:9090/api/users -H 'Content-Type: application/json' -d '{"name":"Alice"}'`

The SDK in the demo app intercepts each request/response, truncates large bodies, and posts a record to the backend.

## View Analytics (Backend)

Backend API is protected by an API key filter. Use header `X-API-Key: hackathon-2025-super-secret-key`.

Project ID used by the demo: `demo-project-001`.

- Latest request per unique path:
  - `curl -s -H 'X-API-Key: hackathon-2025-super-secret-key' "http://localhost:8080/api/requests/list?projectId=demo-project-001" | jq .`

- Paginated table of all requests (with filters):
  - `curl -s -H 'X-API-Key: hackathon-2025-super-secret-key' "http://localhost:8080/api/requests/table?projectId=demo-project-001&page=0&size=10&sortBy=createdAt&sortDirection=DESC" | jq .`

- Health metrics per endpoint (latest snapshot per endpoint):
  - `curl -s -H 'X-API-Key: hackathon-2025-super-secret-key' "http://localhost:8080/api/health-metrics/list?projectId=demo-project-001" | jq .`

- Health metrics for a specific endpoint path:
  - `curl -s -H 'X-API-Key: hackathon-2025-super-secret-key' "http://localhost:8080/api/health-metrics/endpoint?projectId=demo-project-001&endpoint=/api/users" | jq .`

Expected fields include response time stats (avg/min/max), success/error counts, success rate, and a simple health score.

## Configuration

- Demo app SDK settings (defaults wired for local):
  - `monitoring.enabled`: `true`
  - `monitoring.api-key`: `hackathon-2025-super-secret-key`
  - `monitoring.project-id`: `demo-project-001`
  - `monitoring.backend-url`: `http://localhost:8080/api` (outside Docker) or `http://backend:8080/api` (inside Compose)
  - `monitoring.async`: `true`

When using Docker Compose, the demo app is configured via env vars in `docker-compose.yml`.

- Backend DB: Postgres with schema auto-update and data persisted in the `postgres_data` volume.

To reset local data: `docker compose down -v` and then `docker compose up --build`.

## Local Development (without Docker)

- Requirements: Java 17 and Maven, plus a Postgres instance
- Start Postgres (example):
  - `docker run --rm -p 5432:5432 -e POSTGRES_DB=monitoring_db -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres postgres:15-alpine`
- Build all modules:
  - `mvn clean install`
- Run backend:
  - `mvn -pl monitoring-backend spring-boot:run`
- In a second terminal, run the demo app:
  - `mvn -pl demo-application spring-boot:run`

If you run the demo outside Docker, ensure its `monitoring.backend-url` points to `http://localhost:8080/api`.

## Tests

- Run unit tests for all modules: `mvn test`
- Or per module: `mvn -pl monitoring-backend test` / `mvn -pl monitoring-sdk test`

## What’s Inside

- `monitoring-sdk`: Auto-configured servlet filter that captures method, path, headers, bodies (truncated), status, response time; posts to backend.
- `monitoring-backend`: Spring Boot API with Postgres; stores requests and maintains simple health metrics per endpoint.
- `demo-application`: Small REST app using the SDK to generate sample traffic.

## Troubleshooting

- 401/403 from backend: include `X-API-Key: hackathon-2025-super-secret-key`.
- No analytics showing: hit the demo endpoints first to generate traffic.
- Demo can’t reach backend in Docker: ensure `MONITORING_BACKEND_URL` is `http://backend:8080/api` (it is set in Compose).
