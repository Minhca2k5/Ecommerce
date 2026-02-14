# Backend-First Data Roadmap (For This Ecommerce Project)

## Goal
Become a strong Backend Intern/Fresher with practical Data Engineering capability that directly improves this project.

- Primary identity: Backend Engineer
- Secondary edge: Data-aware backend (event tracking, ETL, analytics API)
- Principle: Apply only when it improves product quality, reliability, or measurable performance.

## Why This Roadmap
You already have a large backend system with Spring Boot, RabbitMQ, Redis, Mongo sink, MySQL, Elasticsearch, tests, and security gates. You do not need random new tools. You need focused, high-ROI additions.

## Working Ratio
- 80% backend core
- 20% data engineering practical

## 10-Week Plan (Project-Driven)

### Week 1-2: Analytics Data Contract
Objective: Standardize events so downstream data is trustworthy.

Deliverables:
- Define event taxonomy:
  - `VIEW_PRODUCT`
  - `ADD_TO_CART`
  - `PLACE_ORDER`
  - `PAYMENT_SUCCESS`
- Define required fields for every event:
  - `eventType`, `eventTime`, `requestId`, `userId` or `guestId`, `productId` (if relevant), `source`
- Add validation and fallback behavior:
  - Reject/ignore malformed event payloads
  - Keep sink failures non-blocking for user flow

Apply in current project:
- Extend `backend/src/main/java/com/minzetsu/ecommerce/mongo/ClickstreamEventService.java`
- Keep existing "safe save" pattern but add metrics/logging for dropped events.

Success criteria:
- Event schema documented in repo
- Event fields consistent across all emission points

### Week 3-4: Build ETL Batch to MySQL
Objective: Turn raw clickstream into queryable analytics metrics.

Deliverables:
- Create MySQL table `daily_product_metrics`:
  - `metric_date`, `product_id`, `views`, `add_to_cart`, `orders`, `unique_users`, `conversion_rate`
- Implement daily scheduled job:
  - Read Mongo clickstream by date range
  - Aggregate metrics
  - Upsert into MySQL
- Handle idempotency:
  - Re-run same day without double counting (truncate+recompute or deterministic upsert)

Success criteria:
- Job rerun is safe
- Metrics are reproducible for same input

### Week 5-6: Serve Analytics via Backend API
Objective: Expose useful analytics to admin side.

Deliverables:
- Add admin analytics APIs:
  - `GET /api/admin/analytics/funnel?from=...&to=...`
  - `GET /api/admin/analytics/top-products?from=...&to=...&limit=...`
- Add DTO + filter + pagination conventions aligned with existing project style
- Add simple cache for heavy read endpoints (short TTL)

Success criteria:
- API latency is stable under repeated reads
- Returned metrics match ETL table

### Week 7-8: Reliability + Observability for Data Flows
Objective: Data pipelines should be debuggable and production-like.

Deliverables:
- Add ETL job logs with request/correlation context
- Add metrics:
  - processed events
  - dropped events
  - ETL duration
  - ETL failures
- Add retry policy where appropriate (bounded retries only)
- Add data quality checks:
  - null checks
  - duplicate keys
  - missing date partitions

Success criteria:
- You can explain exactly what failed and where in <5 minutes
- Failed run does not corrupt analytics table

### Week 9-10: Testing + CV Story Packaging
Objective: Convert work into interview proof.

Deliverables:
- Unit tests:
  - aggregation logic
  - conversion calculation
  - idempotent upsert behavior
- Integration tests:
  - ETL happy path
  - ETL rerun path
  - analytics API contract
- Add one concise architecture doc with diagrams:
  - event emitters -> Mongo sink -> ETL -> MySQL metrics -> admin analytics API

Success criteria:
- You can demo end-to-end in 10 minutes
- You can explain one tradeoff decision clearly (e.g., batch over streaming for internship scope)

## What NOT to Add Now
Do not add these unless current scope is stable:
- Kafka migration (you already have RabbitMQ)
- Spark cluster
- Airflow + dbt together
- Data Lake/Data Mesh architecture
- Kubernetes-only complexity

Reason: high complexity, low internship ROI for your current objective.

## Technology Decisions (For This Project)
Use current stack first:
- Event bus: keep RabbitMQ
- Raw event sink: keep MongoDB
- Source of record + analytics serving: MySQL
- API layer: Spring Boot
- Cache: Redis (only where needed)

Optional later (only if all milestones above are done):
- Add one orchestrator (Airflow or Spring Scheduler enhancement) for better job observability

## CV Positioning (How to describe this)
Use language like:
- "Built backend event tracking and daily ETL pipeline from Mongo clickstream to MySQL analytics mart."
- "Designed admin analytics APIs for funnel and top-product conversion with caching and reliability checks."
- "Implemented idempotent batch processing, data quality checks, and integration tests for analytics reliability."

## Weekly Execution Rule
Every week must produce all 3:
- 1 code change
- 1 test improvement
- 1 measurable outcome (latency, correctness, or reliability)

If a new technology does not improve one of these, skip it.

## Answer to Your Question: Learn by project vs theory
Yes, for your goal this is correct: learning through your real project is much more effective.

Best approach:
- 20% theory (only what is needed to avoid blind coding)
- 80% implementation + testing + explaining tradeoffs

Theory is still necessary, but it should be just-in-time and tied to a concrete feature in this repository.
