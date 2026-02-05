# P5 M7.2 - API Consumer Guide

## Auth contract
- Bearer token via `Authorization: Bearer <jwt>`
- Idempotent create endpoints support `Idempotency-Key`

## Pagination/filter contract
- Admin/user listing endpoints use `page`, `size`, `sort`.
- Filter fields are optional; unspecified filters must not alter baseline result set.

## Idempotency behavior
- Same key + same scope returns same resource response.
- Checkout/payment create endpoints should always send stable key from FE.

## Caching behavior (public endpoints)
- Public home endpoint may return `ETag`.
- Client should send `If-None-Match` and handle `304 Not Modified`.

## Error handling expectation
- FE should parse `message` + `status` from standard error payload.
- For `429`, FE should show retry-later UX.
