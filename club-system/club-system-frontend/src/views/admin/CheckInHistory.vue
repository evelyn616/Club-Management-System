<template>
  <div class="cih-wrap">

    <!-- ── Page Header ── -->
    <div class="page-header">
      <div class="header-left">
        <div class="eyebrow">
          <span class="eyebrow-line"></span>
          <span class="eyebrow-text">CHECK-IN HISTORY</span>
        </div>
        <h1 class="page-title">
          簽到<span class="title-accent">歷史</span>
        </h1>
        <p class="page-sub">已完成 / 已取消 活動的報名與簽到紀錄</p>
      </div>
      <div class="header-actions">
        <button class="back-btn" @click="router.push({ name: 'checkin-dashboard' })">
          ← 返回簽到管理
        </button>
      </div>
    </div>

    <!-- ── Activity List ── -->
    <div class="section-label mono">SELECT ACTIVITY</div>

    <div v-if="loadingActivities" class="list-state mono">LOADING...</div>
    <div v-else-if="historyActivities.length === 0" class="empty-wrap">
      <div class="empty-big">EMPTY</div>
      <p class="empty-desc mono">目前沒有已完成或已取消的活動</p>
    </div>

    <div v-else class="activity-cards">
      <div
        v-for="act in historyActivities"
        :key="act.id"
        class="act-card"
        :class="{ selected: selectedActivityId === act.id, cancelled: act.status === 'CANCELLED' }"
        @click="selectActivity(act)"
      >
        <div class="act-card-top">
          <span class="act-status-badge" :class="act.status.toLowerCase()">{{ statusLabel(act.status) }}</span>
          <span class="act-date mono">{{ formatDate(act.startTime) }}</span>
        </div>
        <div class="act-title">{{ act.title }}</div>
        <div class="act-meta mono" v-if="act.location">📍 {{ act.location }}</div>
      </div>
    </div>

    <!-- ── Registrations Panel ── -->
    <div class="panel" v-if="selectedActivityId">
      <div class="panel-header">
        <div class="panel-title-row">
          <h2 class="panel-title">{{ selectedActivity?.title }}</h2>
          <div class="panel-stats">
            <span class="pstat">總報名 <strong>{{ stats.total }}</strong></span>
            <span class="pstat accent">已簽到 <strong>{{ stats.checkedIn }}</strong></span>
            <span class="pstat warn">遲到 <strong>{{ stats.late }}</strong></span>
          </div>
        </div>

        <!-- Search + Filter -->
        <div class="search-filter-row">
          <div class="search-wrap">
            <input v-model="searchKeyword" placeholder="搜尋姓名或 ID..." class="search-input" />
            <span class="search-icon">→</span>
          </div>
          <div class="filter-pills">
            <button
              v-for="f in filters" :key="f.value"
              :class="['pill', { active: activeFilter === f.value }]"
              @click="activeFilter = f.value"
            >{{ f.label }}</button>
          </div>
        </div>
      </div>

      <div class="reg-list">
        <div v-if="loadingRegs" class="list-state mono">LOADING...</div>
        <div v-else-if="filteredRegistrations.length === 0" class="list-state mono">沒有符合的紀錄</div>
        <div
          v-for="reg in filteredRegistrations"
          :key="reg.id"
          class="reg-row"
          :class="{
            'is-checked': reg.status === 'ATTENDED' && !reg.late,
            'is-late': reg.status === 'ATTENDED' && reg.late === true
          }"
        >
          <div class="reg-left">
            <div class="reg-avatar" :class="{
              'av-checked': reg.status === 'ATTENDED' && !reg.late,
              'av-late': reg.status === 'ATTENDED' && reg.late === true
            }">{{ (reg.userName || reg.userId || '?')[0].toUpperCase() }}</div>
            <div class="reg-info">
              <span class="reg-name">{{ reg.userName || reg.userId }}</span>
              <span class="reg-meta mono">
                {{ reg.userId }}
                <span class="meta-sep">·</span>
                報名 {{ formatDate(reg.registrationTime) }}
              </span>
              <span v-if="reg.status === 'ATTENDED'" class="check-meta mono">
                簽到 {{ formatDate(reg.checkInTime) }}
                <span class="late-pill" v-if="reg.late">LATE</span>
              </span>
            </div>
          </div>
          <div class="reg-right">
            <span class="pay-badge" :class="reg.paymentStatus?.toLowerCase()">
              {{ paymentLabel(reg.paymentStatus) }}
            </span>
            <span class="status-tag" :class="reg.status.toLowerCase()">
              {{ regStatusLabel(reg.status) }}
            </span>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { activityApi } from '@/api/activity'
