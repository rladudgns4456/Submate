import { useCallback, useRef, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { subscriptionApi } from '../../api/subscriptionApi'
import { errorMessage } from '../../api/axios'
import { useResource } from '../../hooks/useResource'
import ResourceState from '../../components/common/ResourceState'
import { money } from '../../utils/formatUtils'

export default function PaymentPage() {
  const { productId } = useParams()
  const navigate = useNavigate()
  const loader = useCallback(
    () => subscriptionApi.quote(productId),
    [productId]
  )
  const resource = useResource(loader)
  const [method, setMethod] = useState('CARD')
  const [failure, setFailure] = useState(false)
  const [agreed, setAgreed] = useState(false)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const attempt = useRef(null)
  const submitting = useRef(false)
  async function handlePay(event) {
    event.preventDefault()
    if (!agreed || submitting.current) return
    submitting.current = true
    setBusy(true)
    setError('')
    const signature = `${productId}:${method}:${failure}`
    if (attempt.current?.signature !== signature)
      attempt.current = { signature, key: crypto.randomUUID() }
    try {
      const result = await subscriptionApi.checkout({
        productId: Number(productId),
        paymentMethod: method,
        requestKey: attempt.current.key,
        simulateFailure: failure,
      })
      navigate('/payment/result', { state: result, replace: true })
    } catch (problem) {
      setError(errorMessage(problem))
      // 응답이 유실된 경우 동일 키를 보존하여 안전하게 재시도한다.
    } finally {
      setBusy(false)
      submitting.current = false
    }
  }
  const product = resource.data
  return (
    <>
      <Link className="back-link" to="/subscriptions">
        ← 내 구독
      </Link>
      <header className="page-heading">
        <div>
          <p className="eyebrow">CHECKOUT</p>
          <h1>구독 신청</h1>
          <p className="muted">실제 금액이 청구되지 않는 가상 결제입니다.</p>
        </div>
      </header>
      <ResourceState {...resource} />
      {product && (
        <div className="checkout-grid">
          <section className="panel">
            <span className="product-icon large">
              {product.name.slice(0, 1)}
            </span>
            <h2>{product.name}</h2>
            <p className="price">
              {money(product.price)}{' '}
              <small>
                / {product.billingCycle === 'MONTHLY' ? '월' : '년'}
              </small>
            </p>
            <ul className="benefits">
              <li>결제 완료 후 바로 이용</li>
              <li>이용 기간 중 해지 신청 가능</li>
              <li>추가 자동 결제 없음</li>
            </ul>
            <p className="muted">
              최종 결제 금액과 이용 기간은 결제 시점의 상품 정보를 기준으로
              서버에서 결정됩니다.
            </p>
          </section>
          <form className="panel" onSubmit={handlePay}>
            <h2>결제 정보</h2>
            <fieldset disabled={busy}>
              <legend>가상 결제 수단</legend>
              <label className="choice">
                <input
                  type="radio"
                  name="method"
                  value="CARD"
                  checked={method === 'CARD'}
                  onChange={(e) => setMethod(e.target.value)}
                />
                카드
              </label>
              <label className="choice">
                <input
                  type="radio"
                  name="method"
                  value="BANK_TRANSFER"
                  checked={method === 'BANK_TRANSFER'}
                  onChange={(e) => setMethod(e.target.value)}
                />
                계좌이체
              </label>
              <label className="check">
                <input
                  type="checkbox"
                  checked={agreed}
                  onChange={(e) => setAgreed(e.target.checked)}
                />
                가상 결제 금액과 구독 신청 내용을 확인했습니다.
              </label>
              <details>
                <summary>가상 결제 실패 확인</summary>
                <label className="check">
                  <input
                    type="checkbox"
                    checked={failure}
                    onChange={(e) => setFailure(e.target.checked)}
                  />
                  이번 결제를 실패로 처리
                </label>
              </details>
            </fieldset>
            {error && (
              <p className="message error" role="alert">
                {error}
              </p>
            )}
            <button className="primary full" disabled={busy || !agreed}>
              {busy ? '결제 처리 중…' : `${money(product.price)} 가상 결제`}
            </button>
            <p className="fine">
              카드번호나 계좌번호를 입력하거나 저장하지 않습니다.
            </p>
          </form>
        </div>
      )}
    </>
  )
}
