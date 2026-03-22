import apiClient from './axios'

/**
 * 繳費相關 API
 */

/**
 * 取得我的所有繳費記錄
 */
export const getMyPayments = async () => {
  const response = await apiClient.get('/payments/my')
  return response.data
}

/**
 * 取得我的待繳費記錄
 */
export const getMyPendingPayments = async () => {
  const response = await apiClient.get('/payments/my/pending')
  return response.data
}

/**
 * 取得繳費記錄詳情
 */
export const getPaymentById = async (paymentId) => {
  const response = await apiClient.get(`/payments/${paymentId}`)
  return response.data
}

/**
 * 創建繳費記錄
 */
export const createPayment = async (paymentData) => {
  const response = await apiClient.post('/payments', paymentData)
  return response.data
}

/**
 * 更新轉帳後五碼
 */
export const updateBankProof = async (paymentId, bankAccountProof) => {
  const response = await apiClient.put(`/payments/${paymentId}/bank-proof`, {
    bankAccountProof
  })
  return response.data
}

/**
 * 取消繳費
 */
export const cancelPayment = async (paymentId, reason) => {
  const response = await apiClient.put(`/payments/${paymentId}/cancel`, {
    reason
  })
  return response.data
}

/**
 * 創建綠界結帳訂單
 */
export const createEcpayCheckout = async (checkoutData) => {
  const response = await apiClient.post('/payments/ecpay/checkout', checkoutData)
  return response.data
}

/**
 * 審核繳費（管理員）
 */
export const approvePayment = async (paymentId, approveData) => {
  const response = await apiClient.put(`/payments/${paymentId}/approve`, approveData)
  return response.data
}

/**
 * 取得待審核的現金繳費（管理員）
 */
export const getPendingCashPayments = async () => {
  const response = await apiClient.get('/payments/admin/pending-cash')
  return response.data
}

/**
 * 取得繳費統計（管理員）
 */
export const getPaymentStatistics = async () => {
  const response = await apiClient.get('/payments/admin/statistics')
  return response.data
}

/**
 * 選擇現金付款
 */
export const selectCashPayment = async (paymentId, data) => {
  const response = await apiClient.post(`/payments/${paymentId}/cash`, data)
  return response.data
}

/**
 * 審核現金付款（管理員）
 */
export const approveCashPayment = async (paymentId, reviewNote) => {
  const response = await apiClient.put(`/payments/${paymentId}/approve-cash`, {
    reviewNote
  })
  return response.data
}

/**
 * 拒絕現金付款（管理員）
 */
export const rejectCashPayment = async (paymentId, reason) => {
  const response = await apiClient.put(`/payments/${paymentId}/reject-cash`, {
    reason
  })
  return response.data
}

/**
 * 取得待審核的現金付款列表（管理員）
 */
export const getPendingReviewPayments = async () => {
  const response = await apiClient.get('/payments/admin/pending-review')
  return response.data
}

/**
 * 取得所有付款記錄（管理員）
 */
export const getAllPayments = async (params = {}) => {
  const response = await apiClient.get('/payments/admin/all', { params })
  return response.data
}

/**
 * 取得繳費統計（管理員）- 增強版
 */
export const getPaymentStats = async () => {
  const response = await apiClient.get('/payments/admin/statistics')
  return response.data
}

/**
 * 查詢綠界訂單狀態（管理員）
 */
export const queryEcpayStatus = async (paymentId) => {
  const response = await apiClient.get(`/payments/${paymentId}/ecpay-status`)
  return response.data
}
