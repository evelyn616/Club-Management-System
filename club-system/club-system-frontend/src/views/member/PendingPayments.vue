<template>
  <div class="pending-payments">
    <h1>我的待繳款</h1>

    <div v-if="loading" class="loading">載入中...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    <div v-else>
      <div v-if="pendingPayments.length === 0" class="empty">您目前沒有任何待繳款活動喔~</div>
      <ul v-else class="pending-payments-list">
        <li v-for="reg in pendingPayments" :key="reg.id" class="pending-payments-card">
          活動ID: {{ reg.activityId }} - 報名日期: {{ reg.registrationTime }} - 付款狀態: {{ reg.paymentStatus }} - <input type="button" value="去付款"/>
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
const pendingPayments = ref([]);
//載入狀態，預設為還沒開始載入
const loading = ref(false);
//錯誤訊息，預設為沒有錯誤
const error = ref(null);

const loadPendingPayments = async () => {
//先將loading改為true，表示正在載入
  loading.value = true;
  error.value = null;
  
  try {
    //呼叫API取得使用者的報名資料
    const response = await registrationApi.getPendingPayments(userStore.userId);
    //將取得的資料存到pendingPayments
    pendingPayments.value = response.data;
  } catch (err) {
    //抓錯誤並顯示錯誤訊息
    error.value = '載入待繳款資料時發生錯誤，請稍後再試。';
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  loadPendingPayments();
});
</script>