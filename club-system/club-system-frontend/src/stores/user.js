import { defineStore } from "pinia";
import { ref } from "vue";

//暫時寫死userId，userName，isLoggedIn等資訊
export const useUserStore = defineStore("user", () => {

    const userId = ref(null);
    const userName = ref(null);
    const isLoggedIn = ref(false);
    const token = ref(localStorage.getItem('token') || null);

    //登入
    function login(jwtResponse) {
        token.value = jwtResponse.token;
        userId.value = jwtResponse.user.id;
        userName.value = jwtResponse.user.name;
        isLoggedIn.value = true;
        localStorage.setItem('token',jwtResponse.token);
    }

    //登出
    function logout(){
        token.value = null;
        userId.value = null;
        userName.value = null;
        isLoggedIn.value = false;
        localStorage.removeItem('token');

    }

    //重新載入頁面時，若還攜帶token就自動還員登入狀態
    async function restoreFromStorage(){
        const saveToken = localStorage.getItem('token');
        if(saveToken){
            token.value = saveToken;
            isLoggedIn.value = true;

            try{
                const response = await fetch('http://localhost:8080/api/auth/me',{
                    headers: {
                        'Authorization': `Bearer ${saveToken}`
                    }
                });
                if(response.ok){
                    const userData = await response.json();
                    userId.value = userData.id;
                    userName.value = userData.name;
                }
            }
            catch(error){
                console.error('還原用戶資訊失敗:', error);
            // 如果失敗就清掉 token
            logout();
            }
        }

    }

    return{
        userId,
        userName,
        isLoggedIn,
        token,
        login,
        logout,
        restoreFromStorage
    };
});
