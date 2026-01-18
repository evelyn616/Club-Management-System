<template>
  <div class="container">
    <div class="header">
      <h1>請假申請</h1>
      <p>目前狀態：<span class="offline-tag">後端離線測試中</span></p>
    </div>

    <section class="form-section">
      <div class="card">
        <h3><i class="fas fa-plus"></i> 填寫申請單</h3>
        <div class="form-grid">
          <div class="input-group">
            <label>會員編號</label>
            <input v-model="form.memberId" type="text" placeholder="例如: m0001">
          </div>
          <div class="input-group">
            <label>請假類型</label>
            <select v-model="form.leaveType">
              <option value="事假">事假</option>
              <option value="病假">病假</option>
              <option value="公假">公假</option>
            </select>
          </div>
          <div class="input-group full-width">
            <label>請假原因</label>
            <textarea v-model="form.reason" rows="3"></textarea>
          </div>
        </div>
        <button @click="handleMockSubmit" class="btn-primary">提交申請</button>
      </div>
    </section>

    <section class="list-section">
      <div class="card">
        <h3><i class="fas fa-list"></i> 申請紀錄</h3>
        <table class="styled-table">
          <thead>
            <tr>
              <th>單號</th>
              <th>會員編號</th>
              <th>類型</th>
              <th>原因</th>
              <th>狀態</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in mockData" :key="item.id">
              <td>#{{ item.id }}</td>
              <td>{{ item.memberId }}</td>
              <td>{{ item.leaveType }}</td>
              <td>{{ item.reason }}</td>
              <td>
                <span :class="['status-badge', item.status.toLowerCase()]">
                  {{ item.status }}
                </span>
              </td>
              <td>
                <button @click="removeData(item.id)" class="btn-text">刪除</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref } from 'vue';

// 模擬資料 (Mock Data)
const mockData = ref([
  { id: 101, memberId: 'M0001', leaveType: '事假', reason: '家裡有事', status: 'PENDING' },
  { id: 102, memberId: 'M0005', leaveType: '病假', reason: '感冒發燒', status: 'APPROVED' }
]);

const form = ref({
  memberId: '',
  leaveType: '事假',
  reason: ''
});

// 模擬提交邏輯
const handleMockSubmit = () => {
  if (!form.value.memberId || !form.value.reason) return alert('請填寫完整');
  
  const newData = {
    id: Date.now(), // 隨機生成 ID
    ...form.value,
    status: 'PENDING'
  };
  
  mockData.value.unshift(newData); // 加到列表最前面
  alert('模擬提交成功！');
  // 重置表單
  form.value = { memberId: '', leaveType: '事假', reason: '' };
};

// 模擬刪除
const removeData = (id) => {
  mockData.value = mockData.value.filter(item => item.id !== id);
};
</script>

<style scoped>
/* 美化過的 CSS */
.container { padding: 40px; background-color: #f4f7f6; min-height: 100vh; }
.card { background: white; padding: 25px; border-radius: 12px; box-shadow: 0 4px 6px rgba(0,0,0,0.05); }
.header { margin-bottom: 30px; }
.offline-tag { background: #eee; padding: 4px 8px; border-radius: 4px; font-size: 0.8em; color: #666; }
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 20px; }
.full-width { grid-column: span 2; }
.input-group label { display: block; margin-bottom: 8px; font-weight: bold; color: #333; }
input, select, textarea { width: 100%; border: 1px solid #ddd; padding: 10px; border-radius: 6px; }
.btn-primary { background: #42b983; color: white; border: none; padding: 12px 24px; border-radius: 6px; cursor: pointer; font-weight: bold; }
.styled-table { width: 100%; border-collapse: collapse; margin-top: 20px; }
.styled-table th { text-align: left; padding: 12px; border-bottom: 2px solid #eee; color: #666; }
.styled-table td { padding: 12px; border-bottom: 1px solid #eee; }
.status-badge { padding: 4px 10px; border-radius: 20px; font-size: 0.85em; }
.status-badge.pending { background: #fff3e0; color: #ef6c00; }
.status-badge.approved { background: #e8f5e9; color: #2e7d32; }
.btn-text { color: #ff5252; background: none; border: none; cursor: pointer; }
</style>