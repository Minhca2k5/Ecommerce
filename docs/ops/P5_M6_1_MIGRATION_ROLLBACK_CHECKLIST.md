# P5 M6.1 - Migration Rollback Checklist

Use this checklist for every Liquibase release.

## Before release
- Backup DB (`mysqldump`) and record backup file path.
- Record current Liquibase state (`DATABASECHANGELOG` count + latest id).
- Validate rollback SQL for each new changeset.
- Confirm app version + changelog version mapping in release notes.

## During release
- Apply migration in staging first.
- Validate critical flows: login, product listing, checkout, payment callback.
- Monitor error rate, DB locks, slow queries.

## Rollback trigger
- Migration fails or critical flow broken.
- Error rate exceeds threshold for 10+ minutes.
- Data consistency checks fail.

## Rollback steps
1. Stop write traffic (maintenance or scale app to zero write workers).
2. Restore DB from latest verified backup.
3. Re-deploy last stable app version.
4. Run sanity checks.
5. Re-open traffic.

## Evidence to keep
- Backup file name + timestamp
- Migration start/end time
- Rollback start/end time (if executed)
- Incident summary and follow-up actions
