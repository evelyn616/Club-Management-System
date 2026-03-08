<template>
  <div class="registration-history">
    <!-- Header -->
    <div class="page-header">
      <div class="header-left">
        <button class="back-btn" @click="$router.back()">← 返回</button>
        <div class="header-text">
          <div class="header-icon">📋</div>
          <div>
            <h1 class="page-title">歷史報名紀錄</h1>
            <p class="page-subtitle">已完成或已取消的活動</p>
          </div>
        </div>
      </div>
      <div class="header-stats" v-if="!loading && registrations.length > 0">
        <div class="stat-chip">
          <span class="stat-number">{{ registrations.length }}</span>
          <span class="stat-label">總紀錄</span>
        </div>
        <div class="stat-chip green">
          <span class="stat-number">{{ attendedCount }}</span>
          <span class="stat-label">已出席</span>
        </div>
        <div class="stat-chip gray">
          <span class="stat-number">{{ absentCount }}</span>
          <span class="stat-label">未出席</span>
        </div>
      </div>
    </div>

    <!-- Filter Tabs -->
    <div class="filter-tabs" v-if="!loading && registrations.length > 0">
      <button
        v-for="tab in tabs"
        :key="tab.value"
        class="tab-btn"
        :class="{ active: activeTab === tab.value }"
        @click="activeTab = tab.value"
      >
        <span class="tab-dot" :class="tab.dotClass"></span>
        {{ tab.label }}
        <span class="tab-count">{{ getTabCount(tab.value) }}</span>
      </button>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="state-container">
      <div class="loading-wrapper">
        <div class="spinner"></div>
        <p class="loading-text">載入歷史紀錄中...</p>
      </div>
    </div>

    <!-- Error -->
    <div v-else-if="error" class="state-container">
      <div class="error-card">
        <div class="error-icon">⚠️</div>
        <p class="error-text">{{ error }}</p>
        <button class="retry-btn" @click="loadHistory">重新載入</button>
      </div>
    </div>

    <!-- Empty -->
    <div v-else-if="registrations.length === 0" class="state-container">
      <div class="empty-card">
        <div class="empty-illustration">
          <div class="empty-circle"></div>
          <span class="empty-icon">🗂️</span>
        </div>
        <h3 class="empty-title">還沒有歷史紀錄</h3>
        <p class="empty-desc">參加活動後，紀錄會出現在這裡。</p>
        <router-link to="/activity-registration-list-container" class="cta-btn">瀏覽活動</router-link>
      </div>
    </div>

    <!-- List -->
    <div v-else>
      <TransitionGroup name="card" tag="div" class="cards-container">
        <div
          v-for="(reg, index) in filteredRegistrations"
          :key="reg.id"
          class="history-card"
          :style="{ '--delay': index * 0.04 + 's' }"
        >
          <!-- 左側狀態色條 -->
          <div class="card-side-line" :class="getAttendanceClass(reg.status)"></div>

          <div class="card-content">
            <!-- 頂部：類型 + 出席狀態 -->
            <div class="card-top">
              <span class="activity-type-badge" v-if="reg.activityType">
                {{ getActivityTypeLabel(reg.activityType) }}
              </span>
              <span class="attendance-badge" :class="getAttendanceClass(reg.status)">
                {{ getAttendanceLabel(reg.status) }}
              </span>
            </div>

            <!-- 活動名稱 -->
            <h3 class="activity-title">{{ reg.activityTitle || '（活動資訊不可用）' }}</h3>

            <!-- 活動資訊 -->
            <div class="info-grid">
              <div class="info-item" v-if="reg.activityLocation">
                <span class="info-icon">📍</span>
                <span class="info-text">{{ reg.activityLocation }}</span>
              </div>
              <div class="info-item" v-if="reg.activityStartTime">
                <span class="info-icon">🗓️</span>
                <span class="info-text">
                  {{ formatDate(reg.activityStartTime) }}
                  <span class="time-sep">—</span>
                  {{ formatTimeOnly(reg.activityEndTime) }}
                </span>
              </div>

              <!-- 已出席：顯示簽到時間 -->
              <div class="info-item" v-if="reg.status === 'ATTENDED'">
                <span class="info-icon">✅</span>
                <span class="info-text">
                  簽到 {{ formatDate(reg.checkedInTime) }}
                  <span v-if="reg.late" class="late-tag">遲到</span>
                </span>
              </div>

              <!-- 費用（有費用才顯示） -->
              <div class="info-item" v-if="reg.paymentAmount > 0">
                <span class="info-icon">🏷️</span>
                <span class="info-text">NT$ {{ reg.paymentAmount }}</span>
              </div>
            </div>

            <!-- 底部：報名日期 + 活動狀態標記 -->
            <div class="card-footer">
              <span class="reg-date">報名於 {{ formatDate(reg.registrationTime) }}</span>
              <span class="activity-status-tag" :class="getActivityStatusClass(reg.activityStatus)">
                {{ getActivityStatusLabel(reg.activityStatus) }}
              </span>
            </div>
          </div>
        </div>
      </TransitionGroup>

      <div v-if="filteredRegistrations.length === 0" class="no-result">
        此分類目前沒有紀錄
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { registrationApi } from '@/api/registration';
import { useUserStore } from '@/stores/user';

