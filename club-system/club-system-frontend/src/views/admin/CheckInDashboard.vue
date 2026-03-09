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
/* ===== Design: Industrial Utility ===== */
@import url('https://fonts.googleapis.com/css2?family=IBM+Plex+Mono:wght@400;600&family=IBM+Plex+Sans+TC:wght@400;500;600;700&display=swap');

* { box-sizing: border-box; }

.dashboard-container {
  min-height: 100vh;
  background: #f0f0ec;
  font-family: 'IBM Plex Sans TC', sans-serif;
  padding: 28px 32px;
  color: #1a1a1a;
}

/* Header */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.header-left { display: flex; align-items: center; gap: 16px; }
.back-btn {
  background: none;
  border: 2px solid #1a1a1a;
  padding: 8px 14px;
  border-radius: 6px;
  cursor: pointer;
  font-family: inherit;
  font-size: 0.85rem;
  font-weight: 600;
  transition: background 0.2s;
}
.back-btn:hover { background: #1a1a1a; color: #f0f0ec; }
h1 { font-size: 1.7rem; font-weight: 700; margin: 0; letter-spacing: -0.5px; }
.subtitle { color: #666; font-size: 0.9rem; margin: 2px 0 0; }

.qr-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #1a1a1a;
  color: #f0f0ec;
  border: none;
  padding: 10px 20px;
  border-radius: 8px;
  cursor: pointer;
  font-family: inherit;
  font-weight: 600;
  font-size: 0.9rem;
  transition: opacity 0.2s;
}
.qr-btn:disabled { opacity: 0.4; cursor: not-allowed; }
.qr-btn:hover:not(:disabled) { opacity: 0.85; }

/* Activity Selector */
.activity-selector-card {
  background: white;
  border: 1px solid #e0e0d8;
  border-radius: 10px;
  padding: 18px 22px;
  margin-bottom: 20px;
}
.field-label { font-size: 0.8rem; font-weight: 600; color: #888; text-transform: uppercase; letter-spacing: 0.5px; display: block; margin-bottom: 8px; }
.activity-select {
  width: 100%;
  padding: 10px 14px;
  border: 2px solid #e0e0d8;
  border-radius: 8px;
  font-family: inherit;
  font-size: 0.95rem;
  background: #fafaf8;
  cursor: pointer;
  outline: none;
  transition: border-color 0.2s;
}
.activity-select:focus { border-color: #1a1a1a; }

/* Stats */
.stats-row {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 14px;
  margin-bottom: 20px;
}
.stat-card {
  background: white;
  border: 1px solid #e0e0d8;
  border-radius: 10px;
  padding: 18px 16px;
  text-align: center;
}
.stat-card.accent { background: #1a1a1a; color: white; border-color: #1a1a1a; }
.stat-card.warn { background: #fff8e1; border-color: #ffe082; }
.stat-card.info { background: #e3f2fd; border-color: #90caf9; }
.stat-card.rate { text-align: left; }
.stat-num { font-size: 2rem; font-weight: 700; font-family: 'IBM Plex Mono', monospace; line-height: 1; }
.stat-label { font-size: 0.78rem; color: inherit; opacity: 0.65; margin-top: 5px; font-weight: 500; }
.stat-card.accent .stat-label { opacity: 0.75; }
.progress-bar { height: 4px; background: #e0e0d8; border-radius: 2px; margin-top: 10px; }
.progress-fill { height: 100%; background: #1a1a1a; border-radius: 2px; transition: width 0.5s ease; }

/* Section */
.section-card {
  background: white;
  border: 1px solid #e0e0d8;
  border-radius: 10px;
  padding: 22px;
  margin-bottom: 20px;
}
.section-header { display: flex; align-items: center; gap: 12px; margin-bottom: 18px; }
.section-header h2 { font-size: 1.1rem; font-weight: 700; margin: 0; }
.badge {
  background: #f0f0ec;
  border: 1px solid #d0d0c8;
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 600;
  color: #666;
}

/* Search */
.search-row { display: flex; gap: 12px; align-items: center; margin-bottom: 16px; flex-wrap: wrap; }
.search-input {
  flex: 1;
  min-width: 200px;
  padding: 10px 14px;
  border: 2px solid #e0e0d8;
  border-radius: 8px;
  font-family: inherit;
  font-size: 0.9rem;
  outline: none;
  transition: border-color 0.2s;
}
.search-input:focus { border-color: #1a1a1a; }
.filter-tabs { display: flex; gap: 6px; }
.filter-tab {
  padding: 7px 14px;
  border: 2px solid #e0e0d8;
  border-radius: 6px;
  background: white;
  font-family: inherit;
  font-size: 0.82rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  color: #666;
}
.filter-tab.active, .filter-tab:hover { background: #1a1a1a; color: white; border-color: #1a1a1a; }

/* Registration List */
.registration-list { display: flex; flex-direction: column; gap: 8px; max-height: 420px; overflow-y: auto; }
.loading-state, .empty-state { text-align: center; padding: 40px; color: #aaa; font-size: 0.9rem; }

.registration-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  border: 1px solid #e0e0d8;
  border-radius: 8px;
  transition: border-color 0.2s;
  background: #fafaf8;
}
.registration-row.checked { background: #f1f8f4; border-color: #a5d6a7; }
.registration-row.late { border-left: 3px solid #ff9800; }

.reg-info { display: flex; flex-direction: column; gap: 3px; }
.reg-id { font-weight: 700; font-family: 'IBM Plex Mono', monospace; font-size: 0.95rem; }
.reg-time, .check-time { font-size: 0.78rem; color: #888; }
.late-badge {
  background: #ff9800;
  color: white;
  font-size: 0.7rem;
  padding: 1px 6px;
  border-radius: 4px;
  margin-left: 6px;
  font-weight: 600;
}

.reg-status { display: flex; align-items: center; gap: 10px; }
.payment-badge {
  font-size: 0.75rem;
  padding: 3px 9px;
  border-radius: 5px;
  font-weight: 600;
}
.payment-badge.paid { background: #e8f5e9; color: #2e7d32; }
.payment-badge.pending { background: #fff8e1; color: #f57f17; }
.payment-badge.free { background: #e3f2fd; color: #1565c0; }

.checkin-btn {
  background: #2e7d32;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 7px;
  cursor: pointer;
  font-family: inherit;
  font-weight: 700;
  font-size: 0.85rem;
  transition: opacity 0.2s;
}
.checkin-btn:hover:not(:disabled) { opacity: 0.85; }
.checkin-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.checked-badge { color: #2e7d32; font-weight: 700; font-size: 0.85rem; }

/* Empty Activity */
.empty-activity { text-align: center; padding: 80px; }
.empty-icon { font-size: 3rem; margin-bottom: 12px; }
.empty-activity p { color: #aaa; }

/* Modal */
.modal-overlay {
  position: fixed; inset: 0;
  background: rgba(0,0,0,0.5);
  display: flex; align-items: center; justify-content: center;
  z-index: 1000;
}
.modal {
  background: white;
  border-radius: 14px;
  width: 340px;
  overflow: hidden;
  box-shadow: 0 20px 40px rgba(0,0,0,0.15);
}
.modal-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 18px 22px;
  border-bottom: 1px solid #e0e0d8;
}
.modal-header h3 { margin: 0; font-size: 1rem; font-weight: 700; }
.modal-close {
  background: none; border: none; font-size: 1.1rem;
  cursor: pointer; color: #888; padding: 4px;
}
.modal-body { padding: 24px; text-align: center; }
.qr-activity-name { font-weight: 700; margin-bottom: 16px; font-size: 0.95rem; }
.qr-wrapper { display: flex; justify-content: center; margin-bottom: 14px; }
.qr-image { border: 3px solid #1a1a1a; border-radius: 10px; }
.qr-loading { width: 220px; height: 220px; display: flex; align-items: center; justify-content: center; background: #f0f0ec; border-radius: 10px; color: #888; }
.qr-hint { font-size: 0.82rem; color: #888; margin-bottom: 12px; }
.qr-url-box {
  background: #f0f0ec;
  border: 1px solid #e0e0d8;
  border-radius: 6px;
  padding: 8px 12px;
  font-family: 'IBM Plex Mono', monospace;
  font-size: 0.72rem;
  color: #555;
  word-break: break-all;
}

/* Toast */
.toast {
  position: fixed;
  bottom: 30px; left: 50%; transform: translateX(-50%);
  padding: 12px 24px;
  border-radius: 8px;
  font-weight: 600;
  font-size: 0.9rem;
  z-index: 2000;
  animation: slideUp 0.3s ease;
}
.toast.success { background: #1a1a1a; color: white; }
.toast.error { background: #d32f2f; color: white; }

@keyframes slideUp {
  from { opacity: 0; transform: translateX(-50%) translateY(10px); }
  to { opacity: 1; transform: translateX(-50%) translateY(0); }
}

@media (max-width: 768px) {
  .stats-row { grid-template-columns: repeat(2, 1fr); }
  .dashboard-container { padding: 16px; }
  .header { flex-direction: column; align-items: flex-start; gap: 12px; }
}
</style>