import { registrationApi } from '@/api/registration'

const router = useRouter()

const activities = ref([])
const loadingActivities = ref(true)
const selectedActivityId = ref(null)
const registrations = ref([])
const loadingRegs = ref(false)
const searchKeyword = ref('')
const activeFilter = ref('all')
const stats = ref({ total: 0, checkedIn: 0, late: 0 })

const filters = [
  { label: '全部', value: 'all' },
  { label: '已簽到', value: 'checkedIn' },
  { label: '未簽到', value: 'pending' },
  { label: '遲到', value: 'late' },
]

const historyActivities = computed(() =>
  [...activities.value]
    .filter(a => a.status === 'COMPLETED' || a.status === 'CANCELLED')
    .sort((a, b) => new Date(b.startTime) - new Date(a.startTime))
)

const selectedActivity = computed(() =>
  activities.value.find(a => a.id === selectedActivityId.value)
)

const filteredRegistrations = computed(() => {
  let list = registrations.value
  if (searchKeyword.value) {
    const kw = searchKeyword.value.toLowerCase()
    list = list.filter(r => r.userId?.toLowerCase().includes(kw) || r.userName?.toLowerCase().includes(kw))
  }
  if (activeFilter.value === 'pending')   list = list.filter(r => r.status === 'REGISTERED')
  if (activeFilter.value === 'checkedIn') list = list.filter(r => r.status === 'ATTENDED')
  if (activeFilter.value === 'late')      list = list.filter(r => r.status === 'ATTENDED' && r.late === true)
  return list
})

onMounted(async () => {
  try {
    const res = await activityApi.getAllActivities()
    activities.value = res.data || []
  } catch (e) { console.error(e) }
  finally { loadingActivities.value = false }
})

async function selectActivity(act) {
  if (selectedActivityId.value === act.id) return
  selectedActivityId.value = act.id
  activeFilter.value = 'all'
  searchKeyword.value = ''
  loadingRegs.value = true
  try {
    const [regRes, countRes, checkinCountRes] = await Promise.all([
      registrationApi.getActivityRegistrations(act.id),
      registrationApi.countRegistrations(act.id),
      registrationApi.countCheckedIn(act.id),
    ])
    registrations.value = regRes.data || []
    const lateCount = registrations.value.filter(r => r.late === true).length
    stats.value = {
      total: countRes.data || 0,
      checkedIn: checkinCountRes.data || 0,
      late: lateCount,
    }
  } catch (e) { console.error(e) }
  finally { loadingRegs.value = false }
}

