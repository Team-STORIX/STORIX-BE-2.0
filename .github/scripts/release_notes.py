from __future__ import annotations

import json
import os
import re
import subprocess
import sys

RELEASE_COMMIT = re.compile(r'^Release (v\d+\.\d+\.\d+) \(#(\d+)\)')
CHERRY_PICK = re.compile(r'PR #(\d+)의 커밋 cherry-pick \((.+)\)')
TABLE_ROW = re.compile(r'^\| #(\d+) \| (.+?) \|$', re.M)
TYPE_PREFIX = re.compile(r'^(?:Feat|Fix|Refactor|Chore|Docs|Test): ')


def parse_commit(message: str) -> tuple[str, int] | None:
    m = RELEASE_COMMIT.match((message or '').splitlines()[0] if message else '')
    return (m.group(1), int(m.group(2))) if m else None


def included_prs(body: str) -> list[tuple[str, str]]:
    rows = CHERRY_PICK.findall(body or '') or TABLE_ROW.findall(body or '')
    return [(n, TYPE_PREFIX.sub('', title.strip())) for n, title in rows]


def notes(version: str, release_pr: int, prs: list[tuple[str, str]]) -> str:
    lines = '\n'.join(f'- {title} (#{n})' for n, title in prs) or '- (Release PR 본문에서 PR 목록을 찾지 못함)'
    included = ', '.join(f'#{n}' for n, _ in prs) or '-'
    return f'## STORIX {version} 릴리즈\n\n{lines}\n\n포함 PR: {included}\nRelease PR: #{release_pr}\n'


def gh(*args: str) -> subprocess.CompletedProcess:
    return subprocess.run(['gh', *args], capture_output=True, text=True)


def main(message: str, sha: str) -> int:
    parsed = parse_commit(message)
    if not parsed:
        print('Release 커밋이 아님 — 건너뜀')
        return 0
    version, pr = parsed
    repo = os.environ['GITHUB_REPOSITORY']
    if gh('api', f'repos/{repo}/git/ref/tags/{version}').returncode == 0:
        print(f'{version} 태그가 이미 있음 — 건너뜀')
        return 0
    view = gh('pr', 'view', str(pr), '-R', repo, '--json', 'body')
    body = json.loads(view.stdout).get('body', '') if view.returncode == 0 else ''
    text = notes(version, pr, included_prs(body))
    created = subprocess.run(['gh', 'release', 'create', version, '-R', repo, '--target', sha, '--title', version,
                              '--notes-file', '-'], input=text, capture_output=True, text=True)
    if created.returncode:
        print(created.stderr, file=sys.stderr)
        return 1
    print(f'{version} 릴리스를 만들었습니다\n{text}')
    return 0


if __name__ == '__main__':
    sys.exit(main(sys.argv[1], sys.argv[2]))
