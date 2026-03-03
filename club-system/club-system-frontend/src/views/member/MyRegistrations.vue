<template>
  <div class="my-registrations">
    <!-- Header -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-icon">🎭</div>
        <div>
          <h1 class="page-title">我的報名</h1>
          <p class="page-subtitle">追蹤你的所有活動報名狀態</p>
        </div>
      </div>
      <div class="header-stats" v-if="!loading && registrations.length > 0">
        <div class="stat-chip">
          <span class="stat-number">{{ registrations.length }}</span>
          <span class="stat-label">總報名</span>
        </div>
        <div class="stat-chip accent">
          <span class="stat-number">{{ activeCount }}</span>
          <span class="stat-label">進行中</span>
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

    <!-- Loading State -->
    <div v-if="loading" class="state-container">
      <div class="loading-wrapper">
        <div class="spinner"></div>
        <p class="loading-text">載入報名資料中...</p>
      </div>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="state-container">
      <div class="error-card">
        <div class="error-icon">⚠️</div>
        <p class="error-text">{{ error }}</p>
        <button class="retry-btn" @click="loadRegistrations">重新載入</button>
      </div>
    </div>

    <!-- Empty State -->
    <div v-else-if="registrations.length === 0" class="state-container">
      <div class="empty-card">
        <div class="empty-illustration">
          <div class="empty-circle"></div>
          <span class="empty-icon">🎪</span>
        </div>
        <h3 class="empty-title">還沒有報名紀錄</h3>
        <p class="empty-desc">快去探索社團活動，開始你的舞蹈之旅！</p>
        <router-link to="/activities" class="cta-btn">瀏覽活動</router-link>
      </div>
    </div>

    <!-- Registration List -->
    <div v-else class="registration-grid">
      <TransitionGroup name="card" tag="div" class="cards-container">
        <div
          v-for="(reg, index) in filteredRegistrations"
          :key="reg.id"
          class="registration-card"
          :style="{ '--delay': index * 0.05 + 's' }"
        >
          <!-- Card Top Line (status color) -->
          <div class="card-accent-line" :class="getStatusClass(reg.status)"></div>

          <!-- Card Header -->
          <div class="card-header">
            <div class="activity-id-badge">
              <span class="id-label">活動</span>
              <span class="id-value">#{{ reg.activityId }}</span>
            </div>
            <div class="status-badge" :class="getStatusClass(reg.status)">
              <span class="status-dot"></span>
              {{ getStatusLabel(reg.status) }}
            </div>
          </div>

          <!-- Card Body -->
          <div class="card-body">
            <div class="info-row">
              <div class="info-item">
                <span class="info-icon">📅</span>
                <div class="info-content">
                  <span class="info-label">報名日期</span>
                  <span class="info-value">{{ formatDate(reg.registrationTime) }}</span>
                </div>
              </div>
              <div class="info-item">
                <span class="info-icon">💰</span>
                <div class="info-content">
                  <span class="info-label">繳費狀態</span>
                  <span class="payment-badge" :class="getPaymentClass(reg.paymentStatus)">
                    {{ getPaymentLabel(reg.paymentStatus) }}
                  </span>
                </div>
              </div>
            </div>

            <div class="info-row" v-if="reg.paymentAmount > 0">
              <div class="info-item">
                <span class="info-icon">🏷️</span>
                <div class="info-content">
                  <span class="info-label">費用</span>
                  <span class="info-value amount">NT$ {{ reg.paymentAmount }}</span>
                </div>
              </div>
              <div class="info-item" v-if="reg.checkedIn">
                <span class="info-icon">✅</span>
                <div class="info-content">
                  <span class="info-label">簽到狀態</span>
                  <span class="info-value">
                    已簽到
                    <span v-if="reg.isLate" class="late-tag">遲到</span>
                  </span>
                </div>
              </div>
            </div>

            <div class="note-row" v-if="reg.note">
              <span class="info-icon">📝</span>
              <span class="note-text">{{ reg.note }}</span>
            </div>
          </div>

          <!-- Card Footer -->
          <div class="card-footer">
            <button
              v-if="canCancel(reg)"
              class="cancel-btn"
              @click="handleCancel(reg.id)"
              :disabled="cancellingId === reg.id"
            >
              {{ cancellingId === reg.id ? '取消中...' : '取消報名' }}
            </button>
            <span v-else class="cancelled-label" :class="{ 'is-cancelled': reg.status === 'CANCELLED' }">
              {{ reg.status === 'CANCELLED' ? '已取消' : '' }}
            </span>
          </div>
        </div>
      </TransitionGroup>

      <div v-if="filteredRegistrations.length === 0" class="no-filter-result">
        <span>此分類目前沒有報名紀錄</span>
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
const cancellingId = ref(null);
const activeTab = ref('ALL');

