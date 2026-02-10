<template>
  <div class="container">
    <div class="header">
      <h1 class="black-text">請假申請</h1>
      <div v-if="targetActivityTitle" class="activity-context">
        正在申請：<strong>{{ targetActivityTitle }}</strong> (ID: {{ form.activityId }})
      </div>
      <!-- ✅ 新增：顯示除錯資訊 -->
      <div style="background: #f0f0f0; padding: 10px; margin: 10px 0; font-size: 12px;">
        <strong>除錯資訊：</strong><br>
        接收到的 activityId: {{ route.query.activityId }}<br>
        接收到的 userId: {{ route.query.userId }}<br>
        表單中的 activityId: {{ form.activityId }}<br>
        表單中的 userId: {{ form.userId }}
      </div>
      <p class="black-text">目前狀態：<span class="offline-tag">填寫模式</span></p>
    </div>
    
    <section class="form-section">
      <div class="card">
        <h3 class="black-text"><i class="fas fa-plus"></i> 填寫申請單</h3>
        <div class="form-grid">
          <div class="input-group">
            <label class="black-text">會員 ID (userId)</label>
            <!-- ✅ 改成唯讀，顯示傳過來的值 -->
            <input v-model="form.userId" type="text" readonly class="black-text readonly-input">
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
            <input v-model="form.activityId" type="number" readonly class="black-text readonly-input">
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

// 1. 接收從上一頁傳過來的資料
const targetActivityTitle = ref(route.query.activityTitle || '');

// 2. ✅ 修正：使用傳過來的值，而不是鎖死
const form = ref({
  userId: route.query.userId || 'm0009',      // 優先使用傳過來的值
  activityId: Number(route.query.activityId) || '',   // 這個應該會正確接收
  leaveType: '事假',
  reason: ''
});

// ✅ 新增：頁面載入時檢查資料
onMounted(() => {
  console.log('=== AddLeave 頁面載入 ===');
  console.log('route.query:', route.query);
  console.log('form 初始值:', form.value);
  console.log('activityId 型別:', typeof form.value.activityId);
  
  // 如果沒有 activityId，顯示警告
  if (!form.value.activityId) {
    console.warn('⚠️ 警告：沒有接收到 activityId！');
    alert('錯誤：缺少活動 ID，請從報名列表重新進入');
  }
});

// 提交邏輯
const handleSubmit = async () => {
  // ✅ 加強驗證
  console.log('準備提交的資料:', JSON.stringify(form.value));
  
  if (!form.value.userId) {
    return alert('缺少會員 ID');
  }
  
  if (!form.value.activityId) {
    return alert('缺少活動 ID，請從報名列表重新選擇活動');
  }
  
  if (!form.value.reason || form.value.reason.trim() === '') {
    return alert('請填寫請假原因');
  }

  try {
    console.log('發送 POST 請求:', form.value);
    
    const response = await axios.post('http://localhost:8080/api/leaves', form.value);
    
    if (response.status === 201 || response.status === 200) {
      alert('請假申請已成功送出！');
      router.push('/leave-request-member'); 
    }
  } catch (error) {
    console.error("提交失敗:", error);
    console.error("錯誤詳情:", error.response);
    
    let errorMsg = '提交失敗';
    if (error.response) {
      // ✅ 後端修正後這裡能拿到具體錯誤訊息
      errorMsg = error.response.data?.message 
              || error.response.data 
              || `HTTP ${error.response.status} 錯誤`;
    } else {
      errorMsg = '無法連線到後端伺服器';
    }
    
    alert(errorMsg);
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