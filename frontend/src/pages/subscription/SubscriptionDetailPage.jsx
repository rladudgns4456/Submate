import { useCallback, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { subscriptionApi } from '../../api/subscriptionApi'
import { errorMessage } from '../../api/axios'
import { useResource } from '../../hooks/useResource'
import ResourceState from '../../components/common/ResourceState'
import StatusBadge from '../../components/subscription/StatusBadge'
import { dateTime } from '../../utils/formatUtils'

export default function SubscriptionDetailPage({ admin = false }) {
  const { id } = useParams()
  const loader = useCallback(
    () => subscriptionApi.detail(id, admin),
    [id, admin]
  )
  const resource = useResource(loader)
  const [confirming, setConfirming] = useState(false)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')
  async function handleCancel() {
    setBusy(true)
    setError('')
    try {
      await subscriptionApi.cancel(id, admin)
      setConfirming(false)
      setNotice('해지가 예약되었습니다. 기존 종료 시점까지 이용할 수 있어요.')
      resource.reload()
    } catch (failure) {
      setError(errorMessage(failure))
    } finally {
      setBusy(false)
    }
  }
  const item = resource.data
  return (
    <>
      <Link
        className="back-link"
        to={admin ? '/admin/subscriptions' : '/subscriptions'}
      >
        ← 구독 목록
      </Link>
      <header className="page-heading">
        <div>
          <p className="eyebrow">SUBSCRIPTION DETAIL</p>
          <h1>구독 상세</h1>
        </div>
      </header>
      <ResourceState {...resource} />
      {notice && (
        <p className="message success" role="status">
          {notice}
        </p>
      )}
      {error && (
        <p className="message error" role="alert">
          {error}
        </p>
      )}
      {item && (
        <section className="panel detail">
          <div className="card-top">
            <h2>{item.productName}</h2>
            <StatusBadge status={item.status} />
          </div>
          <dl>
            <div>
              <dt>구독 번호</dt>
              <dd>#{item.subscriptionId}</dd>
            </div>
            <div>
              <dt>시작일</dt>
              <dd>{item.startDate}</dd>
            </div>
            <div>
              <dt>이용 종료 시점 (한국 시간)</dt>
              <dd>{item.endDate} 00:00</dd>
            </div>
            <div>
              <dt>현재 이용 가능</dt>
              <dd>{item.usable ? '가능' : '불가능'}</dd>
            </div>
            <div>
              <dt>해지 신청</dt>
              <dd>{dateTime(item.cancelledAt)}</dd>
            </div>
          </dl>
          <p className="muted">
            해지는 남은 기간의 이용을 유지합니다. 환불은 결제 내역에서 별도로
            요청할 수 있어요.
          </p>
          {item.status === 'ACTIVE' && item.usable && !confirming && (
            <button
              className="danger-outline"
              onClick={() => setConfirming(true)}
            >
              구독 해지
            </button>
          )}
          {confirming && (
            <div className="confirm-box">
              <h3>구독을 해지하시겠어요?</h3>
              <p>
                {item.endDate} 00:00까지 이용할 수 있으며, 자동 환불은 발생하지
                않습니다.
              </p>
              <div className="actions">
                <button
                  disabled={busy}
                  className="danger"
                  onClick={handleCancel}
                >
                  {busy ? '처리 중…' : '해지 확정'}
                </button>
                <button
                  disabled={busy}
                  className="secondary"
                  onClick={() => setConfirming(false)}
                >
                  계속 이용
                </button>
              </div>
            </div>
          )}
        </section>
      )}
    </>
  )
}
