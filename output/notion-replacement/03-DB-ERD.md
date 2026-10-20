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

## 11. Member / Category / Product 상세 설계 — 통합안

이 절은 원본의 회원·카테고리·상품 전체 필드를 보존한 통합 설계입니다. 최소 읽기 계약 이외에는 팀원 구현과 대조하여 확정합니다. 모든 설계표의 ‘제안’은 아직 실제 MySQL에 적용된 DDL이 아닙니다.

### members

| 컬럼 | 타입 제안 | NULL | 키·제약 | 설명 |
|---|---|---|---|---|
| member_id | BIGINT | NO | PK, AUTO_INCREMENT | 회원 ID |
| email | VARCHAR(255) | NO | UNIQUE | 로그인 이메일 |
| password | VARCHAR(255) | NO | 해시 저장 | 원문 비밀번호 저장 금지 |
| name | VARCHAR(50) | NO | 공백 검증 | 표시 이름 |
| role | VARCHAR(20) | NO | USER/ADMIN CHECK 제안 | 서버가 일반 가입 USER 지정 |
| created_at | TIMESTAMP | NO | 생성 시 서버 설정 | 가입 시점 |
| updated_at | TIMESTAMP | YES | 수정 시 서버 설정 | 변경 시점 |

현재 Gateway 필수는 member_id/email/role입니다. 기존 원문의 사건 시점 DATETIME은 현재 거래 DDL의 TIMESTAMP와 통일하는 안으로 기재했습니다. 팀원 DB가 DATETIME을 사용한다면 실제 타입·JDBC 시간대·변환을 함께 합의하고 마이그레이션 없이 임의 변경하지 않습니다.

탈퇴 정책은 별도 결정입니다. 비활성 컬럼을 추가할지, 비식별 처리할지 먼저 합의하며 위 테이블이 이미 탈퇴 보존 기능을 구현했다고 보지 않습니다.

### categories

| 컬럼 | 타입 제안 | NULL | 키·제약 | 설명 |
|---|---|---|---|---|
| category_id | BIGINT | NO | PK, AUTO_INCREMENT | 분야 ID |
| name | VARCHAR(50) | NO | UNIQUE | 분야명 |
| created_at | TIMESTAMP | NO | 서버 설정 | 생성 시점 |

분야 예시: 프로그래밍, 컴퓨터 기초, 데이터·AI, 자격증, 외국어, 업무·생산성. 실제 제공 콘텐츠에 맞춰 선택합니다. 예시 카테고리가 이미 등록되었다는 뜻은 아닙니다.

원본 Category 1:N Product 관계는 유지 제안합니다. 강의도 동일 카테고리를 참조하는 안이며, 상품과 강의가 같은 분류를 쓸지 별도 분류가 필요한지 팀에서 확정합니다. 연결 상품/강의가 있으면 무조건적인 카테고리 삭제는 금지합니다.

### products

| 컬럼 | 타입 제안 | NULL | 키·제약 | 설명 |
|---|---|---|---|---|
| product_id | BIGINT | NO | PK, AUTO_INCREMENT | 이용권 ID |
| category_id | BIGINT | NO | FK categories 제안 | 상품 분야 |
| name | VARCHAR(100) | NO | 공백 검증 | 이용권 이름 |
| description | TEXT | YES | - | 상품 설명 |
| price | DECIMAL(10,2) | NO | CHECK(price>0) 제안 | 현재 판매 가격 |
| billing_cycle | VARCHAR(20) | NO | MONTHLY/YEARLY CHECK 제안 | 이용 기간 단위 |
| status | VARCHAR(20) | NO | ACTIVE/INACTIVE CHECK 제안 | 신규 판매 여부 |
| created_at | TIMESTAMP | NO | 서버 설정 | 생성 시점 |
| updated_at | TIMESTAMP | YES | 서버 설정 | 수정 시점 |

CatalogGateway가 사용하는 실제 필드명은 product_id/name/price/billing_cycle/status입니다. INACTIVE는 신규 신청을 막고 기존 거래를 삭제하지 않습니다. billing_cycle은 자동 결제 설정이 아닙니다.

