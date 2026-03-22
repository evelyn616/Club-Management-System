<template>
  <div class="ci-wrap">


    <!-- ── Page Header ── -->
    <div class="page-header">
      <div class="header-left">
        <div class="eyebrow">
          <span class="eyebrow-line"></span>
          <span class="eyebrow-text">CHECK-IN DASHBOARD</span>
        </div>
        <h1 class="page-title">
          簽到<span class="title-accent">管理</span>
        </h1>
        <p class="page-sub" v-if="currentActivity">{{ currentActivity.title }}</p>
        <p class="page-sub empty-sub" v-else>請選擇活動開始管理簽到</p>
      </div>

      <div class="header-actions">
        <!-- Activity Select -->
        <div class="activity-selector">
          <label class="selector-label">SELECT ACTIVITY</label>
          <select v-model="selectedActivityId" @change="onActivityChange" class="activity-select">
            <option value="">-- 選擇活動 --</option>
            <option v-for="act in activeActivities" :key="act.id" :value="act.id">
              {{ act.title }} ({{ formatDate(act.startTime) }})
            </option>
          </select>
        </div>
        <!-- QR Button -->
        <button class="qr-btn" @click="showQrModal = true" :disabled="!selectedActivityId">
          <span class="qr-icon">▣</span>
          QR Code
        </button>
        <!-- History Button -->
        <button class="history-btn" @click="router.push({ name: 'checkin-history' })">
          歷史紀錄 →
        </button>
      </div>
    </div>

    <!-- ── Stats Row ── -->
    <div class="stats-row" v-if="selectedActivityId">
      <div class="stat-card">
        <span class="stat-label">TOTAL</span>
        <span class="stat-num">{{ stats.total }}</span>
        <span class="stat-desc">總報名人數</span>
      </div>
      <div class="stat-card accent">
        <span class="stat-label">CHECKED IN</span>
        <span class="stat-num">{{ stats.checkedIn }}</span>
        <span class="stat-desc">已簽到</span>
      </div>
      <div class="stat-card neutral">
        <span class="stat-label">PENDING</span>
        <span class="stat-num">{{ stats.total - stats.checkedIn }}</span>
        <span class="stat-desc">未簽到</span>
      </div>
      <div class="stat-card warn">
        <span class="stat-label">LATE</span>
        <span class="stat-num">{{ stats.late }}</span>
        <span class="stat-desc">遲到人數</span>
      </div>
      <div class="stat-card rate">
        <span class="stat-label">RATE</span>
        <span class="stat-num pink">{{ stats.total > 0 ? Math.round((stats.checkedIn / stats.total) * 100) : 0 }}%</span>
        <div class="progress-bar">
          <div class="progress-fill" :style="{ width: stats.total > 0 ? (stats.checkedIn / stats.total * 100) + '%' : '0%' }"></div>
        </div>
      </div>
    </div>

    <!-- ── Empty (no activity selected) ── -->
    <div class="empty-wrap" v-if="!selectedActivityId">
      <div class="empty-big">SELECT</div>
      <p class="empty-desc mono">請從上方選擇活動</p>
    </div>

    <!-- ── Check-in Panel ── -->
    <div class="panel" v-if="selectedActivityId">
      <div class="panel-header">
        <div class="panel-title-row">
          <h2 class="panel-title">手動簽到</h2>
          <span class="admin-badge">ADMIN</span>
        </div>

        <!-- Search + Filter -->
        <div class="search-filter-row">
          <div class="search-wrap">
            <input
              v-model="searchKeyword"
              placeholder="搜尋姓名或 ID..."
              class="search-input"
            />
            <span class="search-icon">→</span>
          </div>
          <div class="filter-pills">
            <button
              v-for="f in filters"
              :key="f.value"
              :class="['pill', { active: activeFilter === f.value }]"
              @click="setFilter(f.value)"
            >{{ f.label }}</button>
          </div>
        </div>
      </div>

      <!-- List -->
      <div class="reg-list">
        <div v-if="loading" class="list-state mono">LOADING...</div>
        <div v-else-if="filteredRegistrations.length === 0" class="list-state mono">
          沒有符合的紀錄
        </div>
        <div
          v-for="reg in filteredRegistrations"
          :key="reg.id"
          class="reg-row"
          :class="{
            'is-checked': reg.status === 'ATTENDED' && !reg.late,
            'is-late': reg.status === 'ATTENDED' && reg.late === true
          }"
        >
          <!-- Left -->
          <div class="reg-left">
            <div class="reg-avatar" :class="{
              'av-checked': reg.status === 'ATTENDED' && !reg.late,
              'av-late': reg.status === 'ATTENDED' && reg.late === true
            }">
              {{ (reg.userName || reg.userId || '?')[0].toUpperCase() }}
            </div>
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

          <!-- Right -->
          <div class="reg-right">
            <span class="pay-badge" :class="reg.paymentStatus?.toLowerCase()">
              {{ paymentLabel(reg.paymentStatus) }}
            </span>
            <button
              v-if="reg.status === 'REGISTERED'"
              class="checkin-btn"
              @click="handleManualCheckIn(reg)"
              :disabled="checkingIn === reg.id"
            >
              {{ checkingIn === reg.id ? '...' : '✓ 簽到' }}
            </button>
            <span v-else-if="reg.status === 'ATTENDED'" class="done-badge">✓ 已簽到</span>
          </div>
        </div>
      </div>
    </div>

    <!-- ── QR Code Modal ── -->
    <Teleport to="body">
      <div v-if="showQrModal" class="modal-overlay" @click.self="showQrModal = false">
        <div class="modal-box">
          <button class="modal-close" @click="showQrModal = false">×</button>
          <div class="modal-eyebrow mono">QR CODE</div>
          <h3 class="modal-title">{{ currentActivity?.title }}</h3>
          <div class="qr-wrap">
            <img :src="qrCodeUrl" alt="QR Code" class="qr-img" v-if="qrCodeUrl" />
            <div class="qr-loading mono" v-else>產生中...</div>
          </div>
          <p class="qr-hint mono">會員掃碼後可自助簽到</p>
          <div class="qr-url mono">{{ checkinUrl }}</div>
          <a class="qr-download" :href="qrCodeUrl" target="_blank" download>下載 QR Code →</a>
        </div>
      </div>
    </Teleport>

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

