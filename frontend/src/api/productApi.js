import api from './axios'

export const productApi = {
  list: (categoryId) =>
    api
      .get('/products', { params: categoryId ? { categoryId } : {} })
      .then((response) => response.data),
  categories: () => api.get('/categories').then((response) => response.data),
}
