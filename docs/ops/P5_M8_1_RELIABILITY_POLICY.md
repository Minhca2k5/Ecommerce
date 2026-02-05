# P5 M8.1 - Reliability Policy (SLI/SLO/SLA)

## Services in scope
- Auth (login/register OTP)
- Checkout (order create)
- Payment callback (MoMo IPN)
- Order status read

## SLI definitions
- Availability: successful responses / total requests
- Latency: p95 response time
- Correctness: failed business transactions / total business transactions

## Target SLOs
- Auth availability: `99.9%`, p95 `< 500ms`
- Checkout availability: `99.5%`, p95 `< 1200ms`
- Payment callback availability: `99.9%`, p95 `< 800ms`
- Order status read availability: `99.9%`, p95 `< 400ms`

## Error budget policy
- Monthly error budget breach >= 50%: freeze non-critical releases.
- Monthly error budget breach >= 100%: reliability sprint required.

## Escalation
1. On-call engineer triage (0-15 min)
2. Incident owner assignment (15-30 min)
3. Rollback/fix decision (<= 60 min)
4. Postmortem within 48h for Sev-1/Sev-2 incidents
