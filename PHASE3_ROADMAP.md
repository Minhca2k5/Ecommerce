# Phase 3 Roadmap: Performance, Scalability, Reliability (Backend-First)

Goal: measurable latency improvements (baseline + p95), higher throughput, and
more predictable behavior under load using caching, DB tuning, observability,
and resilience. Frontend work is optional and limited to request-id headers if
needed.

Guiding principles:
- No blind tuning: measure before/after with clear SLOs.
- Focus hot paths: Home, Product detail, Search, Cart, Checkout, Orders.
- Cache with explicit key/TTL/invalidation to avoid stale data and stampede.
- Any Phase 3 change must include minimal logging/metrics + rollback note.

Current notes:
- No Redis/Spring Cache yet.
- No correlation id, metrics dashboard, or tracing.
- `HomeService` aggregates multiple services per request; good cache candidate.
- `ProductService` computes multiple metrics per product; possible N+1 or chatty
  queries when listing/ranking.

---

## Phase 3 Definition of Done (DoD)
- Baseline + after report for key flows (p50/p95, error rate, throughput).
- Redis caching for read-heavy endpoints with measured hit rate.
- Top slow queries identified and addressed (index or query refactor).
- Rate limit + timeout + retry/backoff + circuit breaker for hot endpoints.
- Observability: structured logs + correlation id + metrics (Actuator).
- Minimal frontend changes only if needed for request-id propagation.

---

## 0) Baseline & Scope (Milestone P3-M0)

### 0.1) Pick SLOs + benchmark scope
Endpoints/flows:
- Home: `GET /api/public/home`
- Product list: `GET /api/public/products` (+ filter/paging)
- Product detail: `GET /api/public/products/slug/{slug}` (or `{id}`)
- Cart: `GET/POST/PUT/DELETE /api/users/me/carts/**`
- Checkout: `POST /api/users/me/orders` + payment create
- Orders: `GET /api/users/me/orders`, `GET /api/users/me/orders/{id}`

Suggested SLO:
- p95 latency (read-heavy) 300-500ms local; error rate < 1%.

### 0.2) Perf artifacts
Create `docs/perf/` containing:
- k6 or JMeter scripts
- baseline results (CSV/JSON)
- report template (before/after)

Checkpoint:
- Baseline metrics for 5-6 flows.

---

## 1) Observability Foundation (Milestone P3-M1)

### 1.1) Request ID (correlation)
- Backend filter generates `X-Request-Id` if missing, stores in MDC.
- Response always returns `X-Request-Id`.
- Frontend adds `X-Request-Id` header only if needed.

### 1.2) Structured logging
- Use JSON or structured pattern with fields:
  `requestId`, `userId`, `path`, `method`, `status`, `latencyMs`.
- Reduce noisy SQL logs in non-dev.

### 1.3) Metrics (Actuator)
- Enable actuator endpoints (health, metrics) by environment.
- Track:
  - request latency (p95), error rate
  - DB pool saturation (Hikari)
  - cache hit rate (post-Redis)

Checkpoint:
- Request ID in logs and response.
- Metrics/health endpoints visible in local/dev.

---

## 2) Caching Strategy (Milestone P3-M2)

### 2.1) Integrate Redis
- Add Redis + Spring Cache.
- Define cache key naming: `namespace:version:params`.

### 2.2) Cache candidates (priority)
- Home aggregate: `GET /api/public/home` (TTL 30-120s)
- Product detail (id/slug): TTL 1-5 min
- Category tree/details: TTL 5-30 min
- Banners/vouchers public: TTL 1-5 min

### 2.3) Cache key/TTL/invalidation table
| Cache | Key | TTL | Invalidation |
| --- | --- | --- | --- |
| Home | `home:v1` | 60s | banner/product/category update |
| Product detail | `product:v1:{id}` | 5m | product/image/review update |
| Category tree | `category:v1:tree` | 10m | category update |
| Public banners | `banner:v1:active` | 2m | banner update |

### 2.4) Stampede protection
- Soft TTL or mutex for hot keys.

Checkpoint:
- Cache hit-rate metric available.
- p95 decreases on Home/Product detail after caching.

---

## 3) Database Optimization (Milestone P3-M3)

### 3.1) Identify slow queries
- Enable MySQL slow query log or Hibernate statistics in dev.
- Focus on:
  - Home aggregate
  - Product list/top-rank + recent metrics
  - Order history + order detail items

### 3.2) Index plan (deliverable)
Add/verify indexes for:
- `/api/public/products` filters/sorts (status, category, price, createdAt, slug)
- order history (user_id, updated_at, status)
- review lookup (product_id, updated_at)

### 3.3) Reduce chatty queries (N+1)
- Batch query by productIds for main image and metrics.
- Consider projection/DTO query for ranked lists.
- Consider denormalized counters if needed.

Checkpoint:
- 3-5 slow queries addressed with measurable improvement.
- No N+1 in list/top-rank endpoints (verified by logs/metrics).

---

## 4) Resilience Hardening (Milestone P3-M4)

### 4.1) Timeouts + retries
- Backend timeouts for outbound calls (if any).
- Retry with backoff for transient errors only.

### 4.2) Rate limiting (middleware)
- Add rate limit for hot public endpoints and auth endpoints.
- Separate limits for public vs authenticated.

### 4.3) Graceful degradation
- Serve cached home/product detail if DB is slow (stale-if-error).

Checkpoint:
- Under load, rate limiting returns 429 instead of timeouts.
- Error rate decreases and failure modes are predictable.

---

## 5) Async Processing (Milestone P3-M5)

Offload non-critical tasks:
- notifications/email (if any), cleanup jobs, exports.
- Start with in-process async + outbox if delivery must be guaranteed.

Checkpoint:
- Checkout does not block on non-critical tasks.

---

## 6) Frontend State & Data Fetching (Milestone P3-M6, Optional)

Only if needed:
- Add request-id header to API client for correlation.
- Defer React Query + Zustand unless required by Phase 3 goals.

Checkpoint:
- No extra frontend work unless requested.

---

## 7) Load Testing + Final Report (Milestone P3-M7)

### 7.1) Load test scenarios
- Read-heavy: home/products/product detail.
- Mixed: login + cart + checkout + orders.
- Admin: list/search (optional).

### 7.2) Phase 3 Review bundle
- Before/after report: p95, throughput, error rate.
- Cache hit-rate + TTL/invalidation summary.
- Top slow queries addressed + index list.
- Known limitations + Phase 4/5 backlog.

Checkpoint:
- Phase 3 review section added to `PROJECT_PLAN.md`.
