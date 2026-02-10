<template>
  <div class="container">
    <div class="top-bar">
      <button @click="goBack" class="btn-back">
        <i class="fas fa-arrow-left"></i> 返回
      </button>
    </div>
    <div class="header">
      <h1>請假申請紀錄</h1>
      <p>目前狀態：<span class="offline-tag">唯讀模式</span></p>
    </div>

    <section class="list-section">
      <div class="card">
        <h3><i class="fas fa-list"></i> 已提交的申請單</h3>
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
              <td>{{ item.userId }}</td>
              <td>{{ item.leaveType }}</td>
              <td>{{ item.reason }}</td>
              <td>
                <span :class="['status-badge', item.status.toLowerCase()]">
                  {{ item.status }}
                </span>
              </td>
              <td>
                <button @click="removeData(item.id)" class="btn-text">刪除紀錄</button>
              </td>
            </tr>
            <tr v-if="mockData.length === 0">
              <td colspan="6" style="text-align: center; padding: 20px; color: #999;">目前沒有任何申請紀錄</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router';

const router = useRouter();
// 初始資料設為空，等待後端連線
const mockData = ref([]);

// 頁面加載時自動抓取後端已提交的資料
// onMounted(async () => {
//   try {
//     const response = await axios.get('http://localhost:8080/api/leaves');
//     mockData.value = response.data;
//   } catch (error) {
//     console.error("無法抓取資料:", error);
//     // 如果後端沒開，可以暫時保留一些假資料測試畫面
//     mockData.value = [
//       { id: 101, memberId: 'M0001', leaveType: '事假', reason: '家裡有事', status: 'PENDING' },
//       { id: 102, memberId: 'M0005', leaveType: '病假', reason: '感冒發燒', status: 'APPROVED' }
//     ];
//   }
// });

onMounted(async () => {
  try {
    // 這裡調用你 Service 裡的 getLeavesByUserId 邏輯
    const response = await axios.get('http://localhost:8080/api/leaves/user/m0009');
    mockData.value = response.data; // 這裡的變數名可改為 leaveList
  } catch (error) {
    console.error("無法抓取個人資料:", error);
    mockData.value = []; // 清空，不顯示假資料
    alert('讀取紀錄失敗');
  }
});

// 模擬刪除功能
const removeData = async (id) => {
  if (confirm('確定要刪除這筆申請紀錄嗎？')) {
    try {
      await axios.delete(`http://localhost:8080/api/leaves/${id}`);
      mockData.value = mockData.value.filter(item => item.id !== id);
    } catch (error) {
      alert('刪除失敗，請檢查後端連線');
    }
  }
};
const goBack = () => {
  router.push('/');
};
</script>

<style scoped>
.container { padding: 40px; background-color: #f4f7f6; min-height: 100vh; color: #000000}
.card { background: white; padding: 25px; border-radius: 12px; box-shadow: 0 4px 6px rgba(0,0,0,0.05); }
.header { margin-bottom: 30px; }
.offline-tag { background: #e8f0fe; padding: 4px 8px; border-radius: 4px; font-size: 0.8em; color: #1967d2; }

.styled-table { width: 100%; border-collapse: collapse; margin-top: 20px; }
.styled-table th { text-align: left; padding: 12px; border-bottom: 2px solid #eee; color: #666; background-color: #fafafa; }
.styled-table td { padding: 12px; border-bottom: 1px solid #eee; }

.status-badge { padding: 4px 10px; border-radius: 20px; font-size: 0.85em; font-weight: bold; }
.status-badge.pending { background: #fff3e0; color: #ef6c00; }
.status-badge.approved { background: #e8f5e9; color: #2e7d32; }
.status-badge.rejected { background: #ffebee; color: #c62828; }

.btn-text { color: #ff5252; background: none; border: none; cursor: pointer; font-weight: 500; }
.btn-text:hover { text-decoration: underline; }
</style>