#!/usr/bin/env bash
# Railway GraphQL helpers for waking/stopping standby services from the Pi.
# Requires: curl, jq, RAILWAY_TOKEN

RAILWAY_GRAPHQL_URL="${RAILWAY_GRAPHQL_URL:-https://backboard.railway.com/graphql/v2}"

railway_api_log() {
  echo "[$(date -Iseconds)] $*"
}

railway_api_require_token() {
  if [[ -z "${RAILWAY_TOKEN:-}" ]]; then
    echo "Missing required variable: RAILWAY_TOKEN" >&2
    exit 1
  fi
  if ! command -v jq >/dev/null 2>&1; then
    echo "jq is required for railway-api.sh (install: sudo apt install -y jq)" >&2
    exit 1
  fi
}

railway_api_graphql() {
  local query="$1"
  local variables="${2:-{}}"
  local dry_run="${3:-false}"
  if [[ "${dry_run}" == "true" ]]; then
    railway_api_log "DRY-RUN GraphQL: ${query}"
    echo "{}"
    return 0
  fi
  local payload
  payload="$(jq -n --arg q "${query}" --argjson v "${variables}" '{query: $q, variables: $v}')"
  curl -sS -X POST "${RAILWAY_GRAPHQL_URL}" \
    -H "Authorization: Bearer ${RAILWAY_TOKEN}" \
    -H "Content-Type: application/json" \
    -d "${payload}"
}

railway_api_get_active_deployment_id() {
  local service_id="$1"
  local environment_id="$2"
  local dry_run="${3:-false}"
  if [[ "${dry_run}" == "true" ]]; then
    railway_api_log "DRY-RUN: get active deployment for service ${service_id}"
    echo "dry-run-deployment-id"
    return 0
  fi
  local query='query($serviceId: String!, $environmentId: String!) {
    deployments(
      input: {
        serviceId: $serviceId
        environmentId: $environmentId
      }
      first: 10
    ) {
      edges {
        node {
          id
          status
        }
      }
    }
  }'
  local variables
  variables="$(jq -n \
    --arg serviceId "${service_id}" \
    --arg environmentId "${environment_id}" \
    '{serviceId: $serviceId, environmentId: $environmentId}')"
  local response
  response="$(railway_api_graphql "${query}" "${variables}" "false")"
  if echo "${response}" | jq -e '.errors' >/dev/null 2>&1; then
    railway_api_log "GraphQL error: $(echo "${response}" | jq -c '.errors')"
    return 1
  fi
  local deployment_id
  deployment_id="$(echo "${response}" | jq -r '
    .data.deployments.edges
    | map(.node)
    | map(select(.status == "SUCCESS" or .status == "RUNNING" or .status == "DEPLOYING" or .status == "CRASHED"))
    | .[0].id // empty
  ')"
  if [[ -z "${deployment_id}" ]]; then
    railway_api_log "No active deployment found for service ${service_id}"
    return 1
  fi
  echo "${deployment_id}"
}

railway_api_service_redeploy() {
  local service_id="$1"
  local environment_id="$2"
  local dry_run="${3:-false}"
  railway_api_log "Redeploying service ${service_id}"
  if [[ "${dry_run}" == "true" ]]; then
    railway_api_log "DRY-RUN: serviceInstanceRedeploy ${service_id}"
    return 0
  fi
  local query='mutation($serviceId: String!, $environmentId: String!) {
    serviceInstanceRedeploy(serviceId: $serviceId, environmentId: $environmentId)
  }'
  local variables
  variables="$(jq -n \
    --arg serviceId "${service_id}" \
    --arg environmentId "${environment_id}" \
    '{serviceId: $serviceId, environmentId: $environmentId}')"
  local response
  response="$(railway_api_graphql "${query}" "${variables}" "false")"
  if echo "${response}" | jq -e '.errors' >/dev/null 2>&1; then
    railway_api_log "serviceInstanceRedeploy failed: $(echo "${response}" | jq -c '.errors')"
    return 1
  fi
  railway_api_log "serviceInstanceRedeploy accepted for ${service_id}"
}

railway_api_deployment_stop() {
  local deployment_id="$1"
  local dry_run="${2:-false}"
  railway_api_log "Stopping deployment ${deployment_id}"
  if [[ "${dry_run}" == "true" ]]; then
    railway_api_log "DRY-RUN: deploymentStop ${deployment_id}"
    return 0
  fi
  local query='mutation($id: String!) {
    deploymentStop(id: $id)
  }'
  local variables
  variables="$(jq -n --arg id "${deployment_id}" '{id: $id}')"
  local response
  response="$(railway_api_graphql "${query}" "${variables}" "false")"
  if echo "${response}" | jq -e '.errors' >/dev/null 2>&1; then
    railway_api_log "deploymentStop failed: $(echo "${response}" | jq -c '.errors')"
    return 1
  fi
  railway_api_log "deploymentStop accepted for ${deployment_id}"
}

railway_api_wake_service() {
  local service_id="$1"
  local environment_id="$2"
  local dry_run="${3:-false}"
  railway_api_service_redeploy "${service_id}" "${environment_id}" "${dry_run}"
}

railway_api_sleep_service() {
  local service_id="$1"
  local environment_id="$2"
  local dry_run="${3:-false}"
  local deployment_id
  if ! deployment_id="$(railway_api_get_active_deployment_id "${service_id}" "${environment_id}" "${dry_run}")"; then
    railway_api_log "Service ${service_id} already stopped or no deployment to stop"
    return 0
  fi
  railway_api_deployment_stop "${deployment_id}" "${dry_run}"
}

railway_api_require_ids() {
  local missing=()
  for var in RAILWAY_PROJECT_ID RAILWAY_ENVIRONMENT_ID RAILWAY_MYSQL_SERVICE_ID RAILWAY_BACKEND_SERVICE_ID; do
    if [[ -z "${!var:-}" ]]; then
      missing+=("$var")
    fi
  done
  if [[ ${#missing[@]} -gt 0 ]]; then
    echo "Missing required Railway API variables: ${missing[*]}" >&2
    exit 1
  fi
}
