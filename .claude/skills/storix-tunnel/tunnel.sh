#!/bin/bash
# STORIX 터널 열기 · 닫기 · 상태. BE 레포의 터널 스크립트를 백그라운드로 띄운다.
#   tunnel.sh status
#   tunnel.sh open  db|grafana
#   tunnel.sh close db|grafana|all
set -u
BE=${STORIX_BE_DIR:-$(cd "$(dirname "$0")/../../.." && pwd)}
STATE=$HOME/.storix-tunnel
mkdir -p "$STATE"

script_of() { case $1 in db) echo storix-db-tunnel.sh ;; grafana) echo storix-grafana-tunnel.sh ;; *) return 1 ;; esac; }
port_of()   { case $1 in db) echo 13306 ;; grafana) echo 13000 ;; *) return 1 ;; esac; }
label_of()  { case $1 in db) echo "운영 DB    127.0.0.1:13306" ;; grafana) echo "Grafana    http://localhost:13000" ;; esac; }
is_open()   { lsof -nP -iTCP:"$(port_of "$1")" -sTCP:LISTEN >/dev/null 2>&1; }  # nc -z 로 찌르면 터널 너머 RDS 에 handshake 오류가 쌓인다

status() {
  for n in db grafana; do
    if is_open $n; then echo "열림  $(label_of $n)"; else echo "닫힘  $(label_of $n)"; fi
  done
}

open() {
  local n=$1 s; s=$(script_of "$n") || { echo "db 또는 grafana"; exit 2; }
  if is_open "$n"; then echo "이미 열려 있음  $(label_of "$n")"; return; fi
  [ -f "$BE/$s" ] || { echo "$BE/$s 가 없습니다 (STORIX_BE_DIR 로 경로 지정)"; exit 1; }
  # 새 세션으로 띄워 터미널이 닫혀도 남고, 닫을 때 그룹째 끈다
  nohup perl -MPOSIX -e 'POSIX::setsid(); exec @ARGV' bash "$BE/$s" >"$STATE/$n.log" 2>&1 </dev/null &
  echo $! >"$STATE/$n.pid"
  for _ in $(seq 1 40); do
    is_open "$n" && { echo "열었음  $(label_of "$n")"; return; }
    kill -0 "$(cat "$STATE/$n.pid")" 2>/dev/null || break
    sleep 0.5
  done
  echo "터널이 열리지 않았습니다. 로그:"; tail -5 "$STATE/$n.log"; close "$n" >/dev/null; exit 1
}

close() {
  local n=$1
  if [ "$n" = all ]; then close db; close grafana; return; fi
  local p; p=$(port_of "$n") || { echo "db, grafana, all 중 하나"; exit 2; }
  if [ -f "$STATE/$n.pid" ]; then
    local pid; pid=$(cat "$STATE/$n.pid")
    kill -TERM -- -"$pid" 2>/dev/null || kill -TERM "$pid" 2>/dev/null
    rm -f "$STATE/$n.pid"
  fi
  # aws 프로세스만 끄면 session-manager-plugin 이 포트를 계속 잡는다. 스크립트로 직접 연 것도 포트로 찾아 끈다
  pkill -f "session-manager-plugin.*\"$p\"" 2>/dev/null
  for _ in $(seq 1 10); do is_open "$n" || { echo "닫았음  $n"; return; }; sleep 0.3; done
  echo "포트 $p 가 아직 열려 있습니다 (lsof -i :$p)"; exit 1
}

case ${1:-status} in
  status) status ;;
  open) open "${2:-}" ;;
  close) close "${2:-}" ;;
  *) echo "사용법: tunnel.sh status | open db|grafana | close db|grafana|all"; exit 2 ;;
esac
