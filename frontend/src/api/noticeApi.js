import api from './axios'

export async function getNotices() {
  const response = await api.get('/notices')
  return response.data
}

export async function getNotice(noticeId) {
  const response = await api.get(`/notices/${noticeId}`)
  return response.data
}
