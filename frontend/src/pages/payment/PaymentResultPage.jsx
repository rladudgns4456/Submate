import { Link, useLocation } from 'react-router-dom'
import { money } from '../../utils/formatUtils'
export default function PaymentResultPage() {
  const { state } = useLocation()
  if (!state?.subscription)
    return (
      <section className="empty">
        <h1>결제 내역을 확인해 주세요</h1>
        <Link className="button primary" to="/payments">
          결제 내역 보기
        </Link>
      </section>
    )
  if (state.payment.status !== 'COMPLETED')
    return (
      <section className="panel result">
        <h1>이미 처리된 결제입니다</h1>
        <p>현재 구독과 결제 상태를 확인해 주세요.</p>
        <Link className="button primary" to="/payments">
          결제 내역 확인
        </Link>
      </section>
    )
  return (
    <section className="panel result">
      <span className="result-check">✓</span>
      <p className="eyebrow">SUBSCRIPTION CONFIRMED</p>
      <h1>구독이 시작됐어요</h1>
      <p>{state.subscription.productName}의 가상 결제가 완료되었습니다.</p>
      <dl>
        <div>
          <dt>결제 금액</dt>
          <dd>{money(state.payment.amount)}</dd>
        </div>
        <div>
          <dt>구독 번호</dt>
          <dd>#{state.subscription.subscriptionId}</dd>
        </div>
      </dl>
      <Link
        className="button primary"
        to={`/subscriptions/${state.subscription.subscriptionId}`}
      >
        내 구독 확인하기
      </Link>
    </section>
  )
}
