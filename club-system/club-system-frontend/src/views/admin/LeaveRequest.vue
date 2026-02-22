<template>
  <div class="outer-wrapper">
    <div class="container">
      <div class="top-navigation">
        <button @click="goBack" class="btn-back-minimal">
          <i class="fas fa-arrow-left"></i> 返回
        </button>
      </div>

      <div class="header-section">
        <div class="title-group">
          <h1>管理員審核後台</h1>
          <span class="admin-badge">系統管理員</span>
        </div>
        <div class="stats-minimal">
          <div class="stat-item">
            <span class="label">總申請</span>
            <span class="value">{{ mockData.length }}</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item highlight">
            <span class="label">待處理</span>
            <span class="value">{{ pendingCount }}</span>
          </div>
        </div>
      </div>

      <section class="filter-panel">
        <div class="filter-group">
          <div class="toggle-group">
            <button 
              :class="['toggle-btn', { active: filterType === 'member' }]"
              @click="filterType = 'member'; filterValue = 'all'"
            >按會員</button>
            <button 
              :class="['toggle-btn', { active: filterType === 'activity' }]"
              @click="filterType = 'activity'; filterValue = 'all'"
            >按活動</button>
          </div>

          <div class="select-wrapper">
            <select v-model="filterValue" class="minimal-select">
              <option value="all">顯示全部來源</option>
              <option v-for="opt in currentOptions" :key="opt" :value="opt">
                {{ filterType === 'member' ? '會員' : '活動' }}: {{ opt }}
              </option>
            </select>
          </div>

          <button @click="resetFilter" class="btn-reset">重置</button>
        </div>
      </section>

      <div class="table-responsive">
        <table>
          <thead>
            <tr>
              <th>單號</th>
              <th>會員 / 活動</th>
              <th>請假類型</th>
              <th>狀態</th>
              <th>審核操作</th>
              <th>管理</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in filteredData" :key="item.id">
              <td><span class="id-text">#{{ item.id }}</span></td>
              <td>
                <div class="info-cell">
                  <span class="user-id">👤 {{ item.userId }}</span>
                  <span class="activity-id">🎯 {{ item.activityId }}</span>
                </div>
              </td>
              <td>{{ item.leaveType }}</td>
              <td>
                <span :class="['status-badge', item.status.toLowerCase()]">
                  {{ item.status }}
                </span>
              </td>
              <td>
                <select 
                  :value="item.status" 
                  @change="(e) => updateStatus(item.id, e.target.value)"
                  class="action-select"
                  :disabled="item.status === 'APPROVED'"
                >
                  <option value="PENDING" disabled>待處理</option>
                  <option value="APPROVED">准假 Approve</option>
                  <option value="REJECTED">駁回 Reject</option>
                </select>
              </td>
              <td>
                <button @click="removeData(item.id)" class="btn-delete-minimal">
                  <i class="fas fa-trash-alt"></i>
                </button>
              </td>
            </tr>
            <tr v-if="filteredData.length === 0">
              <td colspan="6" class="no-data-minimal">查無符合條件的請假紀錄</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import axios from 'axios';
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';

const router = useRouter();

// 設定 API 基礎路徑
const API_BASE = 'http://localhost:8080/api/leaves';

const mockData = ref([]); // 這裡將存放從後端抓回來的 LeaveResponseDTO 列表
const filterType = ref('member');
const filterValue = ref('all');
const userStore = useUserStore();
const isLoading = ref(false);

// --- 1. 初始化讀取：取得所有請假單 ---
onMounted(async () => {
  await fetchAllLeaves();
});

const fetchAllLeaves = async () => {
  isLoading.value = true;
  try {
    const response = await axios.get(API_BASE);
    mockData.value = response.data;
  } catch (error) {
    console.error("獲取資料失敗", error);
    alert("無法讀取審核資料");
  } finally {
    isLoading.value = false;
  }
};

// --- 2. 篩選邏輯 ---
const currentOptions = computed(() => {
  return [...new Set(mockData.value.map(item => 
    filterType.value === 'member' ? item.userId : item.activityId
  ))];
});

const filteredData = computed(() => {
  if (filterValue.value === 'all') return mockData.value;
  return mockData.value.filter(item => 
    (filterType.value === 'member' ? item.userId : item.activityId) === filterValue.value
  );
});

// --- 3. 審核操作：對應你的 @PutMapping("/review/{id}") ---
const updateStatus = async (id, newStatus) => {
  try {
    // 根據你的後端定義，status 是作為 RequestParam 傳遞
    // URL 格式為: /api/leaves/review/{id}?status=APPROVED&note=Checked...
    await axios.put(`${API_BASE}/review/${id}`, null, {
      params: {
        status: newStatus,
        reviewerId: 'm0010', // 你可以根據登入狀況修改
        //reviewerId: userStore.userId || 'admin'
        note: '管理員審核通過'
      }
    });

    // 重新抓取資料或本地更新
    const item = mockData.value.find(i => i.id === id);
    if (item) item.status = newStatus;
    
    alert(`單號 #${id} 已更新為 ${newStatus}`);
  } catch (error) {
    console.error("更新失敗", error);
    alert('狀態更新失敗，請檢查後端連線');
  }
};