const tabs = [
  { label: '全部', value: 'ALL' },
  { label: '已報名', value: 'REGISTERED' },
  { label: '已出席', value: 'ATTENDED' },
  { label: '已取消', value: 'CANCELLED' },
];

const activeCount = computed(() =>
  registrations.value.filter(r => r.status === 'REGISTERED').length
);

const filteredRegistrations = computed(() => {
  if (activeTab.value === 'ALL') return registrations.value;
  return registrations.value.filter(r => r.status === activeTab.value);
});

const getTabCount = (tabValue) => {
  if (tabValue === 'ALL') return registrations.value.length;
  return registrations.value.filter(r => r.status === tabValue).length;
};

const getStatusLabel = (status) => {
  const map = {
    REGISTERED: '已報名',
    ATTENDED: '已出席',
    CANCELLED: '已取消',
    WAITLISTED: '候補中',
  };
  return map[status] || status;
};

const getStatusClass = (status) => {
  const map = {
    REGISTERED: 'status-registered',
    ATTENDED: 'status-attended',
    CANCELLED: 'status-cancelled',
    WAITLISTED: 'status-waitlisted',
  };
  return map[status] || '';
};

const getPaymentLabel = (paymentStatus) => {
  const map = {
    PENDING: '待繳費',
    PAID: '已繳費',
    NOT_REQUIRED: '免費',
    REFUNDED: '已退款',
  };
  return map[paymentStatus] || paymentStatus;
};

const getPaymentClass = (paymentStatus) => {
  const map = {
    PENDING: 'pay-pending',
    PAID: 'pay-paid',
    NOT_REQUIRED: 'pay-free',
    REFUNDED: 'pay-refunded',
  };
  return map[paymentStatus] || '';
};

const formatDate = (dateTime) => {
  if (!dateTime) return '-';
  const d = new Date(dateTime);
  return d.toLocaleDateString('zh-TW', { year: 'numeric', month: '2-digit', day: '2-digit' })
    + ' ' + d.toLocaleTimeString('zh-TW', { hour: '2-digit', minute: '2-digit' });
};

const canCancel = (reg) => {
  return reg.status === 'REGISTERED' && !reg.checkedIn;
};

const loadRegistrations = async () => {
  loading.value = true;
  error.value = null;
  try {
    const response = await registrationApi.getMyRegistrations(userStore.userId);
    registrations.value = response.data;
  } catch (err) {
    error.value = '載入報名資料時發生錯誤，請稍後再試。';
  } finally {
    loading.value = false;
  }
};

const handleCancel = async (registrationId) => {
  if (!confirm('確定要取消此報名嗎？')) return;
  cancellingId.value = registrationId;
  try {
    await registrationApi.cancel(registrationId);
    const reg = registrations.value.find(r => r.id === registrationId);
    if (reg) reg.status = 'CANCELLED';
  } catch (err) {
    alert('取消報名失敗，請稍後再試。');
  } finally {
    cancellingId.value = null;
  }
};

onMounted(() => {
  loadRegistrations();
});
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Noto+Sans+TC:wght@300;400;500;700&family=Space+Mono:wght@400;700&display=swap');

/* ===== Base ===== */
.my-registrations {
  min-height: 100vh;
  background: #f5f6fa;
  padding: 2rem 1.5rem 4rem;
  font-family: 'Noto Sans TC', sans-serif;
  color: #1e1f26;
  max-width: 960px;
  margin: 0 auto;
}

