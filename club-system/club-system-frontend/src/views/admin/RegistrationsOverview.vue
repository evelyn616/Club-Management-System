<template>
  <div class="overview-wrap">


    <!-- ── Page Header ── -->
    <div class="page-header">
      <div class="header-left">
        <div class="eyebrow">
          <span class="eyebrow-line"></span>
          <span class="eyebrow-text">REGISTRATION OVERVIEW</span>
        </div>
        <h1 class="page-title">報名<span class="title-accent">總覽</span></h1>
      </div>
      <div class="header-right">
        <div class="header-search">
          <input
            v-model="searchKeyword"
            type="text"
            placeholder="搜尋活動名稱..."
            class="search-input"
          />
          <span class="search-icon">→</span>
        </div>
        <select v-model="selectedType" class="type-select">
          <option value="">TYPE: ALL</option>
          <option value="REGULAR">社課</option>
          <option value="SPECIAL">特殊活動</option>
          <option value="TRAINING">團練</option>
          <option value="PERFORMANCE">演出</option>
          <option value="COMPETITION">比賽</option>
        </select>
      </div>
    </div>

    <!-- ── KPI Cards ── -->
    <div class="kpi-row">
      <div class="kpi-card">
        <span class="kpi-label">TOTAL REGISTRATIONS</span>
        <span class="kpi-num">{{ totalRegistrations }}</span>
        <span class="kpi-sub">總報名人次</span>
      </div>
      <div class="kpi-card accent">
        <span class="kpi-label">ACTIVITIES</span>
        <span class="kpi-num">{{ activitiesWithRegistrations }}</span>
        <span class="kpi-sub">有報名的活動數</span>
      </div>
      <div class="kpi-card warn">
        <span class="kpi-label">NEARLY FULL</span>
        <span class="kpi-num">{{ nearlyFullActivities }}</span>
        <span class="kpi-sub">即將額滿</span>
      </div>
      <div class="kpi-card pink">
        <span class="kpi-label">TODAY</span>
        <span class="kpi-num">{{ newRegistrationsToday }}</span>
        <span class="kpi-sub">本日新增報名</span>
      </div>
    </div>

    <!-- ── Table Section ── -->
    <div class="table-section">

      <!-- Loading -->
      <div v-if="loading" class="state-wrap">
        <div class="loading-bars">
          <span v-for="i in 5" :key="i" :style="{ animationDelay: (i * 0.1) + 's' }"></span>
        </div>
        <p class="mono">LOADING...</p>
      </div>

      <!-- Empty -->
      <div v-else-if="filteredActivities.length === 0" class="state-wrap">
        <div class="empty-big">EMPTY</div>
        <p class="empty-desc">目前沒有符合條件的活動</p>
      </div>

      <!-- Table -->
      <table v-else class="reg-table">
        <thead>
          <tr>
            <th class="th-num">#</th>
            <th>活動名稱</th>
            <th>活動時間</th>
            <th>報名截止</th>
            <th class="th-center">報名人數</th>
            <th class="th-center">報名率</th>
            <th class="th-center">狀態</th>
            <th class="th-center">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="(activity, index) in filteredActivities"
            :key="activity.id"
            class="table-row"
            :style="{ animationDelay: (index * 0.04) + 's' }"
          >
            <td class="td-num">{{ String(index + 1).padStart(2, '0') }}</td>

            <td class="td-title">
              <span class="activity-title">{{ activity.title }}</span>
              <span class="activity-type-tag">{{ getActivityTypeLabel(activity.activityType) }}</span>
            </td>

            <td class="td-time">{{ formatDateTime(activity.startTime) }}</td>

            <td class="td-deadline">
              <span :class="['deadline-tag', getDeadlineClass(activity.registrationDeadline)]">
                {{ formatDeadline(activity.registrationDeadline) }}
              </span>
            </td>

            <td class="td-center">
              <span class="count-display">
                {{ activity.registrationCount || 0 }}
                <span v-if="activity.maxParticipants" class="count-max">/ {{ activity.maxParticipants }}</span>
              </span>
              <!-- mini capacity bar -->
              <div v-if="activity.maxParticipants" class="mini-bar">
                <div
                  class="mini-bar-fill"
                  :class="{ 'fill-warn': (activity.registrationCount / activity.maxParticipants) >= 0.9 }"
                  :style="{ width: Math.min((activity.registrationCount / activity.maxParticipants) * 100, 100) + '%' }"
                ></div>
              </div>
            </td>

            <td class="td-center">
              <span :class="['rate-tag', getRateClass(activity.registrationRate)]">
                {{ formatRate(activity.registrationRate) }}
              </span>
            </td>

            <td class="td-center">
              <span class="status-tag" :class="'s-' + activity.status?.toLowerCase()">
                {{ getStatusLabel(activity.status) }}
              </span>
            </td>

            <td class="td-center">
              <button class="view-btn" @click="goToDetail(activity.id)">
                查看名單 →
              </button>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- Row count hint -->
      <div v-if="!loading && filteredActivities.length > 0" class="table-footer">
        <span class="mono">SHOWING {{ filteredActivities.length }} ACTIVITIES</span>
      </div>
    </div>

  </div>
