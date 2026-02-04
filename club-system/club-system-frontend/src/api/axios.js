import axios from 'axios';
import { useUserStore } from '@/stores/user';


const apiClient = axios.create({
    baseURL: 'http://localhost:8080/api',
    headers: {
        'Content-Type': 'application/json',
    },
    timeout: 10000,
});

//api請求攔截器
apiClient.interceptors.request.use((config)=> {
    const userStore = useUserStore();
    if(userStore.token){
        config.headers.Authorization = `Bearer ${userStore.token}`;
    }
    return config;
}
)

//回應攔截器，讓token失效時清除狀態
apiClient.interceptors.response.use((response) => response,(error) => {
    if(error.response?.status === 401){
        const userStore = useUserStore();
        userStore.logout();
    }
    return Promise.reject(error);
})
export default apiClient;
