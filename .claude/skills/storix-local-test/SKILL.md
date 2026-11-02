---
name: storix-local-test
description: STORIX-BE-2.0을 로컬에서 실제로 띄워 API를 호출해 검증한다. 테스터 계정 pendingId로 로그인해 JWT를 확보하고, 만료되면 refreshToken으로 재발급해가며 계속 호출한다. "로컬로 테스트해봐", "로컬에서 확인해봐", "테스터 계정으로 찔러봐", "실제로 돌려봐" 요청 시 사용.
---

# STORIX 로컬 테스트

무엇을 검증할지는 그때그때 사용자가 정한다. 이 스킬은 **거기까지 가는 준비(환경 확인 → 로그인 → 토큰 유지)를 자동화**한다.

## 절대 규칙

**`local.env`를 읽지 마라.** RDS 자격증명, JWT 시크릿, AWS 키, Slack 봇 토큰, Firebase 서비스 계정, OAuth 시크릿이 들어 있다. 값을 출력하는 것도, `source`해서 프로세스 환경에 올리는 것도 안 된다.

**앱은 `app.sh` 로만 켠다.** `local.env` 는 Gradle 초기화 스크립트(`envfile.init.gradle`)가 bootRun 프로세스에만 넘기고, 셸에 `source` 하거나 내용을 읽지 않는다.

```bash
# 레포 루트에서
.claude/skills/storix-local-test/app.sh start <워크트리 경로>   # 8080 뜰 때까지 대기
.claude/skills/storix-local-test/app.sh status | stop | log [줄수]
```

로그는 `~/.storix-local/app.log`. `log` 는 ERROR · WARN · `>>>` 줄만 보여준다. 8080 이 이미 사용 중이면(사용자가 IntelliJ 로 띄운 경우) 끄지 말고 그대로 쓴다.

로컬에서는 `local.env` 에 `SPRING_DATA_REDIS_HOST=localhost` · `SPRING_ELASTICSEARCH_URIS=http://localhost:9200` 가 있어야 한다. `application.yml` 기본값이 Docker 이름(`storix-redis` · `storix-elasticsearch`)이라 없으면 Redis 잠금을 쓰는 API 가 500 이다. 새 워크트리는 `.git/hooks/post-checkout` 이 메인 checkout 의 `local.env` 와 IntelliJ 실행 설정(`.run/`)을 복사한다.

**`docker-compose.yml`로 띄우지 마라.** 그건 운영용이다. `SPRING_PROFILES_ACTIVE=prod` + ECR 이미지 + **RDS 직결**에 `ddl-auto=update`까지 걸려 있어서, 로컬에서 실행하면 스키마 변경이 운영 DB로 나간다.

경계는 이렇다. 이 셋만 쓰면 시크릿을 만질 일이 없다.

| | 주소 |
|---|---|
| API | `localhost:8080` |
| Redis | `localhost:6379` |
| Elasticsearch | `localhost:9200` |
| MySQL | `localhost:3306` |

## 1. 사전 조건 확인

```bash
(nc -z 127.0.0.1 3306 && echo "MySQL OK")   || echo "MySQL DOWN"
(nc -z 127.0.0.1 6379 && echo "Redis OK")   || echo "Redis DOWN"
(nc -z 127.0.0.1 9200 && echo "ES OK")      || echo "ES DOWN"
(nc -z 127.0.0.1 8080 && echo "App OK")     || echo "App DOWN"
```

### 워크트리에서 띄울 때는 로컬 설정 파일 3개를 복사한다

