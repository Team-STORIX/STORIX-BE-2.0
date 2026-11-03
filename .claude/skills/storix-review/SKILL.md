---
name: storix-review
description: STORIX 백엔드(Spring/JPA/Redis/S3, 헥사고날) 변경분을 작성과 분리된 2차 적대적 패스로 리뷰한다. "기능이 맞나"가 아니라 "어떻게 실패/깨지나" 관점의 고정 렌즈로 부분실패·동시성·리소스수명·상태불변식·프레임워크함정·경로일관성·비밀값 노출을 점검. 커밋/PR 전 자기 코드 점검, "리뷰해줘", "내 코드 점검", "coderabbit처럼 봐줘" 요청 시 사용.
---

# STORIX 백엔드 셀프 리뷰 스킬

CodeRabbit류가 잡는 2차 관점을 코드화한 것. **작성 직후 happy-path 검증(컴파일·기능·테스트)만으로는 못 잡는 실패모드**를 별도 패스로 잡는다.

## 핵심 원칙

1. **작성자 모자를 벗는다.** "내가 뭘 의도했나"가 아니라 "이 코드를 어떻게 깨뜨리나"로 본다. 방금 내가 짠 코드일수록 더 적대적으로.
2. **변경분(diff)만이 아니라 그 변경이 닿는 경로 전체를 본다.** `git diff`로 시작하되, 바뀐 메서드의 호출자/형제 경로/상태전이까지 확인.
3. **각 렌즈를 명시적으로 통과시킨다.** "해당 없음"도 한 줄로 근거를 남긴다 (건너뛰면 그게 곧 놓침).
4. **결과는 판정표로.** 각 발견에 `반영/스킵/보류` + 한 줄 사유 + 최소 수정안. (STORIX 사용자 선호 포맷)

## 리뷰 렌즈 (반드시 전부 통과)

### 1. 리소스 수명 / 사이드이펙트 보상
외부 사이드이펙트(S3 업로드, 파일, 외부 API 호출, 메시지 발행)가 **검증보다 먼저** 일어나거나, **실패/교체 시 정리 안 되면** orphan·leak.
- 트리거 질문: *"이 다음 줄이 예외를 던지면 방금 만든 외부 리소스는 어떻게 되나? 교체(update) 시 옛 리소스는 해제되나?"*
- STORIX 예: `PopupUseCase`가 S3 업로드 후 `service.create()` 검증 실패 → S3 orphan. update 시 옛 이미지 미삭제 → 누적. (보상 삭제 / 성공 후 옛것 삭제)

### 2. 부분 실패 & 시퀀스 일관성
여러 스텝이 **각자 독립 커밋**(`@Transactional` 분리)인데 뒤 스텝 실패가 앞 스텝의 후속처리(캐시 무효화·보상)를 건너뛰게 함.
- 트리거 질문: *"스텝 N이 실패하면 1..N-1은 이미 커밋됐나? 그럼 그들에 필요한 후속(evict 등)은 여전히 도나?"*
- STORIX 예: `EventDisplayScheduler`가 팝업/배너 4스텝 후 일괄 evict → 배너 스텝 실패 시 팝업 캐시 evict 누락. (도메인별 격리 + finally evict)

### 3. 팬아웃/이벤트 처리의 실패 격리
N건 반복 처리(loop, batch, 이벤트 리스너) 중 한 건 예외가 나머지를 중단시킴.
- 트리거 질문: *"한 건이 터지면 나머지도 멈추나?"*
- 특히 `@TransactionalEventListener(AFTER_COMMIT)`, `forEach(publishEvent)`, 배치 루프. → 건별 try/catch 격리 또는 비동기/에러관리 경로.
- STORIX 예: `TitleAcquiredEventListener` 한 칭호 실패 → 같은 커밋 다른 칭호 처리 중단.

### 4. 상태머신 불변식
종료/취소/삭제 같은 **터미널 상태**는 특정 필드를 동결해야 하는데, `update()`가 무조건 덮어써서 불변식 위반.
- 트리거 질문: *"각 터미널 상태(ENDED/CANCELLED/DELETED)에서 바뀌면 안 되는 필드는? update()가 막나?"*
- STORIX 예: `Banner.update`/`Popup.update`가 status만 가드하고 displayStartAt/EndAt는 덮어씀 → ENDED 배너 기간 불일치.

### 5. 프레임워크/라이브러리 시맨틱 함정
애노테이션/설정이 **조용히 no-op 이거나 예상과 다르게** 동작.
- 트리거 질문: *"이 애노테이션이 이 타입/상황에서 정말 작동하나? 전역 설정과 중복/충돌은?"*
- 상시 체크: `@JsonFormat(timezone=)` on `LocalDateTime`(무효 — zoneless), `@Transactional` self-invocation/private(무효), LAZY 직렬화, N+1, `@Transactional(readOnly)` 안 매핑, cascade/orphanRemoval.
- STORIX 예: `AppEventResponse`의 timezone 속성 — 단 전역 `TimeZone.setDefault(Asia/Seoul)`이 있어 출력은 정상(→ 스킵). **전역 설정 먼저 확인하고 판정.**

