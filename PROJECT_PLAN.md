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
**Status: In Progress**
**Focus:** Building the User Interface to validate Backend APIs and visualize the system flow. This helps identify redundant endpoints or missing data early.

*   [ ] **Setup & Architecture**:
    *   [x] Initialize React project (Vite + React + TypeScript).
    *   [x] Setup UI foundation (TailwindCSS + shadcn/ui).
    *   [x] Setup routing + base layout (M2).
    *   [x] Setup API Client (Fetch) + typed errors.
    *   [ ] Setup server-state (React Query) + client-state (Zustand).
*   [ ] **Customer Storefront (User View)**:
    *   [x] Home Page (Banners, Product Lists + pagination).
    *   [x] Product Search & Detail Pages (filters + pagination + detail view).
    *   [x] Category Browse & Detail Pages (subcategories + product list pagination).
    *   [x] Authentication (Login/Register + token refresh).
    *   Cart & Checkout Flow.
    *   User Profile & Order History.
*   [ ] **Admin Dashboard (Management View)**:
    *   Product & Category Management.
    *   Order Processing.
    *   User Management.
*   [ ] **API Namespace Convention**:
    *   Public storefront APIs under `/api/public/**`.
    *   Auth APIs under `/api/auth/**`.
    *   User APIs under `/api/users/me/**`.
    *   Admin APIs under `/api/admin/**`.
*   [ ] **Feedback Loop**:
    *   Refine Backend APIs based on Frontend requirements (e.g., missing fields in DTOs, pagination adjustments).
*   [ ] **Phase 2 Review**:
    *   Manual testing of full user journeys (Register -> Search -> Add to Cart -> Checkout).
    *   Verify UI responsiveness and error display.

## Phase 3: Performance & Scalability
**Status: Pending**
**Focus:** Optimizing response times and handling high traffic.

*   [ ] **Caching Strategy**:
    *   Integrate **Redis**.
    *   Cache heavy read operations: Homepage data, Product details, Category trees.
    *   Implement Cache Eviction policies.
*   [ ] **Database Optimization**:
    *   Analyze and optimize slow queries (N+1 problem).
    *   Add necessary Indexes.
    *   Connection Pooling configuration (HikariCP).
*   [ ] **Asynchronous Processing**:
    *   Offload non-critical tasks (Sending Emails, Push Notifications) to background threads or Message Queues (RabbitMQ/Kafka).
*   [ ] **Phase 3 Review**:
    *   Benchmark response times (Before vs After Caching).
    *   Stress test critical endpoints (Home, Product Detail) to ensure stability under load.

## Phase 4: Advanced Features & Integrations
**Status: Pending**
**Focus:** Adding value-added features and external connections.

*   [ ] **Payment Gateway Integration**:
    *   Integrate VNPay / Momo / Stripe / PayPal.
    *   Handle IPN (Instant Payment Notification) callbacks securely.
*   [ ] **Advanced Search Engine**:
    *   Integrate **Elasticsearch** for full-text search, fuzzy matching, and faceting.
*   [ ] **Real-time Features**:
    *   WebSocket implementation for Order Status updates and Chat support.
*   [ ] **Phase 4 Review**:
    *   Sandbox Testing: Verify Payment Gateway flows (Success/Failure/Cancel).
    *   Verify Search accuracy and relevance.

## Phase 5: Comprehensive QA & Pre-release Testing
**Status: Pending**
**Focus:** Verifying system correctness and reliability.

*   [ ] **Unit Testing**: Write JUnit/Mockito tests for critical Service logic (Order calculation, Inventory checks).
*   [ ] **Integration Testing**: Test API endpoints using `MockMvc` or TestContainers.
*   [ ] **Load Testing**: Simulate high traffic using JMeter or k6.
*   [ ] **API Documentation**: Finalize Swagger/OpenAPI specs for Frontend developers.

## Phase 6: DevOps & Deployment
**Status: Pending**
**Focus:** Automating delivery and managing infrastructure.

*   [ ] **Containerization**:
    *   Create `Dockerfile` for the Spring Boot application.
    *   Create `docker-compose.yml` for the full stack (App, MySQL, Redis, etc.).
*   [ ] **CI/CD Pipeline**:
    *   Setup GitHub Actions or Jenkins for automated building and testing.
*   [ ] **Monitoring & Logging**:
    *   Centralized logging (ELK Stack or Loki).
    *   Metrics monitoring (Prometheus + Grafana).
