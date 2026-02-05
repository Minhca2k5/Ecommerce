# P5-M2.3: Admin Audit Log Query Surface

## Implemented endpoint
- `GET /api/admin/audit-logs`

## Supported filters
- `userId`
- `action` (contains, case-insensitive)
- `entityType` (contains, case-insensitive)
- `entityId`
- `success`
- `from` (ISO datetime, against `createdAt`)
- `to` (ISO datetime, against `createdAt`)

## Pagination/sort
- Standard Spring pageable (`page`, `size`, `sort`).
- Default sort fallback: `createdAt DESC`.

## Verify quickly
1. Call:
   - `/api/admin/audit-logs?page=0&size=20`
2. Call with filters:
   - `/api/admin/audit-logs?entityType=ORDER&success=true&page=0&size=10`
3. Expected:
   - 200 response with paged results.
   - Filtered records match criteria.
