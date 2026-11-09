import { useEffect, useRef, useState } from 'react'
import {
  fetchGooglePlayReceipts,
  loadGoogleIdentity,
} from '../../api/gmailReceipts'

const clientId = import.meta.env.VITE_GOOGLE_CLIENT_ID

export default function GmailReceiptImport({ onChoose, disabled }) {
  const [oauth, setOauth] = useState(null)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [receipts, setReceipts] = useState(null)
  const pending = useRef(null)

  useEffect(() => {
    if (!clientId) return
    let mounted = true
    loadGoogleIdentity().then(
      (sdk) => {
        if (mounted) setOauth(sdk)
      },
      (failure) => {
        if (mounted) setError(failure.message)
      }
    )
    return () => {
      mounted = false
      pending.current?.abort()
    }
  }, [])

  async function search() {
    if (pending.current) return
    const controller = new AbortController()
    pending.current = controller
    setBusy(true)
    setError('')
    setReceipts(null)
    try {
      const results = await fetchGooglePlayReceipts(
        oauth,
        clientId,
        controller.signal
      )
      if (!controller.signal.aborted) setReceipts(results)
    } catch (failure) {
      if (!controller.signal.aborted) setError(failure.message)
    } finally {
      if (!controller.signal.aborted) setBusy(false)
      pending.current = null
    }
  }

  return (
    <section className="panel tracker-panel">
      <h2>Gmail 영수증 불러오기</h2>
      <p className="muted">
        최근 1년의 Google Play 구독 영수증을 최대 25건 확인합니다. 이미 관리하는
        서비스가 있다면 기존 항목을 수정해 주세요.
      </p>
      {!clientId && (
        <p className="muted">
          이 환경에서 Google 계정 연결을 설정하면 사용할 수 있습니다.
        </p>
      )}
      {clientId && (
        <button
          type="button"
          disabled={!oauth || busy || disabled}
          onClick={search}
        >
          {busy ? '영수증 찾는 중…' : 'Google 계정 선택'}
        </button>
      )}
      {error && (
        <p className="message error" role="alert">
          {error}
        </p>
      )}
      {receipts?.length === 0 && (
        <p role="status">검색 조건에 맞는 구독 영수증이 없습니다.</p>
      )}
      {receipts?.length > 0 && (
        <ul className="tracker-receipts">
          {receipts.map((receipt) => (
            <li key={receipt.receiptId}>
              <span>
                {receipt.name || receipt.subject || 'Google Play 영수증'} ·{' '}
                {receipt.receiptDate}
              </span>
              <button
                type="button"
                disabled={disabled}
                onClick={() => onChoose(receipt)}
              >
                내용 확인
              </button>
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}
