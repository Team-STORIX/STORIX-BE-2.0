---
name: storix-pr
description: "STORIX-BE-2.0 코드 작업물을 커밋하고 develop 브랜치 대상 PR을 생성합니다. 커밋 메시지 컨벤션, PR 템플릿, Assignee/Label 설정을 자동으로 처리합니다."
user_invocable: true
---

# STORIX-BE-2.0 PR 생성 스킬

현재 브랜치의 변경사항을 커밋하고 develop 브랜치 대상으로 PR을 생성합니다.

## 실행 순서

### 1. 상태 확인
- `git status`로 변경사항 확인
- `git log --oneline -5`로 최근 커밋 메시지 스타일 확인
- `git branch --show-current`로 현재 브랜치 확인

### 2. 커밋
- 변경된 파일만 선택적으로 `git add` (git add -A 사용 금지)
- .env, credentials 등 민감 파일 절대 포함하지 않을 것
- 커밋 메시지 컨벤션: `Feat:`, `Fix:`, `Refactor:`, `Chore:`, `Docs:`
- Co-Authored-By 포함하지 않을 것

### 3. Push
- `git push origin {현재브랜치}`

### 4. 연결된 이슈 확인
- 브랜치명에서 키워드를 뽑아 검색한다 (예: feed-spoiler → 피드 · 스포일러). `--search` 없이 목록만 받으면 최근 30건만 와서 놓친다
  ```bash
  gh issue list --repo Team-STORIX/STORIX-BE-2.0 --state all --search "<키워드>" --limit 50
  ```
- 못 찾으면 이슈 번호를 사용자에게 묻는다

### 5. PR 생성
- `gh pr create`로 PR 생성
- **base**: develop
- **assignee**: `@me` (PR 을 만드는 사람)
- **label**: 작업 유형에 맞는 라벨 선택 (`:rocket: feat`, `:hammer: refactor`, `:globe_with_meridians: chore`, `:memo: docs`)

### 6. PR에 이슈 연결 (Development)
- PR 생성 후 GitHub API로 이슈와 PR을 연결한다
- `gh api graphql`을 사용하여 PR의 Development 섹션에 이슈를 연결:
```bash
# 1) PR node ID 조회
PR_ID=$(gh pr view {PR번호} --repo Team-STORIX/STORIX-BE-2.0 --json id -q '.id')

# 2) Issue node ID 조회
ISSUE_ID=$(gh api graphql -f query='{ repository(owner:"Team-STORIX", name:"STORIX-BE-2.0") { issue(number:{이슈번호}) { id } } }' -q '.data.repository.issue.id')

# 3) PR body에 "Closes #이슈번호" 키워드가 포함되어 있으면 자동 연결됨
# PR 본문의 "⭐ 관련 이슈 번호" 섹션에 "- Closes #이슈번호" 형식으로 작성
```
- PR 본문의 관련 이슈 번호를 `- Closes #xx` 형식으로 작성하면 GitHub가 자동으로 Development에 연결한다

### 6. PR 본문 템플릿

```markdown
## 💡 PR 작업 내용
- [ ] 기능 관련 작업
- [ ] 버그 수정
- [ ] 코드 개선 및 수정
- [ ] 문서 작성
- [ ] 배포
- [ ] 브랜치
- [ ] 기타 (아래에 자세한 내용 기입해 주세요)

## 📋 세부 작업 내용
### 1. {작업 제목}
**[기존]** {무엇이 문제였는지 한두 문장}

**[변경 후]**
- {바뀐 동작}

### 2. {작업 제목}
...

### 확인
- {추가한 테스트, 전체 테스트 N건 통과, 로컬 API 호출 여부}

## 📸 작업 화면 (스크린샷)

## 💬 리뷰 요구사항(선택)

## ⚠️ PR하기 전에 확인해 주세요.
- [ ] 로컬테스트를 진행하셨나요?
- [x] 머지할 브랜치를 확인하셨나요?
- [x] 관련 label을 선택하셨나요?

## ⭐ 관련 이슈 번호 [#이슈번호]
- Closes #{이슈번호}

## 🖇️ 기타
```

### 세부 작업 내용 작성 규칙
1. **체크박스 형식 사용 금지** — `### 1.`, `### 2.` 넘버링 형식으로 작성
2. **새로운 기능 추가**: 도입 배경/사유를 먼저 설명하고, 구현 내역을 나열
3. **기존 기능 리팩토링**: `[기존]`과 `[변경 후]` 차이점을 명확히 설명
4. **`### 확인`** 은 마지막 섹션으로 두고 실제로 한 검증만 적는다. 안 한 검증은 "아직"이라고 적는다
5. 한 항목에 속성(요청·응답·거절 등)이 여러 개면 표로 정리한다 (#380 참고)
4. **환경변수/시크릿 값은 절대 PR 본문에 포함하지 않을 것** — 키 이름만 나열하고 "값은 팀 내부 채널에서 공유"로 표기

### 주의사항
- PR 본문에 환경변수 실제 값, API Key, Secret 등 민감 정보를 절대 포함하지 않을 것
- 관련 이슈 번호는 `- Closes #xx` 형식으로 작성. `- #xx` 는 Development 연결과 머지 시 자동 종료가 안 된다
- 로컬테스트 칸은 이번 변경으로 로컬 API 를 실제로 호출해 봤을 때만 `[x]`
- PR 작업 내용 체크박스에서 해당하는 항목만 [x]로 체크
