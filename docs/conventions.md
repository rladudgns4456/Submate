> SubMate 프로젝트의 코드 품질과 협업 효율을 유지하기 위한 공통 개발 규칙입니다.
>
>
> 팀원 모두 아래 규칙을 기준으로 개발하며, 새로운 규칙이 필요한 경우 협의 후 문서에 반영합니다.
>

---

## 1. 기본 개발 원칙

- 기능 구현 전에 담당 영역과 API 명세를 확인합니다.
- 한 번에 여러 기능을 수정하지 않고 **기능 단위로 개발**합니다.
- 중복 코드는 가능한 한 공통 함수 또는 컴포넌트로 분리합니다.
- 사용하지 않는 코드, import, 주석은 제거합니다.
- 테스트용 `console.log`, 임시 데이터, 주석 처리된 코드는 커밋 전에 정리합니다.
- 비밀번호, JWT Secret, DB 계정 등 민감 정보는 코드에 직접 작성하지 않습니다.
- API, DB 구조 등 팀원에게 영향을 주는 변경은 임의로 진행하지 않고 먼저 공유합니다.

---

## 2. 네이밍 컨벤션

### Java / Spring Boot

| 구분 | 규칙 | 예시 |
| --- | --- | --- |
| Class | PascalCase | `SubscriptionService` |
| Method | camelCase | `createSubscription()` |
| Variable | camelCase | `subscriptionStatus` |
| Constant | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT` |
| Package | 소문자 | `subscription`, `payment` |
| Entity | 단수형 | `Subscription`, `Payment` |
| DTO | 역할 명시 | `SubscriptionRequest`, `SubscriptionResponse` |
| Repository | Entity + Repository | `SubscriptionRepository` |
| Service | Entity + Service | `SubscriptionService` |
| Controller | Entity + Controller | `SubscriptionController` |

메서드명은 기능을 알 수 있도록 작성합니다.

```
createSubscription()
cancelSubscription()
expireSubscription()
findSubscriptionById()
```

`data`, `value`, `temp`, `test`처럼 의미를 파악하기 어려운 이름은 지양합니다.

---

### React / JavaScript

| 구분 | 규칙 | 예시 |
| --- | --- | --- |
| Component | PascalCase | `SubscriptionCard.jsx` |
| Function | camelCase | `handleCancelSubscription()` |
| Variable | camelCase | `subscriptionList` |
| Constant | UPPER_SNAKE_CASE | `API_BASE_URL` |
| Custom Hook | use 접두사 | `useSubscription()` |
| Event Handler | handle 접두사 | `handleSubmit()` |

Boolean 값은 상태를 명확하게 표현합니다.

```
isLoading
isLoggedIn
isAdmin
hasSubscription
```

---

## 3. Backend 구조 규칙

기본적으로 다음 계층을 기준으로 개발합니다.

```
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
```

### Controller

- HTTP 요청 및 응답 처리
- Request DTO 검증
- Service 호출
- 비즈니스 로직을 직접 작성하지 않음

### Service

- 핵심 비즈니스 로직 처리
- 상태 변경 및 검증
- 트랜잭션 처리
- 여러 Repository 간 작업 조정

### Repository

- 데이터베이스 접근 담당
- 조회, 저장, 수정, 삭제 처리
- 비즈니스 로직을 작성하지 않음

### DTO

Entity를 API 응답으로 직접 반환하지 않고 DTO를 사용합니다.

```
Request DTO
Client → Controller

Response DTO
Controller → Client
```

---

## 4. Frontend 구조 규칙

페이지와 재사용 컴포넌트를 구분합니다.

```
pages/
components/
api/
hooks/
utils/
constants/
```

### 역할

- `pages` : 라우팅 단위 화면
- `components` : 재사용 가능한 UI
- `api` : 서버 API 요청
- `hooks` : 공통 상태 및 로직
- `utils` : 공통 함수
- `constants` : 상수 및 고정 값

페이지 내부에 API 요청 로직을 반복해서 작성하지 않고 `api` 영역으로 분리합니다.

```
// 권장
subscriptionApi.cancel(subscriptionId);

