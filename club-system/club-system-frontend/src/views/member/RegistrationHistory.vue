<template>
  <div class="registration-history">

    <!-- Header -->
    <div class="page-header">
      <div class="header-left">
        <div class="header-label">
          <span class="label-line"></span>
          <span class="label-text">REGISTRATION HISTORY</span>
          <span class="label-num">2026</span>
        </div>
        <h1 class="page-title">
          <span class="title-line title-line-1">歷史</span>
          <span class="title-line title-line-2"><span class="title-accent">紀錄</span></span>
        </h1>
        <p class="page-subtitle">
          <span class="subtitle-inner">已完成或已取消的活動報名</span>
        </p>
      </div>
      <div class="header-right">
        <button class="back-btn" @click="$router.back()">← MY REGISTRATIONS</button>
        <div class="header-stats" v-if="!loading && registrations.length > 0">
          <div class="stat-block">
            <span class="stat-number">{{ registrations.length }}</span>
            <span class="stat-label">TOTAL</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-block">
            <span class="stat-number">{{ attendedCount }}</span>
            <span class="stat-label">ATTENDED</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-block muted">
            <span class="stat-number">{{ absentCount }}</span>
            <span class="stat-label">ABSENT</span>
          </div>
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
        {{ tab.label }}
        <span class="tab-count">{{ getTabCount(tab.value) }}</span>
      </button>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="state-container">
      <div class="loading-wrapper">
        <div class="loading-bars">
          <span v-for="i in 5" :key="i" :style="{ animationDelay: (i * 0.1) + 's' }"></span>
        </div>
        <p class="loading-text">LOADING...</p>
      </div>
    </div>

    <!-- Error -->
    <div v-else-if="error" class="state-container">
      <div class="error-card">
        <div class="error-code">ERR</div>
        <p class="error-text">{{ error }}</p>
        <button class="retry-btn" @click="loadHistory">RETRY</button>
      </div>
    </div>

    <!-- Empty -->
    <div v-else-if="registrations.length === 0" class="state-container">
      <div class="empty-card">
        <div class="empty-big-text">EMPTY</div>
        <p class="empty-desc">還沒有歷史紀錄，參加活動後會出現在這裡</p>
        <router-link to="/activity-registration-list-container" class="cta-btn">
          瀏覽活動 <span>→</span>
        </router-link>
      </div>
    </div>

    <!-- Tickets -->
    <div v-else class="tickets-list" :class="{ 'cards-fade': !cardsVisible }">

      <div
        v-for="(reg, index) in displayedRegistrations"
        :key="reg.id"
        class="ticket"
        :class="getAttendanceClass(reg.status)"
      >
        <!-- Top gray band -->
        <div class="ticket-band">
          <span class="band-left">
            <span class="band-star">★</span>
            {{ getActivityTypeLabel(reg.activityType) || 'ACTIVITY' }}
            <span class="band-star">★</span>
          </span>
          <span class="band-right">NO. {{ String(reg.id).padStart(6, '0') }}</span>
        </div>

        <!-- Ticket body -->
        <div class="ticket-body">

          <!-- Main section -->
          <div class="ticket-main">

            <div class="ticket-top-row">
              <div class="attendance-badge" :class="getAttendanceClass(reg.status)">
                <span class="status-dot"></span>
                {{ getAttendanceLabel(reg.status) }}
              </div>
              <span class="ticket-index">{{ String(index + 1).padStart(2, '0') }}</span>
            </div>

            <h3 class="ticket-title">{{ reg.activityTitle || '（活動資訊不可用）' }}</h3>

            <div class="ticket-info-grid">
              <div class="ticket-info-item" v-if="reg.activityStartTime">
                <span class="tkt-label">DATE &amp; TIME</span>
                <span class="tkt-value">
                  {{ formatDate(reg.activityStartTime) }}
                  <span class="time-sep">—</span>
                  {{ formatTimeOnly(reg.activityEndTime) }}
                </span>
              </div>
              <div class="ticket-info-item" v-if="reg.activityLocation">
                <span class="tkt-label">VENUE</span>
                <span class="tkt-value">{{ reg.activityLocation }}</span>
              </div>
            </div>

            <!-- 已出席：簽到時間 -->
            <div class="ticket-payment-row" v-if="reg.status === 'ATTENDED'">
              <div class="ticket-info-item">
                <span class="tkt-label">CHECK-IN</span>
                <span class="tkt-value">
                  {{ formatDate(reg.checkedInTime) }}
                  <span v-if="reg.late" class="late-tag">LATE</span>
                </span>
              </div>
              <div class="ticket-info-item" v-if="reg.paymentAmount > 0">
                <span class="tkt-label">AMOUNT</span>
                <span class="tkt-value tkt-amount">NT$ {{ reg.paymentAmount }}</span>
              </div>
            </div>

            <!-- 未出席 / 已取消：費用 -->
            <div class="ticket-payment-row" v-else-if="reg.paymentAmount > 0">
              <div class="ticket-info-item">
                <span class="tkt-label">AMOUNT</span>
                <span class="tkt-value tkt-amount">NT$ {{ reg.paymentAmount }}</span>
              </div>
            </div>

          </div>

          <!-- Perforation -->
          <div class="ticket-perf">
            <div class="perf-notch perf-top"></div>
            <div class="perf-line"></div>
            <div class="perf-notch perf-bottom"></div>
          </div>

          <!-- Stub -->
          <div class="ticket-stub">
            <div class="stub-admit">DANCE</div>
            <div class="stub-one">CLUB</div>
            <div class="stub-ornament">✦ ✦ ✦</div>
            <div class="stub-date-block">
              <span class="stub-date-label">DATE</span>
              <span class="stub-date-val">{{ formatDateShort(reg.activityStartTime) }}</span>
            </div>
            <div class="stub-footer">
              <span class="stub-reg-date">報名 {{ formatDateShort(reg.registrationTime) }}</span>
              <span class="stub-activity-status" :class="getActivityStatusClass(reg.activityStatus)">
                {{ getActivityStatusLabel(reg.activityStatus) }}
              </span>
            </div>
            <button
              v-if="reg.activityStatus === 'COMPLETED' && reg.status === 'ATTENDED'"
              class="feedback-btn"
              @click="router.push('/feedback/activity/' + reg.activityId)"
            >✍️ 填寫回饋</button>
          </div>

        </div>
      </div>

      <div v-if="displayedRegistrations.length === 0" class="no-filter-result">
        此分類目前沒有紀錄
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { registrationApi } from '@/api/registration';
import { useUserStore } from '@/stores/user';

