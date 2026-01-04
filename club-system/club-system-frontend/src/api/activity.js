import apiClient from "./axios";

export const activityApi = {
    // 取得所有可報名活動列表
    getRegistrableActivities: () => {
        return apiClient.get(`/activities/registrable`);},
       
    
    }