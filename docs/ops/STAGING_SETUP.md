# Staging Setup (AWS EC2)

## Objective
Provision one AWS staging host and make GitHub Actions deploy it through `.github/workflows/staging-deploy.yml`.

## 1) EC2 Provisioning
- Create Ubuntu EC2 instance.
- Attach security group:
  - `22` (SSH) from trusted IP only.
  - `80`, `443` for web access (recommended).
  - Optional direct app ports for troubleshooting: `8080`, `5173`.
- Reserve static public IP (Elastic IP) for stable staging endpoint.

## 2) Server Bootstrap
Run on EC2:

```bash
sudo apt-get update
sudo apt-get install -y git curl ca-certificates
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker "$USER"
newgrp docker
docker --version
docker compose version
```

## 3) App Directory + Env
```bash
sudo mkdir -p /opt/ecommerce
sudo chown -R "$USER":"$USER" /opt/ecommerce
cd /opt/ecommerce
git clone <your-repo-url> .
cp .env.staging.example .env.staging
```

- Edit `.env.staging` with real staging values:
  - `JWT_SECRET_KEY`
  - `MOMO_*`
  - `MAIL_*`
  - `GUEST_CHECKOUT_*`
  - update staging domains for `MOMO_IPN_URL`, `MOMO_REDIRECT_URL`, `VITE_API_BASE_URL`

## 4) GitHub Environment Secrets (`staging`)
In `Settings -> Environments -> staging`, add:
- `STAGING_SSH_HOST`
- `STAGING_SSH_USER`
- `STAGING_SSH_KEY` (private key)
- `STAGING_SSH_PORT` (usually `22`)
- `STAGING_APP_DIR` (optional, default `/opt/ecommerce`)

## 5) First Deploy
- Trigger workflow `Staging Deploy` manually (`workflow_dispatch`), set `deploy_ref` (e.g. `phase7`).
- Verify in Actions that deploy and smoke checks pass.

## 6) Smoke Verification on Host
```bash
cd /opt/ecommerce
docker compose ps
bash scripts/smoke/staging_smoke.sh
```

## 7) Rollback (Baseline)
```bash
cd /opt/ecommerce
git checkout <previous-good-ref>
git pull --ff-only origin <previous-good-ref>
cp .env.staging .env
docker compose up --build -d
```

## Notes
- `staging-deploy.yml` copies `.env.staging` to `.env` before running compose.
- Keep `.env.staging` and `.env.production` only on servers, never in git.
