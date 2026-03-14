import { defineStore } from "pinia";
import { ref } from "vue";


export const useUserStore = defineStore("user", () => {

    const userId = ref(null);
    const userName = ref(null);
    const userRole = ref(null);
    const isLoggedIn = ref(false);
    const token = ref(localStorage.getItem('token') || null);

    //登入
    function login(jwtResponse) {
        token.value = jwtResponse.token;
        userId.value = jwtResponse.user.id;
        userName.value = jwtResponse.user.name;
        userRole.value = jwtResponse.user.role;
        isLoggedIn.value = true;
        localStorage.setItem('userId', jwtResponse.user.id);
        localStorage.setItem('userName', jwtResponse.user.name);
        localStorage.setItem('userRole', jwtResponse.user.role);
        localStorage.setItem('token',jwtResponse.token);
    }

    //登出
    function logout(){
        token.value = null;
        userId.value = null;
        userName.value = null;
        userRole.value = null;
        isLoggedIn.value = false;
        localStorage.removeItem('token');

    }

    function restoreFromStorage() {
    const savedToken = localStorage.getItem('token');
    if (savedToken) {
        token.value = savedToken;
        isLoggedIn.value = true;
        userId.value = localStorage.getItem('userId') || null;
        userName.value = localStorage.getItem('userName') || null;
        userRole.value = localStorage.getItem('userRole') || null;
    }
}

    return{
        userId,
        userName,
        userRole,
        isLoggedIn,
        token,
        login,
        logout,
        restoreFromStorage
    };
});
