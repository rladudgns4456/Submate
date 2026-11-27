# 구현 구조와 통합 경계

기존 패키지 com.submate.backend를 유지합니다.

- subscription: controller / dto / entity / repository / service / scheduler
- payment, refund: controller / dto / entity / repository / service
- admin/subscription/controller: 담당 영역 관리자 API
- subscription/integration/CatalogGateway: Member/Product 읽기 및 결제 시 잠금
- subscription/support: 담당 API 전용 인증/오류 처리/Clock
- subscription/demo: demo 프로필 전용 Basic 인증

Controller는 요청·인증 회원 식별·DTO 응답, Service는 검증·상태 전이·트랜잭션, Repository는 저장/조회만 담당합니다.
오류 Advice는 이번 담당 패키지에만 적용됩니다.

## 동시성

- 신청: Member → Product 잠금. 같은 회원 신청 직렬화 후 멱등키/유효 구독 검사.
- 해지/만료: Subscription 비관적 잠금.
- 환불 요청: Payment 잠금.
- 환불 승인: Refund → Payment → Subscription 잠금.
- UNIQUE 및 version 컬럼을 보조 제약으로 사용.
- 승인 시 결제/환불/구독은 하나의 트랜잭션.
- Product 잠금은 가격 변경과 신청을 직렬화합니다. 대규모 서비스 최적화는 후속 과제입니다.

매 분 만료 대상을 100개씩 독립 트랜잭션으로 처리합니다.
status IN (ACTIVE,CANCELLED) AND end_date <= 오늘.
EXPIRED는 다시 처리하지 않으며, usable은 날짜로 별도 계산하므로 스케줄러 지연이 이용 기간을 늘리지 않습니다.
설정: submate.scheduler.enabled, submate.scheduler.expiry-cron.

구독 목록의 상품명은 건별 Gateway 조회입니다. 페이지 최대 100개로 제한하며 대량 데이터에서는 projection/join 최적화가 필요합니다.

## Frontend

- api/axios.js: API Base URL, 메모리 인증 헤더
- api/*Api.js: 도메인별 호출
- routes/AppRouter.jsx: 사용자/관리자 라우팅
- context/AuthContext.jsx + authState.js: 세션과 데모 로그인
- api/authApi.js: 기존 /auth/login·/auth/signup·/auth/me 연결, token → accessToken 변환
- pages/auth/AuthPage.jsx: 실제 로그인/회원가입 연결 UI
- pages/payment/ProductSelectionPage.jsx: 기존 상품 배열 API → 결제 연결 화면
- hooks/useResource.js: 로딩/오류/이전 응답 무시
- pages/: 실제 화면, components/: 상태/페이지네이션
- styles/tokens.css: 팀 공통 색상·폰트·간격·모서리 토큰
- index.css: 공통 기본 스타일 (main.jsx에서 로딩)
- styles/subscriptions.css: 공통 UI 및 구독 화면 반응형 스타일

팀 공통 규칙은 [프론트엔드 가이드](frontend-guide.md)를 따릅니다.

JavaScript, React Router, 기존 Vite 구조를 유지합니다.
서버는 요청마다 권한을 확인하므로 클라이언트 메뉴/라우트 제한만 신뢰하지 않습니다.

## JWT·회원·상품 담당자 인수인계

1. Authentication.name에 회원 이메일, authorities에 ROLE_USER/ROLE_ADMIN 설정.
2. members.role과 인증 역할 일치 확인.
3. App에 session={accessToken,email,role}, onLogout 콜백 전달.
4. 상품 신청 링크 /subscribe/{productId} 연결.
5. members/products 실제 컬럼 계약 확인, SQL 적용.
6. 기존 전역 permitAll 설정을 팀 JWT 정책으로 교체.
7. MySQL + 실제 JWT 기반 E2E 수행.

현재 전역 SecurityConfig에는 병합된 JwtAuthenticationFilter가 연결되어 있습니다.
Frontend는 로그인 token을 받은 뒤 /api/auth/me로 회원/권한을 조회합니다. 실제 토큰 만료는 보호 API의 401 뒤 /auth/me를 확인하여 처리하고, 업무 오류의 401만으로 세션을 종료하지 않습니다. 인증은 메모리 방식이며 새로고침 시 초기화합니다.

현재 상품 목록과 카테고리는 배열 응답이며 거래 목록의 PageResponse와 구분합니다. 공개 /products에서 상품을 선택하고 로그인 후 원래 /subscribe/{productId}로 돌아옵니다. demo 모드의 상품 API는 기존 demo Security에 맞춰 로그인 뒤 접근합니다.

실제 MySQL 통합·강의/차시/수강 권한·학습 진도는 별도 후속 단계입니다. [김영훈 작업 순서](younghoon-work-plan.md)에 상태와 연결 경계를 기록합니다.
