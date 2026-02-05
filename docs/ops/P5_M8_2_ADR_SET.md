# P5 M8.2 - ADR Set

## ADR-001: Monolith-first architecture
- **Status:** Accepted
- **Context:** Current team size and project scope favor fast iteration over distributed complexity.
- **Decision:** Keep modular monolith architecture for Phase 5.
- **Consequences:** Faster delivery, simpler local debugging, lower ops overhead.

## ADR-002: Service split trigger criteria
- **Status:** Accepted
- **Split candidates:** payment processing, search/indexing, notification delivery.
- **Triggers to split into separate service:**
  1. sustained p95 latency > SLO by 2 releases
  2. independent scaling need > 3x from core API
  3. team ownership split requires separate deployment cadence
  4. compliance/security boundary requires isolation

## ADR-003: Reliability before decomposition
- **Status:** Accepted
- **Decision:** Invest first in SLOs, rollback, observability, and security gates before microservice migration.
