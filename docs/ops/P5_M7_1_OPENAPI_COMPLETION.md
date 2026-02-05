# P5 M7.1 - OpenAPI Completion Notes

## Source of truth
- Swagger UI: `/docs`
- OpenAPI annotations: controllers + `SwaggerConfig`

## Standardized error model
Current error shape:
```json
{
  "message": "string",
  "timestamp": "2026-02-05T10:12:59.876+00:00",
  "status": 400
}
```

## Completion checklist
- Public/user/admin routes exposed in Swagger.
- Security scheme (`bearerAuth`) configured.
- New guest checkout endpoint included.
- New order pricing breakdown fields included in response models.

## Next maintenance rule
Any new endpoint must include:
- `@Operation(summary = "...")`
- request/response DTO docs (field naming kept stable)
- explicit error behavior in controller/service docs
