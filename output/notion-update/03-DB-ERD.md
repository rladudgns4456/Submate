# SubMate DB / ERD 설계

## 1. 설계 구분

현재 거래 스키마와 학습 콘텐츠 확장안을 구분합니다. 이 문서는 신규 강의 테이블이 이미 생성되었다는 의미가 아닙니다.

- 현재 DDL: `backend/src/main/resources/db/subscription-schema.sql`.
- Member/Product: 실제 도메인 연결 전 최소 읽기 계약 존재.
- 신규 설계 제안: Course, Lesson, ProductCourse, LearningProgress.
- Category·공지사항: 정식 통합 때 정의·검증할 영역.

## 2. 현재 거래 관계

| 부모 | 자식 | 관계 |
|---|---|---|
| members | subscriptions | 1:N |
| products | subscriptions | 1:N |
| members | payments | 1:N |
| subscriptions | payments | 1:N — 현재 최초 결제 1건만 생성 |
| payments | refunds | 1:0..1 |

Subscription은 회원과 상품을 각각 참조합니다. Payment는 Product가 아니라 Subscription을 참조하고 회원 ID도 저장합니다. Refund는 원결제를 참조합니다.

## 3. 회원·상품 읽기 계약

| 테이블 | 현재 CatalogGateway에 필요한 컬럼 |
|---|---|
| members | member_id BIGINT, email UNIQUE, role USER/ADMIN |
| products | product_id BIGINT, name, price DECIMAL, billing_cycle MONTHLY/YEARLY, status ACTIVE/INACTIVE |

실제 회원의 password/name 등 전체 스키마는 김현빈 담당과 합의합니다. demo-catalog.sql은 데모/테스트용이며 정식 회원·상품 모델을 대체하지 않습니다.

Product는 학습 이용권입니다. 기간을 임의의 이용 일수로 바꾸지 않고 기존 월간/연간 계약을 유지합니다. Category를 강의 분류에 연결하는 것을 제안하며, 기존 상품 분류와 공유할지는 별도 합의합니다.

## 4. Subscription — 현재 구현

| 컬럼 | 타입 / 제약 | 의미 |
|---|---|---|
| subscription_id | BIGINT PK AUTO_INCREMENT | 구독 ID |
| member_id | BIGINT NOT NULL FK | 회원 |
| product_id | BIGINT NOT NULL FK | 상품 |
| status | VARCHAR(20) NOT NULL | ACTIVE/CANCELLED/EXPIRED |
| start_date / end_date | DATE NOT NULL | 한국 날짜 기준 이용 기간 |
| cancelled_at | TIMESTAMP NULL | 해지 신청 시점 |
| created_at / updated_at | TIMESTAMP | 생성·수정 시점 |
| version | BIGINT NOT NULL DEFAULT 0 | 낙관적 잠금 |

CHECK end_date >= start_date. 인덱스 `(member_id,product_id,end_date)`, `(status,end_date)`.

이용 기간은 [start_date,end_date), CANCELLED도 종료일까지 이용할 수 있습니다. 환불 승인 시 미래 end_date를 승인일로 단축하고 EXPIRED로 바꿉니다.

## 5. Payment — 현재 구현

| 컬럼 | 타입 / 제약 | 의미 |
|---|---|---|
| payment_id | BIGINT PK AUTO_INCREMENT | 결제 ID |
| subscription_id | BIGINT NOT NULL FK | 구독 |
| member_id | BIGINT NOT NULL FK | 회원 |
| request_key | VARCHAR(64) NOT NULL | 중복 요청 식별 |
| amount | DECIMAL(10,2) NOT NULL, 양수 | 결제 당시 금액 |
| payment_method | VARCHAR(30) NOT NULL | CARD/BANK_TRANSFER |
| status | VARCHAR(20) NOT NULL | PENDING/COMPLETED/FAILED/REFUNDED |
| paid_at | TIMESTAMP NULL | 성공 시점 |
| created_at | TIMESTAMP NOT NULL | 생성 시점 |
| version | BIGINT NOT NULL DEFAULT 0 | 동시성 제어 |

UNIQUE `(member_id,request_key)`. 현재 흐름은 성공 시 COMPLETED를 저장하고 환불 승인 시 REFUNDED로 변경합니다. 실패한 최초 결제는 저장하지 않으며 실패 이력 저장이 필요하면 payment_attempts 등의 별도 설계를 검토합니다.

## 6. Refund — 현재 구현

