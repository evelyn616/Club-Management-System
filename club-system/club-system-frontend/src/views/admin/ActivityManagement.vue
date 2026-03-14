<template>
    <nav class="admin-navbar">
      <div class="nav-container">
        <h2 class="logo">管理後台</h2>
        <div class="nav-right">
          <span class="admin-name">{{  userStore.userName || '管理員' }}</span>
          <button @click="handleLogout" class="btn-logout">登出</button>
        </div>
      </div>
    </nav>

    
  <div class="am-wrap">

    <!-- ===== 頂部：標題 + 快速功能按鈕 ===== -->
    <div class="am-topbar">
      <div class="am-title-block">
        <span class="am-eyebrow">ADMIN</span>
        <h1 class="am-title">活動管理</h1>
      </div>
      <div class="am-actions">
        <button class="action-btn primary" @click="router.push('/admin/create-activity-container')">
          <span class="btn-icon">＋</span> 新增活動
        </button>
        <button class="action-btn" @click="router.push('/admin/activity-list-container')">
          <span class="btn-icon">☰</span> 活動列表
        </button>
        <button class="action-btn" @click="router.push('/admin/publish-activity-container')">
          <span class="btn-icon">🚀</span> 發布活動
        </button>
        <button class="action-btn" @click="router.push('/admin/checkin/dashboard')">
          <span class="btn-icon">✓</span> 簽到管理
        </button>
        <button class="action-btn" @click="router.push('/admin/draft-box-container')">
          <span class="btn-icon">📝</span> 草稿箱
        </button>
      </div>
    </div>


    <!-- ===== Dashboard Grid ===== -->
    <div class="am-grid">

      <!-- KPI 區：橫排四格 -->
      <div class="grid-kpi">
        <div class="kpi-card" v-for="kpi in kpiList" :key="kpi.label">
          <span class="kpi-label">{{ kpi.label }}</span>
          <span class="kpi-value">{{ kpi.value }}</span>
          <span class="kpi-tag" :class="kpi.tagClass">{{ kpi.tag }}</span>
        </div>
      </div>

      <!-- 左欄：狀態總覽 + 待辦 -->
      <div class="grid-left">

        <!-- 狀態總覽 -->
        <div class="panel">
          <div class="panel-header">
            <span class="panel-title">活動狀態總覽</span>
            <button class="panel-link" @click="router.push('/admin/activity-list-container')">全部 →</button>
          </div>
          <div class="status-grid">
            <div class="status-card" v-for="s in statusList" :key="s.key"
                 @click="goToList(s.key)" :class="'s-' + s.key.toLowerCase()">
              <span class="sc-num">{{ statusStats[s.key] }}</span>
              <span class="sc-label">{{ s.label }}</span>
              <span class="sc-arrow">→</span>
            </div>
          </div>
        </div>

        <!-- 待辦事項 -->
        <div class="panel">
          <div class="panel-header">
            <span class="panel-title">待辦事項</span>
          </div>
          <div v-if="pendingTasks.urgent.length === 0 && pendingTasks.attention.length === 0" class="no-task">
            <span class="no-task-icon">✓</span>
            <span>目前沒有待辦事項</span>
          </div>
          <div v-else class="task-list">
            <div v-for="(task, i) in pendingTasks.urgent" :key="'u'+i" class="task-item urgent">
              <span class="task-dot"></span>{{ task }}
            </div>
            <div v-for="(task, i) in pendingTasks.attention" :key="'a'+i" class="task-item attention">
              <span class="task-dot"></span>{{ task }}
            </div>
          </div>
        </div>

      </div>

      <!-- 右欄：近期活動 -->
      <div class="grid-right">
        <div class="panel panel-full">
          <div class="panel-header">
            <span class="panel-title">近期活動</span>
            <button class="panel-link" @click="goToList('PUBLISHED')">已發布 →</button>
          </div>

          <div v-if="upcomingActivities.length === 0" class="no-task">
            <span class="no-task-icon">📅</span>
            <span>目前沒有即將舉辦的活動</span>
          </div>

          <div v-else class="upcoming-list">
            <div v-for="(act, idx) in upcomingActivities" :key="act.id"
                 class="upcoming-card" :class="idx === 0 ? 'uc-featured' : ''"
                 @click="goToDetail(act.id)">

              <div class="uc-top">
                <span class="uc-badge">{{ getTimeLabel(act) }}</span>
                <span class="uc-type">{{ act.activityType || '' }}</span>
              </div>

              <h4 class="uc-title">{{ act.title }}</h4>
              <p class="uc-meta">
                <span>{{ formatDateTime(act.startTime) }}</span>
                <span v-if="act.location" class="uc-sep">·</span>
                <span v-if="act.location">{{ act.location }}</span>
              </p>

              <!-- 容量進度 -->
              <div v-if="act.maxParticipants" class="uc-capacity">
                <div class="cap-bar">
                  <div class="cap-fill"
                       :style="{ width: Math.min((act.registrationCount || 0) / act.maxParticipants * 100, 100) + '%' }">
                  </div>
                </div>
                <span class="cap-text">{{ act.registrationCount || 0 }} / {{ act.maxParticipants }}</span>
              </div>

              <!-- 功能按鈕列 -->
              <div class="uc-btns" @click.stop>
                <button class="uc-btn" @click="openQrModal(act)">
                  QR 產生
                </button>
                
                <button class="uc-btn" @click="router.push('/admin/update-activity-container/' + act.id)">
                  更新活動
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

    </div>

    <!-- ===== QR Code Modal ===== -->
    <Teleport to="body">
      <div v-if="qrModal.show" class="qr-overlay" @click.self="closeQrModal">
        <div class="qr-modal">
          <button class="qr-close" @click="closeQrModal">×</button>
          <p class="qr-activity-name">{{ qrModal.activityTitle }}</p>
          <h2 class="qr-headline">QR Code 簽到</h2>
          <div class="qr-img-wrap">
            <img :src="qrModal.url" alt="QR Code" class="qr-img" />
          </div>
          <p class="qr-hint">請學員掃描此 QR Code 進行簽到</p>
          <div class="qr-actions">
            <button class="qr-btn" @click="router.push({ path: '/admin/checkin/dashboard', query: { activityId: qrModal.activityId } }); closeQrModal()">
              前往簽到頁面 →
            </button>
            <a class="qr-btn secondary" :href="qrModal.url" target="_blank" download>下載 QR Code</a>
          </div>
        </div>
      </div>
    </Teleport>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { activityApi } from '@/api/activity'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const router = useRouter()
