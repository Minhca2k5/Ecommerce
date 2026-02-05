# P5-M2.2: Audit Log Sensitive Field Masking

## What was implemented
- Audit error messages are sanitized before persistence in `audit_logs.error_message`.
- Masking is applied in `AuditLogAspect.trimError(...)`:
  - token/password/secret/api-key/authorization/otp key-value patterns
  - bearer tokens
  - email addresses
  - phone-like numeric strings

## Why
- Prevent accidental persistence of sensitive data and PII in audit logs.

## Verify quickly
1. Trigger a controlled error that includes sensitive text in message, for example:
   - `token=abc123`
   - `password=hello123`
   - `email=test@example.com`
2. Query latest failed audit logs:
   - `SELECT id, error_message FROM audit_logs WHERE success = 0 ORDER BY id DESC LIMIT 5;`
3. Expected:
   - Sensitive fragments appear masked (`***`) instead of raw values.

## Rollback
- Revert `AuditLogAspect` masking logic if needed (not recommended for production).
