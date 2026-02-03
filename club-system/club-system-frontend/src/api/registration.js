import apiClient from "./axios";

export const registrationApi = {
    // 取得使用者的所有報名紀錄
    getMyRegistrations: (userId) => {
        return apiClient.get(`/registrations/my`,{ 
            params: { userId } });
    },
    //取得所有報名紀錄
    getAllRegistrations: () =>{
        return apiClient.get(`/registrations/all`)
    },
    //取得單一活動的報名紀錄
    getActivityRegistrations: () =>{
        return apiClient.get(`registrations/activity/${activityId}`)
    },
    //取消報名紀錄
    cancel: (registrationId) => {
        return apiClient.delete(`/registrations/${registrationId}`);
        },
    // 取得使用者的待繳款紀錄
    getPendingPayments: (userId) => {
        return apiClient.get(`/registrations/pending-payments`,{
            params: { userId }
        });
    },
    //更新付款狀態
    updatePayment(registrationId){
        return apiClient.put(`/registrations/${registrationId}/payment`)
    },
    //新增報名紀錄
    createRegistration(registrationData){
        return apiClient.post(`/registrations`, registrationData);
        data: registrationData
        
    },

};