| 컬럼 | 타입 / 제약 | 의미 |
|---|---|---|
| refund_id | BIGINT PK AUTO_INCREMENT | 환불 ID |
| payment_id | BIGINT NOT NULL FK UNIQUE | 원결제, 요청 최대 1회 |
| amount | DECIMAL(10,2) NOT NULL, 양수 | 요청일에 확정한 금액 |
| total_days / remaining_days | BIGINT NULL | 일할 계산 당시 일수 |
| reason | VARCHAR(255) NOT NULL | 사유 |
| status | VARCHAR(20) NOT NULL | REQUESTED/COMPLETED/REJECTED |
| refunded_at | TIMESTAMP NULL | 승인 완료 시점 |
| created_at | TIMESTAMP NOT NULL | 요청 시점 |
| version | BIGINT NOT NULL DEFAULT 0 | 동시성 제어 |

일수 컬럼의 NULL은 정책 도입 전 이력 호환을 위한 것입니다. 기존 금액을 새 정책으로 소급 변경하지 않습니다. 반려도 동일 원결제의 환불 행을 유지하므로 재요청은 불가합니다.

## 7. 학습 콘텐츠 확장 — 제안

| 테이블 | 주요 컬럼 제안 | 제약 / 목적 |
|---|---|---|
| courses | course_id, category_id, title, description, learning_objectives, level, status, created_at, updated_at | 강의 소개·학습 분야·공개 상태 |
| lessons | lesson_id, course_id, title, position, content, video_url, status, created_at, updated_at | 강의별 차시, UNIQUE(course_id,position) |
| product_courses | product_id, course_id | 복합 PK 또는 UNIQUE(product_id,course_id), 이용권 포함 강의 |
| learning_progress | progress_id, member_id, lesson_id, completed_at, last_accessed_at, version | UNIQUE(member_id,lesson_id), 회원별 학습 기록 |

FK 제안: Category 1:N Course, Course 1:N Lesson, Product N:M Course, Member N:M Lesson(learning_progress 경유).

키 타입은 기존 BIGINT와 맞춥니다. 영상 링크는 선택값이고, 첫 범위는 텍스트/자료 기반 차시를 제안합니다. 정확한 길이·NULL·상태 enum·인덱스는 구현 전에 DDL과 API를 함께 확정합니다.

진도는 구독 행마다 복제하기보다 회원·차시 기준으로 보존하는 안을 제안합니다. 강의 진도율은 공개된 차시 중 완료 차시 비율로 계산하고, 차시 증감 시 분모 처리와 완료 판정 기준은 별도 합의합니다.

## 8. 수강 권한과 데이터 보존

- 회원의 유효 구독 상품이 강의를 포함하는지 서버에서 확인합니다.
- 유효 조건은 ACTIVE/CANCELLED와 날짜 범위를 모두 검사합니다.
- 다른 유효 이용권이 같은 강의를 포함하면 해당 이용권으로 수강할 수 있는 안을 제안합니다.
- 만료·환불 후 진도는 보존하되 권한 없는 콘텐츠 조회·진도 변경은 차단합니다.
- 강의 판매 중지와 기존 수강자 공개 중단은 다른 결정이므로 상태 정책을 분리 검토합니다.
- 상품-강의 구성 변경이 기존 구독의 접근 범위에 미치는 영향은 구현 전 합의해야 합니다. 현재 원본 코드에는 이를 보존할 스냅샷이 없습니다.

## 9. 공지사항·통계

공지사항 테이블은 후속 설계 대상입니다. 제목·내용·관리자 작성자·작성/수정 시각을 기본으로 제안합니다. 통계는 기존 원본 데이터를 집계하며 통계 테이블을 먼저 만들지 않습니다.

## 10. 변경·마이그레이션 원칙

- DB명 `submate`, 테이블·컬럼 snake_case, 회원 테이블은 `members`로 통일합니다.
- 거래 FK에 무조건적인 CASCADE DELETE를 설정하지 않습니다.
- 금액은 BigDecimal/DECIMAL로 처리합니다.
- DATE 이용 기간과 TIMESTAMP 사건 시점을 구분하고 Asia/Seoul 기준을 유지합니다.
- 기존 DB 수정은 별도 마이그레이션으로 작성하고 적용 순서와 데이터 영향을 기록합니다.
- JPA 기본 validate, 실제 MySQL InnoDB FK/CHECK·잠금 검증 필요.
- 신규 학습 테이블 DDL이 없는 상태를 DB 구축 완료로 표시하지 않습니다.
