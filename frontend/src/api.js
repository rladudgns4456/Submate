import axios from 'axios'

export function createApi(username, password) {
  return axios.create({
    baseURL: import.meta.env.VITE_API_URL || '/api',
    timeout: 15000,
    auth: { username, password },
  })
}

export function errorMessage(error) {
  if (error.response?.status === 401)
    return '아이디 또는 비밀번호를 확인해 주세요.'
  if (error.response?.status === 403) return '관리자 권한이 필요합니다.'
  return (
    error.response?.data?.message ||
    '서버에 연결할 수 없습니다. 잠시 후 다시 시도해 주세요.'
  )
}
