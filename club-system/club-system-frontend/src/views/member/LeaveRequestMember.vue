<template>
  <div class="outer-wrapper">
    <nav class="navbar" :class="{ 'navbar-hidden': navHidden }">
      <div class="nav-inner">
        <router-link to="/dashboard" class="nav-logo">CLUB SYSTEM</router-link>
        <span class="nav-crumb">MEMBER / <span class="nav-crumb-active">我的請假紀錄</span></span>
      </div>
    </nav>

    <div class="page-header">
      <div class="header-left">
        <div class="eyebrow">
          <span class="eyebrow-line"></span>
          <span class="eyebrow-text">PERSONAL LEAVE LOG</span>
        </div>
        <h1 class="page-title">請假<span class="title-accent">紀錄</span></h1>
      </div>

      <div class="header-right">
        <select v-model="filterValue" class="type-select">
          <option value="all">ALL STATUS (全部狀態)</option>
          <option value="PENDING">PENDING (待處理)</option>
          <option value="APPROVED">APPROVED (已核准)</option>
          <option value="REJECTED">REJECTED (已駁回)</option>
        </select>
        <button @click="resetFilter" class="view-btn reset-btn">RESET</button>
      </div>
    </div>

    <div class="kpi-row">
      <div class="kpi-card">
        <span class="kpi-label">TOTAL REQUESTS</span>
        <span class="kpi-num">{{ mockData.length }}</span>
        <span class="kpi-sub">總申請次數</span>
      </div>
      <div class="kpi-card accent">
        <span class="kpi-label">WAITING</span>
        <span class="kpi-num">{{ pendingCount }}</span>
        <span class="kpi-sub">尚未審核數</span>
      </div>
    </div>

    <div class="table-section">
      <table v-if="filteredData.length > 0" class="reg-table">
        <thead>
          <tr>
            <th class="th-num">#</th>
            <th class="col-info">對應活動</th>
            <th class="col-type">類型</th>
            <th class="col-reason">原因</th>
            <th class="th-center">狀態</th>
            
          </tr>
        </thead>
        <tbody>
          <tr v-for="(item, index) in filteredData" :key="item.id" class="table-row">
            <td class="td-num">{{ String(index + 1).padStart(2, '0') }}</td>
            <td>
              <span class="activity-title"> {{ activityTitles[item.activityId] || '載入中...' }}</span>
              
            </td>
            <td class="mono">{{ item.leaveType }}</td>
            <td class="td-reason">
              <p class="reason-text" :title="item.reason">{{ item.reason }}</p>
            </td>
            <td class="td-center">
              <span class="status-tag" :class="'s-' + item.status.toLowerCase()">
                {{ item.status }}
              </span>
            </td>
            
          </tr>
        </tbody>
      </table>

      <div v-else class="state-wrap">
        <div class="empty-big">EMPTY</div>
        <p class="empty-desc mono">查無申請紀錄</p>
      </div>

      <div v-if="filteredData.length > 0" class="table-footer">
        <span class="mono">TOTAL {{ filteredData.length }} RECORDS</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, onUnmounted } from 'vue'; // 記得導入 computed 和 onUnmounted
import axios from 'axios';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';

const router = useRouter();
const userStore = useUserStore();

// --- 響應式基礎資料 ---
const mockData = ref([]);
const userNames = ref({});
const activityTitles = ref({});
const filterValue = ref('all'); // 篩選值
const navHidden = ref(false);   // 導航欄顯示狀態
let lastScrollY = 0;

// --- 1. 計算屬性：篩選後的資料 (解決報錯關鍵) ---
const filteredData = computed(() => {
  const data = mockData.value || [];
  if (filterValue.value === 'all') return data;
  return data.filter(item => item.status === filterValue.value);
});

// --- 2. 計算屬性：待處理數量 ---
const pendingCount = computed(() => {
  return (mockData.value || []).filter(i => i.status === 'PENDING').length;
});

// --- 導航欄滾動邏輯 ---
const handleScroll = () => {
  navHidden.value = window.scrollY > lastScrollY && window.scrollY > 80;
  lastScrollY = window.scrollY;
};

// --- 重置篩選 ---
const resetFilter = () => {
  filterValue.value = 'all';
};

// --- 抓取細節資料 ---
const fetchDetails = async (data) => {
  const authConfig = { headers: { Authorization: `Bearer ${userStore.token}` } };
  const userIds = [...new Set(data.map(item => item.userId))];
  const activityIds = [...new Set(data.map(item => item.activityId))];

  userIds.forEach(async (id) => {
    if (!userNames.value[id]) {
      try {
        const res = await axios.get(`http://localhost:8080/api/users/${id}`, authConfig);
        userNames.value[id] = res.data.name;
      } catch (e) { userNames.value[id] = id; }
    }
  });

  activityIds.forEach(async (id) => {
    if (!activityTitles.value[id]) {
      try {
        const res = await axios.get(`http://localhost:8080/api/activities/${id}`, authConfig);
        activityTitles.value[id] = res.data.title;
      } catch (e) { activityTitles.value[id] = `活動 #${id}`; }
    }
  });
};

