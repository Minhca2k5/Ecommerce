# Frontend Phase 2 Roadmap (React) - Based on Backend Endpoints (UPDATED)

Phase 2 goal: build a responsive frontend for laptop/mobile with clean UI and
consistent UX states (loading/error/empty), and cover nearly all backend
endpoints so the project is CV-ready and prepared for Phase 3
(performance/caching).

Principles:
- Do not guess APIs: always check Swagger/OpenAPI before coding.
- Each milestone must have working UI, real API calls, and
  loading/error/empty states with basic responsive layout.
- Prioritize end-to-end user journey first (browse -> cart -> checkout ->
  order -> payment), then build admin.

UI/UX rules (apply from M3 onward):
- Do not dump raw JSON in the UI (no `JSON.stringify` to display data).
- Show only user-relevant fields; hide technical fields
  (`createdAt`, `updatedAt`, `id`) unless required for UX.
- Every screen must look like a real product page: clear CTA, empty state,
  skeleton loading, and error state.
- Every feature must be discoverable in UI (menu/button/link).
- After each milestone, UI/style must be completed, not a technical demo.

---

## Phase 2 Status (Project)
- Status: Completed
- Notes:
  - UI/UX polish and responsive audit completed.
  - Endpoint coverage summary: `frontend/docs/ENDPOINT_COVERAGE.md`.
  - Voucher UX updated: My Vouchers + checkout voucher picker
    (eligible list by min order + search by code).
  - Optional PWA deferred to a later phase.

## 0) Preparation (Required)

### 0.1) Run backend (Swagger + test API)
- `cd backend`
- `.\mvnw spring-boot:run`
- Swagger UI:
  - `http://localhost:8080/docs` (or `http://localhost:8080/swagger-ui/index.html`)
- OpenAPI JSON:
  - `http://localhost:8080/v3/api-docs`

Checkpoint:
- Swagger loads
- `GET /api/public/home` returns 200 + JSON

---

## 1) Initialize frontend (Milestone M1)

### 1.1) Scaffold React (TypeScript)
- At repo root:
  - `npx create-vite@latest frontend -- --template react-ts`
  - `cd frontend`
  - `npm install`
  - `npm run dev`

### 1.2) Configure backend base URL
- Create `frontend/.env`:
  - `VITE_API_BASE_URL=http://localhost:8080`

Checkpoint:
- Dev server runs at `http://localhost:5173`
- Env is readable via `import.meta.env.VITE_API_BASE_URL`

---

## 2) Routing + layout foundation (Milestone M2)

### 2.1) Install router
- `npm install react-router-dom`

### 2.2) Create minimal layout (mobile-first)
Minimum routes:
- `/` Home
- `/products` Product list
- `/products/:productId` Product detail
- `/categories/:categoryId` Category detail (public)
- `/login` Login

Checkpoint:
- Layout component + nested routes
- Navigation with `Link`, params with `useParams`

---

## 3) Minimal API layer (Milestone M3) - before auth

### 3.1) Build API client wrapper
- Create `frontend/src/lib/apiClient.ts`

Requirements:
- Base URL from `VITE_API_BASE_URL`
- `fetch` + JSON parsing
- Throw errors on `!response.ok` with status + message
- Start with `unknown`/`any`, later replace with types

### 3.2) Connect first public endpoint
Backend:
- `GET /api/public/home`

UI:
- Banner `target_url` format: `/[table plural]/slug/<slug>`
  (e.g. `/products/slug/smartphone-x20`, `/categories/slug/programming-books`)
- Home page calls API and renders clean UI (no raw JSON)
- Must have loading skeleton, empty state, error state

Checkpoint:
- Loading/error states implemented
- Requests visible in Network tab

---

## 4) Public storefront (Milestone M4)
Goal: users can browse without login.

### 4.1) Product listing + paging/filter
Backend:
- `GET /api/public/products` (filter + pageable)
- `GET /api/public/products/top-rating`
- `GET /api/public/products/most-favorite`
- `GET /api/public/products/most-viewed`
- `GET /api/public/products/best-selling`

UI:
- `/products`: list + pagination + search + category filter (sidebar or chips)
- Home section: selector (dropdown) for top list:
  `top-rating` (default) / `most-favorite` / `most-viewed` / `best-selling`

