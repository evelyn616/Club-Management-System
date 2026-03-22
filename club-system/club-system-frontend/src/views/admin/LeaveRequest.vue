<template>
  <div class="outer-wrapper">
    <nav class="navbar" :class="{ 'navbar-hidden': navHidden }">
      <div class="nav-inner">
        <router-link to="/admin/activity-management-container" class="nav-logo">CLUB SYSTEM</router-link>
        <span class="nav-crumb">ADMIN / <span class="nav-crumb-active">請假總覽</span></span>
      </div>
    </nav>

    <div class="page-header">
      <div class="header-left">
        <div class="eyebrow">
          <span class="eyebrow-line"></span>
          <span class="eyebrow-text">LEAVE REQUEST OVERVIEW</span>
        </div>
        <h1 class="page-title">請假<span class="title-accent">總覽</span></h1>
      </div>

      <div class="header-right">
        <div class="mode-toggle">
          <button 
            :class="['mode-btn', { active: filterType === 'member' }]"
            @click="filterType = 'member'; filterValue = 'all'"
          >BY MEMBER</button>
          <button 
            :class="['mode-btn', { active: filterType === 'activity' }]"
            @click="filterType = 'activity'; filterValue = 'all'"
          >BY ACTIVITY</button>
        </div>

        <select v-model="filterValue" class="type-select">
          <option value="all">ALL SOURCES</option>
          <option v-for="opt in currentOptions" :key="opt" :value="opt">
            {{ filterType === 'member' ? 'USER' : 'ACT' }}: {{ opt }}
          </option>
        </select>

        <button @click="resetFilter" class="view-btn reset-btn">RESET</button>
      </div>
    </div>

    <div class="kpi-row">
      <div class="kpi-card">
        <span class="kpi-label">TOTAL LEAVE REQUESTS</span>
        <span class="kpi-num">{{ mockData.length }}</span>
        <span class="kpi-sub">總申請人次</span>
      </div>
      <div class="kpi-card accent">
        <span class="kpi-label">PENDING</span>
        <span class="kpi-num">{{ pendingCount }}</span>
        <span class="kpi-sub">待處理數</span>
      </div>
    </div>

    <div class="table-section">
      <table v-if="filteredData.length > 0" class="reg-table">
        <thead>
          <tr>
            <th class="th-num">#</th>
            <th>會員 / 活動</th>
            <th>請假類型</th>
            <th class="th-center">目前狀態</th>
            <th class="th-center">審核操作</th>
            <th class="th-center">管理</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="(item, index) in filteredData"
            :key="item.id"
            class="table-row"
            :style="{ animationDelay: (index * 0.04) + 's' }"
          >
            <td class="td-num">{{ String(index + 1).padStart(2, '0') }}</td>
            <td class="td-title">
              <span class="activity-title">👤 {{ userNames[item.userId] || '載入中...' }}</span>
              <span class="activity-type-tag">🎯 {{ activityTitles[item.activityId] || '載入中...' }}</span>
            </td>
            <td class="td-time">
              <span class="mono">{{ item.leaveType }}</span>
            </td>
            <td class="td-center">
              <span class="status-tag" :class="'s-' + item.status.toLowerCase()">
                {{ item.status }}
              </span>
            </td>
            <td class="td-center">
              <div class="action-wrapper">
                <select 
                  :value="item.status" 
                  @change="(e) => updateStatus(item.id, e.target.value)"
                  class="type-select small-select"
                  :disabled="item.status === 'APPROVED'"
                >
                  <option value="PENDING">待處理 PENDING</option>
                  <option value="APPROVED">准假 APPROVE</option>
                  <option value="REJECTED">駁回 REJECT</option>
                </select>
              </div>
            </td>
            <td class="td-center">
              <button @click="removeData(item.id)" class="view-btn delete-mode">
                DELETE ✕
              </button>
            </td>
          </tr>
        </tbody>
      </table>

      <div v-else class="state-wrap">
        <div class="empty-big">EMPTY</div>
        <p class="empty-desc mono">查無符合條件的請假紀錄</p>
      </div>

      <div v-if="filteredData.length > 0" class="table-footer">
        <span class="mono">SHOWING {{ filteredData.length }} LEAVE REQUESTS</span>
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
const userNames = ref({});    // 格式：{ "u001": "張小明" }
const activityTitles = ref({}); // 格式：{ "101": "進階街舞課程" }

// --- 1. 初始化讀取：取得所有請假單 ---
onMounted(async () => {
  await fetchAllLeaves();
});

// --- 批次獲取名稱的函式 ---
const fetchDetails = async (data) => {
  const token = userStore.token;
  const authConfig = { headers: { Authorization: `Bearer ${token}` } };

  // 取得所有不重複的 ID
  const userIds = [...new Set(data.map(item => item.userId))];
  const activityIds = [...new Set(data.map(item => item.activityId))];

  // 1. 補抓使用者名稱 (假設 API: /api/users/{id})
  userIds.forEach(async (id) => {
    if (!userNames.value[id]) {
      try {
        const res = await axios.get(`http://localhost:8080/api/users/${id}`, authConfig);
        userNames.value[id] = res.data.name; // 確認後端欄位是 name 還是 username
      } catch (e) {
        userNames.value[id] = `用戶 ${id}`; // 失敗備案
      }
    }
  });

  // 2. 補抓活動標題 (假設 API: /api/activities/{id})
  activityIds.forEach(async (id) => {
    if (!activityTitles.value[id]) {
      try {
        const res = await axios.get(`http://localhost:8080/api/activities/${id}`, authConfig);
        activityTitles.value[id] = res.data.title;
      } catch (e) {
        activityTitles.value[id] = `活動 #${id}`; // 失敗備案
      }
    }
  });
};


