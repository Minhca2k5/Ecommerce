# Analytics Mart Policy (Phase 6 - Item 2)

## Table scope

- Table: `daily_product_metrics`
- Grain: one row per (`metric_date`, `product_id`)
- Purpose: serve fast admin analytics reads from MySQL, not from raw Mongo events.

## Indexing strategy

- Primary key: (`metric_date`, `product_id`)
  - Guarantees uniqueness for deterministic ETL upsert.
- Secondary indexes:
  - `idx_dpm_product_date` (`product_id`, `metric_date`): product trend lookups.
  - `idx_dpm_date_conversion` (`metric_date`, `conversion_rate`): top conversion ranking by date.
  - `idx_dpm_date_orders` (`metric_date`, `orders`): top ordered products by date.
  - `idx_dpm_updated_at` (`updated_at`): freshness/staleness checks.

## Retention policy

- Keep `daily_product_metrics` for 730 days (24 months rolling window).
- Purge policy:
  - Delete records where `metric_date &lt; CURRENT_DATE - INTERVAL 730 DAY`.
  - Run monthly during low-traffic window.
- Safety:
  - Purge only after backup success signal of MySQL.
  - Keep retention SQL in ETL job maintenance stage (Phase 6 ETL implementation).
