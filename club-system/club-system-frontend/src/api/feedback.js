import apiClient from './axios'

/**
 * 建立回饋表單（管理員）
 */
export const createFeedbackForm = (data) =>
  apiClient.post('/feedback/forms', data).then(r => r.data)

/**
 * 取得活動的回饋表單（含題目）
 * GET /api/feedback/forms/activity/:activityId
 */
export const getFormByActivity = (activityId) =>
  apiClient.get(`/feedback/forms/activity/${activityId}`).then(r => r.data)

/**
 * 取得表單（by formId）
 */
export const getFormById = (formId) =>
  apiClient.get(`/feedback/forms/${formId}`).then(r => r.data)

/**
 * 更新表單標題/說明/積分（管理員）
 */
export const updateFeedbackForm = (formId, data) =>
  apiClient.put(`/feedback/forms/${formId}`, data).then(r => r.data)

/**
 * 開啟/關閉表單（管理員）
 */
export const toggleFeedbackForm = (formId) =>
  apiClient.put(`/feedback/forms/${formId}/toggle`).then(r => r.data)

/**
 * 新增題目（管理員）
 * data: { questionText, questionType, isRequired, maxRating, options: [] }
 */
export const addQuestion = (formId, data) =>
  apiClient.post(`/feedback/forms/${formId}/questions`, data).then(r => r.data)

/**
 * 更新題目（管理員）
 */
export const updateQuestion = (formId, questionId, data) =>
  apiClient.put(`/feedback/forms/${formId}/questions/${questionId}`, data).then(r => r.data)

/**
 * 刪除題目（管理員）
 */
export const deleteQuestion = (formId, questionId) =>
  apiClient.delete(`/feedback/forms/${formId}/questions/${questionId}`).then(r => r.data)

/**
 * 提交回饋（公開；登入時帶 JWT 可得積分）
 * data: { answers: [{ questionId, textAnswer?, ratingAnswer?, selectedOptionIds?: [] }] }
 */
export const submitFeedback = (formId, data) =>
  apiClient.post(`/feedback/forms/${formId}/submit`, data).then(r => r.data)

/**
 * 取得統計摘要（管理員）
 */
export const getFeedbackStats = (formId) =>
  apiClient.get(`/feedback/forms/${formId}/stats`).then(r => r.data)

/**
 * 取得全體活動綜合滿意度（管理員）
 * Returns: { score: number, totalRatings: number }
 */
export const getOverallSatisfaction = () =>
  apiClient.get('/feedback/satisfaction/overall').then(r => r.data)
