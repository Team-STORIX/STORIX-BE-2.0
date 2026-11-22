#!/bin/sh
# Elasticsearch 계정 · 권한을 .env 값에 맞춘다. 배포가 ES 를 띄운 뒤 실행한다. 여러 번 돌려도 같다.
#
#   sh es-users.sh
#
# elastic         관리자. 비밀번호는 ES 가 처음 로그인을 켤 때 ELASTIC_PASSWORD 로 정한다
# storix_app      앱 · batch. works-* · feeds-* 인덱스만
# kibana_system   로컬 Kibana 가 ES 에 붙을 때
# storix_reader   사람이 Kibana 로 볼 때. 읽기만
# 비밀번호는 화면에 찍지 않는다.
set -eu
cd "$(dirname "$0")"

val() { sed -n "s/^$1=//p" .env | tail -1; }
ELASTIC_PASSWORD=$(val ELASTIC_PASSWORD)
APP_USER=$(val SPRING_ELASTICSEARCH_USERNAME)
APP_PASSWORD=$(val SPRING_ELASTICSEARCH_PASSWORD)
KIBANA_PASSWORD=$(val ES_KIBANA_SYSTEM_PASSWORD)
READER_PASSWORD=$(val ES_READER_PASSWORD)
ES=${ES_URL:-http://127.0.0.1:9200}

log() { echo "[es-users] $*"; }

# 관리자 계정으로 요청한다. 응답 본문은 버리고 상태 코드만 본다.
# ES 를 막 다시 띄운 직후에는 보안 인덱스가 준비되기 전이라 503 · 연결 실패가 나서 최대 2분 다시 시도한다
es() {
  for _ in $(seq 1 60); do
    _code=$(curl -s -o /dev/null -w '%{http_code}' -u "elastic:$ELASTIC_PASSWORD" \
      -H 'Content-Type: application/json' -X "$1" "$ES$2" ${3:+-d "$3"})
    case $_code in
      2*) return 0 ;;
      000|503) sleep 2 ;;
      *) break ;;
    esac
  done
  log "$1 $2 실패 HTTP $_code"
  return 1
}

es GET '/_cluster/health?wait_for_status=yellow&timeout=60s' || { log "ES 에 관리자로 로그인하지 못했다"; exit 1; }

es PUT /_security/role/storix_app '{
  "cluster": ["monitor"],
  "indices": [{"names": ["works-*", "feeds-*"], "privileges": ["all"]}]
}'
es PUT "/_security/user/$APP_USER" "{\"password\": \"$APP_PASSWORD\", \"roles\": [\"storix_app\"]}"

es POST /_security/user/kibana_system/_password "{\"password\": \"$KIBANA_PASSWORD\"}"

es PUT /_security/role/storix_reader '{
  "cluster": ["monitor"],
  "indices": [{"names": ["works-*", "feeds-*", "works_ai*"], "privileges": ["read", "view_index_metadata", "monitor"]}]
}'
es PUT /_security/user/storix_reader "{\"password\": \"$READER_PASSWORD\", \"roles\": [\"storix_reader\", \"kibana_admin\"]}"

log "계정 · 권한을 맞췄다 ($APP_USER · kibana_system · storix_reader)"