const activities = ref([])
const selectedActivityId = ref(route.query.activityId || '')

// 過濾掉已完成/已取消，並依開始時間由近到遠排序
const activeActivities = computed(() =>
  [...activities.value]
    .filter(a => a.status !== 'COMPLETED' && a.status !== 'CANCELLED')
    .sort((a, b) => new Date(a.startTime) - new Date(b.startTime))
)

const currentActivity = computed(() => activities.value.find(a => a.id == selectedActivityId.value))
const registrations = ref([])
const loading = ref(false)
const checkingIn = ref(null)
const showQrModal = ref(false)
const searchKeyword = ref('')
const activeFilter = ref('all')
const stats = ref({ total: 0, checkedIn: 0, late: 0 })

const filters = [
  { label: '全部', value: 'all' },
  { label: '未簽到', value: 'pending' },
  { label: '已簽到', value: 'checkedIn' },
  { label: '遲到', value: 'late' },
]
const toast = ref({ show: false, message: '', type: '' })

// Navbar scroll
const navHidden = ref(false)
let lastY = 0
const onScroll = () => {
  const y = window.scrollY
  navHidden.value = y > lastY && y > 60
  lastY = y
}
onMounted(async () => {
  window.addEventListener('scroll', onScroll, { passive: true })
  try {
    const res = await activityApi.getAllActivities()
    activities.value = res.data || []
    if (selectedActivityId.value) await onActivityChange()
  } catch (e) { console.error(e) }
})
onUnmounted(() => window.removeEventListener('scroll', onScroll))