const loading = ref(false)

// ===== KPI =====
const stats = ref({
  semesterActivities: 0,
  activityChange: 0,
  executionRate: 0,
  participationRate: 0,
  satisfactionRate: 0
})

const kpiList = computed(() => [
  {
    label: '本學期活動數',
    value: stats.value.semesterActivities,
    tag: `${stats.value.activityChange >= 0 ? '↑' : '↓'} ${Math.abs(stats.value.activityChange)} 場`,
    tagClass: stats.value.activityChange >= 0 ? 'tag-pos' : 'tag-neg'
  },
  {
    label: '活動執行率',
    value: stats.value.executionRate + '%',
    tag: getExecutionRateLabel(stats.value.executionRate),
    tagClass: stats.value.executionRate >= 80 ? 'tag-pos' : 'tag-warn'
  },
  {
    label: '活動參與率',
    value: stats.value.participationRate + '%',
    tag: getParticipationLabel(stats.value.participationRate),
    tagClass: getParticipationClass(stats.value.participationRate)
  },
  {
    label: '活動滿意度',
    value: 'N/A',
    tag: '待開發',
    tagClass: 'tag-warn'
  }
])

// ===== Status =====
const statusStats = ref({ DRAFT: 0, PUBLISHED: 0, COMPLETED: 0, CANCELLED: 0 })
const statusList = [
  { key: 'DRAFT',     label: '草稿中' },
  { key: 'PUBLISHED', label: '已發布' },
  { key: 'COMPLETED', label: '已完成' },
  { key: 'CANCELLED', label: '已取消' }
]

