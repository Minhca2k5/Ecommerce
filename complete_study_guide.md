# 📚 Tổng hợp: Lộ trình đọc + Phỏng vấn + So sánh công nghệ — 14 ngày

> **Chiến lược**: Đọc từ nền tảng → nâng cao. Mỗi ngày 1 chủ đề. Mỗi file có link trực tiếp.
> Mỗi mục gồm: 📂 **File cần đọc** → 🧠 **Tại sao dùng / Không dùng thì sao** → 🔄 **So sánh alternatives** → ❓ **Câu hỏi phỏng vấn**

---

## 🗓️ Ngày 1: Security — JWT + Filter Chain + RBAC

### 📂 Thứ tự đọc

| # | File | Lý do |
|---|------|-------|
| 1 | [SecurityConfig.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/common/config/SecurityConfig.java) | Filter chain: [RequestIdFilter](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/common/config/RequestIdFilter.java#15-56) → `JwtAuthFilter` → [RateLimitFilter](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/common/config/RateLimitFilter.java#21-198) → `RequestLoggingFilter`. URL rules: `/api/public/**` permitAll, `/api/admin/**` ROLE_ADMIN |
| 2 | [RequestIdFilter.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/common/config/RequestIdFilter.java) | Gắn UUID vào MDC → mọi log cùng request có chung trace ID |
| 3 | [JwtService.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/auth/service/JwtService.java) | HS256 signing, access token 1 ngày, refresh token 7 ngày, roles nhúng trong claims |
| 4 | [JwtAuthenticationFilter.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/common/config/JwtAuthenticationFilter.java) | Extract Bearer token → validate → set SecurityContextHolder |
| 5 | [CustomUserDetailsService.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/common/config/CustomUserDetailsService.java) | Load user từ MySQL cho Spring Security |
| 6 | [AuthController.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/auth/controller/AuthController.java) | Endpoints: login, register, OTP verify, refresh |
| 7 | [AuthService.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/auth/service/AuthService.java) | Register → send OTP email → verify → create user → issue tokens |
| 8 | [RefreshTokenService.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/auth/service/RefreshTokenService.java) | Tạo & rotate refresh token |

### 🧠 Tại sao dùng JWT stateless?
- Server **không lưu session** → scale N server instances dễ dàng (không cần sticky session)
- Token tự chứa thông tin (username + roles + exp) → server không query DB mỗi request

**Nếu không có JWT?** → Dùng session-based: cần shared session store (Redis) khi scale, CSRF risk cao hơn

**RequestId Filter nếu không có?** → Log từ nhiều request lẫn lộn, debug production gần như bất khả thi

### 🔄 So sánh Authentication Strategies

| Giải pháp | Stateless? | Revoke ngay? | Scale? | Complexity |
|-----------|:---:|:---:|:---:|:---:|
| **JWT (project dùng)** | ✅ | ❌ (phải đợi expire hoặc dùng blacklist) | ✅ Rất dễ | Trung bình |
| **Session + Cookie** | ❌ | ✅ | ⚠️ Cần shared store | Thấp |
| **OAuth2 / OIDC** | ✅ | ✅ (provider revoke) | ✅ | Cao |
| **Paseto** | ✅ | ❌ | ✅ | Trung bình |

### ❓ Câu hỏi phỏng vấn
1. **Access token vs Refresh token khác nhau thế nào?** → Access ngắn hạn (kèm mỗi request), Refresh dài hạn (chỉ dùng lấy access mới). Tách ra để giảm risk nếu access bị lộ.
2. **JWT có nhược điểm gì?** → Không revoke ngay được, token size lớn, secret key lộ = toàn bộ compromise.
3. **Filter chain hoạt động thế nào? Tại sao thứ tự quan trọng?** → Phải có requestId trước (tracing), rồi JWT (biết user), rồi rate limit (per user), rồi logging.
4. **Tại sao dùng `BCryptPasswordEncoder`?** → Bcrypt có salt built-in + cost factor adjustable → chống rainbow table, brute force.

---

## 🗓️ Ngày 2: Redis — Caching + Rate Limiting

### 📂 Thứ tự đọc

| # | File | Lý do |
|---|------|-------|
| 1 | [RateLimitProperties.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/common/config/RateLimitProperties.java) | Config: capacity, refillTokens, period cho auth/public/user/admin |
| 2 | [RateLimitFilter.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/common/config/RateLimitFilter.java) | **Quan trọng nhất**. Token Bucket algorithm (inner class dòng 154-196). Dual mode: in-memory vs Redis |
| 3 | [RedisRateLimitService.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/common/config/RedisRateLimitService.java) | Redis-backed rate limit cho multi-instance |
| 4 | [CacheConfig.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/common/config/CacheConfig.java) | Per-cache TTL (home 60s, productDetail 5m, category 10m), custom PageImpl serializer, CacheErrorHandler |
| 5 | [PublicHttpCacheConfig.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/common/config/PublicHttpCacheConfig.java) | HTTP Cache-Control + ETag cho public APIs |

Sau đó tìm `@Cacheable`, `@CacheEvict` trong bất kỳ ServiceImpl nào để xem cách dùng.

### 🧠 Token Bucket algorithm
```
Bucket: capacity=100, refill=10 tokens/giây
→ Mỗi request tiêu 1 token → hết token = 429 Too Many Requests
→ Token tự refill theo thời gian → burst OK, sustained overload blocked
```

**Tại sao 2 mode (in-memory vs Redis)?** In-memory nhanh nhưng chỉ đúng cho 1 instance. Redis khi chạy 2+ servers.

**Nếu không có caching?** Homepage 5+ queries mỗi request → cache 60s = giảm ~98% DB calls.

**CacheErrorHandler**: Redis down → log warning, request vẫn chạy bình thường (**graceful degradation**).

### 🔄 So sánh Cache Solutions

| Giải pháp | Distributed? | Speed | Data structures | Dùng khi |
|-----------|:---:|:---:|:---:|---|
| **Redis (project dùng)** | ✅ | Nhanh (~1ms) | Hash, Set, List, Sorted Set, Streams | Multi-instance, cần TTL/eviction |
| **Caffeine (in-memory)** | ❌ | Cực nhanh (~ns) | Map only | Single instance, read-heavy |
| **Memcached** | ✅ | Nhanh | Key-Value only | Simple cache, no data structures |
| **Hazelcast** | ✅ | Nhanh | Map, Queue, Topic | Embedded distributed cache |

### 🔄 So sánh Rate Limit Algorithms

| Algorithm | Burst? | Memory | Accuracy |
|-----------|:---:|:---:|:---:|
| **Token Bucket (project dùng)** | ✅ Cho phép burst | Thấp | Tốt |
| **Sliding Window Counter** | ⚠️ Giới hạn | Trung bình | Rất tốt |
| **Fixed Window** | ⚠️ Edge case 2x burst | Thấp | Trung bình |
| **Leaky Bucket** | ❌ Smooth rate | Thấp | Tốt |

### ❓ Câu hỏi phỏng vấn
5. **Cache invalidation strategies có những loại nào?** → TTL (time-based), manual eviction (`@CacheEvict`), event-driven (RabbitMQ trigger). Project dùng cả 3.
6. **Cache stampede là gì? Cách phòng?** → Nhiều requests đồng thời miss cache → tất cả query DB. Giải pháp: mutex lock, stale-while-revalidate, warm cache trước khi expire.
7. **Rate limiting ở đâu trong filter chain? Tại sao?** → Sau JwtAuthFilter vì cần biết user identity để rate limit per-user thay vì chỉ per-IP.

---

## 🗓️ Ngày 3: Order Flow — Cart → Checkout → Idempotency

### 📂 Thứ tự đọc

| # | File | Lý do |
|---|------|-------|
| 1 | [Cart.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/cart/entity/Cart.java) | Entity: user, guestId |
| 2 | [CartItem.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/cart/entity/CartItem.java) | `unitPriceSnapshot` — snapshot giá lúc add vào cart |
| 3 | [CartServiceImpl.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/cart/service/impl/CartServiceImpl.java) | Guest cart + merge on login |
| 4 | [CartReservationCleanupService.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/cart/service/impl/CartReservationCleanupService.java) | Scheduled cleanup expired reservations |
| 5 | [OrderStatus.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/order/entity/OrderStatus.java) | PENDING → PAID → PROCESSING → SHIPPED → DELIVERED |
| 6 | [OrderServiceImpl.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/order/service/impl/OrderServiceImpl.java) | **393 dòng — đọc kỹ nhất**. Luồng: validate cart → calculate pricing → idempotency check → save → publish events → SSE notify |
| 7 | [IdempotencyService.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/common/idempotency/service/IdempotencyService.java) | Generic: key exists → return old result / key new → create + save key |
| 8 | [DatabaseRetryExecutor.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/common/utils/DatabaseRetryExecutor.java) | Retry deadlocks/connection timeouts |
| 9 | [CheckoutAbuseServiceImpl.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/order/service/impl/CheckoutAbuseServiceImpl.java) | Fraud/abuse rules |
| 10 | [GuestCheckoutIdentityService.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/order/service/GuestCheckoutIdentityService.java) | Resolve guest user nội bộ |
| 11 | [GuestOrderAccessTokenService.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/order/service/GuestOrderAccessTokenService.java) | JWT riêng cho guest tra cứu đơn |

### 🧠 Luồng tạo order (method [createOrderInternal](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/order/service/impl/OrderServiceImpl.java#320-359) dòng 320-358)
```
1. Validate cart ownership + fetch active items
2. calculatePricing(): subtotal → voucher discount → shipping → tax → total
3. Idempotency check (client gửi UUID key)
4. Save Order + OrderItems (READ_COMMITTED transaction + retry)
5. Publish: OrderCreatedEvent (Spring) + DomainEvent (RabbitMQ)
6. Record clickstream (MongoDB)
7. SSE push notification (user + admin)
8. Return response (+ guest access token nếu guest)
```

**Idempotency nếu không có?** → User click 2 lần = 2 orders, double charge → khiếu nại.

**Tại sao READ_COMMITTED thay vì SERIALIZABLE?** → SERIALIZABLE lock rất nặng, nhiều concurrent requests timeout. READ_COMMITTED + idempotency + retry = đủ safe.

### ❓ Câu hỏi phỏng vấn
8. **Idempotency là gì? Implement thế nào?** → Cùng request gửi N lần chỉ tạo 1 kết quả. Client gửi UUID key → server check DB: tồn tại → return cũ, mới → create + lưu key.
9. **Guest checkout hoạt động ra sao?** → Tạo "guest user" nội bộ, gán cart bằng guestId, phát hành JWT riêng (guest access token) để tra cứu đơn hàng.
10. **Transaction isolation levels: READ_COMMITTED vs REPEATABLE_READ vs SERIALIZABLE?** → RC: no dirty read, có phantom read. RR: no phantom read nhưng lock nhiều hơn. SERIALIZABLE: full isolation nhưng performance thấp nhất.

---

## 🗓️ Ngày 4: Payment — MoMo + IPN + Signature

### 📂 Thứ tự đọc

| # | File | Lý do |
|---|------|-------|
| 1 | [PaymentStatus.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/payment/entity/PaymentStatus.java) | INITIATED → SUCCEEDED / FAILED |
| 2 | [MomoProperties.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/payment/momo/MomoProperties.java) | Config: partnerCode, accessKey, secretKey, endpoints |
| 3 | [MomoSignatureUtil.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/payment/momo/MomoSignatureUtil.java) | HMAC-SHA256 utility |
| 4 | [MomoPaymentService.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/payment/momo/MomoPaymentService.java) | **Quan trọng nhất**. Create payment + IPN handling + signature verify (`MessageDigest.isEqual` chống timing attack) |
| 5 | [MomoIpnController.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/payment/controller/pub/MomoIpnController.java) | Public endpoint nhận callback từ MoMo |
| 6 | [PaymentServiceImpl.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/payment/service/impl/PaymentServiceImpl.java) | CRUD payment records |
| 7 | [OutboundRetryExecutor.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/common/utils/OutboundRetryExecutor.java) | Retry external API calls |

### 🧠 Luồng IPN
```
User pay trên app MoMo → MoMo POST IPN đến server → verify HMAC signature (timing-safe)
→ resultCode=0 → SUCCEEDED / khác → FAILED → publish domain event
```

**`MessageDigest.isEqual` vs `string.equals`?** → `equals` dừng sớm khi ký tự khác → attacker đo thời gian để đoán (**timing attack**). `isEqual` so sánh hết → constant time.

### 🔄 So sánh Payment Gateways

| Gateway | Region | IPN/Webhook | SDK | Sandbox |
|---------|--------|:---:|:---:|:---:|
| **MoMo (project dùng)** | Việt Nam | ✅ IPN | REST API | ✅ |
| **VNPay** | Việt Nam | ✅ IPN | REST API | ✅ |
| **Stripe** | Global | ✅ Webhook + CLI | Full SDK | ✅ |
| **PayPal** | Global | ✅ Webhook | SDK | ✅ |

### ❓ Câu hỏi phỏng vấn
11. **IPN là gì? Tại sao cần thay vì chỉ redirect?** → IPN là server-to-server callback. Redirect phụ thuộc user browser (có thể đóng trước khi redirect). IPN đáng tin cậy hơn.
12. **Timing attack là gì?** → So sánh string byte-by-byte, dừng sớm khi khác → attacker đo response time để đoán từng byte. `MessageDigest.isEqual` so sánh tất cả bytes → thời gian không đổi.
13. **Payment idempotency xử lý thế nào?** → `momoOrderId = "order_" + orderId + "_" + timestamp` + idempotency key từ client → không tạo duplicate payment records.

---

## 🗓️ Ngày 5: RabbitMQ — Event-Driven Architecture

### 📂 Thứ tự đọc

| # | File | Lý do |
|---|------|-------|
| 1 | [RabbitConfig.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/messaging/config/RabbitConfig.java) | TopicExchange, 3 queues + 3 DLQs, bindings, retry 3 lần |
| 2 | [DomainEventType.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/messaging/event/DomainEventType.java) | Enum tất cả event types |
| 3 | [DomainEventPublisher.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/messaging/event/DomainEventPublisher.java) | Routing: PRODUCT_* → search, ORDER/PAYMENT → notification, CATEGORY/VOUCHER → chatbot |
| 4 | [SearchIndexConsumer.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/messaging/consumer/SearchIndexConsumer.java) | Consume → reindex Elasticsearch |
| 5 | [NotificationConsumer.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/messaging/consumer/NotificationConsumer.java) | Consume → create notification records |
| 6 | [ChatbotCacheConsumer.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/messaging/consumer/ChatbotCacheConsumer.java) | Consume → invalidate chatbot knowledge cache |

### 🧠 Topology
```
TopicExchange "ecommerce.events"
├── event.search       → SearchQueue       → SearchIndexConsumer      (reindex ES)
├── event.notification → NotificationQueue  → NotificationConsumer     (send notifs)
└── event.chatbot      → ChatbotCacheQueue  → ChatbotCacheConsumer     (invalidate cache)
    Mỗi queue có DLQ (Dead Letter Queue) qua DLX exchange
```

**Tại sao message broker thay vì gọi trực tiếp?**
- **Decoupling**: OrderService không biết SearchService tồn tại
- **Async**: Không block user response
- **Reliability**: Consumer down → message chờ trong queue
- **Nếu không có**: 1 service lỗi → ảnh hưởng cả order flow, response chậm

### 🔄 So sánh Message Brokers

| Broker | Throughput | Message model | DLQ | Dùng khi |
|--------|:---:|---|:---:|---|
| **RabbitMQ (project dùng)** | Cao | Push (broker gửi đến consumer) | ✅ Built-in | Task queues, routing phức tạp |
| **Kafka** | Rất cao | Pull (consumer poll), log-based | ✅ | Event sourcing, replay, high throughput |
| **Redis Streams** | Cao | Pull | ⚠️ Manual | Đã có Redis, simple use cases |
| **Spring Events** | N/A | In-process | ❌ | Đơn giản, không cần durability |

### ❓ Câu hỏi phỏng vấn
14. **DLQ là gì? Tại sao cần?** → Queue chứa messages fail sau max retry. Không mất data, có thể inspect/replay. Main queue không bị block.
15. **Topic vs Direct vs Fanout exchange?** → Topic: routing pattern (`event.#`). Direct: exact match. Fanout: broadcast tất cả queues. Project dùng Topic cho flexibility.
16. **Nếu consumer xử lý chậm hơn producer?** → Messages tích tụ trong queue. Giải pháp: scale thêm consumer, prefetch limit, hoặc back-pressure.

---

## 🗓️ Ngày 6: Elasticsearch + SSE + Notification

### 📂 Thứ tự đọc

| # | File | Lý do |
|---|------|-------|
| 1 | [ProductDocument.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/search/document/ProductDocument.java) | ES document: id, name, description |
| 2 | [ProductSearchService.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/search/service/ProductSearchService.java) | `NativeQuery` + `multiMatch` trên name & description → get IDs → fetch MySQL |
| 3 | [ProductIndexService.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/search/service/ProductIndexService.java) | Index/delete/bulk reindex |
| 4 | [SseEmitterService.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/realtime/service/SseEmitterService.java) | ConcurrentHashMap quản lý emitters per user/admin |
| 5 | [UserRealtimeController.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/realtime/controller/UserRealtimeController.java) | SSE subscribe endpoint |
| 6 | [OrderNotificationListener.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/notification/listener/OrderNotificationListener.java) | `@EventListener`: OrderCreatedEvent → tạo notification |
| 7 | [NotificationServiceImpl.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/notification/service/impl/NotificationServiceImpl.java) | CRUD notifications |

### 🧠 Hybrid search pattern
```
User search "laptop" → ES multiMatch (name, description) → ranked IDs → fetch MySQL (giá/stock mới nhất)
→ Tại sao hybrid? ES là eventually consistent, MySQL có data ACID.
```

### 🔄 So sánh Search vs Realtime

| Search | Full-text? | Fuzzy? | Ranking? | Dùng khi |
|--------|:---:|:---:|:---:|---|
| **Elasticsearch (project)** | ✅ | ✅ | ✅ | Large dataset, complex queries |
| **MySQL LIKE** | ⚠️ Basic | ❌ | ❌ | Simple, small dataset |
| **MySQL Full-Text** | ✅ | ❌ | ⚠️ Basic | Medium dataset, no extra infra |
| **Meilisearch** | ✅ | ✅ | ✅ | Easy setup, typo tolerance |

| Realtime | Direction | Auto-reconnect | Protocol | Dùng khi |
|----------|-----------|:---:|---|---|
| **SSE (project)** | Server→Client | ✅ | HTTP | Notifications, live updates |
| **WebSocket** | Bidirectional | ❌ | Custom | Chat, gaming, real-time collab |
| **Long Polling** | Client pull | N/A | HTTP | Fallback, simple |

### ❓ Câu hỏi phỏng vấn
17. **Inverted index là gì?** → Mapping: word → list of documents (như index cuối sách). Search O(1) thay vì O(n) full scan.
18. **SSE vs WebSocket? Tại sao chọn SSE?** → SSE: one-way, HTTP, auto-reconnect, đơn giản. Project chỉ cần push → SSE đủ.
19. **ES down thì sao?** → Fallback sang MySQL LIKE search (chậm hơn nhưng vẫn hoạt động). Design for graceful degradation.

---

## 🗓️ Ngày 7: MongoDB + Analytics ETL + AOP

### 📂 Thứ tự đọc

| # | File | Lý do |
|---|------|-------|
| 1 | [ClickstreamEventDocument.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/mongo/document/ClickstreamEventDocument.java) | Document: eventType, productId, userId, eventTime |
| 2 | [ClickstreamEventService.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/mongo/service/ClickstreamEventService.java) | Record VIEW_PRODUCT, ADD_TO_CART, PLACE_ORDER |
| 3 | [AnalyticsEtlService.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/analytics/service/AnalyticsEtlService.java) | **324 dòng**. Scheduled 1:15AM UTC → read Mongo → validate quality → aggregate → write MySQL mart |
| 4 | [AnalyticsRealtimeCounterService.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/analytics/service/AnalyticsRealtimeCounterService.java) | Redis Hash/Set counters cho today (chưa ETL) |
| 5 | [AdminAnalyticsServiceImpl.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/analytics/service/impl/AdminAnalyticsServiceImpl.java) | Merge: MySQL (historical) + Redis (today) |
| 6 | [AuditLogAspect.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/common/audit/aop/AuditLogAspect.java) | `@Around`: intercept `@AuditAction` methods → auto log |
| 7 | [GeneralExceptionHandler.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/common/exception/GeneralExceptionHandler.java) | `@ControllerAdvice`: global exception → standardized ErrorResponse |

### 🧠 ETL Pipeline
```
MongoDB (raw events) → Daily ETL → Validate quality → Aggregate → MySQL (daily_product_metrics)
Redis (realtime counters today) + MySQL (historical) = Merged API response
```

**Tại sao MongoDB cho clickstream?** → High write volume, schema linh hoạt, không cần ACID. MySQL overhead cho write-heavy analytics data.

### 🔄 So sánh Databases theo use case

| Storage | ACID? | Schema | Best for | Project dùng cho |
|---------|:---:|---|---|---|
| **MySQL** | ✅ | Strict | Transactions, relations | Users, orders, payments, products |
| **MongoDB** | ⚠️ Document-level | Flexible | Logs, events, analytics | Clickstream, chatbot logs, audit archive |
| **Redis** | ❌ | Key-Value | Cache, counters, sessions | Rate limit, cache, realtime counters |
| **Elasticsearch** | ❌ | Schema-less | Full-text search | Product search |

### ❓ Câu hỏi phỏng vấn
20. **AOP là gì? Liên hệ project?** → Tách cross-cutting concerns ra khỏi business logic. `@AuditAction("ORDER_CREATED")` → AuditLogAspect tự ghi log ai làm gì, entity nào, lúc nào.
21. **ETL idempotent rerun nghĩa là gì?** → Chạy lại cùng ngày → DELETE cũ + INSERT mới → kết quả giống nhau. Quan trọng khi job fail giữa chừng.
22. **ControllerAdvice hoạt động thế nào?** → Global exception handler. Bất kỳ controller throw exception → catch + convert thành `ErrorResponse` chuẩn (status code, message, timestamp).

---

## 🗓️ Ngày 8: Backend CRUD — User, Product, Promotion

> **Mục tiêu**: Đọc các domain "core CRUD" — đây là nền tảng data mà **tất cả tính năng nâng cao** (order, search, analytics, payment) đều phụ thuộc vào. Hiểu entity relationships, Specification pattern, và cách cache được áp dụng.

### 📂 Thứ tự đọc — User & Role & Address (33 files)

| # | File | Lý do |
|---|------|-------|
| 1 | [User.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/user/entity/User.java) | Core entity: username, email, password, roles, addresses |
| 2 | [Role.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/user/entity/Role.java) | RBAC role entity |
| 3 | [Address.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/user/entity/Address.java) | User shipping addresses |
| 4 | [UserServiceImpl.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/user/service/impl/UserServiceImpl.java) | User CRUD + search/filter (Specification pattern) |
| 5 | [RoleServiceImpl.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/user/service/impl/RoleServiceImpl.java) | Role CRUD |
| 6 | [AddressServiceImpl.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/user/service/impl/AddressServiceImpl.java) | Address CRUD |
| 7 | [UserController.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/user/controller/user/UserController.java) | User self-service endpoints |
| 8 | [AdminUserController.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/user/controller/admin/AdminUserController.java) | Admin user management |

### 📂 Thứ tự đọc — Product & Category (41 files)

| # | File | Lý do |
|---|------|-------|
| 9 | [Product.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/product/entity/Product.java) | Core entity: name, slug, price, category, status |
| 10 | [ProductStatus.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/product/entity/ProductStatus.java) | Enum: ACTIVE, INACTIVE, DRAFT |
| 11 | [Category.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/product/entity/Category.java) | Category tree (parent-child relationship) |
| 12 | [ProductServiceImpl.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/product/service/impl/ProductServiceImpl.java) | Product CRUD + `@Cacheable` + Specification search |
| 13 | [CategoryServiceImpl.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/product/service/impl/CategoryServiceImpl.java) | Category CRUD + tree structure |
| 14 | [ProductImageServiceImpl.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/product/service/impl/ProductImageServiceImpl.java) | Image upload/management |
| 15 | [ProductSpecification.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/product/repository/ProductSpecification.java) | Dynamic query builder (JPA Criteria API) |

### 📂 Thứ tự đọc — Promotion: Banner & Voucher (37 files)

| # | File | Lý do |
|---|------|-------|
| 16 | [Banner.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/promotion/entity/Banner.java) | Homepage banner entity |
| 17 | [Voucher.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/promotion/entity/Voucher.java) | Discount voucher: type (PERCENT/FIXED), min order, max discount, usage limit |
| 18 | [VoucherDiscountType.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/promotion/entity/VoucherDiscountType.java) | Enum: PERCENTAGE, FIXED_AMOUNT |
| 19 | [VoucherUse.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/promotion/entity/VoucherUse.java) | Track voucher usage per user |
| 20 | [VoucherServiceImpl.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/promotion/service/impl/VoucherServiceImpl.java) | Voucher CRUD + validate + apply discount logic |
| 21 | [BannerServiceImpl.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/promotion/service/impl/BannerServiceImpl.java) | Banner CRUD + `@Cacheable` |

### 🧠 Cần hiểu sâu

**JPA Specification pattern — Cách project search/filter:**
```java
// Thay vì viết 10 repository methods:
findByName(), findByCategory(), findByPriceBetween(), findByStatus()...

// Dùng 1 Specification compose nhiều điều kiện:
Specification<Product> spec = ProductSpecification.build(filter);
// → name LIKE %keyword% AND category = X AND price BETWEEN min-max AND status = ACTIVE
productRepository.findAll(spec, pageable);
```
→ Tái sử dụng, dễ mở rộng, clean code.

**Voucher validation flow:**
```
1. Check voucher status = ACTIVE
2. Check expiry date chưa hết hạn
3. Check min order amount (đơn hàng >= minOrderAmount?)
4. Check total usage (đã dùng < maxUses?)
5. Check per-user usage (user này đã dùng < maxUsesPerUser?)
6. Calculate: PERCENTAGE → min(total * percent, maxDiscount)
           FIXED → min(fixedAmount, total)
7. Tạo VoucherUse record
```

**Product ↔ Category relationship**: `@ManyToOne` — Category tree cho phép nested navigation (Điện tử → Laptop → Laptop Gaming). Frontend dựa vào parent-child relationship để render breadcrumb + sidebar filter.

**User entity liên kết với hầu hết mọi domain**: User → Orders, Cart, Reviews, Wishlist, Notifications, Addresses, Roles. Đây là central entity.

### ❓ Câu hỏi phỏng vấn
42. **JPA Specification pattern là gì? So với query methods?** → Dynamic query builder. Compose nhiều `Predicate` từ filter DTO. Ưu điểm: 1 method thay vì 10, dễ combine conditions. Nhược điểm: khó debug hơn native SQL.
43. **Voucher discount calculate thế nào? Edge cases?** → PERCENTAGE: [min(orderTotal * percent, maxDiscount)](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/realtime/service/SseEmitterService.java#47-59). FIXED: [min(fixedAmount, orderTotal)](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/realtime/service/SseEmitterService.java#47-59). Edge: voucher hết lượt giữa lúc user checkout → cần check lại lúc place order, không chỉ lúc hiển thị.
44. **Slug là gì? Tại sao cần?** → URL-friendly version of name ("Laptop Gaming" → "laptop-gaming"). SEO tốt hơn, user-friendly URL. Unique constraint trong DB. Phải handle trùng slug (thêm suffix `-1`, `-2`).

---

## 🗓️ Ngày 9: Backend — Inventory, Review, Activity, Home, Chatbot

### 📂 Thứ tự đọc — Inventory & Warehouse (22 files)

| # | File | Lý do |
|---|------|-------|
| 1 | [Warehouse.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/inventory/entity/Warehouse.java) | Warehouse locations |
| 2 | [Inventory.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/inventory/entity/Inventory.java) | Stock: product × warehouse → quantity |
| 3 | [InventoryServiceImpl.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/inventory/service/impl/InventoryServiceImpl.java) | Stock management + reservation logic |
| 4 | [WarehouseServiceImpl.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/inventory/service/impl/WarehouseServiceImpl.java) | Warehouse CRUD |

### 📂 Thứ tự đọc — Review (11 files)

| # | File | Lý do |
|---|------|-------|
| 5 | [Review.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/review/entity/Review.java) | User review: rating, comment, product |
| 6 | [ReviewServiceImpl.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/review/service/impl/ReviewServiceImpl.java) | Review CRUD + validation (đã mua mới được review?) |
| 7 | [UserReviewController.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/review/controller/user/UserReviewController.java) | User submit/edit reviews |

### 📂 Thứ tự đọc — Activity: Wishlist & Recent Views (16 files)

| # | File | Lý do |
|---|------|-------|
| 8 | [Wishlist.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/activity/entity/Wishlist.java) | User saved products |
| 9 | [RecentView.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/activity/entity/RecentView.java) | Recently viewed products (track for recommendations) |
| 10 | [WishlistServiceImpl.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/activity/service/impl/WishlistServiceImpl.java) | Wishlist toggle (add/remove) |
| 11 | [RecentViewServiceImpl.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/activity/service/impl/RecentViewServiceImpl.java) | Record & retrieve recent views |

### 📂 Thứ tự đọc — Home Aggregation (4 files)

| # | File | Lý do |
|---|------|-------|
| 12 | [HomeService.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/home/service/HomeService.java) | **Aggregation API**: gom banners, categories, new arrivals, best sellers, flash sale vào 1 response |
| 13 | [HomeResponse.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/home/dto/response/HomeResponse.java) | DTO chứa tất cả data cho homepage |
| 14 | [HomeController.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/home/controller/HomeController.java) | Endpoint: GET /api/public/home → cached 60s |

### 📂 Thứ tự đọc — Chatbot (31 files)

| # | File | Lý do |
|---|------|-------|
| 15 | [ChatbotProperties.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/chatbot/config/ChatbotProperties.java) | Config: provider, model, API key |
| 16 | [ChatbotService.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/chatbot/service/ChatbotService.java) | Core: send prompt → LLM → stream response |
| 17 | [ChatbotQueryService.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/chatbot/service/ChatbotQueryService.java) | Query service: conversations, messages history |
| 18 | [ChatbotSchemaProvider.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/chatbot/service/ChatbotSchemaProvider.java) | Provide product/category knowledge to LLM context |
| 19 | [ProjectKnowledgeService.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/chatbot/service/ProjectKnowledgeService.java) | RAG-like: inject project knowledge into prompts |
| 20 | [ChatbotController.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/chatbot/controller/ChatbotController.java) | Endpoints: create conversation, send message, stream response |
| 21 | [ChatConversation.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/chatbot/entity/ChatConversation.java) | Conversation entity |
| 22 | [ChatMessage.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/chatbot/entity/ChatMessage.java) | Individual message entity |
| 23 | [ChatGroup.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/chatbot/entity/ChatGroup.java) | Group chat feature |

### 🧠 Cần hiểu sâu

**Home Aggregation API — Tại sao gom thay vì gọi riêng?**
```
Không có aggregation:          Có aggregation:
GET /api/banners               GET /api/public/home
GET /api/categories             → 1 response chứa tất cả:
GET /api/products/new             banners + categories +
GET /api/products/best-seller     newArrivals + bestSellers +
GET /api/products/flash-sale      flashSale
= 5 HTTP requests              = 1 HTTP request (cached 60s)
```
→ Giảm 80% network round trips, frontend chỉ cần 1 `useQuery`.

**Chatbot RAG architecture:**
```
User hỏi "laptop nào rẻ nhất?" → ChatbotService:
1. ProjectKnowledgeService fetch product data từ DB
2. ChatbotSchemaProvider format thành context string
3. Inject context + user question vào LLM prompt
4. Stream response qua SSE (server-sent event)
5. Lưu transcript vào MongoDB (ChatbotTranscriptDocument)

Khi admin update product → RabbitMQ event → ChatbotCacheConsumer
→ invalidate knowledge cache → lần chat tiếp sẽ dùng data mới
```

**Inventory concurrency problem:**
```
Stock = 1. User A checkout → read stock=1 → OK
              User B checkout → read stock=1 → OK
              User A commit → stock=0
              User B commit → stock=-1 ❌ OVERSOLD!
```
Giải pháp: `SELECT ... FOR UPDATE` (pessimistic lock) hoặc `WHERE stock >= quantity` (atomic check-and-update).

### ❓ Câu hỏi phỏng vấn
45. **API Aggregation pattern ưu nhược điểm?** → Ưu: giảm round trips, 1 cache key. Nhược: 1 source chậm → cả response chậm, khó cache invalidation riêng từng phần. Giải pháp: `CompletableFuture.allOf()` parallel fetch + timeout.
46. **Inventory race condition giải quyết thế nào?** → Pessimistic lock (`SELECT FOR UPDATE` → serialize queries), Optimistic lock (`@Version` → retry on conflict), hoặc atomic SQL `UPDATE SET stock = stock - :qty WHERE stock >= :qty` (rows affected = 0 → sold out).
47. **RAG (Retrieval Augmented Generation) là gì?** → Inject relevant data (products, categories) vào LLM prompt → chatbot trả lời dựa trên data thật thay vì hallucinate. Project dùng: SchemaProvider format DB data → inject vào system prompt.

---

## 🗓️ Ngày 10: Frontend — Infrastructure & API Layer

> Frontend có tổng ~100 files: 24 storefront pages, 22 admin pages, 8 shared components, 4 UI primitives, 31 lib files, 9 app files. Chia 3 ngày đọc từ foundation → pages → admin.

### 📂 Thứ tự đọc — Config & Entry

| # | File | Lý do |
|---|------|-------|
| 1 | [package.json](file:///d:/Projects/Web/Ecommerce/frontend/package.json) | Stack: React 19, Vite 7, TypeScript 5.9, TailwindCSS 3, React Query 5, Zustand 5, React Router 7 |
| 2 | [vite.config.ts](file:///d:/Projects/Web/Ecommerce/frontend/vite.config.ts) | Vite config: plugins, path aliases |
| 3 | [tailwind.config.js](file:///d:/Projects/Web/Ecommerce/frontend/tailwind.config.js) | Design tokens: colors, spacing, breakpoints |
| 4 | [index.css](file:///d:/Projects/Web/Ecommerce/frontend/src/index.css) | Global styles, CSS variables, base overrides |
| 5 | [main.tsx](file:///d:/Projects/Web/Ecommerce/frontend/src/main.tsx) | Entry point: providers wrapping (QueryClient, Auth, Theme, Toast, Notification) |

### 📂 Thứ tự đọc — App-level (Providers, Guards, Layout)

| # | File | Lý do |
|---|------|-------|
| 6 | [router.tsx](file:///d:/Projects/Web/Ecommerce/frontend/src/app/router.tsx) | **138 dòng**. Tất cả routes + `React.lazy()` code splitting + Suspense fallback. Hiểu route nesting: public → RequireAuth → RequireAdmin → AdminLayout |
| 7 | [AppLayout.tsx](file:///d:/Projects/Web/Ecommerce/frontend/src/app/AppLayout.tsx) | Main layout: header, nav, footer, Outlet |
| 8 | [AuthProvider.tsx](file:///d:/Projects/Web/Ecommerce/frontend/src/app/AuthProvider.tsx) | Auth context: token state, auto login check |
| 9 | [NotificationProvider.tsx](file:///d:/Projects/Web/Ecommerce/frontend/src/app/NotificationProvider.tsx) | SSE subscribe + notification badge count |
| 10 | [ThemeProvider.tsx](file:///d:/Projects/Web/Ecommerce/frontend/src/app/ThemeProvider.tsx) | Dark/light mode toggle |
| 11 | [ToastProvider.tsx](file:///d:/Projects/Web/Ecommerce/frontend/src/app/ToastProvider.tsx) | Global toast notification system |
| 12 | [RequireAuth.tsx](file:///d:/Projects/Web/Ecommerce/frontend/src/app/RequireAuth.tsx) | Route guard: redirect /login nếu chưa auth |
| 13 | [RequireAdmin.tsx](file:///d:/Projects/Web/Ecommerce/frontend/src/app/RequireAdmin.tsx) | Route guard: chỉ ROLE_ADMIN vào admin routes |

### 📂 Thứ tự đọc — Lib core (HTTP, Auth, State)

| # | File | Lý do |
|---|------|-------|
| 14 | [http.ts](file:///d:/Projects/Web/Ecommerce/frontend/src/lib/http.ts) | **Quan trọng nhất**. [apiJson()](file:///d:/Projects/Web/Ecommerce/frontend/src/lib/http.ts#77-131) + [apiMultipart()](file:///d:/Projects/Web/Ecommerce/frontend/src/lib/http.ts#133-158): auto token refresh (single in-flight), requestId header, typed errors. 158 dòng |
| 15 | [apiClient.ts](file:///d:/Projects/Web/Ecommerce/frontend/src/lib/apiClient.ts) | Wrapper cũ hoặc high-level client |
| 16 | [apiError.ts](file:///d:/Projects/Web/Ecommerce/frontend/src/lib/apiError.ts) | Typed API error class |
| 17 | [errors.ts](file:///d:/Projects/Web/Ecommerce/frontend/src/lib/errors.ts) | Error handling utilities |
| 18 | [authStorage.ts](file:///d:/Projects/Web/Ecommerce/frontend/src/lib/authStorage.ts) | Token persistence (localStorage): get/set/clear |
| 19 | [env.ts](file:///d:/Projects/Web/Ecommerce/frontend/src/lib/env.ts) | `VITE_API_BASE_URL` config |
| 20 | [sse.ts](file:///d:/Projects/Web/Ecommerce/frontend/src/lib/sse.ts) | [createAuthedEventSource()](file:///d:/Projects/Web/Ecommerce/frontend/src/lib/sse.ts#4-12): SSE client truyền access token qua query param |

### 📂 Thứ tự đọc — State stores (Zustand)

| # | File | Lý do |
|---|------|-------|
| 21 | [useAuthStore.ts](file:///d:/Projects/Web/Ecommerce/frontend/src/lib/stores/useAuthStore.ts) | Auth state: user, isLoggedIn, login/logout actions |
| 22 | [useCartStore.ts](file:///d:/Projects/Web/Ecommerce/frontend/src/lib/stores/useCartStore.ts) | Cart state: items, counts, add/remove actions |

### 📂 Thứ tự đọc — API layer (domain-specific)

Lướt qua từng file để hiểu cách frontend gọi backend APIs:

| # | File | Domain |
|---|------|--------|
| 23 | [authApi.ts](file:///d:/Projects/Web/Ecommerce/frontend/src/lib/authApi.ts) | Login, register, OTP, refresh |
| 24 | [userApi.ts](file:///d:/Projects/Web/Ecommerce/frontend/src/lib/userApi.ts) | Profile, addresses, password |
| 25 | [cartApi.ts](file:///d:/Projects/Web/Ecommerce/frontend/src/lib/cartApi.ts) | Cart + guest cart CRUD |
| 26 | [orderApi.ts](file:///d:/Projects/Web/Ecommerce/frontend/src/lib/orderApi.ts) | Orders, checkout, guest order |
| 27 | [paymentApi.ts](file:///d:/Projects/Web/Ecommerce/frontend/src/lib/paymentApi.ts) | Payment records |
| 28 | [momoApi.ts](file:///d:/Projects/Web/Ecommerce/frontend/src/lib/momoApi.ts) | MoMo payment flow |
| 29 | [chatbotApi.ts](file:///d:/Projects/Web/Ecommerce/frontend/src/lib/chatbotApi.ts) | Chatbot: conversations, messages, groups, streaming |
| 30 | [notificationApi.ts](file:///d:/Projects/Web/Ecommerce/frontend/src/lib/notificationApi.ts) | Notifications CRUD |
| 31 | [voucherApi.ts](file:///d:/Projects/Web/Ecommerce/frontend/src/lib/voucherApi.ts) | Vouchers |
| 32 | [wishlistApi.ts](file:///d:/Projects/Web/Ecommerce/frontend/src/lib/wishlistApi.ts) | Wishlist |
| 33 | [recentViewApi.ts](file:///d:/Projects/Web/Ecommerce/frontend/src/lib/recentViewApi.ts) | Recent views |
| 34 | [reviewApi.ts](file:///d:/Projects/Web/Ecommerce/frontend/src/lib/reviewApi.ts) | Reviews |
| 35 | [adminApi.ts](file:///d:/Projects/Web/Ecommerce/frontend/src/lib/adminApi.ts) | Admin-specific API calls |

### 🧠 Điểm hay trong [http.ts](file:///d:/Projects/Web/Ecommerce/frontend/src/lib/http.ts)
- **Auto token refresh**: 401/403 → gọi refresh → retry → seamless UX
- **Single refresh in-flight**: `refreshInFlight` promise → nhiều request 401 cùng lúc chỉ refresh 1 lần (tránh race condition)
- **RequestId**: `crypto.randomUUID()` → gửi kèm mọi request → correlate với backend logs

### 🔄 So sánh Frontend State Management

| Tool | Server state? | Client state? | Cache? | Dùng khi |
|------|:---:|:---:|:---:|---|
| **React Query (project dùng)** | ✅ | ❌ | ✅ Auto | API data: fetch, cache, refetch, optimistic |
| **Zustand (project dùng)** | ❌ | ✅ | ❌ | Client UI state: auth, cart, theme |
| **Redux Toolkit** | ⚠️ Manual | ✅ | ⚠️ RTK Query | Large complex state |
| **Jotai / Recoil** | ❌ | ✅ | ❌ | Atomic state management |
| **Context API** | ❌ | ✅ | ❌ | Simple shared state, nhưng re-render nhiều |

### 🔄 So sánh Frontend Build Tools

| Tool | HMR Speed | Config | Ecosystem | Dùng khi |
|------|:---:|:---:|:---:|---|
| **Vite (project dùng)** | Rất nhanh (ESM) | Minimal | Tốt | SPA, modern browsers |
| **Webpack (CRA)** | Chậm hơn | Nhiều config | Rất lớn | Legacy, phức tạp |
| **Turbopack** | Nhanh | Next.js only | Mới | Next.js projects |
| **esbuild** | Cực nhanh | Minimal | Nhỏ | Bundling library code |

### 🔄 So sánh CSS Frameworks

| Framework | Approach | Customization | Bundle size | Dùng khi |
|-----------|---------|:---:|:---:|---|
| **TailwindCSS (project dùng)** | Utility-first | ✅ Config file | Purged = nhỏ | Rapid prototyping, consistency |
| **Vanilla CSS / CSS Modules** | Traditional | ✅ Full control | Manual | Full control needed |
| **Styled Components** | CSS-in-JS | ✅ | Runtime overhead | Component-scoped, dynamic |
| **shadcn/ui (project dùng)** | Component library | ✅ Copy-paste | Only what you use | Pre-built accessible components |

### ❓ Câu hỏi phỏng vấn
23. **React Query vs Zustand: khi nào dùng cái nào?** → React Query cho server state (API data, caching, refetch). Zustand cho client state (UI flags, auth, cart). Tách 2 loại state = clean architecture.
24. **Auto token refresh pattern hoạt động thế nào?** → 401 → check `refreshInFlight` (đang refresh chưa?) → gọi refresh → retry. Single in-flight tránh N requests cùng gọi refresh.
25. **SPA routing vs SSR?** → SPA: client render, fast navigation, SEO kém. SSR (Next.js): server render, SEO tốt, phức tạp hơn. Project là e-commerce internal → SPA đủ.
26. **`React.lazy()` + `Suspense` là gì?** → Code splitting: mỗi page chỉ load khi user navigate đến → initial bundle nhỏ → load nhanh hơn. `Suspense` hiển thị fallback (loading) trong khi chunk đang load.
27. **TailwindCSS ưu nhược điểm?** → Ưu: rapid dev, consistent design, purge unused CSS. Nhược: HTML dài, learning curve, khó custom complex animations.

---

## 🗓️ Ngày 11: Frontend — Storefront Pages & Components

> **Mục tiêu**: Đọc các trang customer-facing. Focus vào luồng user journey: Home → Search → Product → Cart → Checkout → Order.

### 📂 Thứ tự đọc — Shared Components (đọc trước vì pages dùng)

| # | File | Lý do |
|---|------|-------|
| 1 | [ProductCard.tsx](file:///d:/Projects/Web/Ecommerce/frontend/src/components/ProductCard.tsx) | Reusable product display: image, price, rating, wishlist button |
| 2 | [RatingStars.tsx](file:///d:/Projects/Web/Ecommerce/frontend/src/components/RatingStars.tsx) | Star rating component |
| 3 | [CategoryIcon.tsx](file:///d:/Projects/Web/Ecommerce/frontend/src/components/CategoryIcon.tsx) | Category icon mapping |
| 4 | [SafeImage.tsx](file:///d:/Projects/Web/Ecommerce/frontend/src/components/SafeImage.tsx) | Image error fallback |
| 5 | [ConfirmDialog.tsx](file:///d:/Projects/Web/Ecommerce/frontend/src/components/ConfirmDialog.tsx) | Reusable confirm modal |
| 6 | [Modal.tsx](file:///d:/Projects/Web/Ecommerce/frontend/src/components/Modal.tsx) | Base modal component |
| 7 | [EmptyState.tsx](file:///d:/Projects/Web/Ecommerce/frontend/src/components/EmptyState.tsx) | Empty data display |
| 8 | [LoadingCard.tsx](file:///d:/Projects/Web/Ecommerce/frontend/src/components/LoadingCard.tsx) | Loading skeleton |

### 📂 Thứ tự đọc — Storefront Pages (theo user journey)

| # | File | Size | Luồng |
|---|------|------|-------|
| 9 | [HomePage.tsx](file:///d:/Projects/Web/Ecommerce/frontend/src/pages/HomePage.tsx) | 38KB | Banners, categories, new arrivals, best sellers, chatbot widget |
| 10 | [CategoriesPage.tsx](file:///d:/Projects/Web/Ecommerce/frontend/src/pages/CategoriesPage.tsx) | 10KB | Browse categories |
| 11 | [CategoryDetailPage.tsx](file:///d:/Projects/Web/Ecommerce/frontend/src/pages/CategoryDetailPage.tsx) | 11KB | Category products + pagination |
| 12 | [ProductsPage.tsx](file:///d:/Projects/Web/Ecommerce/frontend/src/pages/ProductsPage.tsx) | 33KB | Search + filters + pagination |
| 13 | [ProductDetailPage.tsx](file:///d:/Projects/Web/Ecommerce/frontend/src/pages/ProductDetailPage.tsx) | 31KB | Detail, images, reviews, add to cart, wishlist |
| 14 | [CartPage.tsx](file:///d:/Projects/Web/Ecommerce/frontend/src/pages/CartPage.tsx) | 15KB | Cart items, quantity, remove, subtotal |
| 15 | [CheckoutPage.tsx](file:///d:/Projects/Web/Ecommerce/frontend/src/pages/CheckoutPage.tsx) | 33KB | **Quan trọng**: address selection, voucher, pricing summary, place order, idempotency |
| 16 | [MomoQrPaymentPage.tsx](file:///d:/Projects/Web/Ecommerce/frontend/src/pages/MomoQrPaymentPage.tsx) | 13KB | MoMo QR display, payment status polling |
| 17 | [OrdersPage.tsx](file:///d:/Projects/Web/Ecommerce/frontend/src/pages/OrdersPage.tsx) | 9KB | Order history list |
| 18 | [OrderDetailPage.tsx](file:///d:/Projects/Web/Ecommerce/frontend/src/pages/OrderDetailPage.tsx) | 22KB | Full order detail: items, status, payment, timeline |
| 19 | [GuestOrderPage.tsx](file:///d:/Projects/Web/Ecommerce/frontend/src/pages/GuestOrderPage.tsx) | 7KB | Guest order lookup bằng access token |
| 20 | [LoginPage.tsx](file:///d:/Projects/Web/Ecommerce/frontend/src/pages/LoginPage.tsx) | 6KB | Login form → auth API |
| 21 | [RegisterPage.tsx](file:///d:/Projects/Web/Ecommerce/frontend/src/pages/RegisterPage.tsx) | 7KB | Register + OTP verify flow |
| 22 | [ProfilePage.tsx](file:///d:/Projects/Web/Ecommerce/frontend/src/pages/ProfilePage.tsx) | 5KB | User profile display |
| 23 | [ProfileEditPage.tsx](file:///d:/Projects/Web/Ecommerce/frontend/src/pages/ProfileEditPage.tsx) | 8KB | Edit profile form |
| 24 | [AddressesPage.tsx](file:///d:/Projects/Web/Ecommerce/frontend/src/pages/AddressesPage.tsx) | 12KB | Address book CRUD |
| 25 | [WishlistPage.tsx](file:///d:/Projects/Web/Ecommerce/frontend/src/pages/WishlistPage.tsx) | 8KB | Saved products |
| 26 | [NotificationsPage.tsx](file:///d:/Projects/Web/Ecommerce/frontend/src/pages/NotificationsPage.tsx) | 8KB | Notification list + SSE live updates |
| 27 | [MyVouchersPage.tsx](file:///d:/Projects/Web/Ecommerce/frontend/src/pages/MyVouchersPage.tsx) | 9KB | User voucher list |
| 28 | [MyVoucherDetailPage.tsx](file:///d:/Projects/Web/Ecommerce/frontend/src/pages/MyVoucherDetailPage.tsx) | 6KB | Voucher detail |
| 29 | [VoucherUsesPage.tsx](file:///d:/Projects/Web/Ecommerce/frontend/src/pages/VoucherUsesPage.tsx) | 7KB | Voucher usage history |
| 30 | [PasswordPage.tsx](file:///d:/Projects/Web/Ecommerce/frontend/src/pages/PasswordPage.tsx) | 6KB | Change password |
| 31 | [ChatbotWidget.tsx](file:///d:/Projects/Web/Ecommerce/frontend/src/app/ChatbotWidget.tsx) | 33KB | Chatbot: conversations, streaming, voice, groups |

### 🧠 Khi đọc pages, tập trung vào:
- **React Query patterns**: `useQuery` (fetch), `useMutation` (create/update/delete), `queryClient.invalidateQueries` (cache invalidation)
- **Form handling**: controlled inputs, validation, error display
- **Optimistic updates**: update UI trước khi server confirm (nếu có)
- **Pagination pattern**: page state + API params + prev/next navigation

### ❓ Câu hỏi phỏng vấn
28. **React Query `useQuery` vs `useMutation`?** → `useQuery`: auto fetch + cache + refetch (GET). `useMutation`: manual trigger (POST/PUT/DELETE) + `onSuccess` invalidate cache.
29. **Controlled vs Uncontrolled components?** → Controlled: React state quản lý value. Uncontrolled: DOM tự quản lý (ref). Project dùng controlled cho form validation.
30. **Code splitting giúp gì?** → Mỗi page là 1 chunk riêng → initial page load chỉ download code cần thiết → FCP (First Contentful Paint) nhanh hơn.

---

## 🗓️ Ngày 12: Frontend — Admin Dashboard Pages

> **Mục tiêu**: Đọc admin pages — CRUD tables, filters, analytics charts.

### 📂 Thứ tự đọc — Admin Layout

| # | File | Lý do |
|---|------|-------|
| 1 | [AdminLayout.tsx](file:///d:/Projects/Web/Ecommerce/frontend/src/admin/AdminLayout.tsx) | Sidebar navigation + content area |
| 2 | [adminNav.ts](file:///d:/Projects/Web/Ecommerce/frontend/src/admin/adminNav.ts) | Navigation menu items config |

### 📂 Thứ tự đọc — Admin Pages (theo domain)

**Dashboard & Analytics:**

| # | File | Size | Mô tả |
|---|------|------|-------|
| 3 | [AdminHomePage.tsx](file:///d:/Projects/Web/Ecommerce/frontend/src/pages/admin/AdminHomePage.tsx) | 15KB | Dashboard overview: stats, recent orders |
| 4 | [AdminAnalyticsPage.tsx](file:///d:/Projects/Web/Ecommerce/frontend/src/pages/admin/AdminAnalyticsPage.tsx) | 13KB | Funnel analytics, top products, conversion rates |

**Product & Catalog Management:**

| # | File | Size | Mô tả |
|---|------|------|-------|
| 5 | [AdminProductsPage.tsx](file:///d:/Projects/Web/Ecommerce/frontend/src/pages/admin/AdminProductsPage.tsx) | 16KB | Products CRUD table + filters |
| 6 | [AdminCategoriesPage.tsx](file:///d:/Projects/Web/Ecommerce/frontend/src/pages/admin/AdminCategoriesPage.tsx) | 13KB | Categories CRUD |
| 7 | [AdminProductImagesPage.tsx](file:///d:/Projects/Web/Ecommerce/frontend/src/pages/admin/AdminProductImagesPage.tsx) | 13KB | Product images management |

**Order & Payment:**

| # | File | Size | Mô tả |
|---|------|------|-------|
| 8 | [AdminOrdersPage.tsx](file:///d:/Projects/Web/Ecommerce/frontend/src/pages/admin/AdminOrdersPage.tsx) | 19KB | Orders table + status update (state machine UI) |
| 9 | [AdminOrderItemsPage.tsx](file:///d:/Projects/Web/Ecommerce/frontend/src/pages/admin/AdminOrderItemsPage.tsx) | 11KB | Order items detail |
| 10 | [AdminPaymentsPage.tsx](file:///d:/Projects/Web/Ecommerce/frontend/src/pages/admin/AdminPaymentsPage.tsx) | 14KB | Payment records + status |

**User & Access:**

| # | File | Size | Mô tả |
|---|------|------|-------|
| 11 | [AdminUsersPage.tsx](file:///d:/Projects/Web/Ecommerce/frontend/src/pages/admin/AdminUsersPage.tsx) | 20KB | User management CRUD |
| 12 | [AdminRolesPage.tsx](file:///d:/Projects/Web/Ecommerce/frontend/src/pages/admin/AdminRolesPage.tsx) | 8KB | Role management |
| 13 | [AdminAddressesPage.tsx](file:///d:/Projects/Web/Ecommerce/frontend/src/pages/admin/AdminAddressesPage.tsx) | 12KB | User addresses admin view |

**Inventory & Promotions:**

| # | File | Size | Mô tả |
|---|------|------|-------|
| 14 | [AdminWarehousesPage.tsx](file:///d:/Projects/Web/Ecommerce/frontend/src/pages/admin/AdminWarehousesPage.tsx) | 16KB | Warehouses CRUD |
| 15 | [AdminInventoriesPage.tsx](file:///d:/Projects/Web/Ecommerce/frontend/src/pages/admin/AdminInventoriesPage.tsx) | 16KB | Inventory management |
| 16 | [AdminBannersPage.tsx](file:///d:/Projects/Web/Ecommerce/frontend/src/pages/admin/AdminBannersPage.tsx) | 13KB | Banner management |
| 17 | [AdminVouchersPage.tsx](file:///d:/Projects/Web/Ecommerce/frontend/src/pages/admin/AdminVouchersPage.tsx) | 19KB | Voucher CRUD |
| 18 | [AdminVoucherUsesPage.tsx](file:///d:/Projects/Web/Ecommerce/frontend/src/pages/admin/AdminVoucherUsesPage.tsx) | 11KB | Voucher usage history |

**Operations:**

| # | File | Size | Mô tả |
|---|------|------|-------|
| 19 | [AdminNotificationsPage.tsx](file:///d:/Projects/Web/Ecommerce/frontend/src/pages/admin/AdminNotificationsPage.tsx) | 14KB | Notifications management |
| 20 | [AdminAuditLogsPage.tsx](file:///d:/Projects/Web/Ecommerce/frontend/src/pages/admin/AdminAuditLogsPage.tsx) | 10KB | Audit log viewer + filters |
| 21 | [AdminReviewsPage.tsx](file:///d:/Projects/Web/Ecommerce/frontend/src/pages/admin/AdminReviewsPage.tsx) | 12KB | Review moderation |

**Admin Profile:**

| # | File | Size | Mô tả |
|---|------|------|-------|
| 22 | [AdminProfilePage.tsx](file:///d:/Projects/Web/Ecommerce/frontend/src/pages/admin/AdminProfilePage.tsx) | 4KB | Admin profile view |
| 23 | [AdminProfileEditPage.tsx](file:///d:/Projects/Web/Ecommerce/frontend/src/pages/admin/AdminProfileEditPage.tsx) | 5KB | Edit admin profile |
| 24 | [AdminPasswordPage.tsx](file:///d:/Projects/Web/Ecommerce/frontend/src/pages/admin/AdminPasswordPage.tsx) | 5KB | Admin change password |

### 🧠 Pattern chung của admin pages
- **Table + Filter + Pagination**: mỗi page có search/filter bar → React Query fetch → data table → pagination
- **CRUD Modal pattern**: Create/Edit dùng modal form, Delete dùng confirm dialog
- **Optimistic updates**: một số pages update UI trước khi API response
- **Admin pages dùng [adminApi.ts](file:///d:/Projects/Web/Ecommerce/frontend/src/lib/adminApi.ts)** → tất cả gọi `/api/admin/**` endpoints

### ❓ Câu hỏi phỏng vấn
31. **Table pagination implement thế nào?** → State `page` + `size` → gửi query params → backend trả `Page<T>` (content + totalPages + totalElements) → render pagination controls.
32. **Filter/Search debounce?** → User gõ → đợi 300ms không gõ thêm mới gọi API. Tránh gọi API mỗi keystroke. Dùng `setTimeout` hoặc custom hook `useDebounce`.
33. **Component reusability trong admin?** → Shared pattern: table component, filter bar, CRUD modal → tách thành composable pieces. DRY principle.

---

## 🗓️ Ngày 13: Docker + Docker Compose

### 📂 Thứ tự đọc

| # | File | Lý do |
|---|------|-------|
| 1 | [backend/Dockerfile](file:///d:/Projects/Web/Ecommerce/backend/Dockerfile) | Multi-stage build: Maven build → JRE runtime (không có Maven/src trong final image) |
| 2 | [frontend/Dockerfile](file:///d:/Projects/Web/Ecommerce/frontend/Dockerfile) | Multi-stage: Node build → Nginx serve static (SPA fallback config) |
| 3 | [docker-compose.yml](file:///d:/Projects/Web/Ecommerce/docker-compose.yml) | **8 services**: mysql, redis, rabbitmq, elasticsearch, mongodb, backend, frontend, ngrok |

### 🧠 Multi-stage build — Tại sao quan trọng?

**Backend Dockerfile:**
```dockerfile
# Stage 1: BUILD — có Maven, JDK, source code
FROM maven:3.9.9-eclipse-temurin-21 AS build
RUN ./mvnw -DskipTests clean package

# Stage 2: RUNTIME — chỉ JRE + JAR file
FROM eclipse-temurin:21-jre
COPY --from=build /app/target/*.jar /app/app.jar
```
→ Image final **nhỏ hơn rất nhiều** (không chứa Maven, source, dependencies)

**Frontend Dockerfile:**
```dockerfile
# Stage 1: BUILD — Node + npm
FROM node:20-alpine AS build
RUN npm run build

# Stage 2: SERVE — Nginx serve static files
FROM nginx:1.27-alpine
COPY --from=build /app/dist /usr/share/nginx/html
```
→ `try_files $uri $uri/ /index.html` = SPA fallback: mọi route đều serve index.html

**docker-compose.yml điểm hay:**
- `depends_on` + `condition: service_healthy` → backend đợi MySQL healthy mới start
- Volumes persist data qua restart
- ENV từ [.env](file:///d:/Projects/Web/Ecommerce/.env) file → tách config khỏi code
- Ngrok service → expose localhost ra internet (cho MoMo IPN callback khi dev)

### 🔄 So sánh Container & Orchestration

| Tool | Dùng khi | Single host? | Multi host? |
|------|----------|:---:|:---:|
| **Docker Compose (project)** | Dev + single-server deploy | ✅ | ❌ |
| **Kubernetes** | Production multi-host | ✅ | ✅ |
| **Docker Swarm** | Simple multi-host | ✅ | ✅ |
| **Bare metal (no Docker)** | Dev only | ✅ | ❌ |

**Nếu không dùng Docker?** → Phải cài MySQL, Redis, RabbitMQ, ES, Mongo trên máy local → version conflicts, "works on my machine" problem. Docker = consistent environment.

### ❓ Câu hỏi phỏng vấn
34. **Multi-stage build là gì? Lợi ích?** → Nhiều FROM statements. Stage đầu build, stage cuối chỉ copy artifact. Giảm image size, không lộ source code/build tools.
35. **`docker compose up --build -d` là gì?** → `--build`: rebuild images nếu code thay đổi. `-d`: detached mode (chạy background).
36. **Healthcheck trong docker compose?** → Container chưa healthy → dependent services chưa start. Tránh backend connect MySQL trước khi MySQL sẵn sàng.
37. **Volume dùng để làm gì?** → Persist data bên ngoài container. Container bị xóa/recreate → data vẫn còn.

---

## 🗓️ Ngày 14: CI/CD — GitHub Actions Pipeline

### 📂 Thứ tự đọc

| # | File | Lý do |
|---|------|-------|
| 1 | [platform-ci.yml](file:///d:/Projects/Web/Ecommerce/.github/workflows/platform-ci.yml) | **3 jobs**: backend test (Maven), frontend build (Vite), Docker build validation |
| 2 | [security-gates.yml](file:///d:/Projects/Web/Ecommerce/.github/workflows/security-gates.yml) | **4 jobs**: CodeQL SAST, OWASP dependency scan, Gitleaks secret scan, DAST smoke |
| 3 | [staging-deploy.yml](file:///d:/Projects/Web/Ecommerce/.github/workflows/staging-deploy.yml) | SSH deploy to AWS staging → `docker compose up --build -d` → smoke test (health + frontend) |
| 4 | [production-promote.yml](file:///d:/Projects/Web/Ecommerce/.github/workflows/production-promote.yml) | Manual dispatch → SSH deploy production → smoke test |

### 🧠 CI/CD Pipeline flow
```
Developer push code
    ↓
[platform-ci.yml] — Tự động on push/PR
├── Backend: mvnw test (unit + integration tests)
├── Frontend: npm run build (TypeScript check + build)
└── Docker: build images + validate compose config

[security-gates.yml] — Tự động on push/PR
├── CodeQL: Static Analysis (tìm SQL injection, XSS, etc.)
├── OWASP: Dependency vulnerability scan (CVSS ≥ 7 = fail)
├── Gitleaks: Scan secrets trong code (API keys, passwords)
└── DAST: Dynamic smoke test public endpoints

[staging-deploy.yml] — Auto on push to phase7 branch
└── SSH → AWS staging → git pull → docker compose up → health check

[production-promote.yml] — Manual trigger only
└── SSH → AWS production → git pull → docker compose up → health check
```

**Staging vs Production:**
- Staging: auto deploy → test nhanh
- Production: `workflow_dispatch` (manual) → human approval → safe

### 🔄 So sánh CI/CD Tools

| Tool | Hosting | Config | Ecosystem | Free tier |
|------|---------|--------|-----------|-----------|
| **GitHub Actions (project)** | Cloud | YAML | GitHub-native | 2000 min/month |
| **Jenkins** | Self-hosted | Groovy/DSL | Rất lớn | Free (self-host) |
| **GitLab CI** | Cloud/Self | YAML | GitLab-native | 400 min/month |
| **CircleCI** | Cloud | YAML | Tốt | 6000 min/month |

### 🔄 So sánh Security Scan Types

| Scan | Tìm gì | Khi nào | Tool trong project |
|------|---------|---------|-----------|
| **SAST** | Code vulnerabilities (SQL injection, XSS) | Build time | CodeQL |
| **Dependency Scan** | Known CVE trong libraries | Build time | OWASP Dependency-Check |
| **Secret Scan** | Leaked credentials trong code | Commit time | Gitleaks |
| **DAST** | Runtime vulnerabilities | After deploy | Custom smoke script |

### ❓ Câu hỏi phỏng vấn
38. **CI vs CD là gì?** → CI (Continuous Integration): auto build + test mỗi commit. CD (Continuous Deployment/Delivery): auto deploy sau khi CI pass.
39. **Blue-green deployment là gì?** → 2 môi trường giống hệt nhau (blue=current, green=new). Switch traffic khi green ready. Rollback = switch lại blue.
40. **SAST vs DAST?** → SAST: phân tích source code tĩnh (không chạy app). DAST: test app đang chạy (gửi request thật). SAST tìm code-level bugs, DAST tìm runtime/config bugs.
41. **Tại sao production deploy manual?** → Human approval trước khi deploy production. Tránh bug tự động deploy lên production. Staging auto OK vì ảnh hưởng nhỏ.

---

## 📋 Files infrastructure nên lướt qua bất kỳ lúc nào

| File | Mô tả |
|------|-------|
| [BaseEntity.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/common/base/BaseEntity.java) | `@MappedSuperclass`: createdAt, updatedAt auto |
| [SortableFilter.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/common/base/SortableFilter.java) | Base filter cho pagination + sorting |
| [PageableUtils.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/common/utils/PageableUtils.java) | Specification pattern search utility |
| [AsyncConfig.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/common/config/AsyncConfig.java) | Thread pool cho `@Async` |
| [SwaggerConfig.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/common/config/SwaggerConfig.java) | OpenAPI config |
| [pom.xml](file:///d:/Projects/Web/Ecommerce/backend/pom.xml) | Tất cả dependencies |
| [PROJECT_PLAN.md](file:///d:/Projects/Web/Ecommerce/docs/roadmaps/PROJECT_PLAN.md) | 7 phases roadmap chi tiết |

---

## 🎯 Chiến lược đọc hiệu quả

1. **Mỗi ngày đọc theo đúng thứ tự** — sắp xếp từ config → entity → service → controller
2. **Mỗi file đọc xong, tự hỏi 3 câu:**
   - *"Nếu bỏ file này thì hệ thống sẽ thiếu gì?"*
   - *"Có cách đơn giản hơn không? Trade-off?"*
   - *"Nếu phỏng vấn hỏi về file này, mình trả lời được không?"*
3. **Debug tracing**: Sau mỗi ngày, thử trace 1 request từ controller → service → repository → response (đặt breakpoint hoặc thêm log)
4. **Ngày cuối**: Quay lại [OrderServiceImpl.java](file:///d:/Projects/Web/Ecommerce/backend/src/main/java/com/minzetsu/ecommerce/order/service/impl/OrderServiceImpl.java) và trace toàn bộ flow — file này **tổng hợp gần như mọi thứ**: security, idempotency, cart, voucher, events, SSE, clickstream
5. **Tài liệu tham khảo nhanh**: Mở [README.md](file:///d:/Projects/Web/Ecommerce/README.md) để xem architecture diagram và domain map bất kỳ lúc nào

> 💡 **Tip**: Khi phỏng vấn bị hỏi về phần không rõ, thay vì nói "em không biết", hãy nói *"Em hiểu concept nhưng trong dự án em dùng [X], cách khác là [Y] với trade-off [Z]"* — thể hiện tư duy so sánh.