// QR
const checkinUrl = computed(() =>
  selectedActivityId.value
    ? `${window.location.origin}/checkin/member?activityId=${selectedActivityId.value}`
    : ''
)
const qrCodeUrl = computed(() =>
  selectedActivityId.value
    ? `https://api.qrserver.com/v1/create-qr-code/?size=240x240&data=${encodeURIComponent(checkinUrl.value)}`
    : ''
)

// Filter
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

function setFilter(val) { activeFilter.value = val }

async function onActivityChange() {
  if (!selectedActivityId.value) return
  loading.value = true
  try {
    const [regRes, countRes, checkinCountRes] = await Promise.all([
      registrationApi.getActivityRegistrations(selectedActivityId.value),
      registrationApi.countRegistrations(selectedActivityId.value),
      registrationApi.countCheckedIn(selectedActivityId.value),
    ])
    registrations.value = regRes.data || []
    const lateCount = registrations.value.filter(r => r.late === true).length
    stats.value = {
      total: countRes.data || 0,
      checkedIn: checkinCountRes.data || 0,
      late: lateCount,
    }
  } catch (e) {
    showToast('載入資料失敗', 'error')
  } finally {
    loading.value = false
  }
}

async function handleManualCheckIn(reg) {
  checkingIn.value = reg.id
  try {
    const res = await registrationApi.checkIn(reg.id)
    reg.status = res.data.status
    reg.checkInTime = res.data.checkInTime
    reg.late = res.data.late
    stats.value.checkedIn++
    if (reg.late === true) stats.value.late++
    showToast(`✓ ${reg.userName || reg.userId} 簽到成功`, 'success')
  } catch (e) {
    showToast(e.response?.data?.message || '簽到失敗', 'error')
  } finally {
    checkingIn.value = null
  }
}

function paymentLabel(s) {
  return ({ PAID: '已繳費', PENDING: '待繳費', FREE: '免費', UNPAID: '未繳費' })[s] || s || '—'
}