const router = useRouter();
const userStore = useUserStore();
const registrations = ref([]);
const loading = ref(false);
const error = ref(null);
const activeTab = ref('ALL');
const cardsVisible = ref(true);
const displayedRegistrations = ref([]);

// Navbar scroll hide/show
const navHidden = ref(false);
let lastScrollY = 0;
const handleScroll = () => {
  const currentY = window.scrollY;
  navHidden.value = currentY > lastScrollY && currentY > 60;
  lastScrollY = currentY;
};
onUnmounted(() => window.removeEventListener('scroll', handleScroll));

const handleLogout = () => {
  userStore.logout();
  router.push('/');
};

const tabs = [
  { label: '全部',   value: 'ALL' },
  { label: '已出席', value: 'ATTENDED' },
  { label: '未出席', value: 'ABSENT' },
  { label: '已取消', value: 'CANCELLED' },
];

// Tab 切換：fade out → 換資料 → fade in
watch(activeTab, () => {
  cardsVisible.value = false;
  setTimeout(() => {
    if (activeTab.value === 'ALL')
      displayedRegistrations.value = registrations.value;
    else
      displayedRegistrations.value = registrations.value.filter(r => r.status === activeTab.value);
    cardsVisible.value = true;
  }, 150);
});

const attendedCount = computed(() =>
  registrations.value.filter(r => r.status === 'ATTENDED').length
);
const absentCount = computed(() =>
  registrations.value.filter(r => r.status === 'ABSENT').length
);

const getTabCount = (val) => {
  if (val === 'ALL') return registrations.value.length;
  return registrations.value.filter(r => r.status === val).length;
};

