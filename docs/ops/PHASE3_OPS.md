# Phase 3 Ops

## Objective
Operate Phase 3 performance/reliability upgrades with measurable before/after impact:
- caching
- DB query/index tuning
- resilience controls
- observability foundation

## Measurement First (Baseline + After)
- Keep perf assets under `docs/perf/`:
  - load scripts
  - baseline results
  - after-change results
- Required tracked metrics:
  - p50/p95 latency
  - throughput
  - error rate
  - cache hit behavior

## Observability Runbook
- Ensure request correlation is always present:
  - request header/response header `X-Request-Id`
  - same ID in logs
- Structured logs should include:
  - `requestId`, `userId`, `path`, `method`, `status`, `latencyMs`
- Actuator endpoints available for local/dev diagnostics:
  - health
  - metrics
  - circuit breaker status

## Caching Operations
- Cache targets: home, product detail, categories, public banners/vouchers.
- Operate with explicit key + TTL + invalidation policy.
- For stale-risk endpoints, use stale-if-error fallback where configured.
- Verify cache behavior after writes:
  - update product/category/banner/review
  - ensure affected cached read is refreshed/invalidated

## DB Tuning Operations
- Watch slow query logs on hot endpoints:
  - home aggregate
  - product list/ranked
  - order history/detail
- Confirm index usage after schema/index changes.
- Re-check N+1 behavior on list endpoints after each query refactor.

## Resilience Controls
- Rate limit policy:
  - public/auth/user/admin paths separated
- Retry/backoff:
  - only for transient failures
- Circuit breaker:
  - protect outbound dependencies

## Minimal Incident Playbook
- If latency spikes:
  1. Check DB pool saturation + slow queries.
  2. Check cache hit/miss behavior and invalidation churn.
  3. Check rate-limit/circuit-breaker states.
  4. Roll back latest tuning change if p95 regresses.
