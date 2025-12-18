# Frontend Phase 2 Roadmap (React) — bám theo Backend Endpoints

Mục tiêu: xây frontend chạy tốt trên laptop/mobile (responsive), làm **từng lát cắt nhỏ** để bạn vừa code vừa hiểu. Backend đã có sẵn API theo các nhóm dưới đây; frontend sẽ lần lượt “phủ” từng nhóm.

## 0) Chuẩn bị (bắt buộc)

### 0.1. Chạy backend (để có Swagger + test API)
- `cd backend`
- `.\mvnw spring-boot:run`
- Mở Swagger UI:
  - `http://localhost:8080/docs` (hoặc `http://localhost:8080/swagger-ui/index.html`)
- OpenAPI JSON:
  - `http://localhost:8080/v3/api-docs`

### 0.2. Nguyên tắc xây frontend
- Không “nhớ API bằng đầu” → luôn đối chiếu Swagger/OpenAPI.
- Mỗi milestone phải có:
  - UI chạy được
  - gọi được API thật
  - có loading/error state
  - có responsive cơ bản (mobile-first)

---

## 1) Khởi tạo frontend (Milestone M1)

### 1.1. Scaffold React (TypeScript)
Lý do chọn TS: phổ biến trong công ty, bắt lỗi sớm khi gọi API.

- Tại root repo:
  - `npx create-vite@latest frontend -- --template react-ts`
  - `cd frontend`
  - `npm install`
  - `npm run dev`

### 1.2. Cấu hình base URL backend
- Tạo `frontend/.env`:
  - `VITE_API_BASE_URL=http://localhost:8080`

**Checkpoint hiểu**:
- Vite env dùng `import.meta.env.VITE_*`
- dev server chạy ở `http://localhost:5173` (thường)

---

## 2) Nền tảng routing + layout (Milestone M2)

### 2.1. Cài router
- `npm install react-router-dom`

### 2.2. Tạo layout tối thiểu
Mục tiêu: có header + container + footer; responsive.

Route tối thiểu:
- `/` Home
- `/products` Product list
- `/products/:productId` Product detail
- `/login` Login

**Checkpoint hiểu**:
- Layout component và nested routes
- Điều hướng bằng `Link`, đọc param bằng `useParams`

---

## 3) API layer tối thiểu (Milestone M3) — chưa auth

### 3.1. Viết API client wrapper
Tạo file gợi ý:
- `frontend/src/lib/apiClient.ts`

Yêu cầu:
- Base URL từ `VITE_API_BASE_URL`
- `fetch` + parse JSON
- Throw error khi `!response.ok`
- Kiểu dữ liệu “tạm” dùng `unknown`/`any` trước, rồi cải tiến dần

### 3.2. Kết nối endpoint public đầu tiên
Backend endpoint:
- `GET /api/home`

UI:
- Trang Home gọi API và render JSON thô (để thấy dữ liệu thật trước)

**Checkpoint hiểu**:
- `useEffect` + `useState` + loading/error
- Debug network tab (request/response)

---

## 4) Storefront public (Milestone M4)

### 4.1. Product listing + paging/filter cơ bản
Backend endpoints:
- `GET /api/products` (filter + pageable)
- `GET /api/products/top-rating`
- `GET /api/products/most-favorite`
- `GET /api/products/most-viewed`
- `GET /api/products/best-selling`

UI pages:
- `/products`: list + pagination (Prev/Next)
- Home section: hiển thị “top lists” (ít nhất 1 list)

**Checkpoint hiểu**:
- Dạng response `Page<...>` của Spring (thường có `content`, `totalElements`, `number`, `size`…)
- Query string (page/size/sort + filter)

### 4.2. Product detail + reviews
Backend endpoints:
- `GET /api/products/{productId}`
- `GET /api/products/{productId}/reviews`

UI:
- `/products/:productId`: hiển thị thông tin + danh sách reviews

---

## 5) Auth (Milestone M5) — mở khóa các endpoint `/users/me/*`