const getAttendanceLabel = (status) => {
  const map = { ATTENDED: '已出席', ABSENT: '未出席', CANCELLED: '已取消' };
  return map[status] || status;
};
const getAttendanceClass = (status) => {
  const map = { ATTENDED: 'status-attended', ABSENT: 'status-absent', CANCELLED: 'status-cancelled' };
  return map[status] || '';
};

const getActivityStatusLabel = (status) => {
  const map = { COMPLETED: 'COMPLETED', CANCELLED: 'CANCELLED' };
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
const formatDateShort = (dateTime) => {
  if (!dateTime) return '-';
  const d = new Date(dateTime);
  return d.toLocaleDateString('zh-TW', { month: '2-digit', day: '2-digit' });
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
    registrations.value = (response.data || []).filter(
      r => r.activityStatus === 'COMPLETED' || r.activityStatus === 'CANCELLED'
    );
    displayedRegistrations.value = registrations.value;
  } catch (err) {
    error.value = '載入歷史紀錄時發生錯誤，請稍後再試。';
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  loadHistory();
  window.addEventListener('scroll', handleScroll, { passive: true });
});
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Bebas+Neue&family=Noto+Sans+TC:wght@300;400;500;700&family=Space+Mono:wght@400;700&display=swap');

.registration-history {
  min-height: 100vh;
  background: #ffffff;
  font-family: 'Noto Sans TC', sans-serif;
  color: #0a0a0a;
  max-width: 1400px;
  margin: 0 auto;
  padding: 6rem 2rem 5rem;
}

/* ===== Header ===== */
.page-header {
  display: flex; justify-content: space-between; align-items: flex-end;
  margin-bottom: 2.5rem; padding-bottom: 2rem;
  border-bottom: 2px solid #0a0a0a; flex-wrap: wrap; gap: 2rem;
}
.header-left { display: flex; flex-direction: column; gap: 0.4rem; }

@keyframes slideUpFade {
  from { opacity: 0; transform: translateY(24px); }
  to   { opacity: 1; transform: translateY(0); }
}
@keyframes expandLine {
  from { width: 0; }
  to   { width: 2rem; }
}

