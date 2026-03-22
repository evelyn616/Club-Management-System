import apiClient from './axios'

export const discountApi = {
  // 取得全域折扣設定（管理員）
  getConfig: () => apiClient.get('/discount/config'),

  // 更新全域折扣設定（管理員）
  updateConfig: (data, updatedBy = 'admin') =>
    apiClient.put('/discount/config', data, { params: { updatedBy } }),

  // 報名前折扣預覽
  preview: (activityId, userId) =>
    apiClient.get('/discount/preview', { params: { activityId, userId } }),

  // 查詢我的未使用優惠券
  getMyCoupons: (userId) =>
    apiClient.get('/discount/coupons/my', { params: { userId } }),

  // 查詢某會員所有優惠券（管理員）
  getAllCoupons: (userId) =>
    apiClient.get('/discount/coupons/all', { params: { userId } }),

  // 管理員手動發放優惠券（指定單一會員）
  issueCoupon: (data, issuedBy) =>
    apiClient.post('/discount/coupons/issue', data, { params: { issuedBy } }),

  // 管理員批次發放優惠券（幹部 / 全部會員）
  issueBatchCoupons: (targetGroup, data, issuedBy) =>
    apiClient.post('/discount/coupons/issue-batch', data, { params: { targetGroup, issuedBy } }),

  // 管理員查詢所有優惠券
  adminListAllCoupons: () =>
    apiClient.get('/discount/coupons/admin/all'),
}
