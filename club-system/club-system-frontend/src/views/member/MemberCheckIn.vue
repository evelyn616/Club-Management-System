<template>
  <div class="ci-page">

    <!-- ── Loading ── -->
    <div class="state-screen" v-if="state === 'loading'">
      <div class="loading-bars">
        <span v-for="i in 5" :key="i" :style="{ animationDelay: (i * 0.1) + 's' }"></span>
      </div>
      <p class="state-label mono">正在驗證簽到資訊...</p>
    </div>

    <!-- ── Success ── -->
    <div class="state-screen" v-else-if="state === 'success'">
      <div class="icon-circle success">
        <span class="icon-sym">✓</span>
      </div>
      <div class="eyebrow"><span class="eyebrow-line"></span><span class="eyebrow-text">CHECK-IN SUCCESS</span></div>
      <h1 class="state-title">簽到<span class="title-accent">成功</span></h1>
      <p class="activity-name">{{ activityInfo.title }}</p>
      <div class="info-cards">
        <div class="info-card">
          <span class="ic-label mono">簽到時間</span>
          <span class="ic-val mono">{{ formatTime(checkInTime) }}</span>
        </div>
        <div class="info-card" :class="isLate ? 'ic-warn' : 'ic-ok'">
          <span class="ic-label mono">狀態</span>
          <span class="ic-val mono">{{ isLate ? '⚠ 遲到' : '✓ 準時' }}</span>
        </div>
      </div>
      <p class="done-hint mono">您已完成本場活動簽到</p>
    </div>

    <!-- ── Already Checked In ── -->
    <div class="state-screen" v-else-if="state === 'already'">
      <div class="icon-circle already">
        <span class="icon-sym">!</span>
      </div>
      <div class="eyebrow"><span class="eyebrow-line"></span><span class="eyebrow-text">ALREADY CHECKED IN</span></div>
      <h1 class="state-title">已<span class="title-accent">簽到</span></h1>
      <p class="activity-name">{{ activityInfo.title }}</p>
      <p class="done-hint mono">本場活動您已完成簽到，無需重複操作</p>
    </div>

    <!-- ── Error ── -->
    <div class="state-screen" v-else-if="state === 'error'">
      <div class="icon-circle error">
        <span class="icon-sym">✕</span>
      </div>
      <div class="eyebrow"><span class="eyebrow-line"></span><span class="eyebrow-text">CHECK-IN FAILED</span></div>
      <h1 class="state-title">簽到<span class="title-error">失敗</span></h1>
      <p class="error-msg">{{ errorMessage }}</p>
      <button class="retry-btn" @click="tryCheckIn">重新嘗試 →</button>
    </div>

    <!-- ── Manual (not logged in) ── -->
    <div class="manual-screen" v-else-if="state === 'manual'">

      <!-- Brand -->
      <div class="brand-area">
        <div class="eyebrow"><span class="eyebrow-line"></span><span class="eyebrow-text">MEMBER CHECK-IN</span></div>
        <h1 class="brand-title">活動<span class="title-accent">簽到</span></h1>
      </div>

      <!-- Activity Info -->
      <div class="activity-card" v-if="activityInfo.title">
        <span class="ac-label mono">ACTIVITY</span>
        <span class="ac-title">{{ activityInfo.title }}</span>
        <span class="ac-time mono" v-if="activityInfo.startTime">
          {{ formatDateTime(activityInfo.startTime) }}
        </span>
      </div>

      <!-- Input Form -->
      <div class="form-card">
        <label class="form-label mono">MEMBER ID</label>
        <div class="input-wrap">
          <input
            v-model="manualUserId"
            placeholder="例：M001"
            class="id-input mono"
            @keyup.enter="doManualCheckIn"
            autofocus
          />
        </div>
        <button
          class="submit-btn"
          @click="doManualCheckIn"
          :disabled="!manualUserId || submitting"
        >
          <span v-if="submitting" class="btn-loading">
            <span></span><span></span><span></span>
          </span>
          <span v-else>確認簽到 →</span>
        </button>
      </div>

      <p class="manual-hint mono">請輸入您的會員 ID 完成簽到</p>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { registrationApi } from '@/api/registration'
import { activityApi } from '@/api/activity'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const userStore = useUserStore()

const state = ref('loading')
const errorMessage = ref('')
const checkInTime = ref(null)
const isLate = ref(false)
const submitting = ref(false)
const manualUserId = ref('')

const activityId = route.query.activityId
const activityInfo = ref({ title: '', startTime: '' })

async function loadActivity() {
  if (!activityId) return
  try {
    const res = await activityApi.getActivityDetails(activityId)
    activityInfo.value = res.data
  } catch (e) { /* ignore */ }
}

