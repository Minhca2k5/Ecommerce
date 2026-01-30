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

*   [X] **Define SLOs + Benchmarks** (see `PHASE3_ROADMAP.md`):
    *   Pick critical endpoints and capture baseline p95/error/throughput.
*   [X] **Caching Strategy**:
    *   Integrate Redis + Spring Cache, define key/TTL/invalidation table.
*   [X] **Database Optimization**:
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
**Status: Pending**
**Focus:** Integrations + realtime + search; introduce messaging and event-driven flows.

*   [ ] **Payment Gateway Integration**:
    *   Integrate VNPay / Momo / Stripe / PayPal.
    *   Handle IPN/Webhook callbacks securely (signature verification + replay protection).
    *   Idempotency for payment initiation and callbacks.
    *   Idempotency keys for order/payment creation (prevent duplicate submits).
    *   Payment state machine (PENDING/PAID/FAILED/CANCELED/REFUNDED) + audit log.
*   [ ] **Message Broker (RabbitMQ/Kafka)**:
    *   Publish domain events (OrderCreated, PaymentSucceeded, InventoryLow).
    *   Consumers for notifications/email/analytics.
    *   DLQ + retry policy.
*   [ ] **Advanced Search Engine**:
    *   Integrate Elasticsearch for full-text search, fuzzy matching, faceting.
    *   Indexing pipeline (backfill + incremental updates) + fallback when ES is unavailable.
*   [ ] **Real-time Features**:
    *   WebSocket/SSE for order status updates and admin notifications.
*   [ ] **New Product Propagation (Realtime + Cache + Search)**:
    *   Publish PRODUCT_CREATED/UPDATED events to broker.
    *   Consumers update search index and invalidate product/home caches.
    *   Optional WS/SSE push for "new arrivals" on storefront.
*   [ ] **Chatbot Integration**:
    *   Customer support assistant (FAQ, order status lookup, refund policy).
    *   Escalation flow to human support + conversation logging.
    *   Safety guardrails (rate limit, input validation, prompt injection defense).
    *   LLM provider integration + cost/latency monitoring.
    *   RAG over product catalog, policy docs, and order data (scoped by auth).
    *   Admin tools: prompt templates, knowledge base sync, and audit logs.
*   [ ] **Phase 4 Review**:
    *   Sandbox Testing: Verify payment flows (Success/Failure/Cancel).
    *   Verify search accuracy and relevance.
    *   Verify event delivery + retry/DLQ behavior.

## Phase 5: Advanced Data & Quality
**Status: Pending**
**Focus:** Deepen database capabilities + testing + reliability.

*   [ ] **Advanced Database**:
    *   Isolation levels, lock analysis, deadlock handling.
    *   Replication (read replica) + read/write split (optional).
    *   Backup/restore drill and data retention policy.
*   [ ] **Audit Log Hardening**:
    *   Retention policy (TTL/archive) + scheduled cleanup.
    *   Mask sensitive fields (PII/token/password) in logs.
    *   Admin audit log search/viewer endpoint + filters.
*   [ ] **NoSQL Expansion**:
    *   Redis advanced usage (streams/pubsub).
    *   One document DB (MongoDB) for logs/analytics (optional).
*   [ ] **Testing Suite**:
    *   Unit tests (pricing/discounts, inventory reservation, order totals, RBAC).
    *   Integration tests (`MockMvc` + TestContainers for DB/Redis).
    *   E2E tests (Playwright/Cypress): register/login, browse/search, cart, checkout, admin flows.
*   [ ] **API Documentation**:
    *   Finalize Swagger/OpenAPI + examples + error model.
*   [ ] **Phase 5 Review**:
    *   Data reliability report (backup/restore, replication checks).
    *   Test coverage summary.

## Phase 6: DevOps, Observability & Scale
**Status: Pending**
**Focus:** Production readiness, deployment, and scaling.

*   [ ] **Containerization**:
    *   Dockerfile for Spring Boot.
    *   docker-compose for full stack (App, MySQL, Redis, Elasticsearch, broker).
*   [ ] **CI/CD Pipeline**:
    *   GitHub Actions or Jenkins for build/test.
    *   Staging deploy + smoke tests, then promote to production.
    *   Blue-green or canary strategy.
*   [ ] **Observability Stack**:
    *   Centralized logging (ELK/Loki).
    *   Metrics monitoring (Prometheus + Grafana).
    *   Alerts on SLO burn, DB pool exhaustion, cache failures.
*   [ ] **Web Server & Reverse Proxy**:
    *   Nginx/Caddy for TLS termination + routing.
*   [ ] **Scaling & Reliability**:
    *   Horizontal scaling, load balancer, autoscaling (optional).
    *   Rate limiting + backpressure validation under load.
*   [ ] **Secrets & Environments**:
    *   Separate dev/staging/prod configs and secrets management.
