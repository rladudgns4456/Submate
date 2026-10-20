# SubMate 노션 교체용 전체 본문

페이지별 전체 본문입니다. 각 제목 구간을 해당 노션 페이지에 복사합니다. 제안/현재 구현 구분은 각 문서의 표시를 따릅니다.

# SubMate 프로젝트 개요

## 1. 프로젝트 소개

**SubMate는 학습·공부·강의 콘텐츠를 구독하고, 강의 수강과 학습 진도 및 구독·결제·환불을 관리하는 웹 서비스입니다.**

사용자는 관심 분야의 강의를 살펴보고 해당 강의를 포함한 학습 이용권을 가상 결제로 구독합니다. 구독 이용 기간에는 강의를 학습하고, 내 학습에서 진도와 이어 학습할 차시를 확인하는 것을 목표로 합니다.

기존 구독 관리 기능에 실제로 이용할 학습 콘텐츠를 연결하여 **강의 탐색 → 구독 → 학습 → 진도 관리**의 서비스 경험을 구성합니다. 외부 플랫폼 계정이나 실제 이용권을 연동하지 않습니다.

## 2. 기본 정보

| 항목 | 내용 |
|---|---|
| 프로젝트명 | SubMate |
| 서비스 유형 | 학습·강의 콘텐츠 구독 서비스 |
| 개발 형태 | 2인 팀 프로젝트 |
| 팀장 / 팀원 | 김영훈 / 김현빈 |
| 사용자 | 비회원, 일반 회원, 관리자 |
| 결제 | 실제 금전 이동 없는 가상 결제·환불 |
| 이용권 | 월간 또는 연간 기간형 상품, 최초 1회 결제 |
| Frontend | React, Vite, React Router, JavaScript, CSS |
| Backend | Spring Boot, Spring Security, Spring Data JPA |
| 인증 목표 | JWT 기반 인증·인가 |
| DB | 통합 목표 MySQL, 현재 데모·테스트 H2 |

## 3. 핵심 개념

| 개념 | 의미 | 진행 구분 |
|---|---|---|
| Member | 회원과 권한 | 실제 회원/JWT 통합 필요 |
| Category | 학습 분야 분류 | 팀원 영역, 현재 통합 미확인 |
| Product | 구독 가격·기간과 포함 콘텐츠를 안내하는 이용권 상품 | 최소 읽기 계약 연결, 정식 상품 통합 필요 |
| Course | 강의 소개·학습 목표·커리큘럼 | 신규 개발 |
| Lesson | 강의에 속한 차시와 학습 콘텐츠 | 신규 개발 |
| LearningProgress | 회원별 차시 완료·최근 학습 기록 | 신규 개발 |
| Subscription | 상품 이용 기간·해지·만료 | 구현 |
| Payment / Refund | 가상 결제와 일할 환불 | 구현 |

Product와 Course를 N:M으로 연결해 한 상품에 여러 강의를 포함하는 방식을 제안합니다. 이 연결 방식은 팀 합의 후 확정합니다.

## 4. 사용자 기능

- 비회원: 공개 상품·강의 목록과 소개 확인, 회원가입·로그인.
- 회원: 구독 신청, 가상 결제, 본인 구독·결제·환불 내역 확인.
- 학습: 이용 가능한 강의 수강, 차시 완료 저장, 내 학습과 이어 학습하기.
- 구독 관리: 해지 신청, 종료일 확인, 일할 환불 요청·결과 확인.
- 관리자: 회원·상품·카테고리·강의·차시 관리, 구독·결제 조회, 환불 승인·반려.
- 공지사항·운영 통계: 핵심 기능 통합 이후 공동 구현.

학습 관련 화면과 서버 API는 신규 계획이며 현재 구현되지 않았습니다.

## 5. 기본 흐름

```text
강의 탐색 → 강의 상세·커리큘럼 확인
→ 해당 강의를 포함하는 상품 선택 → 로그인
→ 구독 신청·가상 결제 → ACTIVE 구독 생성
→ 수강 권한 검사 → 차시 학습 → 진도 저장 → 이어 학습하기
```

구독과 성공 결제는 하나의 트랜잭션에서 생성합니다. 실패한 최초 결제는 오류를 반환하고 구독·결제 행을 저장하지 않습니다.

## 6. 구독·환불 정책 — 현재 구현 기준

| 항목 | 기준 |
|---|---|
| 기간 | MONTHLY는 시작일 +1개월, YEARLY는 +1년 |
| 날짜 | Asia/Seoul, DATE 기반 [startDate, endDate) |
| 종료 경계 | endDate 당일 00:00 KST부터 이용 불가 |
| 이용 가능 | ACTIVE 또는 CANCELLED이며 이용 기간 내 |
| 해지 | ACTIVE → CANCELLED, 기존 종료일까지 이용 |
| 만료 | ACTIVE/CANCELLED → EXPIRED, 스케줄러 매 분 처리 |
| 중복 구독 | 동일 회원·상품의 남은 유효 구독이 있으면 차단 |
| 재구독 | 기존 이용 기간 종료 후 새로운 구독 생성 |
| 환불 요청 | 잔여 기간이 있는 정상 결제에 대해 결제당 1회 |
| 환불 금액 | 결제금액 × 요청일 기준 잔여 일수 ÷ 전체 일수, 원 미만 버림 |
| 승인 | 요청 시 저장한 금액 환불, 결제 REFUNDED, 구독 EXPIRED·즉시 이용 종료 |
| 반려 | 기존 결제·구독 유지, 동일 결제 재요청 불가 |

위 환불은 프로젝트의 가상 환불 규칙입니다. 학습 시간·진도율 기반 차감은 현재 정책에 포함하지 않습니다.

## 7. 개발 범위와 우선순위

1. 실제 회원·JWT·상품 및 MySQL 연결.
2. Product/Course 관계와 콘텐츠 제공 방식 합의.
3. 강의 탐색·상세·차시 관리 구현.
4. 서버 수강 권한·진도·내 학습 구현.
5. 구독·결제·학습·해지·환불 전체 시나리오 검증.
6. 공지사항·대시보드·배포·발표 준비.

실제 PG, 자동 갱신·정기 청구, 외부 플랫폼 계정 연동은 제외합니다. 영상 업로드·인코딩·스트리밍, AI 추천·튜터, 실시간 강의, 수료증, 질문·답변·댓글은 후속 확장 제안입니다.

## 8. 완료 기준과 현재 상태

현재 로컬에는 구독·가상 결제·해지·만료·일할 환불 및 사용자/관리자 화면이 구현되어 있습니다. 기본 기능 PR #2와 일할 환불 PR #3의 병합 기록을 확인했습니다.

기존 검증 문서에는 백엔드 27건과 프론트 빌드·린트 통과가 기록되어 있습니다. 실제 JWT·MySQL 및 신규 학습 기능을 포함한 전체 서비스 완료를 뜻하지 않습니다.

최종 완료 기준은 회원가입부터 강의 수강·진도 저장·해지 후 잔여 이용·만료 또는 환불 후 접근 차단까지 서버와 화면에서 일관되게 동작하는 것입니다.

## 9. 프로젝트 목적과 개발 경험

학습 콘텐츠를 실제로 이용하는 과정에 구독 상태와 권한을 연결하여 단순 회원·상품 CRUD를 넘어서는 서비스 흐름을 구현합니다.

- JWT·Spring Security 인증과 USER/ADMIN 인가.
- 공개 강의 소개와 구독자 전용 콘텐츠의 접근 경계.
- 구독·결제 동시 저장과 실패 시 롤백.
- 재요청·동시 요청의 중복 거래·중복 진도 방지.
- 월말·윤년·한국 날짜 경계와 스케줄러 처리.
- 해지와 환불의 독립적인 상태 및 이용 권한 변경.
- 회원별 학습 진도와 최근 학습 이력 저장.
- React·Spring Boot·MySQL 통합과 테스트.
- 2인 도메인 분담, API/DB 계약, PR 리뷰와 문서화.

학습 서비스라는 사용자 목적과 이를 설명할 수 있는 업무 규칙을 함께 완성하는 것이 목표입니다.

## 10. 전체 범위·확장·제외

| 구분 | 기본 목표 | 현재 로컬 상태 |
|---|---|---|
| 회원 | 가입·로그인·정보 조회/수정 | 실제 회원/JWT 통합 필요 |
| 상품 | 이용권 목록·상세·분류·판매 상태 | 최소 읽기 계약, 정식 CRUD 통합 필요 |
| 강의 | 분야별 탐색·소개·커리큘럼 | 신규 계획 |
| 수강 | 권한 검사·차시 콘텐츠 | 신규 계획 |
| 학습 기록 | 완료·진도·이어 학습 | 신규 계획 |
| 구독 | 신청·목록·상세·해지·만료 | 구현 |
| 결제 | 가상 성공/실패·내역·중복 방지 | 구현 |
| 환불 | 일할 요청·승인/반려·상태 연결 | 구현 |
| 관리자 | 회원·상품·강의·구독·거래 관리 | 거래 영역 구현, 나머지 통합/신규 |
| 게시판 | 공지사항 공개 조회·관리자 CRUD 제안 | 후속 |
| 대시보드 | 회원·상품·강의·거래 지표 | 후속 |

일반 게시판·작성자 CRUD는 원본의 확장 요구로 보존합니다. 기본 공지사항과 혼합하지 않고 핵심 수강 흐름 뒤에 범위를 합의합니다. 영상 업로드·스트리밍, 실시간 수업, 수료증·시험, AI 추천·튜터는 기본 범위에 자동 포함하지 않습니다.

환불은 잔여 기간에 따라 결제금액 일부가 될 수 있습니다. 원문의 ‘부분 환불 제외’는 **임의 금액 지정·진도율별 환불 제외**로 정정합니다. 현재 일할 환불을 제외한다는 의미로 사용하지 않습니다.

## 11. 페이지·기능별 담당

| 페이지/기능 | 핵심 동작 | 담당 | 구분 |
|---|---|---|---|
| 회원가입·로그인 | 계정 검증·JWT·공통 세션 | 김현빈 | 기존 |
| 회원정보·관리자 회원 관리 | 본인 수정·관리자 조회/관리 | 김현빈 | 기존 |
| 상품 목록·상세 | 가격·기간·카테고리 | 김현빈 | 기존 |
| 관리자 상품·카테고리 | CRUD·비활성·삭제 제한 | 김현빈 | 기존 |
| 강의 목록·상세 | 분야·소개·학습 목표·커리큘럼 | 김현빈 | 신규 배분 제안 |
| 관리자 강의·차시 | 콘텐츠·순서·공개·상품 연결 | 김현빈 | 신규 배분 제안 |
| 구독 신청·결제·완료 | 견적·가상 결제·결과 | 김영훈 | 기존 |
| 내 구독·상세 | 기간·상태·해지 | 김영훈 | 기존 |
| 결제·환불 내역 | 거래 조회·환불 요청 | 김영훈 | 기존 |
| 관리자 구독·거래 관리 | 해지·조회·승인/반려 | 김영훈 | 기존 |
| 내 학습·진도 | 완료·최근/이어 학습 | 김영훈 | 신규 배분 제안 |
| 수강 화면 | 콘텐츠·권한·진도 연결 | 공동 | 신규 배분 제안 |
| 공지사항 | 관리자 CRUD·공개 조회 | 공동 | 후속 분담 |
| 대시보드 | 도메인별 지표·기간 집계 | 공동 | 후속 분담 |

학습 콘텐츠 추가 담당은 기존 역할 경계에 따른 제안입니다. 실제 업무 배정·일정은 팀 합의 후 확정합니다.

## 12. 시스템 구성·데이터 설계

```text
React / Vite
  ├─ 공개 강의·상품 소개 (신규/통합)
  ├─ 사용자 구독·거래 (현재)
  ├─ 수강·진도 (신규)
  └─ 관리자
        ↓ REST API
Spring Boot
  ├─ Member / Auth / Product / Category (통합)
  ├─ Subscription / Payment / Refund (현재)
  ├─ Course / Lesson / Learning (신규)
  └─ Post / Statistics (후속)
        ↓ JPA / JDBC
MySQL (통합 목표) / H2 (현재 demo/test)
```

회원·상품은 ID로 거래와 연결하고 강의는 차시를 포함합니다. 상품의 현재 가격과 결제 당시 금액, 상품 설정과 구독 확정 기간을 분리합니다. 원결제는 Refund와 최대 1:1 관계입니다. LearningProgress는 회원·차시별 기록으로 제안합니다.

상품명은 현재 Gateway에서 조회하므로 이름의 과거 스냅샷이 구현된 것은 아닙니다. 이용권의 강의 구성도 현재 계약 스냅샷이 없으므로 기존 구독의 포함 강의 보존 정책을 별도 합의해야 합니다.

## 13. 보안·품질·검수 기준

| 검수 | 통과 기준 | 담당 |
|---|---|---|
| 인증 | 잘못된 계정·변조/만료 토큰 거절 | 김현빈 주도 |
| 관리자 | 일반 사용자의 관리자 API 차단 | 공동 |
| 소유자 | 타인 회원·거래·진도 조회/수정 차단 | 공동 |
| 공개 콘텐츠 | 유료 본문/비공개 영상 링크 미노출 | 김현빈 배분 제안 |
| 결제 | 성공 시 두 행 생성·실패 시 미생성·중복 방지 | 김영훈 |
| 날짜 | 월말·윤년·종료일 자정·스케줄러 지연 | 김영훈 |
| 해지 | CANCELLED 전환·잔여 이용·중복 차단 | 김영훈 |
| 환불 | 일할 계산·승인 지연·한 번 요청·일괄 변경 | 김영훈 |
| 수강 | 유효 이용권·강의 포함·만료/환불 차단 | 공동 |
| 진도 | 서버 저장·타인 격리·중복/동시 저장 | 김영훈 배분 제안 |
| 이력 | 상품 변경·탈퇴·비공개 후 보존 정책 준수 | 공동 |
| 통계 | 조회 기간·상태·원본 데이터와 일치 | 공동 |
| UI | 공통 스타일·로딩/오류/빈 상태·모바일 | 공동 |

이 표는 목표 검수 기준이며 전부 통과했다는 의미가 아닙니다.

## 14. 개발·협업·문서 체계

담당자가 도메인 설계·API·화면·테스트·문서를 함께 관리합니다. API/DB 변경은 요청·응답·마이그레이션·영향 화면을 공유하고 실제 코드와 동기화합니다. 기능 브랜치에서 작업하고 develop에 PR로 통합한 뒤 검증 버전을 main에 반영합니다.

프로젝트 문서: 프로젝트 개요 / 요구사항·개발 요청 프롬프트 / R&R / 초기 실행 / DB·ERD / API / 파일 구조 / Git / 개발 규칙 / 프론트엔드 가이드 / 진행·검증 결과 / 테스트·시연 / 발표 자료.

문서는 계획을 표현할 수 있지만 검증 결과는 명령·환경·날짜·근거를 남깁니다. 백엔드 27건은 2026-09-28 기록이고 이번 문서 갱신의 신규 실행 결과가 아닙니다.

## 15. 완료 목표·선행 결정

최종 시연은 강의 발견부터 실제 학습과 거래 관리까지 이어져야 합니다. 화면 이동만으로 완료 판정하지 않고 서버 저장·재조회·권한 차단을 확인합니다.

선행 합의: 상품-강의 연결, 기존 구독의 포함 강의 변경 정책, 차시 콘텐츠 형식, 완료/미리보기 기준, 신규 담당 배분. 이후 회원/JWT/MySQL 통합 → 콘텐츠 → 수강·진도 → 거래/학습 E2E → 후속 기능 순으로 진행합니다.


---

# SubMate 초기 실행 가이드 — Windows / PowerShell

## 1. 실행 모드 구분

| 모드 | 목적 | DB / 인증 |
|---|---|---|
| 로컬 데모 | 현재 구현한 구독·결제·환불 확인 | H2 / demo 전용 Basic 인증 |
| 팀 통합 | 실제 회원·상품·JWT 연결 | MySQL / 팀 JWT 연결 필요 |

처음 실행할 때는 로컬 데모부터 확인합니다. 현재 데모에 강의 수강·진도 기능은 없습니다.

## 2. 준비

Git, Java 21, 프로젝트 의존성의 요구 버전과 맞는 Node.js/npm, 편집기가 필요합니다. MySQL은 팀 통합 단계에서 준비합니다.

```powershell
git --version
java -version
node -v
npm -v
```

Java는 21을 사용합니다. Node의 구체적인 지원 범위는 설치할 프로젝트의 package-lock 및 Vite 패키지 engines를 확인합니다.

저장소의 실제 HTTPS 주소로 clone합니다.

```powershell
git clone <저장소-URL>
cd <복제된-폴더>
git switch develop
git pull --ff-only origin develop
code .
```

clone이 필요한 경우에만 실행합니다. 기존 작업 폴더에서는 미커밋 변경을 먼저 확인합니다. 문서의 `<...>`는 실제 값으로 바꾸는 표시입니다.

## 3. Backend 데모 실행

프로젝트 루트에서 PowerShell 첫 번째 터미널을 엽니다.

```powershell
cd backend
$env:SUBMATE_DEMO_PASSWORD = Read-Host '데모 비밀번호 (12자 이상)'
.\gradlew.bat bootRun --args='--spring.profiles.active=demo'
```

