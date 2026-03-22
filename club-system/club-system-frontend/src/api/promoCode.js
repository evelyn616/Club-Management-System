import apiClient from './axios'

export const promoCodeApi = {
  // 管理員：建立優惠碼
  create: (data, createdBy) =>
    apiClient.post('/promo-codes', data, { params: { createdBy } }),

  // 管理員：查詢所有優惠碼
  listAll: () =>
    apiClient.get('/promo-codes'),

  // 管理員：刪除優惠碼
  remove: (id) =>
    apiClient.delete(`/promo-codes/${id}`),

  // 管理員：啟用/停用優惠碼
  toggle: (id) =>
    apiClient.put(`/promo-codes/${id}/toggle`),

  // 會員：驗證優惠碼（預覽折扣，不計入使用次數）
  validate: (code, activityId) =>
    apiClient.get('/promo-codes/validate', { params: { code, activityId } }),
}
