---
name: storix-release
description: STORIX-BE-2.0 배포 프로세스. 기존 Release PR 이후에 머지된 PR들의 커밋을 cherry-pick해 새 release/v2.6.x 브랜치를 만들고 main 대상 Release PR을 생성한다. 사용자가 "릴리스", "release PR", "배포 PR" 등을 요청할 때 사용.
---

# STORIX-BE-2.0 Release 배포 프로세스

## 컨벤션

- **저장소:** `Team-STORIX/STORIX-BE-2.0`
- **릴리스 브랜치 네이밍:** `release/v2.6.x` (점 없음, x는 직전 릴리스 +1)
  - 현재 시리즈는 `v2.6.x`. 시리즈가 바뀌면 사용자에게 확인.
- **베이스 브랜치:** `main`
- **머지 방식:** **squash merge** (항상)
- **PR 제목:** `Release v2.6.x`

## 호출 시 입력

사용자는 보통 PR 번호 목록만 준다 (예: "PR #35, #37, #38"). 번호를 안 주면:
- 직전 release PR 머지 이후 main에 머지되지 않은 PR들을 후보로 제시하고 확인받는다.

## 절차

### 1. 사전 조사 (병렬 실행)

```bash
# 가장 최근 release PR로 직전 버전 확인 (다음 버전 계산용)
gh pr list --state all --search "Release" --limit 5
```

각 대상 PR의 커밋 목록을 가져온다 (병렬):
```bash
gh pr view <num> --json number,title,commits
```

`commits[].oid`를 시간순으로 모두 수집. **머지 커밋은 자동 제외** (PR view의 commits 필드는 squash 전 원본 커밋만 보여줌).

### 2. 사용자 확인

- 다음 버전 번호 (직전 +1)
- cherry-pick 대상 커밋 목록 (시간순, oid + 헤드라인)
- 브랜치명 `release/v2.6.x`

세 가지를 한 번에 보여주고 진행 확인. 브랜치 네이밍 표기(점 유무 등) 모호하면 반드시 확인.

### 3. 브랜치 생성 + cherry-pick

```bash
git fetch origin
git checkout -b release/v2.6.<x> origin/main
git cherry-pick <oid1> <oid2> ... <oidN>
```

- 시간순(authoredDate 오름차순)으로 한꺼번에 cherry-pick.
- 충돌 시 즉시 멈추고 사용자에게 보고. 임의로 해결하지 말 것.

### 4. Push

```bash
git push -u origin release/v2.6.<x>
```

### 5. Release PR 생성

base=`main`, head=`release/v2.6.<x>`, title=`Release v2.6.<x>`, **assignee=`@me`**(실행한 사람), **label=`:globe_with_meridians: chore`** (저장소 라벨 이름에 이모지 prefix 포함).

본문 템플릿 (PR #34 형식 그대로):

```markdown
## 💡 PR 작업 내용
- [ ] 기능 관련 작업
- [ ] 버그 수정
- [ ] 코드 개선 및 수정
- [ ] 문서 작성
- [x] 배포
- [ ] 브랜치
- [ ] 기타 (아래에 자세한 내용 기입해 주세요)

## 📋 세부 작업 내용
> - [x] PR #<num>의 커밋 cherry-pick (<요약>)
> - [x] PR #<num>의 커밋 cherry-pick (<요약>)
> - [x] release/v2.6.<x> 구성

## 📸 작업 화면 (스크린샷)

## 💬 리뷰 요구사항(선택)

## ⚠️ PR하기 전에 확인해 주세요.
- [x] 로컬테스트를 진행하셨나요?
- [x] 머지할 브랜치를 확인하셨나요?
- [x] 관련 label을 선택하셨나요?

## ⭐ 관련 이슈 번호
- #12

## 🖇️ 기타
```

`gh pr create` 호출 시 본문은 HEREDOC으로 전달. assignee/label 플래그도 같이:

```bash
gh pr create --base main --head release/v2.6.<x> \
  --title "Release v2.6.<x>" \
  --assignee @me \
  --label ":globe_with_meridians: chore" \
  --body "$(cat <<'EOF'
... 본문 ...
EOF
)"
```

### 6. 사용자에게 PR URL 반환

`gh pr create` 출력의 URL을 그대로 보고.

## 주의사항

- **머지는 절대 수행하지 않는다.** 사용자가 직접 GitHub UI에서 squash merge한다.
- 강제 푸시(`-f`)나 `git reset --hard` 같은 파괴적 명령 금지.
- cherry-pick 충돌은 임의 해결하지 말고 사용자에게 즉시 알린다.
- 시리즈 번호(`v2.6.x` → `v2.7.x` 등) 변경은 반드시 사용자 확인 후.
- `gh pr view`의 `commits` 필드는 squash 전 원본 커밋만 포함하므로 머지 커밋 필터링이 자동으로 됨.

## 과거 이력 (참고)

- v2.6.14 (PR #375) — PR #374 cherry-pick
- v2.6.13 (PR #372) — PR #371 cherry-pick
- v2.4.0 (PR #141), v2.4.1 (PR #143) — v2.4 시리즈
- v2.1.6 (PR #39) — 첫 적용 사례: PR #35, #37, #38 cherry-pick
