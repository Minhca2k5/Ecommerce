# Phase 2 Ops

## Objective
Support a stable frontend delivery pipeline for core user/admin flows built in Phase 2:
- public browse
- auth
- user profile/address/cart/checkout/orders/payments
- admin CRUD surfaces

## Runtime & Environment
- Backend required for FE development and verification:
  - Swagger/UI: `/docs`
  - OpenAPI JSON: `/v3/api-docs`
- Frontend env:
  - `VITE_API_BASE_URL=http://localhost:8080`
- Standard local ports:
  - backend `8080`
  - frontend `5173`

## Operational Checklist
1. Start backend and verify `GET /api/public/home` returns 200.
2. Start frontend and verify env binding from `VITE_API_BASE_URL`.
3. Verify auth token flow:
   - login obtains token pair
   - expired access token refreshes via refresh-token endpoint
4. Verify user journey end-to-end:
   - browse -> cart -> checkout -> order -> payment record
5. Verify admin base surfaces load and support paging/filtering.

## API Contract Hygiene
- Follow Swagger as source of truth; do not guess request/response shape.
- Handle Spring `Page` contract (`content`, `totalElements`, `totalPages`, `number`, `size`).
- Enforce consistent UI states on every screen:
  - loading
  - empty
  - error

## Common Failure Modes (Phase 2)
- API base URL mismatch -> all calls fail (network/CORS symptoms).
- Expired access token without refresh retry -> false logout behavior.
- FE assumes flat list but API returns paged payload.
- Missing UX fallback states causes blank screens.

## Minimal Incident Playbook
- If widespread FE errors:
  1. Check backend health and Swagger reachability.
  2. Validate `.env` base URL.
  3. Validate auth refresh flow in Network tab.
  4. Roll back latest FE API contract changes if mismatch introduced.
