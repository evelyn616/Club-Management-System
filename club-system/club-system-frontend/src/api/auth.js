import apiClient from './axios'

/**
 * 用戶註冊
 */
export const register = async (registerData) => {
  const response = await apiClient.post('/auth/register', registerData)
  return response.data
}

/**
 * 用戶登入
 */
export const login = async (loginData) => {
  const response = await apiClient.post('/auth/login', loginData)
  return response.data
}

/**
 * 取得當前用戶資訊
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