`application.yml` 은 서버 compose 기준이라 Redis 는 `storix-redis`, ES 는 `http://storix-elasticsearch:9200` 이고 `ddl-auto` 도 dev · prod 프로필에만 있다(2026-10-09 #314 부터 레포 관리). 로컬 주소와 `ddl-auto` 는 git 에 안 올라가는 `application-local.yml` 이 덮는다.

새 워크트리에는 이 파일들이 따라오지 않는다. 없으면 로그인부터 `COMMON_ERROR_009` 가 나고 앱 콘솔에 `UnknownHostException: storix-redis` 가 찍히거나, 엔티티에 추가한 컬럼이 안 생겨 `Unknown column` 이 난다. 원래 폴더에서 내용을 열지 말고 `cp` 로만 옮긴다.

```bash
SRC=<local.env 가 있는 메인 checkout 경로>
DST=<워크트리 경로>
cp $SRC/local.env $DST/local.env
cp $SRC/STORIX-Api/src/main/resources/application-local.yml $DST/STORIX-Api/src/main/resources/
cp $SRC/STORIX-Batch/src/main/resources/application-local.yml $DST/STORIX-Batch/src/main/resources/
```

- **MySQL** — 보통 항상 떠 있다. 죽어 있으면 사용자에게 알린다
- **Redis** — Docker로 돌린다. 아래 명령으로 직접 켜도 된다
- **App** — `DOWN`이면 `app.sh start <워크트리>` 로 켠다

### Redis 켜기

컨테이너 `storix-redis-local`이 있으면 새로 만들지 말고 시작만 한다.

```bash
docker start storix-redis-local
redis-cli -h 127.0.0.1 -p 6379 ping   # PONG 이면 성공
```

없어졌다면 다시 만든다. compose의 `storix-redis`와 이름이 겹치지 않게 `-local` 접미사를 유지한다.

```bash
docker run -d --name storix-redis-local -p 6379:6379 redis:alpine
```

### Elasticsearch 켜기

작품 검색(초성·오타·별칭)을 볼 때만 필요하다. 없어도 앱은 뜨고 검색은 MySQL 로 넘어간다.
컨테이너 `storix-elasticsearch-local`이 있으면 시작만 한다.

```bash
docker start storix-elasticsearch-local
curl -s localhost:9200/_cluster/health | jq -r .status   # green 또는 yellow 면 성공
```

없어졌다면 다시 만든다. 서버 compose 의 `storix-elasticsearch`와 겹치지 않게 `-local` 접미사를 유지한다.
레포에 로컬용 compose 는 두지 않는다.

피드 검색 인덱스가 한국어 형태소 분석기(nori)를 쓰므로 플러그인을 넣은 이미지로 띄운다. 서버 compose 와 같은 이미지 이름이다.

```bash
docker build -t storix-elasticsearch:8.18.8-nori - <<'EOF2'
FROM docker.elastic.co/elasticsearch/elasticsearch:8.18.8
RUN bin/elasticsearch-plugin install --batch analysis-nori
EOF2

docker run -d --name storix-elasticsearch-local -p 9200:9200 \
  -e discovery.type=single-node -e xpack.security.enabled=false \
  -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" -m 1g \
  -v storix-es-local:/usr/share/elasticsearch/data \
  storix-elasticsearch:8.18.8-nori
```

앱이 뜰 때 `works-local`, `feeds-local` alias 가 없으면 알아서 색인한다. ES 를 앱보다 늦게 켜도 5분 동안 다시 시도한다.
바로 다시 색인하려면 관리자 계정으로 `POST /api/v1/admin/search/works/reindex`, `POST /api/v1/admin/search/feeds/reindex`.

## 2. 테스터 로그인

`pendingId`로 바로 로그인한다. 각자 테스터 가입(`/api/v1/auth/tester/signup`) → Slack 승인 뒤 받은 `pendingId` 를 쓴다.
**`pendingId` 는 로그인 키라 코드 · 문서 · PR 에 적지 않는다.** 셸 환경변수로만 둔다.

```bash
export STORIX_PENDING_ID=<내 테스터 pendingId>
```

```bash
curl -s -X POST localhost:8080/api/v1/auth/tester/login \
  -H 'Content-Type: application/json' \
  -d "{\"pendingId\":\"$STORIX_PENDING_ID\"}"
```

응답 (`CustomResponse` 봉투):
```json
{"isSuccess":true,"code":"DEV_AUTH_SUCCESS_003","result":{"accessToken":"eyJ..."}}
```

**`refreshToken`은 body에 안 온다.** `Set-Cookie`로 내려오므로 나중에 재발급하려면 쿠키 jar에 받아둬야 한다.

```bash
AT=$(curl -s -c /tmp/storix_cookies.txt \
      -X POST localhost:8080/api/v1/auth/tester/login \
      -H 'Content-Type: application/json' -d "{\"pendingId\":\"$STORIX_PENDING_ID\"}" \
      | jq -r '.result.accessToken')
```

이후 모든 호출은 `-H "Authorization: Bearer $AT"`.

푸시 알림까지 검증하려면 로컬 DB 에 내 기기 정보가 등록돼 있어야 한다. 기기의 FCM 토큰은 [fcm-token-grabber](https://github.com/Immmii/fcm-token-grabber) 로 뽑아 `/api/v1/devices/register` 로 넣는다.

### 관리자 계정

`/api/v1/admin/**` 은 `hasRole("ADMIN")` 이라 테스터 계정으로는 403이다. 어드민 API를 찌를 때는 관리자 계정으로 로그인한다.
계정은 팀 내부 채널에서 받고, 테스터와 마찬가지로 환경변수로만 둔다.

```bash
ADMIN_AT=$(curl -s -c /tmp/storix_admin_cookies.txt \
      -X POST localhost:8080/api/v1/auth/admin/login \
      -H 'Content-Type: application/json' \
      -d "{\"email\":\"$STORIX_ADMIN_EMAIL\",\"password\":\"$STORIX_ADMIN_PASSWORD\"}" \
      | jq -r '.result.accessToken')
```

테스터와 마찬가지로 `refreshToken` 은 `Set-Cookie` 로 내려온다. 쿠키 jar를 테스터용과 분리해야 서로 덮어쓰지 않는다.

## 3. 토큰 만료 대응

응답에 `TOKEN_ERROR_003`(401, "토큰이 만료되었습니다")이 오면 재발급한다. 두 가지 방식을 다 받는다.

```bash
# 로그인 때 받아둔 쿠키로 (테스터 로그인은 이쪽)
curl -s -b /tmp/storix_cookies.txt -c /tmp/storix_cookies.txt \
  -X POST localhost:8080/api/v1/auth/tokens/refresh

# refreshToken 문자열을 들고 있으면 body로도 된다 (네이티브 방식)
curl -s -X POST localhost:8080/api/v1/auth/tokens/refresh \
  -H 'Content-Type: application/json' -d "{\"refreshToken\":\"$RT\"}"
```

응답의 `result.accessToken`으로 갈아끼우고 실패했던 요청을 재시도한다. 리프레시도 만료됐으면 2번(테스터 로그인)부터 다시 한다.

재발급은 조용히 처리하고, 사용자에게는 원래 검증하려던 결과를 보고한다.

## 4. 자주 쓰는 엔드포인트

| 목적 | 메서드 | 경로 |
|---|---|---|
| 오늘의 피드 | GET | `/api/v1/home/feeds/today` |
| 독자 게시글 목록 | GET | `/api/v1/feed/reader/board` |
| 게시글 단건 | GET | `/api/v1/feed/reader/board/{boardId}` |
| 토큰 재발급 | POST | `/api/v1/auth/tokens/refresh` |
| 테스터 로그인 | POST | `/api/v1/auth/tester/login` |

전체 목록은 `mcp__storix__swagger_list_endpoints`로 확인한다.

## 5. 결과 검증

API 응답만 보지 말고 **저장소 상태까지 확인**한다. 캐시·스냅샷 계열 기능은 응답이 맞아도 Redis에 안 들어가 있을 수 있다.

```bash
redis-cli -h 127.0.0.1 -p 6379 keys 'feed:*'
redis-cli -h 127.0.0.1 -p 6379 get 'feed:today:20260811'
redis-cli -h 127.0.0.1 -p 6379 ttl 'feed:today:20260811'
```

키 프리픽스는 `STORIX-Common/src/main/java/com/storix/common/utils/RedisKeyStatic.java`에 전부 정의돼 있다.

MySQL 접속이 필요하면 계정을 사용자에게 요청한다. `local.env`에서 꺼내 쓰지 않는다.

## 주의할 점

- **`ddl-auto: update`가 로컬 DB에 걸린다.** 엔티티에 `@Index`나 컬럼을 추가한 상태로 앱을 띄우면 로컬 스키마가 실제로 바뀐다. 의도한 것이면 좋은 검증이고, 아니면 미리 알린다
- **컬럼 삭제는 반영되지 않는다.** `update`는 DROP을 안 한다. 엔티티에서 필드를 지워도 컬럼은 남는다
- **스케줄러는 STORIX-Batch 모듈에 있다.** API만 띄우면 배치가 안 돈다. 배치 동작을 봐야 하면 사용자에게 알리고, 가능하면 배치가 채우는 상태를 직접 만들어(예: Redis 키 주입) 조회 경로만 검증한다
- **끝나면 정리한다.** 내가 만든 테스트 데이터는 API로 지우고, 남긴 Redis 키도 지우고, `docker stop storix-redis-local`으로 Redis를, 켰다면 `docker stop storix-elasticsearch-local`로 ES를 내린다. 앱은 내가 `app.sh` 로 켰으면 `app.sh stop`, 사용자가 띄웠으면 건드리지 않는다

## 테스트 데이터 만들기

로컬 DB에 최근 데이터가 없어 조회가 비는 경우가 많다. DB 자격증명 없이 API로 만든다.

```bash
# 독자 게시글 — files 생략하면 이미지 없이 등록된다
curl -s -X POST localhost:8080/api/v1/plus/reader/board \
  -H "Authorization: Bearer $AT" -H 'Content-Type: application/json' \
  -d '{"isWorksSelected":false,"isSpoiler":false,"content":"[테스트] ..."}'

# 등록 응답에 boardId가 없다. 목록에서 찾는다
curl -s "localhost:8080/api/v1/feed/reader/board?page=0&size=10" -H "Authorization: Bearer $AT" \
  | jq -r '.result.content[] | "\(.board.boardId) \(.board.content)"'

curl -s -X POST   localhost:8080/api/v1/feed/reader/board/{id}/like  -H "Authorization: Bearer $AT"
curl -s -X POST   localhost:8080/api/v1/feed/reader/board/{id}/reply -H "Authorization: Bearer $AT" \
                  -H 'Content-Type: application/json' -d '{"comment":"..."}'
curl -s -X DELETE localhost:8080/api/v1/feed/reader/board/{id}      -H "Authorization: Bearer $AT"
```

좋아요는 유저당 1회 토글이라 하트 수를 늘리려면 테스터 계정 여러 개를 번갈아 쓴다. 내용에 `[테스트]` 접두어를 붙여두면 정리할 때 구분된다.
