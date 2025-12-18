# E-commerce Backend System (Phase 1: Core & Security)

> **Status:** Phase 1 Completed (Foundation, Advanced Security, Database Architecture)  
> **Author:** Phan Đình Minh (Minzetsu)  
> **Last Updated:** December 18, 2025

## Overview
This repository contains a robust, scalable backend for an E-commerce platform built with **Spring Boot 3** and **Spring Security 6**.
Phase 1 focuses on a solid architecture foundation, advanced security (JWT + Refresh Token + RBAC), and a MySQL schema managed by Liquibase.

## Tech Stack
- **Core:** Java 17, Spring Boot 3.x
- **Security:** Spring Security 6, JWT (jjwt), BCrypt, RBAC
- **Database:** MySQL 8.0
- **Migration:** Liquibase (XML changelogs)
- **Docs:** OpenAPI 3.0 (Swagger UI)
- **Build:** Maven Wrapper

## Security Architecture (Phase 1 highlight)
### Authentication (JWT + Refresh Token)
- **Access Token (short-lived):** used for protected resources
- **Refresh Token (long-lived):** stored in DB, used to renew access token without re-login

### Authorization (RBAC)
Access control is enforced at:
1. **Network layer:** CORS configuration
2. **URL layer (SecurityConfig):**
   - `/api/public/**` → public storefront APIs
   - `/api/auth/**` → authentication APIs
   - `/api/users/**` → requires `ROLE_USER`
   - `/api/admin/**` → requires `ROLE_ADMIN`
3. **Method layer:** `@PreAuthorize(...)` on controllers/services

## How to Run
### Prerequisites
- JDK 17+
- MySQL 8.0

### Steps
1. Configure database in `backend/src/main/resources/application.properties`
2. Run the application:
   - `cd backend`
   - `./mvnw spring-boot:run`
3. Open Swagger UI:
   - `http://localhost:8080/docs` (or `http://localhost:8080/swagger-ui/index.html`)
   - OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Next Steps (Phase 2: Frontend)
- Setup React + routing + UI system
- Build storefront (products/categories/banners) on `/api/public/**`
- Build user flows (auth/cart/checkout/orders/payments) on `/api/auth/**` + `/api/users/me/**`
- Build admin dashboard on `/api/admin/**`

