import axios from 'axios';
import { useUserStore } from '@/stores/user';


const apiClient = axios.create({
    baseURL: 'http://localhost:8080/api',
    headers: {
        'Content-Type': 'application/json',
    },
    timeout: 10000,
});

// ====== Request Interceptor：自動帶 JWT token ======
apiClient.interceptors.request.use(
    (config) => {
        // 從 localStorage 取 token（不依賴 store，避免 Pinia 初始化時序問題）
        const token = localStorage.getItem('token');
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);
 
// ====== Response Interceptor：401 自動登出 ======
apiClient.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401) {
            // Token 過期或無效 → 清除登入狀態
            localStorage.removeItem('token');
 
            const isAlreadyOnLogin = window.location.pathname.includes('/login');
 
            // 避免在登入頁本身觸發無限跳轉
            if (!isAlreadyOnLogin) {
                window.location.href = '/login';
            }
        }
        return Promise.reject(error);
    }
);
export default apiClient;
