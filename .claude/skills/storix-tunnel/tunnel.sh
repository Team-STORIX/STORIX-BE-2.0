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
# nc -z 로 찌르면 터널 너머 RDS 에 handshake 오류가 쌓여서 lsof 로 본다
listener()  { lsof -nP +c 0 -iTCP:"$(port_of "$1")" -sTCP:LISTEN -Fc 2>/dev/null | sed -n 's/^c//p' | head -1; }
is_open()   { [ "$(listener "$1")" = session-manager-plugin ]; }
taken()     { local c; c=$(listener "$1"); [ -n "$c" ] && [ "$c" != session-manager-plugin ]; }

status() {
  for n in db grafana; do
    if is_open $n; then echo "열림  $(label_of $n)"
    elif taken $n; then echo "충돌  $(label_of $n)  다른 프로그램($(listener $n))이 포트 사용 중"
    else echo "닫힘  $(label_of $n)"; fi
  done
}

open() {
  local n=$1 s; s=$(script_of "$n") || { echo "db 또는 grafana"; exit 2; }
  if is_open "$n"; then echo "이미 열려 있음  $(label_of "$n")"; return; fi
  if taken "$n"; then echo "포트 $(port_of "$n") 를 다른 프로그램($(listener "$n"))이 쓰고 있어 열지 않음"; exit 1; fi
  [ -f "$BE/$s" ] || { echo "$BE/$s 가 없습니다 (STORIX_BE_DIR 로 경로 지정)"; exit 1; }
  # 새 세션으로 띄워 터미널이 닫혀도 남고, 닫을 때 그룹째 끈다
  nohup perl -MPOSIX -e 'POSIX::setsid(); exec @ARGV' bash "$BE/$s" >"$STATE/$n.log" 2>&1 </dev/null &
  echo $! >"$STATE/$n.pid"
  # 기다리는 중에 끊기면 새 세션으로 뜬 터널만 남으니 닫고 나간다
  trap 'close "$n" >/dev/null; exit 130' INT TERM HUP
  for _ in $(seq 1 40); do
    is_open "$n" && { trap - INT TERM HUP; echo "열었음  $(label_of "$n")"; return; }
    kill -0 "$(cat "$STATE/$n.pid")" 2>/dev/null || break
    sleep 0.5
  done
  trap - INT TERM HUP
  echo "터널이 열리지 않았습니다. 로그:"; tail -5 "$STATE/$n.log"; close "$n" >/dev/null; exit 1
}

close() {
  local n=$1
  if [ "$n" = all ]; then
    local rc=0; close db || rc=1; close grafana || rc=1; return $rc
  fi
  local p; p=$(port_of "$n") || { echo "db, grafana, all 중 하나"; exit 2; }
  if [ -f "$STATE/$n.pid" ]; then
    local pid; pid=$(cat "$STATE/$n.pid")
    kill -TERM -- -"$pid" 2>/dev/null || kill -TERM "$pid" 2>/dev/null
    rm -f "$STATE/$n.pid"
  fi
  # aws 프로세스만 끄면 session-manager-plugin 이 포트를 계속 잡는다. 스크립트로 직접 연 것도 포트로 찾아 끈다
  pkill -f "session-manager-plugin.*\"$p\"" 2>/dev/null
  for _ in $(seq 1 10); do is_open "$n" || { echo "닫았음  $n"; return 0; }; sleep 0.3; done
  echo "포트 $p 가 아직 열려 있습니다 (lsof -i :$p)"; return 1
}

case ${1:-status} in
  status) status ;;
  open) open "${2:-}" ;;
  close) close "${2:-}" || exit 1 ;;
  *) echo "사용법: tunnel.sh status | open db|grafana | close db|grafana|all"; exit 2 ;;
esac
