# E-commerce System Development Plan

## Overview
This document outlines the roadmap for the development of the E-commerce system, from core backend construction to final deployment.

## Phase 1: Core Backend & Security Foundation
**Status: Completed**
**Focus:** Building the fundamental data structures, business logic, API endpoints, and securing the system.

*   [x] **Database Design & Setup**: Schema design for Users, Products, Orders, Carts, etc. Implementation using Liquibase for version control.
*   [x] **Entity & Relationship Mapping**: JPA/Hibernate entities configuration.
*   [x] **Core Modules Implementation**:
    *   [x] Authentication (Login/Register).
    *   [x] Product Management (CRUD, Categories, Inventory).
    *   [x] Shopping Cart & Checkout Logic.
    *   [x] Order Management & History.
    *   [x] User Profile & Address Management.
    *   [x] Promotion System (Vouchers, Banners).
    *   [x] Review & Rating System.
*   [x] **Home Module**: Aggregation API for the homepage (Banners, New Arrivals, Best Sellers).
*   [x] **System Hardening**:
    *   [x] **Global Exception Handling**: `@ControllerAdvice`, Custom Exceptions.
    *   [x] **Input Validation**: JSR-303 annotations (`@NotNull`, `@Size`) on DTOs.
*   [x] **Advanced Security (Current Focus)**:
    *   Refine JWT implementation (Refresh Tokens).
    *   Role-Based Access Control (RBAC) audit.
    *   CORS configuration.
*   [x] **Phase 1 Review**:
    *   Verify all core APIs and Security mechanisms.
    *   Ensure database data integrity.

## Phase 2: Frontend Development (Early Integration)
**Status: Completed**
**Focus:** Building the User Interface to validate Backend APIs and visualize the system flow. This helps identify redundant endpoints or missing data early.

*   [x] **Setup & Architecture**:
    *   [x] Initialize React project (Vite + React + TypeScript).
    *   [x] Setup UI foundation (TailwindCSS + shadcn/ui).
    *   [x] Setup routing + base layout (M2).
    *   [x] Setup API Client (Fetch) + typed errors.
    *   [x] Defer server-state (React Query) + client-state (Zustand) to Phase 3.
*   [x] **Customer Storefront (User View)**:
    *   [x] Home Page (Banners, Product Lists + pagination).
    *   [x] Product Search & Detail Pages (filters + pagination + detail view).
    *   [x] Reviews UX (create multiple reviews, inline edit/delete, instant rating updates).
    *   [x] Category Browse & Detail Pages (subcategories + product list pagination).
    *   [x] Authentication (Login/Register + token refresh).
    *   [x] Cart & Checkout Flow.
    *   [x] User Profile & Address Book (CRUD + set default).
    *   [x] User Order History.
*   [x] **Admin Dashboard (Management View)**:
    *   [x] Product & Category Management.
    *   [x] Order Processing.
    *   [x] User Management.
    *   [x] Marketing Management (Banners, Vouchers, Voucher Uses).
    *   [x] Inventory Management (Warehouses, Inventories).
    *   [x] Notifications + Reviews (Admin view).
    *   [x] Admin Profile routes (under `/admin/profile`).
*   [x] **API Namespace Convention**:
    *   Public storefront APIs under `/api/public/**`.
    *   Auth APIs under `/api/auth/**`.
    *   User APIs under `/api/users/me/**`.
    *   Admin APIs under `/api/admin/**`.
*   [x] **Feedback Loop**:
    *   Refine Backend APIs based on Frontend requirements (e.g., missing fields in DTOs, pagination adjustments).
*   [x] **Phase 2 Review**:
    *   Manual testing of full user journeys (Register -> Search -> Add to Cart -> Checkout).
    *   Verify UI responsiveness and error display.
    *   [x] Frontend docs: `frontend/README.md` + endpoint coverage summary.
    *   [x] Responsive audit (mobile/tablet): admin tables scroll + admin mobile navigation.

## Phase 3: Performance, Scalability & Reliability
**Status: Completed (M0-M7)**
**Focus:** Measurable latency improvements and stability under load. Backend-first.

Primary reference: `PHASE3_ROADMAP.md` (authoritative checklist and milestones).