// 지양
axios.patch(`/api/subscriptions/${subscriptionId}/cancel`);
```

---

## 5. API 규칙

RESTful API 형식을 기준으로 작성합니다.

```
GET    /api/subscriptions
GET    /api/subscriptions/{id}
POST   /api/subscriptions
PATCH  /api/subscriptions/{id}/cancel
DELETE /api/products/{id}
```

### 기본 원칙

- URL에는 동사보다 **리소스명**을 사용합니다.
- 리소스명은 복수형을 사용합니다.
- HTTP Method로 행위를 구분합니다.
- 기존 API 명세서와 실제 구현을 일치시킵니다.
- API 변경 시 프론트엔드 담당자에게 공유하고 명세서를 수정합니다.

---

## 6. HTTP 상태 코드

| 상태 코드 | 사용 기준 |
| --- | --- |
| `200 OK` | 조회 및 수정 성공 |
| `201 Created` | 리소스 생성 성공 |
| `204 No Content` | 응답 데이터가 필요 없는 성공 |
| `400 Bad Request` | 잘못된 요청 |
| `401 Unauthorized` | 인증 필요 또는 인증 실패 |
| `403 Forbidden` | 접근 권한 없음 |
| `404 Not Found` | 리소스 없음 |
| `409 Conflict` | 현재 상태와 충돌하는 요청 |
| `500 Internal Server Error` | 서버 내부 오류 |

---

## 7. Git 작업 규칙

작업 시작 전 최신 `develop` 브랜치를 기준으로 작업합니다.

```
git checkout develop
git pull origin develop

git checkout -b feature/subscription
```

기능 개발 완료 후 본인의 브랜치를 `develop`에 병합합니다.

### 브랜치 예시

```
main
develop

feature/member
feature/product
feature/subscription
feature/payment

fix/login-error
fix/payment-validation
```

세부 브랜치 운영 방식은 **브랜치 전략 문서**를 따릅니다.

---

## 8. Commit Message 규칙

커밋 메시지는 다음 형식을 사용합니다.

```
type: 작업 내용
```

| Type | 의미 |
| --- | --- |
| `feat` | 새로운 기능 |
| `fix` | 버그 수정 |
| `refactor` | 코드 리팩토링 |
| `style` | 포맷 및 스타일 수정 |
| `docs` | 문서 수정 |
| `test` | 테스트 코드 |
| `chore` | 설정 및 기타 작업 |

### 예시

```
feat: 구독 신청 API 구현
feat: 결제 화면 구현
fix: 구독 해지 상태 변경 오류 수정
refactor: 결제 서비스 로직 분리
docs: API 명세 수정
chore: 환경 변수 설정 추가
```

한 커밋에는 가능한 한 **하나의 작업 목적**만 포함합니다.

---

## 9. 주석 규칙

코드 자체로 의도가 명확한 경우 불필요한 주석을 작성하지 않습니다.

### 지양

```
// 구독을 조회한다.
Subscription subscription = subscriptionRepository.findById(id);
```

### 필요한 경우

복잡한 비즈니스 규칙이나 구현 이유를 설명합니다.

```
// CANCELLED 상태에서도 종료일까지 서비스 이용이 가능하므로
// 즉시 EXPIRED로 변경하지 않는다.
```

**무엇을 하는 코드인지보다 왜 이렇게 구현했는지를 설명하는 주석을 우선합니다.**

---

## 10. 구독 상태 관리 규칙

SubMate의 구독 상태는 다음 3개만 사용합니다.

```
ACTIVE
CANCELLED
EXPIRED
```

### 상태 전이

```
결제 성공
   ↓
ACTIVE
   ↓ 해지 신청
CANCELLED
   ↓ 종료일 도달
EXPIRED
```

### 핵심 규칙

- 결제 성공 후 구독은 `ACTIVE` 상태가 됩니다.
- 사용자가 해지하면 즉시 `EXPIRED`로 변경하지 않습니다.
- 해지 신청 시 `CANCELLED`로 변경합니다.
- `CANCELLED` 상태에서도 기존 종료일까지 이용할 수 있습니다.
- 종료일이 지나면 Scheduler가 `EXPIRED`로 변경합니다.
- 이미 `EXPIRED`인 구독은 다시 해지할 수 없습니다.
- 상태 변경 로직은 Controller가 아닌 Service에서 처리합니다.

---

## 11. DB 규칙

### 테이블명

`snake_case`를 사용합니다.

```
users
products
subscriptions
payments
refunds
boards
```

### 컬럼명

```
user_id
product_id
subscription_id
created_at
updated_at
start_date
end_date
```

### 기본 원칙

- PK는 `{entity}_id` 형식을 사용합니다.
- FK 관계를 명확하게 설정합니다.
- 날짜/시간 컬럼의 네이밍 방식을 통일합니다.
- 상태값은 프로젝트에서 정의한 Enum을 기준으로 관리합니다.
- DB 구조를 변경하면 **ERD도 함께 수정**합니다.

---

## 12. 예외 처리 규칙

비즈니스 예외는 상황을 구분할 수 있도록 처리합니다.

예시:

```
존재하지 않는 회원
존재하지 않는 상품
존재하지 않는 구독
이미 해지된 구독
만료된 구독
결제되지 않은 구독
권한이 없는 요청
```

Controller마다 개별적으로 예외를 처리하기보다 공통 예외 처리 방식을 사용합니다.

```
GlobalExceptionHandler
        ↓
