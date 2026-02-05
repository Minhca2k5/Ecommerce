# P5-M2.1: Audit Log Retention and Scheduled Cleanup

## What was implemented
- Configurable retention policy for audit logs:
  - `audit-log.retention.enabled`
  - `audit-log.retention.retention-days`
  - `audit-log.retention.cleanup-interval-ms`
  - `audit-log.retention.batch-size`
- Scheduled cleanup service:
  - `AuditLogRetentionCleanupService.cleanupExpiredAuditLogs()`
  - Deletes old `audit_logs` rows in batches.

## Verify quickly
1. Set aggressive values in local `application.properties`:
   - `audit-log.retention.retention-days=0`
   - `audit-log.retention.cleanup-interval-ms=60000`
2. Restart backend.
3. Wait one cleanup interval.
4. Check DB count:
   - `SELECT COUNT(*) FROM audit_logs;`
5. Check backend log for cleanup message:
   - `Audit log retention cleanup deleted ...`

## Rollback
- Disable cleanup without code rollback:
  - `audit-log.retention.enabled=false`
