# Phase 7 Ops

Status: In Progress (kickoff baseline)

## Objective
Run the platform in a production-ready way with repeatable deploy, basic observability, and incident response readiness.

## Runtime Scope
- App services: `backend`, `frontend`
- Data/infra services: `mysql`, `redis`, `rabbitmq`, `elasticsearch`, `mongodb`
- Compose entrypoint: `docker-compose.yml`
- Environment template: `.env.deploy.example` (copy to `.env`)

## Environment Policy
- Keep real secrets only in `.env` (do not commit).
- Use separate secret sets for `dev`, `staging`, `prod`.
- Required variables before startup:
  - `JWT_SECRET_KEY`
  - `MOMO_*`
  - `MAIL_*`
  - `GUEST_CHECKOUT_*`

## Deploy Baseline (Compose)
1. Prepare env:
   - `Copy-Item .env.deploy.example .env`
   - Fill real values in `.env`
2. Build and start:
   - `docker compose up --build -d`
3. Verify container health:
   - `docker compose ps`
   - `docker compose logs backend --tail=200`

## Smoke Test Checklist
- Frontend loads: `http://localhost:5173`
- Swagger loads: `http://localhost:8080/docs`
- Auth flow works (login/register or admin login)
- Checkout flow reaches order creation
- MoMo callback endpoint is reachable (`/api/public/payments/momo/ipn`)
- Admin analytics endpoints return data:
  - `/api/admin/analytics/funnel`
  - `/api/admin/analytics/top-products`

## Observability Baseline
- Backend logs must include request IDs.
- Track these minimum signals:
  - HTTP 5xx rate
  - DB connection errors
  - Redis connectivity errors
  - RabbitMQ connectivity errors
  - ETL stale/failure metrics from Phase 6

## Incident Quick Actions
1. Service down:
   - `docker compose ps`
   - `docker compose logs <service> --tail=300`
   - restart service: `docker compose up -d <service>`
2. Full stack unstable:
   - `docker compose down`
   - `docker compose up -d`
3. Payment callback issue:
   - verify `MOMO_IPN_URL` in `.env`
   - inspect backend logs for webhook signature/validation failures

## Rollback Baseline
- Keep previous container image tags before deploying new tags.
- On failed release:
  - stop current containers
  - redeploy previous known-good image set
  - rerun smoke tests before reopening traffic

## Next Hardening Steps
- Add centralized logs (Loki/ELK).
- Add Prometheus + Grafana dashboards.
- Add alert rules for SLO burn and dependency outages.
- Run first DR/tabletop drill and capture action items.
