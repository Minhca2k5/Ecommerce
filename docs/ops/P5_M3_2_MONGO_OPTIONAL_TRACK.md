# P5-M3.2: Optional MongoDB Track (Document DB)

Status: Completed as optional track decision (deferred implementation)

## Decision
- MongoDB runtime integration is deferred for now.
- Current system already has:
  - MySQL for transactional consistency
  - Redis for cache + pub/sub telemetry
- Adding Mongo now would increase ops complexity without immediate product impact.

## Rationale
- No high-volume analytics workload currently requires document DB.
- Existing audit + telemetry flows are sufficient for current scale.
- Team/project stage prioritizes reliability and delivery speed.

## Future Trigger Conditions (when to enable Mongo)
- Audit/event volume grows beyond relational cost/latency thresholds.
- Need flexible schema analytics documents with frequent shape changes.
- Need longer retention for analytical event payloads with cheap storage.

## Planned Scope When Enabled
- Collection: `analytics_events`
- Fields:
  - `eventType`
  - `entityType`
  - `entityId`
  - `userId`
  - `payload` (JSON object)
  - `createdAt`
- Index candidates:
  - `{ eventType: 1, createdAt: -1 }`
  - `{ entityType: 1, entityId: 1, createdAt: -1 }`
  - TTL index on `createdAt` for selected event classes

## Rollout Notes
- Keep feature-flagged (`analytics.mongo.enabled`) and off by default.
- Start with dual-write from telemetry pipeline in staging only.
- Validate cost + query latency before production enablement.
