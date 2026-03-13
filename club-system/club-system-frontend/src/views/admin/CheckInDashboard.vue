<template>
  <div class="dashboard-container">
    <!-- Header -->
    <div class="header">
      <div class="header-left">
        <button class="back-btn" @click="$router.back()">← 返回</button>
        <div>
          <h1>簽到管理中心</h1>
          <p class="subtitle">{{ currentActivity?.title || '請選擇活動' }}</p>
        </div>
      </div>
      <div class="header-right">
        <button class="qr-btn" @click="showQrModal = true" :disabled="!selectedActivityId">
          <span class="icon">⬛</span> 產生 QR Code
        </button>
      </div>
    </div>

    <!-- Activity Selector -->
    <div class="activity-selector-card">
      <label class="field-label">選擇活動</label>
      <select v-model="selectedActivityId" @change="onActivityChange" class="activity-select">
        <option value="">-- 選擇活動 --</option>
        <option v-for="act in activities" :key="act.id" :value="act.id">
          {{ act.title }} ({{ formatDate(act.startTime) }})
        </option>
      </select>
    </div>

    <!-- Stats Row -->
    <div class="stats-row" v-if="selectedActivityId">
      <div class="stat-card">
        <div class="stat-num">{{ stats.total }}</div>
        <div class="stat-label">總報名人數</div>
      </div>
      <div class="stat-card accent">
        <div class="stat-num">{{ stats.checkedIn }}</div>
        <div class="stat-label">已簽到</div>
      </div>
      <div class="stat-card warn">
        <div class="stat-num">{{ stats.total - stats.checkedIn }}</div>
        <div class="stat-label">未簽到</div>
      </div>
      <div class="stat-card info">
        <div class="stat-num">{{ stats.late }}</div>
        <div class="stat-label">遲到人數</div>
      </div>
      <div class="stat-card rate">
        <div class="stat-num">{{ stats.total > 0 ? Math.round((stats.checkedIn / stats.total) * 100) : 0 }}%</div>
        <div class="stat-label">簽到率</div>
        <div class="progress-bar">
          <div class="progress-fill" :style="{ width: stats.total > 0 ? (stats.checkedIn / stats.total * 100) + '%' : '0%' }"></div>
        </div>
      </div>
    </div>

    <!-- Manual Check-in Section -->
    <div class="section-card" v-if="selectedActivityId">
      <div class="section-header">
        <h2>手動簽到</h2>
        <span class="badge">管理員操作</span>
      </div>
      <div class="manual-checkin">
        <div class="search-row">
          <input
            v-model="searchKeyword"
            @input="filterRegistrations"
            placeholder="搜尋姓名或 ID..."
            class="search-input"
          />
          <div class="filter-tabs">
            <button
              v-for="f in filters"
              :key="f.value"
              :class="['filter-tab', { active: activeFilter === f.value }]"
              @click="setFilter(f.value)"
            >{{ f.label }}</button>
          </div>
        </div>

        <!-- Registration List -->
        <div class="registration-list">
          <div v-if="loading" class="loading-state">載入中...</div>
          <div v-else-if="filteredRegistrations.length === 0" class="empty-state">
            沒有符合的報名紀錄
          </div>
          <div
            v-for="reg in filteredRegistrations"
            :key="reg.id"
            class="registration-row"
            :class="{ 'checked': reg.status === 'ATTENDED', 'late': reg.late === true }"
          >
            <div class="reg-info">
              <span class="reg-id">{{ reg.userName || reg.userId }}</span>
              <span class="reg-time">報名：{{ formatDate(reg.registrationTime) }}</span>
              <span v-if="reg.status === 'ATTENDED'" class="check-time">
                簽到：{{ formatDate(reg.checkInTime) }}
                <span v-if="reg.late === true" class="late-badge">遲到</span>
              </span>
            </div>
            <div class="reg-status">
              <span class="payment-badge" :class="reg.paymentStatus?.toLowerCase()">
                {{ paymentLabel(reg.paymentStatus) }}
              </span>
              <button
                v-if="reg.status === 'REGISTERED'"
                class="checkin-btn"
                @click="handleManualCheckIn(reg)"
                :disabled="checkingIn === reg.id"
              >
                {{ checkingIn === reg.id ? '處理中...' : '✓ 簽到' }}
              </button>
              <span v-else-if="reg.status === 'ATTENDED'" class="checked-badge">✓ 已簽到</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Empty State -->
    <div class="empty-activity" v-if="!selectedActivityId">
      <div class="empty-icon">📋</div>
      <p>請選擇活動以查看簽到情況</p>
    </div>

    <!-- QR Code Modal -->
    <div class="modal-overlay" v-if="showQrModal" @click.self="showQrModal = false">
      <div class="modal">
        <div class="modal-header">
          <h3>活動簽到 QR Code</h3>
          <button class="modal-close" @click="showQrModal = false">✕</button>
        </div>
        <div class="modal-body">
          <p class="qr-activity-name">{{ currentActivity?.title }}</p>
          <div class="qr-wrapper">
            <img :src="qrCodeUrl" alt="QR Code" class="qr-image" v-if="qrCodeUrl" />
            <div class="qr-loading" v-else>產生中...</div>
          </div>
          <p class="qr-hint">會員掃碼後可自助簽到</p>
          <div class="qr-url-box">{{ checkinUrl }}</div>
        </div>
      </div>
    </div>

    <!-- Toast -->
    <div class="toast" :class="toast.type" v-if="toast.show">{{ toast.message }}</div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue';
