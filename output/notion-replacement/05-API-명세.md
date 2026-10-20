# SubMate API 명세서

## 1. 상태 구분과 공통 규칙

**구현 API**는 현재 로컬 코드 기준입니다. **계획 API**는 회원/상품 통합 또는 학습 기능을 위한 제안이며 아직 호출 가능한 계약으로 간주하지 않습니다.

- Base URL `/api`, JSON, 요청/응답 필드는 camelCase.
- 실제 인증 목표: `Authorization: Bearer {accessToken}`. 현재 demo 프로필만 Basic 인증으로 검증.
- 서버 인증 이름은 회원 이메일, 권한은 ROLE_USER/ROLE_ADMIN.
- 관리자 API는 인증 권한과 members.role=ADMIN을 확인.
- 회원 ID·금액·구독 권한은 서버에서 결정.
- 타인 거래는 404, 비인증 401, 비관리자 403.
- 업무 오류 400, 중복/상태 충돌 409.
- 날짜 YYYY-MM-DD, 시점 YYYY-MM-DDTHH:mm:ss, Asia/Seoul.
- 목록은 page=0, size=20 기본, size=1~100, 최신 ID 내림차순.

현재 성공 응답은 DTO 자체입니다. `{success,data,message}` 공통 래퍼는 도입되어 있지 않습니다. 변경하려면 Frontend와 함께 이관해야 합니다.

```json
{"status":409,"message":"현재 상태에서는 처리할 수 없습니다."}
```

목록 응답:

```json
{"content":[],"page":0,"size":20,"totalElements":0,"totalPages":0}
```

## 2. 구독·결제·환불 — 구현 API / 김영훈

| Method | Path (/api 뒤) | 권한 | 기능 |
|---|---|---|---|
| POST | /subscriptions | 로그인 | 가상 결제·구독 동시 생성 |
| GET | /subscriptions | 로그인 | 본인 구독 목록 |
| GET | /subscriptions/quote?productId=1 | 로그인 | 신청 전 상품 견적 |
| GET | /subscriptions/{id} | 본인 | 구독 상세 |
| PATCH | /subscriptions/{id}/cancel | 본인 | 해지 |
| GET | /payments | 로그인 | 본인 결제 목록 |
| POST | /refunds | 본인 | 일할 환불 요청 |
| GET | /refunds | 로그인 | 본인 환불 목록 |
| GET | /admin/subscriptions | 관리자 | 전체 구독 |
| GET | /admin/subscriptions/{id} | 관리자 | 구독 상세 |
| PATCH | /admin/subscriptions/{id}/cancel | 관리자 | 구독 해지 |
| GET | /admin/payments | 관리자 | 전체 결제 |
| GET | /admin/refunds | 관리자 | 전체 환불 |
| PATCH | /admin/refunds/{id} | 관리자 | 환불 승인/반려 |

**현재 없는 API:** 별도 POST /payments, GET /payments/{id}, POST /payments/{id}/refunds, PATCH /admin/refunds/{id}/approve 또는 /reject. 구독 결제 흐름을 이 경로들에 연결하지 않습니다.

### 2-1. 구독 신청과 가상 결제

POST /api/subscriptions

```json
{
  "productId": 1,
  "paymentMethod": "CARD",
  "requestKey": "checkout-unique-key",
  "simulateFailure": false
}
```

- productId 양의 정수. paymentMethod CARD/BANK_TRANSFER.
- requestKey 1~64자 영문·숫자·하이픈·밑줄, 회원별 UNIQUE.
- simulateFailure 필수 boolean. true는 400, 구독·결제 행 미생성.
- 신규 성공 201, 동일 키/동일 요청 재전송 200, 키를 다른 요청에 재사용하면 409.
- 유효한 동일 상품 구독이 ACTIVE/CANCELLED로 남아 있으면 409.
- 기간 MONTHLY/YEARLY, 정기 청구는 없음.

응답 예시 — 기존 데모 상품 기준:

```json
{
  "replayed": false,
  "subscription": {
    "subscriptionId": 1,
    "memberId": 1,
    "productId": 1,
    "productName": "SubMate Cinema",
    "status": "ACTIVE",
    "startDate": "2026-09-15",
    "endDate": "2026-10-15",
    "cancelledAt": null,
    "usable": true
  },
  "payment": {
    "paymentId": 1,
    "subscriptionId": 1,
    "amount": 12900.00,
    "paymentMethod": "CARD",
    "status": "COMPLETED",
    "paidAt": "2026-09-15T18:00:00"
  }
}
```

