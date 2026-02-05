#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${1:-}"
if [[ -z "$BASE_URL" ]]; then
  echo "Missing target URL. Usage: dast_smoke.sh <base-url>"
  exit 1
fi

BASE_URL="${BASE_URL%/}"
echo "DAST smoke target: $BASE_URL"

public_status=$(curl -s -o /tmp/public_home.json -w "%{http_code}" "$BASE_URL/api/public/home")
if [[ "$public_status" != "200" ]]; then
  echo "Expected 200 for /api/public/home but got $public_status"
  exit 1
fi

auth_status=$(curl -s -o /tmp/auth_login.json -w "%{http_code}" \
  -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"test@example.com'\'' OR '\''1'\''='\''1","password":"x"}')

if [[ "$auth_status" == "200" ]]; then
  echo "Login SQLi smoke check failed: endpoint accepted suspicious payload."
  exit 1
fi

header_dump=$(mktemp)
curl -s -D "$header_dump" -o /dev/null "$BASE_URL/api/public/home"

grep -qi "^x-content-type-options:" "$header_dump" || { echo "Missing X-Content-Type-Options"; exit 1; }
grep -qi "^x-frame-options:" "$header_dump" || { echo "Missing X-Frame-Options"; exit 1; }

echo "DAST smoke checks passed."
