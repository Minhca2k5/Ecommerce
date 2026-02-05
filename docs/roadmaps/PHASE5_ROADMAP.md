# Phase 5 Roadmap: Advanced Data, Quality, and Reliability

Status: Planned

Goal: complete the remaining core capabilities for data safety, security quality, and operational reliability, then execute testing as the final milestone before Phase 6.

Guiding principles:
- Finish feature/compliance capabilities first, test as final gate.
- Keep backward compatibility for current APIs and data flows.
- Every milestone must have rollout/rollback notes.
- Prefer pragmatic scope over adding new technologies.

---

## Phase 5 Definition of Done (DoD)
- Database reliability controls are documented and runnable (isolation/deadlock handling, backup/restore drill, replication/read split plan).
- Audit logging is production-safe (retention, masking, searchable endpoints).
- NoSQL expansion scope is implemented (Redis streams/pubsub; optional Mongo module scaffold if selected).
- Checkout enhancements are completed (guest checkout, anti-abuse baseline, multi-currency + tax/shipping rules).
- Security/supply chain checks are integrated in CI (SAST, dependency scan, secret scan, DAST smoke).
- Migration safety process is in place (rollback strategy + rehearsal).
- API docs are finalized (OpenAPI + error model + examples).
- Testing suite is executed as the final milestone and reviewed.

---

## 0) Scope Freeze and Risk Baseline (P5-M0)
### 0.1 Confirm Phase 5 boundaries
- Lock feature scope from `PROJECT_PLAN.md`.
- Mark optional items clearly (Mongo, read replica/read-write split).

### 0.2 Risk list
- Create short risk table: data loss, auth bypass, payment inconsistency, migration failure.

Checkpoint:
- Phase 5 scope and risks approved.

---

## 1) Advanced Database Reliability (P5-M1)
### 1.1 Transaction behavior and contention
- Validate isolation choices on checkout/order/payment paths.
- Add deadlock handling/retry policy for known write-conflict paths.

### 1.2 Data durability operations
- Backup/restore drill with timing (RTO/RPO notes).
- Data retention policy for transactional and audit data.
- Deliverable doc: `docs/ops/P5_M1_2_DB_BACKUP_RESTORE_RETENTION.md`.

### 1.3 Replication/read split plan (optional but prepared)
- Define read replica routing rules and fallback behavior.
- Deliverable doc: `docs/ops/P5_M1_3_READ_REPLICA_PLAN.md`.

Checkpoint:
- Reliability runbook for DB operations exists and is executable.

---

## 2) Audit Log Hardening (P5-M2)
### 2.1 Retention and cleanup
- Add TTL/archive policy and scheduled cleanup job.
- Deliverable doc: `docs/ops/P5_M2_1_AUDIT_LOG_RETENTION.md`.

### 2.2 Sensitive data protection
- Mask PII/token/password fields in logs and audit payloads.
- Deliverable doc: `docs/ops/P5_M2_2_AUDIT_MASKING.md`.

### 2.3 Audit query surface
- Implement/finish admin audit search endpoint + filters.
- Deliverable doc: `docs/ops/P5_M2_3_AUDIT_QUERY_SURFACE.md`.

Checkpoint:
- Audit logs are searchable, retained by policy, and free of sensitive plaintext.

---

## 3) NoSQL Expansion (P5-M3)
### 3.1 Redis advanced usage
- Add streams/pubsub use case (event telemetry or async audit pipeline).
- Deliverable doc: `docs/ops/P5_M3_1_REDIS_PUBSUB_AUDIT_TELEMETRY.md`.

### 3.2 Optional document DB track
- If enabled: scaffold Mongo usage for analytics/log documents.
- If skipped: document rationale and future trigger conditions.
- Deliverable doc: `docs/ops/P5_M3_2_MONGO_OPTIONAL_TRACK.md`.

Checkpoint:
- At least one production-meaningful Redis advanced flow is active.

---

## 4) Checkout Enhancements (P5-M4)
### 4.1 Guest checkout
- Support checkout without account while preserving fraud/rate controls.
- Deliverable doc: `docs/ops/P5_M4_1_GUEST_CHECKOUT.md`.

### 4.2 Fraud/abuse baseline
- Add rules for suspicious retry bursts / repeated failed attempts.
- Deliverable doc: `docs/ops/P5_M4_2_CHECKOUT_ABUSE_BASELINE.md`.

### 4.3 Multi-currency + tax/shipping rules
- Add deterministic calculation pipeline and response breakdown.

Checkpoint:
- Guest and authenticated checkout flows are functionally complete.

---

## 5) Security & Supply Chain Quality (P5-M5)
### 5.1 Static and dependency scanning
- Add SAST and dependency vulnerability scan in CI.

### 5.2 Secret scanning
- Enable repository secret scanning in CI and pre-commit/pre-push hooks.

### 5.3 DAST smoke
- Add lightweight dynamic checks for public/auth endpoints.

Checkpoint:
- Security gates produce reports and block high-severity issues.

---

## 6) Migration Safety (P5-M6)
### 6.1 Rollback-ready changes
- Add rollback note/checklist per Liquibase release.

### 6.2 Rehearsal on production-like data
- Run migration rehearsal with timing + failure recovery notes.

Checkpoint:
- Migration process has tested rollback and rehearsal evidence.

---

## 7) API Documentation Finalization (P5-M7)
### 7.1 OpenAPI completion
- Finalize endpoint coverage, examples, and standardized error model.

### 7.2 Consumer clarity
- Add auth notes, pagination/filter contracts, idempotency behavior.

Checkpoint:
- API documentation is accurate enough for independent frontend consumption.

---

## 8) Reliability Policy & ADR (P5-M8)
### 8.1 SLI/SLO/SLA policy
- Define SLI/SLO/SLA for auth, checkout, payment callback, and order status.
- Add error-budget and escalation policy.

### 8.2 ADR set
- Capture monolith-first decision and split criteria for future services.

Checkpoint:
- Reliability policy and ADR docs are versioned and referenced in plan.

---

## 9) Testing Suite (Final Milestone P5-M9)
> This milestone is intentionally last per current delivery strategy.

### 9.1 Unit tests
- Pricing/discount, reservation TTL, order totals, RBAC rules.

### 9.2 Integration tests
- `MockMvc` + TestContainers (DB/Redis) for core flows.

### 9.3 E2E and contract tests
- E2E (register/login, browse/search, cart, checkout, admin flows).
- Consumer/provider contract tests for FE-BE compatibility.

### 9.4 Security tests
- Auth bypass, rate-limit abuse, OTP brute-force, token misuse.

Checkpoint:
- Phase 5 review package includes test report + coverage + known gaps.

---

## 10) Phase 5 Review Package
- Data reliability report (backup/restore + replication/read-split notes).
- Security scan report (SAST/dependency/secrets/DAST smoke).
- Migration rehearsal + rollback evidence.
- API docs link and ADR/SLO policy links.
- Final test report (unit/integration/e2e/contract/security).
