# Phase 3 M0 Performance Artifacts

This folder contains baseline scope, k6 scripts, and report templates for Phase 3.

## Scope
See `scope.md` for the benchmark scope and SLOs.

## k6 usage
Set env vars before running:
- K6_BASE_URL (default: http://localhost:8080)
- K6_USER_TOKEN (JWT for user flows)
- K6_ADMIN_TOKEN (optional)
- ....

Examples (PowerShell):
```
$env:K6_BASE_URL = 'http://localhost:8080'
$env:K6_USER_TOKEN = '<jwt>'

k6 run .\k6\public-read.js
k6 run .\k6\user-mixed.js
```

## Reports
- `baseline_results/` contains raw outputs (CSV/JSON).
- `report_template.md` is used for before/after reporting.
