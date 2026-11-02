#!/bin/bash
# STORIX 로컬 API 서버를 지정한 워크트리에서 백그라운드로 켜고 끈다. local.env 는 Gradle 이 읽고 셸에는 올리지 않는다
# 사용: app.sh start <워크트리 경로> | stop | status | log [줄수]
set -u
STATE=~/.storix-local; mkdir -p "$STATE"
PIDF="$STATE/app.pid"; LOG="$STATE/app.log"; DIRF="$STATE/app.dir"
INIT="$(cd "$(dirname "$0")" && pwd)/envfile.init.gradle"

listening() { lsof -nP -iTCP:8080 -sTCP:LISTEN -t 2>/dev/null | head -1; }

case "${1:-}" in
  start)
    DIR="$(cd "${2:?워크트리 경로 필요}" && pwd)"
    [ -f "$DIR/local.env" ] || { echo "local.env 없음: $DIR"; exit 1; }
    if [ -n "$(listening)" ]; then echo "8080 이미 사용 중 (pid $(listening)). 먼저 stop"; exit 1; fi
    : > "$LOG"; echo "$DIR" > "$DIRF"
    (cd "$DIR" && nohup ./gradlew :STORIX-Api:bootRun -I "$INIT" --console=plain > "$LOG" 2>&1 & echo $! > "$PIDF")
    for i in $(seq 1 90); do
      if [ -n "$(listening)" ]; then echo "켜짐 $DIR (8080, ${i}x2초)"; exit 0; fi
      if ! kill -0 "$(cat "$PIDF")" 2>/dev/null; then echo "기동 실패. 로그 끝:"; grep -E "ERROR|Exception|Caused by|FAILED" "$LOG" | tail -15; exit 1; fi
      sleep 2
    done
    echo "180초 안에 안 뜸. 로그 끝:"; grep -E "ERROR|Exception|Caused by" "$LOG" | tail -15; exit 1 ;;
  stop)
    P="$(listening)"; [ -n "$P" ] && kill "$P"
    [ -f "$PIDF" ] && pkill -P "$(cat "$PIDF")" 2>/dev/null; [ -f "$PIDF" ] && kill "$(cat "$PIDF")" 2>/dev/null
    for i in $(seq 1 15); do [ -z "$(listening)" ] && { echo "꺼짐"; rm -f "$PIDF"; exit 0; }; sleep 1; done
    echo "8080 아직 사용 중 (pid $(listening))"; exit 1 ;;
  status)
    P="$(listening)"; if [ -n "$P" ]; then echo "켜짐 pid $P $(cat "$DIRF" 2>/dev/null)"; else echo "꺼짐"; fi ;;
  log)
    grep -E "ERROR|WARN|Exception|Caused by|>>>" "$LOG" | tail -"${2:-30}" ;;
  *) echo "사용: $0 start <워크트리> | stop | status | log [줄수]"; exit 1 ;;
esac
