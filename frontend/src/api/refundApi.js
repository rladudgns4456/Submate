import api from './axios'
export const refundApi = {
  list: (page = 0) =>
    api.get('/refunds', { params: { page } }).then((r) => r.data),
  request: (paymentId, reason) =>
    api.post('/refunds', { paymentId, reason }).then((r) => r.data),
  decide: (id, decision) =>
    api.patch(`/admin/refunds/${id}`, { decision }).then((r) => r.data),
}
