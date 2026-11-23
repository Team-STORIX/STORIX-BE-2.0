# 코드 컨벤션

## 메서드 · 클래스

- 한 곳에서만 쓰는 로직은 private 메서드로 빼지 않고 호출하는 곳에 둔다. 두 곳 이상에서 쓸 때만 뺀다
- 입력 검증 · 변환 · 결과 생성은 DTO 정적 메서드(`from`, `of`)로, Redis 잠금 같은 기술 코드는 Helper 로 뺀다
- Service 안에 record · 중첩 클래스를 두지 않는다. 결과 묶음은 그 도메인 `dto` 패키지에 둔다
- 필드가 많고 같은 타입이 섞인 DTO 는 `@Builder` 로 만든다. 위치 인자가 바뀌어도 컴파일이 통과하기 때문이다
- record 는 컴포넌트를 한 줄에 하나씩 쓰고 `) {` 는 따로 줄에 둔다. 정적 팩토리에서 `new` 할 때도 인자를 한 줄에 하나씩

## 예외

- JDK 예외(`IllegalArgumentException` 등)를 직접 던지지 않는다. `ErrorCode` 와 `STORIXCodeException` 싱글톤을 만든다
- 조회 결과가 없으면 Adaptor 에서 `orElseThrow(() -> XxxException.EXCEPTION)`. `Optional` 을 Service 로 넘기지 않는다
- 상세 메시지가 필요하면 새 예외 클래스 대신 `new STORIXDynamicException(ErrorCode.X, "상세", null)`

## JPA

- projection 은 인터페이스 대신 record 생성자 표현식을 쓴다: `SELECT new com.storix.domain.domains.<도메인>.dto.Xxx(...)`
- COUNT 결과는 `Long` 필드로 받는다

## 주석

- 값을 더할 때만 단다. 이름을 반복하는 요약 주석은 달지 않는다
- 클래스 선언 위, `private static final` 상수, `@Configuration` 의 우회 코드에는 달지 않는다. 배경은 PR 에 쓴다
- 짧은 명사형으로 쓴다: `// 작품 검색 재색인`, `// 판정 스킵`. "~한다." 같은 설명 문장은 쓰지 않는다
- 부연은 중간점(`·`)으로 늘어놓지 않고 괄호로 단다: `// 크롤러 카탈로그 + AI 전용 규칙 (19금 연령가, 챗봇 설정 작품 유형)`. 억지 번역어는 피한다
- 단계가 여러 개인 메서드는 `// 1. … // 2. …` 로 번호를 단다
- Controller 는 주석 대신 `@Operation(summary, description)`, API 에 나가는 DTO 필드는 `@Schema(description)`

## 로그

- 메시지는 `>>> [도메인] 상황` 으로 짧게. 대시 뒤 부연이나 괄호 설명을 붙이지 않는다
- 식별자 같은 부가 정보는 메시지가 아니라 KV 로: `log.atError().addKeyValue("worksId", id).log(">>> [Favorite] 관심작품 works 정보 없음")`
- 데이터 정합성 위반은 ERROR, 예상할 수 있는 누락은 WARN
