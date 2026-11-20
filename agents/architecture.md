# 아키텍처

## 모듈

```
STORIX-Common → STORIX-Domain → STORIX-Infrastructure → STORIX-Api
                                                      → STORIX-Batch
                              → STORIX-Internal ─────→ STORIX-Api
```

| 모듈 | 담는 것 |
| --- | --- |
| STORIX-Api | 앱 API. `domain/<도메인>/` 에 Controller · UseCase, Flyway 마이그레이션 |
| STORIX-Internal | 크롤러용 내부 API. 작품 적재 · 병합 · 검수 |
| STORIX-Batch | 스케줄러. 웹 서버 없이 뜬다 |
| STORIX-Infrastructure | Security · Redis · WebSocket · FCM · 소셜 로그인 · 포트원 · S3 · Slack |
| STORIX-Domain | `domains/<도메인>/` 에 entity · repository · adaptor · service · dto · exception |
| STORIX-Common | 공통 응답 · 에러 코드 · 예외 · `@UseCase` 같은 어노테이션 · Redis 키 |

## 계층

```
Controller → UseCase → Service → Adaptor → Repository · Redis · ES
```

| 규칙 | 이유 | 강제 |
| --- | --- | --- |
| Service · Helper 는 Repository 를 직접 쓰지 않고 Adaptor 를 거친다 | 조회 실패 처리와 캐시를 한 곳에 둔다 | `ConventionArchitectureTest` |
| Service 는 다른 Service 를 부르지 않는다. 여러 Service 조합은 UseCase 에서 | 트랜잭션 경계가 꼬이지 않게 | `ConventionArchitectureTest` |
| Adaptor 에 필요한 조회 · 저장이 없으면 Adaptor 에 메서드를 추가한다 | Service 가 Repository 를 우회하지 않게 | 리뷰 |
| UseCase 는 얇게 둔다. private 메서드를 두지 않고 매핑은 DTO 정적 팩토리로 | UseCase 는 흐름만 보이게 | `ConventionArchitectureTest` |

## 트랜잭션

| 규칙 | 강제 |
| --- | --- |
| 트랜잭션은 Service 에 건다. UseCase 에는 `@Transactional` 을 달지 않는다 | `ConventionArchitectureTest` |
| 트랜잭션 안에서 Redis · ES · 외부 API 를 부르지 않는다 | `TransactionBoundaryArchitectureTest` |
| 스케줄러 · `@Async` 메서드에 `@Transactional` 을 달지 않는다. 작업 단위로 나눈다 | `ConventionArchitectureTest`, `BatchTransactionArchitectureTest` |
| private 메서드에 `@Transactional` 을 달지 않는다. 프록시를 타지 않는다 | `ConventionArchitectureTest` |

- `open-in-view: false` 라 응답을 만들기 전에 DTO 로 다 바꿔 둔다. 지연 로딩 필드를 트랜잭션 밖에서 건드리면 `LazyInitializationException`
- DB 는 메모리 1GB 짜리 RDS 하나를 dev · prod 가 같이 쓴다. 긴 작업이나 스트리밍 동안 커넥션을 잡지 않는다

## 도메인 경계

- 도메인 하나 = `com.storix.domain.domains` 바로 아래 패키지 하나. Spring Modulith 로 읽는다
- Api 모듈의 UseCase 는 여러 도메인을 조합하는 자리라 경계 검사 대상이 아니다
- 허용한 도메인 의존은 [domains.yaml](domains.yaml) 의 `dependsOn` 에 적는다. `DomainBoundaryArchitectureTest` 가 코드와 비교한다

| 상황 | 테스트 결과 | 할 일 |
| --- | --- | --- |
| 코드에 새 도메인 패키지가 생김 | 실패 | `domains.yaml` 에 설명과 `dependsOn` 을 적어 등록 |
| `dependsOn` 에 없는 의존이 생김 | 실패 | 먼저 UseCase 조합이나 ID 참조로 피할 수 있는지 본다. 꼭 필요하면 `dependsOn` 에 추가하고 PR 에 이유를 쓴다 |
| 코드에서 의존이 사라짐 | 실패 | `dependsOn` 에서 지운다. 기준선은 줄어드는 방향으로만 |
| 코드 없이 `domains.yaml` 에만 등록 | 통과 | 기획 중인 도메인. 영향 범위 계산에만 쓴다 |

- 처음 `dependsOn` 은 2026-11-20 코드의 의존 121개를 그대로 옮긴 기준선이다. 순환도 32쌍 들어 있다. 새로 늘리지 않는 게 목표다
- 도메인별로 "이걸 바꾸면 어디까지 영향 가나"는 [domain-impact.md](domain-impact.md) 에 있다. 기획할 때 영향 도메인을 여기서 확인한다
- 다른 도메인 엔티티를 새로 참조해야 하면 엔티티 대신 ID 를 들고 조회는 그 도메인의 Adaptor 로 한다