import { activityApi } from '@/api/activity';
import { registrationApi } from '@/api/registration';

const activities = ref([]);
const selectedActivityId = ref('');
const currentActivity = computed(() => activities.value.find(a => a.id == selectedActivityId.value));
const registrations = ref([]);
const loading = ref(false);
const checkingIn = ref(null);
const showQrModal = ref(false);
const searchKeyword = ref('');
const activeFilter = ref('all');
const stats = ref({ total: 0, checkedIn: 0, late: 0 });

const filters = [
  { label: '全部', value: 'all' },
  { label: '未簽到', value: 'pending' },
  { label: '已簽到', value: 'checkedIn' },
  { label: '遲到', value: 'late' },
];

const toast = ref({ show: false, message: '', type: '' });

// QR Code
const checkinUrl = computed(() =>
  selectedActivityId.value
    ? `${window.location.origin}/checkin/member?activityId=${selectedActivityId.value}`
    : ''
);
const qrCodeUrl = computed(() =>
  selectedActivityId.value
    ? `https://api.qrserver.com/v1/create-qr-code/?size=220x220&data=${encodeURIComponent(checkinUrl.value)}`
    : ''
);

// Filtered list
const filteredRegistrations = computed(() => {
  let list = registrations.value;
  if (searchKeyword.value) {
    const kw = searchKeyword.value.toLowerCase();
    list = list.filter(r =>
      r.userId?.toLowerCase().includes(kw) ||
      r.userName?.toLowerCase().includes(kw)
    );
  }
  if (activeFilter.value === 'pending')   list = list.filter(r => r.status === 'REGISTERED');
  if (activeFilter.value === 'checkedIn') list = list.filter(r => r.status === 'ATTENDED');
  if (activeFilter.value === 'late')      list = list.filter(r => r.status === 'ATTENDED' && r.late === true);
  return list;
});

function setFilter(val) { activeFilter.value = val; }
function filterRegistrations() { /* reactive via computed */ }

async function onActivityChange() {
  if (!selectedActivityId.value) return;
  loading.value = true;
  try {
    const [regRes, countRes, checkinCountRes] = await Promise.all([
      registrationApi.getActivityRegistrations(selectedActivityId.value),
      registrationApi.countRegistrations(selectedActivityId.value),
      registrationApi.countCheckedIn(selectedActivityId.value),
    ]);
    registrations.value = regRes.data || [];
    const lateCount = registrations.value.filter(r => r.late === true).length;
    stats.value = {
      total: countRes.data || 0,
      checkedIn: checkinCountRes.data || 0,
      late: lateCount,
    };
  } catch (e) {
    showToast('載入資料失敗', 'error');
  } finally {
    loading.value = false;
  }
}

