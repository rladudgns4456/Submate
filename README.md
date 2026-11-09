# SubMate

React + Vite / Spring Boot / MySQL 기반의 외부 구독 통합 관리 서비스입니다.

## 외부 구독 관리

- 로그인하면 외부 구독 관리 화면(`/tracker`)이 열립니다.
- 서비스 이름, 원화 금액, 월간·연간 결제 주기, 기준 결제일을 입력해 등록·수정·삭제할 수 있습니다.
- 이용 중인 항목의 이번 달 예상 결제액과 다음 결제일을 한국 시간으로 계산합니다. 월말·윤일이 없는 달에는 해당 월의 마지막 날에 결제한 것으로 계산하고, 이후에는 기준일을 다시 적용합니다.
- 구독별로 결제일 최대 30일 전부터 대시보드에 알림을 표시합니다. 0일은 결제 당일, 빈 값은 알림 끔입니다.
- 외부 구독과 SubMate 가상 결제 구독은 각각의 메뉴에서 관리합니다.

### Gmail에서 Google Play 구독 영수증 불러오기

1. Google Cloud 프로젝트에서 Gmail API를 활성화하고 웹 애플리케이션 OAuth 클라이언트를 만듭니다.
2. 사용하는 프론트엔드 주소를 승인된 JavaScript 원본으로 등록합니다. 로컬 기본 주소는 `http://127.0.0.1:5173`입니다. 동의 화면을 테스트 모드로 운영하는 경우 사용할 Google 계정을 테스트 사용자로 추가합니다.
3. `frontend/.env.local`에 `VITE_GOOGLE_CLIENT_ID`를 지정하고 개발 서버를 재시작합니다. 배포 시에는 빌드 환경에 설정합니다.
4. 외부 구독 관리에서 Google 계정을 선택하고 영수증을 검색합니다. 후보를 선택한 뒤 서비스명·금액·주기·기준일을 확인하고 저장합니다. 동일 회원이 같은 영수증을 중복 저장하면 충돌 응답을 반환합니다.