상품명을 학습 이용권으로 변경하는 것은 정식 상품/시드 데이터 작업에서 처리합니다. 문서 예시만 변경해 실제 데이터가 변경된 것으로 표현하지 않습니다.

### 2-2. 견적·조회·해지

견적 DTO는 productId/name/price/billingCycle/status를 반환합니다. 구독 상세와 해지 응답은 위 subscription DTO를 사용합니다. POST가 별도 결제를 먼저 만드는 2단계 흐름은 제공하지 않습니다.

PATCH /api/subscriptions/{id}/cancel은 Body가 없습니다. ACTIVE이며 종료일 전인 경우 CANCELLED로 바꾸고 cancelledAt을 기록합니다. endDate를 유지하며 해지만으로 환불하지 않습니다. 이미 해지·만료되었으면 409입니다.

usable은 상태와 날짜로 계산합니다. 종료일 당일 00:00 KST 이후는 스케줄러가 아직 실행되지 않아도 false입니다.

### 2-3. 환불 요청

POST /api/refunds → 201

```json
{"paymentId":1,"reason":"학습 이용 계획 변경"}
```

사유는 공백 제외 필수, 최대 255자입니다. 정상 결제·구독 잔여 기간·기존 환불 유무를 확인합니다. 만료되었거나 이미 요청한 원결제는 409입니다. CANCELLED도 잔여 기간이 있으면 요청할 수 있습니다.

금액은 `결제금액 × 잔여 일수 ÷ 전체 일수`로 계산하고 원 미만을 버립니다. 종료일은 제외하고 요청 당일은 포함합니다. 예: 12,900원 × 18일 / 28일 = 8,292원.

```json
{
  "refundId": 1,
  "paymentId": 1,
  "amount": 8292.00,
  "reason": "학습 이용 계획 변경",
  "status": "REQUESTED",
  "refundedAt": null,
  "createdAt": "2026-09-15T18:10:00",
  "totalDays": 28,
  "remainingDays": 18
}
```

금액과 일수는 요청 때 저장하고 승인일이 달라져도 유지합니다. 이전 정책의 이력은 일수 필드가 null일 수 있습니다. 환불 요청만으로 수강 권한을 즉시 종료하는 정책은 아닙니다.

### 2-4. 관리자 승인·반려

PATCH /api/admin/refunds/{id}

```json
{"decision":"APPROVE"}
```

반려는 `{"decision":"REJECT"}`입니다. REQUESTED만 처리하며 반복 처리는 409입니다.

- 승인: Refund=COMPLETED, Payment=REFUNDED, 구독 EXPIRED·즉시 이용 종료.
- 반려: Refund=REJECTED, 결제·구독 유지.
- 원결제당 요청 1회이며 반려 후 재요청·실패 환불 재처리는 현재 제공하지 않습니다.

## 3. 회원·상품·카테고리 — 계획 / 김현빈

기존 문서의 경로를 통합 후보로 유지합니다. 실제 담당자 구현과 요청/응답을 대조한 뒤 확정해야 합니다.

| Method | Path (/api 뒤) | 권한 | 목적 |
|---|---|---|---|
| POST | /auth/signup | 공개 | 회원가입 |
| POST | /auth/login | 공개 | 로그인·JWT 발급 |
| GET / PATCH | /members/me | 로그인 | 본인 정보 |
| GET | /products | 공개 | 학습 이용권 목록 |
| GET | /products/{id} | 공개 | 이용권 상세·포함 강의 |
| GET | /categories | 공개 | 학습 분야 |
| GET | /admin/members | 관리자 | 회원 목록 |
| GET | /admin/members/{id} | 관리자 | 회원 상세 |
| GET / POST | /admin/products | 관리자 | 상품 조회·등록 |
| PATCH | /admin/products/{id} | 관리자 | 가격·판매 상태 등 변경 |

상품의 물리 삭제는 거래 FK와 보존 정책을 먼저 확인합니다. 카테고리 CRUD·회원 탈퇴의 정확한 경로/DTO도 담당자와 합의합니다. 미구현 API의 오류 메시지나 토큰 만료 시간을 확정값으로 적지 않습니다.

## 4. 강의·차시 — 신규 API 제안 / 김현빈 배분 제안

| Method | Path (/api 뒤) | 권한 | 목적 |
|---|---|---|---|
| GET | /courses | 공개 | 강의 목록, categoryId/keyword/page/size |
| GET | /courses/{courseId} | 공개 | 소개·목표·커리큘럼·이용권 안내 |
| GET | /courses/{courseId}/lessons/{lessonId} | 로그인+수강권 | 실제 차시 콘텐츠 |
| GET / POST | /admin/courses | 관리자 | 목록·등록 |
| PATCH | /admin/courses/{courseId} | 관리자 | 소개·공개 상태 변경 |
| POST | /admin/courses/{courseId}/lessons | 관리자 | 차시 등록 |
| PATCH | /admin/courses/{courseId}/lessons/{lessonId} | 관리자 | 차시 수정 |
| PUT | /admin/products/{productId}/courses | 관리자 | 포함 강의 구성 변경 |

