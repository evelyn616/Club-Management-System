<template>
  <div class="rd-wrap">


    <!-- ── Loading ── -->
    <div class="state-wrap" v-if="loading">
      <div class="loading-bars">
        <span v-for="i in 5" :key="i" :style="{ animationDelay: (i * 0.1) + 's' }"></span>
      </div>
      <p class="mono state-label">LOADING...</p>
    </div>

    <!-- ── Main ── -->
    <div class="rd-content" v-else>

      <!-- ── Page Header ── -->
      <div class="page-header">
        <div class="header-left">
          <div class="eyebrow"><span class="eyebrow-line"></span><span class="eyebrow-text">REGISTRATION DETAIL</span></div>
          <h1 class="page-title">
            <span class="title-overflow">{{ activity?.title || '報名名單' }}</span>
          </h1>
        </div>
        <div class="header-actions">
          <button class="btn-back" @click="goBack">← 返回總覽</button>
          <button class="btn-export" @click="exportList">匯出名單 ↓</button>
        </div>
      </div>

      <!-- ── Activity Info Cards ── -->
      <div class="info-row" v-if="activity">
        <div class="info-card">
          <span class="ic-label mono">LOCATION</span>
          <span class="ic-val">{{ activity.location || '—' }}</span>
        </div>
        <div class="info-card">
          <span class="ic-label mono">START</span>
          <span class="ic-val mono">{{ formatDateTime(activity.startTime) }}</span>
        </div>
        <div class="info-card">
          <span class="ic-label mono">DEADLINE</span>
          <span class="ic-val mono">{{ formatDateTime(activity.registrationDeadline) }}</span>
        </div>
        <div class="info-card" :class="{ 'ic-pink': activity.feeAmount > 0 }">
          <span class="ic-label mono">FEE</span>
          <span class="ic-val mono">{{ activity.feeAmount > 0 ? `NT$ ${activity.feeAmount}` : '免費' }}</span>
        </div>
      </div>

      <!-- ── Capacity Bar ── -->
      <div class="capacity-section" v-if="activity">
        <div class="cap-header">
          <span class="cap-label mono">REGISTRATION PROGRESS</span>
          <span class="cap-count mono">
            <span class="cap-num" :class="{ 'num-warn': progressPercentage >= 80 }">{{ registrations.length }}</span>
            <span class="cap-sep"> / </span>
            <span>{{ activity.maxParticipants || '無上限' }}</span>
            <span v-if="activity.maxParticipants" class="cap-pct"> ({{ progressPercentage }}%)</span>
          </span>
        </div>
        <div class="cap-bar" v-if="activity.maxParticipants">
          <div class="cap-fill" :class="{ 'fill-warn': progressPercentage >= 80, 'fill-full': progressPercentage >= 100 }"
               :style="{ width: Math.min(progressPercentage, 100) + '%' }"></div>
        </div>
      </div>

      <!-- ── Registration Table ── -->
      <div class="table-section">

        <!-- Stats pills -->
        <div class="table-meta">
          <div class="meta-pill">
            <span class="mp-num">{{ registrations.length }}</span>
            <span class="mp-label mono">TOTAL</span>
          </div>
          <div class="meta-pill pink">
            <span class="mp-num">{{ paidCount }}</span>
            <span class="mp-label mono">PAID</span>
          </div>
          <div class="meta-pill warn">
            <span class="mp-num">{{ pendingCount }}</span>
            <span class="mp-label mono">PENDING</span>
          </div>
        </div>

        <!-- Empty -->
        <div class="empty-state" v-if="registrations.length === 0">
          <div class="empty-big">EMPTY</div>
          <p class="empty-desc">目前還沒有人報名</p>
        </div>

        <!-- Table -->
        <div class="table-wrap" v-else>
          <table class="reg-table">
            <thead>
              <tr>
                <th class="th-num">#</th>
                <th>姓名</th>
                <th>會員 ID</th>
                <th>報名時間</th>
                <th class="th-center">繳費狀態</th>
                <th class="th-center">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="(reg, index) in registrations"
                :key="reg.id"
                class="reg-row"
                :style="{ animationDelay: (index * 0.03) + 's' }"
              >
                <td class="td-num mono">{{ String(index + 1).padStart(2, '0') }}</td>
                <td class="td-name">{{ reg.userName || '—' }}</td>
                <td class="td-id mono">{{ reg.userId }}</td>
                <td class="td-time mono">{{ formatDateTime(reg.registrationTime) }}</td>
                <td class="td-center">
                  <span class="pay-badge" :class="reg.paymentStatus?.toLowerCase()">
                    {{ getPaymentStatusLabel(reg.paymentStatus) }}
                  </span>
                </td>
                <td class="td-center">
                  <div class="action-group">
                    <button
                      v-if="reg.paymentStatus === 'PENDING'"
                      class="act-btn remind"
                      @click="sendPaymentReminder(reg)"
                      :disabled="sending"
                    >提醒繳費</button>
                    <button
                      class="act-btn cancel"
                      @click="cancelRegistration(reg)"
                      :disabled="canceling"
                    >取消報名</button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="table-footer mono" v-if="registrations.length > 0">
          SHOWING {{ registrations.length }} REGISTRATIONS
        </div>
      </div>

    </div>

    <!-- ── Toast ── -->
    <Teleport to="body">
      <div v-if="toast.show" class="toast" :class="toast.type">{{ toast.message }}</div>
    </Teleport>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { activityApi } from '@/api/activity'