ErrorResponse
        ↓
Client
```

에러 응답 형식도 프로젝트 전체에서 통일합니다.

```
{
  "status": 404,
  "message": "구독 정보를 찾을 수 없습니다."
}
```

---

## 13. 보안 규칙

- 비밀번호는 평문으로 저장하지 않습니다.
- Spring Security + BCrypt를 사용합니다.
- 인증은 JWT 기반으로 처리합니다.
- 관리자 API는 `ADMIN` 권한을 검증합니다.
- 일반 사용자는 다른 사용자의 정보를 수정하거나 조회할 수 없도록 검증합니다.
- JWT Secret, DB 비밀번호 등은 환경 변수로 관리합니다.
- `.env`, `application-secret.yml` 등 민감 정보 파일은 Git에 커밋하지 않습니다.
- 클라이언트에서 전달한 사용자 ID나 가격 정보를 그대로 신뢰하지 않습니다.
- 결제 금액 등 중요한 값은 서버에서 다시 검증합니다.

---

## 14. 코드 리뷰 및 Merge 규칙

Merge 전 다음 항목을 확인합니다.

- 기능이 정상적으로 동작하는가
- 기존 기능에 영향을 주지 않는가
- API 명세와 구현이 일치하는가
- 불필요한 코드가 남아 있지 않은가
- `console.log` 및 테스트 코드가 제거되었는가
- 민감 정보가 포함되지 않았는가
- DB 변경 시 ERD가 수정되었는가
- API 변경 시 API 명세가 수정되었는가
- 충돌이 발생하지 않는지 확인했는가

팀원의 담당 영역에 영향을 주는 변경은 **Merge 전에 공유**합니다.

---

## 15. 팀 협업 규칙

### 김현빈 담당 영역

```
회원
상품
카테고리
인증
관리자 회원 관리
관리자 상품 관리
```

### 김영훈 담당 영역

```
구독
결제
환불
구독 상태 관리
Scheduler
관리자 구독 관리
관리자 결제 관리
```

### 공동 영역

```
ERD
API 명세
JWT / Spring Security
게시판
관리자 대시보드
통합 테스트
Git 충돌 해결
프로젝트 문서
발표 자료
```

상대방의 담당 영역을 수정해야 하는 경우 먼저 공유한 뒤 작업합니다.

---

## 16. Definition of Done

기능 하나를 **완료**로 판단하려면 다음 조건을 충족해야 합니다.

```
□ 요구사항에 맞게 기능 구현 완료
□ 정상 동작 확인
□ 예외 상황 처리
□ Backend ↔ Frontend 연동 확인
□ 불필요한 코드 제거
□ Git Commit 완료
□ API 변경 시 API 명세 반영
□ DB 변경 시 ERD 반영
□ 팀원에게 영향을 주는 변경사항 공유
```

단순히 코드 작성을 끝낸 상태가 아니라 **연동·검증·문서 반영까지 완료된 상태**를 기능 완료로 봅니다.

---

## 📌 핵심 원칙

> **담당 영역을 명확하게 나누되, API·DB·인증처럼 서로 연결되는 부분은 반드시 공유한다.**
>

> **Controller는 요청/응답, Service는 비즈니스 로직, Repository는 DB 접근이라는 역할을 지킨다.**
>

> **구독 상태 변경과 결제 검증처럼 중요한 규칙은 Frontend가 아니라 Backend에서 보장한다.**
>

> **코드 변경으로 API나 DB 설계가 달라졌다면 관련 문서도 함께 수정한다.**
>
