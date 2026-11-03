---
name: storix-tunnel
description: STORIX 운영 DB · Grafana(모니터링) SSM 터널을 백그라운드로 열고 닫는다. BE 레포의 storix-db-tunnel.sh · storix-grafana-tunnel.sh 를 대신 띄운다. "그라파나 켜줘", "모니터링 터널 켜줘", "DB 터널 열어줘", "터널 상태", "터널 닫아줘" 요청 시 사용.
---

# STORIX 터널

BE 레포 루트의 터널 스크립트(`storix-db-tunnel.sh` · `storix-grafana-tunnel.sh`)를 백그라운드로 띄운다.
두 스크립트는 레포에 올리지 않는다(`.gitignore`). 팀 내부 채널에서 받아 레포 루트에 둔다.
스크립트를 고치지 않고 그대로 실행하므로, 대상 인스턴스 · 포트가 바뀌면 그 스크립트만 고치면 된다.

| 이름 | 로컬 | 스크립트 |
|---|---|---|
| `grafana` | `http://localhost:13000` | `storix-grafana-tunnel.sh` |
| `db` | `127.0.0.1:13306` | `storix-db-tunnel.sh` (운영 RDS) |

## 명령

```bash
# 레포 루트에서
.claude/skills/storix-tunnel/tunnel.sh status          # 열림 / 닫힘
.claude/skills/storix-tunnel/tunnel.sh open grafana    # 이미 열려 있으면 그대로 둔다
.claude/skills/storix-tunnel/tunnel.sh close grafana   # db | grafana | all
```

- 터널은 새 세션으로 떠서 터미널 · Claude 세션이 끝나도 남는다. 닫을 때까지 유지된다
- pid · 로그는 `~/.storix-tunnel/`. 열리지 않으면 로그 끝 5줄을 보여준다
- `close` 는 터미널에서 스크립트로 직접 연 터널도 포트 번호로 찾아 닫는다

## 규칙

- **`db` 는 운영 DB 로 가는 길이다.** 사용자가 명시적으로 DB 터널을 열라고 한 경우에만 연다. 다 쓰면 닫는다
- 크롤러 staging DB 도 같은 RDS 에 있어 크롤러의 `cli.py stage ...` 를 돌릴 때 이 터널이 필요하다
- DB 자격증명은 이 스킬이 다루지 않는다. 필요하면 사용자에게 받는다. Parameter Store 를 복호화해 꺼내지 않는다
- 포트가 이미 다른 프로그램에 잡혀 있으면 `lsof -i :13306` · `lsof -i :13000` 로 확인하고 사용자에게 알린다

## 왜 이렇게 끄나

`aws ssm start-session` 은 `session-manager-plugin` 을 자식으로 띄운다. aws 프로세스만 끄면 플러그인이 남아
포트를 계속 잡는다(2026-10-09 크롤러 작업 때 남은 터널로 로컬 테스트가 운영 DB 에 붙은 적이 있다).
그래서 프로세스 그룹째 끄고, 마지막으로 `session-manager-plugin.*"<포트>"` 를 한 번 더 찾아 끈다.

## 준비물

- `aws` CLI, `session-manager-plugin`, 터널 대상에 대한 SSM 권한이 있는 AWS 프로필
- 기본은 이 스킬이 들어 있는 레포 루트. 다른 checkout 의 스크립트를 쓰려면 `STORIX_BE_DIR` 로 지정
