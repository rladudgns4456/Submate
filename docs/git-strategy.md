## 1. 기본 전략

SubMate는 **2인 팀 프로젝트**이므로 복잡한 Git Flow 대신 `main + develop + 기능 브랜치` 구조를 사용합니다.

```
main
└── develop
    ├── feature/...
    ├── fix/...
    ├── refactor/...
    └── docs/...
```

| 브랜치 | 용도 |
| --- | --- |
| `main` | 최종 배포 및 발표 가능한 안정 버전 |
| `develop` | 개발 내용을 통합하는 브랜치 |
| `feature/*` | 기능 개발 |
| `fix/*` | 버그 수정 |
| `refactor/*` | 기능 변화 없는 코드 개선 |
| `docs/*` | README, API 명세 등 문서 수정 |

---

## 2. 브랜치 흐름

```
main
  ↑
develop
  ↑
feature/*
```

기본 작업 순서는 다음과 같습니다.

```
# 1. develop 최신화
git switch develop
git pull origin develop

# 2. 작업 브랜치 생성
git switch -c feature/subscription-create

# 3. 기능 개발
git add .
git commit -m "feat: 구독 생성 기능 구현"

# 4. 원격 저장소에 push
git push origin feature/subscription-create

# 5. GitHub에서 Pull Request 생성
feature/subscription-create → develop

# 6. 코드 확인 후 merge
```

**`main`과 `develop`에는 직접 작업하지 않습니다.**

---

# 3. 브랜치 네이밍 규칙

## 기능 개발

```
feature/기능명
```

예시:

```
feature/signup
feature/login
feature/product-list
feature/subscription-create
feature/payment
feature/subscription-cancel
feature/refund
feature/admin-product
feature/admin-subscription
```

## 버그 수정

```
fix/문제명
```

예시:

```
fix/login-token
fix/payment-validation
fix/subscription-status
```

## 리팩터링

```
refactor/대상
```

예시:

```
refactor/subscription-service
refactor/payment-service
```

## 문서

```
docs/문서명
```

예시:

```
docs/readme
docs/api-spec
docs/erd
```

---

# 4. SubMate 담당자별 브랜치 예시

### 김현빈 — 회원 / 상품

```
feature/signup
feature/login
feature/jwt
feature/member
feature/product
feature/category
feature/admin-member
feature/admin-product
```

### 김영훈 — 구독 / 결제

```
feature/subscription
feature/payment
feature/subscription-cancel
feature/subscription-expire
feature/subscription-scheduler
feature/refund
feature/admin-subscription
feature/admin-payment
```

### 공동 작업

```
feature/board
feature/admin-dashboard
docs/api-spec
docs/erd
```

담당자 이름을 브랜치에 넣는 방식은 사용하지 않습니다.

```
❌ feature/younghoon-payment
❌ hyunbin-product

⭕ feature/payment
⭕ feature/product
```

Git 기록으로 작업자를 확인할 수 있으므로 **브랜치는 사람보다 기능을 기준으로 구분**합니다.

---

# 5. 브랜치 크기 기준

하나의 브랜치에 너무 많은 기능을 넣지 않습니다.

예를 들어 구독 전체를 한 번에 구현하는

```
feature/subscription
```

브랜치 하나에서 생성, 조회, 해지, 만료, 스케줄러까지 전부 작업하기보다는 기능이 커지면 다음처럼 나눕니다.

```
feature/subscription-create
feature/subscription-list
feature/subscription-cancel
feature/subscription-expire
feature/subscription-scheduler
```

**기준은 `하나의 PR에서 하나의 목적을 설명할 수 있는가`입니다.**

---

# 6. Commit Message 규칙

```
type: 작업 내용
```

| Type | 의미 | 예시 |
| --- | --- | --- |
| `feat` | 기능 추가 | `feat: 구독 생성 API 구현` |
| `fix` | 버그 수정 | `fix: 구독 만료일 계산 오류 수정` |
| `refactor` | 리팩터링 | `refactor: 결제 서비스 로직 분리` |
| `docs` | 문서 수정 | `docs: API 명세 업데이트` |
| `test` | 테스트 | `test: 구독 서비스 테스트 추가` |
| `chore` | 설정/기타 | `chore: application 설정 수정` |
| `style` | 코드 스타일 | `style: 불필요한 import 제거` |

### 권장

```
feat: 로그인 API 구현
feat: 상품 카테고리 필터 구현
feat: 구독 생성 로직 구현
feat: 가상 결제 처리 구현
fix: CANCELLED 구독 만료 처리 오류 수정
refactor: SubscriptionService 상태 변경 로직 분리
```

### 지양

