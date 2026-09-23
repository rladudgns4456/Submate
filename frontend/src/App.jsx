import { useState } from 'react'
import { createApi, errorMessage } from './api'
import './App.css'

const money = (value) => new Intl.NumberFormat('ko-KR').format(value) + '원'
const labels = {
  ACTIVE: '이용 중',
  CANCELLED: '해지 신청',
  EXPIRED: '이용 종료',
  SUCCESS: '결제 완료',
  FAILED: '결제 실패',
  REFUNDED: '환불 완료',
  REQUESTED: '승인 대기',
  APPROVED: '환불 승인',
  REJECTED: '환불 반려',
}
function Badge({ status }) {
  return <span className={'badge ' + status}>{labels[status] || status}</span>
}
function Empty({ children }) {
  return <div className="empty">{children}</div>
}

export default function App() {
  const [session, setSession] = useState(null)
  const [login, setLogin] = useState({ username: '', password: '' })
  const [page, setPage] = useState('plans')
  const [data, setData] = useState({
    plans: [],
    subscriptions: [],
    payments: [],
    refunds: [],
  })
  const [selected, setSelected] = useState(null)
  const [checkout, setCheckout] = useState(null)
  const [result, setResult] = useState(null)
  const [refundPayment, setRefundPayment] = useState(null)
  const [reason, setReason] = useState('')
  const [note, setNote] = useState('')
  const [decision, setDecision] = useState(null)
  const [cancelTarget, setCancelTarget] = useState(null)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')
  const admin = session?.admin
  const prefix = admin ? '/admin' : ''

  async function refresh(current = session) {
    const root = current.admin ? '/admin' : ''
    const responses = await Promise.all(
      [
        '/plans',
        root + '/subscriptions',
        root + '/payments',
        root + '/refunds',
      ].map((path) => current.api.get(path))
    )
    setData(
      Object.fromEntries(
        ['plans', 'subscriptions', 'payments', 'refunds'].map((key, index) => [
          key,
          responses[index].data,
        ])
      )
    )
  }

  async function action(work) {
    setBusy(true)
    setError('')
    setNotice('')
    try {
      await work()
    } catch (e) {
      setError(errorMessage(e))
    } finally {
      setBusy(false)
    }
  }

  async function signIn(event) {
    event.preventDefault()
    await action(async () => {
      const api = createApi(login.username, login.password)
      const { data: user } = await api.get('/me')
      await refresh({ api, ...user })
      setSession({ api, ...user })
      setPage(user.admin ? 'subscriptions' : 'plans')
      setLogin({ username: '', password: '' })
    })
  }
  function navigate(next) {
    setPage(next)
    setSelected(null)
    setCheckout(null)
    setResult(null)
    setRefundPayment(null)
    setDecision(null)
    setCancelTarget(null)
    setError('')
    setNotice('')
  }
  async function pay(success) {
    await action(async () => {
      const { data: payment } = await session.api.post('/subscriptions', {
        planId: checkout.plan.id,
        requestKey: checkout.key,
        success,
      })
      setResult(payment)
      setCheckout(null)
      await refresh()
    })
  }
  async function showDetail(id) {
    await action(async () => {
      const response = await session.api.get(prefix + '/subscriptions/' + id)
      setSelected(response.data)
    })
  }
  const tabs = admin
    ? [
        ['subscriptions', '구독 관리'],
        ['payments', '결제 관리'],
        ['refunds', '환불 관리'],
      ]
    : [
        ['plans', '구독 신청'],
        ['subscriptions', '내 구독'],
        ['payments', '결제 내역'],
        ['refunds', '환불 내역'],
      ]

  return (
    <div className="app-shell">
      <header>
        <a
          className="brand"
          href="#home"
          onClick={() => session && navigate(admin ? 'subscriptions' : 'plans')}
        >
          <span className="brand-icon">S</span>SubMate
        </a>
        {session && (
          <div className="account">
            <span>
              {session.username} · {admin ? '관리자' : '멤버'}
            </span>
            <button
              className="quiet"
              disabled={busy}
              onClick={() => {
                setSession(null)
                setData({
                  plans: [],
                  subscriptions: [],
                  payments: [],
                  refunds: [],
                })
                navigate('plans')
              }}
            >
              로그아웃
            </button>
          </div>
        )}
      </header>
      {!session ? (
        <main className="login-layout">
          <div>
            <p className="eyebrow">YOUR SUBSCRIPTIONS, SIMPLIFIED</p>
            <h1>
              구독의 시작부터
              <br />
              마지막까지, 한곳에서.
            </h1>
            <p className="muted">
              구독과 결제 내역을 확인하고
              <br />
              필요할 때 간편하게 관리하세요.
            </p>
          </div>
          <form className="panel login" onSubmit={signIn}>
            <h2>로그인</h2>
            <label>
              아이디
              <input
                autoComplete="username"
                required
                value={login.username}
                onChange={(e) =>
                  setLogin({ ...login, username: e.target.value })
                }
              />
            </label>
            <label>
              비밀번호
              <input
                type="password"
                autoComplete="current-password"
                required
                value={login.password}
                onChange={(e) =>
                  setLogin({ ...login, password: e.target.value })
                }
              />
            </label>
            {error && (
              <p role="alert" className="error">
                {error}
              </p>
            )}
            <button disabled={busy}>{busy ? '확인 중…' : '로그인'}</button>
          </form>
        </main>
      ) : (
        <>
          <nav aria-label="주 메뉴">
            {tabs.map(([key, text]) => (
              <button
                key={key}
                className={page === key ? 'tab active' : 'tab'}
                aria-current={page === key ? 'page' : undefined}
                disabled={busy}
                onClick={() => navigate(key)}
              >
                {text}
              </button>
            ))}
          </nav>
          <main>
            <div className="page-heading">
              <div>
                <p className="eyebrow">
                  {admin ? 'MANAGEMENT' : 'MY MEMBERSHIP'}
                </p>
                <h1>{tabs.find(([key]) => key === page)?.[1]}</h1>
              </div>
              <button
                className="quiet"
                disabled={busy}
                onClick={() =>
                  action(async () => {
                    await refresh()
                    setSelected(null)
                  })
                }
              >
                새로고침
              </button>
            </div>
            {error && (
              <div className="error" role="alert">
                {error}
              </div>
            )}
            {notice && (
              <div className="success" role="status">
                {notice}
              </div>
            )}
            <fieldset className="workspace" disabled={busy}>
              {result ? (
                <section className="panel completion">
                  <Badge status={result.status} />
                  <h2>
                    {result.status === 'FAILED'
                      ? '결제가 완료되지 않았어요'
                      : '구독이 시작되었어요'}
                  </h2>
                  <p>
                    {result.status === 'FAILED'
                      ? '구독은 생성되지 않았습니다. 다시 신청해 주세요.'
                      : '결제와 구독 내역에서 자세한 내용을 확인할 수 있어요.'}
                  </p>
                  <strong>{money(result.amount)}</strong>
                  <button
                    onClick={() =>
                      navigate(
                        result.status === 'FAILED' ? 'plans' : 'subscriptions'
                      )
                    }
                  >
                    {result.status === 'FAILED'
                      ? '다시 신청하기'
                      : '내 구독 보기'}
                  </button>
                </section>
              ) : checkout ? (
                <section className="panel checkout">
                  <p className="eyebrow">CHECKOUT</p>
                  <h2>가상 결제</h2>
                  <p>
                    {checkout.plan.name} · {checkout.plan.months}개월
                  </p>
                  <strong className="price">
                    {money(checkout.plan.price)}
                  </strong>
                  <p className="muted">
                    실제 금액은 청구되지 않습니다. 결제 성공 시 구독이
                    시작됩니다.
                  </p>
                  <div className="actions">
                    <button onClick={() => pay(true)}>결제하기</button>
                    <button className="quiet" onClick={() => pay(false)}>
                      실패 결제 테스트
                    </button>
                    <button className="quiet" onClick={() => setCheckout(null)}>
                      돌아가기
                    </button>
                  </div>
                </section>
              ) : page === 'plans' ? (
                <>
                  <p className="intro">나에게 맞는 구독을 선택하세요.</p>
                  <div className="plan-grid">
                    {data.plans.map((plan) => (
                      <article className="panel plan" key={plan.id}>
                        <p className="eyebrow">
                          {plan.months} MONTH MEMBERSHIP
                        </p>
                        <h2>{plan.name}</h2>
                        <p className="muted">{plan.description}</p>
                        <div className="price">
                          {money(plan.price)}
                          <small> / {plan.months}개월</small>
                        </div>
                        <p>
                          결제일부터 이용 시작
                          <br />
                          자동 결제 없이 기간만큼 이용
                        </p>
                        <button
                          onClick={() =>
                            setCheckout({ plan, key: crypto.randomUUID() })
                          }
                        >
                          구독 신청
                        </button>
                      </article>
                    ))}
                  </div>
                </>
              ) : null}
              {page === 'subscriptions' && !result && (
                <>
                  {data.subscriptions.length === 0 ? (
                    <Empty>아직 구독 내역이 없습니다.</Empty>
                  ) : (
                    <div className="table-wrap">
                      <table>
                        <thead>
                          <tr>
                            <th>구독</th>
                            {admin && <th>사용자</th>}
                            <th>이용 기간 (종료일 미포함)</th>
                            <th>상태</th>
                            <th>관리</th>
                          </tr>
                        </thead>
                        <tbody>
                          {data.subscriptions.map((s) => (
                            <tr key={s.id}>
                              <td>
                                <strong>{s.planName}</strong>
                                <small>
                                  #{s.id} · {money(s.price)}
                                </small>
                              </td>
                              {admin && <td>{s.username}</td>}
                              <td>
                                {s.startDate} ~ {s.endDate}
                              </td>
                              <td>
                                <Badge status={s.status} />
                              </td>
                              <td>
                                <button
                                  className="quiet"
                                  onClick={() => showDetail(s.id)}
                                >
                                  상세 보기
                                </button>
                              </td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  )}
                  {selected && (
                    <section className="panel detail">
                      <div className="page-heading">
                        <h2>{selected.planName} 상세</h2>
                        <button
                          className="quiet"
                          onClick={() => setSelected(null)}
                        >
                          닫기
                        </button>
                      </div>
                      <Badge status={selected.status} />
                      <p>
                        구독 번호 #{selected.id} · {selected.username}
                      </p>
                      <p>
                        시작일 {selected.startDate} / 종료일 {selected.endDate}{' '}
                        (종료일 미포함)
                      </p>
                      <p>결제 금액 {money(selected.price)}</p>
                      {selected.status === 'CANCELLED' && (
                        <p>
                          해지 신청이 완료되었습니다. 종료일까지 이용할 수
                          있습니다.
                        </p>
                      )}
                      {selected.status === 'EXPIRED' && (
                        <p>이용이 종료된 구독입니다.</p>
                      )}
                      {selected.status === 'ACTIVE' && (
                        <button
                          className="danger"
                          onClick={() => setCancelTarget(selected.id)}
                        >
                          구독 해지
                        </button>
                      )}
                    </section>
                  )}
                </>
              )}
              {page === 'payments' &&
                (data.payments.length === 0 ? (
                  <Empty>결제 내역이 없습니다.</Empty>
                ) : (
                  <div className="table-wrap">
                    <table>
                      <thead>
                        <tr>
                          <th>결제 번호 / 일시</th>
                          {admin && <th>사용자</th>}
                          <th>요금제</th>
                          <th>금액</th>
                          <th>상태</th>
                          {!admin && <th>환불</th>}
                        </tr>
                      </thead>
                      <tbody>
                        {data.payments.map((p) => (
                          <tr key={p.id}>
                            <td>
                              #{p.id}
                              <small>
                                {new Date(p.createdAt).toLocaleString('ko-KR')}
                              </small>
                            </td>
                            {admin && <td>{p.username}</td>}
                            <td>
                              {data.plans.find((plan) => plan.id === p.planId)
                                ?.name || p.planId}
                            </td>
                            <td>{money(p.amount)}</td>
                            <td>
                              <Badge status={p.status} />
                            </td>
                            {!admin && (
                              <td>
                                {p.status === 'SUCCESS' &&
                                !data.refunds.some(
                                  (r) => r.paymentId === p.id
                                ) &&
                                data.subscriptions.some(
                                  (s) =>
                                    s.id === p.subscriptionId &&
                                    s.status !== 'EXPIRED'
                                ) ? (
                                  <button
                                    className="quiet"
                                    onClick={() => {
                                      setRefundPayment(p)
                                      setReason('')
                                    }}
                                  >
                                    환불 요청
                                  </button>
                                ) : (
                                  '—'
                                )}
                              </td>
                            )}
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                ))}
              {page === 'refunds' && (
                <>
                  <p className="intro">
                    요청일 기준 남은 일수에 비례해 환불하며, 원 미만은 버립니다.
                    승인되면 이용이 즉시 종료됩니다.
                  </p>
                  {data.refunds.length === 0 ? (
                    <Empty>환불 요청 내역이 없습니다.</Empty>
                  ) : (
                    <div className="refund-grid">
                      {data.refunds.map((r) => (
                        <article key={r.id} className="panel">
                          <div className="page-heading">
                            <h2>환불 #{r.id}</h2>
                            <Badge status={r.status} />
                          </div>
                          <p>
                            결제 #{r.paymentId}
                            {admin && ' · ' + r.username}
                          </p>
                          <p className="price">{money(r.amount)}</p>
                          <p>
                            남은 {r.remainingDays}일 / 전체 {r.totalDays}일
                          </p>
                          <p className="muted">요청일 {r.requestedDate}</p>
                          <p className="reason">요청 사유: {r.reason}</p>
                          {r.adminNote && (
                            <p className="reason">처리 메모: {r.adminNote}</p>
                          )}
                          {admin && r.status === 'REQUESTED' && (
                            <button
                              onClick={() => {
                                setDecision(r)
                                setNote('')
                              }}
                            >
                              요청 검토
                            </button>
                          )}
                        </article>
                      ))}
                    </div>
                  )}
                </>
              )}
              {cancelTarget && (
                <section className="panel confirm">
                  <h2>구독을 해지할까요?</h2>
                  <p>종료일까지 이용할 수 있으며 자동 환불되지는 않습니다.</p>
                  <div className="actions">
                    <button
                      className="danger"
                      onClick={() =>
                        action(async () => {
                          const path = admin
                            ? '/admin/subscriptions/' + cancelTarget + '/status'
                            : '/subscriptions/' + cancelTarget + '/cancel'
                          const response = admin
                            ? await session.api.patch(path, {
                                status: 'CANCELLED',
                              })
                            : await session.api.post(path)
                          setSelected(response.data)
                          setCancelTarget(null)
                          setNotice('해지 신청이 완료되었습니다.')
                          await refresh()
                        })
                      }
                    >
                      해지 확정
                    </button>
                    <button
                      className="quiet"
                      onClick={() => setCancelTarget(null)}
                    >
                      유지하기
                    </button>
                  </div>
                </section>
              )}
              {refundPayment && (
                <form
                  className="panel confirm"
                  onSubmit={(event) => {
                    event.preventDefault()
                    action(async () => {
                      await session.api.post('/refunds', {
                        paymentId: refundPayment.id,
                        reason,
                      })
                      setRefundPayment(null)
                      setPage('refunds')
                      await refresh()
                      setNotice(
                        '환불을 요청했습니다. 관리자 승인 후 처리됩니다.'
                      )
                    })
                  }}
                >
                  <h2>결제 #{refundPayment.id} 환불 요청</h2>
                  <p>
                    환불액 = 결제 금액 × 요청일 기준 남은 일수 ÷ 전체 일수 (원
                    미만 버림)
                  </p>
                  <p>
                    요청 당일은 남은 일수에 포함됩니다. 승인 시 이용이
                    종료됩니다.
                  </p>
                  <label>
                    환불 사유
                    <textarea
                      required
                      maxLength={500}
                      value={reason}
                      onChange={(e) => setReason(e.target.value)}
                    />
                  </label>
                  <div className="actions">
                    <button disabled={!reason.trim()}>환불 요청 보내기</button>
                    <button
                      type="button"
                      className="quiet"
                      onClick={() => setRefundPayment(null)}
                    >
                      취소
                    </button>
                  </div>
                </form>
              )}
              {decision && (
                <section className="panel confirm">
                  <h2>환불 #{decision.id} 검토</h2>
                  <p>
                    {money(decision.amount)} · 승인 즉시 구독 이용이 종료됩니다.
                  </p>
                  <label>
                    처리 메모
                    <textarea
                      maxLength={500}
                      value={note}
                      onChange={(e) => setNote(e.target.value)}
                    />
                  </label>
                  <div className="actions">
                    {[true, false].map((approve) => (
                      <button
                        key={String(approve)}
                        className={approve ? '' : 'danger'}
                        onClick={() =>
                          action(async () => {
                            await session.api.post(
                              '/admin/refunds/' + decision.id + '/decision',
                              { approve, note }
                            )
                            setDecision(null)
                            await refresh()
                            setNotice(
                              approve
                                ? '환불을 승인했습니다.'
                                : '환불을 반려했습니다.'
                            )
                          })
                        }
                      >
                        {approve ? '승인' : '반려'}
                      </button>
                    ))}
                    <button className="quiet" onClick={() => setDecision(null)}>
                      닫기
                    </button>
                  </div>
                </section>
              )}
            </fieldset>
            {busy && (
              <p role="status" className="muted">
                처리 중입니다…
              </p>
            )}
          </main>
        </>
      )}
      <footer>
        SubMate <span>구독을 더 편하게.</span>
      </footer>
    </div>
  )
}