*   [x] **Define SLOs + Benchmarks** (see `PHASE3_ROADMAP.md`):
    *   Pick critical endpoints and capture baseline p95/error/throughput.
*   [x] **Caching Strategy**:
    *   Integrate Redis + Spring Cache, define key/TTL/invalidation table.
*   [x] **Database Optimization**:
    *   Slow query report + index list + N+1 reductions.
    *   [x] Reduce redundant queries in user-mixed flow (cart/order creation, inventory updates).
    *   [x] Review list N+1 fixes (product/user fetch) + batch cleanup on user delete.
    *   [x] Add composite indexes for cart/order/review/wishlist/recent view/search log hot paths.
*   [x] **Async Processing**:
    *   Offload non-critical tasks (notifications, cleanup, exports) if needed.
*   [x] **Resilience Hardening**:
    *   Rate limiting middleware + timeouts/retries for external calls.
*   [x] **Observability Foundation**:
    *   Request ID + structured logs + Actuator metrics.
    *   Audit log for admin/user critical actions.
*   [x] **Phase 3 Review**:
    *   Before/after benchmark report + cache hit rate + slow query summary.
    *   Report template: `docs/perf/phase3_report.md`.

## Phase 4: Advanced Features & Integrations
**Status: Completed**
**Focus:** Integrations + realtime + search; introduce messaging and event-driven flows.
**Finalization Note:** HTTP caching policy for public APIs is finalized (fresh reviews/ratings + efficient cache for stable endpoints), and chatbot microphone flow is finalized to voice-to-chat response mode.

*   [x] **Payment Gateway Integration**:
    *   Integrate VNPay / Momo / Stripe / PayPal.
    *   Handle IPN/Webhook callbacks securely (signature verification + replay protection).
    *   Idempotency for payment initiation and callbacks.
    *   Idempotency keys for order/payment creation (prevent duplicate submits).
    *   Payment state machine (PENDING/PAID/FAILED/CANCELED/REFUNDED) + audit log.
*   [x] **Message Broker (RabbitMQ/Kafka)**:
    *   Publish domain events (OrderCreated, PaymentSucceeded, InventoryLow).
    *   Consumers for notifications/email/analytics.
    *   DLQ + retry policy.
*   [x] **Advanced Search Engine**:
    *   Integrate Elasticsearch for full-text search, fuzzy matching, faceting.
    *   Indexing pipeline (backfill + incremental updates) + fallback when ES is unavailable.
*   [x] **Real-time Features**:
    *   WebSocket/SSE for order status updates and admin notifications.
*   [x] **New Product Propagation (Realtime + Cache + Search)**:
    *   Publish PRODUCT_CREATED/UPDATED events to broker.
    *   Consumers update search index and invalidate product/home caches.
    *   Optional WS/SSE push for "new arrivals" on storefront.
*   [x] **Chatbot Integration**:
    *   Customer support assistant (FAQ, order status lookup, refund policy).
    *   Conversation history with auto-title from first user message (replace generic "General Chat N").
    *   Conversation management: rename, delete history, copy message content.
    *   Workspace model: chat groups + projects (create/update/delete) with isolated chat threads by project.
    *   Email notifications for group invites and invite-accepted events (in addition to in-app notifications).
    *   Multimodal I/O (current): read file content + voice input + VN<->EN translation (image analysis removed in local mode for performance/stability).
    *   Escalation flow to human support + conversation logging.
    *   Safety guardrails (rate limit, input validation, prompt injection defense).
    *   Email verification (OTP) for registration before account activation.
    *   LLM provider integration + cost/latency monitoring.
    *   RAG over product catalog, policy docs, and order data (scoped by auth).
    *   Admin tools: prompt templates, knowledge base sync, and audit logs.
    *   Current code location: backend `backend/src/main/java/com/minzetsu/ecommerce/chatbot/*`, frontend widget `frontend/src/app/ChatbotWidget.tsx` + API client `frontend/src/lib/chatbotApi.ts`.
    *   Group collaboration UX completed: owner/member roles, invite by email, accept/refuse flow, owner invite result notifications, invite badge count sync, member list (including owner), and sender name shown in group messages.
    *   Registration security completed: email OTP verification before account creation (1-minute OTP expiry) with user-friendly error responses.
