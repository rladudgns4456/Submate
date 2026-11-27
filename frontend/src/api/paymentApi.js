import api from './axios'
export const paymentApi = {
  list: (page = 0) =>
    api.get('/payments', { params: { page } }).then((r) => r.data),
}
