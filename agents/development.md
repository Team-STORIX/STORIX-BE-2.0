# 개발 환경

## 로컬 실행

- 앱은 `.claude/skills/storix-local-test/app.sh` 로만 켠다
  - `local.env` 는 Gradle 초기화 스크립트가 bootRun 프로세스에만 넘긴다. 셸에 올리면 운영 자격증명이 다른 프로세스와 로그로 샌다
  - IntelliJ 는 `.run/` 실행 설정을 쓴다. 이미 8080 에 떠 있으면 끄지 않고 그대로 쓴다
- `local.env` 에 `SPRING_DATA_REDIS_HOST=localhost` · `SPRING_ELASTICSEARCH_URIS=http://localhost:9200` 가 있어야 한다
  - `application.yml` 기본값이 컨테이너 이름이라, 없으면 Redis 잠금을 쓰는 API 가 500 이다
- `docker-compose.yml` 은 운영 서버용이다. 로컬에서 띄우면 운영 DB 에 붙는다
- 테스터 계정 로그인 · 토큰 재발급까지 하는 절차는 `.claude/skills/storix-local-test/SKILL.md`

## 프로필

| 프로필 | 쓰는 곳 | 스키마 |
| --- | --- | --- |
| 지정 안 함 | 개발자 PC | `application.yml` 기본값. Flyway 꺼짐 |
| dev | dev 서버 | Flyway + `ddl-auto: validate` |
| prod | 운영 서버 | Flyway + `ddl-auto: validate` |

- 프로필은 `application.yml` 안에서 정하지 않고 실행할 때 `SPRING_PROFILES_ACTIVE` 로 넘긴다
- 환경 값은 `${VAR}` 로만 쓰고 `${VAR:-기본값}` 처럼 기본값을 두지 않는다. 값이 빠진 걸 기본값이 가려서 엉뚱하게 동작한다
- 서버 비밀값은 AWS Parameter Store 에 있다. 부팅 때 `deploy/render-env.py` 가 `.env` 를 만든다

## DB 마이그레이션

- 스키마 변경은 `STORIX-Api/src/main/resources/db/migration/V{n}__설명.sql` 로 추가한다
- 이미 dev · prod 에 적용된 파일은 고치지 않는다. 바꿀 게 있으면 다음 번호로 새로 만든다
- 엔티티를 바꾸면 같은 PR 에 마이그레이션을 넣는다. 없으면 dev 배포 때 `validate` 에서 앱이 안 뜬다

## 테스트

- `./gradlew test` 가 CI(`.github/workflows/pr-test.yml`)와 같은 명령이다
- 규칙 테스트는 `STORIX-Api/src/test/java/com/storix/api/architecture/` 와 `STORIX-Batch/src/test/java/com/storix/batch/architecture/`
- `DomainBoundaryArchitectureTest` 는 로컬에서 `agents/domain-impact.md` 를 다시 쓰고, CI 에서는 최신이 아니면 실패한다
  - 도메인 의존을 바꿨으면 로컬에서 한 번 돌려 바뀐 `domain-impact.md` 를 같이 커밋한다
- 도메인별 캔버스와 PlantUML 은 `STORIX-Api/build/spring-modulith-docs/` 에 생긴다. 커밋하지 않는다
