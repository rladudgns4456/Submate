import { Link, useParams } from 'react-router-dom'
import { useEffect, useState } from 'react'
import { getNotice } from '../../api/noticeApi'

export default function NoticeDetailPage() {
  const { id } = useParams()
  const [notice, setNotice] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    getNotice(id)
      .then((data) => {
        setNotice(data)
      })
      .catch(() => {
        setError('공지사항을 불러오지 못했습니다.')
      })
      .finally(() => {
        setLoading(false)
      })
  }, [id])

  if (loading) {
    return (
      <section className="panel">
        <p>공지사항을 불러오는 중입니다.</p>
      </section>
    )
  }

  if (error || !notice) {
    return (
      <section className="panel">
        <p>{error || '공지사항을 찾을 수 없습니다.'}</p>
        <Link to="/notices">목록으로</Link>
      </section>
    )
  }

  return (
    <section className="panel">
      <h1>{notice.title}</h1>

      <p>
        작성일:{' '}
        {notice.createdAt
          ? new Date(notice.createdAt).toLocaleString()
          : ''}
      </p>

      <hr />

      <div style={{ whiteSpace: 'pre-wrap' }}>
        {notice.content}
      </div>

      <br />

      <Link to="/notices">목록으로</Link>
    </section>
  )
}