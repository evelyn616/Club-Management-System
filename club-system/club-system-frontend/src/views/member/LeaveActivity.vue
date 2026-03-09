<template>
  <div class="outer-wrapper">
    <div class="container">
      <div class="top-navigation">
        <button @click="router.back()" class="btn-back-minimal">
          <i class="fas fa-arrow-left"></i> 返回
        </button>
      </div>

      <h1>我的報名列表</h1>
      
      <div class="table-responsive">
        <table>
          <thead>
            <tr>
              <th>課程名稱 / 活動</th>
              <th>報名日期</th>
              <th>狀態</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="reg in registeredList" :key="reg.id">
              <td>
                <div class="activity-cell">
                  <span class="activity-name">{{ reg.activityTitle || '活動 #' + reg.activityId }}</span>
                  <span class="reg-id">單號: #{{ reg.id }}</span>
                </div>
              </td>
              <td class="date-cell">{{ formatDate(reg.createdAt) }}</td>
              <td>
                <span class="status-text">已報名</span>
              </td>
              <td>
                <button @click="goToLeave(reg)" class="btn-leave-minimal">
                  我要請假
                </button>
              </td>
            </tr>
            
            <tr v-if="registeredList.length === 0">
              <td colspan="4" class="no-data-minimal">目前沒有已報名的活動</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import { useUserStore } from '@/stores/user';

const router = useRouter();
const registeredList = ref([]);
const userStore = useUserStore();
const loading = ref(false);

const fetchRegistrations = async () => {
  if (!userStore.userId) {
    console.error("未找到使用者 ID");
    return;
  }

  const token = userStore.token; 

  loading.value = true;
  try {
    const response = await axios.get(`http://localhost:8080/api/registrations/my?userId=${userStore.userId}`, {
      headers: {
        'Authorization': `Bearer ${token}` 
      }
    });
    registeredList.value = response.data; 
  } catch (error) {
    console.error("讀取報名資料失敗:", error);
    
    if (error.response && error.response.status === 401) {
      alert("登入驗證已過期，請重新登入");
      // router.push('/login');
    } else {
      alert("無法取得報名紀錄，請檢查後端連線");
    }
  } finally {
    loading.value = false;
  }
};

const goToLeave = (reg) => {
  router.push({
    name: 'add-leave', 
    query: { 
      activityId: reg.activityId,
      activityTitle: reg.activityTitle || ('活動 #' + reg.activityId),
      uId: reg.userId
    }
  });
};

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  // 配合簡約風，日期格式微調為 YYYY.MM.DD
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, '0');
  const d = String(date.getDate()).padStart(2, '0');
  return `${y}.${m}.${d}`;
};

const goHome = () => {
  router.push('/');
};

onMounted(fetchRegistrations);
</script>

<style scoped>
/* 載入 Inter 字體 */
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
  max-width: 900px;
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

h1 {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 32px;
  letter-spacing: -0.5px;
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
  font-size: 14px;
  color: #666666;
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
  font-size: 15px;
  vertical-align: middle;
}

/* 活動單號微調 */
.activity-cell {
  display: flex;
  flex-direction: column;
}

.activity-name {
  font-weight: 600;
}

.reg-id {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.date-cell {
  color: #666;
}

.status-text {
  font-size: 14px;
  color: #1a1a1a;
}

.btn-leave-minimal {
  background-color: #1a1a1a;
  color: white;
  border: none;
  padding: 8px 16px;
  font-size: 13px;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.btn-leave-minimal:hover {
  background-color: #404040;
  transform: translateY(-1px);
}

.no-data-minimal {
  text-align: center;
  padding: 60px;
  color: #999;
}

/* 響應式優化 */
@media (max-width: 600px) {
  td, th {
    padding: 12px 8px;
    font-size: 13px;
  }
  .btn-leave-minimal {
    padding: 6px 10px;
  }
  h1 {
    font-size: 20px;
  }
}
</style>