async function handleManualCheckIn(reg) {
  checkingIn.value = reg.id;
  try {
    const res = await registrationApi.checkIn(reg.id);
    // 用後端回傳的資料更新，確保 status / late 欄位正確
    reg.status = res.data.status;
    reg.checkInTime = res.data.checkInTime;
    reg.late = res.data.late;
    stats.value.checkedIn++;
    if (reg.late === true) stats.value.late++;
    showToast(`✓ ${reg.userId} 簽到成功`, 'success');
  } catch (e) {
    const msg = e.response?.data?.message || '簽到失敗';
    showToast(msg, 'error');
  } finally {
    checkingIn.value = null;
  }
}

function showToast(message, type = 'success') {
  toast.value = { show: true, message, type };
  setTimeout(() => { toast.value.show = false; }, 3000);
}

function formatDate(dateStr) {
  if (!dateStr) return '-';
  return new Date(dateStr).toLocaleString('zh-TW', {
    month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit'
  });
}

function paymentLabel(status) {
  const map = { PAID: '已繳費', PENDING: '待繳費', FREE: '免費' };
  return map[status] || status;
}

onMounted(async () => {
  try {
    const res = await activityApi.getAllActivities();
    activities.value = (res.data || []).filter(a => a.status === 'ONGOING' || a.status === 'PUBLISHED');
  } catch (e) {
    showToast('載入活動列表失敗', 'error');
  }
});
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Bebas+Neue&family=Space+Mono:wght@400;700&family=Noto+Sans+TC:wght@300;400;500;700&display=swap');

* { box-sizing: border-box; }

/*
  bg       #faf8f5  warm off-white
  card     #ffffff
  card-alt #fdf6f0  blush cream
  border   #ecddd5  dusty rose border
  text     #2d2320
  sub      #a08c84
  peach    #e8775a  warm coral accent
  pink     #f2a4a4  soft blush
  green    #7dbfa0
  amber    #e8b86d
*/

.dashboard-container {
  min-height: 100vh;
  background: #faf8f5;
  font-family: 'Noto Sans TC', sans-serif;
  padding: 0;
  color: #2d2320;
}

/* ── Header ── */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 2rem 3.5rem;
  background: #ffffff;
  border-bottom: 1px solid #ecddd5;
  position: sticky; top: 0; z-index: 10;
}
.header-left { display: flex; align-items: center; gap: 1.5rem; }

