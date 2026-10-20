# 구독·결제·환불 API

## 회원·상품 Frontend 연결 확인 — 2026-10-20

현재 병합된 Backend와 연결한 실제 경로입니다. 아래 거래 계약을 변경하지 않습니다.

| Method | 경로 | 요청/응답 |
|---|---|---|
| POST | /api/auth/signup | {email,password} → {message,memberId} |
| POST | /api/auth/login | {email,password} → {message,token} |
| GET | /api/auth/me | Bearer token → {memberId,email,role} |
| GET | /api/categories | 카테고리 배열 [{categoryId,name}] |
| GET | /api/products?categoryId=1 | 상품 배열 [{productId,categoryId,categoryName,name,price,billingCycle,status}] |

회원 가입/로그인 성공은 현재 200입니다. Frontend는 token을 받은 뒤 /auth/me로 권한을 확인하고 accessToken 세션 규약으로 변환합니다. 상품 목록은 거래 목록의 PageResponse와 다른 배열 응답입니다. 실제 로그인·상품 선택·거래 연결은 별도 H2/일반 JWT 환경에서 확인했고 실제 MySQL은 후속 검증입니다.

## 공통 규칙

- Base URL: /api
- 실제 인증: Authorization: Bearer {accessToken}, Authentication.name = 회원 이메일
- 관리자: ROLE_ADMIN 권한과 members.role=ADMIN 모두 확인
- demo 서버 프로필에서만 테스트 계정 HTTP Basic 사용
- 금액/회원 ID는 서버에서 결정하며 요청 값을 신뢰하지 않음
- 타인 리소스는 404, 비인증 401, 비관리자 403
- 날짜 YYYY-MM-DD, 날짜시간 YYYY-MM-DDTHH:mm:ss (Asia/Seoul)
- 목록 page=0, size=20 기본, size 1~100, 최신 ID 내림차순
- 성공 응답은 DTO 자체, 오류는 {"status":409,"message":"..."}
- 400: 잘못된 요청/가상 결제 실패, 409: 중복/상태/동시성 충돌

## 엔드포인트

| Method | Path | 권한 | 기능 |
|---|---|---|---|
| POST | /subscriptions | 로그인 | 가상 결제 + 구독 생성 |
| GET | /subscriptions | 로그인 | 본인 구독 목록 |
| GET | /subscriptions/quote?productId=1 | 로그인 | 신청 전 상품 정보 |
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

관리자도 임의의 상태 문자열을 입력할 수 없습니다. 전용 해지/환불/만료 로직만 사용합니다.

## 구독 신청

POST /subscriptions

```json
{
  "productId": 1,
  "paymentMethod": "CARD",
  "requestKey": "unique-checkout-key",
  "simulateFailure": false
}
```

- productId: 양의 정수, 필수
- paymentMethod: CARD / BANK_TRANSFER, 필수
- requestKey: 1~64자 영문·숫자·하이픈·밑줄, 회원별 UNIQUE
- simulateFailure: boolean 필수. true면 400이고 DB에 행을 생성하지 않음
- 새 성공은 201, 같은 키/같은 내용의 재전송은 200
- 같은 키를 다른 상품/수단/실패 요청에 재사용하면 409
- 종료일이 미래인 ACTIVE/CANCELLED 동일 상품 구독이 있으면 409

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

재전송은 현재 상태를 반환하므로 나중에 환불된 요청의 payment.status는 REFUNDED일 수 있습니다.
실패 시 저장된 행이 없으므로 같은 키로 정상 재시도가 가능합니다.

## 목록·견적·해지

모든 목록:

```json
{"content":[],"page":0,"size":20,"totalElements":0,"totalPages":0}
```

content는 해당 리소스 DTO 배열입니다.
구독 상세/해지 응답은 위 subscription 객체와 같습니다.
견적은 productId/name/price/billingCycle/status를 반환합니다.
결제 목록은 위 payment 객체 배열입니다.

PATCH /subscriptions/{id}/cancel: Body 없음.
ACTIVE 및 종료일 이전만 허용하며 cancelledAt을 기록하고 endDate를 유지합니다.
이미 해지/만료되었으면 409. 해지 자체로 환불되지 않습니다.
usable은 Scheduler 실행 여부와 무관하게 현재 날짜를 포함하여 계산합니다.

## 환불

POST /refunds → 201

```json
{"paymentId":1,"reason":"이용 계획 변경"}
```

reason은 공백 제외 필수, 최대 255자. 금액은 서버가 결제금액 × 요청일 기준 남은 일수 ÷ 전체 일수로 계산하고 원 미만을 버립니다.
COMPLETED 결제만 요청할 수 있으며 이미 환불 행이 있으면 409입니다.

```json
{
  "refundId": 1,
  "paymentId": 1,
  "amount": 12900.00,
  "reason": "이용 계획 변경",
  "status": "REQUESTED",
  "refundedAt": null,
  "createdAt": "2026-09-15T18:10:00",
  "totalDays": 30,
  "remainingDays": 30
}
```

PATCH /admin/refunds/{id} → 200

한국 날짜 기준이며 종료일은 제외하고 요청 당일은 남은 일수에 포함합니다.
예: 12,900원 × 18일 / 28일 = 8,292원. 요청 시 금액과 일수를 저장하여 승인일이 바뀌어도 금액을 유지합니다.
대기 중 이용은 유지되며 승인 시 즉시 종료됩니다. 정책 적용 전 환불의 일수 필드는 null입니다.

기존 MySQL DB에는 배포 전에 `backend/src/main/resources/db/migrations/20260923-prorated-refunds.sql`을 한 번 적용합니다.
신규 DB는 최신 `subscription-schema.sql`을 사용합니다. 기존 환불 금액은 변경하지 않습니다.

```json
{"decision":"APPROVE"}
```

또는 {"decision":"REJECT"}.

- REQUESTED만 처리, 중복 처리는 409
- 승인: Refund=COMPLETED, Payment=REFUNDED, 구독 이용 즉시 종료(EXPIRED)
- 반려: Refund=REJECTED, 기존 결제·구독 유지
- 결제당 환불 요청 1회, 반려 후 재요청 불가
- 기간이 끝난 구독은 신규 환불 요청 불가 (409)
- 승인 응답은 위 refund 객체와 같고 refundedAt 기록

## Frontend 연결

상품 페이지에서 /subscribe/{productId}로 이동합니다.
구독 신청은 결제와 원자화되어 별도 Payment 생성 API를 호출하지 않습니다.
기존 연동 API가 없는 초기 저장소 기준의 신규 계약입니다.
