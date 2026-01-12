# Phase 3 Performance Report (Before/After)

## Environment
- Date:
- Commit / tag:
- DB size snapshot:
- Notes:

## Scope
- Home: GET /api/public/home
- Product list: GET /api/public/products
- Product detail: GET /api/public/products/slug/{slug}
- Cart: GET/POST/PUT/DELETE /api/users/me/carts/**
- Checkout: POST /api/users/me/orders
- Orders: GET /api/users/me/orders, GET /api/users/me/orders/{id}

## Load profile
- Public read: __ VUs, __ minutes, ramp __
- Mixed user: __ VUs, __ minutes, ramp __

## Results Summary
| Flow | p50 | p95 | Error Rate | Throughput (req/s) | Notes |
| --- | --- | --- | --- | --- | --- |
| Home |  |  |  |  |  |
| Product list |  |  |  |  |  |
| Product detail |  |  |  |  |  |
| Cart |  |  |  |  |  |
| Checkout |  |  |  |  |  |
| Orders |  |  |  |  |  |

## Observations
- 

## Actions Taken
- 

## Regression Risk / Rollback
- 

## Artifacts
- Baseline files: docs/perf/baseline_results/
- k6 scripts: docs/perf/k6/
