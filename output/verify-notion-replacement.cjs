const fs = require('fs')
const path = require('path')
const crypto = require('crypto')
const base = path.resolve('output/notion-replacement')
const files = fs.readdirSync(base).filter(n => /^\d\d-.*\.md$/.test(n) && !n.startsWith('12-'))
const checks = []
const add = (name, ok, detail) => checks.push({ name, ok, detail })
let jsonCount = 0
for (const file of files) {
  const body = fs.readFileSync(path.join(base, file), 'utf8').replace(/^\uFEFF/, '')
  add(`${file} 제목`, body.startsWith('# '), '최상위 문서 제목')
  add(`${file} 코드 블록`, (body.match(/^```/gm) || []).length % 2 === 0, '열기/닫기 개수')
  for (const match of body.matchAll(/^```json\r?\n([\s\S]*?)^```/gm)) {
    try { JSON.parse(match[1]); jsonCount++ }
    catch (error) { add(`${file} JSON`, false, error.message) }
  }
  add(`${file} 문자`, !body.includes('\uFFFD'), 'UTF-8 대체 문자 없음')
}
add('교체 본문 10개', files.filter(n => /^(0[1-9]|10)-/.test(n)).length === 10, '01~10 본문')
const api = fs.readFileSync(path.join(base, '05-API-명세.md'), 'utf8')
const db = fs.readFileSync(path.join(base, '03-DB-ERD.md'), 'utf8')
const guide = fs.readFileSync(path.join(base, '10-프론트엔드-가이드.md'), 'utf8')
const javaBase = 'backend/src/main/java/com/submate/backend/'
const controllers = [
  'subscription/controller/SubscriptionController.java',
  'payment/controller/PaymentController.java',
  'refund/controller/RefundController.java',
  'admin/subscription/controller/SubscriptionAdminController.java',
]
let routeCount = 0
for (const file of controllers) {
  const body = fs.readFileSync(javaBase + file, 'utf8')
  const prefix = body.match(/@RequestMapping\("([^"]+)"\)/)[1]
  for (const m of body.matchAll(/@(Get|Post|Patch|Put|Delete)Mapping(?:\("([^"]*)"\))?/g)) {
    const route = prefix + (m[2] || '')
    const suffix = route.slice(4)
    const method = m[1].toUpperCase()
    const tableLine = api.split('\n').some(line => line.includes(`| ${method} |`) && line.includes(`| ${suffix}`))
    add(`${method} ${route}`, tableLine, '현재 구현 경로 표 존재')
    routeCount++
  }
}
const dtoFiles = [
  'payment/dto/CheckoutRequest.java', 'payment/dto/CheckoutResponse.java',
  'payment/dto/PaymentResponse.java', 'refund/dto/RefundRequest.java',
  'refund/dto/RefundDecisionRequest.java', 'refund/dto/RefundResponse.java',
  'subscription/dto/SubscriptionResponse.java', 'subscription/dto/PageResponse.java',
]
let dtoFieldCount = 0
for (const file of dtoFiles) {
  const code = fs.readFileSync(javaBase + file, 'utf8')
  const header = code.match(/public record [^{]+/)[0]
  for (const m of header.matchAll(/\b(?:Long|String|Boolean|boolean|int|long|BigDecimal|LocalDateTime|LocalDate|PaymentMethod|PaymentStatus|RefundStatus|SubscriptionStatus|SubscriptionResponse|PaymentResponse|Decision|List<T>)\s+(\w+)\s*(?=[,)])/g)) {
    add(`${path.basename(file)}.${m[1]}`, api.includes(m[1]), '현재 DTO 필드명 문서 존재')
    dtoFieldCount++
  }
}
const schema = fs.readFileSync('backend/src/main/resources/db/subscription-schema.sql', 'utf8')
let dbFieldCount = 0
for (const table of schema.matchAll(/CREATE TABLE (\w+) \(([\s\S]*?)\n\);/g)) {
  for (const field of table[2].matchAll(/^\s+(\w+)\s+(?:BIGINT|VARCHAR|DATE|TIMESTAMP|DECIMAL)\b/gm)) {
    add(`${table[1]}.${field[1]}`, db.includes(`| ${field[1]} |`), '현재 DDL 컬럼의 상세표 존재')
    dbFieldCount++
  }
}
const originalDb = fs.readFileSync('C:/Users/rladu/.codex/attachments/215da897-7836-422f-8f40-5d10d133b377/붙여넣은 텍스트.txt', 'utf8')
const originalFields = [...new Set([...originalDb.matchAll(/^\| ([a-z][a-z_]+) \| (?:BIGINT|VARCHAR|TEXT|DECIMAL|DATE)/gm)].map(m => m[1]))]
for (const field of originalFields) add(`원문 DB 컬럼 ${field}`, db.includes(field), '원문 필드명 보존/교정 위치 존재')
const tokens = [...fs.readFileSync('frontend/src/styles/tokens.css', 'utf8').matchAll(/(--sm-[a-z0-9-]+)\s*:/g)].map(m => m[1])
for (const token of tokens) add(`토큰 ${token}`, guide.includes(token), '실제 토큰 이름 가이드 존재')
const a = fs.readFileSync('C:/Users/rladu/.codex/attachments/43a7fa02-730c-42a9-a3df-80ba11ee9a26/붙여넣은 텍스트.txt')
const b = fs.readFileSync('C:/Users/rladu/.codex/attachments/50eeffde-253a-4015-8a9c-596cb9fbb769/붙여넣은 텍스트.txt')
add('중복 Git 원문', crypto.createHash('sha256').update(a).digest('hex') === crypto.createHash('sha256').update(b).digest('hex'), '두 첨부 내용 동일')
add('JSON 예시 파싱', jsonCount > 0, `${jsonCount}개 JSON 블록 파싱`)
const failures = checks.filter(c => !c.ok)
const lines = [
  '# SubMate 문서 정적 검증 결과', '',
  '검사 기준일: 2026-10-19. 원문 주요 주제 대조는 11번 문서에 별도 기록했습니다.', '',
  '## 검사 요약', '',
  `- 검사한 Markdown: ${files.length}개(교체 본문 10개와 안내/대조표).`,
  `- Controller 경로: ${routeCount}개.`,
  `- 현재 DTO 필드 출현 검사: ${dtoFieldCount}개.`,
  `- 현재 거래 DDL 컬럼: ${dbFieldCount}개.`,
  `- 원문 DB 고유 필드명: ${originalFields.length}개.`,
  `- 실제 디자인 토큰: ${tokens.length}개.`,
  `- JSON 코드 예시: ${jsonCount}개.`,
  `- 정적 검사 ${checks.length}건 중 통과 ${checks.length - failures.length}, 실패 ${failures.length}.`, '',
  '## 해석과 한계', '',
  '필드명/경로/토큰 존재, 코드 블록과 JSON 형식 검사는 문서 누락을 줄이기 위한 보조 검사입니다. 값·권한·업무 의미의 전체 정확성이나 실제 서비스 동작을 자동 증명하지 않습니다. 필수/NULL/길이·enum·업무 조건은 DTO·Service·DDL을 읽고 대조했습니다. 미구현 계약은 제안으로 표시했습니다.', '',
  '이번 작업은 문서 변경입니다. Backend 테스트·실제 JWT/MySQL·브라우저 E2E를 새로 실행하지 않았고, 강의·학습 기능을 구현하지 않았습니다. 기존 백엔드 27건 통과 기록과 이번 정적 문서 검사를 구분합니다.', '',
  '## 상세 결과', '', '| 검사 | 결과 | 범위 |', '|---|---|---|',
  ...checks.map(c => `| ${c.name} | ${c.ok ? '통과' : '실패'} | ${c.detail.replace(/\|/g, '/')} |`), '',
]
fs.writeFileSync(path.join(base, '12-문서-검증-결과.md'), lines.join('\n'), 'utf8')
console.log(JSON.stringify({ files: files.length, routes: routeCount, dtoFields: dtoFieldCount, ddlColumns: dbFieldCount, tokens: tokens.length, jsonExamples: jsonCount, checks: checks.length, failures }, null, 2))
process.exitCode = failures.length ? 1 : 0
