# Phase 6 Roadmap: Data Reliability & Analytics Serving

Status: Completed (M0-M7 implemented and verified)

Goal: deliver a reliable analytics pipeline from clickstream sink to MySQL mart, then serve admin analytics APIs with short-latency reads and operational visibility.

Guiding principles:
- Reliability first: deterministic rerun and quality gates before write.
- Keep MySQL as analytics serving source; Mongo remains raw sink.
- Favor simple batch ETL over premature streaming complexity.
- Treat observability and testability as first-class deliverables.

---

## Phase 6 Definition of Done (DoD)
- Event contract standardized for funnel events with schema versioning.
- `daily_product_metrics` mart table exists with indexes and retention policy.
- Daily ETL runs from Mongo to MySQL with idempotent rerun semantics.
- ETL quality controls enforce fail-fast for critical violations and warnings for non-critical signals.
- Admin analytics APIs (`funnel`, `top-products`) are available and cache-backed.
- ETL observability metrics and alert thresholds are in place.
- Unit/API contract tests validate aggregation, conversion, and analytics responses.

---

## 0) Scope Freeze & Contract Baseline (P6-M0)
### 0.1 Confirm analytics scope
- Funnel events: `VIEW_PRODUCT`, `ADD_TO_CART`, `PLACE_ORDER`, `PAYMENT_SUCCESS`.
- Analytics serving source: MySQL mart.

### 0.2 Contract baseline
- Required fields: `eventType`, `eventTime`, `requestId`, `source`, actor (`userId` or `guestId`), and `productId` for product-scoped events.
- Schema version field for compatibility evolution.

Checkpoint:
- Contract documented and enforced in clickstream sink path.

---

## 1) Event Contract Standardization (P6-M1)
- Standardized funnel taxonomy in clickstream writes.
- Added validation + dropped/saved/failed metrics in sink service.
- Added schema/version notes:
  - `docs/analytics/CLICKSTREAM_EVENT_CONTRACT.md`

Checkpoint:
- Mongo documents for funnel events contain required fields and consistent taxonomy.

---

## 2) Analytics Mart Schema (P6-M2)
- Added Liquibase migration:
  - `v16__analytics_mart.xml`
- Created `daily_product_metrics` with:
  - primary key (`metric_date`, `product_id`)
  - serving indexes (`product/date`, `date/conversion`, `date/orders`, `updated_at`)
  - retention policy doc:
    - `docs/analytics/ANALYTICS_MART_POLICY.md`

Checkpoint:
- Mart schema is migration-safe and query-optimized for analytics reads.

---

## 3) ETL Batch + Idempotent Rerun (P6-M3)
- Implemented daily scheduled ETL:
  - `AnalyticsEtlService`
- Source: Mongo clickstream by date window.
- Sink: MySQL `daily_product_metrics`.
- Idempotency strategy:
  - delete target date partition
  - recompute and save deterministic results

Checkpoint:
- Rerunning ETL for same date does not double-count data.

---

## 4) ETL Quality Controls (P6-M4)
- Added source and mart quality gates:
  - null/invalid keys
  - duplicate metric keys
  - missing date partition output
- Policy:
  - critical violations => fail-fast (abort write)
  - non-critical issues => warning logs

Checkpoint:
- Bad input cannot silently corrupt analytics mart.

---

## 5) Analytics Serving APIs + Cache (P6-M5)
- Added admin analytics endpoints:
  - `GET /api/admin/analytics/funnel?from=...&to=...`
  - `GET /api/admin/analytics/top-products?from=...&to=...&limit=...`
- Added short-TTL Redis cache for admin analytics reads:
  - cache name: `analyticsAdmin`
  - TTL: 30 seconds

Checkpoint:
- Admin analytics reads are stable and low-latency under repeated access.

---

## 6) Data Job Observability (P6-M6)
- Added ETL metrics:
  - duration
  - processed events
  - dropped events
  - rows upserted
  - total failures
  - consecutive failures
  - stale days
- Added alert thresholds (configurable):
  - repeated failure threshold
  - stale data days threshold

Checkpoint:
- ETL health can be monitored and alerted via metrics + warning logs.

---

## 7) Tests & Contracts (P6-M7)
- Unit tests:
  - conversion logic
  - ETL aggregation + idempotent rerun
- API contract tests:
  - analytics funnel/top-products response contract

Checkpoint:
- Core analytics pipeline behavior is protected by automated tests.

---

## Phase 6 Review Package
- Contract doc:
  - `docs/analytics/CLICKSTREAM_EVENT_CONTRACT.md`
- Mart policy:
  - `docs/analytics/ANALYTICS_MART_POLICY.md`
- ETL runbook:
  - `docs/analytics/ANALYTICS_ETL_RUNBOOK.md`
- End-to-end proof flow:
  - event emission -> Mongo sink -> ETL -> MySQL mart -> admin analytics API
- Reliability summary:
  - freshness, correctness, rerun safety
