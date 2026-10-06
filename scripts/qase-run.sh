#!/usr/bin/env bash
# Opens or completes one Qase test run that all CI jobs report into.
#
#   scripts/qase-run.sh create "<title>"   -> prints the new run id
#   scripts/qase-run.sh complete <run-id>
#
# Needs QASE_API_TOKEN and QASE_PROJECT in the environment. Never echoes the token.
set -euo pipefail

: "${QASE_API_TOKEN:?QASE_API_TOKEN is not set}"
: "${QASE_PROJECT:?QASE_PROJECT is not set}"
API="https://api.qase.io/v1"

call() {
  local method="$1" path="$2" body="${3:-}"
  local response
  response=$(curl -sS --fail-with-body -X "$method" "$API$path" \
    -H "Token: $QASE_API_TOKEN" \
    -H "Content-Type: application/json" \
    ${body:+-d "$body"})
  if [[ "$(jq -r '.status' <<<"$response")" != "true" ]]; then
    echo "Qase API error on $method $path: $(jq -c '.errorMessage // .' <<<"$response")" >&2
    exit 1
  fi
  echo "$response"
}

case "${1:-}" in
  create)
    title="${2:?run title is required}"
    body=$(jq -n --arg t "$title" --arg d "GitHub Actions: ${GITHUB_SERVER_URL:-}/${GITHUB_REPOSITORY:-}/actions/runs/${GITHUB_RUN_ID:-local}" \
      '{title: $t, description: $d, is_autotest: true}')
    call POST "/run/$QASE_PROJECT" "$body" | jq -r '.result.id'
    ;;
  complete)
    run_id="${2:?run id is required}"
    call POST "/run/$QASE_PROJECT/$run_id/complete" "{}" >/dev/null
    echo "Qase run $run_id completed"
    ;;
  *)
    echo "usage: $0 create <title> | complete <run-id>" >&2
    exit 2
    ;;
esac