공개 강의 상세에는 유료 차시 본문·비공개 영상 URL을 포함하지 않습니다. lessonId의 course 소속도 확인합니다. 초안 강의/차시는 일반 사용자에게 노출하지 않습니다. 상품 구성 변경 API는 기존 구독 영향 정책이 확정된 뒤 구현합니다.

## 5. 학습·진도 — 신규 API 제안 / 김영훈 배분 제안

| Method | Path (/api 뒤) | 권한 | 목적 |
|---|---|---|---|
| GET | /learning/courses | 로그인 | 본인 학습 목록·이용 가능 여부·최근 차시 |
| GET | /learning/courses/{courseId}/progress | 본인 | 강의별 진도 |
| PUT | /learning/lessons/{lessonId}/progress | 본인+수강권 | 차시 완료·학습 기록 저장 |

진도 저장 요청 제안:

```json
{"completed":true}
```

클라이언트가 회원 ID나 전체 진도율을 결정하지 않습니다. 중복 요청은 중복 진도 행을 만들지 않도록 하고 완료 취소 허용 여부는 먼저 합의합니다. 최근 접근 시각은 수강 화면 진입/기록 정책에 따라 서버가 관리합니다.

내 학습 목록의 전체 DTO, 이어 학습할 차시 선정 기준, 빈 강의의 진도율, 완료 판정은 팀 합의 후 확정합니다. 조회된 이력이 있다는 사실이 현재 수강 권한을 의미하지 않습니다.

## 6. 공지사항·대시보드 — 후속 제안 / 공동

- GET /api/posts, GET /api/posts/{id}: 공개 공지 조회.
- POST/PATCH/DELETE /api/admin/posts…: 관리자 공지 관리 제안. 기존 일반 회원 글쓰기 계약은 기본 범위에서 제외.
- GET /api/admin/dashboard: 관리자 통계 제안. 회원·상품·강의와 거래 지표를 분리.

금액 집계는 결제·환불 각각의 처리 시점과 상태를 정의해야 합니다. Payment가 REFUNDED로 바뀌었다고 최초 성공 결제 금액을 이중 차감하지 않도록 집계 기준을 합의합니다.

## 7. 통합 검증

실제 JWT, 타인 정보 격리, 관리자 접근, 중복 결제, 날짜 경계, 일할 환불과 상태 일괄 변경을 확인합니다. 신규 학습 API는 구독 없음·해지 잔여 기간·만료·환불·다른 유효 이용권·타인 진도·차시 소속 오류를 포함해 검증합니다.

계획 API는 구현과 검증 후에만 구현 표로 이동합니다.

## 8. 구현 DTO 상세 사전

아래는 Java record와 Controller를 대조한 현재 계약입니다. 응답 예시의 숫자·시각은 설명용이며 실제 데이터와 다를 수 있습니다.

### CheckoutRequest

| 필드 | JSON 타입 | 필수 | 검증 |
|---|---|---|---|
| productId | number(integer) | O | null 불가, 양수 |
| paymentMethod | string | O | CARD 또는 BANK_TRANSFER |
| requestKey | string | O | 공백 불가, 최대 64자, `[A-Za-z0-9_-]+` |
| simulateFailure | boolean | O | null 불가 |

상품 ID나 요청 키가 올바르더라도 서버의 판매 상태·가격·유효 구독 검사를 통과해야 합니다. 실제 카드번호·계좌번호 필드는 없으며 전송하지 않습니다.

### CheckoutResponse

| 필드 | 타입 | 의미 |
|---|---|---|
| replayed | boolean | 동일 결제 요청의 기존 결과 반환 여부 |
| subscription | SubscriptionResponse | 연결된 구독의 현재 상태 |
| payment | PaymentResponse | 연결된 결제의 현재 상태 |

환불 후 같은 정상 요청 키를 재전송하면 최초 성공 당시 스냅샷이 아니라 현재 REFUNDED 상태가 반환될 수 있습니다. 실패한 최초 요청은 행이 없어 동일 키로 정상 재시도할 수 있습니다.

### SubscriptionResponse