// --- 撤回申請 ---
const removeData = async (id) => {
  if(!confirm('確定要撤回此申請嗎？')) return;
  try {
    await axios.delete(`http://localhost:8080/api/leaves/${id}`, {
      headers: { Authorization: `Bearer ${userStore.token}` }
    });
    mockData.value = mockData.value.filter(item => item.id !== id);
  } catch (e) {
    alert('撤回失敗');
  }
};

// --- 生命週期 ---
onMounted(async () => {
  window.addEventListener('scroll', handleScroll);
  
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
    if (mockData.value.length > 0) {
      await fetchDetails(mockData.value);
    }
  } catch (error) {
    console.error("讀取失敗:", error);
    if (error.response?.status === 401) {
      router.push('/login');
    }
  }
});

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll);
});
</script>

<style scoped>
/* ── 1. 外部字體導入 ── */
@import url('https://fonts.googleapis.com/css2?family=Bebas+Neue&family=Space+Mono:wght@400;700&family=Noto+Sans+TC:wght@300;400;500;700&family=JetBrains+Mono:wght@600&display=swap');

/* ── 2. 全域基礎設定 ── */
* { 
  box-sizing: border-box; 
}

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

.header-right { display: flex; align-items: center; gap: 1rem; }

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
  table-layout: fixed; 
}

.reg-table thead tr { 
  border-bottom: 2px solid #0a0a0a; 
}

.reg-table th {
  padding: 1.25rem 1rem;
  font-family: 'Space Mono', monospace;
  font-size: 0.8rem;
  color: #aaa;
  text-align: left;
}

.reg-table td {
  padding: 1.25rem 1rem;
  vertical-align: middle;
  color: #0a0a0a;
}

/* 欄位寬度分配 */
.th-num { width: 60px; }
.col-info { width: 35%; }    /* 稍微加寬 */
.col-type { width: 10%; }
.col-reason { width: 40%; }  /* 讓原因佔據更多空間 */
.col-status { width: 15%; }

.th-center, .td-center { text-align: center !important; }

.table-row { border-bottom: 1px solid #f0f0f0; transition: background 0.2s; }
.table-row:hover { background: #fafafa; }

/* 內容文字細項 */
.td-num { font-family: 'Space Mono', monospace; color: #ccc; }
.activity-title { display: block; font-weight: 700; font-size: 1rem; }
.activity-type-tag { font-family: 'Space Mono', monospace; font-size: 0.6rem; color: #aaa; }
.mono { font-family: 'Space Mono', monospace; font-size: 0.8rem; }

.reason-text {
  margin: 0;
  font-size: 0.85rem;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 狀態標籤 */
.status-tag {
  font-family: 'Space Mono', monospace; font-size: 0.75rem;
  padding: 0.3rem 0.75rem; font-weight: 700; display: inline-block;
}
.s-pending { background: #fffbeb; color: #d97706; border: 1px solid #fef3c7; }
.s-approved { background: #f0fdf4; color: #15803d; border: 1px solid #bbf7d0; }
.s-rejected { background: #fff1f2; color: #be123c; border: 1px solid #fecdd3; }

/* 操作按鈕 */
.view-btn {
  font-family: 'Space Mono', monospace; font-size: 0.75rem;
  background: #0a0a0a !important; 
  color: #fff !important; 
  border: none;
  padding: 0.6rem 1.2rem; cursor: pointer; font-weight: 700;
  transition: all 0.2s ease;
}

.view-btn:hover { background: #ff2d6b !important; }

.view-btn.reset-btn { 
  background: transparent !important; 
  color: #0a0a0a !important; 
  border: 1px solid #0a0a0a !important; 
}

.view-btn.reset-btn:hover {
  background: #0a0a0a !important;
  color: #fff !important;
}

.mono-locked {
  font-family: 'Space Mono', monospace;
  font-size: 0.6rem;
  color: #eee;
  text-transform: uppercase;
}

/* ── 7. 空狀態與頁尾 ── */
.state-wrap { text-align: center; padding: 5rem 2rem; }
.empty-big {
  font-family: 'Bebas Neue', sans-serif; font-size: 8rem;
  color: transparent; -webkit-text-stroke: 1px #e0e0e0; line-height: 1;
}

.table-footer { 
  margin-top: 2rem; 
  text-align: right; 
  padding-top: 1rem; 
  border-top: 1px solid #f0f0f0; 
}

/* ── 8. RWD 回應式設計 ── */
@media (max-width: 1024px) {
  .page-header { flex-direction: column; align-items: flex-start; gap: 2rem; }
  .header-right { width: 100%; flex-wrap: wrap; }
}

@media (max-width: 768px) {
  /* 手機版隱藏非必要欄位 */
  .col-reason, .td-reason { display: none; }
}
</style>