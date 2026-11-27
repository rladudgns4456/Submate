export default function ResourceState({ loading, error, reload }) {
  if (loading)
    return (
      <p className="panel muted" role="status">
        내역을 불러오고 있어요…
      </p>
    )
  if (error)
    return (
      <div className="message error" role="alert">
        {error}{' '}
        <button className="text-button" onClick={reload}>
          다시 시도
        </button>
      </div>
    )
  return null
}
export function Pagination({ data, setPage }) {
  if (!data || data.totalPages <= 1) return null
  return (
    <nav className="pagination" aria-label="내역 페이지">
      <button disabled={data.page === 0} onClick={() => setPage(data.page - 1)}>
        이전
      </button>
      <span>
        {data.page + 1} / {data.totalPages}
      </span>
      <button
        disabled={data.page + 1 >= data.totalPages}
        onClick={() => setPage(data.page + 1)}
      >
        다음
      </button>
    </nav>
  )
}