| 필드 | 타입 | null | 의미 |
|---|---|---|---|
| subscriptionId | integer | 불가 | 구독 ID |
| memberId | integer | 불가 | 소유 회원 ID |
| productId | integer | 불가 | 상품 ID |
| productName | string | 불가 | 현재 상품명 조회 결과 |
| status | string | 불가 | ACTIVE/CANCELLED/EXPIRED |
| startDate | string(date) | 불가 | 시작일 |
| endDate | string(date) | 불가 | 배타적 종료일 |
| cancelledAt | string(datetime) | 허용 | 해지 신청 시점 |
| usable | boolean | 불가 | 상태가 EXPIRED가 아니며 시작일 이상·종료일 미만인지 |

상품명은 현재 상품에서 읽습니다. 가격·기간과 달리 상품명의 과거 스냅샷을 보존한 구현은 아닙니다.

### ProductReference — 견적 응답

| 필드 | 타입 | 의미 |
|---|---|---|
| productId | integer | 상품 ID |
| name | string | 상품명 |
| price | number | 현재 가격 |
| billingCycle | string | MONTHLY/YEARLY |
| status | string | 상품 판매 상태 |

```json
{"productId":1,"name":"SubMate Cinema","price":12900.00,"billingCycle":"MONTHLY","status":"ACTIVE"}
```

견적은 강의 소개 API를 대신하지 않습니다. 견적 조회 후 가격이 바뀌면 실제 결제는 신청 시 서버가 잠근 상품 가격을 기준으로 처리합니다.

### PaymentResponse

| 필드 | 타입 | null | 의미 |
|---|---|---|---|
| paymentId | integer | 불가 | 결제 ID |
| subscriptionId | integer | 불가 | 연결 구독 |
| amount | number | 불가 | 결제 당시 금액 |
| paymentMethod | string | 불가 | CARD/BANK_TRANSFER |
| status | string | 불가 | 현재 흐름 COMPLETED/REFUNDED |
| paidAt | string(datetime) | 타입상 허용 | 현재 정상 생성 흐름에는 성공 시점 존재 |

PENDING/FAILED는 Entity enum에 있지만 현재 실패 결제 행 저장 흐름은 없습니다. PaymentResponse에는 memberId/requestKey가 노출되지 않습니다.

### RefundRequest / RefundDecisionRequest

| DTO | 필드 | 타입 | 필수·검증 |
|---|---|---|---|
| RefundRequest | paymentId | integer | 필수·양수 |
| RefundRequest | reason | string | 필수·공백 불가·최대 255자 |
| RefundDecisionRequest | decision | string | 필수·APPROVE/REJECT |

reason은 저장 전 양끝 공백을 제거합니다. 클라이언트가 amount/status/memberId를 결정하는 계약은 없습니다.

### RefundResponse

| 필드 | 타입 | null | 의미 |
|---|---|---|---|
| refundId / paymentId | integer | 불가 | 환불·원결제 ID |
| amount | number | 불가 | 요청 시 확정 금액 |
| reason | string | 불가 | 요청 사유 |
| status | string | 불가 | REQUESTED/COMPLETED/REJECTED |
| refundedAt | string(datetime) | 허용 | 승인 완료 시점 |
| createdAt | string(datetime) | 불가 | 요청 시점 |
| totalDays / remainingDays | integer | 허용 | 이전 이력은 null, 신규 요청은 계산값 |

### PageResponse와 오류

| 필드 | 타입 | 의미 |
|---|---|---|
| content | array | 해당 DTO 목록 |
| page | integer | 요청한 0 기준 페이지 |
| size | integer | 요청 페이지 크기 |
| totalElements | integer | 전체 건수 |
| totalPages | integer | 전체 페이지 수 |

빈 목록도 200이며 content=[]입니다. page<0 또는 size가 1~100 밖이면 400입니다. 현재 목록에는 임의 sort 파라미터가 없고 해당 ID 내림차순입니다.

SubscriptionExceptionHandler 적용 패키지에서 업무/검증/DB 충돌 오류는 `{status,message}`를 사용합니다. Security 필터 단계의 인증 실패까지 같은 JSON이 보장된다는 뜻은 아니며 실제 JWT 통합 때 통일 여부를 검증합니다. 예상하지 못한 500 응답을 현재 구현에서 표준화했다고 표시하지 않습니다.

## 9. 구현 엔드포인트별 성공·실패 조건

공통적으로 인증 실패와 관리자 권한 실패가 먼저 발생할 수 있습니다. 아래 표는 주요 업무 오류이며 모든 서버 내부 오류를 열거하지 않습니다.

