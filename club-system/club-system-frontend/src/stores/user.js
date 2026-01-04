import { defineStore } from "pinia";
import { ref } from "vue";

//暫時寫死userId，userName，isLoggedIn等資訊
export const useUserStore = defineStore("user", () => {

    const userId = ref("M001");
    const userName = ref("王大明");
    const isLoggedIn = ref(true);

    return{
        userId,
        userName,
        isLoggedIn
    };
});
