# P5-M1.2: DB Backup/Restore Drill and Data Retention Policy

Status: Draft (ready to run in local/staging)

## 1) Scope
- Database: MySQL `ecommerce`
- Source of truth schema: Liquibase changelog
- Objective:
  - prove backup + restore workflow is repeatable
  - capture rough RTO/RPO values
  - define retention windows by data type

---

## 2) Prerequisites
- A MySQL instance with `ecommerce` data.
- `mysqldump` and `mysql` CLI available.
- Disk space for backup artifacts.
- App writes paused (or maintenance window) for consistent full backup.

---

## 3) Backup Procedure (Full Snapshot)

Example command:

```powershell
$ts = Get-Date -Format "yyyyMMdd_HHmmss"
mysqldump -h localhost -P 3306 -u project -p --single-transaction --routines --events ecommerce > "backup_ecommerce_$ts.sql"
```

Notes:
- `--single-transaction` keeps snapshot consistency for InnoDB with minimal lock impact.
- Store result with timestamp naming.

Validation:
- File exists and non-zero size.
- Quick grep for core tables (users/orders/order_items/payments).

---

## 4) Restore Drill Procedure

Create restore target DB:

```sql
CREATE DATABASE ecommerce_restore;
```

Run restore:

```powershell
mysql -h localhost -P 3306 -u project -p ecommerce_restore < backup_ecommerce_YYYYMMDD_HHMMSS.sql
```

Post-restore checks:
- Row counts for critical tables:
  - `users`
  - `orders`
  - `order_items`
  - `payments`
  - `inventory`
- Application smoke query works against restored DB.

Suggested verification SQL:

```sql
SELECT 'users' AS t, COUNT(*) c FROM users
UNION ALL SELECT 'orders', COUNT(*) FROM orders
UNION ALL SELECT 'order_items', COUNT(*) FROM order_items
UNION ALL SELECT 'payments', COUNT(*) FROM payments
UNION ALL SELECT 'inventory', COUNT(*) FROM inventory;
```

---

## 5) RTO/RPO Capture Template

- Backup start/end time:
- Restore start/end time:
- Total restore duration (RTO observed):
- Data staleness at restore point (RPO observed):
- Failures encountered:
- Recovery actions taken:

Current baseline target (dev/staging):
- RTO target: <= 30 minutes
- RPO target: <= 24 hours (daily full snapshot)

---

## 6) Data Retention Policy (Initial)

| Data class | Tables (examples) | Retention | Action |
| --- | --- | --- | --- |
| Transactional core | users, orders, order_items, payments, inventory | Long-term | Keep, archive by year when needed |
| Security/audit | audit_logs, auth-related OTP/logs | 180 days hot, 365 days archive | Mask sensitive fields + scheduled purge/archive |
| Notification/event logs | notifications, transient event history | 90 days | Periodic cleanup |
| Idempotency keys | idempotency_keys | 30 days | Scheduled purge |

Policy rules:
- No hard delete for financial/order records unless legal/compliance requires.
- Audit logs must never store plaintext secrets/tokens/passwords.
- Purge jobs run off-peak with batch limits.

---

## 7) Operational Cadence
- Full backup: daily.
- Restore drill: monthly in staging.
- Retention review: quarterly.
- Incident path: if restore fails, escalate and keep broken snapshot for forensics.

---

## 8) Deliverables for Phase 5 Review
- Backup artifact naming convention and storage location.
- One successful restore drill report (timestamps + row-count checks).
- RTO/RPO observed values.
- Signed-off retention policy table.