| API | 성공 | 요청 / 반환 | 주요 실패 |
|---|---|---|---|
| POST /subscriptions | 201, 재전송 200 | CheckoutRequest → CheckoutResponse | 400 입력/가상 실패, 404 상품, 409 판매 중지·가격·중복·키 충돌·동시성 |
| GET /subscriptions | 200 | page,size → Page<SubscriptionResponse> | 400 페이지 |
| GET /subscriptions/quote | 200 | productId → ProductReference | 400 필수/타입, 404 상품, 409 판매 중지 |
| GET /subscriptions/{id} | 200 | Body 없음 → SubscriptionResponse | 400 타입, 404 없음/타인 |
| PATCH /subscriptions/{id}/cancel | 200 | Body 없음 → SubscriptionResponse | 404 없음/타인, 409 이미 해지·종료·충돌 |
| GET /payments | 200 | page,size → Page<PaymentResponse> | 400 페이지 |
| POST /refunds | 201 | RefundRequest → RefundResponse | 400 입력, 404 없음/타인, 409 결제 상태·기존 요청·남은 기간 없음·0원·동시성 |
| GET /refunds | 200 | page,size → Page<RefundResponse> | 400 페이지 |
| GET /admin/subscriptions | 200 | page,size → Page<SubscriptionResponse> | 400 페이지, 403 관리자 |
| GET /admin/subscriptions/{id} | 200 | Body 없음 → SubscriptionResponse | 404 없음, 403 관리자 |
| PATCH /admin/subscriptions/{id}/cancel | 200 | Body 없음 → SubscriptionResponse | 404 없음, 409 상태/기간, 403 관리자 |
| GET /admin/payments | 200 | page,size → Page<PaymentResponse> | 400 페이지, 403 관리자 |
| GET /admin/refunds | 200 | page,size → Page<RefundResponse> | 400 페이지, 403 관리자 |
| PATCH /admin/refunds/{id} | 200 | RefundDecisionRequest → RefundResponse | 400 decision, 404 없음, 409 처리 완료/결제 상태/충돌, 403 관리자 |

현재 관리자 구독 목록의 `status=ACTIVE` 필터는 구현되어 있지 않습니다. page/size 외 필터를 문서만 보고 사용하지 않습니다. 추가 필터는 별도 계획입니다.

## 10. 인증·회원 상세 계약 — 통합 제안

이 절 전체는 미구현/통합 미확인 API의 제안입니다. 값 범위와 DTO는 팀원 구현 확인 후 확정합니다. HTTP 상태도 목표 계약이며 현재 응답 보장이 아닙니다.

### 회원가입 POST /api/auth/signup

공개, 담당 김현빈. 요청:

```json
{"email":"learner@example.com","password":"example-only-password","name":"학습자"}
```

| 필드 | 필수 | 제안 검증 |
|---|---|---|
| email | O | 이메일 형식·최대 255자·중복 검사 |
| password | O | 비밀번호 길이/문자 정책은 인증 담당 확정, 평문 저장 금지 |
| name | O | 공백 제외·최대 50자 |

201 응답 제안 `{"memberId":1}`. 400 입력 오류, 409 이메일 중복. role은 입력받지 않고 서버에서 USER로 정합니다. 비밀번호를 응답하지 않습니다.

### 로그인 POST /api/auth/login

공개, 요청 `{"email":"learner@example.com","password":"example-only-password"}`.

200 응답 제안:

```json
{"accessToken":"JWT_TOKEN","email":"learner@example.com","role":"USER"}
```

email/role은 App 연결에 필요하며 로그인 응답에 포함하거나 /members/me로 조회하는 방식을 합의합니다. 400 입력 오류, 401 계정 검증 실패. 만료 시간·Refresh Token·저장 방식은 인증 담당 합의 사항이며 이 문서가 별도 토큰 API를 확정하지 않습니다.

### 로그아웃

현재 별도 서버 로그아웃 엔드포인트를 확정하지 않습니다. 클라이언트 세션·헤더를 제거하고 로그인 화면으로 이동합니다. 발급된 JWT의 서버 즉시 폐기는 별도 기능입니다.

### MemberResponse — 응답 제안

| 필드 | 타입 | 설명 |
|---|---|---|
| memberId | integer | 회원 ID |
| email | string | 이메일 |
| name | string | 이름 |
| role | string | USER/ADMIN |
| createdAt / updatedAt | datetime, 후자는 null 허용 | 시점 |

GET /api/members/me: 로그인, Body 없음, 200 MemberResponse, 401 비인증, 404 조회 정책은 탈퇴 처리와 함께 합의.