demo 프로필은 `127.0.0.1:8080`에 실행되고 H2 메모리 DB를 초기화합니다. 서버 종료 후 데이터가 사라집니다. 실제 비밀번호나 재사용하는 개인 비밀번호를 데모 값으로 쓰지 않습니다.

OneDrive/IDE가 build 폴더를 잠근 경우:

```powershell
.\gradlew.bat bootRun --args='--spring.profiles.active=demo' "-PsubmateBuildDir=$env:TEMP/submate-build"
```

## 4. Frontend 실행

프로젝트 루트에서 두 번째 PowerShell 터미널을 엽니다.

```powershell
cd frontend
npm ci
$env:VITE_SUBMATE_DEMO = 'true'
npm run dev -- --port 5173 --strictPort
```

브라우저에서 `http://127.0.0.1:5173`을 엽니다. 프론트의 `/api` 요청은 Vite 개발 프록시를 통해 `http://127.0.0.1:8080`으로 전달됩니다.

| 데모 계정 | 용도 |
|---|---|
| user@submate.test | 일반 사용자 |
| other@submate.test | 다른 회원 데이터 격리 |
| admin@submate.test | 관리자 |

서버 시작 전에 지정한 데모 비밀번호로 로그인합니다. 페이지 새로고침 시 메모리 인증이 초기화됩니다. 데모 로그인 UI는 개발 서버에서만 활성화됩니다.

현재 샘플 상품 Cinema/Workspace는 기존 구독 검증용 데이터입니다. 학습 서비스 방향으로 문서를 바꾸었다고 샘플 데이터가 자동으로 강의 상품으로 변경되지는 않습니다.

## 5. 데모 확인 순서

1. 일반 사용자로 구독 신청·가상 결제 성공 확인.
2. 내 구독·결제 목록과 시작/종료일 확인.
3. 해지 후 CANCELLED와 종료일까지 이용 가능 안내 확인.
4. 잔여 기간에 환불 요청, 관리자 승인·반려 확인.
5. 승인 시 환불 완료·결제 REFUNDED·구독 이용 종료 확인.
6. 다른 회원으로 로그인하여 타인 거래 미노출 확인.
7. 실패 결제에서 구독·결제 행이 생성되지 않는지 확인.

## 6. 실제 MySQL·JWT 통합

이 단계는 데모 실행과 별도입니다. DB 생성만으로 현재 정식 회원·상품·JWT 통합이 끝나지 않습니다.

```sql
CREATE DATABASE IF NOT EXISTS submate
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

1. 팀에서 실제 members/products 테이블을 준비합니다. 현재 CatalogGateway가 읽는 컬럼과 대조합니다.
2. 신규 DB는 `backend/src/main/resources/db/subscription-schema.sql`을 검토 후 적용합니다.
3. 기존 구독 DB는 현재 스키마를 먼저 확인하고 필요한 마이그레이션만 적용합니다. 기존 DB에 최초 설치 DDL을 반복 실행하지 않습니다.
4. 일할 환불 컬럼이 없는 기존 DB에는 `db/migrations/20260923-prorated-refunds.sql` 적용 여부를 확인합니다.
5. 해당 Backend 터미널에 DB 계정 환경변수를 설정합니다.

```powershell
$env:DB_USERNAME = Read-Host 'DB 사용자명'
$env:DB_PASSWORD = Read-Host '로컬 DB 비밀번호'
.\gradlew.bat bootRun
```

기본 URL은 `jdbc:mysql://localhost:3306/submate`이며 설정 파일은 `application.yaml`입니다. 비밀번호를 코드·커밋·노션에 기록하지 않습니다.

JPA 기본 정책은 `validate`입니다. 빈 DB에서 자동으로 모든 테이블을 생성하지 않습니다. `DB_DDL_AUTO=update`만으로 FK/CHECK·기존 데이터 마이그레이션을 보장하지 않습니다.

현재 일반 SecurityConfig에는 실제 JWT 구현이 연결되지 않았습니다. 김현빈 담당 인증을 연결하고 Authentication.name=회원 이메일, ROLE_USER/ROLE_ADMIN을 통일해야 합니다.

Frontend의 실제 세션 연결:

```jsx
<App session={{ accessToken, email, role }} onLogout={logout} />
```

정식 Frontend 실행 전에는 데모 변수를 제거합니다.

```powershell
Remove-Item Env:VITE_SUBMATE_DEMO -ErrorAction SilentlyContinue
npm run dev -- --port 5173 --strictPort
```

Vite 프록시는 개발 서버 설정입니다. 배포 시 `/api` 연결은 배포 환경에서 별도 구성합니다.

## 7. 검증 명령

Backend 폴더:

```powershell
.\gradlew.bat test
```

build 잠금이 있다면:

```powershell
.\gradlew.bat test "-PsubmateBuildDir=$env:TEMP/submate-test-build"
```

Frontend 폴더:

```powershell
npm run build
npm run lint
```

검사 통과와 실제 JWT·MySQL·브라우저 E2E 통과는 구분해 기록합니다. 현재 검증 범위와 날짜는 저장소 `docs/verification.md`를 확인합니다.

## 8. 자주 확인할 오류

| 증상 | 확인 |
|---|---|
| Java 버전 오류 | Java 21과 JAVA_HOME |
| DB 접속 실패 | MySQL 실행·3306 포트·DB 계정·환경변수 |
| 스키마 검증 실패 | 필요한 테이블·컬럼·DDL 적용 여부 |
| 데모 로그인 실패 | demo 프로필·개발 UI 변수·동일 비밀번호 |
| API 연결 실패 | Backend 8080·Vite 프록시·127.0.0.1:5173 |
| build 파일 삭제 실패 | 실행 중 IDE/서버 잠금·submateBuildDir |
| 새로고침 후 로그인 해제 | 현재 메모리 세션 방식의 동작 |

## 9. 처음 설치하는 팀원을 위한 준비 절차

| 프로그램 | 설치 목적 | 설치 후 확인 |
|---|---|---|
| Git | 저장소 clone·commit·PR | git --version |
| VS Code 또는 사용하는 IDE | 코드 편집·터미널 | 프로젝트 폴더 열기 |
| JDK 21 | Gradle/Spring Boot | java -version, javac -version |
| Node.js와 npm | Vite·React 개발 | node -v, npm -v |
| MySQL Server 8.x | 정식 통합 DB | 서비스 실행·Workbench 접속 |
| MySQL Workbench | DB 생성·DDL 확인 | 로컬 연결 성공 |

Git/Java/Node를 설치한 뒤 기존 터미널에서는 PATH가 갱신되지 않을 수 있으므로 PowerShell을 새로 엽니다. Git 명령 자체가 인식되지 않는 경우와 GitHub 인증 실패는 다른 문제입니다.

Java가 17이나 11로 표시되면 설치된 JDK 중 21이 PATH/JAVA_HOME에서 선택되도록 설정합니다. Backend 폴더에서 다음으로 Gradle JVM도 확인합니다.

```powershell
.\gradlew.bat --version
```

Node/npm 숫자가 출력되어도 패키지 최소 요구 버전을 만족하는지 확인합니다. package-lock.json은 팀에서 합의한 의존성 버전 기록이므로 실행 오류를 피하려고 임의 삭제하지 않습니다.

## 10. 저장 위치·Clone·브랜치·편집기 상세

새 설치는 GitHub 저장소의 Code → HTTPS에서 실제 주소를 복사합니다. 저장할 부모 폴더에서 clone하고 생성된 폴더로 이동합니다. 바탕화면이 OneDrive 경로라면 `cd Desktop`을 고정해서 사용하지 말고 자신의 실제 경로를 선택합니다.

```powershell
git clone <실제-HTTPS-주소>
cd <clone으로-생성된-폴더>
git branch --show-current
git branch -a
git switch develop
git pull --ff-only origin develop
code .
```

원격에는 develop이 있는데 로컬 추적 브랜치가 생성되지 않는 경우에만 다음을 사용합니다.

```powershell
git switch --track origin/develop
```

`git branch`에서 `*`는 현재 브랜치입니다. 편집기에서 backend와 frontend가 동시에 보여야 프로젝트 루트를 연 것입니다. code 명령이 없으면 편집기 메뉴의 ‘폴더 열기’를 사용해도 됩니다.

이미 clone한 폴더에 새 clone을 겹쳐 만들지 않습니다. 기존 작업에서 `git status`에 변경이 있으면 임의 초기화하지 말고 작업을 보존한 뒤 브랜치를 전환합니다.

## 11. 환경변수의 범위와 확인

권장 예시는 `$env:...`로 **현재 PowerShell 프로세스**에 값을 설정합니다. 그 터미널에서 시작한 Backend가 값을 상속합니다. 다른 터미널이나 IDE Run 버튼은 해당 값을 자동으로 공유하지 않습니다.

```powershell
$env:DB_USERNAME = Read-Host '로컬 DB 사용자명'
$env:DB_PASSWORD = Read-Host '로컬 DB 비밀번호'
Test-Path Env:DB_USERNAME
Test-Path Env:DB_PASSWORD
```

Test-Path의 True는 값이 존재한다는 뜻이지 DB 접속 성공을 보장하지 않습니다. 비밀번호 실제 값을 화면에 출력하거나 오류 보고에 포함하지 않습니다. Read-Host는 비밀 저장소가 아니므로 화면 공유 중 비밀번호를 입력하지 않습니다.

원문 `setx`는 새로 시작하는 프로세스에 반영되는 영구 사용자 환경변수 방식입니다. 기존 PowerShell/VS Code에서는 즉시 반영되지 않을 수 있습니다. 이 가이드에서는 비밀번호를 영구 저장하거나 명령 이력에 남기는 단계를 필수로 요구하지 않습니다. IDE에서 실행한다면 IDE의 비공개 실행 설정에 값을 지정하고 커밋되지 않도록 확인합니다.

현재 application.yaml:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/submate?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: ${DB_DDL_AUTO:validate}
```

설정 파일을 직접 수정해 팀원의 DB 비밀번호를 복사하지 않습니다. 값 제거가 필요하면 현재 터미널에서 `Remove-Item Env:DB_PASSWORD -ErrorAction SilentlyContinue`를 사용할 수 있습니다.

## 12. Workbench·DB·스키마 준비 상세

1. MySQL Server를 설치하고 로컬 서비스가 실행 중인지 확인합니다. Windows 서비스 이름은 설치에 따라 다르며 MySQL80은 예시입니다.
2. Workbench에서 실제 host/port/계정으로 접속합니다. 기본 포트는 3306입니다.
3. 새 SQL 탭에서 6절의 CREATE DATABASE IF NOT EXISTS를 실행합니다.
4. SCHEMAS를 새로고침하여 submate를 확인하고 기본 스키마로 선택하거나 `USE submate;`를 실행합니다.
5. 실제 members/products 스키마를 담당자와 준비합니다. 최소 필드만 임시로 만든 것을 정식 회원 기능 구현 완료로 간주하지 않습니다.
6. 신규 DB인지 기존 DB인지 판단하고 맞는 DDL/마이그레이션을 적용합니다.
7. `SHOW TABLES;`와 테이블 정의를 확인합니다.

```sql
USE submate;
SHOW TABLES;
SHOW CREATE TABLE subscriptions;
SHOW CREATE TABLE payments;
SHOW CREATE TABLE refunds;
```

마지막 세 명령은 테이블을 적용한 뒤 실행합니다. 원문처럼 DB만 생성한 상태에서는 기본 validate로 Backend가 실행되지 않을 수 있습니다. FOREIGN KEY, UNIQUE, CHECK와 환불 일수 컬럼까지 확인해야 합니다.

root 계정은 로컬 초기 관리에 사용할 수 있지만 팀 공용 비밀번호를 만들지 않습니다. 별도 애플리케이션 계정·권한 관리는 팀 환경에 맞춰 설정합니다.

## 13. 의존성 설치와 실행 터미널

Git에는 node_modules를 저장하지 않습니다. package.json과 package-lock.json을 읽어 의존성을 재현합니다. 첫 실행과 lockfile 변경을 받은 뒤에는 Frontend 폴더에서 `npm ci`를 사용합니다. 새 라이브러리를 의도적으로 추가할 때는 담당자와 공유한 뒤 npm install <패키지>로 package/lock을 함께 변경합니다.

Backend의 Gradle wrapper도 첫 실행 때 의존성을 내려받을 수 있습니다. 다운로드 실패와 Java/컴파일 오류를 구분합니다.

| 터미널 | 위치 | 데모 실행 |
|---|---|---|
| 1 | backend | 비밀번호 환경변수 설정 후 demo bootRun |
| 2 | frontend | VITE_SUBMATE_DEMO=true 후 Vite |

터미널이 계속 실행 중인 것은 서버가 살아 있기 때문입니다. 종료는 해당 터미널에서 Ctrl+C입니다. Frontend만 종료해도 Backend가 자동 종료되지는 않습니다.

정식 통합에서는 Backend demo 프로필과 Frontend demo 변수를 모두 제거하고 실제 JWT 세션을 연결해야 합니다. 두 모드를 한쪽만 바꾸면 로그인/API 동작이 맞지 않을 수 있습니다.

## 14. 실행 성공을 판단하는 방법

Backend 로그에서 Tomcat의 8080 포트 시작과 BackendApplication 시작을 확인합니다. Frontend에서는 Vite가 안내하는 `http://127.0.0.1:5173` 주소를 확인합니다.

포트가 사용 중이면 strictPort 옵션 때문에 실패할 수 있습니다. 다른 프로그램을 무조건 종료하지 말고 이미 실행 중인 SubMate 서버인지 먼저 확인합니다. 임의로 Frontend 포트를 바꾸면 CORS 허용 Origin도 대조해야 합니다.

원문에 있던 **‘SubMate Backend 연결 성공!’ 고정 문구는 현재 화면의 검증 기준이 아닙니다.** 현재는 로그인 후 실제 구독/결제 목록 요청과 동작을 확인합니다. 개발자 도구 Network에서 요청 URL·HTTP 상태·응답을 확인하되 인증 헤더를 공유하지 않습니다.

HTML 화면이 열렸다는 것만으로 DB 연결을 증명하지 못합니다. 실제 API로 읽기와 저장을 수행하고 화면 반영까지 확인합니다. 기본 `/` URL의 404/리다이렉트가 곧 서버 기동 실패라는 뜻도 아닙니다.

## 15. 오류별 확인 순서

| 오류·증상 | 확인 순서 |
|---|---|
| git/java/node 명령 인식 안 됨 | 설치 → PATH → 새 터미널 |
| 잘못된 Java 버전 | java/javac → JAVA_HOME → Gradle JVM |
| npm ci 실패 | lockfile 존재/일치 → Node 요구 버전 → 네트워크/권한 |
| Access denied | 로컬 DB 사용자·비밀번호·허용 접속 위치 |
| Communications link failure | MySQL 서비스·호스트·3306·연결 설정 |
| Unknown database | submate 생성 여부·대상 서버 |
| Schema-validation 오류 | members/products와 거래 테이블·컬럼·타입·DDL |
| demo 비밀번호 설정 오류 | 같은 터미널의 변수·12자 이상 |
| 401 | 인증 방식·비밀번호·토큰·프로필 일치 |
| 403 | ADMIN 권한과 DB role·CORS 구분 |
| 404 | 실제 API 경로·타인 자원 은닉·미구현 경로 여부 |
| 409 | 중복 구독·이미 해지/환불 처리·판매 상태·동시성 |
| API 프록시 연결 실패 | Backend 실행·8080 → Vite proxy → 요청 /api |
| 파일 잠금 | 기존 서버/IDE 잠금 확인 → 별도 submateBuildDir |
| 새로고침 시 로그인 해제 | 메모리 인증 방식 확인 |

오류를 피하려고 DDL validate를 무조건 update/create로 바꾸거나 기존 DB를 삭제하지 않습니다.

## 16. 기능별 검증 시나리오와 범위

현재 데모에서는 구독 신청 성공·실패, 동일 요청 키, 다른 키 중복 구독, 본인/타인 목록, 해지 후 잔여 이용, 환불 요청과 승인/반려를 확인할 수 있습니다. 자동 만료의 날짜 경계는 테스트 Clock으로 검증한 기록이 있습니다.

학습 확장 후에는 강의 공개 조회 → 이용권 선택 → 실제 JWT 로그인 → 결제 → 콘텐츠 수강 → 진도 저장 → 만료/환불 후 접근 차단을 추가합니다. 이 시나리오는 현재 데모에 구현되어 있지 않습니다.

2026-09-28 백엔드 27건 통과는 과거 검증 기록입니다. 현재 실행한 결과는 새 날짜와 명령·환경을 붙여 별도 기록합니다. 이번 문서 작성으로 새 백엔드 검증을 수행한 것으로 표시하지 않습니다.

## 17. 개발 시작 전 Git 작업

```powershell
git status
git switch develop
git pull --ff-only origin develop
git switch -c feature/course-list
```

기존 작업 브랜치는 새로 생성하지 말고 이동합니다. develop을 합칠 필요가 있으면 변경을 보존한 뒤 `git fetch origin`과 `git merge origin/develop`을 사용합니다. 자세한 충돌 처리와 병합 방식은 Git 전략을 따릅니다.

