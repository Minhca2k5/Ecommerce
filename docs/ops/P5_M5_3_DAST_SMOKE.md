# P5 M5.3 - DAST Smoke for Public/Auth

## What was added
- Script: `scripts/security/dast_smoke.sh`
- CI job: `dast-smoke` in `.github/workflows/security-gates.yml`

## Smoke checks
- `GET /api/public/home` must return `200`.
- `POST /api/auth/login` with suspicious SQLi-like payload must not return `200`.
- Response headers must include:
  - `X-Content-Type-Options`
  - `X-Frame-Options`

## Configuration
Set `DAST_TARGET_URL` in GitHub repository `Variables` or `Secrets`.
Example: `https://staging-api.example.com`

## Verify locally
```bash
bash scripts/security/dast_smoke.sh http://localhost:8080
```
