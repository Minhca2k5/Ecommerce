# P5 M6.2 - Migration Rehearsal Runbook

## Goal
Rehearse migration and recovery on production-like data before production release.

## Inputs
- Latest production-like backup
- Target app build
- Target Liquibase changelog version

## Rehearsal steps
1. Restore backup to rehearsal DB.
2. Record baseline metrics:
   - table row counts (users/orders/order_items/payments/inventory)
   - startup time
3. Run migration with target build.
4. Run smoke checks:
   - auth login/register OTP request
   - browse/search product
   - add to cart + checkout
   - payment callback endpoint
5. Record migration duration and any warnings.
6. Simulate rollback:
   - restore pre-migration backup
   - re-run smoke checks
7. Record RTO/RPO observed values.

## Acceptance
- Migration succeeds with no data-loss indicators.
- Rollback works and restores baseline counts.
- Rehearsal evidence attached to release ticket.