## 18. Commit·Push·PR

```powershell
git status
git diff
git add <변경한-파일>
git diff --cached
git commit -m "feat: 강의 목록 조회 구현"
git push -u origin feature/course-list
```

GitHub에서 해당 브랜치 → develop PR을 만들고 변경 동작·API/DB 영향·테스트 결과·화면을 적습니다. 팀원 검토 후 병합합니다. 본인 브랜치가 아닌 곳에 임의 push하지 않습니다.

| type | 의미 | 예시 |
|---|---|---|
| feat | 새 기능 | feat: 차시 학습 진도 저장 |
| fix | 동작 오류 | fix: 만료 구독 수강 차단 |
| refactor | 동작 유지 구조 개선 | refactor: 수강 권한 서비스 분리 |
| style | 포맷 | style: JSX 들여쓰기 정리 |
| docs | 문서 | docs: 학습 API 명세 추가 |
| test | 테스트 | test: 환불 후 접근 차단 검증 |
| chore | 설정 | chore: 린트 제외 경로 설정 |

## 19. Git에 포함하지 않을 항목

실제 DB 비밀번호, JWT/API Secret, .env 실값, node_modules, backend/build, frontend/dist, 개인 IDE 설정과 임시 리뷰 복사본을 확인합니다. .env.example에는 실제 비밀값 대신 필요한 변수 이름과 예시만 적습니다.

파일을 삭제하거나 .gitignore에 추가하는 것만으로 이미 커밋된 비밀값이 지워지지는 않습니다. 발견한 경우 값 교체와 이력 조치를 팀과 논의합니다.

## 20. 초기 완료 체크리스트

### 프로그램·저장소

- [ ] Git·IDE·Java 21·지원 Node/npm 준비
- [ ] 저장소 clone 및 develop 확인
- [ ] backend/frontend가 있는 루트를 편집기로 열기

### 데모

- [ ] demo Backend 시작·비밀번호 지정
- [ ] npm ci·Frontend demo 변수 지정
- [ ] 127.0.0.1:5173 접속·로그인
- [ ] 실제 API 읽기·결제 저장·내역 반영
- [ ] 타인 격리·실패 결제 확인

### 실제 통합

- [ ] MySQL 실행·submate 생성
- [ ] 실제 회원/상품 스키마와 DDL/마이그레이션 적용
- [ ] 현재 실행 프로세스에 DB 환경변수 전달
- [ ] demo 없이 기동·스키마 검증 통과
- [ ] 실제 JWT와 App 세션 연결
- [ ] MySQL 기반 저장·조회·권한 E2E

### 검사

- [ ] Backend 관련 테스트
- [ ] Frontend build/lint
- [ ] 브라우저 확인 결과를 별도로 기록

## 21. 문제가 생겼을 때 공유할 내용

```text
발생 단계:
현재 브랜치/커밋:
실행 위치와 명령:
demo 또는 실제 통합 모드:
오류 발생 직전 행동:
민감정보를 제거한 오류 로그:
예상 결과 / 실제 결과:
재현 여부:
Git 문제라면 git status:
```

DB 비밀번호·토큰·Authorization·개인정보를 로그/화면에서 제거합니다. ‘안 된다’는 표현만 전달하기보다 실제 명령과 오류 원문을 함께 남깁니다.

## 22. 평소 실행 순서

이미 환경을 갖춘 뒤에는 clone·DB 생성·설치를 매번 반복하지 않습니다.

1. 변경 보존·최신 develop 확인 → 작업 브랜치 이동/생성.
2. 필요한 모드의 Backend 환경변수와 프로필로 시작.
3. Frontend 시작. lockfile이 바뀌었다면 npm ci 재실행.
4. 개발 → 관련 자동 검사 → 실제 화면/API 확인.
5. diff·민감정보·문서 확인 → commit/push → PR.

항상 main/develop 직접 기능 개발을 피하고, 공통 파일·API·DB·스타일 변경을 팀원에게 공유합니다.


---

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


---

# SubMate Git 브랜치 전략

## 1. 기본 구조

2인 팀은 `main + develop + 작업 브랜치`를 사용합니다.

