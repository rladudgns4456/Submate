import api from './axios.js'

export const authApi = {
  login: (email, password) =>
    api
      .post('/auth/login', { email, password }, { skipAuthorization: true })
      .then((response) => response.data),
  signup: (email, password) =>
    api
      .post('/auth/signup', { email, password }, { skipAuthorization: true })
      .then((response) => response.data),
  me: (accessToken) =>
    api
      .get('/auth/me', {
        headers: { Authorization: `Bearer ${accessToken}` },
        skipSessionCheck: true,
      })
      .then((response) => response.data),
}
