import assert from 'node:assert/strict'
import test from 'node:test'
import {
  fetchGooglePlayReceipts,
  parseReceipt,
} from '../src/api/gmailReceipts.js'

const message = {
  id: 'message-1',
  internalDate: String(Date.parse('2026-11-08T16:00:00Z')),
  snippet: 'Item Sample Streaming ₩12,900 Monthly subscription',
  payload: {
    headers: [{ name: 'Subject', value: 'Google Play subscription receipt' }],
  },
}

test('Gmail 영수증에서 원화, 주기와 한국 날짜를 추출한다', () => {
  assert.deepEqual(parseReceipt(message), {
    messageId: 'message-1',
    subject: 'Google Play subscription receipt',
    receiptDate: '2026-11-09',
    name: 'Sample Streaming',
    amount: 12900,
    billingCycle: 'MONTHLY',
  })
})

test('불명확한 값은 입력을 요구하고 환불 영수증은 제외한다', () => {
  const ambiguous = parseReceipt({
    ...message,
    snippet: '연간 또는 월간 구독 ₩1,000 ₩2,000',
  })
  assert.equal(ambiguous.amount, '')
  assert.equal(ambiguous.billingCycle, '')
  assert.equal(
    parseReceipt({
      ...message,
      payload: { headers: [{ name: 'Subject', value: 'Subscription refund' }] },
    }),
    null
  )
})

test('승인한 Gmail 토큰으로 영수증을 읽고 메시지 ID 대신 해시를 반환한다', async (t) => {
  const requests = []
  t.mock.method(globalThis, 'fetch', async (input, options) => {
    const url = new URL(input)
    requests.push(url)
    assert.equal(url.origin, 'https://gmail.googleapis.com')
    assert.equal(options.headers.Authorization, 'Bearer test-token')
    const data = url.pathname.endsWith('/messages')
      ? { messages: [{ id: message.id }] }
      : message
    return Response.json(data)
  })
  const oauth = {
    initTokenClient(config) {
      assert.equal(
        config.scope,
        'https://www.googleapis.com/auth/gmail.readonly'
      )
      return {
        requestAccessToken: () =>
          config.callback({ access_token: 'test-token' }),
      }
    },
    hasGrantedAllScopes: () => true,
  }
  const [receipt] = await fetchGooglePlayReceipts(
    oauth,
    'test-client',
    new AbortController().signal
  )
  assert.equal(requests.length, 2)
  assert.match(
    requests[0].searchParams.get('q'),
    /from:googleplay-noreply@google.com/
  )
  assert.match(receipt.receiptId, /^gmail:[a-f0-9]{64}$/)
  assert.equal(receipt.messageId, undefined)
})