.back-btn {
  font-family: 'Space Mono', monospace;
  font-size: 0.72rem; letter-spacing: 0.1em;
  background: transparent; border: 1.5px solid #d8c4b8;
  border-radius: 20px; padding: 0.5rem 1.1rem;
  cursor: pointer; color: #a08c84;
  transition: all 0.2s;
}
.back-btn:hover { border-color: #e8775a; color: #e8775a; background: #fdf3f0; }

h1 {
  font-family: 'Bebas Neue', sans-serif;
  font-size: 2.8rem; letter-spacing: 0.08em;
  margin: 0; line-height: 1; color: #2d2320;
}
.subtitle {
  font-family: 'Space Mono', monospace;
  font-size: 0.65rem; letter-spacing: 0.15em;
  color: #e8775a; margin: 4px 0 0;
}

.qr-btn {
  display: flex; align-items: center; gap: 0.6rem;
  font-family: 'Space Mono', monospace;
  font-size: 0.72rem; letter-spacing: 0.1em;
  background: #e8775a; color: #fff; border: none;
  border-radius: 24px; padding: 0.75rem 1.75rem;
  cursor: pointer; font-weight: 700;
  transition: all 0.2s;
  box-shadow: 0 2px 12px rgba(232,119,90,0.25);
}
.qr-btn:hover:not(:disabled) { background: #d4654a; box-shadow: 0 4px 18px rgba(232,119,90,0.35); }
.qr-btn:disabled { opacity: 0.4; cursor: not-allowed; box-shadow: none; }

/* ── Body padding ── */
.activity-selector-card,
.stats-row,
.section-card {
  margin-left: 3.5rem;
  margin-right: 3.5rem;
}

/* ── Activity Selector ── */
.activity-selector-card {
  background: #fff8f5;
  border: 1px solid #ecddd5;
  border-top: none;
  padding: 1.25rem 2rem;
  margin-bottom: 0;
  display: flex; align-items: center; gap: 2rem;
}
.field-label {
  font-family: 'Space Mono', monospace;
  font-size: 0.65rem; letter-spacing: 0.18em;
  color: #e8775a; white-space: nowrap; flex-shrink: 0;
}
.activity-select {
  flex: 1;
  padding: 0.75rem 1.2rem;
  border: 1.5px solid #ddc8be;
  border-radius: 10px;
  font-family: 'Noto Sans TC', sans-serif;
  font-size: 0.95rem;
  background: #fff; color: #2d2320;
  cursor: pointer; outline: none;
  transition: border-color 0.2s;
  appearance: none;
}
.activity-select:focus { border-color: #e8775a; }

/* ── Stats ── */
.stats-row {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 1rem;
  margin-top: 2.5rem;
  margin-bottom: 2rem;
}
.stat-card {
  padding: 1.75rem 1.5rem;
  background: #ffffff;
  border-radius: 16px;
  border: 1.5px solid #ecddd5;
  position: relative;
  overflow: hidden;
}
/* soft color bar on left side */
.stat-card::after {
  content: '';
  position: absolute; top: 16px; bottom: 16px; left: 0;
  width: 4px; border-radius: 0 4px 4px 0;
  background: #ecddd5;
}
.stat-card.accent::after { background: #e8775a; }
.stat-card.warn::after   { background: #e8b86d; }
.stat-card.info::after   { background: #f2a4a4; }
.stat-card.rate::after   { background: #7dbfa0; }

.stat-num {
  font-family: 'Bebas Neue', sans-serif;
  font-size: 3.8rem; line-height: 1; letter-spacing: 0.02em;
  color: #2d2320;
}
.stat-card.accent .stat-num { color: #e8775a; }
.stat-card.warn   .stat-num { color: #d4943a; }
.stat-card.info   .stat-num { color: #c97272; }
.stat-card.rate   .stat-num { color: #5a9e80; }

.stat-label {
  font-family: 'Space Mono', monospace;
  font-size: 0.6rem; letter-spacing: 0.14em;
  color: #a08c84; margin-top: 0.35rem;
  text-transform: uppercase;
}
.progress-bar {
  height: 5px; background: #f0e6e0;
  border-radius: 10px; margin-top: 1.25rem;
}
.progress-fill {
  height: 100%; background: #7dbfa0;
  border-radius: 10px;
  transition: width 0.7s cubic-bezier(0.16,1,0.3,1);
}

/* ── Section Card ── */
.section-card {
  background: #ffffff;
  border: 1.5px solid #ecddd5;
  border-radius: 16px;
  padding: 2rem;
  margin-bottom: 2rem;
}
.section-header {
  display: flex; align-items: center; gap: 0.85rem;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 1.5px solid #f0e6e0;
}
.section-header h2 {
  font-family: 'Bebas Neue', sans-serif;
  font-size: 1.9rem; letter-spacing: 0.08em; margin: 0;
  color: #2d2320;
}
.badge {
  font-family: 'Space Mono', monospace;
  font-size: 0.58rem; letter-spacing: 0.1em;
  background: #fdf3f0; border: 1.5px solid #e8c4b4;
  color: #e8775a; padding: 0.25rem 0.8rem;
  border-radius: 20px;
}

/* ── Search Row ── */
.search-row {
  display: flex; align-items: center;
  margin-bottom: 1.25rem; flex-wrap: wrap; gap: 0.75rem;
}
.search-input {
  flex: 1; min-width: 240px;
  padding: 0.8rem 1.25rem;
  border: 1.5px solid #ddc8be; border-radius: 10px;
  font-family: 'Noto Sans TC', sans-serif;
  font-size: 0.95rem; outline: none;
  transition: border-color 0.2s;
  background: #fffaf8; color: #2d2320;
}
.search-input::placeholder { color: #c8b0a8; }
.search-input:focus { border-color: #e8775a; background: #fff; }

.filter-tabs { display: flex; gap: 0.5rem; flex-wrap: wrap; }
.filter-tab {
  font-family: 'Space Mono', monospace;
  font-size: 0.68rem; letter-spacing: 0.08em;
  padding: 0.7rem 1.1rem;
  border: 1.5px solid #ddc8be; border-radius: 20px;
  background: #fff8f5; cursor: pointer;
  transition: all 0.18s; color: #a08c84;
  white-space: nowrap;
}
.filter-tab.active {
  background: #e8775a; color: #fff;
  border-color: #e8775a;
  box-shadow: 0 2px 10px rgba(232,119,90,0.25);
}
.filter-tab:hover:not(.active) { border-color: #e8775a; color: #e8775a; background: #fdf3f0; }

/* ── Registration List ── */
.registration-list {
  display: flex; flex-direction: column;
  max-height: 540px; overflow-y: auto;
  border: 1.5px solid #ecddd5; border-radius: 12px;
  overflow: hidden;
}
.registration-list { overflow-y: auto; }
.registration-list::-webkit-scrollbar { width: 5px; }
.registration-list::-webkit-scrollbar-track { background: #faf8f5; }
.registration-list::-webkit-scrollbar-thumb { background: #ddc8be; border-radius: 10px; }
.registration-list::-webkit-scrollbar-thumb:hover { background: #e8775a; }

.loading-state, .empty-state {
  text-align: center; padding: 4rem;
  font-family: 'Space Mono', monospace;
  font-size: 0.78rem; letter-spacing: 0.12em; color: #c8b0a8;
}

.registration-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.1rem 1.5rem;
  border-bottom: 1px solid #f5ede8;
  background: #fff;
  transition: background 0.15s;
  border-left: 4px solid transparent;
}
.registration-row:last-child { border-bottom: none; }
.registration-row:hover { background: #fffaf7; }
.registration-row.checked {
  border-left-color: #7dbfa0;
  background: #f5fbf8;
}
.registration-row.late {
  border-left-color: #e8b86d;
  background: #fffbf3;
}

.reg-info { display: flex; flex-direction: column; gap: 4px; }
.reg-id {
  font-size: 1.05rem; font-weight: 700; color: #2d2320;
}
.reg-time, .check-time {
  font-family: 'Space Mono', monospace;
  font-size: 0.68rem; color: #b8a09a; letter-spacing: 0.04em;
}
.late-badge {
  font-family: 'Space Mono', monospace;
  font-size: 0.6rem; letter-spacing: 0.08em;
  background: #f5ddb0; color: #8a6020;
  padding: 2px 8px; margin-left: 8px;
  border-radius: 10px; font-weight: 700;
}

.reg-status { display: flex; align-items: center; gap: 0.9rem; }
.payment-badge {
  font-family: 'Space Mono', monospace;
  font-size: 0.62rem; letter-spacing: 0.06em;
  padding: 0.28rem 0.75rem; border-radius: 12px; font-weight: 600;
}
.payment-badge.paid    { background: #edf7f2; color: #4a8a6a; border: 1px solid #b8ddc8; }
.payment-badge.pending { background: #fdf5e8; color: #9a7030; border: 1px solid #e8d0a0; }
.payment-badge.free    { background: #f0eefa; color: #6a54a8; border: 1px solid #ccc0e8; }

.checkin-btn {
  font-family: 'Space Mono', monospace;
  font-size: 0.68rem; letter-spacing: 0.08em;
  background: #e8775a; color: #fff;
  border: none; border-radius: 20px;
  padding: 0.6rem 1.4rem;
  cursor: pointer; font-weight: 700;
  transition: all 0.2s;
  box-shadow: 0 2px 8px rgba(232,119,90,0.2);
}
.checkin-btn:hover:not(:disabled) { background: #d4654a; box-shadow: 0 3px 14px rgba(232,119,90,0.35); }
.checkin-btn:disabled { opacity: 0.35; cursor: not-allowed; box-shadow: none; }
.checked-badge {
  font-family: 'Space Mono', monospace;
  font-size: 0.68rem; letter-spacing: 0.08em;
  color: #5a9e80; font-weight: 700;
}

/* ── Empty Activity ── */
.empty-activity { text-align: center; padding: 6rem 2rem; }
.empty-icon { font-size: 3rem; margin-bottom: 1rem; opacity: 0.35; }
.empty-activity p {
  font-family: 'Space Mono', monospace;
  font-size: 0.78rem; letter-spacing: 0.12em; color: #c0a8a0;
}

/* ── Modal ── */
.modal-overlay {
  position: fixed; inset: 0;
  background: rgba(45,35,32,0.45);
  display: flex; align-items: center; justify-content: center;
  z-index: 1000; backdrop-filter: blur(6px);
}
.modal {
  background: #fff;
  width: 400px; overflow: hidden;
  border-radius: 20px;
  box-shadow: 0 24px 60px rgba(45,35,32,0.18);
  border: 1.5px solid #ecddd5;
}
.modal-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 1.5rem 2rem;
  background: #fff8f5;
  border-bottom: 1px solid #ecddd5;
}
.modal-header h3 {
  margin: 0;
  font-family: 'Bebas Neue', sans-serif;
  letter-spacing: 0.1em; font-size: 1.6rem; color: #2d2320;
}
.modal-close {
  background: none; border: none; font-size: 1.1rem;
  cursor: pointer; color: #c0a8a0; padding: 4px;
  transition: color 0.2s;
}
.modal-close:hover { color: #e8775a; }
.modal-body { padding: 2rem; text-align: center; }
.qr-activity-name {
  font-weight: 700; font-size: 1rem;
  margin-bottom: 1.5rem; color: #2d2320;
}
.qr-wrapper { display: flex; justify-content: center; margin-bottom: 1.25rem; }
.qr-image { border: 2px solid #ecddd5; border-radius: 12px; }
.qr-loading {
  width: 220px; height: 220px;
  display: flex; align-items: center; justify-content: center;
  background: #faf8f5; color: #c0a8a0; border-radius: 12px;
  font-family: 'Space Mono', monospace; font-size: 0.72rem;
}
.qr-hint {
  font-family: 'Space Mono', monospace;
  font-size: 0.68rem; letter-spacing: 0.1em;
  color: #b8a09a; margin-bottom: 0.75rem;
}
.qr-url-box {
  background: #faf8f5; border: 1px solid #ecddd5;
  border-radius: 8px; padding: 0.75rem 1rem;
  font-family: 'Space Mono', monospace;
  font-size: 0.62rem; color: #a08c84;
  word-break: break-all; text-align: left;
}

/* ── Toast ── */
.toast {
  position: fixed;
  bottom: 2.5rem; left: 50%; transform: translateX(-50%);
  padding: 0.9rem 2rem;
  font-family: 'Space Mono', monospace;
  font-size: 0.75rem; letter-spacing: 0.08em;
  font-weight: 700; z-index: 2000;
  animation: slideUp 0.3s cubic-bezier(0.16,1,0.3,1);
  white-space: nowrap; border-radius: 24px;
}
.toast.success { background: #2d2320; color: #fdf3f0; }
.toast.error   { background: #e8775a; color: #fff; }

@keyframes slideUp {
  from { opacity: 0; transform: translateX(-50%) translateY(12px); }
  to   { opacity: 1; transform: translateX(-50%) translateY(0); }
}

/* ── Responsive ── */
@media (max-width: 900px) {
  .stats-row { grid-template-columns: repeat(3, 1fr); }
}
@media (max-width: 768px) {
  .stats-row { grid-template-columns: repeat(2, 1fr); }
  .header { padding: 1.5rem; flex-direction: column; align-items: flex-start; gap: 1rem; }
  .activity-selector-card, .stats-row, .section-card { margin-left: 1rem; margin-right: 1rem; }
  .activity-selector-card { flex-direction: column; align-items: flex-start; }
}
</style>