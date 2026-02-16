# Phase 7 Roadmap: DevOps, Observability & Scale

Status: In Progress (M0 completed, M1 in progress)

Goal: move the system from feature-complete to production-ready by establishing a repeatable deployment pipeline, operational visibility, reliability runbooks, and cost-aware scaling controls.

Guiding principles:
- Ship safely: every release must be test-gated and rollback-ready.
- Operate with evidence: use logs, metrics, and alerts as primary signals.
- Prefer incremental hardening: start with minimum viable production controls, then tighten.
- Keep Phase 6 analytics flows first-class in platform operations.

---

## Phase 7 Definition of Done (DoD)
- Containerized backend/frontend images are buildable in CI and runnable in staging/prod.
- CI/CD pipeline performs build + tests + deploy-to-staging + smoke checks + promotion gate.
- Production entrypoint uses TLS termination and reverse proxy routing.
- Centralized logs and metrics dashboards are available for app + infra dependencies.
- Alerting is configured for SLO burn and critical dependency failures.
- Incident/DR runbooks exist and are validated via at least one drill.
- Secrets and environment separation are enforced for dev/staging/prod.
- Cost/capacity guardrails are defined for major infrastructure components.

---

## 0) Phase 7 Kickoff & Scope Lock (P7-M0)
### 0.1 Deployment target decision
- Choose deployment topology (single VM, managed container platform, or Kubernetes-lite).
- Lock environment model: `dev`, `staging`, `prod`.

Decision (locked):
- Target cloud: AWS.
- Initial production topology: EC2 + Docker Compose (single-host baseline).
- Environment model: `dev`, `staging`, `prod`.
- Ownership: personal project (single owner for deploy, CI/CD, and ops).

### 0.2 Production baseline checklist
- Define critical user journeys for smoke tests:
  - auth/login
  - checkout + payment callback
  - admin analytics read paths

Checkpoint:
- Phase 7 acceptance criteria and ownership matrix are agreed. (Completed)

---

## 1) Containerization Baseline (P7-M1)
- Add production Dockerfiles for backend/frontend.
- Provide compose profile for local prod-like validation:
  - app, mysql, redis, rabbitmq, elasticsearch, mongodb.
- Define image tags and versioning strategy.

Checkpoint:
- `docker build` and `docker compose up` succeed in clean environment.

---

## 2) CI/CD Pipeline (P7-M2)
- Extend GitHub Actions (or Jenkins) with:
  - backend tests
  - frontend build
  - container image build
- Add deployment stages:
  - deploy staging
  - smoke tests
  - manual/automated promotion to production
- Add release gates:
  - migration safety check
  - API contract check
  - security scan check

Checkpoint:
- One end-to-end pipeline run reaches staging with passing smoke tests.

---

## 3) Runtime Edge & TLS (P7-M3)
- Configure Nginx/Caddy for:
  - HTTPS termination
  - backend/frontend routing
  - basic security headers
- Ensure payment callback endpoints are publicly reachable and stable.

Checkpoint:
- Domain + HTTPS live; payment provider callbacks validate successfully.

---

## 4) Observability Platform (P7-M4)
- Centralized logging (ELK or Loki).
- Metrics + dashboards (Prometheus + Grafana).
- Integrate existing app metrics (Phase 3 + Phase 6 ETL metrics).
- Alerts:
  - SLO burn rate
  - DB pool exhaustion
  - cache failures
  - broker pressure
  - ETL stale/failure thresholds

Checkpoint:
- On-call can detect and triage incidents from dashboards + alerts only.

---

## 5) Reliability & Incident Readiness (P7-M5)
- Validate scaling behavior:
  - rate limiting/backpressure under load
- Execute DR checklist:
  - backup verification
  - rollback rehearsal
  - failover scenarios (where applicable)
- Write incident runbooks:
  - cache outage
  - broker outage
  - payment webhook delay/failure
  - DB outage/failover

Checkpoint:
- One tabletop or live drill completed with follow-up actions captured.

---

## 6) Secrets, Environment Isolation, and Compliance (P7-M6)
- Move runtime secrets to platform secret store.
- Eliminate plaintext secrets from tracked config files.
- Separate config per environment with explicit override policy.

Checkpoint:
- Security review confirms production config/secrets posture.

---

## 7) Cost & Capacity Controls (P7-M7)
- Add infra cost dashboards for:
  - compute
  - Redis
  - Elasticsearch
  - broker
  - analytics workload growth
- Define capacity thresholds and scale triggers.

Checkpoint:
- Monthly cost variance and saturation risk can be reviewed from dashboards.

---

## 8) Phase 7 Review Package
- Deployment architecture diagram (current production topology).
- CI/CD release flow and gate policy.
- Monitoring dashboards + alert catalog.
- Incident runbooks + DR drill report.
- Capacity/cost baseline with next-quarter forecast.
- Ops baseline runbook:
  - `docs/ops/PHASE7_OPS.md`

Success criteria:
- Team can release predictably, detect regressions fast, and recover from incidents with documented procedures.