import { registrationApi } from '@/api/registration'

const route = useRoute()
const router = useRouter()

const activity = ref(null)
const registrations = ref([])
const loading = ref(false)
const canceling = ref(false)
const sending = ref(false)
const toast = ref({ show: false, message: '', type: '' })

const activityId = route.params.activityId

// Navbar scroll
const navHidden = ref(false)
let lastY = 0
const onScroll = () => {
  const y = window.scrollY
  navHidden.value = y > lastY && y > 60
  lastY = y
}
onMounted(() => {
  window.addEventListener('scroll', onScroll, { passive: true })
  loadData()
})
onUnmounted(() => window.removeEventListener('scroll', onScroll))

const loadActivity = async () => {
  const res = await activityApi.getActivityDetails(activityId)
  activity.value = res.data
}
const loadRegistrations = async () => {
  const res = await registrationApi.getActivityRegistrations(activityId)
  registrations.value = res.data || []
}
const loadData = async () => {
  loading.value = true
  try { await Promise.all([loadActivity(), loadRegistrations()]) }
  catch (e) { console.error('載入失敗:', e) }
  finally { loading.value = false }
}

// Computed
const progressPercentage = computed(() => {
  if (!activity.value?.maxParticipants) return 0
  return Math.round((registrations.value.length / activity.value.maxParticipants) * 100)
})
const paidCount = computed(() => registrations.value.filter(r => r.paymentStatus === 'PAID').length)
const pendingCount = computed(() => registrations.value.filter(r => r.paymentStatus === 'PENDING').length)

// Actions
const goBack = () => router.push({ name: 'registrations-overview-container' })
const exportList = async () => {
  try {
    const response = await fetch(
      `http://localhost:8080/api/export/activities/${activityId}/registrations/excel`,
      { headers: { Authorization: `Bearer ${localStorage.getItem('token')}` } }
    )
    if (!response.ok) throw new Error('匯出失敗')
    const blob = await response.blob()
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `報名表單_${activityId}.xlsx`
    a.click()
    URL.revokeObjectURL(url)
  } catch (e) {
    showToast('匯出失敗，請稍後再試', 'error')
  }
}

const cancelRegistration = async (reg) => {
  if (!confirm(`確定要取消 ${reg.userName || reg.userId} 的報名嗎？`)) return
  canceling.value = true
  try {
    await registrationApi.cancel(reg.id)
    await loadRegistrations()
    showToast('取消報名成功', 'success')
  } catch (e) {
    showToast(e.response?.data?.message || '取消失敗', 'error')
  } finally { canceling.value = false }
}

const sendPaymentReminder = async (reg) => {
  if (!confirm(`確定要發送繳費提醒給 ${reg.userName || reg.userId}？`)) return
  sending.value = true
  try {
    await registrationApi.sendPaymentReminder(reg.id)
    showToast('繳費提醒已發送', 'success')
  } catch (e) {
    showToast(e.response?.data?.message || '發送失敗', 'error')
  } finally { sending.value = false }
}

