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
**Status: Pending**
**Focus:** Measurable latency improvements and stability under load. Backend-first.

Primary reference: `PHASE3_ROADMAP.md` (authoritative checklist and milestones).

*   [ ] **Define SLOs + Benchmarks** (see `PHASE3_ROADMAP.md`):
    *   Pick critical endpoints and capture baseline p95/error/throughput.
*   [ ] **Caching Strategy**:
    *   Integrate Redis + Spring Cache, define key/TTL/invalidation table.
*   [ ] **Database Optimization**:
    *   Slow query report + index list + N+1 reductions.
*   [ ] **Async Processing**:
    *   Offload non-critical tasks (notifications, cleanup, exports) if needed.
*   [ ] **Resilience Hardening**:
    *   Rate limiting middleware + timeouts/retries for external calls.
*   [ ] **Observability Foundation**:
    *   Request ID + structured logs + Actuator metrics.
*   [ ] **Phase 3 Review**:
    *   Before/after benchmark report + cache hit rate + slow query summary.

## Phase 4: Advanced Features & Integrations
**Status: Pending**
**Focus:** Adding value-added features and external connections.

*   [ ] **Payment Gateway Integration**:
    *   Integrate VNPay / Momo / Stripe / PayPal.
    *   Handle IPN/Webhook callbacks securely (signature verification + replay protection).
    *   Idempotency for payment initiation and callbacks.
    *   Payment state machine (PENDING/PAID/FAILED/CANCELED/REFUNDED) + audit log.
*   [ ] **Advanced Search Engine**:
    *   Integrate **Elasticsearch** for full-text search, fuzzy matching, and faceting.
    *   Indexing pipeline (backfill + incremental updates) and fallback when ES is unavailable.
*   [ ] **Real-time Features**:
    *   WebSocket for order status updates and admin notifications (chat can be deferred unless needed).
*   [ ] **Phase 4 Review**:
    *   Sandbox Testing: Verify Payment Gateway flows (Success/Failure/Cancel).
    *   Verify Search accuracy and relevance.

## Phase 5: Comprehensive QA & Pre-release Testing
**Status: Pending**
**Focus:** Verifying system correctness and reliability.

*   [ ] **Unit Testing**: JUnit/Mockito for critical Service logic (pricing/discounts, inventory reservation, order totals, RBAC rules).
*   [ ] **Integration Testing**: `MockMvc` + TestContainers (DB/Redis); verify auth flows and security boundaries.
*   [ ] **E2E Testing**:
    *   Playwright/Cypress flows: register/login, browse/search, cart, checkout, order history, admin product edit.
*   [ ] **Load Testing**: Simulate high traffic using JMeter or k6, aligned with Phase 3 SLOs.
*   [ ] **API Documentation**: Finalize Swagger/OpenAPI specs + examples + consistent error model.
*   [ ] **Release Readiness**:
    *   Migration dry-run, backup/restore drill, and rollback plan.

## Phase 6: DevOps & Deployment
**Status: Pending**
**Focus:** Automating delivery and managing infrastructure.

*   [ ] **Containerization**:
    *   Create `Dockerfile` for the Spring Boot application.
    *   Create `docker-compose.yml` for the full stack (App, MySQL, Redis, etc.).
    *   Add staging/prod compose variants or overlays as needed.
*   [ ] **CI/CD Pipeline**:
    *   Setup GitHub Actions or Jenkins for build/test.
    *   Staging deploy + smoke tests, then promote to production.
*   [ ] **Monitoring & Logging**:
    *   Centralized logging (ELK Stack or Loki).
    *   Metrics monitoring (Prometheus + Grafana).
    *   Alerts on SLO burn (latency/error spikes), DB pool exhaustion, cache failures.
*   [ ] **Secrets & Environments**:
    *   Separate dev/staging/prod configs and secrets management strategy.