const userStore = useUserStore();
const registrations = ref([]);
const loading = ref(false);
const error = ref(null);
const activeTab = ref('ALL');

const tabs = [
  { label: '全部',   value: 'ALL',       dotClass: 'dot-gray' },
  { label: '已出席', value: 'ATTENDED',  dotClass: 'dot-green' },
  { label: '未出席', value: 'ABSENT',    dotClass: 'dot-orange' },
  { label: '已取消', value: 'CANCELLED', dotClass: 'dot-gray' },
];

const attendedCount = computed(() =>
  registrations.value.filter(r => r.status === 'ATTENDED').length
);
const absentCount = computed(() =>
  registrations.value.filter(r => r.status === 'ABSENT').length
);

const filteredRegistrations = computed(() => {
  if (activeTab.value === 'ALL') return registrations.value;
  return registrations.value.filter(r => r.status === activeTab.value);
});

const getTabCount = (val) => {
  if (val === 'ALL') return registrations.value.length;
  return registrations.value.filter(r => r.status === val).length;
};

// 出席狀態
const getAttendanceLabel = (status) => {
  const map = { ATTENDED: '已出席', ABSENT: '未出席', CANCELLED: '已取消' };
  return map[status] || status;
};
const getAttendanceClass = (status) => {
  const map = { ATTENDED: 'attend-yes', ABSENT: 'attend-no', CANCELLED: 'attend-cancel' };
  return map[status] || '';
};

// 活動狀態
const getActivityStatusLabel = (status) => {
  const map = { COMPLETED: '活動已完成', CANCELLED: '活動已取消' };
  return map[status] || status;
};
const getActivityStatusClass = (status) => {
  return status === 'COMPLETED' ? 'act-completed' : 'act-cancelled';
};

const getActivityTypeLabel = (type) => {
  const map = { REGULAR: '社課', SPECIAL: '特別活動', TRAINING: '團練', PERFORMANCE: '演出', COMPETITION: '比賽' };
  return map[type] || type;
};

const formatDate = (dateTime) => {
  if (!dateTime) return '-';
  const d = new Date(dateTime);
  return d.toLocaleDateString('zh-TW', { month: '2-digit', day: '2-digit' })
    + ' ' + d.toLocaleTimeString('zh-TW', { hour: '2-digit', minute: '2-digit' });
};
const formatTimeOnly = (dateTime) => {
  if (!dateTime) return '';
  return new Date(dateTime).toLocaleTimeString('zh-TW', { hour: '2-digit', minute: '2-digit' });
};

const loadHistory = async () => {
  loading.value = true;
  error.value = null;
  try {
    const response = await registrationApi.getMyRegistrationsDetail(userStore.userId);
    // 只顯示活動已完成或已取消的報名
    registrations.value = (response.data || []).filter(
      r => r.activityStatus === 'COMPLETED' || r.activityStatus === 'CANCELLED'
    );
  } catch (err) {
    error.value = '載入歷史紀錄時發生錯誤，請稍後再試。';
  } finally {
    loading.value = false;
  }
};

onMounted(() => loadHistory());
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Noto+Sans+TC:wght@300;400;500;700&family=Space+Mono:wght@400;700&display=swap');

.registration-history {
  min-height: 100vh;
  background: #fafafa;
  font-family: 'Noto Sans TC', sans-serif;
  color: #1e1f26;
  max-width: 1400px;
  margin: 0 auto;
  padding: 3rem 2rem;
}