const getAuthConfig = () => ({
  headers: {
    'Authorization': `Bearer ${userStore.token}`
  }
});


const fetchAllLeaves = async () => {
  isLoading.value = true;
  try {
    const response = await axios.get(API_BASE, {
      headers: { Authorization: `Bearer ${userStore.token}` }
    });
    mockData.value = response.data;
    
    // 關鍵步驟：抓完假單後立刻去抓詳細名稱
    await fetchDetails(mockData.value);
  } catch (error) {
    console.error("載入失敗", error);
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


const updateStatus = async (id, newStatus) => {
  try {
    // put 請求的第二個參數是 body (這裡傳 null)，第三個才是 config (包含 params 和 headers)
    await axios.put(`${API_BASE}/review/${id}`, null, {
      ...getAuthConfig(),
      params: {
        status: newStatus,
        reviewerId: userStore.userId || 'admin',
        note: '管理員審核處理'
      }
    });

    const item = mockData.value.find(i => i.id === id);
    if (item) item.status = newStatus;
    alert(`單號 #${id} 狀態已更新`);
  } catch (error) {
    console.error("更新失敗", error);
    alert('操作失敗，請確認管理員權限');
  }
};

const removeData = async (id) => {
  if(confirm('確定要永久刪除此紀錄嗎？')) {
    try {
      // 確保路徑沒有多餘的斜線
      const url = `${API_BASE.replace(/\/$/, '')}/${id}`;
      
      await axios.delete(url, getAuthConfig());
      
      mockData.value = mockData.value.filter(item => item.id !== id);
      alert('刪除成功');
    } catch (error) {
      console.error("刪除失敗詳情:", error.response);
      
      if (error.response?.status === 404) {
        alert(`刪除失敗 (404)：找不到該單號 (#${id})。請確認資料是否已被刪除或後端路徑正確。`);
      } else if (error.response?.status === 403) {
        alert('權限不足，無法刪除');
      } else {
        alert(`刪除失敗：${error.response?.data || '伺服器錯誤'}`);
      }
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
  router.push('/admin/dashboard');
};
</script>

<style scoped>
/* 1. 外部字體導入 */
@import url('https://fonts.googleapis.com/css2?family=Bebas+Neue&family=Space+Mono:wght@400;700&family=Noto+Sans+TC:wght@300;400;500;700&display=swap');

/* 2. 全域基礎設定 */
* { box-sizing: border-box; }

.outer-wrapper {
  min-height: 100vh;
  background: #ffffff;
  font-family: 'Noto Sans TC', sans-serif;
  color: #0a0a0a;
  padding-top: 80px; /* 為固定導航留出空間 */
}

/* ── 3. Navbar 導航欄 ── */
.navbar {
  position: fixed; top: 0; left: 0; right: 0; z-index: 100;
  background: rgba(255,255,255,0.9);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(0,0,0,0.08);
  transition: transform 0.3s cubic-bezier(0.4,0,0.2,1);
}
.navbar-hidden { transform: translateY(-100%); }
.nav-inner {
  max-width: 1400px; margin: 0 auto;
  padding: 1rem 3rem;
  display: flex; justify-content: space-between; align-items: center;
}
.nav-logo {
  font-family: 'Space Mono', monospace;
  font-size: 1rem; letter-spacing: 0.18em;
  font-weight: 700; color: #0a0a0a; text-decoration: none;
}
.nav-logo:hover { color: #ff2d6b; }
.nav-crumb {
  font-family: 'Space Mono', monospace;
  font-size: 0.65rem; letter-spacing: 0.15em; color: #aaa;
}
.nav-crumb-active { color: #ff2d6b; }

/* ── 4. Page Header 頁面標題與篩選 ── */
.page-header {
  max-width: 1400px; margin: 0 auto;
  padding: 4rem 3rem 2.5rem;
  display: flex; justify-content: space-between; align-items: flex-end;
  border-bottom: 2px solid #0a0a0a;
}
.eyebrow { display: flex; align-items: center; gap: 0.75rem; margin-bottom: 0.75rem; }
.eyebrow-line { width: 28px; height: 2px; background: #ff2d6b; }
.eyebrow-text {
  font-family: 'Space Mono', monospace;
  font-size: 0.65rem; letter-spacing: 0.2em; color: #ff2d6b;
}
.page-title {
  font-family: 'Bebas Neue', sans-serif;
  font-size: 4.5rem; line-height: 1; margin: 0;
}
.title-accent { color: #ff2d6b; }

/* 頂部右側篩選區 */
.header-right { display: flex; align-items: center; gap: 1rem; }
.header-search { position: relative; }
.search-input {
  padding: 0.65rem 1rem;
  border: 1px solid #0a0a0a;
  font-family: 'Space Mono', monospace; font-size: 0.75rem;
  width: 200px; outline: none;
}
.mode-toggle { display: flex; background: #f5f5f5; padding: 2px; border: 1px solid #0a0a0a; }
.mode-btn {
  font-family: 'Space Mono', monospace; font-size: 0.65rem;
  padding: 0.5rem 0.8rem; border: none; background: transparent;
  color: #aaa; cursor: pointer; font-weight: 700;
}
.mode-btn.active { background: #0a0a0a; color: #fff; }

.type-select {
  padding: 0.65rem 1rem; border: 1px solid #0a0a0a;
  font-family: 'Space Mono', monospace; font-size: 0.72rem;
  background: #fff; cursor: pointer; outline: none;
}

/* ── 5. KPI 統計數據 ── */
.kpi-row {
  max-width: 1400px; margin: 0 auto;
  padding: 0 3rem; display: grid; grid-template-columns: repeat(2, 1fr);
  border-bottom: 1px solid #e8e8e8;
}
.kpi-card {
  padding: 1.5rem 2rem; border-right: 1px solid #e8e8e8;
  display: flex; flex-direction: column; gap: 0.35rem; position: relative;
}
.kpi-card:last-child { border-right: none; }
.kpi-card::before { content: ''; position: absolute; top: 0; left: 0; right: 0; height: 3px; background: #e8e8e8; }
.kpi-card.accent::before { background: #ff2d6b; }
.kpi-label { font-family: 'Space Mono', monospace; font-size: 0.65rem; color: #aaa; }
.kpi-num { font-family: 'Bebas Neue', sans-serif; font-size: 3rem; line-height: 1; }
.kpi-sub { font-size: 0.75rem; color: #888; }

/* ── 6. Table 表格設計 ── */
.table-section { 
  max-width: 1400px; 
  margin: 0 auto; 
  padding: 2.5rem 3rem; 
}

.reg-table { 
  width: 100%; 
  border-collapse: collapse; 
  table-layout: fixed; /* 強制寬度分配，防止被內容撐開 */
}

.reg-table thead tr { 
  border-bottom: 2px solid #0a0a0a; 
}

.reg-table th, 
.reg-table td {
  padding: 1.25rem 1rem; /* 確保上下左右 padding 統一 */
  text-align: left;
  vertical-align: middle;
}

.th-center, .td-center { 
  text-align: center !important; 
}

.table-row { border-bottom: 1px solid #f0f0f0; transition: background 0.2s; }
.table-row:hover { background: #fafafa; }

.td-num { font-family: 'Space Mono', monospace; color: #ccc; }
.activity-title { display: block; font-weight: 700; font-size: 1rem; }
.activity-type-tag { font-family: 'Space Mono', monospace; font-size: 0.6rem; color: #aaa; }
.mono { font-family: 'Space Mono', monospace; font-size: 0.8rem; }

/* 狀態標籤 */
.status-tag {
  font-family: 'Space Mono', monospace; font-size: 0.75rem;
  padding: 0.3rem 0.75rem; font-weight: 700; display: inline-block;
}
.s-pending { background: #fffbeb; color: #d97706; border: 1px solid #fef3c7; }
.s-approved { background: #f0fdf4; color: #15803d; border: 1px solid #bbf7d0; }
.s-rejected { background: #fff1f2; color: #be123c; border: 1px solid #fecdd3; }

/* 操作按鈕 */
.small-select { width: 140px; border-left: 4px solid #0a0a0a; }
.view-btn {
  font-family: 'Space Mono', monospace; font-size: 0.75rem;
  background: #0a0a0a; color: #fff; border: none;
  padding: 0.6rem 1.2rem; cursor: pointer; font-weight: 700;
}
.view-btn:hover { background: #ff2d6b; }
.view-btn.reset-btn { background: transparent; color: #0a0a0a; border: 1px solid #0a0a0a; }
.view-btn.delete-mode { 
  background: #0a0a0a;  /* 改為黑色 */
  color: #ffffff;       /* 改為白色文字 */
  border: 1px solid #0a0a0a; 
  transition: all 0.2s ease;
}
.view-btn.delete-mode:hover { background: #ff2d6b; color: #fff; border-color: #ff2d6b; }

/* ── 7. 空狀態與頁尾 ── */
.state-wrap { text-align: center; padding: 5rem 2rem; }
.empty-big {
  font-family: 'Bebas Neue', sans-serif; font-size: 8rem;
  color: transparent; -webkit-text-stroke: 1px #e0e0e0; line-height: 1;
}
.table-footer { margin-top: 2rem; text-align: right; padding-top: 1rem; border-top: 1px solid #f0f0f0; }

/* ── 8. RWD 回應式設計 ── */
@media (max-width: 1024px) {
  .page-header { flex-direction: column; align-items: flex-start; gap: 2rem; }
  .header-right { width: 100%; flex-wrap: wrap; }
  .search-input { width: 100%; }
}
</style>