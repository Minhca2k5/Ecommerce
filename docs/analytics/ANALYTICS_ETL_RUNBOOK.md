# Analytics ETL Runbook (Phase 6 - Item 3)

## Job behavior

- Schedule: `analytics.etl.daily-cron` (default `01:15` server time, daily).
- Target date: previous UTC day (`today - 1`).
- Source: Mongo `clickstream_events`.
- Sink: MySQL `daily_product_metrics`.

## Idempotent rerun strategy

- The job always:
  - deletes existing rows for `targetDate`
  - recomputes metrics from Mongo for the same date window
  - inserts fresh rows
- Result: rerun for the same date does not double-count.

## Aggregation logic

- Group key: (`metric_date`, `product_id`)
- `views`: count `VIEW_PRODUCT`
- `add_to_cart`: count `ADD_TO_CART`
- `orders`: count `PLACE_ORDER`
- `unique_users`: distinct actor key (`userId` or `guestId`) per product/day
- `conversion_rate`: `orders / views` (scale 4, 0 when views=0)

## Verification queries

```sql
SELECT metric_date, product_id, views, add_to_cart, orders, unique_users, conversion_rate
FROM daily_product_metrics
ORDER BY metric_date DESC, product_id ASC
LIMIT 50;
```

```sql
SELECT metric_date, product_id, COUNT(*) c
FROM daily_product_metrics
GROUP BY metric_date, product_id
HAVING c > 1;
```
