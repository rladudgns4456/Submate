import { useCallback, useState } from 'react'
import api, { errorMessage } from '../../api/axios'
import { refundApi } from '../../api/refundApi'
import { useResource } from '../../hooks/useResource'
import ResourceState, {
  Pagination,
} from '../../components/common/ResourceState'
import StatusBadge from '../../components/subscription/StatusBadge'
import { money, dateTime } from '../../utils/formatUtils'

export default function RefundsPage({ admin = false }) {
  const [page, setPage] = useState(0)
  const loader = useCallback(
    () =>
      admin
        ? api.get('/admin/refunds', { params: { page } }).then((r) => r.data)
        : refundApi.list(page),
    [admin, page]
  )
  const resource = useResource(loader)
  const [decision, setDecision] = useState(null)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  async function handleDecide() {
    setBusy(true)
    setError('')
    try {
      await refundApi.decide(decision.id, decision.value)
      setDecision(null)
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
          <p className="eyebrow">REFUNDS</p>
          <h1>{admin ? '환불 관리' : '환불 내역'}</h1>
          <p className="muted">요청부터 처리 완료까지, 환불 진행 상황입니다.</p>
        </div>
        <button className="secondary" onClick={resource.reload}>
          새로고침
        </button>
      </header>
      <ResourceState {...resource} />
      {error && (
        <p className="message error" role="alert">
          {error}
        </p>
      )}
      {decision && (
        <section className="confirm-box">
          <h2>
            환불 #{decision.id} {decision.value === 'APPROVE' ? '승인' : '반려'}
          </h2>
          <p>
            {decision.value === 'APPROVE'
              ? '요청 시 확정된 금액으로 환불 처리하며 구독 이용이 즉시 종료됩니다.'
              : '환불 요청을 반려합니다. 기존 결제와 구독은 유지됩니다.'}
          </p>
          <div className="actions">
            <button className="primary" disabled={busy} onClick={handleDecide}>
              {busy ? '처리 중…' : '처리 확정'}
            </button>
            <button
              className="secondary"
              disabled={busy}
              onClick={() => setDecision(null)}
            >
              취소
            </button>
          </div>
        </section>
      )}
      {resource.data && (
        <>
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>환불 / 결제</th>
                  <th>요청일</th>
                  <th>금액</th>
                  <th>사유</th>
                  <th>상태</th>
                  {admin && <th>처리</th>}
                </tr>
              </thead>
              <tbody>
                {resource.data.content.map((item) => (
                  <tr key={item.refundId}>
                    <td>
                      환불 #{item.refundId}
                      <br />
                      <span className="muted">결제 #{item.paymentId}</span>
                    </td>
                    <td>{dateTime(item.createdAt)}</td>
                    <td className="amount">
                      {money(item.amount)}
                      {item.totalDays != null && (
                        <>
                          <br />
                          <span className="muted">
                            남은 {item.remainingDays}일 / 전체 {item.totalDays}
                            일
                          </span>
                        </>
                      )}
                    </td>
                    <td className="reason">{item.reason}</td>
                    <td>
                      <StatusBadge status={item.status} />
                    </td>
                    {admin && (
                      <td>
                        {item.status === 'REQUESTED' && (
                          <div className="actions">
                            <button
                              disabled={busy}
                              className="text-button"
                              onClick={() =>
                                setDecision({
                                  id: item.refundId,
                                  value: 'APPROVE',
                                })
                              }
                            >
                              승인
                            </button>
                            <button
                              disabled={busy}
                              className="text-button danger-text"
                              onClick={() =>
                                setDecision({
                                  id: item.refundId,
                                  value: 'REJECT',
                                })
                              }
                            >
                              반려
                            </button>
                          </div>
                        )}
                      </td>
                    )}
                  </tr>
                ))}
              </tbody>
            </table>
            {resource.data.content.length === 0 && (
              <p className="empty">환불 요청 내역이 없습니다.</p>
            )}
          </div>
          <Pagination data={resource.data} setPage={setPage} />
        </>
      )}
    </>
  )
}