PATCH /api/members/me: 로그인, `{"name":"새 이름"}`, 200 MemberResponse, 400 공백/길이. email·password·role 임의 변경은 이 요청에 허용하지 않는 안을 제안합니다.

GET /api/admin/members: 관리자, page/size, 200 Page<MemberResponse>. GET /api/admin/members/{memberId}: 관리자, 200 MemberResponse, 404 없음.

회원정보 관리자 수정·탈퇴는 원문 R&R의 범위를 유지하되 구체적 경로·삭제/비활성 정책은 추가 확정 대상입니다. 구현 전까지 호출 가능한 API로 표시하지 않습니다.

## 11. 상품·카테고리 상세 계약 — 통합 제안

### ProductResponse

| 필드 | 타입 | 설명 |
|---|---|---|
| productId | integer | 상품 ID |
| categoryId | integer | 상품 분류 ID, 기존 설계 유지 제안 |
| name | string | 이용권 이름 |
| description | string/null | 설명 |
| price | number | 금액 |
| billingCycle | string | MONTHLY/YEARLY |
| status | string | ACTIVE/INACTIVE |
| courses | array | 상세에서 포함 강의 요약, 신규 제안 |

GET /api/products: 공개, categoryId/page/size 제안, 200 Page<ProductResponse>. 공개 목록은 ACTIVE를 기본으로 하는 안을 제안하며 INACTIVE 노출 정책은 합의합니다. GET /api/products/{productId}: 공개, 200 ProductResponse, 404 없음/비공개 정책 대상.

POST /api/admin/products: 관리자, 다음 요청 제안, 201 ProductResponse.

```json
{"name":"개발 기초 월간 이용권","categoryId":1,"description":"개발 기초 강의 모음","price":12900,"billingCycle":"MONTHLY","status":"ACTIVE"}
```

name 필수·최대 100자, categoryId 필수 양수, price 필수·양수·DECIMAL(10,2) 범위, billingCycle/status는 enum을 검증합니다. 400 입력, 404 카테고리, 403 권한.

PATCH /api/admin/products/{productId}: 관리자, 변경할 허용 필드만 전송, 예 `{"price":14900,"status":"INACTIVE"}`, 200 ProductResponse. 이미 생성된 결제 금액과 구독 기간은 변경하지 않습니다.

DELETE /api/admin/products/{productId}: 기존 문서의 삭제 요구는 보존한 계획 항목입니다. 거래/강의 연결이 없는 경우만 삭제할지, 항상 비활성화로 대체할지 합의 후 구현합니다. 물리 삭제 허용 시 204, FK 충돌 시 409를 목표로 하며 현재 구현으로 간주하지 않습니다.

GET /api/admin/products: 관리자, page/size 및 상태 필터 제안, 200 Page<ProductResponse>.

### CategoryResponse / CRUD

응답 제안 `{"categoryId":1,"name":"프로그래밍","createdAt":"2026-10-19T10:00:00"}`.

| Method / Path | 권한 | 요청 | 성공 | 주요 오류 제안 |
|---|---|---|---|---|
| GET /categories | 공개 | 없음 | 200 CategoryResponse[] | 서버 오류 |
| POST /admin/categories | 관리자 | {name} | 201 CategoryResponse | 400 공백/50자, 409 중복 |
| PATCH /admin/categories/{id} | 관리자 | {name} | 200 CategoryResponse | 404 없음, 409 중복 |
| DELETE /admin/categories/{id} | 관리자 | 없음 | 204 | 404 없음, 409 상품/강의 연결 존재 |

위 표의 Path는 /api 뒤입니다. Product.categoryId 기존 설계와 Course.categoryId 확장 설계를 함께 유지하는 안이며 실제 FK 합의가 필요합니다.

## 12. 강의·차시 상세 계약 — 신규 제안

김현빈 배분 제안. 목록/상세의 공개 범위와 차시 본문 접근을 분리합니다.

### CourseSummary / CourseDetail

| 필드 | 타입 | 제안 규칙 |
|---|---|---|
| courseId / categoryId | integer | 필수 |
| title | string | 필수, 최대 200자 |
| description | string | 소개 |
| level | string | BEGINNER/INTERMEDIATE/ADVANCED 제안 |
| status | string | DRAFT/PUBLISHED/ARCHIVED 제안, 공개 응답에 노출 여부 합의 |
| lessonCount | integer | 공개 차시 수, 서버 계산 |
| learningObjectives | string | 상세 학습 목표 |
| lessons | array | 상세 커리큘럼: lessonId/title/position, 본문 제외 |
| products | array | 상세 포함 이용권 요약 |

GET /api/courses: 공개, categoryId/keyword/page/size. keyword 최대 100자 제안. 200 Page<CourseSummary>, 400 페이지/필터, 비공개 강의 제외.

