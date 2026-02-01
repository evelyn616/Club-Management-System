<template>
  <div class="container">
    <div class="header">
      <h1><i class="fas fa-calendar-check"></i> 我的活動報名</h1>
      <p>僅顯示已報名且可請假的活動</p>
    </div>

    <div class="card">
      <table class="styled-table">
        <thead>
          <tr>
            <th>報名單號</th>
            <th>活動名稱</th>
            <th>報名時間</th>
            <th>狀態</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="reg in registeredList" :key="reg.id">
            <td>#{{ reg.id }}</td>
            <td>
              <div class="activity-info">
                <strong>{{ reg.activityTitle }}</strong>
                <span class="id-tag">ID: {{ reg.activityId }}</span>
              </div>
            </td>
            <td>{{ formatDate(reg.createdAt) }}</td>
            <td>
              <span class="status-badge registered">已報名</span>
            </td>
            <td>
              <button @click="goToLeave(reg)" class="btn-leave">
                <i class="fas fa-sign-out-alt"></i> 我要請假
              </button>
            </td>
          </tr>
          <tr v-if="registeredList.length === 0">
            <td colspan="5" class="no-data">目前沒有已報名的活動</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const registeredList = ref([]);

// 🛠️ 改成模擬假資料
const fetchRegistrations = () => {
  console.log("正在加載模擬報名資料...");
  
  // 模擬從資料庫抓回來的 REGISTERED 狀態資料
  const mockData = [
    { 
      id: 1001, 
      activityId: 1, 
      activityTitle: '熱舞社期末成果展 - 第一次團練', 
      createdAt: '2026-02-01T10:00:00',
      status: 'REGISTERED',
      userId: 'm0001'
    },
    { 
      id: 1005, 
      activityId: 2, 
      activityTitle: '基礎律動教學課程', 
      createdAt: '2026-02-02T14:30:00',
      status: 'REGISTERED',
      userId: 'm0001'
    },
    { 
      id: 1010, 
      activityId: 5, 
      activityTitle: '社團迎新晚會', 
      createdAt: '2026-02-05T09:00:00',
      status: 'REGISTERED',
      userId: 'm0001'
    }
  ];

  // 為了模擬 API 非同步感，可以用個 setTimeout (選用)
  registeredList.value = mockData;
};

const goToLeave = (reg) => {
  // 這裡會跳轉到 AddLeave 頁面，網址會變成 /leave-request-member?activityId=1&activityTitle=...
  router.push({
    name: 'add-leave', 
    query: { 
      activityId: reg.activityId,
      activityTitle: reg.activityTitle,
      uId: reg.userId // 順便把會員 ID 帶過去
    }
  });
};

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return `${date.getFullYear()}/${date.getMonth() + 1}/${date.getDate()} ${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`;
};

onMounted(fetchRegistrations);
</script>

<style scoped>
.container { padding: 40px; max-width: 1000px; margin: 0 auto; color: #000000}
.card { background: white; padding: 20px; border-radius: 12px; box-shadow: 0 4px 10px rgba(0,0,0,0.1); }
.styled-table { width: 100%; border-collapse: collapse; }
.styled-table th { text-align: left; padding: 15px; border-bottom: 2px solid #eee; }
.styled-table td { padding: 15px; border-bottom: 1px solid #eee; }

.activity-info { display: flex; flex-direction: column; }
.id-tag { font-size: 0.8em; color: #999; }

.status-badge.registered { background: #e3f2fd; color: #1976d2; padding: 4px 10px; border-radius: 20px; font-size: 0.9em; }

.btn-leave { 
  background: #ff9800; color: white; border: none; padding: 8px 16px; 
  border-radius: 6px; cursor: pointer; transition: 0.3s; 
}
.btn-leave:hover { background: #f57c00; transform: scale(1.05); }

.no-data { text-align: center; color: #999; padding: 40px; }
</style>