import { useCallback, useState } from 'react'
import { Link } from 'react-router-dom'
import { productApi } from '../../api/productApi'
import { useResource } from '../../hooks/useResource'
import ResourceState from '../../components/common/ResourceState'
import { money } from '../../utils/formatUtils'

// 기존 상품 API에서 결제 화면으로 연결하는 조회 전용 화면.
export default function ProductSelectionPage() {
  const [categoryId, setCategoryId] = useState('')
  const categoriesLoader = useCallback(() => productApi.categories(), [])
  const productsLoader = useCallback(
    () => productApi.list(categoryId),
    [categoryId]
  )
  const categories = useResource(categoriesLoader)
  const products = useResource(productsLoader)
  return (
    <>
      <header className="page-heading">
        <div>
          <p className="eyebrow">SUBSCRIPTIONS</p>
          <h1>구독 상품 선택</h1>
          <p className="muted">
            상품과 이용 기간을 확인하고 구독을 시작하세요. 실제 금액은 청구되지
            않습니다.
          </p>
        </div>
        <button
          className="secondary"
          onClick={() => {
            categories.reload()
            products.reload()
          }}
        >
          새로고침
        </button>
      </header>
      <section className="panel catalog-filter" aria-label="상품 분류">
        <label htmlFor="category-filter">카테고리</label>
        <select
          id="category-filter"
          value={categoryId}
          onChange={(event) => setCategoryId(event.target.value)}
        >
          <option value="">전체</option>
          {categories.data?.map((category) => (
            <option key={category.categoryId} value={category.categoryId}>
              {category.name}
            </option>
          ))}
        </select>
        {categories.error && (
          <p className="muted" role="status">
            카테고리를 불러오지 못했습니다. 전체 상품은 확인할 수 있습니다.
          </p>
        )}
      </section>
      <ResourceState {...products} />
      {products.data &&
        (products.data.length === 0 ? (
          <section className="empty">
            <h2>등록된 상품이 없습니다</h2>
            <p>다른 카테고리를 선택하거나 나중에 다시 확인해 주세요.</p>
          </section>
        ) : (
          <div className="subscription-grid">
            {products.data.map((product) => (
              <article className="subscription-card" key={product.productId}>
                <div className="card-top">
                  <span className="product-icon" aria-hidden="true">
                    {product.name.slice(0, 1)}
                  </span>
                  <span
                    className={`status ${product.status === 'ACTIVE' ? 'status-active' : ''}`}
                  >
                    {product.status === 'ACTIVE' ? '신청 가능' : '판매 중지'}
                  </span>
                </div>
                <p className="muted">{product.categoryName}</p>
                <h2>{product.name}</h2>
                <p className="price">
                  {money(product.price)}{' '}
                  <small>
                    / {product.billingCycle === 'MONTHLY' ? '1개월' : '1년'}
                  </small>
                </p>
                <p className="muted catalog-note">
                  한 번 결제하고 정해진 기간 동안 이용합니다. 자동 결제는
                  없습니다.
                </p>
                {product.status === 'ACTIVE' ? (
                  <Link
                    className="button primary full"
                    to={`/subscribe/${product.productId}`}
                  >
                    구독 신청
                  </Link>
                ) : (
                  <button className="secondary full" disabled>
                    신규 신청 불가
                  </button>
                )}
              </article>
            ))}
          </div>
        ))}
    </>
  )
}
