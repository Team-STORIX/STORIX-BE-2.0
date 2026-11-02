#!/bin/sh
# 앱 서버 배포. CD 가 SSM 으로 /home/ubuntu/storix 에서 실행한다.
#
#   sh deploy.sh app <dev|prod> <IMAGE_TAG> <SITE_DOMAIN> <PULL_BATCH:true|false>
#   sh deploy.sh batch     # 미뤄 둔 batch 교체. deploy.sh 가 systemd 타이머로 건다
#
# app  : blue · green 중 안 쓰는 색에 새 버전을 띄우고, 헬스체크가 통과하면 Caddy 가 그쪽으로 보내게 바꾼 뒤 옛 색을 내린다.
#        새 버전이 안 뜨면 옛 색을 그대로 두고 실패로 끝난다.
# batch: 두 벌을 같이 띄우면 같은 작업이 두 번 돌아서 내렸다 올린다.
#        매시 정각 · 5분 작업과 새벽 정리 작업 시간에 걸리면 그 시간이 지난 뒤로 미룬다.
set -eu
cd /home/ubuntu/storix

STATE=deploy.state
HEALTH_TRIES=90          # 2초 간격, 최대 3분
MIN_FREE_MB=900          # 새 app 이 뜰 여유 메모리. 모자라면 옛 색을 먼저 내려서 잠깐 끊긴다

log() { echo "[deploy] $*"; }
running() { docker ps --format '{{.Names}}' | grep -qx "$1"; }
state_get() { sed -n "s/^$1=//p" "$STATE" 2>/dev/null | tail -1; }

write_state() {
  cat > "$STATE" << EOF
DEPLOY_ENV=$DEPLOY_ENV
IMAGE_TAG=$IMAGE_TAG
ACTIVE_COLOR=$ACTIVE_COLOR
SITE_DOMAIN=$SITE_DOMAIN
EOF
}

# ---------- batch ----------

# KST 기준 지금 batch 를 내리면 안 되는 시간인지
#   매시 :57 ~ :06  정각 · 5분에 도는 랭킹 · 알림 · 성인인증 작업
#   03:57 ~ 05:06   04:00 ~ 04:50 에 도는 하드 삭제 · 개인정보 파기 · 푸시 기기 정리
blocked_at() {
  _m=$(( $1 % 60 ))
  [ "$_m" -ge 57 ] || [ "$_m" -lt 7 ] && return 0
  [ "$1" -ge 237 ] && [ "$1" -lt 307 ] && return 0
  return 1
}

# 다음으로 batch 를 내려도 되는 시각까지 남은 초. 지금 되면 0
batch_wait_seconds() {
  _h=$(TZ=Asia/Seoul date +%-H); _m=$(TZ=Asia/Seoul date +%-M); _s=$(TZ=Asia/Seoul date +%-S)
  _now=$(( _h * 60 + _m )); _t=$_now
  while blocked_at $(( _t % 1440 )); do _t=$(( _t + 1 )); done
  if [ "$_t" -eq "$_now" ]; then echo 0; else echo $(( (_t - _now) * 60 - _s + 5 )); fi
}

swap_batch() {
  log "batch 교체 ($IMAGE_TAG)"
  docker compose up -d --no-deps batch
}

schedule_batch() {
  _wait=$(batch_wait_seconds)
  # 앞서 미뤄 둔 교체가 있으면 지우고 새로 건다. 교체 때는 그때 deploy.state 의 최신 태그를 쓴다
  systemctl stop storix-batch-swap.timer 2>/dev/null || true
  systemctl reset-failed storix-batch-swap.service 2>/dev/null || true
  if [ "$_wait" -eq 0 ]; then
    swap_batch
  else
    log "예약 작업 시간이라 batch 교체를 ${_wait}초 뒤로 미룬다"
    systemd-run --unit=storix-batch-swap --on-active="${_wait}s" --timer-property=AccuracySec=1s \
      --setenv=HOME=/root --working-directory=/home/ubuntu/storix \
      /bin/sh /home/ubuntu/storix/deploy.sh batch
  fi
}

if [ "${1:-}" = "batch" ]; then
  exec 9>.deploy.lock
  flock 9
  DEPLOY_ENV=$(state_get DEPLOY_ENV); IMAGE_TAG=$(state_get IMAGE_TAG)
  SITE_DOMAIN=$(state_get SITE_DOMAIN); ACTIVE_COLOR=$(state_get ACTIVE_COLOR)
  export IMAGE_TAG SITE_DOMAIN
  swap_batch
  exit 0
fi

# ---------- app ----------

