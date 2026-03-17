# Ecommerce Frontend (Phase 2 + UX Polish)

React + TypeScript + Vite + TailwindCSS (shadcn-style primitives) UI để “visualize” toàn bộ backend APIs.

> **Status:** Phase 2 Completed + ongoing UX polish (user/admin)  
> **Last Updated:** March 18, 2026

## Requirements
- Node.js 18+
- Backend running at `http://localhost:8080`

## Environment
Create `frontend/.env` (or copy from `frontend/.env.example`):
```bash
VITE_API_BASE_URL=http://localhost:8080
```

## Run (Dev)
```bash
cd frontend
npm install
npm run dev
```
Open `http://localhost:5173`

## Build
```bash
cd frontend
npm run build
npm run preview
```

## Auth + Role UX
- Tokens are stored in `localStorage`: `accessToken`, `refreshToken`, `tokenType`.
- If account has **multiple roles**, after login user is redirected to `/choose-role`.
- Role switcher is available in the account dropdown (no need to logout).
- Admin routes live under `/admin/*` and require:
  - Token contains admin authority/role, and
  - `selectedRole === "ADMIN"` (chosen by the user).

## Vouchers
- `My vouchers`: `/me/vouchers` (browse eligible vouchers + search by code).
- Checkout: click `Apply voucher` to open eligible list + search by code (Shopee-like picker).

## Theme
- Light/Dark toggle is available in the header (persisted locally).

Key files:
- `src/lib/http.ts` (auth attach + refresh + retry once)
- `src/lib/roleSelection.ts` (extract roles from JWT + selected role storage)
- `src/pages/ChooseRolePage.tsx`
- `src/app/RequireAuth.tsx`, `src/app/RequireAdmin.tsx`

## UI Structure
- `src/app/router.tsx`: route tree
- `src/app/AppLayout.tsx`: header/nav + alerts dropdown + role switcher UI
- `src/admin/AdminLayout.tsx`: admin layout (sidebar desktop + mobile navigate select)
- `src/pages/*`: storefront + user pages
- `src/pages/admin/*`: admin CRUD pages mapped to `/api/admin/**`
- `src/lib/*Api.ts`: API wrappers per domain

## Conventions
- No raw JSON dumps in UI.
- Every page should have: loading state, empty state, error state.
- Tables must be horizontally scrollable on mobile (wrapped with `overflow-x-auto`).

## Endpoint Coverage
See `frontend/docs/ENDPOINT_COVERAGE.md`.

## Recent UX + Behavior Updates (March 2026)
- Orders list cards now show product-first preview (name + quantity) instead of generic order text.
- Orders list can hydrate item previews via `/api/users/me/orders/{orderId}/items/all` if item details are missing in `/api/users/me/orders`.
- User order status chips use friendly labels (Pending/Paid/Cancelled/...).
- Products sort behavior:
  - `Top rated` uses `/api/public/products/top-rating`.
  - `Best sellers` uses `/api/public/products/best-selling`.
  - This ensures visible list changes when switching sort options.
- Home page promo cleanup:
  - Removed duplicated promo category image section.
  - Featured offers use stable ordering from active banners.
