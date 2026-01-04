<template>
  <div class="registrable-activities">
    <h1>可報名活動</h1>

    <div v-if="loading" class="loading">載入中...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    <div v-else>
      <div v-if="registrableActivities.length === 0" class="empty">目前沒有任何可報名活動喔~</div>
      <ul v-else class="registrable-activities-list">
        <li v-for="act in registrableActivities" :key="act.id" class="registrable-activities-card">
          活動名稱: {{ act.title }} - 活動日期: {{ act.startTime }} - 活動地點: {{ act.location }} - 簡介: {{ act.description }} - <input type="button" value="報名"/>
        </li>
      </ul>
    </div>
  </div>
 
</template>

<script setup>
import { ref, onMounted } from 'vue';
import {activityApi} from '@/api/activity';



//空陣列
const registrableActivities = ref([]);
//載入狀態，預設為還沒開始載入
const loading = ref(false);
//錯誤訊息，預設為沒有錯誤
const error = ref(null);

const loadRegistrableActivities = async () => {
//先將loading改為true，表示正在載入
  loading.value = true;
  error.value = null;
  
  try {
    //呼叫API取得使用者的報名資料
    const response = await activityApi.getRegistrableActivities();
    //將取得的資料存到pendingPayments
    registrableActivities.value = response.data;
  } catch (err) {
    //抓錯誤並顯示錯誤訊息
    error.value = '載入可報名活動資料時發生錯誤，請稍後再試。';
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  loadRegistrableActivities();
});
</script>