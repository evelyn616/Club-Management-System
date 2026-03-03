<template>
  <div class="checkin-page">
    <!-- Loading State -->
    <div class="status-screen" v-if="state === 'loading'">
      <div class="spinner"></div>
      <p>正在驗證您的簽到資訊...</p>
    </div>

    <!-- Success State -->
    <div class="status-screen success" v-else-if="state === 'success'">
      <div class="check-circle">✓</div>
      <h2>簽到成功！</h2>
      <p class="activity-name">{{ activityInfo.title }}</p>
      <div class="info-grid">
        <div class="info-item">
          <span class="info-label">簽到時間</span>
          <span class="info-val">{{ formatTime(checkInTime) }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">狀態</span>
          <span class="info-val" :class="isLate ? 'late' : 'ontime'">{{ isLate ? '⚠ 遲到' : '✓ 準時' }}</span>
        </div>
      </div>
    </div>

    <!-- Already Checked In -->
    <div class="status-screen already" v-else-if="state === 'already'">
      <div class="info-circle">!</div>
      <h2>您已簽到過</h2>
      <p>本場活動無需重複簽到</p>
      <p class="activity-name">{{ activityInfo.title }}</p>
    </div>

    <!-- Error State -->
    <div class="status-screen error" v-else-if="state === 'error'">
      <div class="x-circle">✕</div>
      <h2>簽到失敗</h2>
      <p class="error-msg">{{ errorMessage }}</p>
      <button class="retry-btn" @click="tryCheckIn">重試</button>
    </div>

    <!-- Initial / Manual Mode -->
    <div class="manual-page" v-else-if="state === 'manual'">
      <div class="manual-card">
        <div class="logo-mark">📍</div>
        <h2>活動簽到</h2>
        <p class="manual-hint">請輸入您的會員 ID 進行簽到</p>

        <div class="activity-info-box" v-if="activityInfo.title">
          <span class="ai-label">活動</span>
          <span class="ai-title">{{ activityInfo.title }}</span>
        </div>

        <div class="form-group">
          <label>會員 ID</label>
          <input
            v-model="manualUserId"
            placeholder="例：M001"
            class="id-input"
            @keyup.enter="doManualCheckIn"
            autofocus
          />
        </div>
        <button class="checkin-action-btn" @click="doManualCheckIn" :disabled="!manualUserId || submitting">
          {{ submitting ? '處理中...' : '確認簽到' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { registrationApi } from '@/api/registration';
import { activityApi } from '@/api/activity';
import { useUserStore } from '@/stores/user';

const route = useRoute();
const userStore = useUserStore();

const state = ref('loading'); // loading | success | already | error | manual
const errorMessage = ref('');
const checkInTime = ref(null);
const isLate = ref(false);
const submitting = ref(false);
const manualUserId = ref('');

const activityId = route.query.activityId;
const activityInfo = ref({ title: '', startTime: '' });

async function loadActivity() {
  if (!activityId) return;
  try {
    const res = await activityApi.getActivityDetails(activityId);
    activityInfo.value = res.data;
  } catch (e) { /* ignore */ }
}

async function tryCheckIn() {
  const userId = userStore.userId;
  if (!userId) {
    // 沒有登入 → 進入手動輸入模式
    state.value = 'manual';
    return;
  }
  state.value = 'loading';
  await doCheckInForUser(userId);
}

async function doManualCheckIn() {
  if (!manualUserId.value || submitting.value) return;
  submitting.value = true;
  await doCheckInForUser(manualUserId.value);
  submitting.value = false;
}

async function doCheckInForUser(userId) {
  try {
    // 1. 找到此用戶對應此活動的 registration id
    const regsRes = await registrationApi.getMyRegistrations(userId);
    const regs = regsRes.data || [];
    const reg = regs.find(r => String(r.activityId) === String(activityId));

    if (!reg) {
      state.value = 'error';
      errorMessage.value = '查無此活動的報名紀錄，請確認是否已完成報名';
      return;
    }

    if (reg.checkedIn) {
      state.value = 'already';
      return;
    }

    // 2. 執行簽到
    const res = await registrationApi.checkIn(reg.id);
    checkInTime.value = res.data.checkInTime || new Date().toISOString();
    isLate.value = res.data.isLate || false;
    state.value = 'success';

  } catch (e) {
    state.value = 'error';
    errorMessage.value = e.response?.data?.message || '簽到過程發生錯誤，請聯繫管理員';
  }
}

function formatTime(dateStr) {
  if (!dateStr) return '-';
  return new Date(dateStr).toLocaleTimeString('zh-TW', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
}

onMounted(async () => {
  await loadActivity();
  if (!activityId) {
    state.value = 'error';
    errorMessage.value = '無效的簽到連結，請重新掃描 QR Code';
    return;
  }
  await tryCheckIn();
});
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=IBM+Plex+Mono:wght@600&family=Noto+Sans+TC:wght@400;500;700;900&display=swap');

.checkin-page {
  min-height: 100vh;
  background: #0e0e0e;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: 'Noto Sans TC', sans-serif;
  padding: 20px;
}

/* Status Screens */
.status-screen {
  text-align: center;
  padding: 48px 32px;
  max-width: 360px;
  width: 100%;
  animation: fadeIn 0.4s ease;
}

.status-screen h2 {
  font-size: 1.6rem;
  font-weight: 900;
  margin: 20px 0 10px;
  color: white;
}

.status-screen p {
  color: #aaa;
  font-size: 0.95rem;
}

.activity-name {
  color: #e0e0e0 !important;
  font-weight: 700;
  font-size: 1.05rem !important;
  margin-top: 12px;
}

/* Icons */
.check-circle, .info-circle, .x-circle {
  width: 90px; height: 90px;
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 2.5rem;
  font-weight: 900;
  margin: 0 auto;
}
.check-circle { background: #1b5e20; color: #69f0ae; border: 3px solid #69f0ae; }
.info-circle { background: #1a237e; color: #82b1ff; border: 3px solid #82b1ff; font-size: 2rem; }
.x-circle { background: #b71c1c; color: #ff8a80; border: 3px solid #ff8a80; }

/* Info Grid (success) */
.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-top: 24px;
}
.info-item {
  background: #1c1c1c;
  border: 1px solid #2a2a2a;
  border-radius: 10px;
  padding: 14px;
}
.info-label { display: block; font-size: 0.72rem; color: #666; margin-bottom: 6px; text-transform: uppercase; letter-spacing: 0.5px; }
.info-val { font-weight: 700; font-size: 0.95rem; color: white; font-family: 'IBM Plex Mono', monospace; }
.info-val.late { color: #ff9800; }
.info-val.ontime { color: #69f0ae; }

/* Spinner */
.spinner {
  width: 48px; height: 48px;
  border: 4px solid #2a2a2a;
  border-top-color: #69f0ae;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin: 0 auto 20px;
}

/* Error */
.error-msg { color: #ff8a80 !important; font-weight: 500; margin: 10px 0 20px; }
.retry-btn {
  background: #333;
  color: white;
  border: 1px solid #555;
  padding: 10px 24px;
  border-radius: 8px;
  cursor: pointer;
  font-family: inherit;
  font-size: 0.9rem;
  font-weight: 600;
  transition: background 0.2s;
}
.retry-btn:hover { background: #444; }

/* Manual Card */
.manual-card {
  background: #161616;
  border: 1px solid #2a2a2a;
  border-radius: 20px;
  padding: 40px 32px;
  max-width: 380px;
  width: 100%;
  text-align: center;
  animation: fadeIn 0.4s ease;
}
.logo-mark { font-size: 2.5rem; margin-bottom: 12px; }
.manual-card h2 { font-size: 1.5rem; font-weight: 900; color: white; margin-bottom: 6px; }
.manual-hint { color: #888; font-size: 0.85rem; margin-bottom: 24px; }

.activity-info-box {
  background: #1e1e1e;
  border: 1px solid #2a2a2a;
  border-radius: 10px;
  padding: 12px 16px;
  margin-bottom: 20px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  text-align: left;
}
.ai-label { font-size: 0.72rem; color: #666; text-transform: uppercase; letter-spacing: 0.5px; }
.ai-title { color: #e0e0e0; font-weight: 700; font-size: 0.95rem; }

.form-group { margin-bottom: 16px; text-align: left; }
.form-group label { display: block; font-size: 0.8rem; color: #888; margin-bottom: 8px; font-weight: 600; }
.id-input {
  width: 100%;
  background: #1e1e1e;
  border: 2px solid #2a2a2a;
  border-radius: 10px;
  padding: 12px 16px;
  color: white;
  font-family: 'IBM Plex Mono', monospace;
  font-size: 1rem;
  outline: none;
  transition: border-color 0.2s;
}
.id-input:focus { border-color: #69f0ae; }
.id-input::placeholder { color: #444; }

.checkin-action-btn {
  width: 100%;
  background: #69f0ae;
  color: #0e0e0e;
  border: none;
  padding: 14px;
  border-radius: 10px;
  font-family: inherit;
  font-size: 1rem;
  font-weight: 900;
  cursor: pointer;
  transition: opacity 0.2s;
}
.checkin-action-btn:hover:not(:disabled) { opacity: 0.9; }
.checkin-action-btn:disabled { opacity: 0.4; cursor: not-allowed; }

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}
@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>