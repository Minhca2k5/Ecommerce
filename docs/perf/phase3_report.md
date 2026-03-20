# Phase 3 Performance Report (M7)

Status: Completed summary (baseline and optimization results captured)

## Scope
- Home: `GET /api/public/home`
- Product list: `GET /api/public/products`
- Product detail: `GET /api/public/products/slug/{slug}`
- Cart: `GET/POST/PUT/DELETE /api/users/me/carts/**`
- Checkout: `POST /api/users/me/orders`
- Orders: `GET /api/users/me/orders`, `GET /api/users/me/orders/{id}`

## Environment
- Date: 2026-01-22
- Environment: local
- DB size: (fill)
- Cache: Redis enabled (Y)
- Build/version: (fill)

## Baseline (M0)
Public read test:
- Script: `docs/perf/k6/public-read.js`
- p50: 765.77 ms
- p95: 4452.43 ms
- error rate: 0%
- throughput (rps): (fill)

User mixed test:
- Script: `docs/perf/k6/user-mixed.js`
- p50: 29.29 ms
- p95: 59.14 ms
- error rate: 18.77%
- throughput (rps): (fill)

## After Optimizations (M3/M4/M5/M6)
Public read test:
- Script: `docs/perf/k6/public-read.js`
- p50: 6.59 ms
- p95: 24.79 ms
- error rate: 0%
- throughput (rps): (fill)

User mixed test:
- Script: `docs/perf/k6/user-mixed.js`
- p50: 30.42 ms
- p95: 58.76 ms
- error rate: 5.17%
- throughput (rps): (fill)

## Cache Summary
- Caches enabled: home/productDetail/category/banner/voucher
- TTLs: home 60s, product detail 5m, category tree 10m, banners 2m, vouchers (per endpoint)
- Cache hit rate (if available): (fill)

## Slow Query Summary
- Top 3-5 queries:
- Indexes added: `backend/src/main/resources/db/changelog/v3__indexes.xml`
- N+1 fixes: ranked product lists, reviews/user cleanup

## Resilience Summary
- Rate limiting enabled (Y)
- Stale-if-error fallback: home/product detail (Y)
- Outbound timeouts/retry configured (Y)
- Circuit breaker: outbound webhook (Y)

## Async Summary
- Events offloaded: order-created notification + webhook (plus domain webhooks)

## Notes / Limitations
- Throughput values were not captured in this draft and remain to be backfilled if a final benchmark summary is needed.