function formatDate(dt) {
  if (!dt) return '—'
  const d = new Date(dt)
  return `${String(d.getMonth()+1).padStart(2,'0')}/${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
}

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

.ci-wrap {
  min-height: 100vh;
  background: #ffffff;
  font-family: 'Noto Sans TC', sans-serif;
  color: #0a0a0a;
}
.mono { font-family: 'Space Mono', monospace; }

/* ── Navbar ── */

.navbar-hidden { transform: translateY(-100%); }
.nav-inner {
  max-width: 1300px; margin: 0 auto;
  padding: 1rem 2.5rem;
  display: flex; align-items: center; gap: 1.5rem;
}
.back-btn {
  font-family: 'Space Mono', monospace;
  font-size: 0.68rem; letter-spacing: 0.1em;
  background: none; border: 1px solid #e0e0e0;
  border-radius: 20px; padding: 0.4rem 1rem;
  cursor: pointer; color: #888;
  transition: all 0.18s;
}
.back-btn:hover { border-color: #0a0a0a; color: #0a0a0a; }
.nav-logo {
  font-family: 'Space Mono', monospace;
  font-size: 0.88rem; font-weight: 700; letter-spacing: 0.18em; color: #0a0a0a;
  flex: 1;
}
.nav-crumb {
  font-family: 'Space Mono', monospace;
  font-size: 0.62rem; letter-spacing: 0.12em; color: #aaa;
}
.nav-accent { color: #ff2d6b; }

/* ── Page Header ── */
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
.page-sub { font-size: 0.88rem; color: #555; margin: 0.4rem 0 0; }
.empty-sub { color: #ccc; }

.header-actions { display: flex; align-items: flex-end; gap: 1rem; flex-wrap: wrap; }
.activity-selector { display: flex; flex-direction: column; gap: 0.4rem; }
.selector-label {
  font-family: 'Space Mono', monospace;
  font-size: 0.58rem; letter-spacing: 0.18em; color: #aaa;
}
.activity-select {
  padding: 0.7rem 1.2rem;
  border: 1.5px solid #e0e0e0;
  border-radius: 24px;
  font-family: 'Noto Sans TC', sans-serif;
  font-size: 0.88rem; color: #0a0a0a;
  background: #fff; outline: none;
  cursor: pointer; min-width: 260px;
  transition: border-color 0.2s;
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='8' viewBox='0 0 12 8'%3E%3Cpath d='M1 1l5 5 5-5' stroke='%23aaa' stroke-width='1.5' fill='none' stroke-linecap='round'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 1rem center;
  padding-right: 2.5rem;
}
.activity-select:focus { border-color: #ff2d6b; }

.qr-btn {
  display: flex; align-items: center; gap: 0.5rem;
  padding: 0.7rem 1.5rem;
  background: #0a0a0a; color: #fff; border: none;
  border-radius: 24px;
  font-family: 'Space Mono', monospace;
  font-size: 0.68rem; font-weight: 700; letter-spacing: 0.08em;
  cursor: pointer; transition: all 0.2s;
  white-space: nowrap;
}
.qr-btn:hover:not(:disabled) { background: #ff2d6b; }
.qr-btn:disabled { opacity: 0.35; cursor: not-allowed; }
.qr-icon { font-size: 0.9rem; }
.history-btn {
  display: flex; align-items: center;
  padding: 0.7rem 1.5rem;
  background: transparent; color: #555; border: 1.5px solid #e0e0e0;
  border-radius: 24px;
  font-family: 'Space Mono', monospace;
  font-size: 0.68rem; font-weight: 700; letter-spacing: 0.08em;
  cursor: pointer; transition: all 0.2s;
  white-space: nowrap;
}
.history-btn:hover { border-color: #0a0a0a; color: #0a0a0a; }

/* ── Stats Row ── */
.stats-row {
  max-width: 1300px; margin: 0 auto;
  padding: 0 2.5rem;
  display: grid; grid-template-columns: repeat(5, 1fr);
  gap: 1rem;
  padding-top: 2rem; padding-bottom: 2rem;
}
.stat-card {
  background: #fff;
  border: 1.5px solid #f0f0f0;
  border-radius: 20px;
  padding: 1.5rem 1.25rem;
  display: flex; flex-direction: column; gap: 0.3rem;
  transition: box-shadow 0.2s;
}
.stat-card:hover { box-shadow: 0 4px 20px rgba(0,0,0,0.06); }
.stat-label {
  font-family: 'Space Mono', monospace;
  font-size: 0.58rem; letter-spacing: 0.18em; color: #bbb;
}
.stat-num {
  font-family: 'Bebas Neue', sans-serif;
  font-size: 3.2rem; line-height: 1; letter-spacing: 0.02em; color: #0a0a0a;
}
.stat-num.pink { color: #ff2d6b; }
.stat-card.accent .stat-num { color: #ff2d6b; }
.stat-card.neutral .stat-num { color: #888; }
.stat-card.warn .stat-num { color: #d97706; }
.stat-desc { font-size: 0.78rem; color: #888; }
.progress-bar { height: 4px; background: #f0f0f0; border-radius: 10px; margin-top: 0.75rem; }
.progress-fill { height: 100%; background: #ff2d6b; border-radius: 10px; transition: width 0.7s cubic-bezier(0.16,1,0.3,1); }

/* ── Empty ── */
.empty-wrap { text-align: center; padding: 5rem 2rem; }
.empty-big {
  font-family: 'Bebas Neue', sans-serif; font-size: 7rem;
  color: transparent; -webkit-text-stroke: 1px #eee; line-height: 1;
}
.empty-desc { font-size: 0.72rem; letter-spacing: 0.15em; color: #ccc; margin-top: 0.5rem; }

/* ── Panel ── */
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
.panel-title-row { display: flex; align-items: center; gap: 0.85rem; margin-bottom: 1.25rem; }
.panel-title {
  font-family: 'Bebas Neue', sans-serif;
  font-size: 1.8rem; letter-spacing: 0.08em; margin: 0; color: #0a0a0a;
}
.admin-badge {
  font-family: 'Space Mono', monospace;
  font-size: 0.56rem; letter-spacing: 0.12em;
  background: #fff5f7; border: 1px solid #fecdd3;
  color: #ff2d6b; padding: 0.22rem 0.75rem;
  border-radius: 20px; font-weight: 700;
}

/* Search + Filter */
.search-filter-row { display: flex; align-items: center; gap: 0.75rem; flex-wrap: wrap; }
.search-wrap { position: relative; flex: 1; min-width: 220px; }
.search-input {
  width: 100%; padding: 0.75rem 2.5rem 0.75rem 1.25rem;
  border: 1.5px solid #e8e8e8; border-radius: 24px;
  font-family: 'Noto Sans TC', sans-serif; font-size: 0.9rem;
  background: #fafafa; color: #0a0a0a; outline: none;
  transition: all 0.2s;
}
.search-input::placeholder { color: #ccc; }
.search-input:focus { border-color: #ff2d6b; background: #fff; }
.search-icon {
  position: absolute; right: 1rem; top: 50%;
  transform: translateY(-50%); color: #ccc; pointer-events: none;
  font-size: 0.9rem;
}
.filter-pills { display: flex; gap: 0.4rem; flex-wrap: wrap; }
.pill {
  font-family: 'Space Mono', monospace;
  font-size: 0.62rem; letter-spacing: 0.08em;
  padding: 0.55rem 1rem;
  border: 1.5px solid #e8e8e8; border-radius: 20px;
  background: #fff; color: #888; cursor: pointer;
  transition: all 0.18s; white-space: nowrap;
}
.pill.active { background: #0a0a0a; color: #fff; border-color: #0a0a0a; }
.pill:hover:not(.active) { border-color: #ff2d6b; color: #ff2d6b; }

/* ── Registration List ── */
.reg-list {
  border: 1.5px solid #f0f0f0;
  border-top: none;
  border-radius: 0 0 20px 20px;
  overflow: hidden;
  max-height: 560px; overflow-y: auto;
}
.reg-list::-webkit-scrollbar { width: 4px; }
.reg-list::-webkit-scrollbar-track { background: #fafafa; }
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
  background: #fff;
  transition: background 0.15s;
  border-left: 4px solid transparent;
}
.reg-row:last-child { border-bottom: none; }
.reg-row:hover { background: #fafafa; }
.reg-row.is-checked { border-left-color: #22c55e; background: #f0fdf4; }
.reg-row.is-late { border-left-color: #f59e0b; background: #fffbeb; }

/* Avatar */
.reg-left { display: flex; align-items: center; gap: 1rem; }
.reg-avatar {
  width: 40px; height: 40px; border-radius: 50%;
  background: #f0f0f0; color: #888;
  display: flex; align-items: center; justify-content: center;
  font-family: 'Bebas Neue', sans-serif; font-size: 1.1rem;
  flex-shrink: 0;
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
  font-family: 'Space Mono', monospace;
  font-size: 0.55rem; letter-spacing: 0.1em;
  background: #fef3c7; color: #92400e;
  padding: 1px 7px; border-radius: 10px;
  margin-left: 6px; font-weight: 700;
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

.checkin-btn {
  font-family: 'Space Mono', monospace;
  font-size: 0.65rem; font-weight: 700; letter-spacing: 0.08em;
  background: #ff2d6b; color: #fff; border: none;
  border-radius: 20px; padding: 0.55rem 1.25rem;
  cursor: pointer; transition: all 0.18s;
  box-shadow: 0 2px 10px rgba(255,45,107,0.25);
}
.checkin-btn:hover:not(:disabled) { background: #e01f5a; box-shadow: 0 4px 16px rgba(255,45,107,0.35); }
.checkin-btn:disabled { opacity: 0.35; cursor: not-allowed; box-shadow: none; }
.done-badge {
  font-family: 'Space Mono', monospace;
  font-size: 0.65rem; color: #16a34a; font-weight: 700; letter-spacing: 0.06em;
}

/* ── QR Modal ── */
.modal-overlay {
  position: fixed; inset: 0;
  background: rgba(0,0,0,0.45);
  backdrop-filter: blur(8px);
  display: flex; align-items: center; justify-content: center;
  z-index: 1000;
}
.modal-box {
  background: #fff;
  border-radius: 24px;
  width: 380px; max-width: 90vw;
  padding: 2rem;
  text-align: center;
  box-shadow: 0 24px 60px rgba(0,0,0,0.15);
  position: relative;
}
.modal-close {
  position: absolute; top: 1rem; right: 1.25rem;
  background: none; border: none; font-size: 1.4rem;
  color: #ccc; cursor: pointer; line-height: 1;
  transition: color 0.15s;
}
.modal-close:hover { color: #ff2d6b; }
.modal-eyebrow {
  font-size: 0.58rem; letter-spacing: 0.2em; color: #ff2d6b; margin-bottom: 0.4rem;
}
.modal-title {
  font-family: 'Bebas Neue', sans-serif;
  font-size: 1.5rem; letter-spacing: 0.06em; margin: 0 0 1.25rem; color: #0a0a0a;
}
.qr-wrap {
  display: flex; justify-content: center;
  margin-bottom: 1rem;
}
.qr-img {
  border-radius: 16px;
  border: 1.5px solid #f0f0f0;
  box-shadow: 0 4px 16px rgba(0,0,0,0.08);
}
.qr-loading {
  width: 240px; height: 240px;
  background: #fafafa; border-radius: 16px;
  display: flex; align-items: center; justify-content: center;
  font-size: 0.68rem; letter-spacing: 0.12em; color: #ccc;
}
.qr-hint { font-size: 0.62rem; letter-spacing: 0.12em; color: #aaa; margin-bottom: 0.75rem; }
.qr-url {
  background: #fafafa; border: 1px solid #f0f0f0;
  border-radius: 12px; padding: 0.65rem 1rem;
  font-size: 0.6rem; color: #aaa;
  word-break: break-all; text-align: left;
  margin-bottom: 1rem;
}
.qr-download {
  display: inline-block;
  font-family: 'Space Mono', monospace;
  font-size: 0.65rem; letter-spacing: 0.1em;
  color: #ff2d6b; text-decoration: none; font-weight: 700;
  transition: opacity 0.15s;
}
.qr-download:hover { opacity: 0.7; }

/* ── Toast ── */
.toast {
  position: fixed; bottom: 2.5rem; left: 50%;
  transform: translateX(-50%);
  padding: 0.85rem 2rem;
  font-family: 'Space Mono', monospace;
  font-size: 0.72rem; letter-spacing: 0.08em; font-weight: 700;
  border-radius: 24px; z-index: 2000;
  white-space: nowrap;
  animation: slideUp 0.3s cubic-bezier(0.16,1,0.3,1);
}
.toast.success { background: #0a0a0a; color: #fff; }
.toast.error   { background: #ff2d6b; color: #fff; }
@keyframes slideUp {
  from { opacity: 0; transform: translateX(-50%) translateY(12px); }
  to   { opacity: 1; transform: translateX(-50%) translateY(0); }
}

/* ── Responsive ── */
@media (max-width: 1024px) {
  .stats-row { grid-template-columns: repeat(3, 1fr); }
}
@media (max-width: 768px) {
  .stats-row { grid-template-columns: repeat(2, 1fr); }
  .page-header { flex-direction: column; align-items: flex-start; }
  .page-header, .stats-row, .panel { padding-left: 1.25rem; padding-right: 1.25rem; }
  .nav-inner { padding-left: 1.25rem; padding-right: 1.25rem; }
  .header-actions { width: 100%; }
  .activity-select { min-width: 100%; }
}
</style>