Checkpoint:
- Handle Spring `Page<...>` format (`content`, `totalElements`, `number`, `size`)
- Preserve filter/paging in query string
- Hide technical fields unless necessary
- Product card shows: primary image, name, price, rating, stock status (if any)

### 4.2) Product detail + reviews (public read)
Backend:
- `GET /api/public/products/{productId}`
- `GET /api/public/products/slug/{slug}` (used by banner target_url)
- `GET /api/public/products/{productId}/reviews`

UI:
- `/products/:productId` layout:
  - Image gallery (primary + thumbnails)
  - Name, price, status badge, description
  - Reviews list (rating stars + comment + created date)

### 4.3) Product images (gallery)
Backend:
- `GET /api/public/products/{productId}/images`
- `GET /api/public/products/{productId}/images/primary`
- `GET /api/public/products/{productId}/images/{imageId}`

UI:
- `/products/:productId`: gallery (primary + thumbnails) + skeleton loading
- Banner click uses `/products/slug/<slug>`, with fallback if slug lookup fails

### 4.4) Categories (public)
Backend:
- `GET /api/public/categories` (filter + pageable)
- `GET /api/public/categories/{categoryId}`
- `GET /api/public/categories/slug/{slug}` (banner target_url)
- `GET /api/public/categories/{categoryId}/details`
- `GET /api/public/categories/slug/{slug}/details` (banner target_url)
- `GET /api/public/categories/{categoryId}/subcategories`
- `GET /api/public/categories/slug/{slug}/subcategories` (optional)

UI:
- Provide a clear entry point to Categories in header or products page
- `/products`: category filter (sidebar or chips)
- `/categories/:categoryId`: detail + subcategories

### 4.5) Banners (public)
Backend:
- `GET /api/public/banners` (filter + pageable)

UI:
- Home hero carousel driven by real banners (responsive + swipe on mobile)

---

## 5) Auth (Milestone M5) - unlock `/api/users/me/*`