학습 상품명 예시: 개발 기초 월간 이용권, 외국어 학습 연간 이용권. 기존 demo 시드는 Cinema/Workspace이므로 학습용 데이터 전환은 별도 작업입니다.

## 12. 현재 거래 테이블 전체 컬럼·NULL·제약 재확인

### subscriptions

| 컬럼 | 실제 DDL 타입 | NULL | 실제 제약·기본값 |
|---|---|---|---|
| subscription_id | BIGINT | NO | PK AUTO_INCREMENT |
| member_id | BIGINT | NO | FK members(member_id) |
| product_id | BIGINT | NO | FK products(product_id) |
| status | VARCHAR(20) | NO | CHECK ACTIVE/CANCELLED/EXPIRED |
| start_date | DATE | NO | - |
| end_date | DATE | NO | CHECK end_date>=start_date |
| cancelled_at | TIMESTAMP | YES | - |
| created_at | TIMESTAMP | NO | 생성 로직에서 설정 |
| updated_at | TIMESTAMP | YES | - |
| version | BIGINT | NO | DEFAULT 0, @Version |

인덱스: idx_subscription_member(member_id,product_id,end_date), idx_subscription_expiry(status,end_date).
유효 기간 중복 방지에 단순 UNIQUE(member_id,product_id)를 사용하지 않습니다. 만료 후 재구독 이력을 저장해야 하므로 서버 잠금과 유효 기간 조회로 제어합니다.

### payments

| 컬럼 | 실제 DDL 타입 | NULL | 실제 제약·기본값 |
|---|---|---|---|
| payment_id | BIGINT | NO | PK AUTO_INCREMENT |
| subscription_id | BIGINT | NO | FK subscriptions(subscription_id) |
| member_id | BIGINT | NO | FK members(member_id) |
| request_key | VARCHAR(64) | NO | UNIQUE(member_id,request_key) |
| amount | DECIMAL(10,2) | NO | CHECK amount>0 |
| payment_method | VARCHAR(30) | NO | CHECK CARD/BANK_TRANSFER |
| status | VARCHAR(20) | NO | CHECK PENDING/COMPLETED/FAILED/REFUNDED |
| paid_at | TIMESTAMP | YES | 현재 성공 흐름에서 설정 |
| created_at | TIMESTAMP | NO | 서버 설정 |
| version | BIGINT | NO | DEFAULT 0, @Version |

member_id는 연결 구독의 소유자와 같아야 하며 현재 생성 Service가 보장합니다. DB의 단순 FK가 두 회원 ID의 일치를 자동 보장하는 것은 아닙니다.

### refunds

| 컬럼 | 실제 DDL 타입 | NULL | 실제 제약·기본값 |
|---|---|---|---|
| refund_id | BIGINT | NO | PK AUTO_INCREMENT |
| payment_id | BIGINT | NO | FK payments(payment_id), UNIQUE |
| amount | DECIMAL(10,2) | NO | CHECK amount>0 |
| total_days | BIGINT | YES | 이전 이력 호환 |
| remaining_days | BIGINT | YES | 이전 이력 호환 |
| reason | VARCHAR(255) | NO | 앱에서 공백 검증 |
| status | VARCHAR(20) | NO | CHECK REQUESTED/COMPLETED/REJECTED |
| refunded_at | TIMESTAMP | YES | 승인 완료 때 설정 |
| created_at | TIMESTAMP | NO | 요청 시점 |
| version | BIGINT | NO | DEFAULT 0, @Version |

현재 DDL에 total_days/remaining_days의 범위 CHECK는 없습니다. Service에서 신규 요청의 양수 기간과 환불 양수 금액을 검사합니다. 설계에 더 강한 CHECK를 제안할 경우 기존 null 이력·실제 MySQL 적용을 고려해야 합니다.

## 13. 학습 테이블 전체 컬럼 — 신규 제안

### courses

