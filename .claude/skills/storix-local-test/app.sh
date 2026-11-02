#!/bin/bash
# STORIX 로컬 API 서버를 지정한 워크트리에서 백그라운드로 켜고 끈다. local.env 는 Gradle 이 읽고 셸에는 올리지 않는다
# 사용: app.sh start <워크트리 경로> | stop | status | log [줄수]
set -u
STATE=~/.storix-local; mkdir -p "$STATE"
PIDF="$STATE/app.pid"; LOG="$STATE/app.log"; DIRF="$STATE/app.dir"
# 이 스크립트가 띄운 앱이 8080 을 잡은 pid. 이 값과 같을 때만 끄고 경로를 보여준다
APPF="$STATE/app.listen.pid"
INIT="$(cd "$(dirname "$0")" && pwd)/envfile.init.gradle"

listening() { lsof -nP -iTCP:8080 -sTCP:LISTEN -t 2>/dev/null | head -1; }
managed() { local p; p="$(listening)"; [ -n "$p" ] && [ -f "$APPF" ] && [ "$p" = "$(cat "$APPF")" ]; }
kill_gradle() {
  [ -f "$PIDF" ] || return 0
  pkill -P "$(cat "$PIDF")" 2>/dev/null; kill "$(cat "$PIDF")" 2>/dev/null; rm -f "$PIDF"
}

case "${1:-}" in
  start)
    DIR="$(cd "${2:?워크트리 경로 필요}" && pwd)"
    [ -f "$DIR/local.env" ] || { echo "local.env 없음: $DIR"; exit 1; }
    if [ -n "$(listening)" ]; then echo "8080 이미 사용 중 (pid $(listening)). 그대로 쓰거나, 이 스크립트로 켠 거면 stop"; exit 1; fi
    rm -f "$APPF"; : > "$LOG"; echo "$DIR" > "$DIRF"
    (cd "$DIR" && nohup ./gradlew :STORIX-Api:bootRun -I "$INIT" --console=plain > "$LOG" 2>&1 & echo $! > "$PIDF")
    for i in $(seq 1 90); do
      P="$(listening)"
      if [ -n "$P" ]; then echo "$P" > "$APPF"; echo "켜짐 $DIR (8080, ${i}x2초)"; exit 0; fi
      if ! kill -0 "$(cat "$PIDF")" 2>/dev/null; then echo "기동 실패. 로그 끝:"; grep -E "ERROR|Exception|Caused by|FAILED" "$LOG" | tail -15; rm -f "$PIDF"; exit 1; fi
      sleep 2
    done
    kill_gradle
    echo "180초 안에 안 떠서 빌드를 멈췄다. 로그 끝:"; grep -E "ERROR|Exception|Caused by" "$LOG" | tail -15; exit 1 ;;
  stop)
    P="$(listening)"
    if [ -n "$P" ] && ! managed; then echo "8080 은 이 스크립트가 켠 앱이 아니다 (pid $P). 끄지 않음"; exit 1; fi
    [ -n "$P" ] && kill "$P"
    kill_gradle
    for i in $(seq 1 15); do [ -z "$(listening)" ] && { echo "꺼짐"; rm -f "$APPF"; exit 0; }; sleep 1; done
    echo "8080 아직 사용 중 (pid $(listening))"; exit 1 ;;
  status)
    P="$(listening)"
    if [ -z "$P" ]; then echo "꺼짐"
    elif managed; then echo "켜짐 pid $P $(cat "$DIRF" 2>/dev/null)"
    else echo "켜짐 pid $P (이 스크립트로 켠 앱 아님, 워크트리 모름)"; fi ;;
  log)
    grep -E "ERROR|WARN|Exception|Caused by|>>>" "$LOG" | tail -"${2:-30}" ;;
  *) echo "사용: $0 start <워크트리> | stop | status | log [줄수]"; exit 1 ;;
esac
