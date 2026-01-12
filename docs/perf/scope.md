# Phase 3 Benchmark Scope and SLOs (M0)

## Endpoints / Flows
Public:
- Home: GET /api/public/home
- Product list: GET /api/public/products
- Product detail: GET /api/public/products/slug/{slug}

User:
- Cart: GET/POST/PUT/DELETE /api/users/me/carts/**
- Checkout: POST /api/users/me/orders
- Orders: GET /api/users/me/orders, GET /api/users/me/orders/{id}

## Test data assumptions
- At least 200 products with mixed categories/prices
- At least 1 active user with existing cart + order history
- Product slugs are stable and valid

## Suggested SLOs (local/dev)
- Read-heavy p95 latency: 300-500ms
- Write-heavy p95 latency (cart/checkout): 500-800ms
- Error rate: < 1%

## Load profile (baseline)
- Public read: 20 VUs, 2-3 minutes, 30s ramp
- Mixed user: 10 VUs, 2-3 minutes, 30s ramp
- Max VUs should be tuned based on local capacity

## Output format
- k6 JSON output for p95/throughput/error rate
- Optional CSV for plotting