function statusLabel(s) {
  return { COMPLETED: '已完成', CANCELLED: '已取消' }[s] || s
}
function regStatusLabel(s) {
  return { ATTENDED: '已簽到', REGISTERED: '未簽到', ABSENT: '缺席' }[s] || s
}
function paymentLabel(s) {
  return ({ PAID: '已繳費', PENDING: '待繳費', FREE: '免費', UNPAID: '未繳費' })[s] || s || '—'
}
function formatDate(dt) {
  if (!dt) return '—'
  const d = new Date(dt)
  return `${String(d.getMonth()+1).padStart(2,'0')}/${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Bebas+Neue&family=Space+Mono:wght@400;700&family=Noto+Sans+TC:wght@300;400;500;700&display=swap');
* { box-sizing: border-box; }

.cih-wrap {
  min-height: 100vh;
  background: #ffffff;
  font-family: 'Noto Sans TC', sans-serif;
  color: #0a0a0a;
}
.mono { font-family: 'Space Mono', monospace; }

/* Header */
.page-header {
  max-width: 1300px; margin: 0 auto;
  padding: 8rem 2.5rem 2.5rem;
  display: flex; justify-content: space-between; align-items: flex-end;
  border-bottom: 2px solid #0a0a0a;
  gap: 2rem; flex-wrap: wrap;
}
.eyebrow { display: flex; align-items: center; gap: 0.6rem; margin-bottom: 0.6rem; }
.eyebrow-line { display: block; width: 24px; height: 2px; background: #ff2d6b; border-radius: 2px; }
.eyebrow-text { font-family: 'Space Mono', monospace; font-size: 0.6rem; letter-spacing: 0.2em; color: #ff2d6b; }
.page-title {
  font-family: 'Bebas Neue', sans-serif;
  font-size: 4rem; letter-spacing: 0.06em; line-height: 1; margin: 0;
}
.title-accent { color: #ff2d6b; }
.page-sub { font-size: 0.85rem; color: #777; margin: 0.4rem 0 0; }
.back-btn {
  font-family: 'Space Mono', monospace;
  font-size: 0.68rem; letter-spacing: 0.1em;
  background: none; border: 1.5px solid #e0e0e0;
  border-radius: 24px; padding: 0.65rem 1.25rem;
  cursor: pointer; color: #666;
  transition: all 0.18s;
}
.back-btn:hover { border-color: #0a0a0a; color: #0a0a0a; }

/* Section label */
.section-label {
  max-width: 1300px; margin: 0 auto;
  padding: 2rem 2.5rem 1rem;
  font-size: 0.6rem; letter-spacing: 0.2em; color: #aaa;
}

/* Empty */
.empty-wrap { text-align: center; padding: 5rem 2rem; }
.empty-big {
  font-family: 'Bebas Neue', sans-serif; font-size: 7rem;
  color: transparent; -webkit-text-stroke: 1px #eee; line-height: 1;
}
.empty-desc { font-size: 0.72rem; letter-spacing: 0.15em; color: #ccc; margin-top: 0.5rem; }

/* Activity cards */
.activity-cards {
  max-width: 1300px; margin: 0 auto;
  padding: 0 2.5rem 2rem;
  display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 1rem;
}
.act-card {
  background: #fff;
  border: 1.5px solid #f0f0f0;
  border-radius: 16px;
  padding: 1.1rem 1.25rem;
  cursor: pointer;
  transition: all 0.2s;
}
.act-card:hover { border-color: #ccc; box-shadow: 0 4px 16px rgba(0,0,0,0.06); }
.act-card.selected { border-color: #ff2d6b; background: #fff5f8; }
.act-card.cancelled { opacity: 0.7; }
.act-card-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.6rem; }
.act-status-badge {
  font-family: 'Space Mono', monospace;
  font-size: 0.58rem; letter-spacing: 0.1em; font-weight: 700;
  padding: 0.2rem 0.6rem; border-radius: 20px;
}
.act-status-badge.completed { background: #f0fdf4; color: #16a34a; border: 1px solid #bbf7d0; }
.act-status-badge.cancelled { background: #f5f5f5; color: #888; border: 1px solid #e0e0e0; }
.act-date { font-size: 0.62rem; color: #aaa; }
.act-title { font-weight: 700; font-size: 0.95rem; color: #0a0a0a; margin-bottom: 0.3rem; line-height: 1.4; }
.act-meta { font-size: 0.68rem; color: #aaa; }

/* Panel */
.panel {
  max-width: 1300px; margin: 0 auto;
  padding: 0 2.5rem 4rem;
}
.panel-header {
  background: #fff;
  border: 1.5px solid #f0f0f0;
  border-radius: 20px 20px 0 0;
  padding: 1.5rem 1.75rem 1.25rem;
  border-bottom: none;
}
.panel-title-row {
  display: flex; align-items: center; gap: 1.25rem;
  margin-bottom: 1.25rem; flex-wrap: wrap;
}
.panel-title {
  font-family: 'Bebas Neue', sans-serif;
  font-size: 1.6rem; letter-spacing: 0.08em; margin: 0; color: #0a0a0a; flex: 1;
}
.panel-stats { display: flex; gap: 1.25rem; }
.pstat {
  font-family: 'Space Mono', monospace;
  font-size: 0.68rem; color: #888; letter-spacing: 0.04em;
}
.pstat strong { color: #0a0a0a; }
.pstat.accent strong { color: #ff2d6b; }
.pstat.warn strong { color: #d97706; }

/* Search + Filter (same as CheckInDashboard) */
.search-filter-row { display: flex; align-items: center; gap: 0.75rem; flex-wrap: wrap; }
.search-wrap { position: relative; flex: 1; min-width: 220px; }
.search-input {
  width: 100%; padding: 0.75rem 2.5rem 0.75rem 1.25rem;
  border: 1.5px solid #e8e8e8; border-radius: 24px;
  font-family: 'Noto Sans TC', sans-serif; font-size: 0.9rem;
  background: #fafafa; color: #0a0a0a; outline: none; transition: all 0.2s;
}
.search-input::placeholder { color: #ccc; }
.search-input:focus { border-color: #ff2d6b; background: #fff; }
.search-icon {
  position: absolute; right: 1rem; top: 50%;
  transform: translateY(-50%); color: #ccc; pointer-events: none;
}
.filter-pills { display: flex; gap: 0.4rem; flex-wrap: wrap; }
.pill {
  font-family: 'Space Mono', monospace;
  font-size: 0.62rem; letter-spacing: 0.08em;
  padding: 0.55rem 1rem;
  border: 1.5px solid #e8e8e8; border-radius: 20px;
  background: #fff; color: #888; cursor: pointer; transition: all 0.18s;
}
.pill.active { background: #0a0a0a; color: #fff; border-color: #0a0a0a; }
.pill:hover:not(.active) { border-color: #ff2d6b; color: #ff2d6b; }

/* Reg list */
.reg-list {
  border: 1.5px solid #f0f0f0; border-top: none;
  border-radius: 0 0 20px 20px; overflow: hidden;
  max-height: 520px; overflow-y: auto;
}
.reg-list::-webkit-scrollbar { width: 4px; }
.reg-list::-webkit-scrollbar-thumb { background: #e0e0e0; border-radius: 10px; }
.reg-list::-webkit-scrollbar-thumb:hover { background: #ff2d6b; }

.list-state {
  text-align: center; padding: 4rem;
  font-size: 0.72rem; letter-spacing: 0.15em; color: #ccc;
}
.reg-row {
  display: flex; justify-content: space-between; align-items: center;
  padding: 1rem 1.75rem;
  border-bottom: 1px solid #f5f5f5;
  background: #fff; transition: background 0.15s;
  border-left: 4px solid transparent;
}
.reg-row:last-child { border-bottom: none; }
.reg-row:hover { background: #fafafa; }
.reg-row.is-checked { border-left-color: #22c55e; background: #f0fdf4; }
.reg-row.is-late { border-left-color: #f59e0b; background: #fffbeb; }

.reg-left { display: flex; align-items: center; gap: 1rem; }
.reg-avatar {
  width: 40px; height: 40px; border-radius: 50%;
  background: #f0f0f0; color: #888;
  display: flex; align-items: center; justify-content: center;
  font-family: 'Bebas Neue', sans-serif; font-size: 1.1rem; flex-shrink: 0;
}
.reg-avatar.av-checked { background: #dcfce7; color: #16a34a; }
.reg-avatar.av-late { background: #fef3c7; color: #d97706; }
.reg-info { display: flex; flex-direction: column; gap: 2px; }
.reg-name { font-weight: 700; font-size: 0.95rem; color: #0a0a0a; }
.reg-meta, .check-meta {
  font-family: 'Space Mono', monospace;
  font-size: 0.62rem; color: #aaa; letter-spacing: 0.04em;
}
.meta-sep { margin: 0 0.3em; }
.late-pill {
  font-family: 'Space Mono', monospace; font-size: 0.55rem;
  background: #fef3c7; color: #92400e; padding: 1px 7px;
  border-radius: 10px; margin-left: 6px; font-weight: 700;
}
.reg-right { display: flex; align-items: center; gap: 0.75rem; }
.pay-badge {
  font-family: 'Space Mono', monospace;
  font-size: 0.58rem; letter-spacing: 0.06em;
  padding: 0.25rem 0.7rem; border-radius: 20px; font-weight: 600;
}
.pay-badge.paid    { background: #f0fdf4; color: #16a34a; border: 1px solid #bbf7d0; }
.pay-badge.pending { background: #fffbeb; color: #92400e; border: 1px solid #fde68a; }
.pay-badge.free    { background: #f5f3ff; color: #6d28d9; border: 1px solid #ddd6fe; }
.pay-badge.unpaid  { background: #fff1f2; color: #be123c; border: 1px solid #fecdd3; }
.status-tag {
  font-family: 'Space Mono', monospace;
  font-size: 0.6rem; letter-spacing: 0.06em; font-weight: 700;
  padding: 0.25rem 0.75rem; border-radius: 20px;
}
.status-tag.attended { background: #dcfce7; color: #16a34a; }
.status-tag.registered { background: #f5f5f5; color: #888; }
.status-tag.absent { background: #fff7ed; color: #c2410c; }

@media (max-width: 768px) {
  .page-header, .section-label, .activity-cards, .panel {
    padding-left: 1.25rem; padding-right: 1.25rem;
  }
  .page-title { font-size: 3rem; }
  .activity-cards { grid-template-columns: 1fr; }
}
</style>
