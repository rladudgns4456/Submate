import api from './axios'
export const subscriptionApi = {
  list: (page = 0) =>
    api.get('/subscriptions', { params: { page } }).then((r) => r.data),
  quote: (productId) =>
    api
      .get('/subscriptions/quote', { params: { productId } })
      .then((r) => r.data),
  detail: (id, admin = false) =>
    api.get(`${admin ? '/admin' : ''}/subscriptions/${id}`).then((r) => r.data),
  checkout: (request) =>
    api.post('/subscriptions', request).then((r) => r.data),
  cancel: (id, admin = false) =>
    api
      .patch(`${admin ? '/admin' : ''}/subscriptions/${id}/cancel`)
      .then((r) => r.data),
}
