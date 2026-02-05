# P5 M5.1 - SAST + Dependency Scan in CI

## What was added
- GitHub Actions workflow: `.github/workflows/security-gates.yml`
- SAST: CodeQL for Java (`codeql-sast` job)
- Dependency vulnerability scan: OWASP Dependency-Check (`dependency-scan` job)

## Gate policy
- Dependency scan fails build for CVSS `>= 7`.
- Reports are uploaded as CI artifacts (`dependency-check-report`).

## Verify
1. Push a branch / open PR.
2. Confirm jobs run:
   - `SAST (CodeQL)`
   - `Dependency Scan (OWASP)`
3. Confirm dependency report artifact is attached.
