#!/usr/bin/env bash
# Install daily Railway sync cron on the Pi (run once as swarm user).
#
# Usage:
#   ./scripts/setup-daily-railway-cron.sh
#   ./scripts/setup-daily-railway-cron.sh --show

set -euo pipefail

REPO_DIR="$(cd "$(dirname "$0")/.." && pwd)"
CRON_DAILY="0 2 * * * cd ${REPO_DIR} && ./scripts/daily-railway-sync.sh >> /var/log/swarm-sync.log 2>&1"
CRON_WEEKLY="0 3 * * 0 cd ${REPO_DIR} && ./scripts/sync-to-railway.sh --dump-only >> /var/log/swarm-sync.log 2>&1"

if [[ "${1:-}" == "--show" ]]; then
  echo "# Add these lines with: crontab -e"
  echo "${CRON_DAILY}"
  echo "${CRON_WEEKLY}"
  exit 0
fi

chmod +x "${REPO_DIR}/scripts/daily-railway-sync.sh" \
  "${REPO_DIR}/scripts/railway-api.sh" \
  "${REPO_DIR}/scripts/sync-to-railway.sh"

existing="$(crontab -l 2>/dev/null || true)"
{
  echo "${existing}" | grep -v 'daily-railway-sync.sh' | grep -v 'sync-to-railway.sh --dump-only' || true
  echo "${CRON_DAILY}"
  echo "${CRON_WEEKLY}"
} | sed '/^$/d' | crontab -

echo "Installed daily Railway sync cron:"
crontab -l | grep -E 'daily-railway-sync|sync-to-railway.sh --dump-only'
