# 김영훈 담당 — 7일 개발 결과

실제 7일 동안 작업했다는 근무 기록이 아니라, 요청한 **약 7일치 분량을 기능 단위로 정리한 결과**입니다.

| 일차 | 개발 범위 | 산출물 |
|---|---|---|
| 1일 | 도메인·DB 계약 | Subscription/Payment/Refund Entity·Enum·DDL, 회원/상품 Gateway |
| 2일 | 구독 신청·가상 결제 | 원자적 생성, 서버 가격 검증, 실패 처리, 중복 요청 방지 |
| 3일 | 조회·권한 | 본인 구독·결제·환불 목록, 구독 상세, 페이지네이션, 소유권 검증 |
| 4일 | 해지·만료 | CANCELLED, 월말/윤년 계산, Clock, 만료 Scheduler |
| 5일 | 환불·관리자 | 요청·승인·반려, 일할 환불, 관리자 조회·해지 |
| 6일 | Frontend | 신청·결제·완료·내 구독·상세·결제/환불 내역·관리자 화면 |
| 7일 | 검증·문서 | 통합 테스트, 브라우저 확인, API·DB·실행·인수인계 문서 |

## 이번 범위에서 구현한 기능

- 결제 성공 시 Subscription과 Payment를 하나의 트랜잭션으로 생성
- 같은 요청 키 재전송 시 기존 결과 반환, 다른 키의 중복 이용 기간 차단
- ACTIVE → CANCELLED, ACTIVE/CANCELLED → EXPIRED
- 해지한 구독은 종료 시각 전까지 이용, 만료 후 새 구독 생성
- 관리자 일할 환불 승인 시 Payment/Refund/Subscription 일괄 변경
- 일반 사용자와 관리자 API 및 화면, 오류/로딩/빈 상태 처리
- 인증 회원 식별, 타인 정보 접근 차단, 금액 서버 검증

## 팀 검토가 필요한 결정

| 항목 | 이번 구현 | 이유 / 영향 |
|---|---|---|
| 결제 실패 이력 | 400 응답, 구독·결제 행 미생성 | 성공 후 구독 생성과 Payment.subscription_id NOT NULL을 동시에 유지 |
| 이용 종료 | endDate 당일 00:00 KST, 배타적 경계 | 상태 전이 경계 통일. 화면에도 정확한 시각 표시 |
| 환불 승인 | 일할 환불 + 즉시 이용 종료 | 미래 endDate를 오늘로 단축하고 EXPIRED |
| 환불 재요청 | 반려 후에도 불가 | 원본 Payment 1:0..1 Refund UNIQUE 유지 |
| DB 추가 | Payment.member_id/request_key, 각 Entity.version | 멱등성·조회·동시성 |
| CORS | 기존 localhost에 127.0.0.1:5173 추가 | 로컬 Vite POST 요청 허용 |
| DDL 정책 | 기본 validate | FK·CHECK가 포함된 SQL을 명시적으로 적용 |
| 인증 | 실제 JWT 연결 규약 + demo 전용 Basic | 회원/인증 담당자 영역을 대체하지 않고 검증 |

결제 실패 내역을 영속화하려면 payment_attempts 또는 nullable subscription FK를 공동 설계해야 합니다.
PENDING/FAILED enum은 보존했지만 현재 결제 흐름에서는 저장하지 않습니다.

## 남은 팀 통합

- [ ] 김현빈 담당 회원가입·로그인·JWT와 연결
- [ ] 실제 Member/Product Entity 및 MySQL 연동
- [ ] MySQL InnoDB 동시성·DDL 재검증
- [ ] 공통 AuthContext/Router/SecurityConfig 병합
- [ ] 추가 컬럼·날짜·환불 정책 공동 리뷰
- [x] 기본 구독·결제·환불 구현 develop 병합 (PR #2)
- [ ] 회원가입부터 시작하는 전체 서비스 E2E

게시판·대시보드·발표 자료, 실제 PG·자동 갱신·임의 금액 환불은 이번 범위 밖입니다.

## Git

최신 origin/develop에서 feature/subscription-lifecycle을 생성했습니다.
사용자가 미리 변경한 SecurityConfig 들여쓰기는 보존했습니다.
공통 파일 변경과 API/DB 계약은 이 문서로 인수인계하며, 팀원 합의나 원격 병합 완료로 간주하지 않습니다.

백엔드·프론트엔드·문서를 각각 로컬 커밋으로 정리합니다.
기본 구현은 PR #2로 develop에 병합되었습니다. 일할 환불 보완은 별도 PR로 반영합니다.
검증 결과는 [verification.md](verification.md)에 기록했습니다.