// --- 4. 刪除紀錄：對應你的 @DeleteMapping("/{id}") ---
const removeData = async (id) => {
  if(confirm('確定要永久刪除此紀錄嗎？')) {
    try {
      await axios.delete(`${API_BASE}/${id}`);
      
      // 後端刪除成功 (204 No Content) 後更新 UI
      mockData.value = mockData.value.filter(item => item.id !== id);
    } catch (error) {
      console.error("刪除失敗", error);
      alert('刪除失敗');
    }
  }
};

const pendingCount = computed(() => 
  mockData.value.filter(i => i.status === 'PENDING').length
);

const resetFilter = () => {
  filterType.value = 'member';
  filterValue.value = 'all';
};
const goBack = () => {
  router.push('/dashboard');
};
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;600&display=swap');

.outer-wrapper {
  font-family: 'Inter', sans-serif;
  background-color: #ffffff;
  min-height: 100vh;
  padding: 40px 20px;
  color: #1a1a1a;
  display: flex;
  justify-content: center;
}

.container { width: 100%; max-width: 1100px; }

/* 頂部導航 */
.btn-back-minimal {
  background: none; border: none; color: #666; font-size: 14px;
  cursor: pointer; display: flex; align-items: center; gap: 8px; margin-bottom: 24px;
}

/* 標題與統計 */
.header-section {
  display: flex; justify-content: space-between; align-items: flex-end;
  margin-bottom: 40px; border-bottom: 1px solid #eee; padding-bottom: 20px;
}

h1 { font-size: 28px; font-weight: 600; margin: 0; letter-spacing: -1px; }

.admin-badge {
  font-size: 12px; background: #1a1a1a; color: white;
  padding: 2px 8px; border-radius: 4px; margin-top: 8px; display: inline-block;
}

.stats-minimal { display: flex; gap: 24px; align-items: center; }
.stat-item { display: flex; flex-direction: column; align-items: flex-end; }
.stat-item .label { font-size: 12px; color: #999; text-transform: uppercase; }
.stat-item .value { font-size: 20px; font-weight: 600; }
.stat-item.highlight .value { color: #ff5252; }
.stat-divider { width: 1px; height: 30px; background: #eee; }

/* 篩選面板 */
.filter-panel { margin-bottom: 32px; }
.filter-group { display: flex; align-items: center; gap: 16px; flex-wrap: wrap; }

.toggle-group { background: #f5f5f5; padding: 4px; border-radius: 6px; display: flex; }
.toggle-btn {
  border: none; background: none; padding: 6px 16px; font-size: 13px;
  cursor: pointer; border-radius: 4px; transition: 0.2s;
}
.toggle-btn.active { background: white; font-weight: 600; box-shadow: 0 2px 4px rgba(0,0,0,0.05); }

.minimal-select, .action-select {
  padding: 8px 12px; border: 1px solid #ddd; border-radius: 4px;
  background: white; font-size: 13px; cursor: pointer;
}

.btn-reset {
  background: none; border: 1px solid #eee; color: #999;
  padding: 8px 16px; border-radius: 4px; cursor: pointer; font-size: 13px;
}

/* 表格樣式 */
table { width: 100%; border-collapse: collapse; }
thead th {
  text-align: left; padding: 12px 16px; font-size: 12px;
  color: #999; text-transform: uppercase; border-bottom: 2px solid #1a1a1a;
}
tbody tr { border-bottom: 1px solid #eee; transition: 0.2s; }
tbody tr:hover { background: #fafafa; }
td { padding: 16px; font-size: 14px; }

.info-cell { display: flex; flex-direction: column; }
.user-id { font-weight: 600; }
.activity-id { font-size: 12px; color: #999; }

.status-badge {
  font-size: 11px; font-weight: 700; padding: 4px 8px; border-radius: 3px; text-transform: uppercase;
}
.status-badge.pending { background: #fff3e0; color: #ef6c00; }
.status-badge.approved { background: #e8f5e9; color: #2e7d32; }
.status-badge.rejected { background: #ffebee; color: #c62828; }

.action-select { border-left: 3px solid #1a1a1a; width: 130px; }

.btn-delete-minimal {
  background: none; border: none; color: #ccc; cursor: pointer; transition: 0.2s;
}
.btn-delete-minimal:hover { color: #ff5252; }

.no-data-minimal { text-align: center; padding: 60px; color: #ccc; }
</style>