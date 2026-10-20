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

## 8. 담당별 브랜치·작업 크기

| 담당 | 기존 작업 예 | 학습 확장 예 |
|---|---|---|
| 김현빈 | feature/member-login, feature/product-list, feature/category | feature/course-list, feature/lesson-management |
| 김영훈 | feature/subscription-create, feature/payment, feature/refund | feature/learning-access, feature/learning-progress |
| 공동 | feature/board, feature/admin-dashboard | feature/course-player-integration, docs/learning-erd |

학습 확장 배분은 제안입니다. 사람 이름 대신 기능·문제를 사용하고 소문자·하이픈으로 표기합니다. Codex가 만드는 브랜치는 codex/ 목적명을 사용할 수 있습니다.

구독 전체나 강의 전체를 무조건 한 PR에 넣지 않습니다. 목록·상세·관리·권한·진도처럼 독립 검토 가능한 목적을 기준으로 나눕니다. 반대로 같은 동작에 필요한 DTO·API·UI·검증을 기계적으로 분리해 중간 버전을 깨뜨리지 않습니다.

## 9. PR 작성 템플릿

```markdown
## 변경 목적과 동작
- 사용자가 어떤 조건에서 무엇을 할 수 있게 되는지:

## 구현 범위
- 담당 도메인/화면:
- API 변경:
- DB/마이그레이션:
- 공통 스타일·인증 영향:

## 검증
- 실행한 명령·환경·결과:
- 정상/오류/권한·상태 경계:
- PC/모바일 화면:
- 아직 수행하지 않은 검증:

## 검토 필요
- 미확정 정책·다른 담당자 영향:
```

제목 예: `feat: 강의 목록과 분야 필터 제공`, `fix: 환불 후 차시 접근 차단`, `docs: 학습 진도 API 계약 추가`. 다른 접두사 형식이 저장소 템플릿에 있다면 팀 규칙과 맞춥니다.

## 10. 리뷰·Merge 점검

- [ ] 기능이 요구사항·API·DB 계약과 일치
- [ ] 기존 거래 흐름·타인 데이터·관리자 권한 회귀 확인
- [ ] 관련 테스트·빌드·린트 결과 명시
- [ ] UI 공통 토큰·반응형·오류 상태 확인
- [ ] 비밀값·디버그 출력·임시 파일 미포함
- [ ] 필요한 마이그레이션과 문서 동반
- [ ] 최신 develop과 충돌 여부 확인
- [ ] 다른 담당자 영향 공유·리뷰 반영

기능 브랜치에서 main으로 직접 병합하지 않습니다. main으로의 PR은 develop 통합 검증 이후 생성합니다. 테스트 기록 없이 발표 가능한 버전으로 표시하지 않습니다.

## 11. 충돌 해결 절차

1. 미커밋 변경을 확인하고 보존합니다.
2. fetch 후 자신의 브랜치에서 origin/develop을 merge합니다.
3. 충돌 파일의 두 변경 의도를 읽고 담당자와 계약을 맞춥니다.
4. 충돌 표시를 제거하고 해당 파일을 선택적으로 stage합니다.
5. 관련 자동 검사와 수동 연동 확인을 수행합니다.
6. merge 커밋을 완료하고 본인 브랜치에 push합니다.

merge 도중 취소가 필요하면 자신이 시작한 merge인지 확인하고 `git merge --abort`를 사용할 수 있습니다. 상대 코드를 보존하지 않은 강제 reset이나 공유 브랜치 이력 재작성으로 충돌을 숨기지 않습니다.

## 12. 병합 뒤 브랜치 정리 예시

PR이 병합되고 필요한 변경이 원격에 포함되었는지 확인한 뒤 실행합니다.

```powershell
git switch develop
git pull --ff-only origin develop
git branch -d feature/course-list
```

원격 브랜치가 남아 있고 더 이상 필요 없으며 해당 브랜치 소유자가 확인했다면:

```powershell
git push origin --delete feature/course-list
```

Squash 이후 -d가 거절될 수 있습니다. 이때 삭제를 강행하기 전에 PR diff·남은 커밋과 미반영 작업을 확인합니다. 강제 삭제는 기본 절차로 제공하지 않습니다.

## 13. 변경 공유 형식

API: 경로/Method → 이유 → Request/Response → 오류/권한 → 영향 페이지.
DB: 테이블/컬럼/제약 → 기존 데이터 영향 → 적용·복구 순서 → ERD.
스타일: 변경 토큰/공통 클래스 → 영향 화면 → PC/모바일 확인.

작은 변경도 공통 계약을 바꾸면 공유합니다. 같은 파일을 동시에 크게 바꿀 예정이면 먼저 범위를 나눕니다.

## 14. 전체 흐름과 최종 규칙

```text
origin/develop 최신화 → 목적별 브랜치 → 개발·검증·문서
→ commit/push → 작업 브랜치 → develop PR
→ 팀원 리뷰·충돌/회귀 확인 → merge
→ 통합 E2E → develop → main PR → 안정 버전
```

작업 전 변경 보존, 기능 중심 브랜치, 공통 계약 공유, 증거가 있는 검증, PR을 통한 통합을 지킵니다. 문서의 브랜치 예시는 명령 사용법이며 실제 브랜치를 새로 만들었다는 기록이 아닙니다.