```
❌ 수정
❌ 작업
❌ 기능 추가
❌ test
❌ ㅇㅇ
❌ 최종
❌ 진짜최종
```

---

# 7. Pull Request 규칙

모든 기능 브랜치는 **PR을 통해 `develop`에 병합**합니다.

```
feature/* ── PR ──→ develop
fix/*     ── PR ──→ develop
```

PR 제목 예시:

```
[FEAT] 구독 생성 기능 구현
[FEAT] 가상 결제 기능 구현
[FIX] 구독 만료 처리 오류 수정
[REFACTOR] 구독 상태 변경 로직 개선
```

### PR 내용

```
## 작업 내용
- 구독 생성 API 구현
- 시작일/종료일 계산
- 최초 상태 ACTIVE 설정

## 확인 사항
- [x] 정상 구독 생성
- [x] 종료일 계산 확인
- [x] 중복 구독 검증

## 관련 기능
- Subscription
- Payment
```

---

# 8. Merge 규칙

### 기능 개발

```
feature/*
    ↓
   PR
    ↓
develop
```

### 최종 안정 버전

```
develop
   ↓
  PR
   ↓
 main
```

`main`에는 **정상 동작이 확인된 코드만 병합**합니다.

특히 다음은 금지합니다.

```
❌ feature → main 직접 merge
❌ main에서 기능 개발
❌ develop에서 기능 개발
❌ 상대방 브랜치에 임의 push
❌ 테스트하지 않은 코드 main merge
```

---

# 9. Merge 방식

SubMate에서는 기능 브랜치를 `develop`에 병합할 때 **Squash and merge를 권장**합니다.

예를 들어 작업 중 커밋이

```
feat: 구독 엔티티 추가
fix: 오타 수정
fix: 다시 수정
test
fix: 진짜 수정
```

처럼 쌓였더라도 최종적으로

```
feat: 구독 생성 기능 구현
```

하나의 커밋으로 `develop`에 들어가게 할 수 있습니다.

프로젝트 이력이 깔끔해지고 2인 프로젝트에서 관리하기 편합니다.

---

# 10. 작업 시작 전 필수 절차

매 작업 시작 전에 반드시 `develop`을 최신 상태로 맞춥니다.

```
git switch develop
git pull origin develop
git switch -c feature/기능명
```

이미 작업 중인 브랜치에서 `develop`의 변경사항이 필요한 경우:

```
git switch develop
git pull origin develop

git switch feature/payment
git merge develop
```

충돌이 발생하면 **충돌 내용을 확인한 뒤 해결하고 테스트**합니다.

---

# 11. 작업 완료 후

PR이 `develop`에 병합되었다면 사용한 기능 브랜치는 삭제합니다.

```
git branch -d feature/payment
```

원격 브랜치도 필요 없으면 삭제합니다.

```
git push origin --delete feature/payment
```

완료된 브랜치를 계속 쌓아두지 않습니다.

---

# 12. 충돌 방지 규칙

2인 프로젝트에서는 다음 규칙을 지킵니다.

1. 작업 시작 전에 `develop`을 `pull`
2. 같은 파일을 동시에 대규모 수정하지 않기
3. 공통 파일 수정 전 팀원에게 공유
4. 한 브랜치에서 여러 기능을 동시에 개발하지 않기
5. PR 전에 최신 `develop`과 충돌 여부 확인
6. 충돌 해결 후 반드시 실행 및 테스트
7. 상대방 코드를 임의로 삭제하지 않기

특히 다음 파일은 두 사람이 모두 수정할 가능성이 높으므로 주의합니다.

```
application.yml
build.gradle
package.json
router 설정
SecurityConfig
공통 Layout
공통 CSS
Entity 연관관계
```

---

# 13. 전체 개발 흐름

```
                 ┌─ feature/member
                 ├─ feature/product
                 ├─ feature/subscription
                 ├─ feature/payment
                 ├─ feature/refund
                 │
                 ↓
               develop
                 │
          통합 테스트 / 수정
                 │
                 ↓
               main
                 │
          최종 발표 / 배포
```

---

# 14. SubMate 최종 규칙

> **브랜치는 사람 기준이 아니라 기능 기준으로 생성한다.**
>

> **모든 개발은 `develop`에서 분기한다.**
>

> **`main`, `develop`에 직접 기능을 작성하지 않는다.**
>

> **기능 하나당 하나의 브랜치를 원칙으로 한다.**
>

> **기능 완료 후 PR을 생성하고 `develop`에 병합한다.**
>

> **PR 병합은 `Squash and merge`를 기본으로 한다.**
>

> **작업 시작 전 항상 `develop`을 최신화한다.**
>

> **`main`에는 통합 테스트가 완료된 안정 버전만 병합한다.**
>
