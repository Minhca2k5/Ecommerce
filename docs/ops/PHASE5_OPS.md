# Phase 5 Ops

This file consolidates all previous `docs/ops/P5_*` runbooks into one place.

## M1 - Advanced Database Reliability

### M1.2 Backup / Restore / Retention
- Backup by `mysqldump` (with `--no-tablespaces` when DB grants are limited).
- Restore drill to a secondary database and compare row counts.
- Record observed RTO/RPO and backup artifact path.

### M1.3 Read Replica Plan (optional track)
- Primary handles writes, replica handles read-heavy queries.
- Define fallback to primary if replica is unavailable/lagging.
- Rollout target remains Phase 6 deployment topology.

## M2 - Audit Log Hardening

### M2.1 Retention & Cleanup
- Scheduled cleanup with retention window and batch deletion.
- Configurable via `audit-log.retention.*` properties.

### M2.2 Sensitive Data Masking
- Mask passwords/tokens/secrets/api-key/authorization/otp/email/phone in audit payloads.
- Keep audit searchable while removing plaintext sensitive data exposure.

### M2.3 Audit Query Surface
- Admin audit search endpoint with filters and pagination.
- Supports incident investigation and compliance review workflows.

## M3 - NoSQL Expansion

### M3.1 Redis Pub/Sub Audit Telemetry
- Publish audit events to Redis channel.
- Subscriber consumes events for observability/async integrations.

### M3.2 Optional Mongo Track
- Mongo path documented as optional, with defer rationale and trigger conditions.

## M4 - Checkout Enhancements

### M4.1 Guest Checkout
- Public guest checkout endpoint integrated with existing order flow.
- Guest identity/cart constraints enforced before order creation.

### M4.2 Fraud/Abuse Baseline
- Redis-backed failed checkout counter per scope.
- Block repeated failures with HTTP 429 windowed threshold policy.

### M4.3 Multi-Currency + Tax/Shipping
- Deterministic pricing pipeline:
  - subtotal -> discount -> shipping -> tax -> total
- Persist response breakdown fields (`subtotal`, `discount`, `shipping`, `tax`, `total`).

## M5 - Security & Supply Chain Gates

### M5.1 SAST + Dependency Scan CI
- GitHub Actions jobs for CodeQL (SAST) and OWASP Dependency-Check.

### M5.2 Secret Scanning
- Gitleaks in CI.
- Local pre-commit/pre-push hooks for developer-side secret blocking.

### M5.3 DAST Smoke
- Scripted smoke checks for public/auth endpoints and key security headers.
- Optional CI run when `DAST_TARGET_URL` is configured.

## M6 - Migration Safety

### M6.1 Rollback Checklist
- Pre-release backup + changelog checkpoint.
- Rollback trigger conditions and execution steps.

### M6.2 Migration Rehearsal
- Rehearsal on production-like dataset.
- Capture migration timing, smoke checks, and recovery notes.

## M7 - API Documentation Finalization

### M7.1 OpenAPI Completion
- `/docs` remains source-of-truth for endpoint contracts.
- Standardized error model documented for clients.

### M7.2 API Consumer Guide
- Auth contract, pagination/filter behavior, idempotency expectations, caching notes.

## M8 - Reliability Policy & ADR

### M8.1 SLI/SLO/SLA Policy
- Defined SLI/SLO targets for auth, checkout, payment callback, order status.
- Error budget + escalation path documented.

### M8.2 ADR Set
- Monolith-first decision and service-split trigger criteria captured.

## Verification Summary
- Backend compile check used during implementation: `mvn -q -DskipTests compile`.
- Frontend build check used during integration: `npm run -s build`.
- DB backup/restore drill validated with row-count parity and measured RTO.
