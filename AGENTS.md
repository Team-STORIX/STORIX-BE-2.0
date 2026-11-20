# STORIX-BE-2.0

웹툰 · 웹소설 팬덤 앱 STORIX 의 백엔드. Java 17 · Spring Boot 3.5 · Gradle 멀티모듈.
Claude Code · Codex 같은 AI 도구가 같이 읽는 규칙 원본이다. `CLAUDE.md` 는 이 파일을 불러오기만 한다.

## 먼저 지킬 것

- `local.env` 를 열거나 `source` 하지 않는다. 운영 자격증명이 들어 있다
- `docker-compose.yml` 로 로컬 실행하지 않는다. 운영 설정이라 운영 DB 에 붙는다
- 커밋 · 푸시 · PR 은 사람이 요청할 때만 한다. `develop` · `main` 에 직접 push 하지 않는다
- 답변 · 주석 · 로그 · 커밋 · PR 은 한국어로 쓴다
- 규칙 대부분은 ArchUnit 테스트가 강제한다. 테스트가 막으면 테스트를 고치지 말고 코드를 고친다

## 자주 쓰는 명령

| 목적 | 명령 |
| --- | --- |
| 전체 테스트 | `./gradlew test` |
| 계층 · 트랜잭션 · 도메인 경계 테스트 | `./gradlew :STORIX-Api:test --tests 'com.storix.api.architecture.*'` |
| 로컬 서버 실행 | `.claude/skills/storix-local-test/app.sh start` |

## 문서

| 문서 | 내용 |
| --- | --- |
| [agents/development.md](agents/development.md) | 로컬 실행, 프로필, 테스트, DB 마이그레이션 |
| [agents/architecture.md](agents/architecture.md) | 모듈 · 계층 · 트랜잭션 규칙, 도메인 경계 |
| [agents/conventions/code.md](agents/conventions/code.md) | 코드 · 주석 · 로그 · 예외 · DTO |
| [agents/conventions/git.md](agents/conventions/git.md) | 이슈 · 브랜치 · 커밋 · PR · 릴리스 |
| [agents/decisions/](agents/decisions) | 결정 기록. 날짜와 이유를 같이 남긴다 |
| [agents/domains.yaml](agents/domains.yaml) | 도메인 목록과 허용한 도메인 의존 |
| [agents/domain-impact.md](agents/domain-impact.md) | 도메인별 영향 범위. 테스트가 만든다 |

모듈 구조와 기술 스택은 [README](README.md) 에 있다.

## 논의 중

확정 전 항목이다. 정해지면 해당 문서로 옮기고 `agents/decisions/` 에 남긴다.

| 항목 | 지금 상태 |
| --- | --- |
| 다른 도메인 엔티티 직접 참조를 ID 참조로 바꾸기 | 기준선만 고정. 어디부터 줄일지 미정 |
| 이슈 템플릿에 "영향 도메인" 칸 추가 | 미정 |
| 기획의 영향 도메인과 PR 실제 변경 도메인을 비교하는 CI | 아이디어 단계 |
