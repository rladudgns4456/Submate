import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env?.VITE_API_BASE_URL || '/api',
  timeout: 15000,
})
let authorization = null

export function setAuthorization(value) {
  authorization = value
}
api.interceptors.request.use((config) => {
  if (config.skipAuthorization) delete config.headers.Authorization
  else if (authorization && !config.headers.Authorization)
    config.headers.Authorization = authorization
  return config
})
export function errorMessage(error) {
  return (
    error.response?.data?.message ||
    (error.response?.status === 401
      ? '로그인이 필요합니다. 다시 로그인해 주세요.'
      : '요청을 처리하지 못했습니다. 연결 상태를 확인하고 다시 시도해 주세요.')
  )
}
export default api
