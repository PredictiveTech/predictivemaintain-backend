#!/usr/bin/env bash
#
# Smoke test: walks through the main business flow against a running API, local or deployed.
#
#   scripts/smoke-test.sh http://localhost:8080
#   scripts/smoke-test.sh https://your-app.up.railway.app
#
# It creates two throwaway companies in the target database (their names start with "Smoke").
# Requires: bash, curl, python3. Exit code 0 only if every check passes.
set -uo pipefail

BASE="${1:-}"
if [ -z "$BASE" ]; then
  echo "Usage: $0 <base-url>"
  exit 2
fi
BASE="${BASE%/}"

pass=0
fail=0
JSON='Content-Type: application/json'
TMP="$(mktemp)"
trap 'rm -f "$TMP"' EXIT

check() {   # check <description> <expected> <actual>
  if [ "$2" = "$3" ]; then
    echo "  PASS  $1"
    pass=$((pass + 1))
  else
    echo "  FAIL  $1  (expected $2, got $3)"
    fail=$((fail + 1))
  fi
}
present() { if [ -n "$1" ]; then echo yes; else echo no; fi; }
field() { python3 -c "import sys, json; d = json.load(sys.stdin); print(eval('d' + sys.argv[1]))" "$1" 2>/dev/null; }
code() { curl -s -o /dev/null -w "%{http_code}" --max-time 60 "$@"; }
call() { curl -s --max-time 60 "$@"; }
finish() {
  echo
  echo "Result: $pass passed, $fail failed"
  [ "$fail" -eq 0 ]
  exit $?
}

echo "Smoke test against $BASE"
echo
echo "[1] Availability and documentation"
check "health endpoint answers 200" 200 "$(code "$BASE/actuator/health")"
check "OpenAPI document answers 200" 200 "$(code "$BASE/v3/api-docs")"
check "public plans answers 200" 200 "$(code "$BASE/api/v1/plans")"
check "private endpoint without a token answers 401" 401 "$(code "$BASE/api/v1/assets")"

echo
echo "[2] Two companies register and log in"
RUN="$(date +%s)"
register() {   # register <company> <email> -> HTTP code
  code -X POST "$BASE/api/v1/auth/register" -H "$JSON" \
    -d "{\"companyName\":\"$1\",\"email\":\"$2\",\"password\":\"Secret123!\",\"registrationId\":\"$(python3 -c 'import uuid; print(uuid.uuid4())')\"}"
}
login() {      # login <email> -> access token
  call -X POST "$BASE/api/v1/auth/login" -H "$JSON" -d "{\"email\":\"$1\",\"password\":\"Secret123!\"}" | field '["accessToken"]'
}
EMAIL_A="smoke-a-$RUN@example.com"
EMAIL_B="smoke-b-$RUN@example.com"
check "company A registers (201)" 201 "$(register "Smoke A $RUN" "$EMAIL_A")"
check "company B registers (201)" 201 "$(register "Smoke B $RUN" "$EMAIL_B")"
TOKEN_A="$(login "$EMAIL_A")"
TOKEN_B="$(login "$EMAIL_B")"
check "company A got an access token" yes "$(present "$TOKEN_A")"
check "company B got an access token" yes "$(present "$TOKEN_B")"
if [ -z "$TOKEN_A" ] || [ -z "$TOKEN_B" ]; then
  echo "  Cannot continue without a token."
  fail=$((fail + 1))
  finish
fi
AUTH_A="Authorization: Bearer $TOKEN_A"

echo
echo "[3] Asset, sensor and threshold"
ASSET="$(call -X POST "$BASE/api/v1/assets" -H "$AUTH_A" -H "$JSON" \
  -d "{\"code\":\"SMK-$RUN\",\"name\":\"Smoke pump\",\"location\":\"Plant A\",\"assetType\":\"PUMP\",\"criticality\":\"HIGH\"}" | field '["id"]')"
check "asset created" yes "$(present "$ASSET")"
SENSOR_JSON="$(call -X POST "$BASE/api/v1/assets/$ASSET/sensors" -H "$AUTH_A" -H "$JSON" -d '{"metric":"VIBRATION","unit":"mm/s"}')"
SENSOR="$(echo "$SENSOR_JSON" | field '["id"]')"
KEY="$(echo "$SENSOR_JSON" | field '["deviceKey"]')"
check "sensor registered, with its device key" yes "$(present "$KEY")"
check "threshold configured (200)" 200 "$(code -X PUT "$BASE/api/v1/sensors/$SENSOR/threshold" -H "$AUTH_A" -H "$JSON" \
  -d '{"lowerBound":10,"upperBound":80,"severity":"CRITICAL"}')"

echo
echo "[4] The sensor sends readings (no user session: only its device key)"
send_reading() {   # send_reading <deviceKey> <sourceKey> <value> -> HTTP code; the body is left in $TMP
  curl -s --max-time 60 -o "$TMP" -w "%{http_code}" -X POST "$BASE/api/v1/sensors/readings" \
    -H "X-Device-Key: $1" -H "$JSON" \
    -d "{\"sensorId\":\"$SENSOR\",\"sourceKey\":\"$2\",\"value\":$3,\"unit\":\"mm/s\",\"measuredAt\":\"$(date -u +%Y-%m-%dT%H:%M:%SZ)\"}"
}
check "in-range reading accepted (201)" 201 "$(send_reading "$KEY" "S1-$RUN" 50)"
check "wrong device key rejected (401)" 401 "$(send_reading "pmk_wrong" "S2-$RUN" 50)"
sleep 1.5   # the API refuses readings of one sensor that arrive less than a second apart
check "out-of-range reading accepted (201)" 201 "$(send_reading "$KEY" "S3-$RUN" 95)"
check "that reading raised an alert" true "$(field '["alertRaised"]' < "$TMP" | tr 'A-Z' 'a-z')"

echo
echo "[5] The alert reaches the manager and becomes a work order"
ALERTS="$(call "$BASE/api/v1/alerts" -H "$AUTH_A")"
ALERT="$(echo "$ALERTS" | field '["items"][0]["id"]')"
check "one alert is listed" 1 "$(echo "$ALERTS" | field '["totalElements"]')"
check "the alert carries its diagnostic" VIBRATION "$(echo "$ALERTS" | field '["items"][0]["diagnostic"]["metric"]')"
check "alert confirmed (200)" 200 "$(code -X PATCH "$BASE/api/v1/alerts/$ALERT/status" -H "$AUTH_A" -H "$JSON" -d '{"status":"CONFIRMED"}')"
check "work order created (201)" 201 "$(code -X POST "$BASE/api/v1/work-orders" -H "$AUTH_A" -H "$JSON" -d "{\"alertId\":\"$ALERT\"}")"

echo
echo "[6] Subscription and isolation between companies"
check "subscription panel answers 200" 200 "$(code "$BASE/api/v1/subscription" -H "$AUTH_A")"
check "the initial invoice exists" 1 "$(call "$BASE/api/v1/invoices" -H "$AUTH_A" | field '["totalElements"]')"
check "company B cannot see company A's asset (404)" 404 "$(code "$BASE/api/v1/assets/$ASSET" -H "Authorization: Bearer $TOKEN_B")"

finish