const scope = 'https://www.googleapis.com/auth/gmail.readonly'
const apiRoot = 'https://gmail.googleapis.com/gmail/v1/users/me/messages'
let sdkLoad

export function loadGoogleIdentity() {
  if (!sdkLoad) {
    sdkLoad = new Promise((resolve, reject) => {
      const script = document.createElement('script')
      script.src = 'https://accounts.google.com/gsi/client'
      function unavailable() {
        script.remove()
        sdkLoad = undefined
        reject(new Error('Google 연결을 준비하지 못했습니다.'))
      }
      script.onload = () => {
        const oauth = window.google?.accounts?.oauth2
        if (oauth) resolve(oauth)
        else unavailable()
      }
      script.onerror = unavailable
      document.head.append(script)
    })
  }
  return sdkLoad
}

function authorize(oauth, clientId) {
  return new Promise((resolve, reject) => {
    const client = oauth.initTokenClient({
      client_id: clientId,
      scope,
      include_granted_scopes: false,
      callback: (response) => {
        if (
          !response.access_token ||
          !oauth.hasGrantedAllScopes(response, scope)
        ) {
          reject(new Error('영수증을 불러오려면 Gmail 읽기 권한이 필요합니다.'))
          return
        }
        resolve(response.access_token)
      },
      error_callback: () =>
        reject(new Error('Google 인증 창이 닫혔거나 열리지 않았습니다.')),
    })
    client.requestAccessToken({ prompt: 'select_account' })
  })
}

export function parseReceipt(message) {
  const subject =
    message.payload?.headers?.find(
      ({ name }) => name.toLowerCase() === 'subject'
    )?.value || ''
  const snippet = (message.snippet || '')
    .replaceAll('&amp;', '&')
    .replaceAll('&#39;', "'")
  const text = subject + ' ' + snippet
  if (!/subscription|구독/i.test(text) || /refund|환불|취소됨/i.test(subject))
    return null

  const annual = /\b(?:yearly|annual|annually|per year)\b|연간|매년/i.test(text)
  const monthly = /\b(?:monthly|per month)\b|월간|매월/i.test(text)
  const won = Array.from(
    text.matchAll(/(?:₩|KRW)\s*([\d,]+)(?![\d,.])|([\d,]+)\s*원/g),
    (match) => Number((match[1] || match[2]).replaceAll(',', ''))
  )
  const amounts = [
    ...new Set(
      won.filter(
        (amount) =>
          Number.isInteger(amount) && amount > 0 && amount <= 999999999
      )
    ),
  ]
  const name = snippet.match(
    /(?:^|\s)(?:Item|항목)\s*[:：]?\s+(.+?)\s+(?=(?:₩|KRW)\s*\d|[\d,]+\s*원)/i
  )?.[1]
  const timestamp = Number(message.internalDate)
  if (
    !message.id ||
    !Number.isSafeInteger(timestamp) ||
    timestamp <= 0 ||
    timestamp > 8.64e15
  )
    return null
  const receiptDate = new Intl.DateTimeFormat('sv-SE', {
    timeZone: 'Asia/Seoul',
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  }).format(timestamp)

  return {
    messageId: message.id,
    subject,
    receiptDate,
    name: name?.trim().slice(0, 100) || '',
    amount: amounts.length === 1 ? amounts[0] : '',
    billingCycle: annual === monthly ? '' : annual ? 'YEARLY' : 'MONTHLY',
  }
}

async function receiptKey(messageId) {
  const digest = await crypto.subtle.digest(
    'SHA-256',
    new TextEncoder().encode(messageId)
  )
  const hex = Array.from(new Uint8Array(digest), (byte) =>
    byte.toString(16).padStart(2, '0')
  ).join('')
  return 'gmail:' + hex
}

export async function fetchGooglePlayReceipts(oauth, clientId, signal) {
  const token = await authorize(oauth, clientId)

  async function get(path, params) {
    const response = await fetch(
      apiRoot + path + '?' + new URLSearchParams(params),
      {
        headers: { Authorization: 'Bearer ' + token },
        signal: AbortSignal.any([signal, AbortSignal.timeout(15000)]),
      }
    )
    if (!response.ok) {
      throw new Error(
        'Gmail에서 영수증을 읽지 못했습니다. Google 계정 권한을 확인해 주세요.'
      )
    }
    return response.json()
  }

  const result = await get('', {
    q: 'from:googleplay-noreply@google.com newer_than:1y {subscription 구독}',
    maxResults: '25',
    fields: 'messages(id)',
  })
  const messages = await Promise.all(
    (result.messages || []).map(({ id }) =>
      get('/' + encodeURIComponent(id), {
        format: 'full',
        fields: 'id,internalDate,snippet,payload(headers(name,value))',
      })
    )
  )
  const receipts = messages.map(parseReceipt).filter(Boolean)
  return Promise.all(
    receipts.map(async ({ messageId, ...receipt }) => ({
      ...receipt,
      receiptId: await receiptKey(messageId),
    }))
  )
}