// ===== Pending Tasks =====
const pendingTasks = ref({ urgent: [], attention: [] })

const calculatePendingTasks = (activities) => {
  const urgent = []
  const attention = []
  const now = new Date()

  activities.forEach(activity => {
    if (activity.status !== 'PUBLISHED') return
    const startTime = new Date(activity.startTime)
    const hoursUntilStart = (startTime - now) / (1000 * 60 * 60)

    if (hoursUntilStart > 0 && hoursUntilStart <= 24)
      urgent.push(`「${activity.title}」將於 ${startTime.toLocaleString()} 開始`)

    if (activity.maxParticipants && activity.registrationCount) {
      const rate = activity.registrationCount / activity.maxParticipants
      if (rate > 0.9)
        urgent.push(`「${activity.title}」報名即將額滿 (${Math.round(rate * 100)}%)`)
    }

    if (hoursUntilStart > 24 && hoursUntilStart <= 72)
      attention.push(`「${activity.title}」將於 ${startTime.toLocaleString()} 開始`)
  })

  const draftCount = activities.filter(a => a.status === 'DRAFT').length
  if (draftCount > 5)
    attention.push(`目前有 ${draftCount} 個草稿活動，請盡快完成發布`)

  pendingTasks.value = { urgent, attention }
}

// ===== Upcoming =====
const upcomingActivities = ref([])

// ===== QR Modal =====
const qrModal = ref({ show: false, activityId: null, activityTitle: '', url: '' })

const openQrModal = (activity) => {
  const checkinUrl = `${window.location.origin}/checkin/${activity.id}`
  qrModal.value = {
    show: true,
    activityId: activity.id,
    activityTitle: activity.title,
    url: `https://api.qrserver.com/v1/create-qr-code/?data=${encodeURIComponent(checkinUrl)}&size=220x220&margin=16`
  }
}
const closeQrModal = () => { qrModal.value.show = false }

// ===== Helpers =====
const getTimeLabel = (activity) => {
  const diffDays = Math.floor((new Date(activity.startTime) - new Date()) / (1000 * 60 * 60 * 24))
  if (diffDays === 0) return '今天'
  if (diffDays === 1) return '明天'
  if (diffDays === 2) return '後天'
  return `${diffDays} 天後`
}

const formatDateTime = (dt) => {
  if (!dt) return '-'
  return new Date(dt).toLocaleString('zh-TW', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit'
  })
}

const getExecutionRateLabel = (r) => r >= 80 ? '高' : r >= 50 ? '中' : '低'
const getParticipationLabel = (r) => r >= 80 ? '良好' : r >= 50 ? '普通' : '需改善'
const getParticipationClass = (r) => r >= 80 ? 'tag-pos' : r >= 50 ? 'tag-warn' : 'tag-neg'
const getSatisfactionLabel = (r) => r >= 4 ? '高' : r >= 3 ? '中' : '低'
const getSatisfactionClass = (r) => r >= 4 ? 'tag-pos' : r >= 3 ? 'tag-warn' : 'tag-neg'

const goToDetail = (id) => router.push('/admin/update-activity-container/' + id)
const goToList = (status) => router.push({ path: '/admin/activity-list-container', query: { status } })