*   [x] **Anonymous Cart + Merge on Login**:
    *   Guest cart persisted by device/session.
    *   Merge guest cart into user cart on login (conflict resolution rules).
*   [x] **Inventory Reservation TTL + Release**:
    *   Reserve stock during checkout with expiration.
    *   Release inventory on timeout/cancel/fail.
*   [x] **HTTP Caching for Public APIs**:
    *   Add Cache-Control/ETag for home, product list/detail, and category public endpoints.
    *   Apply conditional GET (If-None-Match) to reduce payload and improve repeat-read latency.
*   [x] **Phase 4 Review**:
    *   Sandbox Testing: Verify payment flows (Success/Failure/Cancel).
    *   Verify search accuracy and relevance.
    *   Verify event delivery + retry/DLQ behavior.
    *   Verify HTTP caching behavior (cache headers + 304 responses).

## Phase 5: Advanced Data & Quality
**Status: Completed (Feature Track + Testing Completed)**
**Focus:** Deepen database capabilities + testing + reliability.

*   [x] **Advanced Database**:
    *   Isolation levels, lock analysis, deadlock handling.
    *   Replication (read replica) + read/write split (optional).
    *   Backup/restore drill and data retention policy.
    *   **Scope note:** This phase validates data-layer correctness/recoverability in engineering environments; full production DR operations are handled in Phase 7.
*   [x] **Audit Log Hardening**:
    *   Retention policy (TTL/archive) + scheduled cleanup.
    *   Mask sensitive fields (PII/token/password) in logs.
    *   Admin audit log search/viewer endpoint + filters.
*   [x] **NoSQL Expansion**:
    *   [x] Redis advanced usage (streams/pubsub).
    *   [x] MongoDB analytics/log sink (implemented: clickstream events, chatbot transcripts, audit event archive).
*   [x] **Checkout Enhancements**:
    *   Guest checkout (no account required).
    *   Guest secure re-open flow (access token + guest order page + guest MoMo path).
    *   Fraud/abuse detection (basic rules).
    *   Multi-currency + tax/shipping rules.
*   [x] **Testing Suite**:
    *   Unit tests (pricing/discounts, inventory reservation, order totals, RBAC).
    *   Integration tests (`MockMvc` + TestContainers for DB/Redis).
    *   E2E tests (Playwright/Cypress): register/login, browse/search, cart, checkout, admin flows.
    *   API contract tests (consumer/provider) to keep frontend-backend schema aligned.
    *   Security tests: auth bypass, rate-limit abuse, OTP brute-force, token misuse scenarios.
*   [x] **Security & Supply Chain Quality**:
    *   SAST + dependency vulnerability scanning (OWASP/dependency-check style).
    *   Secret scanning in CI and commit hooks.
    *   DAST smoke checks for public endpoints.
*   [x] **Migration Safety**:
    *   Liquibase rollback strategy per release.
    *   Migration rehearsal on production-like data volume.
*   [x] **API Documentation**:
    *   Finalize Swagger/OpenAPI + examples + error model.
*   [x] **Service Level Objectives & Reliability Policy**:
    *   Define production SLI/SLO/SLA per critical flow (auth, checkout, payment callback, order status).
    *   Error budget policy + on-call alert thresholds + escalation matrix.
    *   **Scope note:** This phase defines policy/targets; implementing platform alert rules and paging integrations is part of Phase 7.
*   [x] **Architecture Decision Records (ADR)**:
    *   Capture monolith-first decision, service boundaries, and criteria to split to microservices.
    *   Map decisions to current constraints (team size, ops overhead, delivery speed).
*   [x] **Phase 5 Review**:
    *   Data reliability report (backup/restore, replication checks).
    *   Test coverage summary.
    *   SLO/SLA and ADR documents approved and versioned.

## Phase 6: Data Reliability & Analytics Serving
**Status: Pending**
**Focus:** Build new analytics capabilities (data mart + ETL + analytics APIs) on top of existing Phase 5 foundations.

