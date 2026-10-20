# SubMate

React + Vite / Spring Boot / MySQL 기반 2인 팀 구독 관리 프로젝트입니다.

## 구현한 기능

- 구독·결제 원자적 생성, 서버 가격 검증, 중복 요청 방지
- 본인 구독 목록·상세, 해지, 한국 시간 기준 자동 만료
- 결제 내역, 환불 요청 및 관리자 일할 금액 승인·반려
- 사용자/관리자 화면과 API 연동
- 권한·동시성·경계 날짜·트랜잭션 통합 테스트

회원가입·JWT·상품 CRUD는 김현빈 담당 연결 대상으로 남아 있습니다.

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
2. backend/src/main/resources/db/subscription-schema.sql을 검토하고 최초 한 번 적용합니다.
3. DB_USERNAME, DB_PASSWORD 환경변수를 지정합니다. 기본 DB는 localhost:3306/submate입니다.
4. demo 프로필 없이 실행하고 팀 JWT 필터를 연결합니다.
5. Authentication.name=회원 이메일, 권한=ROLE_USER/ROLE_ADMIN 규약을 지킵니다.
6. 실제 로그인 세션을 App에 전달합니다: `<App session={{accessToken,email,role}} onLogout={logout} />`.
7. 상품의 신청 링크는 /subscribe/{productId}입니다.

현재 전역 SecurityConfig는 JWT 미구현 상태입니다.
이번 API는 CurrentMember에서 인증/관리자 권한을 검증하지만, 전체 서비스 인증 완료를 의미하지 않습니다.

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
npm run build
npm run lint
```

## 문서

- [프론트엔드 공통 스타일 가이드](docs/frontend-guide.md)
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
- 실제 회원/상품/JWT 및 MySQL 통합 검증과 팀원 리뷰는 후속 작업입니다.
