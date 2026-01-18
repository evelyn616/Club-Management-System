import apiClient from "./axios";

export const activityApi = {
    // 取得所有可報名活動列表
    getRegistrableActivities: () => {
        return apiClient.get(`/activities/registrable`);},
    
    //取得所有活動列表（管理員用）
    getAllActivities: () => {
        return apiClient.get(`/activities`);},
    // 取得單一活動詳細資訊
    getActivityDetails: (id) => {
        return apiClient.get(`/activities/${id}`);
    },
    // 建立新活動
    createActivity: (activityData) => {
        return apiClient.post(`/activities`, activityData);
    },
    // 更新活動資訊
    updateActivity: (id, activityData) => {
        return apiClient.put(`/activities/${id}`, activityData);
    },
    // 刪除活動
    deleteActivity: (id) => {
        return apiClient.delete(`/activities/${id}`);
    },
    // 發布活動
    publishActivity: (id) => {
        return apiClient.put(`/activities/${id}/publish`);
    },
    //預約發布活動
    schedulePublishActivity: (id, scheduleData) => {
        return apiClient.put(`/activities/${id}/schedule-publish`, scheduleData);
    },

    // 取消發布活動
    cancelScheduledPublish: (id) => {
        return apiClient.put(`/activities/${id}/cancel-schedule`);
    },

    //完成活動
    completeActivity: (id) => {
        return apiClient.put(`/activities/${id}/complete`);
    },

    // 取消活動
    cancelActivity: (id) => {
        return apiClient.put(`/activities/${id}/cancel`);
    },
};