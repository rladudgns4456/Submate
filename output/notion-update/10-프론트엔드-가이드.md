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
