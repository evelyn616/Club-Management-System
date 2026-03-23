import apiClient from './axios'

/**
 * 用戶註冊
 */
export const register = async (registerData) => {
  const response = await apiClient.post('/auth/register', registerData)
  return response.data
}

/**
 * 一般會員登入
 * POST /api/auth/login
 */
export const login = async (loginData) => {
  const response = await apiClient.post('/auth/login', loginData)
  return response.data
}

/**
 * 管理員登入
 * POST /api/auth/admin/login — 後端會驗證 role 必須為 ADMIN
 */
export const adminLogin = async (loginData) => {
  const response = await apiClient.post('/auth/admin/login', loginData)
  return response.data
}

/**
 * 取得當前用戶資訊
 * GET /api/auth/me
 */
export const getCurrentUser = async () => {
  const response = await apiClient.get('/auth/me')
  return response.data
}

/**
 * 取得用戶資料
 */
export const getUserById = async (userId) => {
  const response = await apiClient.get(`/users/${userId}`)
  return response.data
}

/**
 * 更新用戶資料
 */
export const updateUser = async (userId, userData) => {
  const response = await apiClient.put(`/users/${userId}`, userData)
  return response.data
}

/**
 * 修改密碼
 */
export const changePassword = async (userId, passwordData) => {
  const response = await apiClient.put(`/users/${userId}/password`, passwordData)
  return response.data
}

/**
 * 發送 MFA 驗證碼（管理員二次驗證）
 * POST /api/auth/admin/mfa/send
 */
export const sendAdminMfa = async (type) => {
  const response = await apiClient.post('/auth/admin/mfa/send', { type })
  return response.data
}

/**
 * 驗證 MFA 驗證碼
 * POST /api/auth/admin/mfa/verify
 */
export const verifyAdminMfa = async (code) => {
  const response = await apiClient.post('/auth/admin/mfa/verify', { code })
  return response.data
}

/**
 * 忘記密碼：發送驗證碼到信箱
 * POST /api/auth/password/request-reset
 */
export const requestPasswordReset = (email) =>
  apiClient.post('/auth/password/request-reset', { email }).then(r => r.data)

/**
 * 忘記密碼：驗碼並重設密碼
 * POST /api/auth/password/reset
 */
export const resetPassword = (email, code, newPassword) =>
  apiClient.post('/auth/password/reset', { email, code, newPassword }).then(r => r.data)