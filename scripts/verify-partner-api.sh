#!/usr/bin/env bash
# PURPOSE: Operational script for SWARM deployment or maintenance.
# Verify SWARM Partner API connectivity (run after creating org + key in emPOWER Partners UI).
# Usage: ./scripts/verify-partner-api.sh <api_key> [plant_id] [base_url]

set -euo pipefail

API_KEY="${1:?Usage: $0 <api_key> [plant_id] [base_url]}"
PLANT_ID="${2:-1}"
BASE_URL="${3:-https://api.swarm.co.in/api/partner/v1}"

echo "=== Partner health ==="
curl -sf -H "Authorization: Bearer ${API_KEY}" "${BASE_URL}/health" | tee /dev/stderr
echo ""

echo "=== Plants ==="
curl -sf -H "Authorization: Bearer ${API_KEY}" "${BASE_URL}/plants" | tee /dev/stderr
echo ""

echo "=== Plant health (plantId=${PLANT_ID}) ==="
curl -sf -H "Authorization: Bearer ${API_KEY}" "${BASE_URL}/telemetry/health/${PLANT_ID}" | tee /dev/stderr
echo ""

echo "OK — Partner API verified"
