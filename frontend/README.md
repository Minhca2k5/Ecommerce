# Ecommerce Frontend

React 19 + TypeScript + Vite + TailwindCSS storefront and admin UI for the e-commerce system.

> **Status:** Phase 2 completed, with ongoing UX polish  
> **Last Updated:** March 20, 2026

## Overview

This app visualizes the backend domains end to end:

- customer storefront flows
- authenticated user account flows
- admin management flows
- realtime notifications and chatbot integration

## Stack

- React 19
- TypeScript
- Vite
- TailwindCSS
- React Query
- Zustand
- React Router
- shadcn-style UI primitives

## Requirements

- Node.js 18+
- Backend running at `http://localhost:8080`

## Environment

Create `frontend/.env`:

```bash
VITE_API_BASE_URL=http://localhost:8080
```

## Run

### Dev

```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173`.

### Build

```bash
cd frontend
npm run build
npm run preview
```

## Routes

### Storefront

- `/`
- `/categories`
- `/products`
- `/products/:productId`
- `/products/slug/:slug`
- `/cart`
- `/checkout`
- `/guest/orders/:orderId`

### Authenticated User

- `/login`
- `/register`
- `/choose-role`
- `/me`
- `/me/edit`
- `/me/password`
- `/me/addresses`
- `/me/vouchers`
- `/me/vouchers/:voucherId`
- `/me/voucher-uses`
- `/me/wishlist`
- `/notifications`
- `/orders`
- `/orders/:orderId`
- `/orders/:orderId/momo-qr`

### Admin

- `/admin`
- `/admin/products`
- `/admin/categories`
- `/admin/product-images`
- `/admin/orders`
- `/admin/order-items`
- `/admin/payments`
- `/admin/analytics`
- `/admin/users`
- `/admin/roles`
- `/admin/addresses`
- `/admin/warehouses`
- `/admin/inventories`
- `/admin/banners`
- `/admin/vouchers`
- `/admin/voucher-uses`
- `/admin/notifications`
- `/admin/audit-logs`
- `/admin/reviews`
- `/admin/profile`
- `/admin/profile/edit`
- `/admin/profile/password`

## Auth And Role UX

- Tokens are stored in `localStorage`: `accessToken`, `refreshToken`, `tokenType`.
- Accounts with multiple roles are redirected to `/choose-role` after login.
- The header role switcher lets users change the selected role without logging out.
- Admin routes require an admin authority in the token and `selectedRole === "ADMIN"`.

## UX Conventions

- No raw JSON dumps in the UI.
- Every page should have loading, empty, and error states.
- Tables should be horizontally scrollable on mobile via `overflow-x-auto`.
- Light/dark theme toggle is available in the header and persisted locally.

## Behavior Notes

- Orders list cards use a product-first preview instead of generic order text.
- Orders can hydrate missing item previews from `/api/users/me/orders/{orderId}/items/all`.
- User order chips use friendly labels such as Pending, Paid, and Cancelled.
- Product sorting uses backend-supported endpoints for Top rated and Best sellers.
- Home page promo sections are kept stable and non-duplicated.

## Key Files

- `src/app/router.tsx` - route tree
- `src/app/AppLayout.tsx` - header, nav, alerts, role switcher
- `src/admin/AdminLayout.tsx` - admin shell and navigation
- `src/app/RequireAuth.tsx` - authenticated route guard
- `src/app/RequireAdmin.tsx` - admin route guard
- `src/lib/http.ts` - auth attach, refresh, retry once
- `src/lib/roleSelection.ts` - role extraction and selected-role storage
- `src/lib/*Api.ts` - API wrappers by domain

## Endpoint Coverage

See [endpoint coverage](docs/ENDPOINT_COVERAGE.md).

## Related Docs

- Root project overview: [../README.md](../README.md)
- Backend-specific docs: [../docs/roadmaps/PROJECT_PLAN.md](../docs/roadmaps/PROJECT_PLAN.md)