// ===== Load =====
const loadStatusStats = async () => {
  try {
    loading.value = true
    const response = await activityApi.getAllActivities()
    const activities = response.data
    if (!Array.isArray(activities)) throw new Error('格式錯誤')

    // 狀態統計
    statusStats.value = {
      DRAFT:     activities.filter(a => a.status === 'DRAFT').length,
      PUBLISHED: activities.filter(a => a.status === 'PUBLISHED').length,
      COMPLETED: activities.filter(a => a.status === 'COMPLETED').length,
      CANCELLED: activities.filter(a => a.status === 'CANCELLED').length
    }

    // KPI 計算
    const currentYear = new Date().getFullYear()
    const semesterCount = activities.filter(a =>
      a.startTime && new Date(a.startTime).getFullYear() === currentYear
    ).length

    const finished = statusStats.value.COMPLETED + statusStats.value.CANCELLED
    const executionRate = finished > 0
      ? Math.round(statusStats.value.COMPLETED / finished * 100)
      : 0

    const completedWithCap = activities.filter(a => a.status === 'COMPLETED' && a.maxParticipants > 0)
    const participationRate = completedWithCap.length > 0
      ? Math.round(
          completedWithCap.reduce((sum, a) =>
            sum + Math.min((a.registrationCount || 0) / a.maxParticipants, 1), 0
          ) / completedWithCap.length * 100
        )
      : 0

    stats.value = {
      semesterActivities: semesterCount,
      activityChange: 0,
      executionRate,
      participationRate,
      satisfactionRate: 0
    }

    // 待辦 & 近期活動
    calculatePendingTasks(activities)

    const now = new Date()
    upcomingActivities.value = activities
      .filter(a => a.status === 'PUBLISHED' && a.startTime && new Date(a.startTime) >= now)
      .sort((a, b) => new Date(a.startTime) - new Date(b.startTime))
      .slice(0, 3)

  } catch (e) {
    console.error('載入失敗:', e)
  } finally {
    loading.value = false
  }
}

const handleLogout = () => {
  if (confirm('確定要登出嗎？')) {
    userStore.logout()
    router.push('/admin/login')
  }
}

onMounted(() => {
  loadStatusStats()
})
</script>

<style scoped>
/* ===== 基底 ===== */
.am-wrap {
  padding: 2rem;
  max-width: 1400px;
  margin: 0 auto;
  font-family: 'Noto Sans TC', sans-serif;
  color: #111;
}

