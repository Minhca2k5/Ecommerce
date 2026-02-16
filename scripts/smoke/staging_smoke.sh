#!/usr/bin/env bash
set -euo pipefail

BACKEND_URL="${BACKEND_URL:-http://127.0.0.1:8080}"
FRONTEND_URL="${FRONTEND_URL:-http://127.0.0.1:5173}"

echo "[smoke] checking backend health: ${BACKEND_URL}/actuator/health"
curl --fail --silent "${BACKEND_URL}/actuator/health" > /dev/null

echo "[smoke] checking backend docs: ${BACKEND_URL}/docs"
curl --fail --silent "${BACKEND_URL}/docs" > /dev/null

echo "[smoke] checking frontend index: ${FRONTEND_URL}"
curl --fail --silent "${FRONTEND_URL}" > /dev/null

echo "[smoke] checking public products endpoint"
curl --fail --silent "${BACKEND_URL}/api/public/products?page=0&size=1" > /dev/null

echo "[smoke] staging smoke checks passed"
