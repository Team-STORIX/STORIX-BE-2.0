# Git · PR

## 흐름

1. 이슈를 만든다
2. 이슈 기준으로 브랜치를 만든다 (`gh issue develop` 로 만들면 이슈에 연결된다)
3. PR 을 `develop` 으로 연다

- 브랜치를 먼저 push 하면 이슈 Development 칸에 연결할 수 없다. 이슈부터 만든다
- 직전 PR 이 아직 머지되지 않았으면 다음 브랜치를 만들지 않고 로컬에서 먼저 작업한다

## 이름

| 대상 | 형식 | 예 |
| --- | --- | --- |
| 이슈 · PR · 커밋 제목 | `Type: [도메인] 무엇을 했는지` 명사형 종결 | `Feat: [알림] 마케팅 야간 발송 정책 추가` |
| Feat · Refactor · Chore 브랜치 | `feature/2.0/SPRINT-N-설명` | `feature/2.0/SPRINT-6-batch-metrics` |
| Fix 브랜치 | `hotfix/2.0/SPRINT-N-설명` | |
| 릴리스 PR | `Release vX.Y.Z` | `Release v2.7.1` |

- Type 은 `Feat` · `Fix` · `Refactor` · `Chore`, 이슈에는 `Bug` 도 쓴다
- "…하기", "…하는 모듈" 처럼 말하듯 끝내지 않고 "…추가", "…정비" 처럼 끝낸다
- 접두사를 줄이지 않는다. `feat/` 가 아니라 `feature/`
- 이슈와 PR 은 보통 같은 제목을 쓴다

## 커밋

- 제목 한 줄만 쓴다. 설명은 PR 본문에
- 제목에 이슈 번호 `(#123)` 를 붙이지 않는다
- 사람 이름을 넣지 않는다
- `local.env` · `.env` · 툴 상태 폴더(`.omc/` 등)는 스테이징하지 않는다

## PR

- `.github/PULL_REQUEST_TEMPLATE.md` 를 쓴다
- 세부 작업은 `### 1.` 번호 제목으로 쓰고, 고친 것은 `[기존]` · `[변경 후]` 로 나눈다
- `### 확인` 에는 실제로 한 검증만 쓴다. 안 한 건 "아직" 으로 쓴다
- 본문에 `Closes #이슈번호` 를 넣어 이슈와 연결한다
- assignee 와 라벨을 붙인다: `:rocket: feat` · `:hammer: refactor` · `:globe_with_meridians: chore` · `:memo: docs` · `🐛 bug`
- 비밀값은 키 이름만 쓴다

## 릴리스

- `main` 에는 직접 머지하지 않는다. 직전 릴리스 이후 `develop` 에 머지된 PR 커밋을 `release/vX.Y.Z` 에 cherry-pick 해서 `main` 으로 PR 을 연다
- 절차는 `.claude/skills/storix-release/SKILL.md`
