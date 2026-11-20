# 도메인 경계는 기준선으로 시작

- 날짜: 2026-11-20
- 상태: 확정

## 결정

- Spring Modulith 1.4 로 `com.storix.domain.domains` 아래 도메인 23개의 의존을 읽는다
- 허용 의존은 `agents/domains.yaml` 의 `dependsOn` 에 적고, 처음 값은 그날 코드의 의존 121개를 그대로 옮긴다
- 새 의존 · 사라진 의존 · 미등록 도메인이 생기면 `DomainBoundaryArchitectureTest` 가 실패한다
- 영향 범위 문서 `agents/domain-impact.md` 는 테스트가 만들고, CI 에서는 최신이 아니면 실패한다

## 이유

- 도메인끼리 엔티티를 직접 import 하는 곳이 170군데, 양방향 순환이 32쌍이라 Modulith 기본 검증(`verify()`)을 켜면 첫 빌드부터 실패한다
- 지금 구조를 한 번에 고치는 대신 더 나빠지지 않게 막고, 줄일 때마다 기준선을 같이 줄인다
- 의존 지도를 손으로 그리면 코드와 어긋나서 테스트가 코드에서 직접 만든다

## 다른 선택지

| 선택지 | 안 고른 이유 |
| --- | --- |
| Modulith `verify()` 를 그대로 켜기 | 지금 코드로는 통과할 수 없다 |
| ArchUnit 만 쓰기 | 도메인 캔버스 · PlantUML 자동 생성을 직접 만들어야 한다 |
| Spring Boot 4 + Modulith 2 | 이 레포는 Boot 3.5 라 올리는 건 별도 작업이다 |

## 범위 밖

- Api 모듈의 UseCase 는 여러 도메인을 조합하는 자리라 검사하지 않는다
- 엔티티 직접 참조를 ID 참조로 바꾸는 건 아직 정하지 않았다