### 6. 동시성 / 원자성
check-then-act(exists→save, count→insert)에 락/제약이 없으면 동시 요청 레이스.
- 트리거 질문: *"두 요청이 동시에 오면 둘 다 검증 통과하고 둘 다 쓸 수 있나?"*
- STORIX 예: 배너 겹침 count 후 save — 비원자적. (정책이 유동적이면 보류 판단, 확정이면 DB 제약/비관락)

### 7. 데이터 감사/일관성 디테일
네이티브 쿼리·벌크 업데이트가 JPA 감사(`updated_at`, `@PrePersist/@PreUpdate`)를 우회.
- 트리거 질문: *"이 네이티브/벌크가 감사 컬럼·파생 카운터를 일관되게 유지하나?"*
- STORIX 예: `upsertWinner`의 `ON DUPLICATE KEY UPDATE`가 `updated_at` 미갱신.

### 8. 공유 경로 완전성
한 진입 경로에만 규칙을 걸고 형제 경로엔 빠뜨림.
- 트리거 질문: *"이 상태/규칙에 도달하는 모든 경로가 같은 처리를 하나?"* (cancel vs 자연만료, create 검증 vs update 검증)
- STORIX 예: `AppEventService.cancel`은 팝업/배너만 종료하고 당첨자 확정(`finalizeWinners`)은 누락 — 자연만료 경로에도 없음.

### 9. 비밀값 · 노출 범위
코드뿐 아니라 스크립트 · 워크플로 · 스킬 문서에 적힌 명령도 본다. 그대로 따라 치면 비밀값이 새거나 서비스가 밖으로 열린다.
- 트리거 질문: *"이 값이 명령행 · 파일 · 네트워크 중 어디에 남고, 거기서 누가 읽을 수 있나?"*
- 상시 체크
  - **포트 바인딩**: `docker run -p 6379:6379` 처럼 주소 없이 열면 같은 네트워크 모든 기기에 열린다. 인증을 끈 서비스(`xpack.security.enabled=false` 등)면 더 위험. 로컬용은 `-p 127.0.0.1:6379:6379`
  - **명령행 인자 속 비밀값**: `curl -d "{\"pw\":\"$SECRET\"}"` 는 셸이 값을 펼쳐 인자로 넘겨 `ps` 로 보인다. `jq -n '{pw: env.SECRET}' | curl -H 'Content-Type: application/json' --data-binary @-` 처럼 표준 입력으로
  - **문자열로 조립한 JSON**: 값에 `"` · `\` · 줄바꿈이 있으면 깨지거나 다른 값이 된다. `jq` · 직렬화 라이브러리로 만든다
  - **비밀값을 담은 파일**: 토큰 · 쿠키 jar 를 `/tmp` 에 기본 권한으로 두지 않는다. 나만 읽는 폴더(`mkdir -m 700`)에 두고 끝나면 지운다
- STORIX 예: #416 `storix-local-test` 가 Redis · ES 를 모든 네트워크에 열고(ES 는 인증 꺼짐), `pendingId` · 어드민 비밀번호를 `curl -d` 인자로, refreshToken 쿠키를 `/tmp` 에 남김.

## 판정 기준

- **반영**: 유효하고 수정이 작다(단거). 바로 고친다.
- **스킵**: 무효하거나 이미 다른 곳에서 처리됨(전역설정 등). **근거 명시.** 추측 금지 — 코드/설정으로 확인.
- **보류**: 유효하나 크거나(배선·보상 다수) 미구현 의존이거나 정책 유동적. 사유 + "후속" 표시.

## 워크플로우

1. 변경분 확보. 브랜치 전체는 `git diff origin/develop...HEAD`, 커밋 전 작업분은 `git status --short` 로 목록을 먼저 보고 `git diff`(unstaged) · `git diff --cached`(staged) · 새 파일(`??`) 내용까지 읽는다. `git diff` 만 보면 staged 와 새 파일이 빠진다. 바뀐 심볼의 호출자·형제 경로도 읽는다.
2. **렌즈 1~9를 순서대로** 변경분에 적용. 각 렌즈마다 트리거 질문을 실제로 던지고 코드로 답을 확인.
3. 발견마다 **실패 시나리오를 구체적으로** 재현(입력→상태→잘못된 결과)해서 진짜인지 검증. 추측이면 폐기.
4. 판정표 작성: `# | 대상 | 판정 | 사유 | 최소 수정안`.
5. 반영 항목만 고치고 **컴파일 + 관련 테스트로 검증**. 스킵/보류는 사유와 함께 남긴다.

## 수정 시 스타일 (반드시 준수)

- 주석은 [[feedback_comment_convention]] — 최소화, 상수/자명한 것/클래스선언 위 X, `()` 부연·억지번역체 지양, WHY만 짧게.
- UseCase는 [[feedback_usecase_no_private]] — private 헬퍼 금지, 얇게. 다단계는 `// 1./2.` 스텝만.
- 로그 프리픽스 `>>> [도메인]`. 커밋 메시지 `Type: [도메인] 설명`, Co-Authored-By 금지. **커밋 전 사용자 승인**([[feedback_commit_permission]]).

## 한계 인식

이 스킬조차 놓칠 수 있는 것: 비즈니스 요구 오해(스펙 자체가 틀림), 대규모 성능(부하 테스트 필요), 보안 심층(별도 security-review). 그런 건 렌즈로 안 잡히니 별도 검토를 권한다.