*   [ ] **Event Contract Standardization**:
    *   Standardize event taxonomy for core funnel: `VIEW_PRODUCT`, `ADD_TO_CART`, `PLACE_ORDER`, `PAYMENT_SUCCESS`.
    *   Ensure required fields: `eventType`, `eventTime`, `requestId`, `userId/guestId`, `source`, `productId` (when applicable).
    *   Add schema/version notes for backward-compatible event evolution (analytics scope only).
*   [ ] **Analytics Data Mart (MySQL)**:
    *   Create `daily_product_metrics` (date, product, views, add-to-cart, orders, unique users, conversion rate).
    *   Define indexing strategy and retention policy for analytics tables.
*   [ ] **ETL Batch from Mongo Sink to MySQL**:
    *   Scheduled daily aggregation from clickstream sink to analytics mart.
    *   Idempotent rerun strategy (deterministic upsert/recompute) to avoid double counting.
*   [ ] **Analytics ETL Quality Controls**:
    *   Add checks for null keys, duplicate metric keys, and missing date partitions.
    *   Add fail-fast policy for critical quality violations and warning policy for non-critical issues in ETL jobs.
*   [ ] **Analytics Serving APIs**:
    *   Admin endpoints for funnel and top products by conversion.
    *   Add short-TTL cache for high-frequency analytics reads.
*   [ ] **Data Job Observability**:
    *   Add ETL-specific metrics: duration, processed events, dropped events, failure count.
    *   Add ETL-specific alert thresholds for repeated failures or stale data windows.
    *   **Scope note:** Focus on data-pipeline telemetry quality for analytics workloads; global observability stack integration is finalized in Phase 7.
*   [ ] **Data Pipeline Tests & Contracts**:
    *   Unit tests for aggregation and conversion logic.
    *   Integration tests for ETL happy path + rerun idempotency + analytics API contracts.
*   [ ] **Phase 6 Review**:
    *   Demo end-to-end flow: event emission -> Mongo sink -> ETL -> MySQL mart -> admin analytics API.
    *   Publish data reliability summary (freshness, correctness, rerun safety).

## Phase 7: DevOps, Observability & Scale
**Status: Pending**
**Focus:** Production platform readiness for the full system (including the new Phase 6 analytics flows).

*   [ ] **Containerization**:
    *   Dockerfile for Spring Boot.
    *   docker-compose for full stack (App, MySQL, Redis, Elasticsearch, broker).
*   [ ] **CI/CD Pipeline**:
    *   GitHub Actions or Jenkins for build/test.
    *   Staging deploy + smoke tests, then promote to production.
    *   Blue-green or canary strategy.
    *   Release gates: migration check, API contract check, security scan check.
    *   Add lightweight periodic load test on staging to catch latency regressions before production.
*   [ ] **Observability Stack**:
    *   Centralized logging (ELK/Loki).
    *   Metrics monitoring (Prometheus + Grafana).
    *   Platform-level alerts on SLO burn, DB pool exhaustion, cache failures, and broker pressure.
    *   **Scope note:** This phase operationalizes and centralizes signals already introduced earlier (Phase 3 app observability baseline + Phase 6 ETL telemetry).
*   [ ] **Web Server & Reverse Proxy**:
    *   Nginx/Caddy for TLS termination + routing.
*   [ ] **Scaling & Reliability**:
    *   Horizontal scaling, load balancer, autoscaling (optional).
    *   Rate limiting + backpressure validation under load.
    *   Disaster recovery drill (RTO/RPO targets), backup verification, rollback playbook.
    *   Incident runbook for cache outage, broker outage, payment webhook delay/failure, and DB failover.
    *   **Scope note:** This is production-operations DR/readiness (runbooks, RTO/RPO execution), not a repeat of Phase 5 data validation drills.
*   [ ] **Experimentation**:
    *   Feature flags + A/B testing rollout support.
*   [ ] **Secrets & Environments**:
    *   Separate dev/staging/prod configs and secrets management.
*   [ ] **Cost & Capacity Observability**:
    *   Cost dashboards/alerts for Redis, Elasticsearch, broker, outbound integrations, and analytics workload growth.
    *   Capacity thresholds + autoscaling trigger runbooks.
