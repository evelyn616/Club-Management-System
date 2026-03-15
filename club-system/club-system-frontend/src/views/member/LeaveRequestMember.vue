<template>
  <div class="outer-wrapper">
    <div class="container">
      <header class="page-header">
        <button @click="goBack" class="btn-back">
          <i class="fas fa-chevron-left"></i> 總覽
        </button>
        <div class="title-row">
          <h1>請假申請紀錄</h1>
          <div class="badge-group">
            <span class="count-tag">{{ mockData.length }} 筆紀錄</span>
            <span class="mode-tag">個人查詢</span>
          </div>
        </div>
      </header>

      <main class="content-card">
        <div class="table-responsive">
          <table>
            <thead>
              <tr>
                <th width="10%">單號</th>
                <th width="15%">姓名</th>
                <th width="25%">對應活動</th>
                <th width="15%">類型</th>
                <th width="25%">原因</th>
                <th width="10%">狀態</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in mockData" :key="item.id">
                <td><span class="id-text">#{{ item.id }}</span></td>
                <td>
                  <div class="user-info">
                    <span class="user-name">{{ userNames[item.userId] || '載入中...' }}</span>
                  </div>
                </td>
                <td>
                  <div class="activity-info">
                    <i class="fas fa-calendar-alt icon-dim"></i>
                    {{ activityTitles[item.activityId] || '載入中...' }}
                  </div>
                </td>
                <td>
                  <span class="type-tag">{{ item.leaveType }}</span>
                </td>
                <td>
                  <p class="reason-text" :title="item.reason">{{ item.reason }}</p>
                </td>
                <td>
                  <div :class="['status-indicator', item.status.toLowerCase()]">
                    <span class="dot"></span>
                    {{ item.status }}
                  </div>
                </td>
              </tr>
              
              <tr v-if="mockData.length === 0">
                <td colspan="6" class="empty-state">
                  <i class="fas fa-inbox"></i>
                  <p>目前沒有任何申請紀錄</p>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';

const router = useRouter();
const userStore = useUserStore();
const mockData = ref([]);

// 名稱對照表
const userNames = ref({});
const activityTitles = ref({});

const fetchDetails = async (data) => {
  const authConfig = { headers: { Authorization: `Bearer ${userStore.token}` } };
  
  // 取得清單中所有不重複的 ID
  const userIds = [...new Set(data.map(item => item.userId))];
  const activityIds = [...new Set(data.map(item => item.activityId))];

  // 抓取用戶姓名
  userIds.forEach(async (id) => {
    if (!userNames.value[id]) {
      try {
        const res = await axios.get(`http://localhost:8080/api/users/${id}`, authConfig);
        userNames.value[id] = res.data.name;
      } catch (e) {
        userNames.value[id] = id; // 失敗則顯示 ID
      }
    }
  });

  // 抓取活動標題
  activityIds.forEach(async (id) => {
    if (!activityTitles.value[id]) {
      try {
        const res = await axios.get(`http://localhost:8080/api/activities/${id}`, authConfig);
        activityTitles.value[id] = res.data.title;
      } catch (e) {
        activityTitles.value[id] = `活動 #${id}`;
      }
    }
  });
};

onMounted(async () => {
  if (!userStore.userId) {
    alert('請先登入以查看請假紀錄');
    router.push('/login');
    return;
  }

  try {
    const response = await axios.get(`http://localhost:8080/api/leaves/user/${userStore.userId}`, {
      headers: { 'Authorization': `Bearer ${userStore.token}` }
    });
    mockData.value = response.data;
    
    // 取得紀錄後，緊接著抓取對應的名稱細節
    if (mockData.value.length > 0) {
      await fetchDetails(mockData.value);
    }
  } catch (error) {
    console.error("讀取失敗:", error);
    if (error.response?.status === 401) {
      alert('登入過期，請重新登入');
      router.push('/login');
    } else {
      alert('讀取紀錄失敗');
    }
  }
});

const goBack = () => {
  router.push('/dashboard');
};
</script>

