# E-commerce System (Phase 6 Completed, Phase 7 In Progress)

> **Branch:** `phase6`  
> **Status:** Phase 6 Completed (Data Reliability & Hybrid Realtime Analytics Serving), moving to Phase 7 (DevOps, Observability & Scale)  
> **Author:** Phan Dinh Minh (Minzetsu)  
> **Last Updated:** February 16, 2026

## Overview
This repository is an end-to-end E-commerce system:
- **Backend:** Spring Boot 3 + Spring Security 6 (JWT + Refresh Token + RBAC), Liquibase, MySQL
- **Integrations:** RabbitMQ events, Elasticsearch product search, MoMo payment (sandbox), SSE realtime
- **Reliability:** Idempotency keys for order/payment, inventory reservation TTL + release
- **Observability:** Request ID + structured logs + audit logs
- **Customer Experience:** Anonymous cart + merge on login
- **Guest Checkout (Phase 5):** secure guest order access token + guest order tracking + guest MoMo payment
- **Chatbot:** LLM-backed assistant with project/DB context + conversation history
- **MongoDB Analytics/Log Sink (Phase 5):** clickstream events, chatbot transcript archive, audit event archive (Mongo is sink-only; MySQL remains system of record)
- **Analytics Serving (Phase 6):** standardized funnel contract + hybrid serving (Redis realtime counters + daily ETL Mongo -> MySQL mart) + admin analytics APIs (`funnel`, `top-products`) + short-TTL cache + payment-success funnel metric + previous-period comparisons + today snapshot cards + `N/A` trend fallback when previous baseline is zero + attribution warning (`orders > views`)
- **Chatbot Collaboration (Phase 4):** personal/project/group scopes, group invites (accept/refuse), member list + sender display names in group chat
- **Auth Hardening (Phase 4):** email OTP verification for registration
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
1. Configure services in `backend/src/main/resources/application.properties` (or environment variables).
2. Run:
   - `cd backend`
   - `./mvnw spring-boot:run`
3. Swagger:
   - `http://localhost:8080/docs` (or `http://localhost:8080/swagger-ui/index.html`)
   - OpenAPI: `http://localhost:8080/v3/api-docs`

### Deployment-Ready Config (Recommended)
- Use environment variables instead of hardcoded secrets:
  - `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
  - `JWT_SECRET_KEY`
  - `REDIS_HOST`, `REDIS_PORT`
  - `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USERNAME`, `RABBITMQ_PASSWORD`
  - `ELASTICSEARCH_URIS`
  - `MOMO_ACCESS_KEY`, `MOMO_SECRET_KEY`, `MOMO_IPN_URL`, `MOMO_REDIRECT_URL`
  - `MAIL_USERNAME`, `MAIL_PASSWORD`
  - `CHATBOT_BASE_URL`, `CHATBOT_API_KEY`, `CHATBOT_MODEL`
- Use production profile on cloud:
  - `SPRING_PROFILES_ACTIVE=prod`
- If cloud runtime has no local Ollama service, set:
  - `CHATBOT_ENABLED=false` (or point `CHATBOT_BASE_URL` to your AI provider gateway).

### Frontend
1. Configure API base URL:
   - `frontend/.env` -> `VITE_API_BASE_URL=http://localhost:8080`
2. Run:
   - `cd frontend`
   - `npm install`
   - `npm run dev`
3. Open: `http://localhost:5173`

## Database Migration Notes
- Liquibase changelog is managed from `backend/src/main/resources/db/changelog/db.changelog-master.xml`.
- Early files `v1__schema.xml`, `v2__seed.xml`, `v3__indexes.xml` were refactored into modular wrappers with includes:
  - `db/changelog/v1/*.xml`
  - `db/changelog/v2/*.xml`
  - `db/changelog/v3/*.xml`
- Catalog/data expansion is added in `v15__catalog_expansion.xml` (products, product_images, inventory, warehouse allocations, vouchers, notifications, and related records).
- Analytics mart schema is added in `v16__analytics_mart.xml` (`daily_product_metrics` + indexes + constraints).

## Roadmap
- Phase 3 roadmap (performance/caching/reliability): `docs/roadmaps/PHASE3_ROADMAP.md`
- Phase 4 roadmap (integrations/realtime/search/chatbot): `docs/roadmaps/PHASE4_ROADMAP.md`
- Phase 5 roadmap (reliability/security/ops hardening): `docs/roadmaps/PHASE5_ROADMAP.md` 
- Phase 6 roadmap (data reliability + analytics serving): `docs/roadmaps/PHASE6_ROADMAP.md`
- Phase 7 roadmap (devops/observability/scale): `docs/roadmaps/PHASE7_ROADMAP.md`
- High-level project plan (including Phase 6/7): `docs/roadmaps/PROJECT_PLAN.md`
- Data-engineer-aligned backend roadmap: `docs/roadmaps/DATA_ENGINEER_BACKEND_ROADMAP.md`
- Phase 5 ops notes (runbooks + Mongo collections): `docs/ops/PHASE5_OPS.md` 
- Phase 6 ops notes (ETL + serving + observability): `docs/ops/PHASE6_OPS.md`
- Phase 3 report: `docs/perf/phase3_report.md`

## Notes (Current Scope)
- Local mode currently prioritizes chatbot stability: file + voice workflows are enabled; image analysis path is disabled by default in this phase.
- Group invites are designed as in-app first, email as best-effort notification channel.


