const labels = {
  ACTIVE: '이용 중',
  CANCELLED: '해지 예약',
  EXPIRED: '이용 종료',
  COMPLETED: '완료',
  FAILED: '실패',
  PENDING: '대기',
  REFUNDED: '환불 완료',
  REQUESTED: '검토 중',
  REJECTED: '반려',
}
export default function StatusBadge({ status }) {
  return (
    <span className={`status status-${status.toLowerCase()}`}>
      {labels[status] || status}
    </span>
  )
}