| 컬럼 | 타입 제안 | NULL | 제약 제안 | 설명 |
|---|---|---|---|---|
| course_id | BIGINT | NO | PK AUTO_INCREMENT | 강의 ID |
| category_id | BIGINT | NO | FK categories | 분야 |
| title | VARCHAR(200) | NO | 공백 검증 | 제목 |
| description | TEXT | YES | - | 소개 |
| learning_objectives | TEXT | YES | - | 목표·대상 |
| level | VARCHAR(20) | NO | BEGINNER/INTERMEDIATE/ADVANCED | 난이도 |
| status | VARCHAR(20) | NO | DRAFT/PUBLISHED/ARCHIVED | 공개 상태 |
| created_at | TIMESTAMP | NO | 서버 설정 | 생성 시점 |
| updated_at | TIMESTAMP | YES | 서버 설정 | 수정 시점 |

인덱스 제안 `(status,category_id,course_id)`. 학습 시간·썸네일은 데이터 확보와 화면 합의 후 선택적으로 추가하며 가짜 값으로 채우지 않습니다.

### lessons

| 컬럼 | 타입 제안 | NULL | 제약 제안 | 설명 |
|---|---|---|---|---|
| lesson_id | BIGINT | NO | PK AUTO_INCREMENT | 차시 ID |
| course_id | BIGINT | NO | FK courses | 소속 강의 |
| title | VARCHAR(200) | NO | 공백 검증 | 차시명 |
| position | INT | NO | CHECK >0, UNIQUE(course_id,position) | 학습 순서 |
| content | TEXT | YES | 공개 시 본문/허용 영상 존재 검증 제안 | 본문 |
| video_url | VARCHAR(2048) | YES | 앱에서 형식·허용 URL 검증 | 선택 영상 |
| status | VARCHAR(20) | NO | DRAFT/PUBLISHED/ARCHIVED | 공개 상태 |
| created_at | TIMESTAMP | NO | 서버 설정 | 생성 시점 |
| updated_at | TIMESTAMP | YES | 서버 설정 | 수정 시점 |

순서를 바꿀 때 UNIQUE 위반과 동시 수정을 처리해야 합니다. 순번 전체를 임시로 겹치게 저장하는 변경은 피하고 트랜잭션 설계를 검토합니다. 비공개 전환 뒤에도 진도 FK는 유지합니다.

### product_courses

| 컬럼 | 타입 제안 | NULL | 제약 제안 | 설명 |
|---|---|---|---|---|
| product_id | BIGINT | NO | FK products, 복합 PK 구성 | 이용권 |
| course_id | BIGINT | NO | FK courses, 복합 PK 구성 | 포함 강의 |

PK(product_id,course_id), course_id 역방향 조회 인덱스 제안. 같은 강의를 여러 이용권에 포함할 수 있습니다.

이는 현재 상품 구성 모델이며 과거 계약의 포함 강의를 별도로 고정하지 않습니다. 기존 구독 구성 보존을 선택하면 subscription_courses 같은 스냅샷이나 상품 버전 모델을 추가 검토해야 합니다. 이 문서에서 해당 확장을 자동 확정하지 않습니다.

### learning_progress

| 컬럼 | 타입 제안 | NULL | 제약 제안 | 설명 |
|---|---|---|---|---|
| progress_id | BIGINT | NO | PK AUTO_INCREMENT | 진도 ID |
| member_id | BIGINT | NO | FK members | 학습자 |
| lesson_id | BIGINT | NO | FK lessons | 차시 |
| completed_at | TIMESTAMP | YES | null이면 미완료 | 완료 시점 |
| last_accessed_at | TIMESTAMP | NO | 서버 설정 | 최근 학습 시점 |
| version | BIGINT | NO | DEFAULT 0, @Version | 동시 수정 |

UNIQUE(member_id,lesson_id), 목록 최적화 인덱스 `(member_id,last_accessed_at)` 제안. completed boolean은 completed_at 존재로 계산하는 안이며 DB에 중복 저장하지 않습니다. API progressPercent는 분자·분모를 서버에서 계산하며 DB에서 임의 수정하지 않습니다.

