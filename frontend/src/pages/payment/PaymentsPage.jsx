import { useCallback, useState } from 'react'
import { Link } from 'react-router-dom'
import { paymentApi } from '../../api/paymentApi'
import api, { errorMessage } from '../../api/axios'
import { refundApi } from '../../api/refundApi'
import { useResource } from '../../hooks/useResource'
import ResourceState, {
  Pagination,
} from '../../components/common/ResourceState'
import StatusBadge from '../../components/subscription/StatusBadge'
import { money, dateTime } from '../../utils/formatUtils'

export default function PaymentsPage({ admin = false }) {
  const [page, setPage] = useState(0)
  const loader = useCallback(
    () =>
      admin
        ? api.get('/admin/payments', { params: { page } }).then((r) => r.data)
        : paymentApi.list(page),
    [admin, page]
  )
  const resource = useResource(loader)
  const [selected, setSelected] = useState(null)
  const [reason, setReason] = useState('')
  const [busy, setBusy] = useState(false)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  async function handleRefund(event) {
    event.preventDefault()
    setBusy(true)
    setError('')
    setMessage('')
    try {
      await refundApi.request(selected, reason.trim())
      setSelected(null)
      setReason('')
      setMessage(
        '환불 요청이 접수되었습니다. 환불 내역에서 처리 상태를 확인하세요.'
      )
      resource.reload()
    } catch (failure) {
      setError(errorMessage(failure))
    } finally {
      setBusy(false)
    }
  }
  return (
    <>
      <header className="page-heading">
        <div>
          <p className="eyebrow">PAYMENTS</p>
          <h1>{admin ? '결제 관리' : '결제 내역'}</h1>
          <p className="muted">결제 금액과 환불 상태를 한눈에 확인하세요.</p>
        </div>
        <button className="secondary" onClick={resource.reload}>
          새로고침
        </button>
      </header>
      {message && (
        <p className="message success" role="status">
          {message} <Link to="/refunds">환불 내역 →</Link>
        </p>
      )}
      {error && (
        <p className="message error" role="alert">
          {error}
        </p>
      )}
      <ResourceState {...resource} />
      {resource.data && (
        <>
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>결제 / 구독</th>
                  <th>결제일</th>
                  <th>금액</th>
                  <th>수단</th>
                  <th>상태</th>
                  {!admin && <th>관리</th>}
                </tr>
              </thead>
              <tbody>
                {resource.data.content.map((item) => (
                  <tr key={item.paymentId}>
                    <td>
                      #{item.paymentId}
                      <br />
                      <Link
                        to={`${admin ? '/admin' : ''}/subscriptions/${item.subscriptionId}`}
                      >
                        구독 #{item.subscriptionId}
                      </Link>
                    </td>
                    <td>{dateTime(item.paidAt)}</td>
                    <td className="amount">{money(item.amount)}</td>
                    <td>
                      {item.paymentMethod === 'CARD' ? '카드' : '계좌이체'}
                    </td>
                    <td>
                      <StatusBadge status={item.status} />
                    </td>
                    {!admin && (
                      <td>
                        {item.status === 'COMPLETED' && (
                          <button
                            className="text-button"
                            onClick={() => {
                              setSelected(item.paymentId)
                              setError('')
                              setReason('')
                            }}
                          >
                            환불 요청
                          </button>
                        )}
                      </td>
                    )}
                  </tr>
                ))}
              </tbody>
            </table>
            {resource.data.content.length === 0 && (
              <p className="empty">결제 내역이 없습니다.</p>
            )}
          </div>
          <Pagination data={resource.data} setPage={setPage} />
        </>
      )}
      {selected && (
        <form className="panel refund-form" onSubmit={handleRefund}>
          <h2>결제 #{selected} 환불 요청</h2>
          <p className="muted">
            요청일 기준 남은 일수에 비례해 환불하며 원 미만은 버립니다. 요청
            당일은 남은 일수에 포함됩니다. 관리자 승인 시 이용이 종료됩니다.
            결제당 한 번만 요청할 수 있습니다.
          </p>
          <label>
            환불 사유
            <textarea
              autoFocus
              required
              maxLength={255}
              value={reason}
              disabled={busy}
              onChange={(e) => setReason(e.target.value)}
            />
          </label>
          <div className="actions">
            <button className="primary" disabled={busy || !reason.trim()}>
              {busy ? '접수 중…' : '환불 요청 접수'}
            </button>
            <button
              type="button"
              className="secondary"
              disabled={busy}
              onClick={() => setSelected(null)}
            >
              취소
            </button>
          </div>
        </form>
      )}
    </>
  )
}