</template>

<script setup>
import { activityApi } from '@/api/activity'
import { registrationApi } from '@/api/registration'
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const activities = ref([])
const loading = ref(false)
const selectedType = ref('')
const searchKeyword = ref('')

// ── Navbar scroll ──
const navHidden = ref(false)
let lastY = 0
const onScroll = () => {
  const y = window.scrollY
  navHidden.value = y > lastY && y > 60
  lastY = y
}
onMounted(() => {
  window.addEventListener('scroll', onScroll, { passive: true })
  loadActivities()
})
onUnmounted(() => window.removeEventListener('scroll', onScroll))

// ── Load ──
const loadActivities = async () => {
  loading.value = true
  try {
    const response = await activityApi.getAllActivities()
    activities.value = response.data

    const countPromises = activities.value.map(activity =>
      registrationApi.getActivityRegistrationCount(activity.id)
        .then(res => { activity.registrationCount = res.data })
        .catch(() => { activity.registrationCount = 0 })
    )
    await Promise.all(countPromises)

    activities.value.forEach(activity => {
      if (activity.maxParticipants) {
        activity.registrationRate = activity.registrationCount / activity.maxParticipants
      }
    })
  } catch (e) {
    console.error('載入活動失敗', e)
  } finally {
    loading.value = false
  }
}

// ── KPI Computed ──
const totalRegistrations = computed(() =>
  activities.value.reduce((sum, a) => sum + (a.registrationCount || 0), 0)
)
const activitiesWithRegistrations = computed(() =>
  activities.value.filter(a => (a.registrationCount || 0) > 0).length
)
const nearlyFullActivities = computed(() =>
  activities.value.filter(a => a.maxParticipants && (a.registrationCount / a.maxParticipants) >= 0.9).length
)
const newRegistrationsToday = computed(() => {
  const today = new Date().toDateString()
  return activities.value.filter(a => {
    if (!a.updatedAt) return false
    return new Date(a.updatedAt).toDateString() === today
  }).length
})

// ── Filter ──
// 狀態優先順序
const STATUS_ORDER = { ONGOING:0, PUBLISHED:1, SCHEDULED:2, DRAFT:3, COMPLETED:4, CANCELLED:5 }

const filteredActivities = computed(() => {
  let result = [...activities.value]
  if (selectedType.value) {
    result = result.filter(a => a.activityType === selectedType.value)
  }
  if (searchKeyword.value) {
    const kw = searchKeyword.value.toLowerCase()
    result = result.filter(a =>
      (a.title || '').toLowerCase().includes(kw) ||
      (a.description || '').toLowerCase().includes(kw)
    )
  }
  result.sort((a, b) => {
    const oA = STATUS_ORDER[a.status] ?? 99
    const oB = STATUS_ORDER[b.status] ?? 99
    if (oA !== oB) return oA - oB
    // 同群組內：PUBLISHED/ONGOING/SCHEDULED → 開始時間近的排前面
    if (oA <= 2) {
      const tA = a.startTime ? new Date(a.startTime).getTime() : Infinity
      const tB = b.startTime ? new Date(b.startTime).getTime() : Infinity
      return tA - tB
    }
    // COMPLETED/CANCELLED → 最近的排前面
    if (oA >= 4) {
      const tA = a.startTime ? new Date(a.startTime).getTime() : 0
      const tB = b.startTime ? new Date(b.startTime).getTime() : 0
      return tB - tA
    }
    return 0
  })
  return result
})