| 브랜치 | 용도 |
|---|---|
| main | 통합 검증을 마친 발표·배포 가능한 버전 |
| develop | 기능 통합 |
| feature/* | 기능 개발 |
| fix/* | 오류 수정 |
| refactor/* | 동작을 유지하는 구조 개선 |
| docs/* | 문서 |
| codex/* | Codex에서 만든 작업 브랜치 |

브랜치는 사람보다 작업 목적을 기준으로 짓습니다. 예: `feature/course-list`, `feature/lesson-management`, `feature/learning-progress`, `fix/refund-amount`, `docs/learning-api`.

## 2. 새 작업 시작

미커밋 변경이 있으면 보존·정리한 뒤 브랜치를 이동합니다.

```powershell
git status
git switch develop
git pull --ff-only origin develop
git switch -c feature/course-list
```

기능 브랜치에서 개발하고 PR로 develop에 통합합니다. main/develop에서 직접 기능 개발하지 않습니다.

## 3. 커밋과 Push

```powershell
git diff
git add <변경한-파일>
git commit -m "feat: 강의 목록과 분야 필터 구현"
git push -u origin feature/course-list
```

`<변경한-파일>`은 실제 경로로 바꿉니다. 무조건 git add .를 실행하기보다 포함 파일을 확인합니다. 비밀번호·토큰·.env·node_modules·빌드 결과·리뷰용 복사본을 커밋하지 않습니다.

커밋 형식: `type: 작업 내용`. type은 feat/fix/refactor/docs/test/chore/style. 사용자 동작이나 UI가 바뀌면 의미에 맞게 feat/fix를 사용하고, 단순 서식 변경은 style로 구분합니다.

## 4. PR

하나의 PR은 하나의 목적을 설명할 수 있게 구성합니다. API·UI·필요한 마이그레이션이 하나의 동작을 완성한다면 함께 포함할 수 있습니다.

PR 본문에는 다음을 적습니다.

- 문제와 변경 후 동작.
- API/DB/공통 스타일/인증 변경 및 다른 담당자 영향.
- 수행한 검사와 결과. 수행하지 않은 검증도 구분.
- UI 변경 시 PC/모바일 화면.
- 신규 제안이나 미확정 정책이 있으면 그 내용.

PR 방향은 `작업 브랜치 → develop`, 안정 버전은 `develop → main`입니다. 팀원 리뷰 후 병합합니다.

## 5. 최신 develop 반영과 충돌

```powershell
git fetch origin
git switch feature/course-list
git merge origin/develop
```

충돌은 담당자와 변경 의도를 확인해 해결합니다. 인증·라우터·공통 CSS·DB 계약을 한쪽 코드로 무조건 덮어쓰지 않습니다. 해결 후 관련 테스트와 화면/API 연결을 다시 확인합니다. 공유 브랜치의 강제 push는 별도 합의 없이 사용하지 않습니다.

## 6. 병합 방식과 브랜치 정리

Squash and merge를 기본 권장합니다. 기존 merge commit 기록을 바꾸지는 않습니다. PR #2/#3이 merge commit으로 남아 있어도 과거 이력을 재작성하지 않습니다.

병합한 PR과 원격 반영을 확인한 뒤 필요 없는 작업 브랜치를 정리합니다. Squash 병합에서는 `git branch -d`가 병합으로 인식하지 못할 수 있으므로, 실패했다고 곧바로 강제 삭제하지 말고 미반영 변경 여부를 확인합니다.

## 7. 공동 파일과 완료 기준

공동 수정 가능 파일: SecurityConfig, application.yaml, build.gradle, package.json/package-lock.json, AppRouter, AuthContext, Layout, tokens.css, 공통 CSS, ERD/API 문서.

공통 토큰을 바꾸면 frontend-guide와 영향을 받는 화면을 함께 확인합니다. 기능 완료는 코드·관련 테스트·연동·문서·PR 리뷰까지 포함합니다. 문서 추가만으로 강의 기능을 완료 처리하지 않습니다.

## 8. 담당별 브랜치·작업 크기

| 담당 | 기존 작업 예 | 학습 확장 예 |
|---|---|---|
| 김현빈 | feature/member-login, feature/product-list, feature/category | feature/course-list, feature/lesson-management |
| 김영훈 | feature/subscription-create, feature/payment, feature/refund | feature/learning-access, feature/learning-progress |
| 공동 | feature/board, feature/admin-dashboard | feature/course-player-integration, docs/learning-erd |

학습 확장 배분은 제안입니다. 사람 이름 대신 기능·문제를 사용하고 소문자·하이픈으로 표기합니다. Codex가 만드는 브랜치는 codex/ 목적명을 사용할 수 있습니다.

구독 전체나 강의 전체를 무조건 한 PR에 넣지 않습니다. 목록·상세·관리·권한·진도처럼 독립 검토 가능한 목적을 기준으로 나눕니다. 반대로 같은 동작에 필요한 DTO·API·UI·검증을 기계적으로 분리해 중간 버전을 깨뜨리지 않습니다.

## 9. PR 작성 템플릿

```markdown
## 변경 목적과 동작
- 사용자가 어떤 조건에서 무엇을 할 수 있게 되는지:

## 구현 범위
- 담당 도메인/화면:
- API 변경:
- DB/마이그레이션:
- 공통 스타일·인증 영향:

## 검증
- 실행한 명령·환경·결과:
- 정상/오류/권한·상태 경계:
- PC/모바일 화면:
- 아직 수행하지 않은 검증:

## 검토 필요
- 미확정 정책·다른 담당자 영향:
```

제목 예: `feat: 강의 목록과 분야 필터 제공`, `fix: 환불 후 차시 접근 차단`, `docs: 학습 진도 API 계약 추가`. 다른 접두사 형식이 저장소 템플릿에 있다면 팀 규칙과 맞춥니다.

## 10. 리뷰·Merge 점검

- [ ] 기능이 요구사항·API·DB 계약과 일치
- [ ] 기존 거래 흐름·타인 데이터·관리자 권한 회귀 확인
- [ ] 관련 테스트·빌드·린트 결과 명시
- [ ] UI 공통 토큰·반응형·오류 상태 확인
- [ ] 비밀값·디버그 출력·임시 파일 미포함
- [ ] 필요한 마이그레이션과 문서 동반
- [ ] 최신 develop과 충돌 여부 확인
- [ ] 다른 담당자 영향 공유·리뷰 반영

기능 브랜치에서 main으로 직접 병합하지 않습니다. main으로의 PR은 develop 통합 검증 이후 생성합니다. 테스트 기록 없이 발표 가능한 버전으로 표시하지 않습니다.

## 11. 충돌 해결 절차

1. 미커밋 변경을 확인하고 보존합니다.
2. fetch 후 자신의 브랜치에서 origin/develop을 merge합니다.
3. 충돌 파일의 두 변경 의도를 읽고 담당자와 계약을 맞춥니다.
4. 충돌 표시를 제거하고 해당 파일을 선택적으로 stage합니다.
5. 관련 자동 검사와 수동 연동 확인을 수행합니다.
6. merge 커밋을 완료하고 본인 브랜치에 push합니다.

merge 도중 취소가 필요하면 자신이 시작한 merge인지 확인하고 `git merge --abort`를 사용할 수 있습니다. 상대 코드를 보존하지 않은 강제 reset이나 공유 브랜치 이력 재작성으로 충돌을 숨기지 않습니다.

## 12. 병합 뒤 브랜치 정리 예시

PR이 병합되고 필요한 변경이 원격에 포함되었는지 확인한 뒤 실행합니다.

```powershell
git switch develop
git pull --ff-only origin develop
git branch -d feature/course-list
```

원격 브랜치가 남아 있고 더 이상 필요 없으며 해당 브랜치 소유자가 확인했다면:

```powershell
git push origin --delete feature/course-list
```

Squash 이후 -d가 거절될 수 있습니다. 이때 삭제를 강행하기 전에 PR diff·남은 커밋과 미반영 작업을 확인합니다. 강제 삭제는 기본 절차로 제공하지 않습니다.

## 13. 변경 공유 형식

API: 경로/Method → 이유 → Request/Response → 오류/권한 → 영향 페이지.
DB: 테이블/컬럼/제약 → 기존 데이터 영향 → 적용·복구 순서 → ERD.
스타일: 변경 토큰/공통 클래스 → 영향 화면 → PC/모바일 확인.

작은 변경도 공통 계약을 바꾸면 공유합니다. 같은 파일을 동시에 크게 바꿀 예정이면 먼저 범위를 나눕니다.

## 14. 전체 흐름과 최종 규칙

```text
origin/develop 최신화 → 목적별 브랜치 → 개발·검증·문서
→ commit/push → 작업 브랜치 → develop PR
→ 팀원 리뷰·충돌/회귀 확인 → merge
→ 통합 E2E → develop → main PR → 안정 버전
```

작업 전 변경 보존, 기능 중심 브랜치, 공통 계약 공유, 증거가 있는 검증, PR을 통한 통합을 지킵니다. 문서의 브랜치 예시는 명령 사용법이며 실제 브랜치를 새로 만들었다는 기록이 아닙니다.


---

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


---

# SubMate 프로젝트 파일 구조

## 1. 저장소

Frontend와 Backend를 하나의 저장소에서 관리합니다. 현재 존재하는 구조를 유지하고, 학습 기능은 기존 규칙에 맞춰 추가합니다.

```text
프로젝트 루트/
├── backend/
├── frontend/
├── docs/
└── README.md
```

output은 산출물 폴더이며 애플리케이션 소스 구조가 아닙니다. 사용자가 삭제한 tmp 리뷰 복사본은 서비스 실행에 필요하지 않습니다.

## 2. 현재 Backend

기본 패키지: `com.submate.backend`.

```text
config/SecurityConfig.java
subscription/
  controller/ dto/ entity/ repository/ service/ scheduler/
  integration/CatalogGateway.java
  support/
  demo/
payment/
  controller/ dto/ entity/ repository/ service/
refund/
  controller/ dto/ entity/ repository/ service/
admin/subscription/controller/
```

- CatalogGateway: 회원 이메일·권한과 상품 가격·기간·상태를 읽는 연결 지점. Member/Product CRUD 담당을 대체하지 않습니다.
- support: 담당 API 인증 회원 식별·업무 예외·Clock 설정 등.
- demo: demo 전용 Basic 인증과 초기 설정.
- Scheduler: 매 분 만료 대상 처리.
- Controller는 요청/응답, Service는 상태·권한·트랜잭션, Repository는 DB 접근.

현재 전역 JWT·global 공통 패키지가 모두 구현된 것처럼 구조도를 작성하지 않습니다. 기존 패키지를 일괄 이동하지 않고 팀 인증과 순차 통합합니다.

DB 리소스:

```text
src/main/resources/
├── application.yaml
├── application-demo.yaml
└── db/
    ├── subscription-schema.sql
    ├── demo-catalog.sql
    ├── demo-data.sql
    └── migrations/20260923-prorated-refunds.sql
```

테스트는 BackendApplicationTests, SubscriptionFlowTests, DemoSecurityTests와 application-test.yaml을 기준으로 합니다.

## 3. 현재 Frontend

```text
src/
├── api/axios.js, subscriptionApi.js, paymentApi.js, refundApi.js
├── components/common/ResourceState.jsx
├── components/subscription/StatusBadge.jsx
├── context/AuthContext.jsx, authState.js
├── hooks/useAuth.js, useResource.js
├── layouts/SubscriptionLayout.jsx
├── pages/
│   ├── subscription/ConnectionPage, SubscriptionsPage, SubscriptionDetailPage
│   ├── payment/PaymentPage, PaymentResultPage, PaymentsPage
│   └── admin/RefundsPage
├── routes/AppRouter.jsx
├── styles/tokens.css, subscriptions.css
├── utils/formatUtils.js
├── index.css
├── App.jsx
└── main.jsx
```

페이지 파일은 .jsx입니다. RefundsPage는 현재 admin 폴더에 있지만 사용자 환불 목록과 관리자 처리에 재사용합니다. ConnectionPage는 데모/인증 연결 화면이며 정식 회원가입 기능이 아닙니다.

main.jsx는 index.css를, App.jsx는 공통 UI·구독 화면 스타일을 불러옵니다. 디자인 값의 원본은 tokens.css입니다. App.css는 이전 템플릿 스타일을 제거한 안내 파일입니다.

## 4. 신규 학습 구조 — 제안

| 영역 | 추가 위치 제안 | 담당 배분 제안 |
|---|---|---|
| 강의·차시 API/도메인 | backend의 course/, lesson/ | 김현빈 |
| 상품-강의 연결 | 상품 또는 강의 도메인의 명시적 연결 서비스 | 김현빈 |
| 수강 권한·진도 | backend의 learning/ | 김영훈 |
| 강의 탐색·상세 | frontend/src/pages/course/ | 김현빈 |
| 내 학습·수강 | frontend/src/pages/learning/ | 김영훈 주도, 콘텐츠 연결 공동 |
| 강의·차시 관리자 | frontend/src/pages/admin/ | 김현빈 |
| API 모듈 | courseApi.js, learningApi.js | 각 도메인 담당 |
| UI 스타일 | course/learning 전용 CSS, 값은 공통 토큰 | 공동 기준 |

위 디렉터리·파일은 추가 계획이며 현재 모두 존재하는 것이 아닙니다. 기본 언어 JavaScript/JSX를 유지합니다.

## 5. 기존 라우트와 신규 제안

현재: /login, /subscriptions, /subscriptions/:id, /subscribe/:productId, /payment/result, /payments, /refunds, /admin/subscriptions, /admin/subscriptions/:id, /admin/payments, /admin/refunds.

추가 제안: /courses, /courses/:courseId, /learning, /learn/:courseId/:lessonId, /admin/courses.

현재 루트 /는 인증 후 /subscriptions로 이동합니다. 강의 탐색을 홈으로 만드는 변경은 새 라우트·비회원 권한·공통 Layout을 구현할 때 함께 반영합니다.

## 6. 문서와 연결 규칙

- docs/requirements.md: 기존 담당 기능 요구사항.
- docs/erd.md, api-spec.md: 현재 구현 계약.
- docs/architecture.md: 구조와 연결 경계.
- docs/conventions.md, git-strategy.md: 개발·협업 규칙.
- docs/frontend-guide.md: 공통 스타일 기준.
- docs/test-scenarios.md, verification.md: 검증 시나리오와 실제 결과.
- docs/week-1.md: 기존 담당 범위와 인수인계.

노션 확장안은 실제 구현과 합의가 진행되면 저장소 문서에도 반영합니다. 현재 구현 문서를 미구현 확장 설계로 무조건 덮어쓰지 않습니다.

## 7. 현재 Backend 상세 파일 트리

```text
backend/
├── build.gradle
├── gradlew
├── gradlew.bat
├── gradle/
└── src/
    ├── main/
    │   ├── java/com/submate/backend/
    │   │   ├── BackendApplication.java
    │   │   ├── config/SecurityConfig.java
    │   │   ├── subscription/
    │   │   │   ├── controller/SubscriptionController.java
    │   │   │   ├── dto/PageResponse.java, SubscriptionResponse.java
    │   │   │   ├── entity/Subscription.java, SubscriptionStatus.java
    │   │   │   ├── repository/SubscriptionRepository.java
    │   │   │   ├── service/SubscriptionService.java
    │   │   │   ├── scheduler/SubscriptionExpiryScheduler.java
    │   │   │   ├── integration/CatalogGateway.java
    │   │   │   ├── support/
    │   │   │   │   ├── CurrentMember.java
    │   │   │   │   ├── DomainException.java
    │   │   │   │   ├── SubscriptionConfiguration.java
    │   │   │   │   └── SubscriptionExceptionHandler.java
    │   │   │   └── demo/DemoConfiguration.java
    │   │   ├── payment/
    │   │   │   ├── controller/PaymentController.java
    │   │   │   ├── dto/CheckoutRequest.java, CheckoutResponse.java, PaymentResponse.java
    │   │   │   ├── entity/Payment.java, PaymentMethod.java, PaymentStatus.java
    │   │   │   ├── repository/PaymentRepository.java
    │   │   │   └── service/PaymentService.java
    │   │   ├── refund/
    │   │   │   ├── controller/RefundController.java
    │   │   │   ├── dto/RefundRequest.java, RefundDecisionRequest.java, RefundResponse.java
    │   │   │   ├── entity/Refund.java, RefundStatus.java
    │   │   │   ├── repository/RefundRepository.java
    │   │   │   └── service/RefundService.java
    │   │   └── admin/subscription/controller/SubscriptionAdminController.java
    │   └── resources/
    │       ├── application.yaml
    │       ├── application-demo.yaml
    │       └── db/
    │           ├── demo-catalog.sql
    │           ├── demo-data.sql
    │           ├── subscription-schema.sql
    │           └── migrations/20260923-prorated-refunds.sql
    └── test/
        ├── java/com/submate/backend/
        │   ├── BackendApplicationTests.java
        │   └── subscription/SubscriptionFlowTests.java, DemoSecurityTests.java
        └── resources/application-test.yaml
```

한 줄에 쉼표로 나열한 파일은 같은 디렉터리의 개별 파일입니다. 원문의 com/submate/SubMateApplication.java와 달리 현재 기본 패키지·클래스는 com/submate/backend/BackendApplication.java입니다. application-local.yml과 application-prod.yml을 이미 존재하는 설정으로 적지 않습니다.

## 8. 패키지별 현재/추가 역할

| 패키지 | 역할 | 현재 여부 / 담당 |
|---|---|---|
| config | Security·CORS 연결 | 현재 / 공동 검토 |
| auth | JWT 발급·인증 요청 처리 | 통합 제안 / 김현빈 |
| member | 회원 가입·조회·수정·관리 | 통합 대상 / 김현빈 |
| product | 상품 조회·가격·판매 상태 | 정식 도메인 통합 대상 / 김현빈 |
| category | 분류 CRUD·연결 제한 | 통합 대상 / 김현빈 |
| subscription | 구독 조회·해지·기간·만료 | 현재 / 김영훈 |
| payment | checkout·거래 내역 | 현재 / 김영훈 |
| refund | 일할 요청·승인/반려 | 현재 / 김영훈 |
| course, lesson | 강의·차시·콘텐츠·공개 | 신규 제안 / 김현빈 배분 제안 |
| learning | 수강 권한·진도 | 신규 제안 / 김영훈 배분 제안 |
| board | 공지사항·추후 일반 게시판 | 후속 / 공동 |
| admin | 관리자 진입점·조회·처리 | 거래 일부 현재 / 도메인별 |
| global | 공통 설정/예외/보안/응답 분리안 | 원문 설계안, 현재 일괄 생성·이동 안 함 |

global로 공통 예외를 통합하려면 기존 SubscriptionExceptionHandler의 적용 패키지와 응답 계약을 먼저 확인합니다. 현재 Advice를 모든 도메인에 적용되는 전역 처리기로 오해하지 않습니다.

관리자 Controller는 일반 Service의 검증을 재사용하며 관리자용 복제 Service에서 상태 정책을 별도로 만들지 않습니다. 현재 checkout 진입점이 SubscriptionController이고 실제 처리는 PaymentService인 구조를 유지합니다.

## 9. 현재 Frontend 상세 구조·책임

```text
frontend/
├── package.json / package-lock.json
├── vite.config.js
├── .env.example
├── .oxlintrc.json / .prettierrc / .prettierignore
├── index.html
├── public/
└── src/
    ├── api/
    │   ├── axios.js
    │   ├── subscriptionApi.js
    │   ├── paymentApi.js
    │   └── refundApi.js
    ├── components/
    │   ├── common/ResourceState.jsx
    │   └── subscription/StatusBadge.jsx
    ├── context/AuthContext.jsx, authState.js
    ├── hooks/useAuth.js, useResource.js
    ├── layouts/SubscriptionLayout.jsx
    ├── routes/AppRouter.jsx
    ├── pages/
    │   ├── subscription/
    │   │   ├── ConnectionPage.jsx
    │   │   ├── SubscriptionsPage.jsx
    │   │   └── SubscriptionDetailPage.jsx
    │   ├── payment/
    │   │   ├── PaymentPage.jsx
    │   │   ├── PaymentResultPage.jsx
    │   │   └── PaymentsPage.jsx
    │   └── admin/RefundsPage.jsx
    ├── styles/tokens.css, subscriptions.css
    ├── utils/formatUtils.js
    ├── assets/
    ├── index.css
    ├── App.css
    ├── App.jsx
    └── main.jsx
```

| 영역 | 구체적 책임 |
|---|---|
| api | URL·Method·요청 변환·응답 반환 |
| components | 상태 배지·오류/로딩·Pagination 등 재사용 |
| pages | 라우트 단위 UI와 사용자 행동 |
| layouts | 사이드바·헤더·본문·푸터 |
| routes | 라우트·로그인/관리자 가드 |
| context | 공통 인증 상태와 제공자 |
| hooks | 인증 소비·비동기 조회 상태 |
| utils | 금액·날짜·상태 문구 |
| styles | 디자인 토큰과 공통/도메인 배치 |
| constants | 필요할 때 상수 분리 제안, 현재 폴더를 의무 생성하지 않음 |

PrivateRoute는 현재 AppRouter 파일 내부 함수입니다. 원문의 별도 PrivateRoute.jsx/AdminRoute.jsx는 파일 분리 제안이지 실제 파일이 아닙니다. AuthContext.jsx와 authState.js처럼 대소문자만 다른 파일 이름 충돌을 피합니다.

## 10. API·인증·스타일 연결 예시

현재 api/axios.js는 `VITE_API_BASE_URL || '/api'`, timeout 15000ms, 메모리 Authorization 헤더를 사용합니다. 오류 메시지는 서버 message를 우선하고 인증/연결 오류를 안내합니다. 자동 Refresh Token 재발급이나 모든 오류의 중앙 처리가 이미 구현된 것은 아닙니다.

```text
SubscriptionsPage
→ subscriptionApi.list(page)
→ api/axios.js (메모리 인증 헤더)
→ /api/subscriptions
→ SubscriptionController → SubscriptionService → Repository/Gateway
→ PageResponse DTO → 화면
```

구독 신청은 subscriptionApi.checkout → POST /subscriptions이며 별도 paymentApi 결제 생성 API를 추가해 중복 결제하지 않습니다.

```text
main.jsx → index.css → styles/tokens.css
App.jsx → styles/subscriptions.css
새 course/learning 스타일 → --sm-* 토큰 재사용
```

현재 세션은 App에 전달하는 accessToken/email/role과 onLogout으로 연결합니다. 실제 로그인 구현이 들어오면 기존 AuthProvider를 대체·중복 생성하기 전에 연결 규약을 맞춥니다.

## 11. 페이지·역할 전체 계획

| 화면 | 현재 파일 또는 추가 제안 | 담당 |
|---|---|---|
| 로그인/회원가입 | LoginPage/SignupPage 추가, ConnectionPage는 데모 | 김현빈 |
| 회원정보 | MyInfoPage 제안 | 김현빈 |
| 상품 목록/상세 | ProductListPage/ProductDetailPage 제안 | 김현빈 |
| 강의 탐색/상세 | CourseListPage/CourseDetailPage 제안 | 김현빈 배분 제안 |
| 구독 신청/결제 | PaymentPage 현재 | 김영훈 |
| 결제 결과 | PaymentResultPage 현재 | 김영훈 |
| 내/관리자 구독 목록 | SubscriptionsPage 현재 재사용 | 김영훈 |
| 구독 상세 | SubscriptionDetailPage 현재 재사용 | 김영훈 |
| 내/관리자 결제 목록 | PaymentsPage 현재 재사용 | 김영훈 |
| 내/관리자 환불 | RefundsPage 현재 재사용 | 김영훈 |
| 내 학습 | MyLearningPage 제안 | 김영훈 배분 제안 |
| 수강 | LearningPage 제안 | 공동 연결 |
| 관리자 회원/상품/카테고리 | 관리 화면 추가 제안 | 김현빈 |
| 관리자 강의/차시 | 관리 화면 추가 제안 | 김현빈 배분 제안 |
| 공지 목록/상세/작성 | BoardList/Detail/Write 제안 | 공동 |
| 관리자 대시보드 | AdminDashboardPage 제안 | 공동 |

제안 파일명은 구현 단계에서 확정합니다. 원문의 SubscribePage와 MySubscriptionsPage에 해당하는 역할은 현재 PaymentPage와 SubscriptionsPage가 수행하므로 같은 기능의 새 페이지를 무조건 만들지 않습니다.

## 12. 기능별 신규 추가 예

```text
backend/src/main/java/com/submate/backend/
├── course/{controller,service,repository,entity,dto}/
├── lesson/{controller,service,repository,entity,dto}/
└── learning/{controller,service,repository,entity,dto}/

frontend/src/
├── api/courseApi.js, learningApi.js
├── pages/course/CourseListPage.jsx, CourseDetailPage.jsx
├── pages/learning/MyLearningPage.jsx, LearningPage.jsx
├── components/course/
└── styles/courses.css, learning.css
```

중괄호는 디렉터리 구성 예시이며 실제 경로명이 아닙니다. 처음부터 빈 파일을 전부 생성하지 않고 독립 검증 가능한 기능마다 추가합니다. Course/Lesson을 하나의 도메인 아래에 둘지 분리할지는 복잡도를 보고 정합니다.

## 13. 설계 원칙과 검증

도메인별 분리 → 담당 경계 명확화 → 공통 계약 공유 → Service 업무 처리 → DTO 응답을 유지합니다.

인증·DB·라우터·Axios·토큰·공통 Layout 변경은 다른 영역에 미치는 영향을 검토합니다. `.env` 실값은 커밋하지 않고 `.env.example`에는 변수 이름·비밀이 아닌 예시만 둡니다. build/dist/node_modules는 소스 트리에 포함하지 않습니다.

구조 문서를 바꿨다고 폴더가 생성된 것으로 기록하지 않습니다. 구현 후 실제 파일 경로·담당·API·테스트·문서를 함께 갱신합니다.


---

# SubMate 공통 개발 규칙

## 1. 개발 원칙

SubMate는 학습·강의 콘텐츠 구독 서비스입니다. 기존 구독·결제·환불 코드를 유지하면서 회원·상품·콘텐츠·학습 기능을 연결합니다.

- 요구사항·제안·현재 구현·검증 완료를 구분합니다.
- 각 담당자는 자신의 Frontend·Backend·테스트·문서를 함께 관리합니다.
- 다른 담당자의 계약에 영향을 주는 변경은 공유합니다.
- 불필요한 전체 재작성이나 패키지 이동보다 기존 구조 안의 작은 변경을 우선합니다.
- 서버가 권한·소유자·금액·기간·상태 전이를 검증합니다.

## 2. 네이밍

| 구분 | 기준 | 예시 |
|---|---|---|
| Java 클래스 / Entity | PascalCase, 단수 | Course, SubscriptionService |
| Java 메서드·변수 | camelCase | requestRefund |
| 상수 | UPPER_SNAKE_CASE | MAX_PAGE_SIZE |
| 패키지 | 소문자 도메인 | subscription, learning |
| DTO | 목적과 방향 | CheckoutRequest, SubscriptionResponse |
| React 컴포넌트 | PascalCase.jsx | CourseDetailPage.jsx |
| JS 함수·변수 | camelCase | handleCompleteLesson |
| Hook | use 접두사 | useResource |
| DB 테이블·컬럼 | snake_case | learning_progress, member_id |
| CSS 토큰 | --sm- 접두사 | --sm-color-primary |

회원 개념은 Member / members / memberId로 맞추고 users/userId를 새로 혼용하지 않습니다. 파일명의 대소문자와 import는 정확히 일치시킵니다. Windows에서 대소문자만 다른 파일을 만들지 않습니다.

## 3. Backend

- Controller: 입력 검증·인증 식별·Service 호출·DTO 응답.
- Service: 업무 규칙·상태 전이·권한·트랜잭션.
- Repository: 조회·저장·필요한 잠금.
- Entity를 API 응답으로 그대로 반환하지 않습니다.
- 기존 CatalogGateway의 회원/상품 읽기 계약을 존중합니다.
- 중요 변경은 트랜잭션·잠금·UNIQUE 제약을 함께 검토합니다.
- 시간 판단은 주입 가능한 Clock을 사용해 날짜 경계 테스트가 가능하게 합니다.

구독 생성 시 Member → Product, 환불 승인 시 Refund → Payment → Subscription 등 현재 잠금 순서와 충돌하지 않게 연결합니다. 새 학습 진도 UNIQUE(member_id,lesson_id)는 제안이며 실제 DDL과 함께 구현합니다.

## 4. Frontend

- api/: API 호출, pages/: 화면, components/: 재사용 UI, hooks/: 상태/조회 로직.
- API Base URL과 인증 헤더는 기존 api/axios.js를 통해 관리합니다.
- 세션은 공통 AuthProvider와 연결하고 도메인별 로그인 저장소를 따로 만들지 않습니다.
- 로딩·오류·빈 목록·성공을 구분합니다. 중복 제출을 막고 서버 검증 결과를 표시합니다.
- 오래된 비동기 응답이 새 화면 상태를 덮어쓰지 않게 기존 useResource 패턴을 확인합니다.
- 컴포넌트는 명확한 이름과 label·키보드 포커스를 제공합니다.
- 코드 스타일은 저장소 Prettier 설정을 따릅니다.

### 공통 스타일

- 원본은 `src/styles/tokens.css`, 사용 기준은 프론트엔드 공통 가이드입니다.
- 색상·font-size·간격·radius는 토큰을 사용합니다.
- body·h1·button 등을 새 페이지 CSS에서 임의 재정의하지 않습니다.
- 학습 화면도 기존 버튼·입력·패널·상태 배지 규칙을 사용합니다.
- 모바일 720px, 태블릿 1050px, 넓은 화면 1450px 분기점을 공통으로 사용합니다.

## 5. API 계약

- /api, JSON, camelCase, 명사 중심 경로.
- 현재 성공 DTO 직접 응답, 오류 `{status,message}`, 목록 `{content,page,size,totalElements,totalPages}`.
- 기존 응답을 공통 래퍼로 바꾸려면 모든 영향 화면을 함께 이관합니다.
- 200 조회/변경·멱등 재전송, 201 신규 생성, 400 잘못된 요청, 401 비인증, 403 권한 부족, 404 없음/타인 거래, 409 상태·중복 충돌.
- 서버 내부 정보와 SQL·비밀값을 오류에 노출하지 않습니다.
- 구현하지 않은 인증·강의 API는 API 명세에서 계획으로 표시합니다.

## 6. 구독·결제·환불 정책

- 구독과 결제는 POST /api/subscriptions에서 함께 생성합니다.
- 실패한 최초 결제는 오류만 반환하며 거래 행은 만들지 않습니다.
- 월간/연간은 1회 결제 이용 기간이며 자동 청구하지 않습니다.
- 이용 기간은 한국 날짜 기준 [startDate,endDate), 종료일 00:00부터 이용 불가입니다.
- ACTIVE → CANCELLED, ACTIVE/CANCELLED → EXPIRED.
- CANCELLED는 종료일까지 이용 가능하며 동일 상품 중복 구독을 막습니다.
- 환불은 잔여 일수 기준, 원 미만 버림, 요청 시 금액·일수 저장.
- 승인 시 환불·결제·구독을 함께 바꾸고 즉시 이용 종료합니다.
- 반려 후 재요청 불가, 임의 금액 환불·실패 재처리 기능은 기본 범위에 없습니다.

과거 문서의 ‘해지 즉시 종료’, ‘24시간 전액 환불’, ‘반려 후 재요청 가능’은 현재 기준으로 사용하지 않습니다.

## 7. 학습 기능 — 구현 전 합의할 기준

- Course는 강의, Lesson은 차시, Product는 이용권입니다.
- Product-Course 연결 모델과 신규 역할 배분은 제안 상태입니다.
- 콘텐츠 조회와 진도 변경 모두 서버 수강 권한 검사가 필요합니다.
- 진도 기록이 존재한다는 사실만으로 수강을 허용하지 않습니다.
- 공개 상세에 유료 차시 본문·비공개 영상 링크를 포함하지 않습니다.
- 완료 기준, 미리보기, 이용권 구성 변경 영향, 재구독 진도 유지 정책을 합의합니다.
- 신규 기능 상태는 코드·테스트·연동 근거로 갱신합니다.

## 8. DB·보안

- JPA 기본 validate. DB 변경에는 검토 가능한 DDL/마이그레이션을 제공합니다.
- FK·CHECK·인덱스·UNIQUE를 정의하고 실제 MySQL에서 검증합니다.
- 금액은 BigDecimal/DECIMAL. 거래 이력과 학습 이력을 무조건 연쇄 삭제하지 않습니다.
- 비밀번호는 해시 저장, JWT Secret·DB 비밀번호는 환경변수 사용.
- 일반 가입은 USER, ADMIN 권한은 서버 정책으로 관리.
- 로그아웃 시 클라이언트 세션 제거와 토큰 서버 폐기는 구분합니다.
- 메뉴 숨김이나 라우트 가드가 서버 인가를 대체하지 않습니다.
- demo Basic 인증은 demo 프로필에 한정합니다.

## 9. Git·리뷰

기능 브랜치 → PR → develop → 통합 검증 → main을 따릅니다. 자세한 명령은 Git 전략 문서를 사용합니다.

커밋 형식 `type: 작업 내용`, 목적별로 작은 변경을 만듭니다. PR에는 변경 동작·관련 API/DB·검증 결과·제약을 적습니다. UI 변경은 PC/모바일 상태를 확인합니다.

실제 회귀 테스트 코드는 유지합니다. 제거 대상은 임시 디버그 출력·불필요한 실험 코드·비밀값이며, 테스트 코드를 일괄 삭제하지 않습니다.

## 10. 완료 기준

- [ ] 합의한 요구사항과 API에 맞게 구현
- [ ] 정상·오류·권한·소유자·날짜/상태 경계 확인
- [ ] 필요한 DB 제약·동시성 검증
- [ ] Frontend ↔ Backend 연결
- [ ] 관련 자동 검사와 실제 화면 확인 결과 기록
- [ ] API·ERD·가이드·실행 문서 갱신
- [ ] 영향받는 팀원과 공유, PR 검토

Frontend: `npm run build`, `npm run lint`. Backend: `.\gradlew.bat test`. 실제 JWT/MySQL E2E는 데모/H2 검사와 별도입니다.

현재 남은 통합이나 실행하지 않은 검사를 완료로 표시하지 않습니다.

## 11. 네이밍·Boolean·import 상세

Boolean은 isLoading/isLoggedIn/isAdmin/hasSubscription처럼 의미가 드러나게 이름을 짓습니다. 응답의 usable은 현재 API 이름이므로 스타일 통일을 이유로 임의 변경하지 않습니다.

DTO는 실제 방향을 표현합니다. 현재 CheckoutRequest/Response·RefundDecisionRequest 이름을 사용하며 새 이름으로 바꾸려면 import·테스트·문서 영향을 확인합니다. 파일명은 PascalCase 컴포넌트와 camelCase 일반 JS를 구분하고 Windows/Linux에서 같은 import가 해석되도록 실제 대소문자를 맞춥니다.

API 필드는 camelCase, DB는 snake_case입니다. JSON memberId와 DB member_id는 같은 개념이며 userId로 혼용하지 않습니다. enum은 정해진 문자열을 사용하고 화면 번역 문구를 서버 상태값으로 보내지 않습니다.

## 12. 계층별 코드 작성 기준

```text
Client → Request DTO 검증 → Controller
→ Service(업무/권한/트랜잭션) → Repository/Gateway → DB
→ Response DTO → Client
```

Controller가 입력값 검증을 했어도 Service는 현재 데이터 상태와 권한을 검사해야 합니다. 가격·구독 기간·환불 금액·학습 권한을 클라이언트 계산값으로 저장하지 않습니다.

Service에서 여러 행을 바꾸는 승인 처리는 트랜잭션 하나로 보장합니다. 조회 전용은 readOnly를 사용할 수 있으나 잠금/수정 작업과 섞지 않습니다. 재요청을 ‘화면 버튼 비활성’만으로 막지 않고 DB 제약과 서버 상태를 검사합니다.

Repository에 특정 화면 문구나 인증 세션 로직을 넣지 않습니다. 여러 도메인의 상태 전이는 Service에서 조정합니다. admin API라도 기존 Service 검증을 우회하지 않습니다.

## 13. Frontend 요청·비동기·폼

현재 API 호출 예:

```js
import { subscriptionApi } from '../api/subscriptionApi'

await subscriptionApi.cancel(subscriptionId)
```

페이지별 axios 인스턴스·서버 주소·Authorization 헤더 복제를 피합니다. 공통 timeout/오류 메시지는 api/axios.js를 확인합니다. loading 동안 제출을 막고 성공 후 필요한 목록을 재조회합니다. 오류를 사용자가 해결할 수 있는 문구와 함께 표시합니다.

폼에는 label을 연결하고 필수값을 안내합니다. 클라이언트 검증은 사용성을 위한 것이며 서버 검증을 생략하지 않습니다. 토큰·비밀번호·결제 민감정보를 console에 남기지 않습니다. 실제 카드번호 입력은 현재 가상 결제 범위에 없습니다.

비동기 응답이 순서대로 도착한다고 가정하지 않습니다. 페이지·필터·계정이 바뀌면 이전 응답이 새 상태를 덮어쓰지 않도록 처리합니다. 진도율을 낙관적으로 바꿀 경우 실패 복구와 서버 재조회까지 구현합니다.

## 14. 주석·오류·HTTP 처리

코드에 이미 드러나는 동작을 반복하는 주석보다 정책 이유를 적습니다.

```java
// CANCELLED도 종료일까지 이용하므로 해지 시 종료일을 단축하지 않는다.
```

업무 예외는 없음·권한·상태·중복·기간을 구분합니다. 현재 거래 Advice의 ErrorResponse는 status/message입니다. 존재하지 않는 구독과 타인 구독은 같은 404 정책으로 처리합니다. 예상하지 못한 오류는 사용자에게 내부 스택·SQL을 노출하지 않습니다.

| 코드 | 공통 사용 목표 | 현재 주의 |
|---|---|---|
| 200 | 조회·변경 | 동일 checkout 재전송도 200 |
| 201 | 신규 생성 | 구독+결제, 환불 요청 |
| 204 | 내용 없는 삭제 | 계획 CRUD에서 사용, 현재 거래 삭제 API 없음 |
| 400 | 형식·범위·필수값 | 가상 결제 실패도 현재 400 |
| 401 | 인증 필요·실패 | Security 필터 응답 형태 별도 검증 |
| 403 | 역할·수강권 부족 | 학습 수강권 응답은 신규 제안 |
| 404 | 없음·타인 거래 | 정보 노출 제한 |
| 409 | 상태·중복·동시성 | 처리 후 목록 재조회 안내 |
| 500 | 예상하지 못한 오류 | 현재 공통 JSON 표준화 완료로 단정 안 함 |

## 15. 상태 전이·DB·보안 점검

### 상태

```text
구독: 생성 → ACTIVE → CANCELLED → EXPIRED
                    └──────────→ EXPIRED
환불: REQUESTED → COMPLETED 또는 REJECTED
결제: 현재 정상 생성 COMPLETED → 환불 승인 REFUNDED
```

EXPIRED→ACTIVE, CANCELLED→ACTIVE를 되돌리는 기능은 없습니다. 재구독은 새 행입니다. 상태 문자열을 임의 수정하는 관리자 범용 API를 만들지 않습니다. 새 강의 공개 enum은 거래 enum과 분리합니다.

### DB

PK는 entity_id, member 테이블은 members, 구독 start/end는 DATE입니다. cancelled_at/paid_at/refunded_at는 사건 시점입니다. EnumType.STRING을 사용하고 번호 저장을 피합니다. 새 컬럼은 기본값·NULL·기존 데이터·인덱스·FK·복구 전략을 함께 기록합니다.

DDL 적용 전 JPA validate 설정을 확인합니다. DDL update만으로 DB 계약이 만들어졌다고 가정하지 않습니다. H2의 MySQL 모드는 실제 MySQL 잠금/제약 검증을 대신하지 않습니다.

### 보안

비밀번호는 BCrypt 등 팀이 채택한 단방향 해시, 인증은 JWT 목표, demo Basic은 demo에 한정합니다. 사용자 역할은 서버에서 결정하고 DB role과 인증 role 불일치를 검증합니다. 민감 환경변수는 코드/노션에 기록하지 않습니다.

강의 URL을 알고 있다는 것만으로 콘텐츠를 반환하지 않습니다. 공개 메타데이터·유료 본문·관리자 초안 데이터를 별도 DTO로 분리하고 데이터 조회 시점에도 권한을 검사합니다.

## 16. 팀별 협업 범위

| 담당 | 기존 범위 | 신규 제안 |
|---|---|---|
| 김현빈 | 회원·JWT·상품·카테고리·관리 화면 | 강의·차시·상품 포함 강의 |
| 김영훈 | 구독·결제·환불·스케줄러·관리 화면 | 수강 권한·진도·내 학습 |
| 공동 | ERD·API·보안 검토·통합·게시판·통계·문서·발표 | 수강 화면 전체 연결·공통 UI |

상대 영역 수정이 필요하면 이유·계약·영향 범위를 공유합니다. 자동으로 담당자를 바꾸지 않으며 신규 역할은 합의 전 제안 표시를 유지합니다.

## 17. 리뷰·검증·완료 기록 예

```text
기능: 일할 환불 승인
요구사항: 요청일 금액 유지, 승인 시 즉시 종료
변경: Refund/Payment/Subscription Service·관리자 화면
검증: 명령·환경·건수·결과와 확인 날짜
수동 확인: 사용자 요청 → 관리자 승인 → 사용자 재조회
문서: API·DB·정책 반영
미검증: 실제 MySQL/JWT 등 남은 조건
리뷰: 영향 담당자 검토 결과
```

검증 항목은 변경 성격에 맞춥니다. 거래·권한·동시성에는 의미 있는 테스트를 유지하고, 단순 문구/서식 변경에 동작을 그대로 복사하는 테스트를 추가하지 않습니다. 실행하지 않은 검증에는 ‘미수행’을 적습니다.

리뷰에서는 기능·정합성·권한·API·DB·스타일·반응형·비밀값·문서·충돌을 확인합니다. 임시 디버그 코드와 **실제 테스트**를 구분하고 테스트를 삭제 완료 항목으로 삼지 않습니다.

## 18. 최종 Definition of Done

- [ ] 요구사항·담당·범위가 명확함
- [ ] 정상 동작과 예외/권한/경계 처리
- [ ] 필요한 트랜잭션·동시성·DB 제약 검증
- [ ] Frontend/Backend 실제 연동 및 재조회
- [ ] 공통 스타일·접근성·모바일 확인
- [ ] 관련 자동 검사 실행 결과 기록
- [ ] 임시 코드·비밀값·불필요한 산출물 정리
- [ ] API·ERD·화면 안내·가이드 갱신
- [ ] commit·push·PR·팀원 리뷰
- [ ] 남은 통합/미검증을 숨기지 않음

기능의 코드 작성 완료와 전체 서비스 통합 완료를 구분합니다. 실제 JWT/MySQL·강의 수강까지 확인되지 않았다면 전체 E2E 완료로 표시하지 않습니다.


---

# SubMate 개발 요청용 공통 프롬프트

아래 내용을 새로운 개발 대화의 배경 정보로 사용한다. 문서의 제안과 실제 구현 상태를 구분하고, 제공한 저장소 코드를 먼저 확인한다.

---

너는 React와 Spring Boot 기반 2인 팀 프로젝트 SubMate의 설계·구현을 돕는 개발자다.

## 1. 작업 원칙

- 기존 코드·실행 설정·테스트·문서를 확인하고 일치하는 부분은 유지한다.
- 요구사항에 없는 정책·기능·일정·담당자 합의를 확정 사실로 만들지 않는다.
- 현재 구현, 신규 개발 계획, 팀 합의가 필요한 제안, 실제 검증 결과를 구분한다.
- 이해하고 설명하기 쉬운 기존 모놀리식 구조를 유지한다.
- 실제 확인하지 않은 테스트·원격 병합·통합·배포를 완료로 표현하지 않는다.

## 2. 서비스

프로젝트명은 SubMate, **학습·공부·강의 콘텐츠 구독 서비스**다.
사용자는 강의를 탐색하고 해당 강의를 포함한 이용권을 구독한 뒤, 차시 학습·진도 저장·이어 학습하기와 구독 관리를 수행하는 것을 목표로 한다.

- Product: 가격·월간/연간 기간을 가진 이용권.
- Course: 소개·학습 목표·난이도·커리큘럼을 가진 강의.
- Lesson: 강의에 속한 차시 콘텐츠.
- LearningProgress: 회원별 학습 이력.
- Subscription/Payment/Refund: 구독 기간·가상 거래.

Product-Course N:M 연결은 제안이다. 정확한 관계와 상품 구성 변경 시 기존 구독 영향은 구현 전 합의한다.

## 3. 기술과 구조

- Frontend: React/Vite/React Router/JavaScript/JSX/CSS, 기존 axios API 모듈 사용.
- Backend: Java 21/Spring Boot/Spring Security/Spring Data JPA, 기본 패키지 com.submate.backend.
- 실제 인증 목표 JWT, 현재 demo 프로필에서만 Basic 인증.
- 통합 DB MySQL, demo/test H2.
- Controller/Service/Repository/DTO 역할 분리.
- 디자인 값은 frontend/src/styles/tokens.css, 기준은 docs/frontend-guide.md.

신규 프레임워크·TypeScript 전환·마이크로서비스·복잡한 스트리밍 인프라를 요구 없이 추가하지 않는다.

## 4. 역할

기존 담당:

- 김현빈: 회원·JWT/Security·상품·카테고리 및 각 Frontend/Backend.
- 김영훈: 구독·가상 결제·해지·만료·일할 환불 및 각 Frontend/Backend.
- 공동: 설계·공통 UI·보안 검토·통합·문서·공지사항·대시보드.

추가 배분 제안:

- 김현빈: 강의 목록/상세·차시·콘텐츠·관리자 강의 관리·상품-강의 연결.
- 김영훈: 수강 권한·학습 진도·내 학습·이어 학습하기.
- 수강 화면의 콘텐츠와 권한/진도 연결은 공동.

신규 역할 배분을 팀원 합의 완료로 표현하지 않는다. 기능 설명에 담당과 확정/제안 상태를 명시한다.

## 5. 현재 구현 계약

- POST /api/subscriptions에서 가상 결제와 구독을 원자적으로 생성한다.
- 가격·회원은 서버에서 결정한다.
- requestKey로 같은 요청을 재전송하면 기존 결과를 반환한다.
- 동일 상품의 유효 ACTIVE/CANCELLED 구독 중복을 막는다.
- 실패한 최초 결제는 오류를 반환하고 구독/결제 행을 저장하지 않는다.
- MONTHLY는 시작일 +1개월, YEARLY는 +1년. 자동 청구는 없다.
- 날짜는 Asia/Seoul, LocalDate, [startDate,endDate)이다.
- endDate 당일 00:00부터 이용 불가다.
- ACTIVE 해지는 CANCELLED이며 기존 종료일까지 이용할 수 있다.
- 매 분 Scheduler가 ACTIVE/CANCELLED 만료를 EXPIRED로 처리한다.
- 서버는 Scheduler 지연과 무관하게 기간을 검사한다.
- 환불은 요청일 기준 결제금액 × 잔여 일수 / 전체 일수, 원 미만 버림이다.
- 환불 요청 때 금액/일수를 저장하고 승인일이 달라져도 유지한다.
- 원결제당 요청 1회이며 반려 후 재요청할 수 없다.
- 관리자 PATCH /api/admin/refunds/{id}, decision=APPROVE/REJECT를 사용한다.
- 승인 시 Refund=COMPLETED, Payment=REFUNDED, Subscription=EXPIRED·즉시 종료를 함께 처리한다.
- 반려 시 기존 결제·구독을 유지한다.

즉시 해지·24시간 전액 환불·실패 환불 재처리·별도 POST /payments를 현재 정책으로 도입하지 않는다. 정책 변경 요청이 있으면 기존 코드/테스트/데이터 이관 영향을 먼저 설명한다.

## 6. 인증·API 연결

- 실제 인증 Authentication.name=회원 이메일, ROLE_USER/ROLE_ADMIN.
- Frontend App에 session={accessToken,email,role}, onLogout을 전달한다.
- 상품 신청 링크 /subscribe/{productId}.
- API 성공은 DTO 직접 반환, 오류는 {status,message}.
- 목록은 {content,page,size,totalElements,totalPages}, page=0, size=20 기본, 최대 100.
- 비인증 401, 관리자 권한 부족 403, 타인 거래/없음 404, 상태·중복 충돌 409.
- Member/Product 읽기 계약은 CatalogGateway를 확인한다. 같은 Entity를 임의 복제하지 않는다.

## 7. 신규 학습 기능

목표: 공개 강의 목록·상세, 이용권 안내, 강의/차시 관리자 관리, 서버 수강 권한, 차시 수강, 진도 저장, 내 학습, 이어 학습하기.

수강 콘텐츠 조회와 진도 변경은 유효 구독과 포함 강의 관계를 서버에서 검사한다. CANCELLED도 잔여 기간에는 허용하고 만료·환불 후에는 차단한다. 다른 유효 이용권으로 같은 강의를 이용할 수 있는 경우를 설계에서 고려한다.

타인 진도 접근, 유료 콘텐츠의 공개 응답 노출, 차시-강의 소속 불일치, 중복 진도 저장을 방지한다. 진도는 보존과 접근 권한을 분리한다.

첫 콘텐츠는 텍스트/학습 자료 중심과 선택적 허가된 영상 링크를 제안한다. 업로드·인코딩·스트리밍 서버는 별도 확장이다. 완료 기준·공개 미리보기·재구독 이력·상품 구성 변경 영향은 미확정 사항으로 다룬다.

## 8. 추가 범위와 제외

공지사항과 관리자 대시보드는 핵심 기능 통합 이후 구현한다. 공지사항은 공개 조회·관리자 작성 범위를 제안한다. 결제·환불·학습 지표는 조회 기간과 집계 정의를 명시한다.

실제 PG·실제 금전 환불·자동 갱신·외부 플랫폼 계정 연동은 제외한다. 질문/답변·댓글·첨부파일·AI 튜터·개인화 추천·실시간 강의·수료증은 요청이 없는 한 확장 제안으로만 다룬다.

## 9. 데이터·스타일·품질

- 거래 금액/기간은 당시 확정값을 유지한다. 상품 수정으로 과거 결제 금액을 바꾸지 않는다.
- 거래 이력을 무조건 물리/연쇄 삭제하지 않는다.
- JPA 기본 validate, DDL과 마이그레이션 제공, 실제 MySQL 제약·잠금 검증.
- 공통 색상·폰트 크기·4px 간격·버튼/카드 규칙을 사용한다.
- 로딩·오류·빈 목록·키보드 포커스·모바일 화면을 확인한다.
- .env·JWT Secret·DB 비밀번호는 커밋하지 않는다.

## 10. 작업 결과 형식

요청 범위에 맞게 다음을 정리한다.

1. 요구사항 이해와 기존 구현 상태.
2. 확정 정책과 새 제안, 차이와 필요한 결정.
3. 페이지/도메인별 담당 및 변경 파일.
4. API·DB·공통 스타일 영향.
5. 실제 구현 또는 문서 산출물.
6. 수행한 검증과 남은 검증.

코드 요청이면 계획만으로 끝내지 않고 승인된 범위의 구현과 적절한 검증을 수행한다. 문서 요청이면 요청하지 않은 기능을 구현하지 않는다.

## 11. 기능별 상세 요구사항

### 회원·인증 — 김현빈

- 이메일·비밀번호·이름의 필수/형식·중복을 검증한다.
- 비밀번호는 단방향 해시로 저장하고 응답에서 제외한다.
- 일반 가입은 USER이며 요청으로 ADMIN을 설정할 수 없다.
- 로그인 성공 시 JWT를 발급하고 서명·만료·권한을 보호 요청마다 검증한다.
- 로그아웃 시 클라이언트 인증 제거와 서버 토큰 폐기는 별개임을 설명한다.
- 본인 회원정보 조회/수정, 관리자 회원 목록/상세·관리·탈퇴 정책을 구현한다.
- 회원 삭제가 거래/진도를 무조건 삭제하지 않도록 정책을 먼저 합의한다.

### 상품·카테고리 — 김현빈

- 비회원도 공개 상품 목록·상세·분야 필터를 이용한다.
- 상품은 이름·설명·가격·MONTHLY/YEARLY·판매 상태를 가진 학습 이용권이다.
- 관리자는 등록·조회·수정·판매 중지와 조건부 삭제를 수행한다.
- INACTIVE 신규 신청은 서버가 차단하고 기존 거래는 보존한다.
- 카테고리 CRUD와 상품/강의 연결 삭제 제한을 구현한다.
- 기존 상품 category_id와 새 강의 category_id 공유 여부를 확인한다.

### 강의·차시 — 김현빈 배분 제안

- 공개 강의 목록·검색·카테고리·난이도와 상세 소개·학습 목표·커리큘럼을 제공한다.
- 관리자는 강의와 차시를 등록/수정하고 공개 상태·순서를 관리한다.
- Product-Course 연결을 관리하되 상품 구성 변경이 기존 구독에 미치는 영향을 먼저 합의한다.
- 공개 DTO는 유료 본문과 비공개 영상 URL을 포함하지 않는다.
- 차시 콘텐츠 조회는 인증·강의 공개·소속·유효 이용권을 검증한다.
- 첫 콘텐츠 형식은 텍스트 중심·선택적 허가 영상 링크 제안이며 아직 확정된 콘텐츠 제작 의무가 아니다.

### 구독·결제 — 김영훈

- 본인 구독 목록/상세·관리자 전체 조회·해지를 제공한다.
- 성공 결제와 구독 생성은 같은 트랜잭션으로 저장하고 부분 저장을 방지한다.
- 사용자/가격/기간/상태는 서버가 결정한다.
- 요청 키 재전송과 다른 키의 유효 중복 구독을 함께 방지한다.
- 가상 실패는 400이며 최초 실패 이력 행을 저장하지 않는다.
- 동일 키의 다른 상품·수단·simulateFailure 요청은 409로 처리한다.
- 월말·윤년은 LocalDate.plusMonths/plusYears 기준이다.
- 해지 ACTIVE→CANCELLED, 기존 종료일 유지·잔여 이용, 취소/재활성 기능은 없음.
- ACTIVE/CANCELLED 만료를 매 분 검사하고 EXPIRED를 반복 변경하지 않는다.
- 재구독은 새 Subscription이다. 상태를 되살리지 않는다.
- 해지/만료/환불 동시 처리에 잠금·상태 재확인·트랜잭션을 적용한다.

### 환불 — 김영훈

- 원결제 소유자·COMPLETED·환불 요청 유무·잔여 기간을 확인한다.
- ACTIVE/CANCELLED 잔여 기간 모두 요청 가능하며 해지를 선행 조건으로 강제하지 않는다.
- 금액은 원결제금액×요청일 잔여일수/전체일수, 원 미만 버림이다.
- 요청일과 종료일 경계·전체일수·0원·만료 오류를 검사한다.
- 요청 때 금액/일수를 저장하고 승인 지연에도 유지한다.
- 요청 중에는 기존 기간 내 이용이 유지되며 승인 때 종료한다.
- 승인·반려는 REQUESTED만 가능하며 중복 처리를 차단한다.
- 승인 때 Refund/Payment/Subscription이 함께 바뀐다.
- 반려 후 같은 원결제 재요청은 UNIQUE 제약에 의해 불가하다.
- 학습 시간·진도율 차감, 관리자 임의 금액, 실패 환불 재처리는 현재 범위가 아니다.

### 수강·진도 — 김영훈 배분 제안, 콘텐츠 연결 공동

- 유효 구독의 상품이 대상 강의를 포함하는지 서버가 확인한다.
- ACTIVE/CANCELLED와 날짜를 검사하고 EXPIRED·환불 후 차단한다.
- 다른 유효 상품이 같은 강의를 포함하면 접근 가능한 안을 고려한다.
- 진도는 회원·차시 단위 UNIQUE로 보존하고 완료/최근 학습 시각을 서버가 저장한다.
- 진도율은 서버 계산값, 완료 기준·완료 취소·차시 변경 분모는 합의한다.
- 내 학습과 이어 학습은 실제 기록을 재조회하여 표시한다.
- 타인 memberId 요청을 신뢰하지 않고 현재 로그인 회원을 사용한다.
- 만료 후 이력 조회와 유료 콘텐츠·진도 변경 권한을 분리한다.

### 관리자·공지·통계 — 공동

- 관리자 권한은 서버에서 확인한다. 거래 상태를 임의 문자열로 편집하지 않는다.
- 회원/상품/강의 관리는 김현빈 주도, 거래는 김영훈 주도 경계를 유지한다.
- 공지사항은 공개 조회·관리자 작성/수정/삭제를 기본안으로 제안한다.
- 일반 게시글·작성자 권한 요구는 확장 계획으로 보존하며 도입 여부를 묻지 않고 확정하지 않는다.
- 통계는 회원·상품·강의 수, 유효 구독·해지/만료·결제/환불·상품별 현황을 검토한다.
- 조회 기간·시간대·집계 상태·탈퇴/초안 포함 여부를 표시한다.
- REFUNDED 거래의 최초 결제와 환불을 이중 차감하지 않도록 정의한다.

## 12. 데이터 설계 필수 조건

기존 Member/Category/Product/Subscription/Payment/Refund/Post 개념을 보존하고 Course/Lesson/ProductCourse/LearningProgress를 제안한다.

- 현재 거래 DDL의 member_id/request_key/version/환불 일수 필드를 누락하지 않는다.
- subscriptions는 회원/상품 FK, payments는 구독/회원 FK, refunds는 원결제 FK UNIQUE다.
- Member→Subscription→Product→Payment 같은 직렬 FK 모델로 오해하지 않는다.
- 원문 Subscription 1:N Payment는 관계로 유지하지만 현재 최초 결제 한 건만 생성한다.
- 실제 실패 행 저장 기능을 enum 존재만으로 완료 처리하지 않는다.
- 회원/상품의 참조 무결성과 이력 보존을 먼저 고려한다.
- 현재 상품명은 동적 조회이며 과거 명칭 스냅샷 기능을 있다고 주장하지 않는다.
- 신규 FK·NULL·길이·enum·UNIQUE·인덱스는 제안과 현재 DDL을 분리한다.

## 13. 검수 시나리오

### 인증·회원·상품

중복 가입, 필수값 오류, 잘못된 로그인, 만료/변조 JWT, 일반 회원의 관리자 접근, 타인 정보 수정, 판매 중지 상품 신청, 연결된 카테고리 삭제, 기존 거래 보존을 검증한다.

### 거래

성공 결제와 구독 동시 생성, 가상 실패 미저장, 중간 저장 실패 롤백, 같은 요청 키 재전송, 키 충돌, 다른 키 동시 중복, 월말·윤년, 해지 후 잔여 이용, 종료일 자정, 만료 배치 재실행, 만료 후 재구독을 검증한다.

### 환불

일할 금액·원 미만 버림·마지막 1일·0원·만료 후 거절·해지 후 잔여 요청·승인 지연 금액 유지·본인 확인·반려 후 재요청 불가·중복 승인 차단·세 도메인 일괄 변경을 검증한다.

### 콘텐츠·학습

공개 소개와 유료 본문 분리, 초안 미노출, 강의-차시 소속, 무구독 차단, CANCELLED 잔여 허용, 만료/환불 차단, 다른 유효 이용권, 타인 진도, 진도 재전송/동시 저장, 재접속 이어 학습, 빈/모두 완료/차시 비공개 전환을 검증한다.

### 공통 품질

로딩·오류·빈 상태·모바일·키보드·긴 텍스트, 빌드·린트·관련 테스트, 공지 권한·통계 원본 일치를 확인한다. H2 검사와 실제 MySQL/JWT E2E를 구분한다.

## 14. 첫 응답과 후속 작업 형식

사용자가 분석/계획을 요청하면 다음 순서를 기본으로 한다.

1. 요구사항 이해 내용.
2. 기존 코드의 구현 상태와 요구사항 차이.
3. 추가 결정이 필요한 사항과 합리적 제안.
4. 페이지·기능·담당 표.
5. 관계·상태 전이·API 연결.
6. 선행 조건과 구현 우선순위.

이미 특정 문서/코드 작업을 요청했다면 전체 분석을 반복하는 데서 멈추지 않고 해당 산출물을 완성한다. 파일 경로·변경 목적·담당 영역·검증 방법·실제 결과를 제시한다.

현재 저장소 문서에 기록된 2026-09-28 백엔드 27건은 과거 결과다. 새 검증을 실행하지 않았다면 과거 기록으로 표시한다. 팀원의 원격 최신 작업은 확인하지 않고 완료/미완료로 단정하지 않는다.

## 15. 전체 완료 시나리오

회원가입 → 로그인 → 강의 탐색 → 이용권 선택 → 가상 결제 → ACTIVE → 수강 → 완료 저장 → 이어 학습 → 해지 → CANCELLED 잔여 이용 → EXPIRED 차단을 실제 저장/조회와 함께 검증한다.

환불은 별도 경로로 요청 → 관리자 승인 → 즉시 권한 종료, 반려 → 기존 권한 유지, 이미 처리된 요청 재처리 차단을 확인한다. 문서·화면·API·DB 정책이 서로 일치해야 한다.


---

# SubMate 역할 분담 — 학습·강의 구독 서비스

## 1. 팀 구성

| 구분 | 이름 | 기존 주요 담당 |
|---|---|---|
| 팀장 | 김영훈 | 구독·가상 결제·환불 |
| 팀원 | 김현빈 | 회원·인증·상품·카테고리 |
| 공동 | 김영훈·김현빈 | 설계·보안 검토·통합·문서화·공통 UI |

담당자는 자신의 도메인에 대해 Frontend·Backend·테스트·문서까지 책임집니다. 아래 강의·학습 관련 추가 배분은 기존 경계를 바탕으로 한 **제안**이며 팀 합의 후 확정합니다.

## 2. 김영훈 — 구독·결제·환불

### Backend

- 구독·결제 원자적 생성, 서버 가격 검증, 요청 키를 이용한 중복 생성 방지.
- 본인 구독 목록·상세, 소유권 검사, 해지와 자동 만료.
- 월간·연간 상품의 날짜 계산과 한국 시간 기준 이용 가능 여부 판정.
- 가상 결제 성공·실패 처리, 본인 결제 내역 조회.
- 일할 환불 요청·금액 계산, 관리자 승인·반려, 거래 정합성 유지.
- 관리자 구독 목록·상세·해지 및 전체 결제·환불 조회.

실패한 최초 결제는 구독·결제 행을 저장하지 않습니다. Payment의 PENDING/FAILED enum 존재가 실패 이력 저장 기능의 완성을 뜻하지 않습니다.

### Frontend

- 구독 신청·가상 결제·완료 화면.
- 내 구독 목록·상세·해지 확인·이용 종료 시각 안내.
- 본인 결제·환불 목록과 환불 요청.
- 관리자 구독·결제·환불 관리.

### 추가 제안: 수강 권한·학습 진행

- 회원이 특정 강의에 접근할 수 있는지 서버에서 판정.
- 본인 학습 진도 조회·저장, 내 학습과 이어 학습하기.
- 해지 후 잔여 기간 이용, 만료·환불 승인 후 수강/진도 변경 차단.
- 수강 화면의 권한·진도 API 연결과 관련 테스트.

강의 자체의 정보·차시 콘텐츠는 김현빈 담당 제안 영역이며, 수강 화면 연결은 공동 확인합니다.

## 3. 김현빈 — 회원·인증·상품·카테고리

### Backend

- 회원가입·로그인, 회원정보 조회·수정, 관리자 회원 관리.
- 비밀번호 해시, JWT 발급·검증, Spring Security 인증·인가.
- USER/ADMIN 권한 관리. 일반 회원이 가입 시 ADMIN을 선택하지 못하도록 처리.
- 상품·카테고리 목록·상세·관리, 상품 가격·월/연 기간·판매 상태 관리.
- 거래가 연결된 회원·상품의 삭제 제한 또는 비활성 처리 정책 구현.

### Frontend

- 회원가입·로그인·로그아웃, 회원정보 조회·수정.
- 상품 목록·상세·카테고리 필터.
- 관리자 회원·상품·카테고리 관리.
- 실제 세션을 공통 App/AuthProvider에 연결.

### 추가 제안: 강의·차시 콘텐츠

- 강의 목록·상세·검색·분야 필터.
- 강의 소개·학습 목표·난이도·커리큘럼.
- 관리자 강의·차시 등록·수정·공개 상태 관리.
- 구독 상품에 포함할 강의 연결 관리.
- 차시 콘텐츠 제공 API와 수강 화면의 콘텐츠 영역.

## 4. 공동 담당

| 영역 | 협업 내용 |
|---|---|
| 학습 모델 | Product/Course 연결, 차시 구성, 학습 완료 기준 결정 |
| 보안 | 김현빈이 JWT 구현 주도, 김영훈이 구독·학습 API 연결 검증 |
| 공통 UI | 공통 토큰·버튼·입력·카드·오류/로딩 상태 재사용 |
| 게시판 | 기본 공지사항 범위 제안, 핵심 기능 후 세부 배분 |
| 대시보드 | 김현빈: 회원·상품·강의 / 김영훈: 구독·결제·환불·학습 지표 제안 |
| 통합 | 인증 → 강의 → 상품 → 구독 → 수강 → 진도 → 해지·만료·환불 |
| 문서 | 요구사항·ERD·API·구조·테스트·시연·발표 |
| Git | PR 리뷰, 공통 파일 변경 공유, 충돌 해결과 재검증 |

신규 통계 항목은 집계 기준부터 합의하고, 아직 구현하지 않은 통계를 완료로 기록하지 않습니다.

## 5. 기능별 담당 요약

| 기능 | 담당 | 구분 |
|---|---|---|
| 회원·JWT·Security | 김현빈 주도, 공동 검토 | 기존 |
| 상품·카테고리·관리자 관리 | 김현빈 | 기존 |
| 구독·해지·만료·Scheduler | 김영훈 | 기존 |
| 가상 결제·일할 환불·관리자 처리 | 김영훈 | 기존 |
| 강의 탐색·상세·차시·콘텐츠 관리 | 김현빈 | 신규 배분 제안 |
| 수강 권한·진도·내 학습 | 김영훈 | 신규 배분 제안 |
| 수강 화면 전체 연결 | 공동 | 신규 배분 제안 |
| 공통 스타일·설계·통합·문서 | 공동 | 공통 |
| 공지사항·관리자 대시보드 | 공동 | 후속 구현 |

## 6. 개발 경계와 연결 계약

**김현빈:** Member / Product / Category, 추가 제안 Course / Lesson / 상품-강의 연결.

**김영훈:** Subscription / Payment / Refund, 추가 제안 LearningProgress / 수강 권한.

- 실제 인증은 Authentication.name=회원 이메일, 권한 ROLE_USER/ROLE_ADMIN으로 연결합니다.
- App의 세션은 `{accessToken, email, role}`이며 role은 USER/ADMIN입니다.
- 상품 → 결제 링크는 `/subscribe/{productId}`입니다.
- 구독과 결제 생성은 `POST /api/subscriptions` 하나로 처리합니다.
- 강의 접근은 로그인만으로 허용하지 않고, 회원과 유효 구독·강의 포함 관계를 서버에서 확인합니다.
- 한 강의를 여러 이용권이 포함한다면 그중 하나라도 유효할 때 접근을 허용하는 안을 제안합니다.

## 7. 상태와 날짜 정책

```text
결제 성공 → ACTIVE
ACTIVE → 해지 신청 → CANCELLED → 종료일 도달 → EXPIRED
ACTIVE → 종료일 도달 → EXPIRED
ACTIVE/CANCELLED → 환불 승인 → EXPIRED (즉시 이용 종료)
```

CANCELLED는 종료일까지 이용할 수 있습니다. 구독 이용 기간은 한국 날짜 기준 [startDate, endDate)이며 종료일 00:00부터 이용 불가입니다. 스케줄러 지연과 관계없이 서버에서 기간을 검사합니다.

환불은 잔여 일수 기준이며 요청 시 금액을 저장합니다. 승인 시 Payment=REFUNDED, Refund=COMPLETED, 구독 이용 종료를 함께 처리합니다. 반려 시 기존 이용 상태를 유지하며 결제당 재요청은 허용하지 않습니다.

## 8. 협업 규칙

- 사람의 작업 브랜치는 `feature/*`, `fix/*`, `docs/*` 등 목적별로 생성합니다. Codex 작업은 `codex/*`를 사용할 수 있습니다.
- `develop`에서 분기하고 PR로 통합한 뒤 검증된 버전을 `main`에 반영합니다.
- API 변경 시 경로·요청·응답·변경 이유·영향 화면을 공유합니다.
- DB 변경 시 ERD·DDL/마이그레이션·기존 데이터 영향을 함께 검토합니다.
- 공통 CSS·라우터·SecurityConfig·인증 상태 변경은 팀원에게 공유합니다.
- 임의 상태 변경 API나 별도 결제 생성 흐름으로 기존 계약을 우회하지 않습니다.

실제 관계는 Member → Subscription ← Product, Subscription → Payment → Refund입니다. Member → Subscription → Product → Payment처럼 일렬로 연결된 FK 관계가 아닙니다.

## 9. 완료 기준

기능 완료는 화면·API 구현뿐 아니라 권한, 예외 처리, 테스트, 문서, PR 검토까지 포함합니다.

최종 검수 흐름:

```text
회원가입 → 로그인 → 강의 탐색 → 이용권 선택 → 가상 결제
→ ACTIVE → 강의 수강 → 진도 저장 → 이어 학습하기
→ 해지 → CANCELLED 상태의 잔여 기간 이용 → EXPIRED 후 접근 차단
```

환불은 별도 시나리오로 요청 → 관리자 승인 → 즉시 접근 차단을 검증합니다. 반려 시에는 기존 이용 권한이 유지되는지 확인합니다.

## 10. 현재 진행 상태

- 김영훈 담당 구독·가상 결제·일할 환불과 관련 화면은 현재 로컬에 구현되어 있습니다.
- 회원·JWT·정식 상품 및 MySQL 통합은 현재 로컬 기준 후속 작업입니다. 팀원 별도 작업 상태는 확인이 필요합니다.
- 강의·차시·학습 진도는 신규 계획이며 현재 구현 완료 항목에 포함하지 않습니다.
- 신규 담당 배분과 학습 정책은 팀 합의 후 이 문서에 확정으로 변경합니다.

## 11. 김영훈 상세 산출물·책임 경계

| 기능 | Backend 산출물 | Frontend 산출물 | 완료 확인 |
|---|---|---|---|
| 구독 생성 | Checkout·가격/멱등·유효 구독 검증 | 신청·동의·중복 제출 방지 | 성공 원자 저장·실패 롤백 |
| 구독 조회 | 본인/관리자 목록·상세 DTO | 목록·상태·기간·페이지 이동 | 타인 차단·빈 목록 |
| 해지 | 잠금·ACTIVE/날짜 검증·해지 시각 | 확인 UI·취소·결과 안내 | CANCELLED·종료일 유지 |
| 만료 | Clock·월/연 날짜·Scheduler | 만료/이용 불가 표시 | 자정 경계·반복 배치 |
| 가상 결제 | 수단·성공/실패·내역 | 결제·완료·실패 UI | 실제 카드정보 미수집 |
| 환불 요청 | 본인 결제·잔여 일수·금액 저장 | 사유·정책·요청·목록 | 한 번 요청·만료 차단 |
| 환불 처리 | APPROVE/REJECT·일괄 상태 변경 | 관리자 검토·확인·결과 | 승인 지연·반려·재처리 차단 |
| 학습 권한 | 유효 구독·포함 강의 검사 | 접근/구독 필요 안내 | 만료/환불 차단 |
| 학습 진도 | 본인 진도·upsert·최근 차시 | 완료 처리·내 학습·이어 학습 | 타인 격리·재조회·중복 |

마지막 두 행은 신규 배분 제안입니다. 환불 승인 금액을 임의 입력하는 기능, 실패 거래 이력 저장, 실패 환불 재처리를 이미 맡아 구현한 기능으로 추가하지 않습니다.

## 12. 김현빈 상세 산출물·책임 경계

| 기능 | Backend 산출물 | Frontend 산출물 | 완료 확인 |
|---|---|---|---|
| 회원가입 | 형식·이메일 중복·해시·USER 지정 | 가입 폼·검증·안내 | 중복/관리자 선택 차단 |
| 인증 | 로그인·JWT 검증·Security | 로그인·로그아웃·세션 연결 | 변조/만료·ROLE 규약 |
| 회원정보 | 본인 조회/수정 | 마이페이지 정보/수정 | 타인 수정·허용 필드 |
| 관리자 회원 | 목록·상세·관리·탈퇴 정책 | 관리자 관리 화면 | 거래 보존·권한 |
| 상품 | 목록·상세·등록/수정·비활성 | 공개/관리자 상품 UI | 서버 가격·기간·판매 상태 |
| 카테고리 | CRUD·연결 검사 | 필터·관리 UI | 중복·연결된 분류 삭제 제한 |
| 강의 | 목록·상세·학습 목표·공개 | 탐색·상세·관리 화면 | 유료 본문 비노출 |
| 차시 | 콘텐츠·순서·공개·소속 | 커리큘럼·콘텐츠·관리 | 소속·순서·공개 검사 |
| 상품-강의 | 포함 관계·변경 정책 | 포함 강의 관리 | 기존 구독 영향 합의 |

강의·차시·상품-강의 세 행은 신규 배분 제안입니다. 회원 CRUD의 삭제는 거래 FK·탈퇴 보존 정책과 함께 결정하고 무조건 물리 삭제하지 않습니다. JWT/Security는 김현빈 주도지만 다른 도메인 인가까지 공동 검수합니다.

## 13. 공동 설계·보안·게시판·대시보드 상세

### 설계와 공통 UI

요구사항·사용 흐름·공개/보호 페이지·ERD·API·오류 형식·상품-강의 모델·콘텐츠 형식을 공동 결정합니다. 디자인 토큰과 공통 버튼·입력·카드 기준은 두 사람 모두 사용하고 변경 시 공유합니다.

### 인증·보안

JWT 발급/만료 정책·보호 API 범위·USER/ADMIN·CORS·해시·로그인 회원 식별을 통일합니다. 관리자 화면 숨김과 서버 인가를 분리하고 유료 콘텐츠와 타인 진도까지 확인합니다.

### 게시판

원문의 게시글 CRUD·작성자 권한·관리자 관리 요구는 남겨둡니다. 우선 공지사항을 기본안으로 제안하고 관리자 작성/수정/삭제·공개 조회를 먼저 고려합니다. 일반 글쓰기·질문/답변·댓글은 핵심 학습 기능 이후 범위를 합의합니다. 세부 담당을 일방 확정하지 않습니다.

### 대시보드

전체 회원·상품·강의 수, 활성/해지/만료 구독, 결제 건수·금액, 환불 건수·금액, 상품별 구독 수, 선택적 학습 현황을 검토합니다. 김현빈은 회원·상품·콘텐츠, 김영훈은 거래·학습 지표를 담당하는 안을 제안합니다. 조회 기간과 상태별 집계 정의를 공동 검수합니다.

### 문서·발표

README·요구사항·R&R·ERD·API·구조·개발/프론트 가이드·검증·시연·발표 자료를 유지합니다. 담당자는 자신의 API·DB·테스트 내용을 작성하고 연결 관계·최종 발표는 공동으로 확인합니다.

## 14. 도메인 연결 인수인계 목록

| 연결 | 전달할 정보 | 검증 |
|---|---|---|
| Auth → 거래·학습 | 이메일 principal·ROLE·회원 ID 해석 | 실제 JWT 요청 |
| Product → 결제 | ID·이름·현재 가격·MONTHLY/YEARLY·판매 상태 | CatalogGateway/실제 DB |
| Product ↔ Course | 포함 관계·변경 시 기존 권한 | 합의한 DDL/API |
| Course → 수강 | 공개 강의/차시·순서·본문 제공 경로 | 비공개·소속·메타/본문 분리 |
| Subscription → 수강 | 유효 상태·기간·상품 연결 | CANCELLED 잔여/EXPIRED |
| Refund → 수강 | 승인 후 즉시 종료·다른 이용권 | 권한 재검사 |
| Progress → 화면 | 완료 수·전체 수·최근/이어 차시 | 서버 계산·재조회 |

연결 계약에 없는 금액·회원 ID·진도율을 클라이언트가 임의 결정하지 않습니다. 하나의 강의가 여러 유효 이용권에 포함되는 경우도 공동 테스트합니다.

## 15. 통합 테스트·단계별 완료 기준

1. **기존 담당 통합:** 실제 회원·상품·JWT·MySQL로 신청/해지/환불 검증.
2. **콘텐츠:** 공개 탐색·커리큘럼·관리자 공개·유료 본문 분리.
3. **학습:** 이용권 기반 수강·완료 저장·최근/이어 학습.
4. **경계:** 무구독·타인·해지 잔여·만료·환불 승인/반려·다른 이용권.
5. **공통 품질:** 모바일·로딩/오류/빈 상태·빌드·린트·테스트.
6. **최종 통합:** 문서/시연/발표의 내용과 실제 실행 일치.

각 단계에는 담당자·코드/PR·실행 환경·날짜·검증 결과를 기록합니다. 신규 역할을 제안했다고 해당 단계가 시작·완료된 것으로 표시하지 않습니다.

## 16. R&R 변경 원칙

기존 도메인은 기존 담당자가 책임집니다. 새 강의/학습 배분은 두 사람이 확인한 뒤 확정으로 바꾸며 일정·작업량도 함께 조정합니다. 공통 작업은 ‘공동’ 표시만 남기지 말고 착수 시 구체 산출물별 주 담당·검토자를 기록합니다.

정책·API·DB·공통 스타일을 바꾸면 관련 문서를 함께 수정합니다. 완료 기준은 코드 작성이 아니라 연동·검증·문서·리뷰까지입니다.


---

# SubMate 프론트엔드 공통 가이드

기준일: 2026-10-19. 학습·공부·강의 콘텐츠를 제공하는 구독 서비스의 공통 UI 기준입니다.
기존 구독·가상 결제·환불 화면에 적용했습니다. 강의 탐색·수강·진도 관리는 추가 개발 대상이며, 이 가이드가 해당 기능의 구현 완료를 의미하지 않습니다.

## 1. 팀 공통 원칙

- 모든 화면은 `src/styles/tokens.css`의 `--sm-*` 변수를 사용합니다.
- 색상·폰트 크기·간격·모서리를 페이지마다 새로 정의하지 않습니다. 변경은 공통 토큰부터 수정합니다.
- 기본은 밝은 화면입니다. 운영체제의 다크 모드에 따라 일부 화면만 바뀌지 않게 합니다.
- 주요 행동은 초록색, 해지·삭제 등 주의 행동은 빨간색을 사용합니다. 상태는 색상과 문구를 함께 표시합니다.
- 화면마다 핵심 행동 버튼을 명확히 하고, 로딩·오류·빈 목록·접근 불가 상태를 제공합니다.

## 2. 색상

| 용도 | 토큰 | 값 |
|---|---|---|
| 주요 버튼·선택·브랜드 | `--sm-color-primary` | #19856E |
| 주요 버튼 hover | `--sm-color-primary-hover` | #106E59 |
| 선택 메뉴 배경 | `--sm-color-primary-soft` | #EAF5F1 |
| 기본 글자 | `--sm-color-text` | #24332E |
| 보조 설명 | `--sm-color-text-secondary` | #52665D |
| 날짜·보조 정보 | `--sm-color-text-muted` | #64756D |
| 페이지 배경 | `--sm-color-background` | #F6F8FA |
| 카드·입력 배경 | `--sm-color-surface` | #FFFFFF |
| 구분선 | `--sm-color-border` | #DCE5E0 |
| 성공·이용 중 | `--sm-color-success` / `success-soft` | #22745B / #EAF6F0 |
| 대기·해지 예약 | `--sm-color-warning` / `warning-soft` | #855C12 / #FFF3DD |
| 실패·오류·반려 | `--sm-color-danger` / `danger-soft` | #A14848 / #FCF0EF |
| 환불 완료·정보 | `--sm-color-info` / `info-soft` | #52618E / #EDF0FB |

배경용 soft 색상을 본문 글자색으로 사용하지 않습니다. 썸네일 이미지 자체의 색상은 자유롭게 쓰되, UI 버튼과 상태 색상은 위 기준을 따릅니다.

## 3. 폰트와 글자 크기

공통 글꼴: `Noto Sans KR → Malgun Gothic → Apple SD Gothic Neo → system-ui → sans-serif`.
외부 폰트를 자동 다운로드하지 않으며 설치된 글꼴로 표시합니다. 팀에서 동일한 폰트 파일을 도입할 경우 라이선스를 확인한 뒤 공통으로 제공합니다.
기본 크기는 16px이며 rem 단위를 사용합니다.

| 용도 | 토큰 | 크기 |
|---|---|---|
| 상태 배지·부가 정보 | `--sm-font-xs` | 12px |
| 버튼·라벨·보조 설명 | `--sm-font-sm` | 14px |
| 본문·입력 내용 | `--sm-font-md` | 16px |
| 카드 소제목 | `--sm-font-lg` | 18px |
| 섹션 제목 | `--sm-font-xl` | 20px |
| 모바일 페이지 제목 | `--sm-font-2xl` | 24px |
| 데스크톱 페이지 제목·주요 금액 | `--sm-font-3xl` | 32px |

- 본문 줄 높이 1.6, 제목 1.35. 제목은 700, 버튼·강조는 600, 본문은 400을 기본으로 합니다.
- 페이지마다 h1은 하나만 사용하고 h2, h3 순서로 구성합니다.
- 핵심 학습 설명·가격·오류 내용을 12px로 축소하지 않습니다.

## 4. 간격·크기·반응형

| 토큰 | 값 | 대표 용도 |
|---|---|---|
| `--sm-space-1` | 4px | 라벨과 짧은 보조 정보 |
| `--sm-space-2` | 8px | 아이콘과 글자, 작은 요소 |
| `--sm-space-3` | 12px | 버튼 그룹·폼 내부 |
| `--sm-space-4` | 16px | 관련 요소 사이 |
| `--sm-space-5` | 20px | 모바일 화면 좌우 여백 |
| `--sm-space-6` | 24px | 카드 사이, 모바일 카드 안쪽 |
| `--sm-space-7` | 28px | 데스크톱 카드 안쪽 |
| `--sm-space-8` | 32px | 페이지 제목 아래 |
| `--sm-space-10` | 40px | 큰 섹션 여백 |
| `--sm-space-12` | 48px | 데스크톱 화면 좌우 여백 |
| `--sm-space-16` | 64px | 빈 상태·큰 구획 |

- 본문 최대 너비 1200px, 사이드바 232px. 긴 설명·상세 화면은 더 좁게 제한할 수 있습니다.
- 기준 분기점: 720px 이하 모바일, 1050px 이하 태블릿, 1450px 이상 넓은 화면. CSS 미디어 쿼리는 이 값을 사용합니다.
- 모바일은 카드 1열, 태블릿은 최대 2열, 넓은 화면은 최대 3열입니다. 표는 가로 스크롤을 제공합니다.
- 버튼·입력은 최소 높이 44px. 메뉴가 줄바꿈되어도 제목과 버튼이 겹치지 않게 합니다.
- 모서리: 배지 4px, 버튼·입력 8px, 카드·패널 12px. 카드 그림자는 `--sm-shadow-card`만 사용합니다.

## 5. 공통 UI 사용법

현재 모든 라우트가 `App`에서 공통 스타일을 불러옵니다. 페이지 파일에서 `index.css`나 토큰을 다시 import하지 않습니다.

```jsx
<section className="panel">
  <h2>강의 소개</h2>
  <p className="muted">학습 대상과 학습 목표를 안내합니다.</p>
  <div className="actions">
    <button className="primary">학습 시작</button>
    <button className="secondary">목록으로</button>
  </div>
</section>
```

위 강의 문구는 스타일 사용 예시이며 실제 수강 기능은 아직 없습니다.

| 목적 | 기존 클래스/컴포넌트 |
|---|---|
| 화면 제목과 설명 | `.page-heading`, `.muted` |
| 카드/상세 영역 | `.panel`, `.subscription-card` |
| 주요/보조/주의 버튼 | `.primary`, `.secondary`, `.danger-outline`, `.danger` |
| 버튼 묶음 | `.actions` |
| 상태 배지 | `StatusBadge` 컴포넌트 |
| 오류/성공 메시지 | `.message.error`, `.message.success` |
| 로딩·오류 | `ResourceState` 기본 export (`loading`, `error`, `reload`) |
| 빈 목록 | `.empty`와 안내 문구·다음 행동 |
| 페이지 이동 컴포넌트 | `ResourceState.jsx`의 `Pagination` named export |
| 표·페이지 이동 | `.table-wrap`, `.pagination` |

새 도메인의 배치는 해당 CSS에서 정의하되 값은 토큰을 사용합니다.

```css
.course-card {
  padding: var(--sm-space-7);
  background: var(--sm-color-surface);
  border: 1px solid var(--sm-color-border);
  border-radius: var(--sm-radius-lg);
}
```

## 6. 학습 콘텐츠 화면 기준

신규 기획 기준이며 아직 구현하지 않은 화면입니다.

- 강의 탐색: 제목, 카테고리, 난이도, 강의 수/시간, 구독 포함 여부를 같은 순서로 표시합니다.
- 강의 상세: 학습 목표, 대상, 커리큘럼, 이용 가능한 구독 상품, 신청/수강 행동을 안내합니다.
- 내 학습: 수강 중인 강의, 진도율, 이어 학습하기를 보여줍니다. 진도율은 서버 저장 데이터를 기준으로 표시합니다.
- 수강 화면: 차시 목록, 현재 학습 콘텐츠, 완료 처리, 이전/다음 차시를 제공합니다.
- 해지 예약 중에는 기존 정책에 따라 종료 시각까지 이용하고, 만료·환불 승인 후 접근은 서버에서 차단해야 합니다.
- 수강권 검사, 강의/차시 API, 학습 진도 저장은 추가 개발 대상입니다. 클라이언트에서 버튼만 숨기는 것으로 완료 처리하지 않습니다.

## 7. 협업과 확인

1. 화면 개발 전 공통 클래스와 토큰을 확인합니다. 이미 있는 버튼·입력·카드를 재사용합니다.
2. 공통 규칙을 바꾸면 토큰과 이 문서를 같은 변경에서 수정합니다. 담당자나 일정은 별도 합의합니다.
3. JSX 인라인 스타일과 페이지별 전역 `body`, `button`, `h1` 재정의를 피합니다. 도메인별 클래스 이름을 사용합니다.
4. PR에 PC/모바일 화면, 변경한 공통 토큰, 로딩·오류·빈 상태 확인 결과를 남깁니다.
5. 키보드 포커스, 폼 label, 상태 문구, 긴 제목·이메일 줄바꿈을 확인합니다.
6. `npm run build`, `npm run lint`를 통과시킵니다. 실제 브라우저 확인은 빌드 검사와 별도로 기록합니다.

## 8. 파일 위치

- `frontend/src/styles/tokens.css`: 공통 디자인 값의 원본
- `frontend/src/index.css`: 기본 글꼴·배경·문서 초기화
- `frontend/src/styles/subscriptions.css`: 현재 공통 UI와 구독 화면 스타일
- `frontend/src/main.jsx`: 공통 기본 스타일 로딩
- `docs/frontend-guide.md`: 이 문서. 노션 가이드와 규칙이 달라지면 함께 갱신

## 9. 화면 공통 구성

현재 구독 Layout은 사이드바·상단 계정 영역·main·footer입니다. 신규 강의 탐색이 서비스 홈이 될 때 공개 메뉴와 로그인 사용자 메뉴를 함께 설계하되 공통 색·폰트·버튼 기준은 유지합니다.

| 화면 영역 | 기준 |
|---|---|
| 제목 | 페이지 h1 하나, 설명과 핵심 행동을 함께 배치 |
| 본문 | 1200px 최대 너비, 내용에 따라 상세 너비 축소 |
| 카드 목록 | 모바일 1열, 중간 화면 최대 2열, 넓은 화면 최대 3열 |
| 폼 | label → 입력 → 검증/안내 → 행동 버튼 순서 |
| 거래 표 | 금액·상태·일시가 잘 읽히게 하고 모바일 가로 스크롤 |
| 상태 안내 | 로딩·오류·빈 목록·접근 불가를 구분 |

개별 페이지의 긴 제목·가격·이메일로 레이아웃이 깨지지 않게 합니다. 버튼 그룹은 actions의 줄바꿈을 허용합니다. 글자를 잘라야 한다면 중요한 정보를 확인할 별도 방법을 제공합니다.

## 10. 버튼·링크·입력 상세

| UI | 클래스 | 사용 |
|---|---|---|
| 주요 행동 | primary | 신청·저장·학습 시작 |
| 보조 행동 | secondary | 취소·뒤로·필터 초기화 |
| 위험 확인 전 | danger-outline | 해지·삭제 등 주의 행동 |
| 위험 확정 | danger | 확인한 해지·삭제 실행 |
| 가벼운 행동 | text-button | 다시 시도·로그아웃 등 |
| 전체 너비 | full | 폼 제출 등 |

주의 행동은 색상만 바꾸지 말고 대상·결과를 확인합니다. 해지 확인에는 ‘종료일까지 이용 가능’, 환불 승인에는 ‘즉시 이용 종료’를 구분해 안내합니다. 새 기능에 대한 확정 정책이 없으면 임의 문구를 만들지 않습니다.

```jsx
<form onSubmit={handleSubmit}>
  <label htmlFor="course-title">강의 제목</label>
  <input id="course-title" name="title" required maxLength={200} />
  <div className="actions">
    <button type="submit" className="primary" disabled={saving}>
      {saving ? '저장 중…' : '저장'}
    </button>
    <button type="button" className="secondary" onClick={handleCancel}>
      취소
    </button>
  </div>
</form>
```

예시의 handler/saving은 해당 페이지에서 구현해야 하는 값입니다. type 없는 button이 폼을 제출하는 문제를 피합니다. disabled일 때 상태 문구를 제공하고 성공 후 실제 서버 데이터를 재조회합니다.

입력·선택은 최소 44px, 입력 본문 16px, 배경 surface·테두리 border·모서리 8px 기준입니다. placeholder만 label 대신 사용하지 않습니다. 오류는 입력 가까이 제공하고 색상 외 설명을 함께 표시합니다.

## 11. 카드·상태·메시지 재사용

현재 StatusBadge는 기존 거래 상태를 표시합니다. 신규 강의 DRAFT/PUBLISHED/ARCHIVED나 학습 완료 상태가 필요하면 공통 배지 규칙과 번역표를 추가하고 기존 상태 매핑을 훼손하지 않습니다.

| 상태 의미 | 색상 계열 | 예 |
|---|---|---|
| 정상·성공 | success | 이용 중, 결제 완료 |
| 대기·주의 | warning | 해지 예약, 환불 검토 중 |
| 실패·거절 | danger | 결제 실패, 환불 반려 |
| 정보 | info | 환불 완료 |
| 비활성·기간 종료 | neutral | 만료 |

색상은 거래 상태 자체를 바꾸지 않습니다. CANCELLED는 ‘즉시 이용 불가’ 문구로 표시하지 않습니다. usable과 endDate를 함께 확인합니다.

```jsx
<ResourceState loading={loading} error={error} reload={reload} />
```

현재 기본 ResourceState는 로딩과 오류만 다룹니다. 로딩은 role=status, 오류는 role=alert를 사용합니다. 빈 목록은 별도 `.empty` 영역과 목적에 맞는 안내/이동 행동을 구현합니다. Pagination은 같은 파일의 named export입니다.

## 12. 학습 화면 상세 기준 — 신규 구현 목표

### 강의 탐색·상세

강의 카드에 제목·분야·난이도·학습 분량·포함 이용권을 일정한 순서로 배치합니다. 실제 데이터가 없는 시간·평점·학습자 수를 만들어 표시하지 않습니다. 목록과 상세에 같은 가격·기간 의미를 사용하고 강의와 이용권을 구분합니다.

상세에는 소개·목표·대상·커리큘럼·포함 상품을 안내합니다. 비회원은 로그인/상품 안내, 유효 이용권은 학습 행동, 만료는 재구독 안내 등 실제 서버 권한에 맞춰 표시하는 안을 제안합니다.

### 수강·진도

차시 목록과 현재 차시를 명확히 표시하고 본문·완료 버튼·이전/다음 이동을 제공합니다. 모바일에서는 차시 목록을 접거나 순서대로 보여주되 키보드 조작이 가능해야 합니다.

내 학습의 진도율은 서버의 완료/전체 차시 기준값입니다. 0개 강의·전부 완료·권한 만료·차시 비공개 전환을 각각 처리합니다. 로컬 숫자만 바꾸고 저장 성공으로 표시하지 않습니다.

수강권 검사와 콘텐츠 URL 보호는 서버 책임입니다. UI에서 수강 버튼을 숨기거나 진도 막대를 비활성화하는 것으로 차단이 완료되지 않습니다.

### 관리자

관리자 표·폼·공개 상태 선택은 공통 입력·버튼·상태 규칙을 사용합니다. 강의 공개, 상품 포함 강의 변경, 환불 승인처럼 사용자 접근에 영향을 주는 작업은 대상과 결과를 확인합니다. 삭제/비공개/판매 중지를 같은 동작으로 혼용하지 않습니다.

## 13. 접근성·반응형 검수

- 제목 계층 h1→h2→h3, 폼 label/id 연결.
- 링크는 이동, 버튼은 행동에 사용.
- Tab 이동과 focus-visible이 배경에 가려지지 않는지 확인.
- 오류·선택·완료는 색상과 문구 함께 표시.
- 로딩 중/오류 후 다음 행동을 제공.
- 모바일에서 버튼·탭·표 접근, 긴 제목·이메일·가격 줄바꿈 확인.
- 글자 확대 후 내용/행동이 사라지지 않는지 확인.
- 학습 진행 막대는 숫자와 대체 설명을 함께 제공하는 방식으로 구현.

접근성 검증 완료를 선언하려면 실제 화면/도구 확인이 필요합니다. 이 가이드는 기준이며 인증이나 검사 통과 증명서는 아닙니다.

## 14. 폰트·색상·토큰 변경 절차

Noto Sans KR가 설치되지 않으면 시스템 글꼴로 대체됩니다. 현재 폰트 파일을 제공하거나 원격 로드하지 않으므로 기기별로 글꼴이 완전히 같다는 보장은 없습니다. 동일 렌더링이 필요하면 팀에서 적법한 폰트 파일과 로딩 방식을 결정합니다.

토큰은 값의 단일 원본입니다. 새 색을 특정 페이지에 추가하기 전에 기존 semantic 색으로 표현 가능한지 확인합니다. 변경 시 토큰·가이드·영향 화면의 PC/모바일 검증을 함께 기록합니다. 빌드 성공이 색 대비나 레이아웃 확인을 대신하지 않습니다.

새 domain CSS는 `.course-*`, `.learning-*`처럼 범위를 좁히고 `button`, `body`, `h1` 전역 값을 덮어쓰지 않습니다. 현재 공통 클래스 정의는 subscriptions.css에 있으므로 별도 공통 CSS로 이동할 때 import 순서·기존 화면을 함께 검증합니다.

## 15. 추가 공통 토큰 사전

| 토큰 | 값 | 목적 |
|---|---|---|
| --sm-color-primary-border | #CDE5D9 | 선택 영역 테두리 |
| --sm-color-surface-subtle | #F3F7F5 | 보조 표면 |
| --sm-color-warning-border | #EDD8B5 | 주의 테두리 |
| --sm-color-danger-hover | #853939 | 위험 버튼 hover |
| --sm-color-danger-border | #E9CACA | 오류/위험 테두리 |
| --sm-color-neutral-soft | #EEF1F4 | 비활성 배지 배경 |
| --sm-leading-body | 1.6 | 본문 줄 높이 |
| --sm-leading-heading | 1.35 | 제목 줄 높이 |
| --sm-weight-regular | 400 | 본문 |
| --sm-weight-medium | 500 | 중간 강조 |
| --sm-weight-semibold | 600 | 버튼·중요 값 |
| --sm-weight-bold | 700 | 제목 |
| --sm-space-0 | 0 | 여백 제거 |
| --sm-radius-sm | 4px | 배지 |
| --sm-radius-md | 8px | 버튼·입력 |
| --sm-radius-lg | 12px | 카드·패널 |
| --sm-radius-full | 999px | pill 형태 필요 시 |
| --sm-shadow-card | 0 2px 8px rgb(36 51 46 / 5%) | 카드 그림자 |
| --sm-control-height | 44px | 조작 요소 최소 높이 |
| --sm-content-width | 1200px | 본문 최대 너비 |
| --sm-sidebar-width | 232px | 데스크톱 사이드바 |

success-soft/warning-soft/danger-soft/info-soft는 각각 `--sm-color-success-soft`, `--sm-color-warning-soft`, `--sm-color-danger-soft`, `--sm-color-info-soft` 전체 이름으로 사용합니다. font-family는 `--sm-font-family`입니다.

## 16. 구현·PR 완료 체크리스트

- [ ] 기존 클래스/컴포넌트 재사용 여부 확인
- [ ] 새 CSS 값이 --sm-* 토큰을 사용
- [ ] 버튼/입력 상태·포커스·키보드 확인
- [ ] loading/error/empty/success/접근 불가 처리
- [ ] PC/태블릿/모바일·긴 데이터 확인
- [ ] 서버 값과 가격/기간/권한/진도 표시 일치
- [ ] build/lint 통과 및 필요 API 테스트
- [ ] 실제 브라우저 확인 결과를 별도로 기록
- [ ] 공통 변경과 화면 캡처를 PR에 설명
- [ ] 가이드와 토큰 변경을 함께 반영

현재 스타일 작업의 build/lint 통과 기록은 있으며 실제 브라우저 화면 검수는 별도입니다. 신규 강의·학습 화면은 계획이므로 그 화면이 이미 검증되었다고 표시하지 않습니다.


---


