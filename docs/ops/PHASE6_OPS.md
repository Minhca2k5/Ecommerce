# Phase 6 Ops

Status: Completed (hybrid realtime serving + ETL baseline + telemetry)

## Objective
Operate Phase 6 analytics pipeline with reliability guarantees:
- standard event contract
- realtime counters in Redis for current-day funnel metrics
- daily ETL from Mongo sink to MySQL mart for durable history/reconciliation
- quality-gated writes
- admin analytics serving endpoints with cache
- ETL observability and threshold-based alerts

## Runtime Scope
- Raw sink: MongoDB `clickstream_events`
- Serving mart: MySQL `daily_product_metrics`
- Realtime overlay: Redis (`analytics:realtime:*`)
- API surface: `/api/admin/analytics/*`
- Cache: Redis (`analyticsAdmin`)

## Event Contract Ops
- Funnel event taxonomy must remain:
  - `VIEW_PRODUCT`
  - `ADD_TO_CART`
  - `PLACE_ORDER`
  - `PAYMENT_SUCCESS`
- Required fields validation is enforced at sink write path.
- Contract source:
  - `docs/analytics/CLICKSTREAM_EVENT_CONTRACT.md`

Operational checks:
- Confirm new events contain `eventTime`, `requestId`, `source`, `schemaVersion`.
- Investigate rising `clickstream.events.dropped` by `reason`.

## ETL Job Ops
- Job: `AnalyticsEtlService#runDailyEtl`
- Schedule property: `analytics.etl.daily-cron`
- Target partition: previous UTC day.

Idempotent rerun behavior:
- delete `metric_date = targetDate`
- recompute from Mongo source
- save deterministic rows

Operational checks:
- rerun same date does not produce duplicate keys.
- job logs include `eventsRead`, `rowsUpserted`, `durationMs`.

## Data Quality Ops
- Critical violations (fail-fast, no write):
  - missing `eventType` / `eventTime`
  - missing product key for product-scoped events
  - out-of-range event time
  - null/duplicate mart keys
  - missing partition output when source has tracked events
- Non-critical warnings:
  - missing actor
  - unknown event type

Operational checks:
- track warning volume trend; investigate sustained growth.
- resolve critical violations before rerun.

## Serving API Ops
- Endpoints:
  - `GET /api/admin/analytics/funnel`
  - `GET /api/admin/analytics/top-products`
- Auth: admin role only.
- Read strategy:
  - history (`from`..`to` before current UTC day) from MySQL mart
  - current UTC day overlay from Redis realtime counters
- Cache:
  - cache name `analyticsAdmin`
  - TTL 30s

Operational checks:
- validate response freshness against latest ETL partition.
- if stale analytics perceived, compare cache TTL and ETL freshness.

## Observability & Alerts
- ETL metrics:
  - `analytics.etl.duration`
  - `analytics.etl.events.processed.total`
  - `analytics.etl.events.dropped.total`
  - `analytics.etl.rows.upserted.total`
  - `analytics.etl.failures.total`
  - `analytics.etl.consecutive_failures`
  - `analytics.etl.stale_days`
- Alert thresholds:
  - `analytics.etl.alert.failure-threshold`
  - `analytics.etl.alert.stale-days-threshold`

Operational response:
1. Failure spike:
   - check latest critical quality violation in ETL logs.
   - rerun ETL for affected date after source fix.
2. Stale window breach:
   - verify scheduler execution and DB write success.
   - confirm latest mart date and investigate blocked runs.

## Retention Ops
- Keep `daily_product_metrics` for 730 days (rolling 24 months).
- Purge strategy documented in:
  - `docs/analytics/ANALYTICS_MART_POLICY.md`

Operational checks:
- execute purge in low-traffic window after backup confirmation.
- track row count trend and storage growth.

## Validation Checklist (Post-Deploy)
- Liquibase applied (`v16__analytics_mart.xml`).
- ETL schedule active.
- One successful ETL run observed with non-zero metrics.
- Admin analytics endpoints return expected data for known range.
- Metrics visible under actuator `/metrics`.
