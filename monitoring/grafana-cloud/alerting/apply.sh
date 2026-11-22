#!/usr/bin/env bash
# Grafana Cloud 에 STORIX 알림 폴더 · 알림 정책 · 규칙을 그대로 덮어쓴다
#
#   GRAFANA_CLOUD_URL=https://<스택>.grafana.net GRAFANA_CLOUD_SA_TOKEN=<서비스 계정 토큰> ./apply.sh
#
# 연락처 slack-storix 는 Slack 웹후크가 들어 있어 이미 있으면 건드리지 않는다
# 처음 만들 때만 SLACK_WEBHOOK_URL 을 같이 넘긴다
# 화면에서 고친 규칙은 덮어써지니, 고친 뒤에는 이 폴더 파일에도 반영한다
set -euo pipefail

cd "$(dirname "$0")"
: "${GRAFANA_CLOUD_URL:?GRAFANA_CLOUD_URL 이 없다}"
: "${GRAFANA_CLOUD_SA_TOKEN:?GRAFANA_CLOUD_SA_TOKEN 이 없다}"
FOLDER_UID=storix
RESPONSE=$(mktemp)
BODY=$(mktemp)
trap 'rm -f "$RESPONSE" "$BODY"' EXIT

# 토큰이 명령행 인자로 ps 에 보이지 않게 헤더는 파일 디스크립터로 넘긴다
api() {
  local method=$1 path=$2 body=${3:-}
  curl -sS -o "$RESPONSE" -w '%{http_code}' -X "$method" \
    -H @<(printf 'Authorization: Bearer %s\n' "$GRAFANA_CLOUD_SA_TOKEN") \
    -H 'Content-Type: application/json' \
    -H 'X-Disable-Provenance: true' \
    ${body:+--data-binary "@$body"} \
    "$GRAFANA_CLOUD_URL$path"
}

check() {
  local code=$1 what=$2
  if [[ $code != 2* ]]; then
    echo "실패 $what (HTTP $code): $(head -c 300 "$RESPONSE")" >&2
    exit 1
  fi
  echo "적용 $what"
}

if [[ $(api GET "/api/folders/$FOLDER_UID") == 404 ]]; then
  printf '{"uid":"%s","title":"STORIX"}' "$FOLDER_UID" > "$BODY"
  check "$(api POST /api/folders "$BODY")" "폴더 STORIX"
fi

check "$(api GET '/api/v1/provisioning/contact-points?name=slack-storix')" "연락처 조회"
if grep -q '"uid":"slack-storix"' "$RESPONSE"; then
  echo "연락처 slack-storix 는 이미 있어 그대로 둔다"
else
  : "${SLACK_WEBHOOK_URL:?연락처 slack-storix 가 없다. 처음 만들 때는 SLACK_WEBHOOK_URL 을 넘긴다}"
  python3 -c 'import json,os,sys; c=json.load(open("contact-point.json")); c["settings"]["url"]=os.environ["SLACK_WEBHOOK_URL"]; json.dump(c,sys.stdout,ensure_ascii=False)' > "$BODY"
  check "$(api POST /api/v1/provisioning/contact-points "$BODY")" "연락처 slack-storix"
fi

check "$(api PUT /api/v1/provisioning/policies policies.json)" "알림 정책"

for file in rule-groups/*.json; do
  group=$(basename "$file" .json)
  check "$(api PUT "/api/v1/provisioning/folder/$FOLDER_UID/rule-groups/$group" "$file")" "규칙 그룹 $group"
done
