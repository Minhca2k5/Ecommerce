# Phase 5 Ops

Status: Completed (operations baseline M1-M8 + Phase 5 testing evidence finalized)

## Objective
Operate Phase 5 with production-readiness focus:
- reliability runbooks
- security gates
- migration safety
- API consumer clarity
- reliability policy and architecture decisions

## Runtime & Dependency Context
- Backend runtime: Spring Boot + MySQL + Redis + RabbitMQ + Elasticsearch (existing stack).
- CI runtime: GitHub Actions security workflow (`security-gates.yml`).
- Local operational tools used in this phase:
  - `mysqldump` / `mysql`
  - `gitleaks`
  - curl/bash (DAST smoke)

## Operational Streams

### 1) Database Reliability (M1)
- Backup/restore drill:
  - dump production-like data (`mysqldump`, use `--no-tablespaces` when grants are limited)
  - restore into rehearsal DB
  - compare key table row counts and record RTO
- Retention discipline:
  - retain backup artifacts with timestamp/version naming
  - track restore evidence in release notes
- Read-replica plan (prepared, optional rollout in Phase 6):
  - write on primary, read-heavy flows on replica
  - fallback to primary when replica is lagging/down

### 2) Audit Hardening (M2)
- Retention/cleanup:
  - scheduled cleanup via `audit-log.retention.*`
  - batch deletion to avoid heavy long transactions
- Sensitive data masking:
  - mask secrets/passwords/tokens/authorization/otp/PII before persistence
- Investigation surface:
  - admin audit query endpoint with filters + pagination
  - supports incident forensics and compliance review

### 3) NoSQL Expansion Ops (M3)
- Redis telemetry channel:
  - publish audit telemetry events to Redis pub/sub
  - subscriber path available for monitoring and async extensions
- Mongo analytics/log sink (enabled, minimal scope):
  - collections:
    - `clickstream_events` (VIEW/SEARCH)
    - `chatbot_transcripts` (message archive)
    - `audit_events` (audit telemetry archive)
  - best-effort write: Mongo downtime must not fail API requests
  - TTL retention recommended (e.g., 90 days) to cap growth
  - MySQL remains system of record for all business flows

### 4) Checkout Reliability Enhancements (M4)
- Guest checkout flow:
  - public checkout endpoint with guest/cart ownership constraints
  - secure guest order access token (HMAC-signed, TTL) to re-open guest order safely
  - public guest order/payment endpoints guarded by token
  - guest can trigger MoMo payment from guest order page without account login
  - idempotency integrated for duplicate-submit safety
- Abuse baseline:
  - Redis failure counters by scope
  - threshold-based `429` block window for repeated failed attempts
- Deterministic pricing:
  - subtotal -> discount -> shipping -> tax -> total
  - response + DB persist pricing breakdown fields

### 5) Security & Supply Chain Gates (M5)
- SAST + dependency scanning in CI:
  - CodeQL (SAST)
  - OWASP Dependency-Check (vulnerability scan)
- Secret scanning:
  - CI gitleaks gate
  - local `.githooks` pre-commit/pre-push blocking
- DAST smoke:
  - scripted checks for public/auth endpoints
  - verifies core security headers and basic abuse patterns

### 6) Migration Safety (M6)
- Rollback-ready release checklist:
  - backup before migration
  - changelog checkpoint + rollback conditions
  - defined rollback execution order
- Rehearsal flow:
  - run migration on production-like dataset
  - run smoke checks
  - record migration duration and recovery notes

### 7) API Consumer Operations (M7)
- OpenAPI contract:
  - `/docs` remains source-of-truth
  - standard error shape published
- Consumer guide:
  - auth, pagination/filter contracts
  - idempotency behavior
  - cache/conditional request behavior

### 8) Reliability Policy & ADR Governance (M8)
- Reliability policy:
  - SLI/SLO/SLA targets for auth, checkout, callback, order status
  - error budget + escalation path
- ADR governance:
  - monolith-first rationale captured
  - service split trigger criteria documented

## Common Failure Modes (Phase 5)
- Backup restore passes partially due to missing DB privileges.
- Security workflows flaky because target URL/secrets not configured in CI.
- Over-aggressive retention/masking breaks expected audit investigation fields.
- Checkout anti-abuse blocks legitimate retries when scope rules are too strict.
- Migration rehearsal skipped -> production rollback confidence drops.

## Minimal Incident Playbook
1. Stabilize writes first (if data integrity risk exists).
2. Validate latest migration/security gate changes as potential regression points.
3. Use audit query + request id to reconstruct timeline.
4. If data path is impacted, execute rollback checklist from M6.1.
5. Log evidence: impact window, trigger, remediation, and preventive action.

## Verification Evidence Summary
- Backend verification baseline: `mvn -q -DskipTests compile`.
- Frontend integration verification baseline: `npm run -s build`.
- DB reliability evidence: backup/restore parity checks + measured RTO.
- CI evidence: security gates workflow artifacts and job logs.

