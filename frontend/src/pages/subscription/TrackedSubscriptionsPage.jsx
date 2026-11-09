import { useState } from 'react'
import { trackedSubscriptionApi as api } from '../../api/trackedSubscriptionApi'
import { errorMessage } from '../../api/axios'
import ResourceState from '../../components/common/ResourceState'
import { useResource } from '../../hooks/useResource'
import { money } from '../../utils/formatUtils'
import GmailReceiptImport from './GmailReceiptImport'
import TrackedSubscriptionForm from './TrackedSubscriptionForm'

export default function TrackedSubscriptionsPage() {
  const dashboard = useResource(api.list)
  const [editor, setEditor] = useState(null)
  const [deleting, setDeleting] = useState(null)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const items = dashboard.data?.subscriptions || []
  const reminders = items.filter((item) => item.reminderDue)
  const next = items.find((item) => item.nextBillingDate)

  function open(item) {
    setDeleting(null)
    setEditor(item)
    setError('')
    setMessage('')
    requestAnimationFrame(() => {
      const panel = document.getElementById('tracker-editor')
      panel?.scrollIntoView({ block: 'nearest' })
      panel?.querySelector('input')?.focus({ preventScroll: true })
    })
  }

  async function change(action, successMessage) {
    setBusy(true)
    setError('')
    setMessage('')
    try {
      await action()
      setEditor(null)
      setDeleting(null)
      setMessage(successMessage)
      dashboard.reload()
    } catch (failure) {
      setError(errorMessage(failure))
    } finally {
      setBusy(false)
    }
  }

  function save(values) {
    if (editor.id)
      return change(() => api.update(editor.id, values), '구독을 수정했습니다.')
    if (editor.receiptId) {
      return change(
        () => api.importReceipt(editor.receiptId, values),
        '영수증에서 구독을 등록했습니다.'
      )
    }
    return change(() => api.create(values), '구독을 등록했습니다.')
  }

  return (
    <div className="tracker">
      <header className="tracker-heading">
        <div>
          <p className="eyebrow">SUBSCRIPTION TRACKER</p>
          <h1>내 외부 구독</h1>
          <p className="muted">
            여러 서비스의 구독과 다음 결제일을 한곳에서 관리하세요.
          </p>
        </div>
        <button
          type="button"
          className="primary"
          disabled={busy}
          onClick={() => open({})}
        >
          구독 추가
        </button>
      </header>

      <ResourceState {...dashboard} />
      {dashboard.data && (
        <div className="tracker-metrics">
          <section className="panel">
            <h2>{dashboard.data.month} 예상 결제액</h2>
            <strong>{money(dashboard.data.monthlyTotal)}</strong>
          </section>
          <section className="panel">
            <h2>가장 가까운 결제일</h2>
            <strong>{next?.nextBillingDate || '예정 없음'}</strong>
            {next && (
              <p className="muted">
                {next.name} · {money(next.amount)}
              </p>
            )}
          </section>
        </div>
      )}

      <section className="panel tracker-panel">
        <h2>결제일 알림</h2>
        {dashboard.data && reminders.length === 0 && (
          <p className="muted">현재 표시할 알림이 없습니다.</p>
        )}
        {reminders.length > 0 && (
          <ul>
            {reminders.map((item) => (
              <li key={item.id}>
                {item.nextBillingDate === dashboard.data.today
                  ? '오늘'
                  : item.nextBillingDate}{' '}
                · {item.name} · {money(item.amount)}
              </li>
            ))}
          </ul>
        )}
      </section>

      {message && (
        <p className="message success" role="status">
          {message}
        </p>
      )}
      {error && !editor && (
        <p className="message error" role="alert">
          {error}
        </p>
      )}

      {editor && (
        <section className="panel tracker-panel" id="tracker-editor">
          <h2>
            {editor.id
              ? '구독 수정'
              : editor.receiptId
                ? '영수증 내용 확인'
                : '구독 직접 등록'}
          </h2>
          {editor.receiptId && (
            <p className="muted">
              영수증에서 가져온 값과 결제일을 확인하고 빈 항목을 입력해 주세요.
            </p>
          )}
          {error && (
            <p className="message error" role="alert">
              {error}
            </p>
          )}
          <TrackedSubscriptionForm
            key={editor.id || editor.receiptId || 'new'}
            initial={editor}
            busy={busy}
            onSave={save}
            onClose={() => {
              setEditor(null)
              setError('')
            }}
          />
        </section>
      )}

      {dashboard.data && (
        <section className="panel tracker-panel">
          <h2>등록한 구독 ({items.length})</h2>
          {items.length === 0 ? (
            <p className="muted">아직 등록한 구독이 없습니다.</p>
          ) : (
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th scope="col">서비스</th>
                    <th scope="col">금액</th>
                    <th scope="col">주기</th>
                    <th scope="col">다음 결제</th>
                    <th scope="col">관리</th>
                  </tr>
                </thead>
                <tbody>
                  {items.map((item) => (
                    <tr key={item.id}>
                      <th scope="row">
                        {item.name}
                        {item.imported && (
                          <small className="tracker-source">Gmail</small>
                        )}
                      </th>
                      <td>{money(item.amount)}</td>
                      <td>
                        {item.billingCycle === 'YEARLY' ? '매년' : '매월'}
                      </td>
                      <td>{item.nextBillingDate || '중지'}</td>
                      <td>
                        <div className="tracker-actions">
                          {deleting === item.id ? (
                            <>
                              <button
                                type="button"
                                disabled={busy}
                                onClick={() =>
                                  change(
                                    () => api.remove(item.id),
                                    '구독을 삭제했습니다.'
                                  )
                                }
                              >
                                삭제 확인
                              </button>
                              <button
                                type="button"
                                disabled={busy}
                                onClick={() => {
                                  setDeleting(null)
                                  setError('')
                                }}
                              >
                                취소
                              </button>
                            </>
                          ) : (
                            <>
                              <button
                                type="button"
                                disabled={busy}
                                onClick={() => open(item)}
                              >
                                수정
                              </button>
                              <button
                                type="button"
                                disabled={busy}
                                onClick={() => {
                                  setEditor(null)
                                  setDeleting(item.id)
                                  setError('')
                                }}
                              >
                                삭제
                              </button>
                            </>
                          )}
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
      )}

      <GmailReceiptImport
        disabled={busy}
        onChoose={(receipt) =>
          open({
            receiptId: receipt.receiptId,
            name: receipt.name,
            amount: receipt.amount,
            billingCycle: receipt.billingCycle,
            billingDate: receipt.receiptDate,
          })
        }
      />
    </div>
  )
}
