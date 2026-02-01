<template>
  <div class="container">
    <div class="header">
      <h1 class="black-text">請假申請</h1>
      <div v-if="targetActivityTitle" class="activity-context">
        正在申請：<strong>{{ targetActivityTitle }}</strong> (ID: {{ form.activityId }})
      </div>
      <p class="black-text">目前狀態：<span class="offline-tag">填寫模式</span></p>
    </div>

    <section class="form-section">
      <div class="card">
        <h3 class="black-text"><i class="fas fa-plus"></i> 填寫申請單</h3>
        <div class="form-grid">
          <div class="input-group">
            <label class="black-text">會員 ID (userId)</label>
            <input v-model="form.userId" type="text" placeholder="例如: m0001" class="black-text">
          </div>

          <div class="input-group">
            <label class="black-text">請假類型</label>
            <select v-model="form.leaveType" class="black-text">
              <option value="事假">事假</option>
              <option value="病假">病假</option>
              <option value="公假">公假</option>
            </select>
          </div>

          <div class="input-group full-width">
            <label class="black-text">對應活動 ID</label>
            <input v-model="form.activityId" type="text" readonly class="black-text readonly-input">
          </div>

          <div class="input-group full-width">
            <label class="black-text">請假原因</label>
            <textarea v-model="form.reason" rows="4" class="black-text" placeholder="請輸入詳細原因"></textarea>
          </div>
        </div>
        <button @click="handleSubmit" class="btn-primary">提交申請</button>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();

// 1. 接收從上一頁 (RegistrationList) 傳過來的標題
const targetActivityTitle = ref(route.query.activityTitle || '');

// 2. 初始化表單資料，直接對齊後端 DTO 欄位名稱
const form = ref({
  userId: route.query.uId || '',         // 接收 uId
  activityId: route.query.activityId || '', // 接收 activityId
  leaveType: '事假',
  reason: ''
});

// 提交邏輯
const handleSubmit = async () => {
  // 基本驗證
  if (!form.value.userId || !form.value.reason || !form.value.activityId) {
    return alert('請確認會員 ID、活動 ID 與原因均已填寫');
  }

  try {
    // 這裡送出的 JSON 格式將會是 { userId: '...', activityId: '...', leaveType: '...', reason: '...' }
    const response = await axios.post('http://localhost:8080/api/leaves', form.value);
    
    if (response.status === 201 || response.status === 200) {
      alert('請假申請已成功送出！');
      // 成功後跳轉回紀錄頁面或上一頁
      router.push('/leave-request-vue'); 
    }
  } catch (error) {
    console.error("提交失敗:", error);
    const errorMsg = error.response?.data?.message || '請檢查後端連線或資料格式';
    alert('提交失敗：' + errorMsg);
  }
};
</script>

<style scoped>
/* 全域設定文字為黑色 */
.black-text {
  color: #000000 !important;
}

.container { 
  padding: 40px; 
  background-color: #f4f7f6; 
  min-height: 100vh; 
}

.header { 
  margin-bottom: 30px; 
}

.offline-tag { 
  background: #eee; 
  padding: 4px 8px; 
  border-radius: 4px; 
  font-size: 0.8em; 
  color: #000; 
  border: 1px solid #ccc;
}

.card { 
  background: white; 
  padding: 35px; 
  border-radius: 12px; 
  box-shadow: 0 8px 16px rgba(0,0,0,0.1); 
  max-width: 800px;
  margin: 0 auto;
}

.form-grid { 
  display: grid; 
  grid-template-columns: 1fr 1fr; 
  gap: 25px; 
  margin-bottom: 30px; 
}

.full-width { 
  grid-column: span 2; 
}

.input-group label { 
  display: block; 
  margin-bottom: 10px; 
  font-weight: bold; 
}

input, select, textarea { 
  width: 100%; 
  border: 2px solid #ddd; 
  padding: 12px; 
  border-radius: 8px; 
  font-size: 1rem;
  outline: none;
  transition: border-color 0.3s;
}

input:focus, select:focus, textarea:focus {
  border-color: #42b983;
}

.btn-primary { 
  background: #42b983; 
  color: white; 
  border: none; 
  padding: 15px 30px; 
  border-radius: 8px; 
  cursor: pointer; 
  font-weight: bold; 
  font-size: 1.1rem;
  width: 100%;
  transition: background 0.3s;
}

.btn-primary:hover {
  background: #369a6d;
}
</style>