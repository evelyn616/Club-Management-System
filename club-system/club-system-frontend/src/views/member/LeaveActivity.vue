<template>
  <div class="my-registrations">
    <nav class="navbar" :class="{ 'navbar-hidden': navHidden }">
      <div class="nav-container">
        <router-link to="/dashboard" class="nav-logo">CLUB SYSTEM</router-link>
        <div class="nav-right">
          <span class="nav-username">{{ userStore.userName }}</span>
          <router-link to="/profile" class="nav-link">個人資料</router-link>
          <button @click="handleLogout" class="nav-logout">登出</button>
        </div>
      </div>
    </nav>

    <div class="page-header">
      <div class="header-left">
        <div class="header-label">
          <span class="label-line"></span>
          <span class="label-text">LEAVE MANAGEMENT</span>
          <span class="label-num">2026</span>
        </div>
        <h1 class="page-title">
          <span class="title-line title-line-1">我的</span>
          <span class="title-line title-line-2"><span class="title-accent">可請假列表</span></span>
        </h1>
        <p class="page-subtitle">
          <span class="subtitle-inner">僅列出尚未開始或進行中、符合請假資格的報名</span>
        </p>
      </div>
      <div class="header-right">
        <router-link to="/leave-history" class="history-link">
          LEAVE LOG <span class="link-arrow">→</span>
        </router-link>
        <div class="header-stats" v-if="!loading && registeredList.length > 0">
          <div class="stat-block">
            <span class="stat-number">{{ registeredList.length }}</span>
            <span class="stat-label">AVAILABLE</span>
          </div>
        </div>
      </div>
    </div>

    <div class="filter-tabs" v-if="!loading && registeredList.length > 0">
      <button 
        class="tab-btn active"
      >
        ALL ACTIVE
        <span class="tab-count">{{ registeredList.length }}</span>
      </button>
    </div>

    <div v-if="loading" class="state-container">
      <div class="loading-wrapper">
        <div class="loading-bars">
          <span v-for="i in 5" :key="i" :style="{ animationDelay: (i * 0.1) + 's' }"></span>
        </div>
        <p class="loading-text">SCANNING ACTIVITIES...</p>
      </div>
    </div>

    <div v-else-if="registeredList.length === 0" class="state-container">
      <div class="empty-card">
        <div class="empty-big-text">EMPTY</div>
        <p class="empty-desc">目前沒有符合請假條件的活動</p>
        <router-link to="/dashboard" class="cta-btn">
          回到儀表板 <span>→</span>
        </router-link>
      </div>
    </div>

    <div v-else class="registration-grid">
      <div class="cards-container">
        <div
          v-for="(reg, index) in registeredList"
          :key="reg.id"
          class="registration-card status-registered"
        >
          <div class="card-index">{{ String(index + 1).padStart(2, '0') }}</div>

          <div class="card-header">
            <div class="status-badge status-registered">
              <span class="status-dot"></span>
              可請假 (已報名)
            </div>
            <div class="activity-type-badge">
              {{ reg.activityType || '一般活動' }}
            </div>
          </div>

          <div class="card-title-section">
            <h3 class="activity-title">{{ reg.activityTitle || '活動 #' + reg.activityId }}</h3>
          </div>

          <div class="card-body">
            <div class="info-row-horizontal">
              <div class="info-item" v-if="reg.activityLocation">
                <span class="info-label">LOCATION</span>
                <span class="info-value">{{ reg.activityLocation }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">ACTIVITY DATE</span>
                <span class="info-value">
                  {{ formatDate(reg.activityStartTime) }}
                </span>
              </div>
            </div>

            <div class="card-divider"></div>

            <div class="payment-row">
              <div class="info-item">
                <span class="info-label">REG ID</span>
                <span class="info-value">#{{ reg.id }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">REG DATE</span>
                <span class="info-value">{{ formatDate(reg.createdAt) }}</span>
              </div>
            </div>
          </div>

          <div class="card-footer">
            <span class="reg-date-hint">VALID UNTIL START</span>
            <button @click="goToLeave(reg)" class="cancel-btn">
              我要請假 <span class="btn-arrow">→</span>
            </button>
          </div>
        </div>
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
  if (!userStore.userId) return;

  const token = userStore.token;
  loading.value = true;
  
  try {
    // 1. 先抓取報名列表
    const response = await axios.get(`http://localhost:8080/api/registrations/my?userId=${userStore.userId}`, {
      headers: { 'Authorization': `Bearer ${token}` }
    });
    
    const rawList = response.data;

    // 2. 準備補抓活動名稱 (假設獲取活動詳情的 API 是 /api/activities/{id})
    // 使用 Map 當作簡單快取，避免同一個活動 ID 重複請求
    const activityMap = new Map();

    const listWithTitles = await Promise.all(rawList.map(async (reg) => {
      // 如果這個活動名稱之前抓過了，直接用，不用再發請求
      if (activityMap.has(reg.activityId)) {
        return { ...reg, activityTitle: activityMap.get(reg.activityId) };
      }

      try {
        const actRes = await axios.get(`http://localhost:8080/api/activities/${reg.activityId}`, {
          headers: { 'Authorization': `Bearer ${token}` }
        });
        const title = actRes.data.title; // 請確認後端欄位名稱是 title 還是 name
        activityMap.set(reg.activityId, title);
        return { ...reg, activityTitle: title };
      } catch (e) {
        console.warn(`無法取得活動 #${reg.activityId} 的名稱`);
        return { ...reg, activityTitle: `活動 #${reg.activityId}` }; // 失敗時的備案
      }
    }));

    registeredList.value = listWithTitles;
    
  } catch (error) {
    console.error("讀取報名資料失敗:", error);
    // ... 錯誤處理 ...
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
/* 載入字體 */
@import url('https://fonts.googleapis.com/css2?family=Bebas+Neue&family=Noto+Sans+TC:wght@300;400;500;700&family=Space+Mono:wght@400;700&display=swap');

.my-registrations {
  min-height: 100vh;
  background: #ffffff;
  font-family: 'Noto Sans TC', sans-serif;
  color: #0a0a0a;
  max-width: 1400px;
  margin: 0 auto;
  padding: 6rem 2rem 5rem;
}

/* ===== 1. Navbar (磨砂玻璃特效) ===== */
.navbar {
  position: fixed;
  padding: 1rem 0;
  top: 0; left: 0; right: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
  transition: transform 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}
.navbar-hidden { transform: translateY(-100%); }
.nav-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.nav-logo {
  font-family: 'Space Mono', monospace;
  font-size: 1.25rem;
  font-weight: 700;
  letter-spacing: 0.18em;
  color: #0a0a0a;
  text-decoration: none;
}
.nav-logo:hover { color: #ff2d6b; }
.nav-right { display: flex; align-items: center; gap: 1.5rem; font-family: 'Space Mono', monospace; font-size: 0.85rem; }
.nav-username { color: #aaa; }
.nav-logout {
  background: transparent; border: 1px solid #e0e0e0; padding: 0.5rem 1.25rem;
  cursor: pointer; transition: all 0.2s; border-radius: 6px;
}
.nav-logout:hover { border-color: #ff2d6b; color: #ff2d6b; }

/* ===== 2. Page Header & Animations ===== */
.page-header {
  display: flex; justify-content: space-between; align-items: flex-end;
  margin-bottom: 2.5rem; padding-bottom: 2rem;
  border-bottom: 2px solid #0a0a0a; flex-wrap: wrap; gap: 2rem;
}

/* --- 1. 定義動畫 Keyframes (放在 style 最後面即可) --- */
@keyframes slideUpFade {
  from { opacity: 0; transform: translateY(24px); }
  to   { opacity: 1; transform: translateY(0); }
}

@keyframes expandLine {
  from { width: 0; }
  to   { width: 2rem; }
}

@keyframes revealText {
  from { clip-path: inset(0 100% 0 0); }
  to   { clip-path: inset(0 0% 0 0); }
}

.header-label { display: flex; align-items: center; gap: 0.6rem; margin-bottom: 1rem; }
.label-line { width: 2rem; height: 1px; background: #ff2d6b; }
.label-text { font-family: 'Space Mono', monospace; font-size: 0.65rem; color: #ff2d6b; letter-spacing: 0.2em; }

.page-title {
  font-family: 'Bebas Neue', sans-serif; font-size: 5rem;
  line-height: 0.92; margin: 0; letter-spacing: 0.02em;
  display: flex; flex-direction: column;
}
.title-accent { color: #ff2d6b; }
.page-subtitle { font-size: 0.85rem; color: #999; margin-top: 1rem; }

.header-right { display: flex; flex-direction: column; align-items: flex-end; gap: 1rem; }
.history-link {
  font-family: 'Space Mono', monospace; font-size: 0.75rem; color: #0a0a0a;
  text-decoration: none; border-bottom: 1px solid #0a0a0a; padding-bottom: 2px;
}
.header-stats {
  display: flex; align-items: center; gap: 1.25rem;
  background: #f5f5f5; padding: 0.75rem 1.25rem; border-radius: 4px;
}
.stat-block { display: flex; flex-direction: column; align-items: center; }
.stat-number { font-family: 'Bebas Neue', sans-serif; font-size: 2rem; color: #0a0a0a; line-height: 1; }
.stat-label { font-family: 'Space Mono', monospace; font-size: 0.55rem; color: #aaa; letter-spacing: 0.1em; }

/* ===== 3. Filter Tabs ===== */
.filter-tabs { display: flex; margin-bottom: 2rem; border: 1px solid #0a0a0a; width: fit-content; }
.tab-btn {
  background: transparent; border: none; border-right: 1px solid #0a0a0a;
  padding: 0.6rem 1.5rem; font-family: 'Space Mono', monospace; font-size: 0.8rem;
  cursor: pointer; transition: all 0.2s; color: #aaa;
}
.tab-btn:last-child { border-right: none; }
.tab-btn.active { background: #ff2d6b; color: #fff; border-color: #ff2d6b; }
.tab-count { font-size: 0.65rem; margin-left: 0.5rem; opacity: 0.7; }

/* ===== 4. Card Grid System ===== */
.registration-grid { margin-top: 2rem; }
.cards-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 1px; background: #eeeeee; /* 格線效果 */
  border: 1px solid #eeeeee;
}

.registration-card {
  background: #ffffff; padding: 1.8rem;
  position: relative; transition: all 0.25s ease;
}
.registration-card:hover { background: #fafafa; transform: scale(1.005); z-index: 1; }

/* 左側色條裝飾 */
.registration-card::before {
  content: ''; position: absolute; left: 0; top: 0; bottom: 0; width: 4px; background: #ff2d6b;
}

.card-index { font-family: 'Space Mono', monospace; font-size: 0.65rem; color: #ddd; margin-bottom: 1rem; }

.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; }
.status-badge {
  display: inline-flex; align-items: center; gap: 0.4rem;
  font-family: 'Space Mono', monospace; font-size: 0.65rem;
  padding: 0.25rem 0.75rem; background: rgba(255, 45, 107, 0.08);
  color: #ff2d6b; border-radius: 2px;
}
.status-dot { width: 5px; height: 5px; background: #ff2d6b; border-radius: 50%; }

.activity-title {
  font-family: 'Noto Sans TC', sans-serif; font-size: 1.2rem;
  font-weight: 700; color: #0a0a0a; margin-bottom: 1.5rem; line-height: 1.4;
}

.card-body { display: flex; flex-direction: column; gap: 1rem; }
.info-row-horizontal { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }
.info-item { display: flex; flex-direction: column; gap: 0.3rem; }
.info-label { font-family: 'Space Mono', monospace; font-size: 0.55rem; color: #bbb; letter-spacing: 0.1em; }
.info-value { font-size: 0.85rem; font-weight: 500; color: #333; }

.card-divider { height: 1px; background: #f0f0f0; margin: 0.5rem 0; }

/* ===== 5. Footer & Buttons ===== */
.card-footer {
  display: flex; justify-content: space-between; align-items: center;
  margin-top: 1.5rem; padding-top: 1.2rem; border-top: 1px solid #f0f0f0;
}
.reg-date-hint { font-family: 'Space Mono', monospace; font-size: 0.6rem; color: #ccc; }

.cancel-btn {
  background: transparent; border: 1px solid #e0e0e0;
  color: #555; padding: 0.5rem 1.2rem;
  font-family: 'Space Mono', monospace; font-size: 0.7rem;
  cursor: pointer; transition: all 0.2s; border-radius: 2px;
}
.cancel-btn:hover {
  border-color: #0a0a0a; color: #0a0a0a; background: #f5f5f5;
}

/* ===== 6. States (Loading/Empty) ===== */
.state-container { display: flex; justify-content: center; align-items: center; min-height: 300px; }
.empty-big-text {
  font-family: 'Bebas Neue', sans-serif; font-size: 8rem; color: transparent;
  -webkit-text-stroke: 1px #eee; line-height: 1; text-align: center;
}

/* RWD */
@media (max-width: 768px) {
  .page-title { font-size: 3.5rem; }
  .info-row-horizontal { grid-template-columns: 1fr; }
  .my-registrations { padding: 5rem 1rem; }
}

</style>