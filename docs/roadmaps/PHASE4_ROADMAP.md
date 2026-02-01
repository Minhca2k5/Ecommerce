# Phase 4 Roadmap: Advanced Features and Integrations

Status: In progress

Goal: add production-grade integrations (payments, broker, search, realtime, chatbot) and
event-driven data propagation for "new arrivals", with clear DoD and test evidence.

Guiding principles:
- Idempotency first for payment/order flows.
- Event-driven propagation for search/cache/realtime.
- Fallbacks when external systems are down.
- Verify with sandbox tests + metrics + failure scenarios.

---

## Phase 4 Definition of Done (DoD)
- Payment gateway integrated with signature verification + idempotency.
- Broker events published + consumers with retry/DLQ.
- Search index pipeline (backfill + incremental) with fallback.
- Realtime channels (WS/SSE) for order status + admin notifications.
- New product propagation works end-to-end (event -> index/cache -> UI).
- Chatbot is LLM-backed with RAG (project + DB context) and guardrails + logs.

---

## 0) Scope and Baseline (P4-M0)
### 0.1 Define environment + test data
- Sandbox keys for payment gateway.
- Test products + orders.
- Dedicated "webhook receiver" endpoint for local tests.

Checkpoint:
- Sandbox keys configured and safe to test.

---

## 1) Payment Gateway Integration (P4-M1)
### 1.1 Gateway integration
- Choose 1 gateway first (VNPay / MoMo / Stripe / PayPal).
- Implement create payment + redirect/checkout flow.

### 1.2 Webhook/IPN
- Signature verification + replay protection (timestamp/nonce).
- Idempotency keys for order/payment creation.
- Payment state machine (PENDING/PAID/FAILED/CANCELED/REFUNDED).

Checkpoint:
- Success/failure/cancel flows verified in sandbox.

---

## 2) Message Broker (P4-M2)
### 2.1 Broker setup
- RabbitMQ or Kafka docker-compose service.
- Define standard event schema (eventId, type, refId, userId, createdAt, payload).

### 2.2 Producer + consumer
- Publish events: OrderCreated, PaymentSucceeded, InventoryLow, ProductCreated/Updated.
- Consumers for notifications/email/analytics.
- Retry + DLQ (dead letter queue) policy.

Checkpoint:
- Events are published and consumed with retry + DLQ working.

---

## 3) Search Engine (P4-M3)
### 3.1 Elasticsearch integration
- Index mappings for products (name, desc, category, price, status).
- Search query: full-text + filters + sorting + faceting.

### 3.2 Indexing pipeline
- Backfill job for existing products.
- Incremental updates from broker events.
- Fallback to DB search if ES unavailable.

Checkpoint:
- Search accuracy + relevance verified.

---

## 4) Realtime Features (P4-M4)
### 4.1 WS/SSE for order status
- User receives order status updates in real time.
- Admin receives new order notifications.

Checkpoint:
- Realtime updates verified in UI.

---

## 5) New Product Propagation (P4-M5)
### 5.1 Event-driven propagation
- On product create/update: publish event -> update ES index -> invalidate cache.
- Optional WS/SSE push to storefront for "new arrivals".

Checkpoint:
- New product shows in UI without manual refresh.

---

## 6) Chatbot Integration (P4-M6)
### 6.1 LLM + RAG chatbot
- LLM-backed answers for general questions.
- RAG context from project files + DB (products/orders/payments).
- Auth-scoped data access for user.

### 6.2 Safety and logging
- Rate limit + input validation + prompt-injection defenses.
- Conversation logs + escalation to human support.

Checkpoint:
- Chatbot works in sandbox with safe fallbacks.

---

## 7) Phase 4 Review (P4-M7)
- Payment flows: success/fail/cancel + idempotency proof.
- Broker: events + retry/DLQ evidence.
- Search: precision/recall + fallback test.
- Realtime: WS/SSE verified.
- Chatbot: RAG demo + logs.