GET /api/courses/{courseId}: 공개, 200 CourseDetail, 404 없음/비공개. 조회만으로 수강권이 생기지 않습니다.

GET /api/courses/{courseId}/lessons/{lessonId}: 로그인+수강권, 200 LessonContent, 401 비인증, 403 수강권 없음, 404 강의/차시 없음·소속 불일치·비공개. 소속과 공개 여부를 권한 판단 전에 확인할지 등 정보 노출 순서는 구현 때 통일합니다.

```json
{"lessonId":11,"courseId":1,"title":"첫 학습","position":1,"content":"학습 본문","videoUrl":null}
```

### 관리자 강의·차시

POST /api/admin/courses 요청 제안:

```json
{"categoryId":1,"title":"웹 개발 기초","description":"웹 구조 입문","learningObjectives":"요청과 응답을 설명한다.","level":"BEGINNER","status":"DRAFT"}
```

201 CourseDetail. GET /api/admin/courses는 page/size/status 제안, 200 Page<CourseSummary>. PATCH /api/admin/courses/{courseId}는 허용 필드 일부 변경, 200 CourseDetail. 제목·분류·enum 검증, 400/404/409 조건 적용.

POST /api/admin/courses/{courseId}/lessons 요청 제안:

```json
{"title":"첫 학습","position":1,"content":"학습 본문","videoUrl":null,"status":"DRAFT"}
```

title 필수·최대 200자, position 양수·강의 내 중복 불가, content는 TEXT 범위, videoUrl은 선택·최대 2048자·허용 URL 검증 제안. 201 LessonContent. PATCH /api/admin/courses/{courseId}/lessons/{lessonId}는 같은 허용 필드를 일부 수정하며 200. 409는 차시 순번 중복 등.

DELETE 강의·차시는 아직 경로를 확정하지 않습니다. 이미 진행 이력이 있는 차시는 보존/공개 중지 정책부터 합의합니다.

PUT /api/admin/products/{productId}/courses: 관리자, `{"courseIds":[1,2]}`, 중복 없는 양의 ID 목록으로 전체 구성 교체 제안, 200 `{"productId":1,"courseIds":[1,2]}`. 400 입력, 404 대상, 기존 이용권 영향 정책에 따른 409 제안. 이 계약만으로 기존 구독 구성 보존 문제가 해결되지 않습니다.

## 13. 학습 진도 상세 계약 — 신규 제안

김영훈 배분 제안. 계획은 완료/최근 학습 기록 중심이며 동영상 실제 재생 시간 추적은 기본 요구가 아닙니다.

GET /api/learning/courses: 로그인, page/size, 본인 강의 목록. 200 Page<LearningCourseResponse> 제안.

```json
{"courseId":1,"title":"웹 개발 기초","completedLessons":2,"totalLessons":5,"progressPercent":40,"usable":true,"lastLessonId":12,"resumeLessonId":13}
```

이 객체는 content의 항목 예시입니다. lastLessonId와 resumeLessonId는 학습 전/완료 후 정책에 따라 null 가능. 이력만 있고 이용권이 만료된 강의를 포함할지는 합의한 정책에 맞춰 usable=false와 접근 안내를 제공합니다.

GET /api/learning/courses/{courseId}/progress: 본인 이력, 200 다음 응답 제안. 타인 memberId를 Query로 받지 않습니다.

```json
{"courseId":1,"completedLessons":1,"totalLessons":2,"progressPercent":50,"lessons":[{"lessonId":11,"completed":true,"completedAt":"2026-10-19T10:00:00","lastAccessedAt":"2026-10-19T10:00:00"}]}
```

PUT /api/learning/lessons/{lessonId}/progress: 본인+수강권, `{"completed":true}`, 200 차시 진도 객체. 완료 시각/회원 ID는 서버가 결정합니다. UNIQUE(member_id,lesson_id)와 동시성 제어로 재전송 시 중복 행을 만들지 않습니다. 400 형식, 401 비인증, 403 수강권 없음, 404 차시, 409 충돌을 제안합니다. completed=false로 완료를 취소할 수 있는지는 구현 전 결정합니다.

이어 학습할 차시는 별도 저장된 순서 ID를 무조건 신뢰하지 않고 현재 공개 차시와 권한을 다시 확인합니다. 차시 0건, 전부 완료, 비공개 전환, 상품 구성 변경의 동작을 테스트합니다.

## 14. 공지사항·대시보드 상세 계약 — 후속 제안

### 공지사항

