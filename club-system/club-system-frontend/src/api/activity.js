import apiClient from "./axios";

export const activityApi = {
    // 取得所有可報名活動列表
    getRegistrableActivities: () => {
        return apiClient.get(`/activities/registrable`);},
    
    //取得所有活動列表（管理員用）
    getAllActivities: () => {
        return apiClient.get(`/activities`);},
    // 取得單一活動詳細資訊
    getActivityDetails: (activityId) => {
        return apiClient.get(`/activities/${activityId}`);
    },
    // 建立新活動
    createActivity: (activityData) => {
        return apiClient.post(`/activities`, activityData);
    },
    // 更新活動資訊
    updateActivity: (activityId, activityData) => {
        return apiClient.put(`/activities/${activityId}`, activityData);
    },
    // 刪除活動
    deleteDraftActivity: (activityId) => {
        return apiClient.delete(`/activities/${activityId}`);
    },
    // 發布活動
    publishActivity: (activityId) => {
        return apiClient.put(`/activities/${activityId}/publish`);
    },
    //預約發布活動
    schedulePublishActivity: (activityId, scheduleData) => {
        return apiClient.put(`/activities/${activityId}/schedule-publish`, scheduleData);
    },

    // 取消發布活動
    cancelScheduledPublish: (activityId) => {
        return apiClient.put(`/activities/${activityId}/cancel-schedule`);
    },

    //完成活動
    completeActivity: (activityId) => {
        return apiClient.put(`/activities/${activityId}/complete`);
    },

    // 取消活動
    cancelActivity: (activityId) => {
        return apiClient.put(`/activities/${activityId}/cancel`);
    },

    getDraftActivities: () => {
        return apiClient.get(`/activities/drafts`);
    },
};