import { defineStore } from "pinia";
import { ref } from "vue";


export const useUserStore = defineStore("user", () => {

    const userId = ref(null);
    const userName = ref(null);
    const userEmail = ref(null);
    const userRole = ref(null);
    const creditPoints = ref(0);
    const isLoggedIn = ref(false);
    const token = ref(localStorage.getItem('token') || null);

    // 管理員二次驗證狀態（存 sessionStorage，關瀏覽器自動失效）
    const adminElevated = ref(sessionStorage.getItem('adminElevated') === 'true');

    //登入
    function login(jwtResponse) {
        token.value = jwtResponse.token;
        userId.value = jwtResponse.user.id;
        userName.value = jwtResponse.user.name;
        userEmail.value = jwtResponse.user.email;
        userRole.value = jwtResponse.user.role;
        creditPoints.value = jwtResponse.user.creditPoints ?? 0;
        isLoggedIn.value = true;
        localStorage.setItem('userId', jwtResponse.user.id);
        localStorage.setItem('userName', jwtResponse.user.name);
        localStorage.setItem('userEmail', jwtResponse.user.email);
        localStorage.setItem('userRole', jwtResponse.user.role);
        localStorage.setItem('creditPoints', jwtResponse.user.creditPoints ?? 0);
        localStorage.setItem('token', jwtResponse.token);
    }

    //登出
    function logout() {
        token.value = null;
        userId.value = null;
        userName.value = null;
        userEmail.value = null;
        userRole.value = null;
        creditPoints.value = 0;
        isLoggedIn.value = false;
        adminElevated.value = false;
        localStorage.removeItem('token');
        localStorage.removeItem('creditPoints');
        sessionStorage.removeItem('adminElevated');
    }

    // 提升為管理員模式（二次驗證成功後呼叫）
    function elevateAdmin() {
        adminElevated.value = true;
        sessionStorage.setItem('adminElevated', 'true');
    }

    // 退出管理員模式
    function revokeAdmin() {
        adminElevated.value = false;
        sessionStorage.removeItem('adminElevated');
    }

    function restoreFromStorage() {
        const savedToken = localStorage.getItem('token');
        if (savedToken) {
            token.value = savedToken;
            isLoggedIn.value = true;
            userId.value = localStorage.getItem('userId') || null;
            userName.value = localStorage.getItem('userName') || null;
            userEmail.value = localStorage.getItem('userEmail') || null;
            userRole.value = localStorage.getItem('userRole') || null;
            creditPoints.value = Number(localStorage.getItem('creditPoints')) || 0;
            adminElevated.value = sessionStorage.getItem('adminElevated') === 'true';
        }
    }

    return {
        userId,
        userName,
        userEmail,
        userRole,
        creditPoints,
        isLoggedIn,
        token,
        adminElevated,
        login,
        logout,
        elevateAdmin,
        revokeAdmin,
        restoreFromStorage
    };
});
