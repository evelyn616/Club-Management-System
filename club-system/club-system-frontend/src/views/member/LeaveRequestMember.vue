<template>
  <div class="outer-wrapper">
    <div class="container">
      <div class="top-navigation">
        <button @click="goBack" class="btn-back-minimal">
          <i class="fas fa-arrow-left"></i> 返回
        </button>
      </div>

      <div class="header-section">
        <h1>請假申請紀錄</h1>
        <span class="mode-tag">唯讀模式</span>
      </div>
      
      <div class="table-responsive">
        <table>
          <thead>
            <tr>
              <th>單號</th>
              <th>會員編號</th>
              <th>請假類型</th>
              <th>原因</th>
              <th>狀態</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in mockData" :key="item.id">
              <td><span class="id-text">#{{ item.id }}</span></td>
              <td class="user-id-cell">{{ item.userId }}</td>
              <td>{{ item.leaveType }}</td>
              <td class="reason-cell">{{ item.reason }}</td>
              <td>
                <span :class="['status-badge', item.status.toLowerCase()]">
                  {{ item.status }}
                </span>
              </td>
              <td>
                <button @click="removeData(item.id)" class="btn-delete-minimal">
                  刪除
                </button>
              </td>
            </tr>
            
            <tr v-if="mockData.length === 0">
              <td colspan="6" class="no-data-minimal">目前沒有任何申請紀錄</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';

const router = useRouter();
// 初始資料設為空，等待後端連線
const mockData = ref([]);
const userStore = useUserStore();

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
  if (!userStore.userId) {
    console.error("未找到使用者 ID");
    alert('請先登入以查看請假紀錄');
    router.push('/login');
    return;
  }

  try {
    // 這裡調用你 Service 裡的 getLeavesByUserId 邏輯
    const response = await axios.get(`http://localhost:8080/api/leaves/user/${userStore.userId}`);
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
  router.push('/dashboard');
};
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;600&display=swap');

.outer-wrapper {
  font-family: 'Inter', "Microsoft JhengHei", sans-serif;
  background-color: #ffffff;
  min-height: 100vh;
  display: flex;
  justify-content: center;
  padding: 40px 20px;
  color: #1a1a1a;
}

.container {
  width: 100%;
  max-width: 1000px; /* 紀錄頁稍微寬一點點 */
}

.top-navigation {
  margin-bottom: 24px;
}

.btn-back-minimal {
  background: none;
  border: none;
  color: #666;
  font-size: 14px;
  cursor: pointer;
  padding: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: color 0.2s;
}

.btn-back-minimal:hover {
  color: #1a1a1a;
}

.header-section {
  display: flex;
  align-items: baseline;
  gap: 16px;
  margin-bottom: 32px;
}

h1 {
  font-size: 24px;
  font-weight: 600;
  margin: 0;
  letter-spacing: -0.5px;
}

.mode-tag {
  font-size: 12px;
  background-color: #f0f0f0;
  color: #666;
  padding: 2px 8px;
  border-radius: 4px;
}

.table-responsive {
  width: 100%;
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
  text-align: left;
}

thead th {
  font-weight: 600;
  font-size: 13px;
  color: #999;
  text-transform: uppercase;
  padding: 12px 16px;
  border-bottom: 2px solid #1a1a1a;
}

tbody tr {
  border-bottom: 1px solid #eeeeee;
  transition: background-color 0.2s ease;
}

tbody tr:hover {
  background-color: #fafafa;
}

td {
  padding: 20px 16px;
  font-size: 14px;
  vertical-align: middle;
}

.id-text {
  font-family: monospace;
  color: #666;
}

.user-id-cell {
  font-weight: 600;
}

.reason-cell {
  max-width: 250px;
  color: #444;
  line-height: 1.5;
}

/* 狀態標籤優化：維持簡約但有顏色區分 */
.status-badge {
  font-size: 12px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 4px;
  text-transform: uppercase;
}

.status-badge.pending {
  background-color: #fff3e0;
  color: #ef6c00;
}

.status-badge.approved {
  background-color: #e8f5e9;
  color: #2e7d32;
}

.status-badge.rejected {
  background-color: #ffebee;
  color: #c62828;
}

.btn-delete-minimal {
  background: none;
  border: 1px solid #eeeeee;
  color: #ff5252;
  padding: 6px 12px;
  font-size: 12px;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.2s;
}

.btn-delete-minimal:hover {
  background-color: #ff5252;
  color: white;
  border-color: #ff5252;
}

.no-data-minimal {
  text-align: center;
  padding: 80px;
  color: #bbb;
  font-size: 14px;
}

@media (max-width: 768px) {
  .reason-cell {
    display: none; /* 手機版隱藏原因欄位以節省空間 */
  }
}
</style>