## 14. Post 전체 컬럼 — 공지사항 기본안

원문의 일반 게시판도 확장 요구로 보존하되 핵심 기능 이후 범위를 결정합니다.

| 컬럼 | 타입 제안 | NULL | 키·제약 제안 | 설명 |
|---|---|---|---|---|
| post_id | BIGINT | NO | PK AUTO_INCREMENT | 게시글 ID |
| member_id | BIGINT | NO | FK members | 관리자 작성자 |
| title | VARCHAR(200) | NO | 공백 검증 | 제목 |
| content | TEXT | NO | 공백 검증 | 내용 |
| type | VARCHAR(20) | NO | 기본 NOTICE | 게시글 종류 |
| created_at | TIMESTAMP | NO | 서버 설정 | 작성 시점 |
| updated_at | TIMESTAMP | YES | 서버 설정 | 수정 시점 |

일반 게시판 확장 선택 시 GENERAL과 작성자 권한을 추가합니다. 이를 현재 사용자 글쓰기 기능으로 간주하지 않습니다. 게시글 삭제 정책은 거래 데이터 보존 정책과 별도로 합의합니다.

## 15. 전체 FK·관계 설계

현재 거래 관계는 실선, 아직 구현하지 않은 확장 관계는 아래 표의 ‘설계’로 구분합니다.

| 자식 FK | 부모 PK | 관계 | 상태 |
|---|---|---|---|
| subscriptions.member_id | members.member_id | N:1 | 현재 DDL |
| subscriptions.product_id | products.product_id | N:1 | 현재 DDL |
| payments.subscription_id | subscriptions.subscription_id | N:1 | 현재 DDL |
| payments.member_id | members.member_id | N:1 | 현재 DDL |
| refunds.payment_id | payments.payment_id | 0..1:1 | 현재 DDL, UNIQUE |
| products.category_id | categories.category_id | N:1 | 통합 설계 |
| courses.category_id | categories.category_id | N:1 | 신규 설계 |
| lessons.course_id | courses.course_id | N:1 | 신규 설계 |
| product_courses.product_id | products.product_id | N:1 | 신규 설계 |
| product_courses.course_id | courses.course_id | N:1 | 신규 설계 |
| learning_progress.member_id | members.member_id | N:1 | 신규 설계 |
| learning_progress.lesson_id | lessons.lesson_id | N:1 | 신규 설계 |
| posts.member_id | members.member_id | N:1 | 후속 설계 |

```text
Category ──< Product ──< Subscription >── Member
                │             │             │
                │             └──< Payment >┘
                │                     │
                │                     └──0..1 Refund
                │
                └──< ProductCourse >── Course >── Category
                                         │
                                         └──< Lesson
                                                │
Member ─────────────────────────< LearningProgress
Member ──< Post
```

이 그림은 전체 논리 모델입니다. Category·Course·Lesson 등의 확장 관계는 아직 구현되지 않았습니다. 실제 구현 범위는 위 표의 상태를 기준으로 판단합니다.

## 16. データ生成・状態遷移・整合性

### 회원가입·상품 등록 — 통합 대상

회원 입력 검증 → 이메일 중복 검사 → 해시 저장 → USER 회원 생성.
카테고리 존재 확인 → 상품 가격/기간/상태 검사 → 상품 생성.

### 구독 신청 — 현재

회원 잠금 → 요청 키 조회 → 상품 잠금·판매/가격/중복 검사 → 가상 결제 결과 판단 → Subscription 저장 → Payment 저장 → 함께 커밋. 중간 실패는 롤백합니다.

### 해지·만료 — 현재

해지는 본인/관리자 권한·ACTIVE·종료일 전 확인 → cancelled_at 기록 → CANCELLED, end_date 유지.
만료는 ACTIVE/CANCELLED이며 end_date<=오늘인 행을 배치 처리 → EXPIRED. 상태가 그대로여도 기간이 끝났으면 usable=false입니다.

### 환불 — 현재

