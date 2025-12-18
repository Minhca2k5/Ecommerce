# Frontend Phase 2 Roadmap (React) — bám theo Backend Endpoints (UPDATED)

Mục tiêu Phase 2: xây frontend chạy tốt trên laptop/mobile (responsive), UI đẹp, UX chuẩn (loading/error/empty), và **tận dụng hầu hết endpoint** backend để dự án “CV-ready” và sẵn sàng sang Phase 3 (performance/caching).

Nguyên tắc:
- Không đoán API: luôn đối chiếu Swagger/OpenAPI trước khi code.
- Mỗi milestone phải có: UI chạy được + gọi API thật + loading/error + responsive cơ bản.
- Ưu tiên “end-to-end user journey” trước (browse → cart → checkout → order → payment), sau đó mới admin.

---

## 0) Chuẩn bị (bắt buộc)

### 0.1) Chạy backend (có Swagger + test API)
- `cd backend`
- `.\mvnw spring-boot:run`
- Swagger UI:
  - `http://localhost:8080/docs` (hoặc `http://localhost:8080/swagger-ui/index.html`)
- OpenAPI JSON:
  - `http://localhost:8080/v3/api-docs`

**Checkpoint**:
- Mở Swagger được
- `GET /api/public/home` trả 200 + JSON

---

## 1) Khởi tạo frontend (Milestone M1)

### 1.1) Scaffold React (TypeScript)
- Ở root repo:
  - `npx create-vite@latest frontend -- --template react-ts`
  - `cd frontend`
  - `npm install`
  - `npm run dev`

### 1.2) Cấu hình base URL backend
- Tạo `frontend/.env`:
  - `VITE_API_BASE_URL=http://localhost:8080`

**Checkpoint**:
- Dev server chạy `http://localhost:5173`
- Đọc env bằng `import.meta.env.VITE_API_BASE_URL`

---

## 2) Nền tảng routing + layout (Milestone M2)

### 2.1) Cài router
- `npm install react-router-dom`

### 2.2) Tạo layout tối thiểu (mobile-first)
Route tối thiểu:
- `/` Home
- `/products` Product list
- `/products/:productId` Product detail
- `/categories/:categoryId` Category detail (public)
- `/login` Login

**Checkpoint**:
- Layout component + nested routes
- Điều hướng bằng `Link`, đọc param bằng `useParams`

---

## 3) API layer tối thiểu (Milestone M3) — chưa auth

### 3.1) Viết API client wrapper
- Tạo `frontend/src/lib/apiClient.ts`

Yêu cầu:
- Base URL từ `VITE_API_BASE_URL`
- `fetch` + parse JSON
- Throw error khi `!response.ok` (kèm status + message)
- Trước mắt có thể dùng `unknown`/`any`, sau đó type dần

### 3.2) Kết nối endpoint public đầu tiên
Backend:
- `GET /api/public/home`

UI:
- Trang Home gọi API và render dữ liệu thật (có thể render “raw JSON” trước để verify end-to-end)

**Checkpoint**:
- Có loading/error state
- Debug được request/response trong Network tab

---

## 4) Storefront public (Milestone M4)
Mục tiêu: user chưa đăng nhập vẫn browse được “đã đẹp”.

### 4.1) Product listing + paging/filter
Backend:
- `GET /api/public/products` (filter + pageable)
- `GET /api/public/products/top-rating`
- `GET /api/public/products/most-favorite`
- `GET /api/public/products/most-viewed`
- `GET /api/public/products/best-selling`

UI:
- `/products`: list + pagination (Prev/Next) + filter cơ bản
- Home section: hiển thị ít nhất 1 top list

**Checkpoint**:
- Dùng đúng format `Page<...>` của Spring (`content`, `totalElements`, `number`, `size`, …)
- Đồng bộ filter/paging bằng query string (page/size/sort/filter)

### 4.2) Product detail + reviews (public read)
Backend:
- `GET /api/public/products/{productId}`
- `GET /api/public/products/{productId}/reviews`

UI:
- `/products/:productId`: hiển thị thông tin + reviews

### 4.3) Product images (gallery đẹp)
Backend:
- `GET /api/public/products/{productId}/images`
- `GET /api/public/products/{productId}/images/primary`
- `GET /api/public/products/{productId}/images/{imageId}`

UI:
- `/products/:productId`: gallery (primary + thumbnails) + skeleton loading

### 4.4) Categories (public)
Backend:
- `GET /api/public/categories` (filter + pageable)
- `GET /api/public/categories/{categoryId}`
- `GET /api/public/categories/{categoryId}/details`
- `GET /api/public/categories/{categoryId}/subcategories`

UI:
- `/products`: sidebar/filter theo category (tối thiểu: chọn category để lọc product list)
- `/categories/:categoryId`: hiển thị chi tiết + subcategories

### 4.5) Banners (public)
Backend:
- `GET /api/public/banners` (filter + pageable)

UI:
- Home hero carousel lấy từ banners thật (responsive + swipe mobile)

---

## 5) Auth (Milestone M5) — mở khóa các endpoint `/api/users/me/*`

