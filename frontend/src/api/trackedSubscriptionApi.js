import api from './axios'

const root = '/tracked-subscriptions'

export const trackedSubscriptionApi = {
  list: () => api.get(root).then(({ data }) => data),
  create: (subscription) => api.post(root, subscription),
  importReceipt: (receiptId, subscription) =>
    api.post(root + '/imports', { receiptId, subscription }),
  update: (id, subscription) => api.put(root + '/' + id, subscription),
  remove: (id) => api.delete(root + '/' + id),
}
