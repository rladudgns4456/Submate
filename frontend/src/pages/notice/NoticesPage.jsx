import { Link } from 'react-router-dom'
import { useEffect, useState } from 'react'
import { getNotices } from '../../api/noticeApi'

export default function NoticesPage() {
  const [notices, setNotices] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    getNotices()
      .then((data) => {
        setNotices(data)
      })
      .catch(() => {
        setError('공지사항을 불러오지 못했습니다.')
      })
      .finally(() => {
        setLoading(false)
      })
  }, [])

  if (loading) {
    return (
      <section className="panel">
        <p>공지사항을 불러오는 중입니다.</p>
      </section>
    )
  }

  if (error) {
    return (
      <section className="panel">
        <p>{error}</p>
      </section>
    )
  }

  return (
    <section className="panel">
      <h1>공지사항</h1>

      {notices.length === 0 ? (
        <p>등록된 공지사항이 없습니다.</p>
      ) : (
        <ul>
          {notices.map((notice) => (
            <li key={notice.noticeId}>
              <Link to={`/notices/${notice.noticeId}`}>
                {notice.title}
              </Link>
              <span>
                {' '}
                {notice.createdAt
                  ? new Date(notice.createdAt).toLocaleDateString()
                  : ''}
              </span>
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}