Payment 잠금·소유자/COMPLETED/기존 요청 확인 → 연결 Subscription 잠금·일수 계산 → REQUESTED 저장.
승인은 Refund → Payment → Subscription 순서 잠금 → 요청 시 금액 유지 → COMPLETED/REFUNDED/EXPIRED를 함께 커밋합니다. 반려는 Refund만 REJECTED로 바꿉니다.

### 학습 — 신규

인증 → 강의/차시 공개·소속 확인 → 상품 포함·유효 구독 검사 → 콘텐츠 응답 → 진도 upsert·최근 학습 시각 저장. 만료·환불 후 진도 조회 보존 범위와 변경 차단을 구분합니다.

## 17. 인덱스·삭제·금액·시간·Enum 기준

- 위 ‘현재 DDL’에 적은 인덱스·CHECK는 실제 SQL 기준입니다. 신규 인덱스는 EXPLAIN/목록 패턴을 보고 확정합니다.
- 거래·진도가 참조하는 부모를 무조건 CASCADE DELETE하지 않습니다. FK가 있다는 사실이 보존 정책 구현을 대신하지 않습니다.
- DECIMAL(10,2) 최대 범위와 양수 조건을 API에서도 검사합니다. 환불 계산은 BigDecimal, 원 미만 DOWN입니다.
- start_date/end_date는 DATE, 사건 시점은 TIMESTAMP. JDBC/DB 세션 시간대와 Java Clock을 함께 확인합니다.
- Role/BillingCycle/ProductStatus/SubscriptionStatus/PaymentStatus/PaymentMethod/RefundStatus 및 신규 CourseStatus/LessonStatus/Level/PostType은 문자열 저장을 제안합니다.
- JPA `@Enumerated(EnumType.STRING)` 사용, ORDINAL 금지. enum 이름 변경은 데이터 이관을 포함합니다.
- 현재 상품명은 동적 조회이며 명칭 스냅샷 보존은 구현되지 않았습니다. 결제 당시 금액·확정 구독 날짜와 구분합니다.

## 18. 담당·구현 순서·마이그레이션

| 영역 | 담당 | 기준 |
|---|---|---|
| Member/Category/Product | 김현빈 | 기존 분담 |
| Subscription/Payment/Refund | 김영훈 | 기존 분담 |
| Course/Lesson/ProductCourse | 김현빈 | 신규 배분 제안 |
| LearningProgress·수강 권한 | 김영훈 | 신규 배분 제안 |
| Post·관계·보존 정책·통합 | 공동 | 범위 합의 |

순서: 정식 Member/Category/Product 계약 → 거래 DDL 통합 → Course/Lesson → ProductCourse → 권한/진도 → 공지사항/통계. 기존 거래 구현을 처음부터 재작성하지 않습니다.

신규 DB에는 부모 테이블부터 준비하고 subscription-schema.sql을 최초 적용합니다. 기존 DB는 스키마/데이터와 백업을 확인한 뒤 필요한 변경만 순차 적용합니다. `20260923-prorated-refunds.sql`은 일수 컬럼 추가용이며 모든 강의 테이블을 만들어주는 스크립트가 아닙니다.

## 19. ERD 확정·적용 점검

- [ ] 테이블명·PK 타입·FK 컬럼명 확정
- [ ] 현재/계획 관계 구분
- [ ] 모든 컬럼 NULL/기본값/enum/길이 확인
- [ ] 금액·날짜·일수 CHECK와 기존 데이터 호환 확인
- [ ] 회원 탈퇴·상품 판매 중지·강의 비공개 정책 합의
- [ ] Product-Course 구성 변경과 기존 구독 영향 합의
- [ ] 차시 완료/재구독 진도 보존 기준 합의
- [ ] UNIQUE·인덱스·잠금 순서 검토
- [ ] 신규/기존 DB 적용 순서·롤백 계획 기록
- [ ] 실제 MySQL InnoDB에서 제약·동시성 검증
- [ ] Entity/DTO/API/테스트와 ERD 대조