/* ===== Header ===== */
.page-header {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 2rem; flex-wrap: wrap; gap: 1rem;
}
.header-left { display: flex; align-items: center; gap: 1rem; }
.back-btn {
  background: #fff; border: 1px solid #e2e4ed; border-radius: 8px;
  padding: 0.45rem 0.9rem; color: #6b7280; font-size: 0.82rem;
  cursor: pointer; font-family: 'Noto Sans TC', sans-serif;
  transition: all 0.2s; white-space: nowrap;
}
.back-btn:hover { background: #f0f1f8; color: #374151; }
.header-text { display: flex; align-items: center; gap: 0.75rem; }
.header-icon {
  width: 46px; height: 46px;
  background: linear-gradient(135deg, #374151, #6b7280);
  border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  font-size: 1.3rem;
}
.page-title {
  font-size: 1.5rem; font-weight: 700; margin: 0;
  color: #1e1f26;
}
.page-subtitle { margin: 0.15rem 0 0; font-size: 0.8rem; color: #9ca3af; }

.header-stats { display: flex; gap: 0.75rem; }
.stat-chip {
  background: #fff; border: 1px solid #e2e4ed; border-radius: 10px;
  padding: 0.5rem 1rem; text-align: center; min-width: 60px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}
.stat-chip.green { border-color: #6ee7b7; background: rgba(16,185,129,0.05); }
.stat-chip.gray  { border-color: #d1d5db; background: rgba(107,114,128,0.05); }
.stat-number {
  display: block; font-family: 'Space Mono', monospace;
  font-size: 1.2rem; font-weight: 700; line-height: 1; color: #1e1f26;
}
.stat-chip.green .stat-number { color: #059669; }
.stat-chip.gray  .stat-number { color: #6b7280; }
.stat-label { display: block; font-size: 0.65rem; color: #9ca3af; margin-top: 0.2rem; }

/* ===== Tabs ===== */
.filter-tabs { display: flex; gap: 0.5rem; margin-bottom: 1.75rem; flex-wrap: wrap; }
.tab-btn {
  background: #fff; border: 1px solid #e2e4ed; border-radius: 8px;
  padding: 0.45rem 0.9rem; color: #6b7280; font-size: 0.82rem;
  cursor: pointer; transition: all 0.2s;
  display: flex; align-items: center; gap: 0.45rem;
  font-family: 'Noto Sans TC', sans-serif;
}
.tab-btn:hover { background: #f5f5f5; }
.tab-btn.active { background: #f0f1f8; border-color: #c4beff; color: #374151; font-weight: 500; }
.tab-dot { width: 7px; height: 7px; border-radius: 50%; flex-shrink: 0; }
.dot-green  { background: #10b981; }
.dot-orange { background: #f59e0b; }
.dot-gray   { background: #9ca3af; }
.tab-count {
  background: #f0f1f5; border-radius: 20px; padding: 0 6px;
  font-size: 0.7rem; font-family: 'Space Mono', monospace; color: #9ca3af;
}

/* ===== State ===== */
.state-container { display: flex; justify-content: center; align-items: center; min-height: 300px; }
.loading-wrapper { text-align: center; }
.spinner {
  width: 36px; height: 36px; border: 3px solid #e2e4ed;
  border-top-color: #6b7280; border-radius: 50%;
  animation: spin 0.8s linear infinite; margin: 0 auto 1rem;
}
@keyframes spin { to { transform: rotate(360deg); } }
.loading-text { color: #9ca3af; font-size: 0.9rem; }
.error-card { text-align: center; padding: 2rem; }
.error-icon { font-size: 2rem; margin-bottom: 0.5rem; }
.error-text { color: #ef4444; margin-bottom: 1rem; font-size: 0.9rem; }
.retry-btn {
  border: 1px solid #6b7280; color: #6b7280; background: transparent;
  padding: 0.45rem 1rem; border-radius: 8px; cursor: pointer;
  font-family: 'Noto Sans TC', sans-serif;
}
.empty-card { text-align: center; padding: 3rem 2rem; }
.empty-illustration { position: relative; width: 80px; height: 80px; margin: 0 auto 1.5rem; }
.empty-circle { width: 80px; height: 80px; border-radius: 50%; background: #f3f4f6; border: 2px dashed #d1d5db; }
.empty-icon { position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); font-size: 2rem; }
.empty-title { font-size: 1.1rem; font-weight: 600; margin: 0 0 0.5rem; color: #374151; }
.empty-desc { font-size: 0.85rem; color: #9ca3af; margin: 0 0 1.5rem; }
.cta-btn {
  display: inline-block; background: #374151; color: #fff;
  padding: 0.55rem 1.4rem; border-radius: 10px;
  text-decoration: none; font-size: 0.85rem;
}

/* ===== Cards ===== */
.cards-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 1rem;
}

.history-card {
  background: #fff; border: 1px solid #e2e4ed; border-radius: 12px;
  overflow: hidden; display: flex;
  transition: transform 0.2s, box-shadow 0.2s;
  animation: cardIn 0.35s ease both;
  animation-delay: var(--delay, 0s);
}
.history-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0,0,0,0.08);
}
@keyframes cardIn {
  from { opacity: 0; transform: translateY(12px); }
  to   { opacity: 1; transform: translateY(0); }
}

/* 左側色條 */
.card-side-line { width: 4px; flex-shrink: 0; }
.card-side-line.attend-yes    { background: linear-gradient(180deg, #10b981, #34d399); }
.card-side-line.attend-no     { background: linear-gradient(180deg, #f59e0b, #fbbf24); }
.card-side-line.attend-cancel { background: linear-gradient(180deg, #9ca3af, #d1d5db); }

.card-content { flex: 1; padding: 1rem 1rem 0.85rem; display: flex; flex-direction: column; gap: 0.5rem; }

/* 頂部 */
.card-top { display: flex; align-items: center; justify-content: space-between; }
.activity-type-badge {
  background: #f5f6fa; border: 1px solid #e2e4ed;
  border-radius: 5px; padding: 0.18rem 0.55rem;
  font-size: 0.66rem; color: #6b7280; font-weight: 600;
  text-transform: uppercase; letter-spacing: 0.04em;
}

/* 出席狀態 badge */
.attendance-badge {
  display: flex; align-items: center; padding: 0.22rem 0.65rem;
  border-radius: 20px; font-size: 0.72rem; font-weight: 500;
}
.attendance-badge.attend-yes    { background: #d1fae5; color: #059669; }
.attendance-badge.attend-no     { background: #fef3c7; color: #b45309; }
.attendance-badge.attend-cancel { background: #f3f4f6; color: #6b7280; }

/* 活動名稱 */
.activity-title {
  font-size: 0.95rem; font-weight: 700; color: #1e1f26;
  margin: 0; line-height: 1.4;
  display: -webkit-box; -webkit-line-clamp: 2;
  -webkit-box-orient: vertical; overflow: hidden;
}

/* Info grid */
.info-grid { display: flex; flex-direction: column; gap: 0.35rem; }
.info-item { display: flex; align-items: center; gap: 0.45rem; }
.info-icon { font-size: 0.82rem; flex-shrink: 0; }
.info-text { font-size: 0.8rem; color: #4b5563; }
.time-sep { color: #9ca3af; margin: 0 0.2rem; }

.late-tag {
  background: #fee2e2; color: #dc2626;
  font-size: 0.63rem; padding: 0.08rem 0.4rem;
  border-radius: 4px; margin-left: 0.3rem;
}

/* Footer */
.card-footer {
  display: flex; align-items: center; justify-content: space-between;
  padding-top: 0.5rem; border-top: 1px solid #f0f1f5; margin-top: auto;
}
.reg-date { font-size: 0.68rem; color: #c4c9d4; }

/* 活動狀態 tag */
.activity-status-tag {
  font-size: 0.66rem; padding: 0.15rem 0.55rem;
  border-radius: 4px; font-weight: 500;
}
.act-completed { background: #f0fdf4; color: #16a34a; border: 1px solid #bbf7d0; }
.act-cancelled { background: #f9fafb; color: #9ca3af; border: 1px solid #e5e7eb; }

/* No result */
.no-result { text-align: center; color: #9ca3af; font-size: 0.85rem; padding: 2rem 0; }

/* ===== Transition ===== */
.card-enter-active, .card-leave-active { transition: all 0.3s ease; }
.card-enter-from, .card-leave-to { opacity: 0; transform: translateY(10px); }

/* ===== Responsive ===== */
@media (max-width: 560px) {
  .registration-history { padding: 1.5rem 1rem 3rem; }
  .cards-container { grid-template-columns: 1fr; }
  .page-header { flex-direction: column; align-items: flex-start; }
}
</style>