Backend:
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh-token`

### 5.1) Implement login/logout
UI/UX requirements:
- Proper form labels/placeholders/validation
- Password show/hide
- Disable submit while loading + friendly error messages
- Toast/snackbar on login fail/success
- Clear account entry in header (Login/Logout + user name if available)

UI:
- `/login`: login form
- (optional) `/register`: register form

Storage:
- `localStorage`: `accessToken`, `refreshToken`, `tokenType`

### 5.2) Auto attach token + refresh + retry
Requirements:
- Add `Authorization: Bearer <accessToken>` on auth requests
- If 401/403 due to expired token:
  - call `POST /api/auth/refresh-token`
  - update token
  - retry once (avoid loops)

Checkpoint:
- Login/logout works
- Auto refresh works; no infinite retry

UI/UX checkpoint:
- Clear loading/empty/error states; no raw JSON

---

## 6) User core (Milestone M6)

### 6.1) Profile
UI/UX requirements:
- Profile page feels like account settings (avatar placeholder, basic info)
- Edit form with validation + toast + disabled while loading
- Password change with confirm + warnings

Backend:
- `GET /api/users/me`
- `GET /api/users/me/details`
- `PUT /api/users/me`
- `PATCH /api/users/me/password`
- `DELETE /api/users/me`

UI:
- `/me`: profile view
- `/me/edit`: edit profile
- `/me/password`: change password

### 6.2) Addresses
UI/UX requirements:
- Address list as cards with "Default" badge
- Confirm dialog on delete
- Empty state prompting add new

Backend:
- `GET /api/users/me/addresses`
- `POST /api/users/me/addresses`
- `GET /api/users/me/addresses/default`
- `GET /api/users/me/addresses/{addressId}`
- `PUT /api/users/me/addresses/{addressId}`
- `PATCH /api/users/me/addresses/{addressId}/set-default`
- `DELETE /api/users/me/addresses/{addressId}`

UI:
- `/me/addresses`: CRUD + set default
- Checkout: choose default or another address

---

## 7) Cart + Checkout + Orders + Payments (Milestone M7)
Goal: complete the core journey for demo.

### 7.1) Cart
UI/UX requirements:
- Cart items show image, name, price, qty stepper, subtotal
- Sticky order summary (desktop), responsive (mobile)
- Empty cart state + CTA "Continue shopping"

Backend:
- `GET /api/users/me/carts`
- `POST /api/users/me/carts`
- Cart items: all endpoints in `UserCartItemController` (add/update/remove/list/clear)

UI:
- `/cart`: view cart, update qty, remove item, clear cart

### 7.2) Orders
UI/UX requirements:
- Checkout uses clear steps/sections: Address + Payment + Review
- Form validation + disable while loading
- Orders list with status badges, total, created date; detail with timeline/status

Backend:
- `GET /api/users/me/orders`
- `GET /api/users/me/orders/{orderId}`
- `POST /api/users/me/orders`
- Order items: all endpoints in `UserOrderItemController`

UI:
- `/checkout`: create order from cart + address + payment method
- `/orders`: list orders
- `/orders/:orderId`: order detail (items + payment status)

### 7.3) Payments
UI/UX requirements:
- Show payment status clearly (badge + message)
- If multiple records exist: list + detail view

Backend:
- `GET /api/users/me/orders/{orderId}/payments`
- `GET /api/users/me/orders/{orderId}/payments/{paymentId}`
- `POST /api/users/me/orders/{orderId}/payments`

UI:
- In order detail: create payment (Phase 2 only needs record + status)

---

## 8) Promotions + Activity + Notifications + Reviews (Milestone M8)

### 8.1) Vouchers (public) + voucher uses (user)
UI/UX requirements:
- Voucher input states: idle/valid/invalid/applying
- Show reason if not applicable (user-friendly)

Backend (public):
- `GET /api/public/vouchers?code=...`
- `GET /api/public/vouchers/filter?minOrderAmount=...`
- `GET /api/public/vouchers/{voucherId}`

Backend (user):
- `GET /api/users/me/voucher-uses/user/me`
- `GET /api/users/me/voucher-uses/order/{orderId}`
- `GET /api/users/me/voucher-uses/voucher/{voucherId}`

UI:
- Checkout: enter code -> lookup -> show eligible/ineligible
- Profile: voucher uses list + filter by order

### 8.2) Wishlist + Recent views + Search logs
UI/UX requirements:
- Wishlist: grid cards + empty state
- Recent views: horizontal slider
- Search logs: list + clear all + delete one

Backend:
- `UserWishlistController`: `/api/users/me/wishlists/*`
- `UserRecentViewController`: `/api/users/me/recent-views/*`
- `UserSearchLogController`: `/api/users/me/search-logs/*`

UI:
- Wishlist page + add/remove from product detail
- Recent views widget (home or profile)
- Search logs (at least: create on search + list + clear)

### 8.3) Notifications
UI/UX requirements:
- Bell icon with unread badge
- Filter read/unread + mark-all-read (if backend supports)

Backend:
- `UserNotificationController`: `/api/users/me/notifications/*`

UI:
- Notification bell + list + mark read

### 8.4) User reviews (write)
UI/UX requirements:
- Rating input with clickable stars
- Review form with validation + optimistic update (if suitable)

Backend:
- `UserReviewController`: `/api/users/me/reviews/*`

UI:
- Product detail: user can create/edit/delete own review

---

## 9) Admin dashboard (Milestone M9)
UI/UX requirements (admin):
- Sidebar nav + breadcrumbs + responsive layout
- Tables with search/filter/pagination; clear actions; confirm on delete
- Forms with validation + toast
- Do not show raw JSON; show business-relevant fields

Principle: build admin after user flow is stable.

### 9.1) Admin products/categories/images
Backend:
- Products:
  - `GET /api/admin/products`
  - `POST /api/admin/products`
  - `GET /api/admin/products/{productId}`
  - `PUT /api/admin/products/{productId}`
  - `DELETE /api/admin/products/{productId}`
  - `PUT /api/admin/products/{productId}/status?status=...`
- Categories:
  - `GET /api/admin/categories`
  - `POST /api/admin/categories`
  - `GET /api/admin/categories/{categoryId}`
  - `GET /api/admin/categories/{categoryId}/details`
  - `GET /api/admin/categories/{categoryId}/subcategories`
  - `PATCH /api/admin/categories/{categoryId}?name=...&slug=...`
  - `DELETE /api/admin/categories/{categoryId}`
- Product images:
  - `POST /api/admin/product-images`
  - `GET /api/admin/product-images/product/{productId}`
  - `GET /api/admin/product-images/product/{productId}/primary`
  - `GET /api/admin/product-images/{imageId}`
  - `PATCH /api/admin/product-images/{imageId}/url?url=...`
  - `PATCH /api/admin/product-images/{imageId}/primary?productId=...`
  - `DELETE /api/admin/product-images/{imageId}`

UI:
- `/admin/products`, `/admin/categories`, `/admin/product-images`

### 9.2) Admin orders/order-items
Backend:
- Orders:
  - `GET /api/admin/orders`
  - `GET /api/admin/orders/{orderId}`
  - `PATCH /api/admin/orders/{orderId}/status?status=...`
  - `PATCH /api/admin/orders/{orderId}/currency?currency=...`
- Order items:
  - `GET /api/admin/order-items`
  - `GET /api/admin/order-items/order/{orderId}`
  - `GET /api/admin/order-items/{orderItemId}`

### 9.3) Admin payments
Backend:
- `GET /api/admin/payments`
- `GET /api/admin/payments/order/{orderId}`
- `GET /api/admin/payments/{paymentId}`
- `PATCH /api/admin/payments/{paymentId}/status?status=...`

### 9.4) Admin users/roles/addresses
Backend:
- Users:
  - `GET /api/admin/users`
  - `POST /api/admin/users`
  - `GET /api/admin/users/{userId}`
  - `GET /api/admin/users/{userId}/details`
  - `DELETE /api/admin/users/{userId}`
  - `GET /api/admin/users/exists/username?username=...`
  - `GET /api/admin/users/exists/email?email=...`
- Roles:
  - `GET /api/admin/roles`
  - `POST /api/admin/roles`
  - `GET /api/admin/roles/{roleId}`
  - `GET /api/admin/roles/by-name?name=...`
  - `DELETE /api/admin/roles/{roleId}`
- Addresses:
  - `GET /api/admin/addresses`
  - `GET /api/admin/addresses/{addressId}`
  - `GET /api/admin/addresses/user/{userId}/default`

### 9.5) Admin inventory/warehouse
Backend:
- Warehouses:
  - `GET /api/admin/warehouses`
  - `POST /api/admin/warehouses`
  - `GET /api/admin/warehouses/{warehouseId}`
  - `GET /api/admin/warehouses/{warehouseId}/details`
  - `PUT /api/admin/warehouses/{warehouseId}`
  - `DELETE /api/admin/warehouses/{warehouseId}`
  - `PATCH /api/admin/warehouses/{warehouseId}/status?active=...`
- Inventories:
  - `GET /api/admin/inventories`
  - `POST /api/admin/inventories`
  - `DELETE /api/admin/inventories/{inventoryId}`
  - `GET /api/admin/inventories/product/{productId}`
  - `GET /api/admin/inventories/warehouse/{warehouseId}`
  - `PATCH /api/admin/inventories/{inventoryId}/stock?quantity=...`
  - `PATCH /api/admin/inventories/{inventoryId}/reserved?quantity=...`

### 9.6) Admin promotions/notifications/reviews
Backend:
- Banners:
  - `GET/POST /api/admin/banners`
  - `PUT /api/admin/banners/{id}`
  - `DELETE /api/admin/banners/{bannerId}`
- Vouchers:
  - `GET/POST /api/admin/vouchers`
  - `PUT /api/admin/vouchers/{id}`
  - `GET /api/admin/vouchers/{voucherId}`
  - `DELETE /api/admin/vouchers/{voucherId}`
- Voucher uses:
  - `GET /api/admin/voucher-uses`
  - `GET /api/admin/voucher-uses/user/{userId}`
  - `GET /api/admin/voucher-uses/order/{orderId}`
  - `GET /api/admin/voucher-uses/voucher/{voucherId}`
- Notifications:
  - `POST /api/admin/notifications`
  - `PUT /api/admin/notifications/{notificationId}`
  - `GET /api/admin/notifications/filter`
- Reviews:
  - `GET /api/admin/reviews`
  - `GET /api/admin/reviews/{reviewId}`

---

## 10) CV-ready polish (Milestone M10)
- Create `frontend/README.md`: run instructions, structure, conventions, env.
- Responsive audit: mobile (360px), tablet, desktop.
- UX polish: empty states, skeleton loading, toasts, form validation,
  confirm dialogs.
- (Optional) PWA: offline shell + installable.
- Coverage summary: list all endpoints used by milestone
  (prove near-complete API coverage).

---
