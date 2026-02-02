# Phase 4 Roadmap: Advanced Features and Integrations

Status: Completed (updated)

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
- Chatbot is LLM-backed with RAG (project + DB context), conversation/workspace management, practical multimodal I/O, and guardrails + logs.
- Anonymous cart works for guests and merges safely on login.
- Inventory reservations expire and release stock automatically.

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
- Auto-title conversation from first user question (avoid generic names like "General Chat 1").
- Conversation CRUD: rename conversation, delete history, copy message content.

### 6.2 Workspace model (groups + projects)
- Add chat groups and project workspaces.
- Project CRUD (create/update/delete) with isolated conversation lists per project.
- Manage project history (delete/edit names and chat metadata).
- Group collaboration UX:
  - Invite by email to existing accounts.
  - Member actions: Accept/Refuse invite.
  - Owner actions: remove members, delete group.
  - Member list visible in group UI (includes owner) + live member count.
  - Group message history shows sender display name (not only role bubble).
  - Invite badge count stays in sync (pending received + pending sent).
  - Notifications use display names (not raw user IDs).

### 6.3 Multimodal capabilities
- Read uploaded files (txt/pdf/docx baseline) and answer with grounded context.
- Image understanding removed in current local mode (latency/quality tradeoff); keep as optional future extension.
- Voice pipeline: record/upload audio, speech-to-text, text-to-speech playback.
- Voice translation flow VN <-> EN (STT -> translate -> TTS).

### 6.6 Auth hardening related to chatbot collaboration
- Registration flow protected by email OTP verification before account creation.
- OTP expiry window set to 1 minute with countdown in frontend.
- User-facing API errors normalized (avoid generic internal error messages for expected OTP/invite failures).

### 6.4 Safety and logging
- Rate limit + input validation + prompt-injection defenses.
- Conversation logs + escalation to human support.

### 6.5 Placement in current codebase
- Backend chatbot module: `backend/src/main/java/com/minzetsu/ecommerce/chatbot/`.
- Frontend chatbot UI: `frontend/src/app/ChatbotWidget.tsx`.
- Frontend chatbot API client: `frontend/src/lib/chatbotApi.ts`.

Checkpoint:
- Chatbot works in sandbox with safe fallbacks and supports workspace/conversation management.

---

## 7) Phase 4 Review (P4-M7)
- Payment flows: success/fail/cancel + idempotency proof.
- Broker: events + retry/DLQ evidence.
- Search: precision/recall + fallback test.
- Realtime: WS/SSE verified.
- Chatbot: RAG demo + logs + workspace/conversation CRUD + multimodal demo.

---

## 8) Anonymous Cart + Merge on Login (P4-M8)
### 8.1 Guest cart
- Persist guest cart by device/session (cookie or local storage with server sync).
- Support add/remove/update for guest users.

### 8.2 Merge rules
- On login, merge guest cart into user cart.
- Resolve conflicts (same SKU -> sum quantity, cap by inventory).

Checkpoint:
- Guest cart survives reload and merges correctly on login.

---

## 9) Inventory Reservation TTL + Release (P4-M9)
### 9.1 Reservation
- Reserve stock at checkout with TTL.
- Record reservation metadata (orderId, sku, qty, expiresAt).

### 9.2 Release flow
- Auto-release on TTL expiry or payment failure.
- Release on manual cancel.

Checkpoint:
- Stock is restored after TTL or failed payment.