// Helpers
const formatDateTime = (dt) => {
  if (!dt) return '—'
  const d = new Date(dt)
  return `${d.getFullYear()}/${String(d.getMonth()+1).padStart(2,'0')}/${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
}
const getPaymentStatusLabel = (s) =>
  ({ NOT_REQUIRED: '無須繳費', PENDING: '待繳費', PAID: '已繳費', CANCELLED: '已取消', REFUNDED: '已退款', PARTIAL_REFUNDED: '部分退款' })[s] || s || '—'

let toastTimer = null
function showToast(msg, type = 'success') {
  toast.value = { show: true, message: msg, type }
  if (toastTimer) clearTimeout(toastTimer)
  toastTimer = setTimeout(() => { toast.value.show = false }, 2800)
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Bebas+Neue&family=Space+Mono:wght@400;700&family=Noto+Sans+TC:wght@300;400;500;700&display=swap');

* { box-sizing: border-box; }
.rd-wrap { min-height: 100vh; background: #fff; font-family: 'Noto Sans TC', sans-serif; color: #0a0a0a; }
.mono { font-family: 'Space Mono', monospace; }

/* Navbar */

.navbar-hidden { transform: translateY(-100%); }
.nav-inner { max-width: 1400px; margin: 0 auto; display: flex; justify-content: space-between; align-items: center; }
.nav-logo { font-family: 'Space Mono', monospace; font-size: 0.9rem; font-weight: 700; letter-spacing: 0.18em; color: #0a0a0a; text-decoration: none; }
.nav-logo:hover { color: #ff2d6b; }
.nav-crumb { font-family: 'Space Mono', monospace; font-size: 0.62rem; letter-spacing: 0.15em; color: #aaa; }

/* State */
.state-wrap { display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 80vh; gap: 1rem; }
.loading-bars { display: flex; gap: 4px; }
.loading-bars span { display: block; width: 3px; height: 28px; background: #ff2d6b; border-radius: 2px; animation: barPulse 0.8s ease-in-out infinite alternate; }
@keyframes barPulse { from { transform: scaleY(0.3); opacity: 0.3; } to { transform: scaleY(1); opacity: 1; } }
.state-label { font-size: 0.68rem; letter-spacing: 0.2em; color: #aaa; margin: 0; }

/* Content */
.rd-content { max-width: 1400px; margin: 0 auto; padding: 8rem 3rem 4rem; }

/* Header */
.page-header { display: flex; justify-content: space-between; align-items: flex-end; padding-bottom: 2rem; margin-bottom: 2rem; border-bottom: 2px solid #0a0a0a; gap: 2rem; flex-wrap: wrap; }
.eyebrow { display: flex; align-items: center; gap: 0.6rem; margin-bottom: 0.6rem; }
.eyebrow-line { display: block; width: 24px; height: 2px; background: #ff2d6b; }
.eyebrow-text { font-family: 'Space Mono', monospace; font-size: 0.6rem; letter-spacing: 0.2em; color: #ff2d6b; }
.page-title { font-family: 'Bebas Neue', sans-serif; font-size: 3.2rem; letter-spacing: 0.06em; line-height: 1.1; margin: 0; max-width: 700px; }

.header-actions { display: flex; gap: 0.75rem; align-items: flex-end; flex-wrap: wrap; }
.btn-back { font-family: 'Space Mono', monospace; font-size: 0.68rem; letter-spacing: 0.08em; border: 1px solid #e0e0e0; background: transparent; color: #888; padding: 0.65rem 1.25rem; cursor: pointer; transition: all 0.2s; }
.btn-back:hover { border-color: #0a0a0a; color: #0a0a0a; }
.btn-export { font-family: 'Space Mono', monospace; font-size: 0.68rem; font-weight: 700; letter-spacing: 0.08em; background: #0a0a0a; color: #fff; border: none; padding: 0.65rem 1.25rem; cursor: pointer; transition: background 0.2s; }
.btn-export:hover { background: #ff2d6b; }

/* Info cards */
.info-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 1px; background: #e8e8e8; border: 1px solid #e8e8e8; margin-bottom: 2rem; }
.info-card { background: #fff; padding: 1.25rem 1.5rem; display: flex; flex-direction: column; gap: 0.35rem; }
.info-card.ic-pink .ic-val { color: #ff2d6b; font-weight: 700; }
.ic-label { font-family: 'Space Mono', monospace; font-size: 0.58rem; letter-spacing: 0.18em; color: #aaa; }
.ic-val { font-size: 0.92rem; color: #0a0a0a; }
.ic-val.mono { font-family: 'Space Mono', monospace; font-size: 0.8rem; }

/* Capacity */
.capacity-section { margin-bottom: 2.5rem; }
.cap-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.75rem; }
.cap-label { font-size: 0.6rem; letter-spacing: 0.18em; color: #aaa; }
.cap-count { font-size: 0.78rem; color: #888; }
.cap-num { font-family: 'Bebas Neue', sans-serif; font-size: 1.5rem; line-height: 1; color: #0a0a0a; }
.cap-num.num-warn { color: #ff2d6b; }
.cap-sep, .cap-pct { color: #ccc; }
.cap-bar { height: 6px; background: #f0f0f0; border-radius: 3px; overflow: hidden; }
.cap-fill { height: 100%; background: #0a0a0a; border-radius: 3px; transition: width 0.6s cubic-bezier(0.16,1,0.3,1); }
.cap-fill.fill-warn { background: #f59e0b; }
.cap-fill.fill-full { background: #ff2d6b; }

/* Table section */
.table-section { }
.table-meta { display: flex; gap: 1rem; margin-bottom: 1.25rem; }
.meta-pill { display: flex; align-items: baseline; gap: 0.5rem; padding: 0.6rem 1rem; border: 1px solid #f0f0f0; }
.meta-pill.pink { border-color: #fecdd3; }
.meta-pill.warn { border-color: #fde68a; }
.mp-num { font-family: 'Bebas Neue', sans-serif; font-size: 1.8rem; line-height: 1; color: #0a0a0a; }
.meta-pill.pink .mp-num { color: #ff2d6b; }
.meta-pill.warn .mp-num { color: #d97706; }
.mp-label { font-size: 0.58rem; letter-spacing: 0.15em; color: #aaa; }

.empty-state { text-align: center; padding: 5rem 0; }
.empty-big { font-family: 'Bebas Neue', sans-serif; font-size: 7rem; color: transparent; -webkit-text-stroke: 1px #eee; line-height: 1; }
.empty-desc { font-size: 0.85rem; color: #aaa; }

/* Table */
.table-wrap { overflow-x: auto; border: 1px solid #e8e8e8; }
.reg-table { width: 100%; border-collapse: collapse; background: #fff; }
.reg-table thead tr { background: #0a0a0a; }
.reg-table th { padding: 0.75rem 1rem; text-align: left; font-family: 'Space Mono', monospace; font-size: 0.58rem; letter-spacing: 0.15em; color: #fff; font-weight: 400; white-space: nowrap; }
.th-num { width: 48px; }
.th-center { text-align: center !important; }

@keyframes rowFadeUp { from { opacity: 0; transform: translateY(6px); } to { opacity: 1; transform: translateY(0); } }
.reg-row { border-bottom: 1px solid #f5f5f5; animation: rowFadeUp 0.3s ease both; transition: background 0.15s; }
.reg-row:hover { background: #fafafa; }
.reg-row:last-child { border-bottom: none; }
.reg-table td { padding: 0.9rem 1rem; vertical-align: middle; }

.td-num { font-size: 0.68rem; color: #ccc; letter-spacing: 0.1em; }
.td-name { font-weight: 700; font-size: 0.9rem; }
.td-id { font-size: 0.72rem; color: #888; letter-spacing: 0.06em; }
.td-time { font-size: 0.72rem; color: #888; letter-spacing: 0.04em; white-space: nowrap; }
.td-center { text-align: center; }

.pay-badge { font-family: 'Space Mono', monospace; font-size: 0.58rem; letter-spacing: 0.06em; padding: 0.25rem 0.75rem; font-weight: 600; display: inline-block; }
.pay-badge.paid          { background: #f0fdf4; color: #16a34a; border: 1px solid #bbf7d0; }
.pay-badge.pending       { background: #fffbeb; color: #92400e; border: 1px solid #fde68a; }
.pay-badge.not_required  { background: #f5f3ff; color: #6d28d9; border: 1px solid #ddd6fe; }
.pay-badge.cancelled     { background: #fafafa; color: #aaa; border: 1px solid #e8e8e8; }
.pay-badge.refunded      { background: #fff1f2; color: #be123c; border: 1px solid #fecdd3; }

.action-group { display: flex; gap: 0.4rem; justify-content: center; }
.act-btn { font-family: 'Space Mono', monospace; font-size: 0.58rem; font-weight: 700; letter-spacing: 0.06em; padding: 0.35rem 0.75rem; border: 1px solid; cursor: pointer; transition: all 0.15s; white-space: nowrap; }
.act-btn.remind { border-color: #f59e0b; color: #92400e; background: #fffbeb; }
.act-btn.remind:hover:not(:disabled) { background: #f59e0b; color: #fff; border-color: #f59e0b; }
.act-btn.cancel { border-color: #e0e0e0; color: #aaa; background: transparent; }
.act-btn.cancel:hover:not(:disabled) { border-color: #dc2626; color: #dc2626; }
.act-btn:disabled { opacity: 0.35; cursor: not-allowed; }

.table-footer { margin-top: 1rem; text-align: right; font-size: 0.6rem; letter-spacing: 0.15em; color: #ccc; }

/* Toast */
.toast { position: fixed; bottom: 2.5rem; left: 50%; transform: translateX(-50%); padding: 0.85rem 2rem; font-family: 'Space Mono', monospace; font-size: 0.72rem; letter-spacing: 0.08em; font-weight: 700; border-radius: 0; z-index: 2000; white-space: nowrap; animation: slideUp 0.3s cubic-bezier(0.16,1,0.3,1); }
.toast.success { background: #0a0a0a; color: #fff; }
.toast.error   { background: #ff2d6b; color: #fff; }
@keyframes slideUp { from { opacity: 0; transform: translateX(-50%) translateY(12px); } to { opacity: 1; transform: translateX(-50%) translateY(0); } }

@media (max-width: 900px) {
  .info-row { grid-template-columns: repeat(2, 1fr); }
  .page-header { flex-direction: column; align-items: flex-start; }
  .rd-content { padding-left: 1.25rem; padding-right: 1.25rem; }
  
}
</style>