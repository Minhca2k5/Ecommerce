# E-commerce System (Backend + Frontend Phase 2)

> **Branch:** `phase2`  
> **Status:** Phase 2 In Progress (Frontend integration)  
> **Author:** Phan Đình Minh (Minzetsu)  
> **Last Updated:** December 18, 2025

## Overview
This repository is an end-to-end E-commerce system:
- **Backend:** Spring Boot 3 + Spring Security 6 (JWT + Refresh Token + RBAC), Liquibase, MySQL
- **Frontend (Phase 2):** React + TypeScript (see `frontend/`), integrating with backend APIs

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

### Backend
1. Configure DB in `backend/src/main/resources/application.properties`
2. Run:
   - `cd backend`
   - `./mvnw spring-boot:run`
3. Swagger:
   - `http://localhost:8080/docs` (or `http://localhost:8080/swagger-ui/index.html`)
   - OpenAPI: `http://localhost:8080/v3/api-docs`

### Frontend
1. Configure API base URL:
   - `frontend/.env` → `VITE_API_BASE_URL=http://localhost:8080`
2. Run:
   - `cd frontend`
   - `npm install`
   - `npm run dev`
3. Open: `http://localhost:5173`

## Roadmap
- Frontend Phase 2 roadmap: `FRONTEND_PHASE2_ROADMAP.md`
- High-level project plan: `PROJECT_PLAN.md`