async function tryCheckIn() {
  const userId = userStore.userId
  if (!userId) {
    state.value = 'manual'
    return
  }
  state.value = 'loading'
  await doCheckInForUser(userId)
}

async function doManualCheckIn() {
  if (!manualUserId.value || submitting.value) return
  submitting.value = true
  await doCheckInForUser(manualUserId.value)
  submitting.value = false
}

async function doCheckInForUser(userId) {
  try {
    const regsRes = await registrationApi.getMyRegistrations(userId)
    const regs = regsRes.data || []
    const reg = regs.find(r => String(r.activityId) === String(activityId))

    if (!reg) {
      state.value = 'error'
      errorMessage.value = '查無此活動的報名紀錄，請確認是否已完成報名'
      return
    }
    if (reg.checkedIn) {
      state.value = 'already'
      return
    }

    const res = await registrationApi.checkIn(reg.id)
    checkInTime.value = res.data.checkInTime || new Date().toISOString()
    isLate.value = res.data.isLate || false
    state.value = 'success'

  } catch (e) {
    state.value = 'error'
    errorMessage.value = e.response?.data?.message || '簽到過程發生錯誤，請聯繫管理員'
  }
}

function formatTime(dateStr) {
  if (!dateStr) return '—'
  return new Date(dateStr).toLocaleTimeString('zh-TW', {
    hour: '2-digit', minute: '2-digit', second: '2-digit'
  })
}

