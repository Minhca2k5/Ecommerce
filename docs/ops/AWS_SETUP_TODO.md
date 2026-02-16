# AWS Setup TODO (Fill After Infrastructure Is Ready)

Use this checklist after creating AWS services (EC2, network, domain, SSL).

## 1) Staging Server Info
- [ ] `STAGING_SSH_HOST` (EC2 public IP/domain)
- [ ] `STAGING_SSH_USER` (usually `ubuntu`)
- [ ] `STAGING_SSH_KEY` (private key content)
- [ ] `STAGING_SSH_PORT` (usually `22`)
- [ ] `STAGING_APP_DIR` (e.g. `/opt/ecommerce`)

## 2) Production Server Info
- [ ] `PRODUCTION_SSH_HOST` (EC2 public IP/domain)
- [ ] `PRODUCTION_SSH_USER` (usually `ubuntu`)
- [ ] `PRODUCTION_SSH_KEY` (private key content)
- [ ] `PRODUCTION_SSH_PORT` (usually `22`)
- [ ] `PRODUCTION_APP_DIR` (e.g. `/opt/ecommerce`)

## 3) Server Runtime Files
- [ ] Create `/opt/ecommerce/.env.staging` on staging host
- [ ] Create `/opt/ecommerce/.env.production` on production host
- [ ] Fill real values for:
  - [ ] `JWT_SECRET_KEY`
  - [ ] `MOMO_*`
  - [ ] `MAIL_*`
  - [ ] `GUEST_CHECKOUT_*`
  - [ ] `VITE_API_BASE_URL`

## 4) GitHub Environment Secrets
- [ ] Add all `STAGING_*` secrets to environment `staging`
- [ ] Add all `PRODUCTION_*` secrets to environment `production`

## 5) Deployment Validation
- [ ] Run workflow `Staging Deploy`
- [ ] Verify smoke checks pass
- [ ] Run workflow `Production Promote` (manual gate)
