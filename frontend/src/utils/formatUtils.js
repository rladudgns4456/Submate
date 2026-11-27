export const money = (value) =>
  new Intl.NumberFormat('ko-KR', { style: 'currency', currency: 'KRW' }).format(
    Number(value)
  )
export const dateTime = (value) =>
  value ? value.replace('T', ' ').slice(0, 16) : '—'