// ── Helpers ──
const getActivityTypeLabel = (t) =>
  ({ REGULAR: '社課', SPECIAL: '特殊活動', TRAINING: '團練', PERFORMANCE: '演出', COMPETITION: '比賽' })[t] || t

const getStatusLabel = (s) =>
  ({ DRAFT: '草稿', PUBLISHED: '已發布', COMPLETED: '已完成', CANCELLED: '已取消', SCHEDULED: '已預約', ONGOING: '進行中' })[s] || s

const formatDateTime = (dt) => {
  if (!dt) return '—'
  const d = new Date(dt)
  return d.toLocaleString('zh-TW', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

const formatDeadline = (deadline) => {
  if (!deadline) return '—'
  const now = new Date()
  const d = new Date(deadline)
  const diffDays = Math.ceil((d - now) / (1000 * 60 * 60 * 24))
  const dateStr = formatDateTime(deadline)
  if (diffDays < 0) return `${dateStr} 已截止`
  if (diffDays === 0) return `${dateStr} 今天截止`
  return `${dateStr} 剩 ${diffDays}天`
}

const getDeadlineClass = (deadline) => {
  if (!deadline) return ''
  const diffDays = Math.ceil((new Date(deadline) - new Date()) / (1000 * 60 * 60 * 24))
  if (diffDays < 0) return 'dl-passed'
  if (diffDays <= 3) return 'dl-urgent'
  if (diffDays <= 7) return 'dl-warn'
  return 'dl-normal'
}

const formatRate = (rate) => {
  if (rate === null || rate === undefined) return '—'
  return `${Math.round(rate * 100)}%`
}

const getRateClass = (rate) => {
  if (!rate) return ''
  if (rate >= 0.9) return 'rate-high'
  if (rate >= 0.6) return 'rate-mid'
  return 'rate-low'
}

const goToDetail = (activityId) => {
  router.push({ name: 'registration-detail-container', params: { activityId } })
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Bebas+Neue&family=Space+Mono:wght@400;700&family=Noto+Sans+TC:wght@300;400;500;700&display=swap');

* { box-sizing: border-box; }

.overview-wrap {
  min-height: 100vh;
  background: #ffffff;
  font-family: 'Noto Sans TC', sans-serif;
  color: #0a0a0a;
}

/* ── Navbar ── */

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
  transition: color 0.2s;
}
.nav-logo:hover { color: #ff2d6b; }
.nav-crumb {
  font-family: 'Space Mono', monospace;
  font-size: 0.65rem; letter-spacing: 0.15em; color: #aaa;
}
.nav-crumb-active { color: #ff2d6b; }

/* ── Page Header ── */
.page-header {
  max-width: 1400px; margin: 0 auto;
  padding: 8rem 3rem 2.5rem;
  display: flex; justify-content: space-between; align-items: flex-end;
  border-bottom: 2px solid #0a0a0a;
}
.eyebrow {
  display: flex; align-items: center; gap: 0.75rem; margin-bottom: 0.75rem;
}
.eyebrow-line {
  display: block; width: 28px; height: 2px; background: #ff2d6b;
}
.eyebrow-text {
  font-family: 'Space Mono', monospace;
  font-size: 0.65rem; letter-spacing: 0.2em; color: #ff2d6b;
}
.page-title {
  font-family: 'Bebas Neue', sans-serif;
  font-size: 4.5rem; letter-spacing: 0.06em;
  line-height: 1; margin: 0; color: #0a0a0a;
}
.title-accent { color: #ff2d6b; margin-left: 0.1em; }

.header-right {
  display: flex; align-items: center; gap: 0.75rem; padding-bottom: 0.25rem;
}
.header-search { position: relative; }
.search-input {
  padding: 0.65rem 2rem 0.65rem 1rem;
  border: 1px solid #0a0a0a; border-radius: 0;
  font-family: 'Space Mono', monospace; font-size: 0.75rem;
  letter-spacing: 0.06em; background: #fff; color: #0a0a0a;
  outline: none; width: 220px;
  transition: border-color 0.2s;
}
.search-input::placeholder { color: #bbb; }
.search-input:focus { border-color: #ff2d6b; }
.search-icon {
  position: absolute; right: 0.75rem; top: 50%;
  transform: translateY(-50%); color: #bbb; pointer-events: none;
}
.type-select {
  padding: 0.65rem 1rem;
  border: 1px solid #d0d0d0; border-radius: 0;
  font-family: 'Space Mono', monospace; font-size: 0.72rem; letter-spacing: 0.06em;
  background: #fff; color: #555; cursor: pointer; outline: none;
  transition: border-color 0.2s;
}
.type-select:focus { border-color: #0a0a0a; }

/* ── KPI Row ── */
.kpi-row {
  max-width: 1400px; margin: 0 auto;
  padding: 0 3rem;
  display: grid; grid-template-columns: repeat(4, 1fr);
  gap: 0;
  border-bottom: 1px solid #e8e8e8;
}
.kpi-card {
  padding: 2.5rem 2rem;
  border-right: 1px solid #e8e8e8;
  display: flex; flex-direction: column; gap: 0.35rem;
  position: relative;
}
.kpi-card:last-child { border-right: none; }

.kpi-card::before {
  content: ''; position: absolute; top: 0; left: 0; right: 0; height: 3px;
  background: #e8e8e8;
}
.kpi-card.accent::before { background: #0a0a0a; }
.kpi-card.warn::before   { background: #f59e0b; }
.kpi-card.pink::before   { background: #ff2d6b; }

.kpi-label {
  font-family: 'Space Mono', monospace;
  font-size: 0.6rem; letter-spacing: 0.18em; color: #aaa;
  text-transform: uppercase;
}
.kpi-num {
  font-family: 'Bebas Neue', sans-serif;
  font-size: 4rem; line-height: 1; letter-spacing: 0.02em; color: #0a0a0a;
}
.kpi-card.accent .kpi-num { color: #0a0a0a; }
.kpi-card.warn   .kpi-num { color: #d97706; }
.kpi-card.pink   .kpi-num { color: #ff2d6b; }
.kpi-sub {
  font-size: 0.78rem; color: #888;
}

/* ── Table Section ── */
.table-section {
  max-width: 1400px; margin: 0 auto;
  padding: 2.5rem 3rem 4rem;
}

/* ── States ── */
.state-wrap { text-align: center; padding: 5rem 2rem; }
.loading-bars { display: flex; gap: 4px; justify-content: center; margin-bottom: 1rem; }
.loading-bars span {
  display: block; width: 3px; height: 28px; background: #ff2d6b; border-radius: 2px;
  animation: barPulse 0.8s ease-in-out infinite alternate;
}
@keyframes barPulse {
  from { transform: scaleY(0.3); opacity: 0.3; }
  to   { transform: scaleY(1); opacity: 1; }
}
.mono { font-family: 'Space Mono', monospace; font-size: 0.7rem; letter-spacing: 0.2em; color: #aaa; }
.empty-big {
  font-family: 'Bebas Neue', sans-serif; font-size: 8rem;
  color: transparent; -webkit-text-stroke: 1px #e0e0e0;
  letter-spacing: 0.05em; line-height: 1; margin-bottom: 1rem;
}
.empty-desc { font-size: 0.88rem; color: #bbb; }

/* ── Table ── */
@keyframes rowFadeUp {
  from { opacity: 0; transform: translateY(10px); }
  to   { opacity: 1; transform: translateY(0); }
}

.reg-table {
  width: 100%;
  border-collapse: collapse;
}

.reg-table thead tr {
  border-bottom: 2px solid #0a0a0a;
}
.reg-table th {
  font-family: 'Space Mono', monospace;
  font-size: 0.6rem; letter-spacing: 0.15em;
  color: #aaa; text-transform: uppercase;
  padding: 0.75rem 1rem; text-align: left;
  font-weight: 400;
}
.th-num   { width: 48px; }
.th-center { text-align: center !important; }

.table-row {
  border-bottom: 1px solid #f0f0f0;
  animation: rowFadeUp 0.4s ease both;
  transition: background 0.15s;
}
.table-row:hover { background: #fafafa; }
.table-row:last-child { border-bottom: 1px solid #e8e8e8; }

.reg-table td {
  padding: 1.1rem 1rem; vertical-align: middle;
}
.td-num {
  font-family: 'Space Mono', monospace;
  font-size: 0.7rem; color: #ccc; letter-spacing: 0.1em;
}
.td-title {
  display: flex; flex-direction: column; gap: 4px;
}
.activity-title {
  font-weight: 700; font-size: 0.95rem; color: #0a0a0a;
}
.activity-type-tag {
  font-family: 'Space Mono', monospace;
  font-size: 0.58rem; letter-spacing: 0.1em; color: #aaa;
  text-transform: uppercase;
}
.td-time {
  font-family: 'Space Mono', monospace;
  font-size: 0.75rem; color: #888; letter-spacing: 0.04em;
  white-space: nowrap;
}
.td-deadline .deadline-tag {
  font-family: 'Space Mono', monospace;
  font-size: 0.68rem; letter-spacing: 0.04em;
  white-space: nowrap;
}
.dl-normal { color: #888; }
.dl-warn   { color: #d97706; font-weight: 600; }
.dl-urgent { color: #dc2626; font-weight: 700; }
.dl-passed { color: #bbb; text-decoration: line-through; }

.td-center { text-align: center; }
.count-display {
  font-family: 'Space Mono', monospace;
  font-size: 0.88rem; font-weight: 700; color: #0a0a0a;
  display: block;
}
.count-max { font-weight: 400; color: #bbb; }
.mini-bar {
  height: 2px; background: #f0f0f0; margin-top: 5px; border-radius: 2px;
}
.mini-bar-fill {
  height: 100%; background: #0a0a0a; border-radius: 2px;
  transition: width 0.4s ease;
}
.mini-bar-fill.fill-warn { background: #ff2d6b; }

.rate-tag {
  font-family: 'Space Mono', monospace;
  font-size: 0.78rem; font-weight: 700; letter-spacing: 0.06em;
}
.rate-low  { color: #16a34a; }
.rate-mid  { color: #d97706; }
.rate-high { color: #dc2626; }

.status-tag {
  font-family: 'Space Mono', monospace;
  font-size: 0.6rem; letter-spacing: 0.1em;
  padding: 0.3rem 0.75rem; font-weight: 600;
  display: inline-block;
}
.s-draft     { background: #f5f5f5; color: #888; }
.s-published { background: #f0f9ff; color: #0369a1; border: 1px solid #bae6fd; }
.s-ongoing   { background: #f0fdf4; color: #15803d; border: 1px solid #bbf7d0; }
.s-completed { background: #fafafa; color: #aaa; border: 1px solid #e8e8e8; }
.s-cancelled { background: #fff1f2; color: #be123c; border: 1px solid #fecdd3; }
.s-scheduled { background: #fefce8; color: #a16207; border: 1px solid #fde68a; }

.view-btn {
  font-family: 'Space Mono', monospace;
  font-size: 0.65rem; letter-spacing: 0.08em;
  background: #0a0a0a; color: #fff; border: none;
  padding: 0.55rem 1rem;
  cursor: pointer; font-weight: 700;
  transition: background 0.2s;
  white-space: nowrap;
}
.view-btn:hover { background: #ff2d6b; }

/* ── Table Footer ── */
.table-footer {
  margin-top: 1.5rem;
  padding-top: 1rem;
  border-top: 1px solid #f0f0f0;
  text-align: right;
}

/* ── Responsive ── */
@media (max-width: 1024px) {
  .kpi-row { grid-template-columns: repeat(2, 1fr); }
  .page-header { flex-direction: column; align-items: flex-start; gap: 1.5rem; }
}
@media (max-width: 768px) {
  .kpi-row { grid-template-columns: 1fr 1fr; }
  .nav-inner, .page-header, .kpi-row, .table-section { padding-left: 1.25rem; padding-right: 1.25rem; }
  .page-title { font-size: 3rem; }
  .kpi-num { font-size: 2.8rem; }
  .reg-table { font-size: 0.85rem; }
}
</style>