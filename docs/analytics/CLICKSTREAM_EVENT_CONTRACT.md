# Clickstream Event Contract (Phase 6)

Scope: analytics funnel events stored in Mongo `clickstream_events`.

## Event taxonomy

- `VIEW_PRODUCT`
- `ADD_TO_CART`
- `PLACE_ORDER`
- `PAYMENT_SUCCESS`

## Required fields

- `eventType`
- `eventTime`
- `requestId`
- `source`
- `userId` or `guestId` (at least one)
- `productId` for product-scoped events (`VIEW_PRODUCT`, `ADD_TO_CART`)

## Schema/version notes

- Current schema version: `v1`.
- Stored in `schemaVersion` field for every event.
- Backward compatibility rule:
  - Additive changes only for `v1` (new optional fields are allowed).
  - Required field changes must bump schema version (for example `v2`) and keep ETL logic backward compatible during migration.
- Legacy events (for example `SEARCH`) are non-funnel and outside Phase 6 funnel aggregation.