/* ===== Header ===== */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 2rem;
  flex-wrap: wrap;
  gap: 1rem;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.header-icon {
  width: 52px;
  height: 52px;
  background: linear-gradient(135deg, #6c63ff, #ff6584);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  box-shadow: 0 4px 20px rgba(108, 99, 255, 0.35);
}

.page-title {
  font-size: 1.6rem;
  font-weight: 700;
  margin: 0;
  background: linear-gradient(90deg, #1e1f26 40%, #6c63ff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.page-subtitle {
  margin: 0.2rem 0 0;
  font-size: 0.82rem;
  color: #9ca3af;
  letter-spacing: 0.03em;
}

.header-stats {
  display: flex;
  gap: 0.75rem;
}

.stat-chip {
  background: #ffffff;
  border: 1px solid #e2e4ed;
  border-radius: 10px;
  padding: 0.5rem 1rem;
  text-align: center;
  min-width: 64px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}

.stat-chip.accent {
  border-color: #6c63ff44;
  background: rgba(108, 99, 255, 0.06);
}

.stat-number {
  display: block;
  font-family: 'Space Mono', monospace;
  font-size: 1.3rem;
  font-weight: 700;
  color: #1e1f26;
  line-height: 1;
}

.stat-chip.accent .stat-number { color: #6c63ff; }

.stat-label {
  display: block;
  font-size: 0.68rem;
  color: #9ca3af;
  margin-top: 0.2rem;
}

/* ===== Filter Tabs ===== */
.filter-tabs {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1.75rem;
  flex-wrap: wrap;
}

.tab-btn {
  background: #ffffff;
  border: 1px solid #e2e4ed;
  border-radius: 8px;
  padding: 0.45rem 0.9rem;
  color: #6b7280;
  font-size: 0.82rem;
  font-family: 'Noto Sans TC', sans-serif;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 0.4rem;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}

.tab-btn:hover {
  background: #f0f1f8;
  color: #374151;
}

.tab-btn.active {
  background: linear-gradient(135deg, #ede9ff, #fce7ec);
  border-color: #6c63ff66;
  color: #6c63ff;
}

.tab-count {
  background: #f0f1f5;
  border-radius: 20px;
  padding: 0 6px;
  font-size: 0.7rem;
  font-family: 'Space Mono', monospace;
  color: #9ca3af;
}

.tab-btn.active .tab-count {
  background: rgba(108, 99, 255, 0.15);
  color: #6c63ff;
}

/* ===== State Containers ===== */
.state-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
}

.loading-wrapper {
  text-align: center;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #e2e4ed;
  border-top-color: #6c63ff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-text {
  color: #9ca3af;
  font-size: 0.9rem;
}

.error-card {
  text-align: center;
  padding: 2rem;
  background: #fff;
  border: 1px solid #fecaca;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}

.error-icon { font-size: 2.5rem; margin-bottom: 0.75rem; }
.error-text { color: #ef4444; margin-bottom: 1rem; font-size: 0.9rem; }

.retry-btn {
  background: transparent;
  border: 1px solid #6c63ff;
  color: #6c63ff;
  padding: 0.5rem 1.25rem;
  border-radius: 8px;
  cursor: pointer;
  font-family: 'Noto Sans TC', sans-serif;
  transition: all 0.2s;
}

.retry-btn:hover { background: rgba(108, 99, 255, 0.08); }

.empty-card {
  text-align: center;
  padding: 3rem 2rem;
}

.empty-illustration {
  position: relative;
  width: 80px;
  height: 80px;
  margin: 0 auto 1.5rem;
}

.empty-circle {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #f0f1f8, #ede9ff);
  border: 2px dashed #c4beff;
}

.empty-icon {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 2rem;
}

.empty-title {
  font-size: 1.1rem;
  font-weight: 600;
  margin: 0 0 0.5rem;
  color: #374151;
}

.empty-desc {
  font-size: 0.85rem;
  color: #9ca3af;
  margin: 0 0 1.5rem;
}

.cta-btn {
  display: inline-block;
  background: linear-gradient(135deg, #6c63ff, #8b5cf6);
  color: #fff;
  padding: 0.6rem 1.5rem;
  border-radius: 10px;
  text-decoration: none;
  font-size: 0.85rem;
  font-weight: 500;
  transition: all 0.2s;
  box-shadow: 0 4px 15px rgba(108, 99, 255, 0.3);
}

.cta-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(108, 99, 255, 0.45);
}

/* ===== Cards ===== */
.cards-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1rem;
}

.registration-card {
  background: #ffffff;
  border: 1px solid #e2e4ed;
  border-radius: 14px;
  overflow: hidden;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
  animation: cardIn 0.4s ease both;
  animation-delay: var(--delay, 0s);
  position: relative;
}

.registration-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 28px rgba(108, 99, 255, 0.12);
  border-color: #c4beff;
}

@keyframes cardIn {
  from { opacity: 0; transform: translateY(16px); }
  to   { opacity: 1; transform: translateY(0); }
}

/* Card accent top line */
.card-accent-line {
  height: 3px;
  width: 100%;
}

.status-registered .card-accent-line,
.card-accent-line.status-registered { background: linear-gradient(90deg, #6c63ff, #8b5cf6); }
.status-attended .card-accent-line,
.card-accent-line.status-attended   { background: linear-gradient(90deg, #10b981, #34d399); }
.status-cancelled .card-accent-line,
.card-accent-line.status-cancelled  { background: linear-gradient(90deg, #6b7280, #9ca3af); }
.status-waitlisted .card-accent-line,
.card-accent-line.status-waitlisted { background: linear-gradient(90deg, #f59e0b, #fbbf24); }

/* Card Header */
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem 1rem 0.75rem;
}

.activity-id-badge {
  display: flex;
  align-items: center;
  gap: 0.3rem;
  background: #f5f6fa;
  border: 1px solid #e2e4ed;
  border-radius: 6px;
  padding: 0.25rem 0.6rem;
}

.id-label {
  font-size: 0.65rem;
  color: #9ca3af;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.id-value {
  font-family: 'Space Mono', monospace;
  font-size: 0.78rem;
  color: #374151;
  font-weight: 700;
}

.status-badge {
  display: flex;
  align-items: center;
  gap: 0.35rem;
  padding: 0.25rem 0.7rem;
  border-radius: 20px;
  font-size: 0.72rem;
  font-weight: 500;
  letter-spacing: 0.02em;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  display: block;
}

.status-badge.status-registered {
  background: #ede9ff;
  color: #6c63ff;
}
.status-badge.status-registered .status-dot { background: #6c63ff; }

.status-badge.status-attended {
  background: #d1fae5;
  color: #059669;
}
.status-badge.status-attended .status-dot { background: #10b981; }

.status-badge.status-cancelled {
  background: #f3f4f6;
  color: #6b7280;
}
.status-badge.status-cancelled .status-dot { background: #9ca3af; }

.status-badge.status-waitlisted {
  background: #fef3c7;
  color: #d97706;
}
.status-badge.status-waitlisted .status-dot { background: #f59e0b; }

/* Card Body */
.card-body {
  padding: 0 1rem 0.75rem;
}

.info-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.75rem;
  margin-bottom: 0.75rem;
}

.info-item {
  display: flex;
  align-items: flex-start;
  gap: 0.5rem;
}

.info-icon {
  font-size: 0.9rem;
  margin-top: 2px;
  flex-shrink: 0;
}

.info-content {
  display: flex;
  flex-direction: column;
  gap: 0.15rem;
}

.info-label {
  font-size: 0.65rem;
  color: #9ca3af;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.info-value {
  font-size: 0.8rem;
  color: #374151;
  font-weight: 500;
}

.info-value.amount {
  font-family: 'Space Mono', monospace;
  color: #d97706;
  font-size: 0.75rem;
}

.payment-badge {
  font-size: 0.72rem;
  padding: 0.15rem 0.5rem;
  border-radius: 4px;
  font-weight: 500;
  display: inline-block;
}

.pay-pending  { background: #fef3c7; color: #d97706; }
.pay-paid     { background: #d1fae5; color: #059669; }
.pay-free     { background: #ede9ff; color: #7c3aed; }
.pay-refunded { background: #f3f4f6; color: #6b7280; }

.late-tag {
  background: #fee2e2;
  color: #dc2626;
  font-size: 0.65rem;
  padding: 0.1rem 0.4rem;
  border-radius: 4px;
  margin-left: 0.35rem;
}

.note-row {
  display: flex;
  align-items: flex-start;
  gap: 0.5rem;
  background: #f9fafb;
  border-radius: 8px;
  padding: 0.5rem 0.75rem;
  margin-top: 0.25rem;
  border: 1px solid #e2e4ed;
}

.note-text {
  font-size: 0.78rem;
  color: #6b7280;
  line-height: 1.4;
}

/* Card Footer */
.card-footer {
  padding: 0.6rem 1rem 0.9rem;
  display: flex;
  justify-content: flex-end;
}

.cancel-btn {
  background: transparent;
  border: 1px solid #fca5a5;
  color: #ef4444;
  padding: 0.35rem 0.85rem;
  border-radius: 7px;
  font-size: 0.75rem;
  cursor: pointer;
  font-family: 'Noto Sans TC', sans-serif;
  transition: all 0.2s;
}

.cancel-btn:hover:not(:disabled) {
  background: #fee2e2;
  border-color: #ef4444;
}

.cancel-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.cancelled-label {
  font-size: 0.72rem;
  color: #9ca3af;
}

/* ===== No filter result ===== */
.no-filter-result {
  grid-column: 1 / -1;
  text-align: center;
  color: #9ca3af;
  font-size: 0.85rem;
  padding: 2rem 0;
}

/* ===== Transition ===== */
.card-enter-active,
.card-leave-active { transition: all 0.3s ease; }
.card-enter-from,
.card-leave-to { opacity: 0; transform: translateY(12px); }

/* ===== Responsive ===== */
@media (max-width: 560px) {
  .my-registrations { padding: 1.5rem 1rem 3rem; }
  .cards-container { grid-template-columns: 1fr; }
  .page-header { flex-direction: column; align-items: flex-start; }
  .info-row { grid-template-columns: 1fr; }
}
</style>