[ "${1:-}" = "app" ] || { echo "usage: deploy.sh app <env> <tag> <domain> <pull_batch> | deploy.sh batch"; exit 2; }
DEPLOY_ENV=$2 IMAGE_TAG=$3 SITE_DOMAIN=$4 PULL_BATCH=$5
export IMAGE_TAG SITE_DOMAIN

OLD=""
PREV=$(state_get ACTIVE_COLOR)
if [ -n "$PREV" ] && running "storix-app-$PREV"; then OLD=$PREV; fi
# blue/green 이전 compose 의 단일 컨테이너. 첫 배포 때 한 번만 거친다
LEGACY=""
running storix-app && LEGACY=storix-app
OLD_NAME=${OLD:+storix-app-$OLD}
OLD_NAME=${OLD_NAME:-$LEGACY}

NEW=blue
[ "$OLD" = blue ] && NEW=green
NEW_NAME=storix-app-$NEW
log "현재: ${OLD_NAME:-없음} → 새: $NEW_NAME ($IMAGE_TAG)"

docker compose --profile "$NEW" pull "app-$NEW"
[ "$PULL_BATCH" = "true" ] && docker compose pull batch
docker rm -f "$NEW_NAME" 2>/dev/null || true

FREE_MB=$(awk '/MemAvailable/ { print int($2 / 1024) }' /proc/meminfo)
STOPPED_FIRST=""
if [ -n "$OLD_NAME" ] && [ "$FREE_MB" -lt "$MIN_FREE_MB" ]; then
  log "여유 메모리 ${FREE_MB}MB < ${MIN_FREE_MB}MB 라 $OLD_NAME 을 먼저 내린다. 잠깐 끊긴다"
  docker stop -t 40 "$OLD_NAME"
  STOPPED_FIRST=1
fi

docker compose --profile "$NEW" up -d --no-deps "app-$NEW"

# 컨테이너 IP 로 직접 확인한다. 첫 배포 때는 Caddy 가 아직 없다
IP=$(docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' "$NEW_NAME")
i=0
until curl -sf -o /dev/null "http://$IP:9100/actuator/health"; do
  i=$(( i + 1 ))
  if [ "$i" -ge "$HEALTH_TRIES" ] || ! running "$NEW_NAME"; then
    log "$NEW_NAME 가 뜨지 않았다. 옛 버전을 그대로 둔다"
    docker logs --tail 80 "$NEW_NAME" || true
    docker rm -f "$NEW_NAME" || true
    [ -n "$STOPPED_FIRST" ] && docker start "$OLD_NAME"
    exit 1
  fi
  sleep 2
done
log "$NEW_NAME 헬스체크 통과"

# Caddy 가 새 색으로 보내게 바꾼다. 마운트한 디렉터리 안 파일을 제자리에서 덮어쓴다
mkdir -p caddy
cat > caddy/upstream.caddy << EOF
(app_upstream) {
	to $NEW_NAME:8080
}
(metrics_upstream) {
	to $NEW_NAME:9100
}
EOF

# 옛 단일 컨테이너가 8080 · 9100 을 잡고 있어서 내린 뒤에 Caddy 를 띄운다
if [ -n "$LEGACY" ]; then
  log "옛 $LEGACY 를 내리고 Caddy 를 띄운다"
  docker stop -t 40 "$LEGACY" && docker rm "$LEGACY"
fi
docker compose up -d --no-deps caddy
i=0
until docker exec storix-caddy caddy reload --config /etc/caddy/Caddyfile 2>/dev/null; do
  i=$(( i + 1 )); [ "$i" -ge 15 ] && { log "caddy reload 실패"; exit 1; }
  sleep 2
done
curl -sf -o /dev/null http://127.0.0.1:9100/actuator/health || { log "Caddy 를 거친 헬스체크 실패"; exit 1; }
log "트래픽을 $NEW_NAME 로 넘겼다"

ACTIVE_COLOR=$NEW
write_state

if [ -n "$OLD" ]; then
  docker compose --profile "$OLD" stop "app-$OLD"
  docker compose --profile "$OLD" rm -f "app-$OLD"
fi

# batch · app 을 뺀 cache · elasticsearch · alloy 는 설정이 바뀐 것만 다시 뜬다
OTHERS=$(docker compose config --services | grep -vx -e batch -e caddy)
# shellcheck disable=SC2086
docker compose up -d --no-deps $OTHERS

schedule_batch

# 미뤄 둔 batch 이미지는 방금 받아 아직 안 쓰므로 만든 지 얼마 안 된 이미지는 남긴다
docker image prune -af --filter "until=6h" > /dev/null
log "끝"
