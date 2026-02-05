# P5-M1.3: Read Replica and Read/Write Split Plan

Status: Prepared (design-level, rollout deferred)

## 1) Objective
- Define how the system will route read traffic to replica and keep write traffic on primary.
- Define fallback behavior when replica is unhealthy or lagging.
- Keep current monolith/service layer compatibility.

---

## 2) Current State
- Single datasource (`spring.datasource.*`) points to primary MySQL.
- All reads/writes currently hit the same DB endpoint.
- No runtime routing logic enabled yet.

---

## 3) Routing Rules (Target)

### 3.1 Write path -> Primary only
- All `POST/PUT/PATCH/DELETE` operations use primary DB.
- Critical read-after-write flows (checkout/payment/order status after write) also force primary.

### 3.2 Read path -> Replica by default (candidate)
- Public read-heavy endpoints are candidate for replica:
  - `/api/public/home`
  - `/api/public/products/**`
  - `/api/public/categories/**`
- Admin/report queries can be moved later based on consistency needs.

### 3.3 Consistency guardrails
- Any flow requiring strict freshness must read from primary.
- Replica lag threshold (initial policy): if lag > 5s, route reads back to primary.

---

## 4) Fallback Policy
- If replica health check fails, route all reads to primary immediately.
- If replica reconnects and lag is acceptable, gradually restore read traffic.
- Degradation mode must preserve correctness first, performance second.

Operational signals:
- Replica connectivity
- Replica lag
- Error rate on read queries

---

## 5) Rollout Strategy
- Stage 0: design + property scaffolding only (this milestone).
- Stage 1: enable replica routing for one low-risk endpoint group.
- Stage 2: expand to more read endpoints with monitoring.
- Stage 3: evaluate cost/perf impact and keep/rollback decision.

Rollback:
- Feature flag off => all traffic returns to primary.

---

## 6) Configuration Contract (for implementation phase)

Proposed properties:
- `db.readwrite.enabled` (feature toggle)
- `db.primary.url|username|password`
- `db.replica.url|username|password`
- `db.replica.max-lag-seconds`
- `db.replica.fallback-to-primary`

---

## 7) Verification Checklist (when implementation starts)
- Read queries route to replica when toggle is ON.
- Write queries always route to primary.
- Replica down test routes reads to primary without user-visible failure.
- Read-after-write sensitive flows still return fresh data.

---

## 8) Deliverable for Phase 5 Review
- This design doc approved.
- Mapping table of endpoints eligible for replica reads.
- Fallback policy and lag threshold documented.