/* ===== Topbar ===== */
.am-topbar {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 2rem;
  flex-wrap: wrap;
  gap: 1rem;
}
.am-eyebrow {
  display: block;
  font-size: 0.65rem;
  letter-spacing: 0.2em;
  color: #ff2d6b;
  font-weight: 700;
  margin-bottom: 0.25rem;
}
.am-title {
  font-size: 1.75rem;
  font-weight: 800;
  margin: 0;
  color: #111;
}
.am-actions {
  display: flex;
  gap: 0.6rem;
  flex-wrap: wrap;
}
.action-btn {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.55rem 1.1rem;
  border: 1px solid #ddd;
  background: #fff;
  font-size: 0.82rem;
  font-weight: 600;
  color: #333;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.18s;
  white-space: nowrap;
}
.action-btn:hover { border-color: #aaa; background: #f7f7f7; }
.action-btn.primary {
  background: #111; color: #fff; border-color: #111;
}
.action-btn.primary:hover { background: #ff2d6b; border-color: #ff2d6b; }
.btn-icon { font-size: 0.9rem; }

/* ===== Grid ===== */
.am-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  grid-template-rows: auto auto;
  gap: 1.25rem;
}
.grid-kpi {
  grid-column: 1 / -1;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1rem;
}
.grid-left  { display: flex; flex-direction: column; gap: 1.25rem; }
.grid-right { display: flex; flex-direction: column; }

/* ===== KPI Card ===== */
.kpi-card {
  background: #fff;
  border: 1px solid #eee;
  border-radius: 10px;
  padding: 1.4rem 1.25rem;
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
  transition: box-shadow 0.2s;
}
.kpi-card:hover { box-shadow: 0 4px 16px rgba(0,0,0,0.08); }
.kpi-label { font-size: 0.75rem; color: #888; font-weight: 500; }
.kpi-value { font-size: 2rem; font-weight: 800; color: #111; line-height: 1; }
.kpi-tag {
  display: inline-block;
  font-size: 0.72rem;
  font-weight: 700;
  padding: 0.2rem 0.55rem;
  border-radius: 4px;
  width: fit-content;
}
.tag-pos  { background: #e8f5e9; color: #2e7d32; }
.tag-neg  { background: #ffebee; color: #c62828; }
.tag-warn { background: #fff3e0; color: #e65100; }

/* ===== Panel ===== */
.panel {
  background: #fff;
  border: 1px solid #eee;
  border-radius: 10px;
  padding: 1.25rem 1.4rem;
}
.panel-full { flex: 1; }
.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1.1rem;
}
.panel-title { font-size: 0.9rem; font-weight: 700; color: #111; }
.panel-link {
  font-size: 0.75rem; color: #ff2d6b; background: none;
  border: none; cursor: pointer; font-weight: 600;
}

/* ===== Status Grid ===== */
.status-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 0.75rem;
}
.status-card {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  padding: 1rem;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.15s, box-shadow 0.15s;
  border: 1px solid transparent;
  position: relative;
}
.status-card:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
.s-draft     { background: #f5f5f5; border-color: #e0e0e0; }
.s-published { background: #e8f5e9; border-color: #c8e6c9; }
.s-completed { background: #e3f2fd; border-color: #bbdefb; }
.s-cancelled { background: #ffebee; border-color: #ffcdd2; }

.sc-num   { font-size: 2rem; font-weight: 800; color: #111; line-height: 1; }
.sc-label { font-size: 0.78rem; color: #555; margin-top: 0.3rem; font-weight: 600; }
.sc-arrow {
  position: absolute; right: 0.85rem; bottom: 0.85rem;
  font-size: 0.8rem; color: #aaa;
}

/* ===== Task List ===== */
.no-task {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 2rem 0;
  gap: 0.5rem;
  color: #aaa;
  font-size: 0.85rem;
}
.no-task-icon { font-size: 1.75rem; }
.task-list { display: flex; flex-direction: column; gap: 0.5rem; }
.task-item {
  display: flex;
  align-items: flex-start;
  gap: 0.6rem;
  font-size: 0.82rem;
  color: #333;
  padding: 0.6rem 0.75rem;
  border-radius: 6px;
  line-height: 1.4;
}
.task-item.urgent    { background: #ffebee; }
.task-item.attention { background: #fff3e0; }
.task-dot {
  width: 6px; height: 6px; border-radius: 50%;
  flex-shrink: 0; margin-top: 5px;
}
.task-item.urgent .task-dot    { background: #e53935; }
.task-item.attention .task-dot { background: #fb8c00; }

/* ===== Upcoming Cards ===== */
.upcoming-list { display: flex; flex-direction: column; gap: 0.85rem; }
.upcoming-card {
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 1rem 1.1rem;
  cursor: pointer;
  transition: box-shadow 0.18s;
}
.upcoming-card:hover { box-shadow: 0 4px 14px rgba(0,0,0,0.08); }
.uc-featured {
  border-color: #111;
  background: #111;
  color: #fff;
}
.uc-top { display: flex; align-items: center; gap: 0.5rem; margin-bottom: 0.5rem; }
.uc-badge {
  font-size: 0.65rem; font-weight: 700; letter-spacing: 0.08em;
  padding: 0.15rem 0.5rem; border-radius: 3px;
  background: #ff2d6b; color: #fff;
}
.uc-featured .uc-badge { background: rgba(255,45,107,0.85); }
.uc-type { font-size: 0.68rem; color: #aaa; }
.uc-featured .uc-type { color: rgba(255,255,255,0.5); }
.uc-title { font-size: 1rem; font-weight: 700; margin: 0 0 0.35rem; line-height: 1.35; }
.uc-meta { font-size: 0.78rem; color: #888; margin: 0 0 0.65rem; display: flex; gap: 0.4rem; flex-wrap: wrap; }
.uc-featured .uc-meta { color: rgba(255,255,255,0.6); }
.uc-sep { color: #ccc; }

/* 容量 */
.uc-capacity { display: flex; align-items: center; gap: 0.6rem; margin-bottom: 0.85rem; }
.cap-bar { flex: 1; height: 3px; background: rgba(0,0,0,0.08); border-radius: 2px; overflow: hidden; }
.uc-featured .cap-bar { background: rgba(255,255,255,0.15); }
.cap-fill { height: 100%; background: #ff2d6b; border-radius: 2px; transition: width 0.4s; }
.cap-text { font-size: 0.7rem; color: #aaa; white-space: nowrap; }
.uc-featured .cap-text { color: rgba(255,255,255,0.5); }

/* 功能按鈕 */
.uc-btns { display: flex; gap: 0.5rem; flex-wrap: wrap; }
.uc-btn {
  flex: 1;
  padding: 0.45rem 0.6rem;
  font-size: 0.75rem;
  font-weight: 600;
  border: 1px solid rgba(0,0,0,0.15);
  background: rgba(255,255,255,0.12);
  color: #333;
  border-radius: 5px;
  cursor: pointer;
  transition: all 0.15s;
  white-space: nowrap;
  text-align: center;
}
.uc-btn:hover { background: #f0f0f0; border-color: #aaa; }
.uc-featured .uc-btn { color: #fff; border-color: rgba(255,255,255,0.25); background: rgba(255,255,255,0.1); }
.uc-featured .uc-btn:hover { background: rgba(255,255,255,0.22); border-color: rgba(255,255,255,0.5); }

/* ===== QR Modal ===== */
.qr-overlay {
  position: fixed; inset: 0;
  background: rgba(0,0,0,0.55);
  backdrop-filter: blur(4px);
  display: flex; align-items: center; justify-content: center;
  z-index: 1000;
}
.qr-modal {
  background: #fff;
  border-radius: 14px;
  padding: 2.5rem 2rem 2rem;
  width: 340px;
  text-align: center;
  position: relative;
  box-shadow: 0 20px 60px rgba(0,0,0,0.2);
}
.qr-close {
  position: absolute; top: 1rem; right: 1.1rem;
  background: none; border: none; font-size: 1.4rem;
  color: #aaa; cursor: pointer; line-height: 1;
}
.qr-close:hover { color: #111; }
.qr-activity-name { font-size: 0.75rem; color: #ff2d6b; font-weight: 700; letter-spacing: 0.08em; margin: 0 0 0.3rem; }
.qr-headline { font-size: 1.3rem; font-weight: 800; margin: 0 0 1.5rem; color: #111; }
.qr-img-wrap {
  display: inline-flex; padding: 0.75rem;
  border: 1px solid #eee; border-radius: 8px; margin-bottom: 1rem;
}
.qr-img { width: 180px; height: 180px; display: block; }
.qr-hint { font-size: 0.78rem; color: #aaa; margin: 0 0 1.25rem; }
.qr-actions { display: flex; flex-direction: column; gap: 0.5rem; }
.qr-btn {
  display: block; padding: 0.65rem;
  background: #111; color: #fff;
  border: none; border-radius: 7px;
  font-size: 0.82rem; font-weight: 700;
  cursor: pointer; text-decoration: none;
  transition: background 0.15s;
}
.qr-btn:hover { background: #ff2d6b; }
.qr-btn.secondary { background: #f5f5f5; color: #333; }
.qr-btn.secondary:hover { background: #e0e0e0; }

/* ===== Responsive ===== */
@media (max-width: 900px) {
  .am-grid { grid-template-columns: 1fr; }
  .grid-kpi { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 560px) {
  .grid-kpi { grid-template-columns: 1fr 1fr; }
  .am-actions { flex-wrap: wrap; }
}

.admin-navbar {
  background: white;
  border-bottom: 1px solid #e0e0e0;
  padding: 1rem 0;
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  font-size: 1.25rem;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}

.nav-right {
  display: flex;
  gap: 1.5rem;
  align-items: center;
}

.admin-name {
  color: #666;
  font-weight: 500;
}

.btn-logout {
  padding: 0.5rem 1.25rem;
  background: white;
  color: #1a1a1a;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-logout:hover {
  background: #f5f5f5;
  border-color: #ccc;
}
</style>