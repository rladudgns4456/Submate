import { useCallback, useState } from 'react'
import { Link } from 'react-router-dom'
import { subscriptionApi } from '../../api/subscriptionApi'
import api from '../../api/axios'
import { useResource } from '../../hooks/useResource'
import ResourceState, {
  Pagination,
} from '../../components/common/ResourceState'
import StatusBadge from '../../components/subscription/StatusBadge'
import { isDemo } from '../../context/authState'

export default function SubscriptionsPage({ admin = false }) {
  const [page, setPage] = useState(0)
  const loader = useCallback(
    () =>
      admin
        ? api
            .get('/admin/subscriptions', { params: { page } })
            .then((r) => r.data)
        : subscriptionApi.list(page),
    [admin, page]
  )
  const resource = useResource(loader)
  return (
    <>
      <header className="page-heading">
        <div>
          <p className="eyebrow">
            {admin ? 'ADMIN / SUBSCRIPTIONS' : 'MY SUBSCRIPTIONS'}
          </p>
          <h1>{admin ? '구독 관리' : '내 구독'}</h1>
          <p className="muted">
            이용 중인 서비스를 확인하고, 필요한 순간에 관리하세요.
          </p>
        </div>
        <div className="actions">
          {!admin && (
            <Link className="button primary" to="/products">
              상품 선택
            </Link>
          )}
          <button className="secondary" onClick={resource.reload}>
            새로고침
          </button>
        </div>
      </header>
      {!admin && isDemo && (
        <section className="demo-products" aria-label="데모 구독 상품">
          <div>
            <strong>구독 흐름을 시작해 보세요</strong>
            <p className="muted">가상 결제로만 처리되는 샘플 상품입니다.</p>
          </div>
          <Link className="button secondary" to="/subscribe/1">
            Cinema · 월 12,900원
          </Link>
          <Link className="button secondary" to="/subscribe/2">
            Workspace · 연 99,000원
          </Link>
        </section>
      )}
      <ResourceState {...resource} />
      {resource.data && (
        <>
          <p className="count">전체 {resource.data.totalElements}건</p>
          {resource.data.content.length === 0 ? (
            <section className="empty">
              <span className="empty-symbol">↗</span>
              <h2>아직 구독 내역이 없어요</h2>
              <p>상품에서 구독을 신청하면 이곳에서 확인할 수 있어요.</p>
            </section>
          ) : (
            <div className="subscription-grid">
              {resource.data.content.map((item) => (
                <article
                  className="subscription-card"
                  key={item.subscriptionId}
                >
                  <div className="card-top">
                    <span className="product-icon">
                      {item.productName.slice(0, 1)}
                    </span>
                    <StatusBadge status={item.status} />
                  </div>
                  <h2>{item.productName}</h2>
                  <p className="muted">
                    구독 #{item.subscriptionId}
                    {admin ? ` · 회원 #${item.memberId}` : ''}
                  </p>
                  <dl>
                    <div>
                      <dt>시작일</dt>
                      <dd>{item.startDate}</dd>
                    </div>
                    <div>
                      <dt>이용 종료 시점</dt>
                      <dd>{item.endDate} 00:00</dd>
                    </div>
                  </dl>
                  <Link
                    className="card-link"
                    to={`${admin ? '/admin' : ''}/subscriptions/${item.subscriptionId}`}
                  >
                    구독 상세 보기 <span>↗</span>
                  </Link>
                </article>
              ))}
            </div>
          )}
          <Pagination data={resource.data} setPage={setPage} />
        </>
      )}
    </>
  )
}
