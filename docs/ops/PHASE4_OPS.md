# Phase 4 Ops

## Objective
Operate advanced integrations/features introduced in Phase 4:
- payment gateway + webhook/IPN
- broker events + consumers
- search indexing pipeline
- realtime updates (SSE/WS)
- chatbot module
- guest cart/checkout and inventory reservation lifecycle
- HTTP caching with ETag/conditional GET

## Payments & Webhook Ops
- Always verify payment flows in sandbox:
  - success / fail / cancel
- Webhook/IPN controls:
  - signature validation
  - replay protection
  - idempotent payment/order updates
- Incident pattern:
  - webhook accepted but order state unchanged -> check idempotency and status transition rules.

## Broker/Event Ops
- Ensure producer emits required events:
  - order/payment/inventory/product events
- Consumer rules:
  - retry for transient errors
  - DLQ for exhausted retries
- Operational check:
  - message enters queue -> consumer processes -> downstream side effect visible.

## Search Ops
- Maintain two indexing modes:
  - backfill (full)
  - incremental (event-driven)
- If Elasticsearch degrades:
  - fallback query path to DB search must remain available.

## Realtime Ops
- Validate event channels for:
  - user order status updates
  - admin notifications
- If client reports stale status:
  - verify emitter/broker path, then fallback polling endpoint.

## Chatbot Ops
- Service health checks:
  - LLM endpoint reachability
  - context/RAG sources availability
- Guardrails:
  - rate limiting
  - prompt/input validation
  - conversation logging for incident triage
- If LLM unavailable:
  - return graceful fallback response, do not break page flow.

## Guest Cart / Reservation Ops
- Guest cart should persist across reload and merge safely after login.
- Inventory reservation controls:
  - TTL expiry releases stock
  - payment failure/cancel triggers release
- Cleanup jobs should be monitored for execution interval and release count.

## HTTP Caching Ops
- Public endpoints should return `Cache-Control` + `ETag`.
- Conditional request flow:
  - client sends `If-None-Match`
  - server returns `304` if unchanged
- After write operations, ensure ETag changes and clients receive fresh content.

## Minimal Incident Playbook
- If checkout/update appears stale:
  1. Check ETag/304 behavior.
  2. Verify write invalidation/event propagation.
  3. Verify search/realtime consumers and fallback paths.
  4. Temporarily reduce cache TTL or bypass cache for affected endpoint.
