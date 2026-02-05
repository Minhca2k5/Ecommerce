# P5 M5.2 - Secret Scanning (CI + Local Hooks)

## What was added
- CI secret scan:
  - `secret-scan` job in `.github/workflows/security-gates.yml`
  - Uses `gitleaks/gitleaks-action`
- Local Git hooks:
  - `.githooks/pre-commit`
  - `.githooks/pre-push`

## Setup (local)
```bash
git config core.hooksPath .githooks
```

Install gitleaks:
- Windows (choco): `choco install gitleaks`
- macOS (brew): `brew install gitleaks`
- Linux: download from GitHub releases

## Verify
1. Add a fake secret in staged file (for test only), for example `api_key=sk_test_123`.
2. Run `git commit` and confirm pre-commit blocks it.
3. Remove test secret and commit again.