function formatDateTime(dateStr) {
  if (!dateStr) return '—'
  const d = new Date(dateStr)
  return `${d.getFullYear()}/${String(d.getMonth()+1).padStart(2,'0')}/${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
}

onMounted(async () => {
  await loadActivity()
  if (!activityId) {
    state.value = 'error'
    errorMessage.value = '無效的簽到連結，請重新掃描 QR Code'
    return
  }
  await tryCheckIn()
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Bebas+Neue&family=Space+Mono:wght@400;700&family=Noto+Sans+TC:wght@300;400;500;700&display=swap');

* { box-sizing: border-box; }

.ci-page {
  min-height: 100vh;
  background: #ffffff;
  display: flex; align-items: center; justify-content: center;
  font-family: 'Noto Sans TC', sans-serif;
  color: #0a0a0a;
  padding: 2rem 1.25rem;
}
.mono { font-family: 'Space Mono', monospace; }

/* ── Eyebrow ── */
.eyebrow { display: flex; align-items: center; gap: 0.6rem; margin-bottom: 0.5rem; justify-content: center; }
.eyebrow-line { display: block; width: 20px; height: 2px; background: #ff2d6b; border-radius: 2px; }
.eyebrow-text { font-family: 'Space Mono', monospace; font-size: 0.58rem; letter-spacing: 0.2em; color: #ff2d6b; }

/* ── State Screens (shared) ── */
.state-screen {
  text-align: center;
  max-width: 380px; width: 100%;
  animation: fadeUp 0.5s cubic-bezier(0.16,1,0.3,1);
}

@keyframes fadeUp {
  from { opacity: 0; transform: translateY(20px); }
  to   { opacity: 1; transform: translateY(0); }
}

/* ── Loading ── */
.loading-bars { display: flex; gap: 5px; justify-content: center; margin-bottom: 1.5rem; }
.loading-bars span {
  display: block; width: 4px; height: 32px;
  background: #ff2d6b; border-radius: 3px;
  animation: barPulse 0.8s ease-in-out infinite alternate;
}
@keyframes barPulse {
  from { transform: scaleY(0.3); opacity: 0.3; }
  to   { transform: scaleY(1); opacity: 1; }
}
.state-label { font-size: 0.68rem; letter-spacing: 0.15em; color: #aaa; margin: 0; }

/* ── Icon Circle ── */
.icon-circle {
  width: 100px; height: 100px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  margin: 0 auto 1.5rem;
}
.icon-circle.success { background: #f0fdf4; border: 2px solid #bbf7d0; }
.icon-circle.already { background: #f0f9ff; border: 2px solid #bae6fd; }
.icon-circle.error   { background: #fff1f2; border: 2px solid #fecdd3; }
.icon-sym {
  font-family: 'Bebas Neue', sans-serif;
  font-size: 2.8rem; line-height: 1;
}
.icon-circle.success .icon-sym { color: #16a34a; }
.icon-circle.already .icon-sym { color: #0284c7; }
.icon-circle.error   .icon-sym { color: #ff2d6b; }

/* ── Titles ── */
.state-title, .brand-title {
  font-family: 'Bebas Neue', sans-serif;
  font-size: 4rem; letter-spacing: 0.06em; line-height: 1;
  margin: 0 0 0.5rem;
}
.title-accent { color: #ff2d6b; }
.title-error  { color: #ff2d6b; }

.activity-name {
  font-size: 1rem; font-weight: 700; color: #0a0a0a;
  margin: 0.75rem 0 1.5rem;
}

/* ── Info Cards ── */
.info-cards { display: grid; grid-template-columns: 1fr 1fr; gap: 0.75rem; margin-bottom: 1.5rem; }
.info-card {
  background: #fafafa; border: 1px solid #f0f0f0;
  border-radius: 16px; padding: 1rem;
  display: flex; flex-direction: column; gap: 0.3rem; text-align: left;
}
.info-card.ic-ok  { border-color: #bbf7d0; background: #f0fdf4; }
.info-card.ic-warn { border-color: #fde68a; background: #fffbeb; }
.ic-label { font-size: 0.58rem; letter-spacing: 0.15em; color: #aaa; }
.ic-val { font-size: 0.88rem; font-weight: 700; color: #0a0a0a; }
.info-card.ic-ok  .ic-val { color: #16a34a; }
.info-card.ic-warn .ic-val { color: #d97706; }

.done-hint { font-size: 0.68rem; letter-spacing: 0.1em; color: #bbb; margin: 0; }

/* ── Error ── */
.error-msg { font-size: 0.88rem; color: #888; margin: 0.5rem 0 1.5rem; line-height: 1.6; }
.retry-btn {
  font-family: 'Space Mono', monospace;
  font-size: 0.72rem; letter-spacing: 0.1em; font-weight: 700;
  background: #0a0a0a; color: #fff; border: none;
  border-radius: 24px; padding: 0.8rem 2rem;
  cursor: pointer; transition: background 0.2s;
}
.retry-btn:hover { background: #ff2d6b; }

/* ── Manual Screen ── */
.manual-screen {
  width: 100%; max-width: 400px;
  animation: fadeUp 0.5s cubic-bezier(0.16,1,0.3,1);
}

.brand-area { text-align: center; margin-bottom: 2rem; }

/* Activity card */
.activity-card {
  background: #fafafa; border: 1.5px solid #f0f0f0;
  border-radius: 20px; padding: 1.25rem 1.5rem;
  display: flex; flex-direction: column; gap: 4px;
  margin-bottom: 1.25rem;
  border-left: 4px solid #ff2d6b;
}
.ac-label { font-size: 0.58rem; letter-spacing: 0.18em; color: #ff2d6b; }
.ac-title { font-weight: 700; font-size: 1rem; color: #0a0a0a; }
.ac-time { font-size: 0.68rem; color: #aaa; }

/* Form card */
.form-card {
  background: #fff; border: 1.5px solid #f0f0f0;
  border-radius: 20px; padding: 1.5rem;
  margin-bottom: 1.5rem;
  box-shadow: 0 4px 24px rgba(0,0,0,0.04);
}
.form-label { font-size: 0.58rem; letter-spacing: 0.18em; color: #aaa; display: block; margin-bottom: 0.6rem; }
.input-wrap { margin-bottom: 1rem; }
.id-input {
  width: 100%;
  padding: 0.9rem 1.25rem;
  border: 1.5px solid #e8e8e8; border-radius: 14px;
  font-size: 1rem; color: #0a0a0a;
  background: #fafafa; outline: none;
  transition: all 0.2s; letter-spacing: 0.06em;
}
.id-input::placeholder { color: #ccc; letter-spacing: 0.04em; }
.id-input:focus { border-color: #ff2d6b; background: #fff; }

.submit-btn {
  width: 100%; padding: 1rem;
  background: #0a0a0a; color: #fff; border: none;
  border-radius: 14px;
  font-family: 'Space Mono', monospace;
  font-size: 0.8rem; font-weight: 700; letter-spacing: 0.1em;
  cursor: pointer; transition: background 0.2s;
  display: flex; align-items: center; justify-content: center;
  min-height: 52px;
}
.submit-btn:hover:not(:disabled) { background: #ff2d6b; }
.submit-btn:disabled { opacity: 0.4; cursor: not-allowed; }

/* Loading dots in button */
.btn-loading { display: flex; gap: 5px; align-items: center; }
.btn-loading span {
  display: block; width: 6px; height: 6px;
  background: #fff; border-radius: 50%;
  animation: dotPulse 0.6s ease-in-out infinite alternate;
}
.btn-loading span:nth-child(2) { animation-delay: 0.2s; }
.btn-loading span:nth-child(3) { animation-delay: 0.4s; }
@keyframes dotPulse {
  from { opacity: 0.3; transform: scale(0.8); }
  to   { opacity: 1; transform: scale(1); }
}

.manual-hint { font-size: 0.62rem; letter-spacing: 0.1em; color: #ccc; text-align: center; margin: 0; }
</style>