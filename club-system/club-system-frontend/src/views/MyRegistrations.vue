<template>
  <div class="my-registrations">
    <h1>我的報名</h1>

    <div v-if="loading" class="loading">載入中...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    <div v-else>
      <div v-if="registrations.length === 0" class="empty">您目前沒有任何報名資料。</div>
      <ul v-else class="registration-list">
        <li v-for="reg in registrations" :key="reg.id" class="registration-card">
          活動ID: {{ reg.activityId }} - 報名日期: {{ reg.registrationTime }} - 狀態: {{ reg.status }} - 付款狀態: {{ reg.paymentStatus }}
        </li>
      </ul>
    </div>
  </div>
 
</template>

<script setup>
import { ref, onMounted } from 'vue';
import {registrationApi} from '@/api/registration';
import {useUserStore} from '@/stores/user';

const userStore = useUserStore();

//空陣列
const registrations = ref([]);
//載入狀態，預設為還沒開始載入
const loading = ref(false);
//錯誤訊息，預設為沒有錯誤
const error = ref(null);

const loadRegistrations = async () => {
//先將loading改為true，表示正在載入
  loading.value = true;
  error.value = null;
  
  try {
    //呼叫API取得使用者的報名資料
    const response = await registrationApi.getMyRegistrations(userStore.userId);
    //將取得的資料存到registrations
    registrations.value = response.data;
  } catch (err) {
    //抓錯誤並顯示錯誤訊息
    error.value = '載入報名資料時發生錯誤，請稍後再試。';
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  loadRegistrations();
});
</script>