.header-label {
  display: flex; align-items: center; gap: 0.6rem; margin-bottom: 1rem;
  animation: slideUpFade 0.6s cubic-bezier(0.16, 1, 0.3, 1) 0.1s both;
}
.label-line {
  display: inline-block; height: 1px; width: 2rem; background: #ff2d6b;
  animation: expandLine 0.5s cubic-bezier(0.16, 1, 0.3, 1) 0.2s both;
}
.label-text { font-family: 'Space Mono', monospace; font-size: 0.65rem; letter-spacing: 0.2em; color: #ff2d6b; }
.label-num  { font-family: 'Space Mono', monospace; font-size: 0.6rem; letter-spacing: 0.1em; color: #ccc; margin-left: auto; }

.page-title {
  font-family: 'Bebas Neue', sans-serif; font-size: 5rem;
  line-height: 0.92; margin: 0; color: #0a0a0a;
  letter-spacing: 0.02em; display: flex; flex-direction: column;
}
.title-line { display: block; overflow: visible; padding-bottom: 0.1em; font-weight: 500; }
.title-line-1 { animation: slideUpFade 0.7s cubic-bezier(0.16, 1, 0.3, 1) 0.25s both; }
.title-line-2 { animation: slideUpFade 0.7s cubic-bezier(0.16, 1, 0.3, 1) 0.38s both; }
.title-accent { color: #ff2d6b; font-weight: 500; }

.page-subtitle { font-size: 0.78rem; color: #999; margin: 0.75rem 0 0; letter-spacing: 0.04em; overflow: hidden; }
.subtitle-inner { display: inline-block; animation: slideUpFade 0.6s cubic-bezier(0.16, 1, 0.3, 1) 0.52s both; }

.header-right { display: flex; flex-direction: column; align-items: flex-end; gap: 1.25rem; }
.back-btn {
  font-family: 'Space Mono', monospace; font-size: 0.72rem;
  letter-spacing: 0.12em; color: #0a0a0a; background: transparent;
  border: none; border-bottom: 1px solid #0a0a0a;
  padding: 0 0 2px; cursor: pointer;
  transition: color 0.2s, border-color 0.2s;
}
.back-btn:hover { color: #ff2d6b; border-color: #ff2d6b; }

.header-stats {
  display: flex; align-items: center; gap: 1.25rem;
  background: #f5f5f5; border: 1px solid #e0e0e0;
  border-radius: 4px; padding: 0.75rem 1.25rem;
}
.stat-block { display: flex; flex-direction: column; align-items: center; gap: 0.2rem; }
.stat-number { font-family: 'Bebas Neue', sans-serif; font-size: 2rem; line-height: 1; color: #0a0a0a; }
.stat-block.muted .stat-number { color: #aaa; }
.stat-label { font-family: 'Space Mono', monospace; font-size: 0.55rem; letter-spacing: 0.15em; color: #aaa; }
.stat-divider { width: 1px; height: 2rem; background: #e0e0e0; }

/* ===== Filter Tabs ===== */
.filter-tabs {
  display: flex; margin-bottom: 2.5rem;
  border: 1px solid #0a0a0a; overflow: hidden; width: fit-content;
}
.tab-btn {
  background: transparent; border: none; border-right: 1px solid #0a0a0a;
  padding: 0.55rem 1.25rem; color: #aaa; font-size: 0.78rem;
  font-family: 'Space Mono', monospace; letter-spacing: 0.06em;
  cursor: pointer; transition: all 0.2s;
  display: flex; align-items: center; gap: 0.5rem;
}
.tab-btn:last-child { border-right: none; }
.tab-btn:hover { background: #f5f5f5; color: #0a0a0a; }
.tab-btn.active { background: #ff2d6b; color: #fff; border-color: #ff2d6b; }
.tab-count { font-size: 0.65rem; background: rgba(0,0,0,0.08); border-radius: 2px; padding: 0 5px; }
.tab-btn.active .tab-count { background: rgba(255,255,255,0.25); }

/* ===== State Containers ===== */
.state-container { display: flex; justify-content: center; align-items: center; min-height: 40vh; }
.loading-wrapper { text-align: center; }
.loading-bars { display: flex; gap: 4px; justify-content: center; margin-bottom: 1rem; }
.loading-bars span {
  display: block; width: 3px; height: 28px; background: #ff2d6b;
  border-radius: 2px; animation: barPulse 0.8s ease-in-out infinite alternate;
}
@keyframes barPulse {
  from { transform: scaleY(0.3); opacity: 0.3; }
  to   { transform: scaleY(1); opacity: 1; }
}
.loading-text { font-family: 'Space Mono', monospace; font-size: 0.7rem; letter-spacing: 0.2em; color: #aaa; }

.error-card { text-align: center; }
.error-code { font-family: 'Bebas Neue', sans-serif; font-size: 6rem; color: #ff2d6b; line-height: 1; margin-bottom: 1rem; }
.error-text { color: #999; font-size: 0.85rem; margin-bottom: 1.5rem; }
.retry-btn {
  background: transparent; border: 1px solid #ff2d6b; color: #ff2d6b;
  padding: 0.5rem 1.5rem; font-family: 'Space Mono', monospace;
  font-size: 0.72rem; letter-spacing: 0.1em; cursor: pointer; transition: all 0.2s;
}
.retry-btn:hover { background: #ff2d6b; color: #fff; }

.empty-card { text-align: center; }
.empty-big-text {
  font-family: 'Bebas Neue', sans-serif; font-size: 8rem; color: transparent;
  line-height: 1; letter-spacing: 0.05em; margin-bottom: 1rem;
  -webkit-text-stroke: 1px #e0e0e0;
}
.empty-desc { color: #aaa; font-size: 0.85rem; margin-bottom: 2rem; }
.cta-btn {
  display: inline-block; background: #ff2d6b; color: #fff;
  padding: 0.7rem 2rem; text-decoration: none;
  font-family: 'Space Mono', monospace; font-size: 0.75rem;
  letter-spacing: 0.1em; transition: all 0.2s;
}
.cta-btn:hover { background: #0a0a0a; color: #fff; }

/* ===== Tickets List ===== */
.tickets-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  opacity: 1;
  transition: opacity 0.15s ease;
}
.cards-fade { opacity: 0; }

/* ===== Ticket — all gray base ===== */
.ticket {
  border-radius: 6px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0,0,0,0.08), 0 1px 4px rgba(0,0,0,0.05);
  transition: transform 0.2s, box-shadow 0.2s;
  background: #F5F5F3;
  border: 1.5px solid #ddd;
}
.ticket:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 28px rgba(0,0,0,0.11), 0 2px 6px rgba(0,0,0,0.06);
}

/* ---- Band — uniform dark gray ---- */
.ticket-band {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.35rem 1.25rem;
  background: #3A3A3A;
  color: #aaaaaa;
  font-family: 'Space Mono', monospace;
  font-size: 0.62rem;
  letter-spacing: 0.18em;
  font-weight: 700;
}
.band-star { margin: 0 0.35rem; }
.band-left { display: flex; align-items: center; }

/* ---- Body ---- */
.ticket-body {
  display: flex;
  min-height: 160px;
}

/* ---- Main ---- */
.ticket-main {
  flex: 1 1 auto;
  padding: 1.1rem 1.3rem 1.1rem 1.4rem;
  display: flex;
  flex-direction: column;
  gap: 0.6rem;
  background: #F5F5F3;
  background-image: repeating-linear-gradient(
    0deg,
    transparent,
    transparent 23px,
    rgba(0,0,0,0.04) 23px,
    rgba(0,0,0,0.04) 24px
  );
}

.ticket-top-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.ticket-index {
  font-family: 'Space Mono', monospace;
  font-size: 0.6rem;
  color: #bbb;
  letter-spacing: 0.1em;
}

/* ---- Status badges with color ---- */
.attendance-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  font-family: 'Space Mono', monospace;
  font-size: 0.6rem;
  letter-spacing: 0.1em;
  padding: 0.2rem 0.6rem;
  border-radius: 2px;
  font-weight: 700;
}
.status-dot { width: 5px; height: 5px; border-radius: 50%; display: block; }

/* 已出席 → 綠色 */
.attendance-badge.status-attended {
  background: rgba(22,101,52,0.10);
  color: #166534;
  border: 1px solid rgba(22,101,52,0.25);
}
.attendance-badge.status-attended .status-dot { background: #16a34a; }

/* 未出席 → 橙色 */
.attendance-badge.status-absent {
  background: rgba(180,83,9,0.10);
  color: #b45309;
  border: 1px solid rgba(180,83,9,0.25);
}
.attendance-badge.status-absent .status-dot { background: #f59e0b; }

/* 已取消 → 灰色 */
.attendance-badge.status-cancelled {
  background: rgba(0,0,0,0.05);
  color: #888;
  border: 1px solid #ddd;
}
.attendance-badge.status-cancelled .status-dot { background: #bbb; }

.ticket-title {
  font-family: 'Bebas Neue', sans-serif;
  font-size: 1.65rem;
  line-height: 1.1;
  margin: 0;
  color: #222;
  letter-spacing: 0.04em;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.ticket-info-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem 2rem;
}
.ticket-info-item {
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
}
.tkt-label {
  font-family: 'Space Mono', monospace;
  font-size: 0.5rem;
  letter-spacing: 0.18em;
  color: #aaa;
  text-transform: uppercase;
}
.tkt-value {
  font-size: 0.8rem;
  color: #444;
  font-weight: 500;
}
.tkt-value.tkt-amount {
  font-family: 'Space Mono', monospace;
  color: #555;
  font-weight: 700;
}
.time-sep { color: #ccc; margin: 0 0.2rem; }

.ticket-payment-row {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem 2rem;
  padding-top: 0.5rem;
  border-top: 1px dashed #ddd;
  margin-top: 0.2rem;
}

.late-tag {
  font-family: 'Space Mono', monospace; font-size: 0.55rem;
  background: rgba(180,83,9,0.08); color: #b45309;
  padding: 0.1rem 0.4rem; border-radius: 2px; margin-left: 0.4rem;
  letter-spacing: 0.06em; border: 1px solid rgba(180,83,9,0.2);
}

/* ---- Perforation ---- */
.ticket-perf {
  width: 28px;
  flex-shrink: 0;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  background: #F5F5F3;
}
.perf-notch {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  flex-shrink: 0;
  background: #ffffff;
  border: 1.5px solid #ddd;
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  z-index: 2;
}
.perf-top    { top: -9px; }
.perf-bottom { bottom: -9px; }
.perf-line {
  flex: 1;
  width: 2px;
  border-left: 2px dashed #ccc;
  margin: 10px 0;
}

/* ---- Stub — dark gray ---- */
.ticket-stub {
  width: 130px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.3rem;
  padding: 1rem 0.8rem;
  text-align: center;
  background: #3A3A3A;
}

.stub-admit {
  font-family: 'Bebas Neue', sans-serif;
  font-size: 1.1rem;
  letter-spacing: 0.25em;
  line-height: 1;
  color: rgba(255,255,255,0.35);
}
.stub-one {
  font-family: 'Bebas Neue', sans-serif;
  font-size: 2rem;
  letter-spacing: 0.12em;
  line-height: 0.95;
  color: #bbbbbb;
}
.stub-ornament {
  font-size: 0.5rem;
  color: rgba(255,255,255,0.2);
  letter-spacing: 0.2em;
  margin: 0.15rem 0;
}
.stub-date-block {
  display: flex;
  flex-direction: column;
  gap: 0.1rem;
  margin: 0.2rem 0;
}
.stub-date-label {
  font-family: 'Space Mono', monospace;
  font-size: 0.48rem;
  letter-spacing: 0.2em;
  color: rgba(255,255,255,0.35);
}
.stub-date-val {
  font-family: 'Space Mono', monospace;
  font-size: 0.72rem;
  color: rgba(255,255,255,0.7);
  letter-spacing: 0.08em;
}

.stub-footer {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.4rem;
  margin-top: auto;
  width: 100%;
}
.stub-reg-date {
  font-family: 'Space Mono', monospace;
  font-size: 0.47rem;
  color: rgba(255,255,255,0.3);
  letter-spacing: 0.05em;
}
.stub-activity-status {
  font-family: 'Space Mono', monospace;
  font-size: 0.55rem;
  letter-spacing: 0.12em;
  padding: 0.18rem 0.5rem;
  border-radius: 2px;
  width: 100%;
  text-align: center;
}
.act-completed {
  background: rgba(255,255,255,0.08);
  color: rgba(255,255,255,0.5);
  border: 1px solid rgba(255,255,255,0.15);
}
.act-cancelled {
  background: rgba(255,255,255,0.05);
  color: rgba(255,255,255,0.3);
  border: 1px solid rgba(255,255,255,0.1);
}

.feedback-btn {
  margin-top: 0.5rem;
  width: 100%;
  padding: 0.35rem 0;
  background: rgba(255, 45, 107, 0.15);
  border: 1px solid rgba(255, 45, 107, 0.4);
  border-radius: 4px;
  color: #ff6b9d;
  font-size: 0.62rem;
  font-family: 'Space Mono', monospace;
  letter-spacing: 0.06em;
  cursor: pointer;
  transition: all 0.2s;
}
.feedback-btn:hover {
  background: rgba(255, 45, 107, 0.28);
  color: #fff;
}

.no-filter-result {
  text-align: center; color: #ccc;
  font-family: 'Space Mono', monospace; font-size: 0.72rem;
  letter-spacing: 0.1em; padding: 3rem 0;
}

/* ===== Responsive ===== */
@media (max-width: 640px) {
  .registration-history { padding: 5rem 1rem 4rem; }
  .page-title { font-size: 3.5rem; }
  .page-header { flex-direction: column; align-items: flex-start; }
  .header-right { align-items: flex-start; }
  .ticket-stub { width: 100px; }
  .stub-one { font-size: 1.5rem; }
  .ticket-title { font-size: 1.35rem; }
}
</style>
