# P5-M3.1: Redis Pub/Sub Audit Telemetry

## What was implemented
- A Redis Pub/Sub telemetry flow for audit events:
  - Publisher sends event payload when audit log is saved.
  - Subscriber listens and logs received telemetry events.
- Config properties:
  - `audit-log.telemetry.redis.enabled`
  - `audit-log.telemetry.redis.channel`

## Code locations
- Publisher: `backend/src/main/java/com/minzetsu/ecommerce/common/audit/AuditTelemetryPublisher.java`
- Subscriber: `backend/src/main/java/com/minzetsu/ecommerce/common/audit/AuditTelemetrySubscriber.java`
- Listener config: `backend/src/main/java/com/minzetsu/ecommerce/common/config/AuditTelemetryRedisConfig.java`

## Verify quickly
1. Ensure Redis is running.
2. Trigger any action that writes audit logs (for example, create/delete cart item).
3. Check backend logs for:
   - `Audit telemetry event received: ...`
4. Optional direct Redis check:
   - `redis-cli SUBSCRIBE audit-log-events`
   - trigger action and observe message.

## Rollback
- Disable without code rollback:
  - `audit-log.telemetry.redis.enabled=false`