기본 범위는 공지사항으로 제안합니다. 기존 일반 게시글·작성자 수정/삭제 요구는 확장 범위에 보존하며, 팀에서 일반 게시판까지 선택하면 별도 권한/API를 확정합니다.

| Method / Path | 권한 | 요청 | 성공 |
|---|---|---|---|
| GET /api/posts | 공개 | page,size | 200 Page<PostSummary> |
| GET /api/posts/{postId} | 공개 | 없음 | 200 PostResponse |
| POST /api/admin/posts | 관리자 | {title,content} | 201 PostResponse |
| PATCH /api/admin/posts/{postId} | 관리자 | {title,content} 중 변경 필드 | 200 PostResponse |
| DELETE /api/admin/posts/{postId} | 관리자 | 없음 | 204 |

요청 예 `{"title":"신규 학습 콘텐츠 안내","content":"서비스 공지 본문"}`. title 공백 불가·최대 200자, content 필수 제안. 작성자는 인증 정보로 결정, type=NOTICE 서버 지정.

응답 제안은 postId/memberId/title/content/type/createdAt/updatedAt이며 목록에는 본문 생략을 제안합니다. 400 입력, 401 비인증, 403 관리자 아님, 404 없음. HTML/Markdown을 허용한다면 화면 렌더링 정책을 별도로 정하고 사용자 입력을 안전하게 처리합니다.

### 대시보드

GET /api/admin/dashboard?from=2026-10-01&to=2026-11-01, 관리자. from 이상·to 미만 한국 시간 기준 제안이며 from<to와 최대 조회 범위를 검증합니다.

```json
{"from":"2026-10-01","to":"2026-11-01","timezone":"Asia/Seoul","totalMembers":120,"totalProducts":8,"totalCourses":12,"activeSubscriptions":75,"cancelledSubscriptions":8,"expiredSubscriptions":20,"paymentCount":30,"paymentAmount":387000,"refundCount":2,"refundAmount":18000,"netAmount":369000,"subscriptionsByProduct":[{"productId":1,"count":20}]}
```

전부 예시 숫자이며 구현된 통계가 아닙니다. activeSubscriptions는 ACTIVE이며 유효 기간 내인 수, cancelledSubscriptions는 해지 예약 상태 수와 만료 여부를 명확히 구분합니다. 회원 수의 탈퇴 포함, 강의 수의 DRAFT 포함 등 정의를 팀에서 확정합니다. 월별 통계는 월 구간별 같은 규칙으로 집계합니다.

paymentAmount는 paidAt 기간의 최초 성공 거래 합계이며 REFUNDED로 바뀐 거래도 최초 결제 사실을 포함하는 안입니다. refundAmount는 refundedAt 기간의 승인 환불액. netAmount는 두 금액의 차이이며 회계상 매출로 단정하지 않습니다. 기간 이전 결제의 이번 기간 환불로 음수가 될 수 있습니다.

## 15. 기존 명세 변경·보존 기록

| 원문 항목 | 교체 기준 |
|---|---|
| success/message/data 래퍼 | 현재 DTO 직접 응답으로 교정, 래퍼 도입은 이관 작업 |
| POST /subscriptions의 productId만 전송 | CheckoutRequest 네 필드로 교정 |
| POST /payments | 별도 생성 없음, POST /subscriptions로 통합 |
| PAID | 현재 COMPLETED로 통일 |
| GET /payments/{id} | 현재 미구현, 상세 화면 필요 시 추가 계획 |
| /payments/{id}/refunds | 현재 POST /refunds + paymentId |
| /refunds/{id}/approve, /reject | 현재 PATCH /admin/refunds/{id} + decision |
| 관리자 status 필터 | 현재 없음, 후속 추가 계획 |
| 일반 회원 게시글 CRUD | 확장 요구로 보존, 공지사항 기본안과 분리 |
| 대시보드·월별·상품별 집계 | 기간/상태/계산 정의를 추가한 후속 계약 |

## 16. API 완료 점검

- [ ] 모든 구현 경로를 Controller와 대조
- [ ] 입력 필수·형식·범위·enum 검증
- [ ] 성공 응답과 실제 상태 코드 확인
- [ ] 미구현 계약을 계획으로 표시
- [ ] 타인 데이터·관리자 권한·유료 콘텐츠 노출 검증
- [ ] 페이지/정렬/필터 구현 여부 확인
- [ ] 중복 결제·환불·진도 저장 검증
- [ ] 날짜 경계·환불 승인 후 권한 검증
- [ ] Frontend 실제 요청과 응답 필드 대조
- [ ] 변경 시 ERD·화면 문구·테스트 동시 갱신