Backend:
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh-token`

### 5.1) Implement login/logout
UI:
- `/login`: form login
- (tuỳ chọn) `/register`: form register

Storage (đơn giản, dễ hiểu):
- `localStorage`: `accessToken`, `refreshToken`, `tokenType`

### 5.2) Auto attach token + refresh + retry
Yêu cầu:
- Với request cần auth: add header `Authorization: Bearer <accessToken>`
- Nếu gặp 401/403 do token hết hạn:
  - gọi `POST /api/auth/refresh-token`
  - update token
  - retry đúng 1 lần (tránh loop)

**Checkpoint**:
- Login/logout hoạt động
- Auto refresh hoạt động, không retry vô hạn

---

## 6) User core (Milestone M6)

### 6.1) Profile
Backend:
- `GET /api/users/me`
- `GET /api/users/me/details`
- `PUT /api/users/me`
- `PATCH /api/users/me/password`
- `DELETE /api/users/me`

UI:
- `/me`: xem profile
- `/me/edit`: sửa profile
- `/me/password`: đổi mật khẩu

### 6.2) Addresses
Backend (đầy đủ tuỳ implementation hiện tại):
- `GET /api/users/me/addresses`
- `POST /api/users/me/addresses`
- `GET /api/users/me/addresses/default`
- `GET /api/users/me/addresses/{addressId}`
- `PUT /api/users/me/addresses/{addressId}`
- `PATCH /api/users/me/addresses/{addressId}/default`
- `DELETE /api/users/me/addresses/{addressId}`

UI:
- `/me/addresses`: CRUD + set default
- Checkout: chọn address default / chọn address khác

---

## 7) Cart + Checkout + Orders + Payments (Milestone M7)
Mục tiêu: hoàn thiện “core journey” để demo CV.

### 7.1) Cart
Backend:
- `GET /api/users/me/carts`
- `POST /api/users/me/carts`
- Cart items: toàn bộ endpoint trong `UserCartItemController` (add/update/remove/list/clear…)

UI:
- `/cart`: xem giỏ hàng, update qty, remove item, clear cart

### 7.2) Orders
Backend:
- `GET /api/users/me/orders`
- `GET /api/users/me/orders/{orderId}`
- `POST /api/users/me/orders`
- Order items: toàn bộ endpoint trong `UserOrderItemController`

UI:
- `/checkout`: tạo order từ cart + address + payment method
- `/orders`: list orders
- `/orders/:orderId`: order detail (items + payment status)

### 7.3) Payments
Backend:
- `GET /api/users/me/orders/{orderId}/payments`
- `GET /api/users/me/orders/{orderId}/payments/{paymentId}`
- `POST /api/users/me/orders/{orderId}/payments`

UI:
- Trong order detail: tạo payment (phase 2 chỉ cần tạo record + hiển thị status)

---

## 8) Promotions + Activity + Notifications + Reviews (Milestone M8)

### 8.1) Vouchers (public) + voucher uses (user)
Backend (public):
- `GET /api/public/vouchers?code=...`
- `GET /api/public/vouchers/filter?minOrderAmount=...`
- `GET /api/public/vouchers/{voucherId}`

Backend (user):
- `GET /api/users/me/voucher-uses/user/me`
- `GET /api/users/me/voucher-uses/order/{orderId}`
- `GET /api/users/me/voucher-uses/voucher/{voucherId}`

UI:
- Checkout: nhập voucher code → lookup voucher → hiển thị voucher hợp lệ/không hợp lệ
- Profile: “Voucher uses” (đã dùng) + filter theo order

### 8.2) Wishlist + Recent views + Search logs
Backend:
- `UserWishlistController`: `/api/users/me/wishlists/*`
- `UserRecentViewController`: `/api/users/me/recent-views/*`
- `UserSearchLogController`: `/api/users/me/search-logs/*`

UI:
- Wishlist page + add/remove từ product detail
- Recent views widget (home hoặc profile)
- Search logs (tối thiểu: tạo log khi search + list + clear)

### 8.3) Notifications
Backend:
- `UserNotificationController`: `/api/users/me/notifications/*`

UI:
- Notification bell + list + mark read

### 8.4) User reviews (write)
Backend:
- `UserReviewController`: `/api/users/me/reviews/*`

UI:
- Product detail: user có thể tạo/sửa/xoá review của mình (kèm rating/comment)

---

## 9) Admin dashboard (Milestone M9)
Nguyên tắc: làm admin sau khi user flow đã ổn.

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

## 10) Nâng cấp “CV-ready” (Milestone M10)
- Tạo `frontend/README.md`: cách chạy, cấu trúc thư mục, conventions, env.
- Responsive audit: mobile (360px), tablet, desktop.
- UX polish: empty states, skeleton loading, toasts, form validation, confirm dialog.
- (Tuỳ chọn) PWA: offline shell + installable.
- Tổng kết coverage: liệt kê các endpoint đã dùng theo milestone (để chứng minh “dùng hầu hết API”).

---

## Cách làm “đúng chất”
- Mỗi milestone: làm 1–2 màn hình + 1–3 endpoint, xong rồi mới mở rộng.
- Khi xong milestone: tự viết lại 5–10 dòng tóm tắt “mình vừa hiểu gì” (để thật sự nắm hệ thống).
