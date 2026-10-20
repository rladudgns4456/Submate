# SubMate Git 브랜치 전략

## 1. 기본 구조

2인 팀은 `main + develop + 작업 브랜치`를 사용합니다.

| 브랜치 | 용도 |
|---|---|
| main | 통합 검증을 마친 발표·배포 가능한 버전 |
| develop | 기능 통합 |
| feature/* | 기능 개발 |
| fix/* | 오류 수정 |
| refactor/* | 동작을 유지하는 구조 개선 |
| docs/* | 문서 |
| codex/* | Codex에서 만든 작업 브랜치 |

브랜치는 사람보다 작업 목적을 기준으로 짓습니다. 예: `feature/course-list`, `feature/lesson-management`, `feature/learning-progress`, `fix/refund-amount`, `docs/learning-api`.

## 2. 새 작업 시작

미커밋 변경이 있으면 보존·정리한 뒤 브랜치를 이동합니다.

```powershell
git status
git switch develop
git pull --ff-only origin develop
git switch -c feature/course-list
```

기능 브랜치에서 개발하고 PR로 develop에 통합합니다. main/develop에서 직접 기능 개발하지 않습니다.

## 3. 커밋과 Push

```powershell
git diff
git add <변경한-파일>
git commit -m "feat: 강의 목록과 분야 필터 구현"
git push -u origin feature/course-list
```

`<변경한-파일>`은 실제 경로로 바꿉니다. 무조건 git add .를 실행하기보다 포함 파일을 확인합니다. 비밀번호·토큰·.env·node_modules·빌드 결과·리뷰용 복사본을 커밋하지 않습니다.

커밋 형식: `type: 작업 내용`. type은 feat/fix/refactor/docs/test/chore/style. 사용자 동작이나 UI가 바뀌면 의미에 맞게 feat/fix를 사용하고, 단순 서식 변경은 style로 구분합니다.

## 4. PR

하나의 PR은 하나의 목적을 설명할 수 있게 구성합니다. API·UI·필요한 마이그레이션이 하나의 동작을 완성한다면 함께 포함할 수 있습니다.

PR 본문에는 다음을 적습니다.

- 문제와 변경 후 동작.
- API/DB/공통 스타일/인증 변경 및 다른 담당자 영향.
- 수행한 검사와 결과. 수행하지 않은 검증도 구분.
- UI 변경 시 PC/모바일 화면.
- 신규 제안이나 미확정 정책이 있으면 그 내용.

PR 방향은 `작업 브랜치 → develop`, 안정 버전은 `develop → main`입니다. 팀원 리뷰 후 병합합니다.

## 5. 최신 develop 반영과 충돌

```powershell
git fetch origin
git switch feature/course-list
git merge origin/develop
```

충돌은 담당자와 변경 의도를 확인해 해결합니다. 인증·라우터·공통 CSS·DB 계약을 한쪽 코드로 무조건 덮어쓰지 않습니다. 해결 후 관련 테스트와 화면/API 연결을 다시 확인합니다. 공유 브랜치의 강제 push는 별도 합의 없이 사용하지 않습니다.

## 6. 병합 방식과 브랜치 정리

Squash and merge를 기본 권장합니다. 기존 merge commit 기록을 바꾸지는 않습니다. PR #2/#3이 merge commit으로 남아 있어도 과거 이력을 재작성하지 않습니다.

병합한 PR과 원격 반영을 확인한 뒤 필요 없는 작업 브랜치를 정리합니다. Squash 병합에서는 `git branch -d`가 병합으로 인식하지 못할 수 있으므로, 실패했다고 곧바로 강제 삭제하지 말고 미반영 변경 여부를 확인합니다.

## 7. 공동 파일과 완료 기준

공동 수정 가능 파일: SecurityConfig, application.yaml, build.gradle, package.json/package-lock.json, AppRouter, AuthContext, Layout, tokens.css, 공통 CSS, ERD/API 문서.

공통 토큰을 바꾸면 frontend-guide와 영향을 받는 화면을 함께 확인합니다. 기능 완료는 코드·관련 테스트·연동·문서·PR 리뷰까지 포함합니다. 문서 추가만으로 강의 기능을 완료 처리하지 않습니다.