<style scoped>
/* 核心變數：全面調深顏色 */
:host {
  --primary-color: #2563eb;
  --bg-color: #f8fafc;
  --text-main: #000000;      /* 純黑 */
  --text-content: #1a1a1a;   /* 極深灰 */
  --text-muted: #333333;     /* 深灰，取代原本的淺灰 */
  --border-color: #cbd5e1;   /* 稍微加深邊框 */
}

.outer-wrapper {
  font-family: 'Inter', "Microsoft JhengHei", -apple-system, sans-serif;
  background-color: #f8fafc;
  min-height: 100vh;
  padding: 40px 24px;
}

.container {
  max-width: 1100px;
  margin: 0 auto;
}

/* 頁首設計 */
.page-header {
  margin-bottom: 32px;
}

.btn-back {
  background: white;
  border: 1px solid #000000; /* 邊框調黑 */
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 13px;
  color: #000000;           /* 文字調黑 */
  font-weight: 600;
  cursor: pointer;
  margin-bottom: 16px;
  transition: all 0.2s;
}

.btn-back:hover {
  background-color: #000000;
  color: #ffffff;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 16px;
}

h1 {
  font-size: 28px;
  font-weight: 700;
  color: #000000;           /* 標題純黑 */
  margin: 0;
  letter-spacing: -0.5px;
}

.badge-group {
  display: flex;
  gap: 8px;
}

.count-tag {
  background: #000000;      /* 標籤改為黑底白字 */
  color: #ffffff;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.mode-tag {
  background: #e2e8f0;
  color: #000000;           /* 文字純黑 */
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

/* 卡片與表格 */
.content-card {
  background: white;
  border-radius: 12px;
  border: 2px solid #000000; /* 加粗黑邊框 */
  box-shadow: 4px 4px 0px rgba(0, 0, 0, 1); /* 硬派陰影風格 */
  overflow: hidden;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th {
  background: #f1f5f9;
  padding: 16px;
  font-size: 13px;
  font-weight: 800;          /* 字體加粗 */
  color: #000000;           /* 標題純黑 */
  text-align: left;
  border-bottom: 2px solid #000000;
}

td {
  padding: 18px 16px;
  font-size: 14px;
  color: #000000;           /* 表格內容純黑 */
  border-bottom: 1px solid #e2e8f0;
}

.id-text {
  color: #333333;           /* ID 調深 */
  font-family: 'JetBrains Mono', monospace;
  font-weight: 600;
}

.user-name {
  font-weight: 700;
  color: #000000;
}

.activity-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #000000;           /* 活動名稱調黑 */
  font-weight: 500;
}

.icon-dim {
  color: #000000;           /* 圖示也調黑 */
}

.type-tag {
  border: 1px solid #000000;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  color: #000000;
  font-weight: 600;
}

.reason-text {
  margin: 0;
  color: #333333;           /* 原因描述調為深灰黑 */
  font-weight: 400;
}

/* 狀態指示器 (維持顏色邏輯但調深文字) */
.status-indicator {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  font-weight: 800;
  padding: 4px 10px;
  border-radius: 6px;
  border: 1px solid currentColor; /* 加上邊框增加識別度 */
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.status-indicator.pending {
  background: #fff7ed; color: #9a3412; /* 深橘 */
}
.status-indicator.pending .dot { background: #ea580c; }

.status-indicator.approved {
  background: #f0fdf4; color: #166534; /* 深綠 */
}
.status-indicator.approved .dot { background: #16a34a; }

.status-indicator.rejected {
  background: #fef2f2; color: #991b1b; /* 深紅 */
}
.status-indicator.rejected .dot { background: #dc2626; }

/* 空狀態 */
.empty-state {
  padding: 80px 0;
  text-align: center;
  color: #000000;
}

.empty-state i {
  font-size: 48px;
  margin-bottom: 16px;
}

@media (max-width: 768px) {
  .outer-wrapper { padding: 20px 12px; }
  th:nth-child(5), td:nth-child(5) { display: none; }
}
</style>