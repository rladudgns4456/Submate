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
