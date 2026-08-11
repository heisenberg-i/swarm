#!/usr/bin/env bash
# Verify .env.rpi has everything needed for daily Railway sync.
# Usage: ./scripts/verify-railway-sync-env.sh

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
ENV_FILE="${ENV_FILE:-${REPO_ROOT}/.env.rpi}"

if [[ ! -f "${ENV_FILE}" ]]; then
  echo "Missing ${ENV_FILE} — copy from .env.rpi.example" >&2
  exit 1
fi

# shellcheck disable=SC1090
set -a
source "${ENV_FILE}"
set +a

missing=()
for var in \
  DB_PASSWORD \
  RAILWAY_MYSQL_HOST RAILWAY_MYSQL_PASSWORD RAILWAY_MYSQL_DATABASE \
  RAILWAY_TOKEN RAILWAY_PROJECT_ID RAILWAY_ENVIRONMENT_ID \
  RAILWAY_MYSQL_SERVICE_ID RAILWAY_BACKEND_SERVICE_ID; do
  if [[ -z "${!var:-}" ]]; then
    missing+=("$var")
  fi
done

if [[ ${#missing[@]} -gt 0 ]]; then
  echo "Missing required variables in ${ENV_FILE}:" >&2
  printf '  - %s\n' "${missing[@]}" >&2
  exit 1
fi

for cmd in jq curl mysqldump mysql; do
  if ! command -v "${cmd}" >/dev/null 2>&1; then
    echo "Missing command: ${cmd} (install on Pi before first sync)" >&2
    exit 1
  fi
done

echo "OK: ${ENV_FILE} has all daily Railway sync variables."
echo "Next:"
echo "  ./scripts/daily-railway-sync.sh --dry-run"
echo "  ./scripts/daily-railway-sync.sh"
echo "  ./scripts/setup-daily-railway-cron.sh"
