# E-commerce System (Phase 4)

> **Branch:** `phase4`  
> **Status:** Phase 4 Completed (Advanced Integrations)  
> **Author:** Phan Dinh Minh (Minzetsu)  
> **Last Updated:** January 31, 2026

## Overview
This repository is an end-to-end E-commerce system:
- **Backend:** Spring Boot 3 + Spring Security 6 (JWT + Refresh Token + RBAC), Liquibase, MySQL
- **Integrations (Phase 4):** RabbitMQ events, Elasticsearch product search, MoMo payment (sandbox), SSE realtime
- **Reliability:** Idempotency keys for order/payment
- **Observability:** Request ID + structured logs + audit logs
- **Frontend (Phase 2+4):** React + TypeScript (see `frontend/`), integrating with backend APIs, SSE, MoMo

## API Namespace Convention
- Public storefront: `/api/public/**`
- Auth: `/api/auth/**`
- User (requires `ROLE_USER`): `/api/users/me/**`
- Admin (requires `ROLE_ADMIN`): `/api/admin/**`

## How to Run (Local)
### Prerequisites
- JDK 17+
- Node.js 18+ (recommended)
- MySQL 8.0
- RabbitMQ (Phase 4)
- Elasticsearch (Phase 4)
- Redis (cache)

### Backend
1. Configure services in `backend/src/main/resources/application.properties`
2. Run:
   - `cd backend`
   - `./mvnw spring-boot:run`
3. Swagger:
   - `http://localhost:8080/docs` (or `http://localhost:8080/swagger-ui/index.html`)
   - OpenAPI: `http://localhost:8080/v3/api-docs`

### Frontend
1. Configure API base URL:
   - `frontend/.env` -> `VITE_API_BASE_URL=http://localhost:8080`
2. Run:
   - `cd frontend`
   - `npm install`
   - `npm run dev`
3. Open: `http://localhost:5173`

## Roadmap
- Phase 3 roadmap (performance/caching/reliability): `PHASE3_ROADMAP.md`
- Phase 4 roadmap (integrations/realtime/search/chatbot): `PHASE4_ROADMAP.md`
- Phase 3 report: `docs/perf/phase3_report.md`
- High-level project plan: `PROJECT_PLAN.md`