Backend endpoints:
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh-token`

### 5.1. Implement login/logout
UI:
- `/login`: form login
- (tùy chọn) `/register`: form register

Storage (đơn giản để dễ hiểu):
- `localStorage` lưu `accessToken`, `refreshToken`, `tokenType`

### 5.2. Auto attach token + refresh
Yêu cầu:
- Với request cần auth: thêm header `Authorization: Bearer <accessToken>`
- Nếu gặp 401/403 do token hết hạn:
  - gọi `POST /api/auth/refresh-token`
  - update token
  - retry request 1 lần

**Checkpoint hiểu**:
- Vì sao cần refresh token
- Vì sao phải retry và tránh vòng lặp vô hạn

---

## 6) User core (Milestone M6)

### 6.1. Profile
Backend endpoints:
- `GET /api/users/me`
- `GET /api/users/me/details`
- `PUT /api/users/me`
- `PATCH /api/users/me/password`
- `DELETE /api/users/me`

UI:
- `/me`: xem profile
- `/me/edit`: sửa profile
- `/me/password`: đổi mật khẩu

### 6.2. Addresses
Backend endpoints:
- `GET /api/users/me/addresses`
- `POST /api/users/me/addresses`
- `GET /api/users/me/addresses/default`
- `GET /api/users/me/addresses/{addressId}`
- `PUT /api/users/me/addresses/{addressId}`
- `DELETE /api/users/me/addresses/{addressId}`
- `PATCH /api/users/me/addresses/{addressId}/set-default`

UI:
- `/me/addresses`: list + create/edit/delete + set default

---

## 7) Cart → Checkout → Order (Milestone M7) (xương sống ecommerce)

### 7.1. Cart
Backend endpoints:
- `GET /api/users/me/carts`
- `POST /api/users/me/carts`

UI:
- `/cart`: hiển thị cart (tạo cart nếu chưa có)

### 7.2. Cart items
Backend endpoints:
- `GET /api/users/me/carts/{cartId}/items` (page, optional `productName`)
- `POST /api/users/me/carts/{cartId}/items` (add or update)
- `PUT /api/users/me/carts/{cartId}/items/return` (update quantity kiểu “return”)
- `DELETE /api/users/me/carts/{cartId}/items/{cartItemId}`
- `DELETE /api/users/me/carts/{cartId}/items` (clear)

UI:
- add to cart từ product detail
- cart page: update qty, remove, clear, search item theo name (optional)

### 7.3. Orders
Backend endpoints:
- `GET /api/users/me/orders`
- `POST /api/users/me/orders`
- `GET /api/users/me/orders/{orderId}`

UI:
- `/checkout`: submit tạo order
- `/orders`: list
- `/orders/:orderId`: detail

### 7.4. Order items
Backend endpoints:
- `GET /api/users/me/orders/{orderId}/items`
- `GET /api/users/me/orders/{orderId}/items/all`
- `GET /api/users/me/orders/{orderId}/items/{orderItemId}`

UI:
- order detail hiển thị items (dùng endpoint paged hoặc all)

### 7.5. Payments
Backend endpoints:
- `GET /api/users/me/orders/{orderId}/payments`
- `POST /api/users/me/orders/{orderId}/payments`
- `GET /api/users/me/orders/{orderId}/payments/{paymentId}`

UI:
- `/orders/:orderId/payments`: tạo payment + list payment

---

## 8) User “extras” (Milestone M8)

### 8.1. Wishlist
Backend endpoints:
- `GET /api/users/me/wishlists` (page, optional `productName`)
- `POST /api/users/me/wishlists`
- `DELETE /api/users/me/wishlists` (clear)
- `DELETE /api/users/me/wishlists/{wishlistId}`

UI:
- toggle wishlist từ product detail
- `/me/wishlist`: list/search/clear/remove

### 8.2. Recent views
Backend endpoints:
- `GET /api/users/me/recent-views` (page, optional `productName`)
- `POST /api/users/me/recent-views`
- `DELETE /api/users/me/recent-views` (clear)
- `DELETE /api/users/me/recent-views/{recentViewId}`

UI:
- khi user mở product detail thì POST recent view
- `/me/recent-views`: list/search/clear/remove

### 8.3. Search logs
Backend endpoints:
- `GET /api/users/me/search-logs` (optional `keyword`)
- `POST /api/users/me/search-logs`
- `DELETE /api/users/me/search-logs` (clear)
- `DELETE /api/users/me/search-logs/{searchLogId}`

UI:
- khi user search products thì POST search log
- `/me/search-logs`: list/filter/clear/remove

### 8.4. Notifications
Backend endpoints:
- `GET /api/users/me/notifications`
- `POST /api/users/me/notifications`
- `PUT /api/users/me/notifications/{notificationId}/read?isRead=...`
- `PUT /api/users/me/notifications/{notificationId}/hidden?isHidden=...`
- `PUT /api/users/me/notifications/user?isRead=...&isHidden=...`

UI:
- `/me/notifications`: list + mark read/hide + bulk actions

### 8.5. Promotions (banners/vouchers)
Backend endpoints:
- `GET /api/users/me/banners` (page)
- `GET /api/users/me/vouchers?code=...`
- `GET /api/users/me/vouchers/filter?minOrderAmount=...` (page)
- `GET /api/users/me/vouchers/{voucherId}`
- `GET /api/users/me/voucher-uses/user/me`
- `GET /api/users/me/voucher-uses/order/{orderId}`
- `GET /api/users/me/voucher-uses/voucher/{voucherId}`

UI:
- Home: banners
- Checkout: nhập voucher code (nếu backend order có support apply voucher thì kết nối thêm; nếu chưa thì hiển thị danh sách/validate trước)

---

## 9) Admin dashboard (Milestone M9)

Nguyên tắc: làm admin sau khi user flow ổn.

### 9.1. Admin products/categories/images
Backend endpoints:
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

### 9.2. Admin orders/order-items
Backend endpoints:
- Orders:
  - `GET /api/admin/orders`
  - `GET /api/admin/orders/{orderId}`
  - `PATCH /api/admin/orders/{orderId}/status?status=...`
  - `PATCH /api/admin/orders/{orderId}/currency?currency=...`
- Order items:
  - `GET /api/admin/order-items`
  - `GET /api/admin/order-items/order/{orderId}`
  - `GET /api/admin/order-items/{orderItemId}`

### 9.3. Admin payments
Backend endpoints:
- `GET /api/admin/payments`
- `GET /api/admin/payments/order/{orderId}`
- `GET /api/admin/payments/{paymentId}`
- `PATCH /api/admin/payments/{paymentId}/status?status=...`

### 9.4. Admin users/roles/addresses
Backend endpoints:
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

### 9.5. Admin inventory/warehouse
Backend endpoints:
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

### 9.6. Admin promotions/notifications/reviews
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

## 10) Nâng cấp “CV-ready” (Milestone M10) — đề xuất
- Tạo `README` cho frontend: cách chạy, cấu trúc thư mục, quyết định kỹ thuật.
- Responsive audit: mobile (360px), tablet, desktop.
- UX: empty states, skeleton loading, toasts, form validation.
- (Tuỳ chọn) PWA: offline shell + installable.

---

## Cách làm “từng chút một” (gợi ý nhịp)
- Mỗi milestone: làm 1–2 màn hình + 1–3 endpoint.
- Khi xong milestone: bạn tự viết lại 5–10 dòng “mình vừa học được gì” (để nhớ lâu).