브라우저에서 Gmail 읽기 권한을 요청해 최근 Google Play 구독 영수증을 검색합니다. Google 토큰과 원본 메일은 SubMate 서버에 전달하지 않습니다. 사용자가 확인한 구독 값과 중복 방지용 메시지 ID 해시만 저장합니다. 공개 배포 시 Google의 제한 범위 검증 요건을 확인해야 합니다. [Gmail API 권한 안내](https://developers.google.com/workspace/gmail/api/auth/scopes)

## 기존 SubMate 구독·거래 기능

- 구독·결제 원자적 생성, 서버 가격 검증, 중복 요청 방지
- 본인 구독 목록·상세, 해지, 한국 시간 기준 자동 만료
- 결제 내역, 환불 요청 및 관리자 일할 금액 승인·반려
- 사용자/관리자 화면과 API 연동
- 권한·동시성·경계 날짜·트랜잭션 통합 테스트

회원·JWT·상품·카테고리 백엔드와 회원가입·로그인·상품 선택 화면이 연결되어 있습니다.

실제 인증 모드에서는 `/products`에서 상품을 선택한 뒤 `/login` 또는 `/signup`으로 연결됩니다. 로그인 응답의 `token`과 `/api/auth/me`를 공통 세션에 연결하며, 인증 정보는 메모리에만 보관합니다. 새로고침하면 다시 로그인합니다.

## 로컬 데모 실행

Java 21, Node.js, npm이 필요합니다. PowerShell 터미널 2개를 사용합니다.
데모는 H2 메모리 DB를 사용하며 서버 종료 시 데이터가 초기화됩니다.

### Backend

```powershell
cd backend
$env:SUBMATE_DEMO_PASSWORD = Read-Host '데모 비밀번호 (12자 이상)'
.\gradlew.bat bootRun --args='--spring.profiles.active=demo'
```

OneDrive/IDE가 build 폴더를 잠근 경우:

```powershell
.\gradlew.bat bootRun --args='--spring.profiles.active=demo' "-PsubmateBuildDir=$env:TEMP/submate-build"
```

서버는 demo 프로필에서 127.0.0.1:8080으로만 실행됩니다.

### Frontend

```powershell
cd frontend
npm ci
$env:VITE_SUBMATE_DEMO = 'true'
npm run dev -- --port 5173 --strictPort
```

[SubMate 로컬 화면](http://127.0.0.1:5173)에서 계정을 선택하고 지정한 비밀번호로 로그인합니다.

| 계정 | 용도 |
|---|---|
| user@submate.test | 일반 회원 |
| other@submate.test | 다른 회원 데이터 격리 확인 |
| admin@submate.test | 관리자 |

상품 1은 Cinema(월 12,900원), 상품 2는 Workspace(연 99,000원)입니다.
실제 돈을 청구하지 않으며 카드번호/계좌번호를 받지 않습니다.
인증은 메모리에만 보관하므로 페이지 새로고침 시 다시 로그인합니다.
데모 로그인 UI는 개발 서버에서만 활성화됩니다.

## MySQL / JWT 통합

1. 실제 members/products 테이블을 먼저 준비합니다.
2. 신규 거래 스키마에는 `backend/src/main/resources/db/subscription-schema.sql`을 적용합니다. 외부 구독 기능을 추가하려면 `backend/src/main/resources/db/migrations/20261109-tracked-subscriptions.sql`을 한 번 적용합니다. 비어 있는 로컬 검증용 DB는 먼저 `db/demo-catalog.sql`로 회원·카테고리·상품 테이블을 만들 수 있습니다.
3. DB_USERNAME, DB_PASSWORD 환경변수를 지정합니다. 기본 DB는 localhost:3306/submate이며, 다른 주소는 DB_URL로 설정합니다.
4. demo 프로필 없이 실행하면 병합된 JWT 필터를 사용합니다.
5. Authentication.name=회원 이메일, 권한=ROLE_USER/ROLE_ADMIN 규약을 지킵니다.
6. Frontend의 VITE_SUBMATE_DEMO를 제거하거나 false로 실행합니다. 외부 인증 화면에서 세션을 전달할 경우 기존 `<App session={{accessToken,email,role}} onLogout={logout} />` 규약도 유지합니다.
7. 상품의 신청 링크는 /subscribe/{productId}입니다.

현재 전역 SecurityConfig에는 JWT 필터가 연결되어 있습니다.

JPA 기본 DDL 정책은 validate입니다. 검토된 DDL을 먼저 적용해야 합니다.
DB_DDL_AUTO로 개발 환경 설정을 변경할 수 있지만 자동 update만으로 FK/CHECK를 보장하지 않습니다.

## 검증 명령

```powershell
cd backend
.\gradlew.bat test
# build 잠금 환경에서는:
.\gradlew.bat test "-PsubmateBuildDir=$env:TEMP/submate-test-build"
```

```powershell
cd frontend
npm test
npm run build
npm run lint
```

## 문서

- [프론트엔드 공통 스타일 가이드](docs/frontend-guide.md)
- [김영훈 작업 순서와 진행 상태](docs/younghoon-work-plan.md)
- [7일 작업 결과 / 인수인계](docs/week-1.md)
- [API 명세](docs/api-spec.md)
- [DB / ERD](docs/erd.md)
- [구조 / 연결 규약](docs/architecture.md)
- [테스트 시나리오](docs/test-scenarios.md)
- [실제 검증 결과](docs/verification.md)
- [팀 개발 규칙](docs/conventions.md)
- [팀 Git 전략](docs/git-strategy.md)

## 범위와 제한

- 실패한 최초 가상 결제는 오류만 반환하고 구독/결제 이력은 저장하지 않습니다.
- 환불은 결제당 1회 요청, 일할 금액 승인 또는 반려입니다.
- 실제 PG·정기 자동 결제·임의 금액 환불은 포함하지 않습니다.
- 결제일 알림은 외부 구독 대시보드에서 확인할 수 있습니다.
