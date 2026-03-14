<template>
  <div class="al-wrap">

    <!-- Navbar -->
    <nav class="navbar" :class="{ 'navbar-hidden': navHidden }">
      <div class="nav-container">
        <router-link to="/admin/dashboard" class="nav-logo">管理後台</router-link>
        <div class="nav-right">
          <router-link to="/admin/activity-management-container" class="nav-link">← 活動管理</router-link>
        </div>
      </div>
    </nav>

    <!-- Header -->
    <div class="al-header">
      <div class="header-left">
        <span class="al-eyebrow">ADMIN / ACTIVITIES</span>
        <h1 class="al-title">活動列表</h1>
        <p class="al-count" v-if="!loading">
          <span class="mono">{{ filteredActivities.length }}</span> / {{ activities.length }} 筆活動
        </p>
      </div>
      <button class="btn-create" @click="router.push({ name: 'create-activity-container' })">
        ＋ 建立活動
      </button>
    </div>

    <!-- Filter Bar -->
    <div class="filter-bar">
      <div class="search-wrap">
        <input v-model="searchKeyword" type="text" placeholder="搜尋活動..." class="search-input" @input="handleSearch" />
        <span class="search-icon">→</span>
      </div>
      <div class="filter-selects">
        <select v-model="selectedActivityType" class="filter-select" @change="handleFilterChange">
          <option value="">TYPE: ALL</option>
          <option value="REGULAR">社課</option>
          <option value="SPECIAL">特殊活動</option>
          <option value="TRAINING">團練</option>
          <option value="PERFORMANCE">演出</option>
          <option value="COMPETITION">比賽</option>
        </select>
        <select v-model="selectedDanceStyle" class="filter-select" @change="handleFilterChange">
          <option value="">STYLE: ALL</option>
          <option v-for="style in danceStyles" :key="style" :value="style">{{ style }}</option>
        </select>
        <select v-model="selectedStatus" class="filter-select" @change="handleFilterChange">
          <option value="">STATUS: ALL</option>
          <option value="DRAFT">草稿</option>
          <option value="SCHEDULE">預約發布</option>
          <option value="PUBLISHED">已發布</option>
          <option value="COMPLETED">已完成</option>
          <option value="CANCELLED">已取消</option>
        </select>
      </div>
      <div class="filter-right">
        <button class="filter-btn" :class="{ active: hasAdvancedFilters }" @click="openAdvancedFilters">
          進階篩選 <span v-if="hasAdvancedFilters" class="filter-badge">{{ advancedFilterCount }}</span>
        </button>
        <button v-if="hasActiveFilters" class="clear-btn" @click="clearFilters">CLEAR ×</button>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="state-box">
      <div class="loading-bars">
        <span v-for="i in 5" :key="i" :style="{ animationDelay: (i * 0.1) + 's' }"></span>
      </div>
      <p class="mono">LOADING...</p>
    </div>

    <!-- Empty -->
    <div v-else-if="filteredActivities.length === 0" class="state-box">
      <div class="empty-text">EMPTY</div>
      <p class="empty-desc">找不到符合條件的活動</p>
      <button v-if="hasActiveFilters" @click="clearFilters" class="cta-btn">清除篩選 ×</button>
    </div>

    <!-- Table -->
    <div v-else class="table-wrap">
      <table class="al-table">
        <thead>
          <tr>
            <th class="col-id">ID</th>
            <th class="col-title">標題</th>
            <th class="col-time">開始時間</th>
            <th class="col-time">結束時間</th>
            <th class="col-loc">地點</th>
            <th class="col-type">類型</th>
            <th class="col-fee">費用</th>
            <th class="col-status">狀態</th>
            <th class="col-actions">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="activity in filteredActivities" :key="activity.id"
              :class="'row-' + activity.status.toLowerCase()">
            <td class="col-id mono">{{ activity.id }}</td>
            <td class="col-title">
              <span class="activity-title">{{ activity.title }}</span>
            </td>
            <td class="col-time mono">{{ formatDateTime(activity.startTime) }}</td>
            <td class="col-time mono">{{ formatDateTime(activity.endTime) }}</td>
            <td class="col-loc">{{ activity.location || '—' }}</td>
            <td class="col-type">
              <span class="type-tag">{{ getActivityTypeLabel(activity.activityType) }}</span>
            </td>
            <td class="col-fee mono">
              <span v-if="activity.feeAmount > 0" class="fee-amt">NT$ {{ activity.feeAmount }}</span>
              <span v-else class="fee-free">免費</span>
            </td>
            <td class="col-status">
              <span class="status-badge" :class="'s-' + activity.status.toLowerCase()">
                {{ getStatusLabel(activity.status) }}
              </span>
            </td>
            <td class="col-actions">
              <div class="action-group">
                <!-- 草稿 -->
                <template v-if="activity.status === 'DRAFT'">
                  <button class="act-btn edit" @click="updateActivity(activity.id)" title="編輯">編輯</button>
                  <button class="act-btn publish" @click="goToPublish(activity.id)" title="發布">發布</button>
                  <button class="act-btn delete" @click="deleteDraft(activity.id, activity.title)" title="刪除">刪除</button>
                </template>
                <!-- 預約發布 -->
                <template v-else-if="activity.status === 'SCHEDULE'">
                  <button class="act-btn edit" @click="updateActivity(activity.id)">編輯</button>
                  <button class="act-btn cancel" @click="showCancelScheduledDialog(activity)">取消預約</button>
                </template>
                <!-- 已發布 -->
                <template v-else-if="activity.status === 'PUBLISHED'">
                  <button class="act-btn edit" @click="updateActivity(activity.id)">編輯</button>
                  <button class="act-btn cancel" @click="showCancelActivityDialog(activity)">取消</button>
                </template>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- ===== 進階篩選 Modal ===== -->
    <Teleport to="body">
      <div v-if="showAdvancedModal" class="modal-overlay" @click.self="closeAdvancedFilters">
        <div class="modal-box">
          <div class="modal-header">
            <h2 class="modal-title">進階篩選</h2>
            <button class="modal-close" @click="closeAdvancedFilters">×</button>
          </div>
          <div class="modal-body">

            <!-- 時間篩選 -->
            <div class="modal-section">
              <p class="section-label">TIME RANGE</p>
              <div class="quick-pills">
                <button v-for="range in quickTimeRanges" :key="range.value"
                        class="pill" :class="{ active: tempFilters.activeQuickRange === range.value }"
                        @click="applyQuickTimeRange(range.value)">
                  {{ range.label }}
                </button>
              </div>
              <div class="date-row">
                <div class="date-field">
                  <label class="field-label">開始日期</label>
                  <input type="date" v-model="tempFilters.dateFilter.startDate" class="date-input" />
                </div>
                <span class="date-sep">—</span>
                <div class="date-field">
                  <label class="field-label">結束日期</label>
                  <input type="date" v-model="tempFilters.dateFilter.endDate"
                         :min="tempFilters.dateFilter.startDate" class="date-input" />
                </div>
              </div>
              <div class="radio-row">
                <label class="radio-label">篩選依據：</label>
                <label class="radio-opt"><input type="radio" value="startTime" v-model="tempFilters.dateFilterMode" /> 開始時間</label>
                <label class="radio-opt"><input type="radio" value="endTime" v-model="tempFilters.dateFilterMode" /> 結束時間</label>
                <label class="radio-opt"><input type="radio" value="createdAt" v-model="tempFilters.dateFilterMode" /> 創建時間</label>
              </div>
            </div>

            <!-- 排序 -->
            <div class="modal-section">
              <p class="section-label">SORT BY</p>
              <div class="sort-grid">
                <label v-for="opt in sortOptions" :key="opt.value" class="sort-card"
                       :class="{ active: tempFilters.sortOrder === opt.value }">
                  <input type="radio" :value="opt.value" v-model="tempFilters.sortOrder" style="display:none" />
                  <span class="sort-title">{{ opt.label }}</span>
                  <span class="sort-desc">{{ opt.desc }}</span>
                </label>
              </div>
            </div>

            <!-- 其他條件 -->
            <div class="modal-section">
              <p class="section-label">OTHER</p>
              <div class="check-group">
                <label class="check-opt"><input type="checkbox" v-model="tempFilters.showFullOnly" /> 只顯示已額滿</label>
                <label class="check-opt"><input type="checkbox" v-model="tempFilters.showFreeOnly" /> 只顯示免費活動</label>
                <label class="check-opt"><input type="checkbox" v-model="tempFilters.showWithFeeOnly" /> 只顯示收費活動</label>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button class="modal-btn reset" @click="resetAdvancedFilters">↺ 重置</button>
            <div class="modal-footer-right">
              <button class="modal-btn cancel" @click="closeAdvancedFilters">取消</button>
              <button class="modal-btn apply" @click="applyAdvancedFilters">套用</button>
            </div>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- ===== 取消活動 Dialog ===== -->
    <Teleport to="body">
      <div v-if="showCancelDialog" class="modal-overlay" @click.self="closeCancelDialog">
        <div class="dialog-box">
          <div class="dialog-header">
            <h2 class="dialog-title">取消活動</h2>
            <button class="modal-close" @click="closeCancelDialog">×</button>
          </div>
          <div class="dialog-body">
            <p class="dialog-warn">確定要取消以下活動嗎？此操作無法復原。</p>
            <div class="activity-info-card">
              <p class="info-name">{{ cancelTargetActivity?.title }}</p>
              <p class="info-meta mono">{{ formatDateTime(cancelTargetActivity?.startTime) }} · {{ cancelTargetActivity?.location }}</p>
            </div>
            <label class="field-label" style="margin-top:1rem;display:block">取消原因（選填）</label>
            <textarea v-model="cancelReason" class="reason-textarea"
                      placeholder="請說明取消原因，系統將通知已報名的會員..." rows="3"></textarea>
            <div class="notice-list">
              <p>· 取消後無法復原</p>
              <p>· 已報名的會員將收到通知</p>
              <p>· 活動狀態將變為「已取消」</p>
            </div>
          </div>
          <div class="dialog-footer">
            <button class="modal-btn cancel" @click="closeCancelDialog" :disabled="isProcessing">返回</button>
            <button class="modal-btn danger" @click="confirmCancelActivity" :disabled="isProcessing">
              {{ isProcessing ? '處理中...' : '確認取消活動' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- ===== 取消預約發布 Dialog ===== -->
    <Teleport to="body">
      <div v-if="showCancelScheduledDialogFlag" class="modal-overlay" @click.self="closeCancelScheduledDialog">
        <div class="dialog-box">
          <div class="dialog-header">
            <h2 class="dialog-title">取消預約發布</h2>
            <button class="modal-close" @click="closeCancelScheduledDialog">×</button>
          </div>
          <div class="dialog-body">
            <p class="dialog-warn">確定要取消預約發布，將活動回到草稿狀態嗎？</p>
            <div class="activity-info-card">
              <p class="info-name">{{ cancelScheduledTarget?.title }}</p>
              <p class="info-meta mono">{{ formatDateTime(cancelScheduledTarget?.startTime) }} · {{ cancelScheduledTarget?.location }}</p>
            </div>
          </div>
          <div class="dialog-footer">
            <button class="modal-btn cancel" @click="closeCancelScheduledDialog">返回</button>
            <button class="modal-btn danger" @click="confirmCancelScheduledPublish" :disabled="isProcessing">
              {{ isProcessing ? '處理中...' : '確認取消預約發布' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- ===== 刪除草稿 Dialog ===== -->
    <Teleport to="body">
      <div v-if="showDeleteDialog" class="modal-overlay" @click.self="cancelDelete">
        <div class="dialog-box dialog-sm">
          <div class="dialog-header">
            <h2 class="dialog-title">刪除草稿</h2>
            <button class="modal-close" @click="cancelDelete">×</button>
          </div>
          <div class="dialog-body">
            <p class="dialog-warn">確定要刪除以下草稿嗎？此操作無法復原。</p>
            <div class="activity-info-card">
              <p class="info-name">「{{ deleteTarget.name }}」</p>
            </div>
          </div>
          <div class="dialog-footer">
            <button class="modal-btn cancel" @click="cancelDelete">取消</button>
            <button class="modal-btn danger" @click="confirmDelete">確定刪除</button>
          </div>
        </div>
      </div>
    </Teleport>

  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { activityApi } from '@/api/activity'

const router = useRouter()
const route = useRoute()

// ===== Navbar scroll hide =====
const navHidden = ref(false)
let lastScrollY = 0
const handleScroll = () => {
  const current = window.scrollY
  navHidden.value = current > lastScrollY && current > 60
  lastScrollY = current
}
onMounted(() => {
  window.addEventListener('scroll', handleScroll, { passive: true })
  loadActivities()
  // 支援從 ActivityManagement 帶入 status query
  if (route.query.status) selectedStatus.value = route.query.status
})
onUnmounted(() => window.removeEventListener('scroll', handleScroll))

// ===== State =====
const activities = ref([])
const loading = ref(false)
const isProcessing = ref(false)

const searchKeyword = ref('')
const selectedStatus = ref('')
const selectedActivityType = ref('')
const selectedDanceStyle = ref('')

const showAdvancedModal = ref(false)
const advancedFilters = ref({
  dateFilter: { startDate: '', endDate: '' },
  dateFilterMode: 'startTime',
  sortOrder: 'startTime-desc',
  activeQuickRange: '',
  showFullOnly: false, showFreeOnly: false, showWithFeeOnly: false
})
const tempFilters = ref(JSON.parse(JSON.stringify(advancedFilters.value)))

const showDeleteDialog = ref(false)
const deleteTarget = ref({ id: null, name: '' })
const showCancelDialog = ref(false)
const cancelTargetActivity = ref(null)
const cancelReason = ref('')
const showCancelScheduledDialogFlag = ref(false)
const cancelScheduledTarget = ref(null)

// ===== Data =====
const quickTimeRanges = [
  { value: 'today', label: '今天' }, { value: 'tomorrow', label: '明天' },
  { value: 'thisWeek', label: '本週' }, { value: 'nextWeek', label: '下週' },
  { value: 'thisMonth', label: '本月' }, { value: 'nextMonth', label: '下個月' },
  { value: 'past7days', label: '過去 7 天' }, { value: 'past30days', label: '過去 30 天' }
]
const sortOptions = [
  { value: 'startTime-desc', label: '開始時間 新→舊', desc: '最近的活動優先' },
  { value: 'startTime-asc',  label: '開始時間 舊→新', desc: '最早的活動優先' },
  { value: 'created-desc',   label: '創建時間 新→舊', desc: '最新創建的優先' },
  { value: 'created-asc',    label: '創建時間 舊→新', desc: '最早創建的優先' },
  { value: 'title-asc',      label: '標題 A→Z',       desc: '按字母順序' }
]
const danceStyles = ['Hip Hop','Jazz','Popping','Locking','Breaking','House','Waacking','Voguing','Urban','K-pop','Freestyle']

// ===== Load =====
const loadActivities = async () => {
  loading.value = true
  try {
    const response = await activityApi.getAllActivities()
    activities.value = response.data
  } catch (error) {
    console.error('載入活動資料失敗:', error)
    alert('載入活動資料失敗，請稍後再試。')
  } finally {
    loading.value = false
  }
}

// ===== Computed =====
const hasActiveFilters = computed(() =>
  searchKeyword.value || selectedStatus.value || selectedActivityType.value ||
  selectedDanceStyle.value || hasAdvancedFilters.value
)
const hasAdvancedFilters = computed(() =>
  advancedFilters.value.dateFilter.startDate || advancedFilters.value.dateFilter.endDate ||
  advancedFilters.value.sortOrder !== 'startTime-desc' ||
  advancedFilters.value.showFullOnly || advancedFilters.value.showFreeOnly || advancedFilters.value.showWithFeeOnly
)
const advancedFilterCount = computed(() => {
  let c = 0
  if (advancedFilters.value.dateFilter.startDate || advancedFilters.value.dateFilter.endDate) c++
  if (advancedFilters.value.sortOrder !== 'startTime-desc') c++
  if (advancedFilters.value.showFullOnly) c++
  if (advancedFilters.value.showFreeOnly) c++
  if (advancedFilters.value.showWithFeeOnly) c++
  return c
})

const normalizeText = (text) => !text ? '' : text.toLowerCase().replace(/[\s-_]+/g, '').trim()

const filteredActivities = computed(() => {
  let result = [...activities.value]
  if (selectedStatus.value) result = result.filter(a => a.status === selectedStatus.value)
  if (searchKeyword.value) {
    const kw = normalizeText(searchKeyword.value)
    result = result.filter(a => normalizeText(a.title).includes(kw) || (a.description && normalizeText(a.description).includes(kw)))
  }
  if (selectedActivityType.value) result = result.filter(a => a.activityType === selectedActivityType.value)
  if (selectedDanceStyle.value) {
    const style = normalizeText(selectedDanceStyle.value)
    result = result.filter(a => normalizeText(a.title).includes(style) || (a.description && normalizeText(a.description).includes(style)))
  }
  if (advancedFilters.value.dateFilter.startDate || advancedFilters.value.dateFilter.endDate) {
    result = result.filter(a => {
      const dateField = { startTime: a.startTime, endTime: a.endTime, createdAt: a.createdAt }[advancedFilters.value.dateFilterMode] || a.startTime
      if (!dateField) return false
      const d = new Date(dateField); d.setHours(0,0,0,0)
      if (advancedFilters.value.dateFilter.startDate) {
        const s = new Date(advancedFilters.value.dateFilter.startDate); s.setHours(0,0,0,0)
        if (d < s) return false
      }
      if (advancedFilters.value.dateFilter.endDate) {
        const e = new Date(advancedFilters.value.dateFilter.endDate); e.setHours(23,59,59,999)
        if (d > e) return false
      }
      return true
    })
  }
  if (advancedFilters.value.showFullOnly) result = result.filter(a => a.currentParticipants >= a.maxParticipants)
  if (advancedFilters.value.showFreeOnly) result = result.filter(a => a.feeAmount === 0)
  if (advancedFilters.value.showWithFeeOnly) result = result.filter(a => a.feeAmount > 0)
  result.sort((a, b) => {
    switch (advancedFilters.value.sortOrder) {
      case 'startTime-desc': return new Date(b.startTime) - new Date(a.startTime)
      case 'startTime-asc':  return new Date(a.startTime) - new Date(b.startTime)
      case 'created-desc':   return new Date(b.createdAt || 0) - new Date(a.createdAt || 0)
      case 'created-asc':    return new Date(a.createdAt || 0) - new Date(b.createdAt || 0)
      case 'title-asc':      return (a.title || '').localeCompare(b.title || '')
      default: return 0
    }
  })
  return result
})

// ===== Helpers =====
const getActivityTypeLabel = (type) =>
  ({ REGULAR:'社課', SPECIAL:'特殊活動', TRAINING:'團練', PERFORMANCE:'演出', COMPETITION:'比賽' })[type] || type
const getStatusLabel = (status) =>
  ({ DRAFT:'草稿', PUBLISHED:'已發布', COMPLETED:'已完成', CANCELLED:'已取消', SCHEDULE:'已預約' })[status] || status
const formatDateTime = (dt) => {
  if (!dt) return '—'
  return new Date(dt).toLocaleString('zh-TW', { year:'numeric', month:'2-digit', day:'2-digit', hour:'2-digit', minute:'2-digit' })
}

// ===== Filters =====
const handleSearch = () => {}
const handleFilterChange = () => {}
const clearFilters = () => {
  searchKeyword.value = ''; selectedStatus.value = ''; selectedActivityType.value = ''; selectedDanceStyle.value = ''
  advancedFilters.value = { dateFilter:{startDate:'',endDate:''}, dateFilterMode:'startTime', sortOrder:'startTime-desc', activeQuickRange:'', showFullOnly:false, showFreeOnly:false, showWithFeeOnly:false }
}
const openAdvancedFilters = () => { tempFilters.value = JSON.parse(JSON.stringify(advancedFilters.value)); showAdvancedModal.value = true }
const closeAdvancedFilters = () => { showAdvancedModal.value = false }
const applyAdvancedFilters = () => { advancedFilters.value = JSON.parse(JSON.stringify(tempFilters.value)); showAdvancedModal.value = false }
const resetAdvancedFilters = () => {
  tempFilters.value = { dateFilter:{startDate:'',endDate:''}, dateFilterMode:'startTime', sortOrder:'startTime-desc', activeQuickRange:'', showFullOnly:false, showFreeOnly:false, showWithFeeOnly:false }
}

const applyQuickTimeRange = (rangeValue) => {
  const today = new Date(); today.setHours(0,0,0,0)
  tempFilters.value.activeQuickRange = rangeValue
  const fmt = (d) => d.toISOString().split('T')[0]
  switch (rangeValue) {
    case 'today': tempFilters.value.dateFilter = { startDate: fmt(today), endDate: fmt(today) }; break
    case 'tomorrow': { const t = new Date(today); t.setDate(t.getDate()+1); tempFilters.value.dateFilter = { startDate: fmt(t), endDate: fmt(t) }; break }
    case 'thisWeek': { const s = new Date(today); s.setDate(today.getDate()-today.getDay()); const e = new Date(s); e.setDate(s.getDate()+6); tempFilters.value.dateFilter = { startDate: fmt(s), endDate: fmt(e) }; break }
    case 'nextWeek': { const s = new Date(today); s.setDate(today.getDate()+(7-today.getDay())); const e = new Date(s); e.setDate(s.getDate()+6); tempFilters.value.dateFilter = { startDate: fmt(s), endDate: fmt(e) }; break }
    case 'thisMonth': { const s = new Date(today.getFullYear(),today.getMonth(),1); const e = new Date(today.getFullYear(),today.getMonth()+1,0); tempFilters.value.dateFilter = { startDate: fmt(s), endDate: fmt(e) }; break }
    case 'nextMonth': { const s = new Date(today.getFullYear(),today.getMonth()+1,1); const e = new Date(today.getFullYear(),today.getMonth()+2,0); tempFilters.value.dateFilter = { startDate: fmt(s), endDate: fmt(e) }; break }
    case 'past7days': { const s = new Date(today); s.setDate(today.getDate()-7); tempFilters.value.dateFilter = { startDate: fmt(s), endDate: fmt(today) }; break }
    case 'past30days': { const s = new Date(today); s.setDate(today.getDate()-30); tempFilters.value.dateFilter = { startDate: fmt(s), endDate: fmt(today) }; break }
  }
}

// ===== Actions =====
const updateActivity = (id) => router.push(`/admin/update-activity-container/${id}`)
const goToPublish = (id) => router.push({ name: 'publish-activity-container', params: { activityId: id } })

const deleteDraft = (id, name) => { deleteTarget.value = { id, name }; showDeleteDialog.value = true }
const cancelDelete = () => { showDeleteDialog.value = false; deleteTarget.value = { id: null, name: '' } }
const confirmDelete = async () => {
  try {
    await activityApi.deleteDraftActivity(deleteTarget.value.id)
    activities.value = activities.value.filter(a => a.id !== deleteTarget.value.id)
    showDeleteDialog.value = false; deleteTarget.value = { id: null, name: '' }
  } catch (e) {
    const s = e.response?.status
    alert(s === 403 ? '沒有權限刪除' : s === 404 ? '草稿不存在' : s === 400 ? '只能刪除草稿狀態的活動' : '刪除失敗，請稍後再試')
  }
}

const showCancelActivityDialog = (activity) => { cancelTargetActivity.value = activity; cancelReason.value = ''; showCancelDialog.value = true }
const closeCancelDialog = () => { showCancelDialog.value = false; cancelTargetActivity.value = null }
const confirmCancelActivity = async () => {
  if (!cancelTargetActivity.value) return
  isProcessing.value = true
  try {
    await activityApi.cancelActivity(cancelTargetActivity.value.id, { reason: cancelReason.value || '管理員取消活動' })
    const a = activities.value.find(a => a.id === cancelTargetActivity.value.id)
    if (a) { a.status = 'CANCELLED'; a.cancelReason = cancelReason.value }
    closeCancelDialog()
  } catch (e) {
    alert('取消活動失敗，請稍後再試')
  } finally {
    isProcessing.value = false
  }
}

const showCancelScheduledDialog = (activity) => { cancelScheduledTarget.value = activity; showCancelScheduledDialogFlag.value = true }
const closeCancelScheduledDialog = () => { showCancelScheduledDialogFlag.value = false; cancelScheduledTarget.value = null }
const confirmCancelScheduledPublish = async () => {
  if (!cancelScheduledTarget.value) return
  isProcessing.value = true
  try {
    await activityApi.cancelScheduledPublish(cancelScheduledTarget.value.id)
    const a = activities.value.find(a => a.id === cancelScheduledTarget.value.id)
    if (a) a.status = 'DRAFT'
    closeCancelScheduledDialog()
  } catch (e) {
    alert('取消預約發布失敗，請稍後再試')
  } finally {
    isProcessing.value = false
  }
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Bebas+Neue&family=Noto+Sans+TC:wght@300;400;500;700&family=Space+Mono:wght@400;700&display=swap');

/* ===== Base ===== */
.al-wrap {
  min-height: 100vh;
  background: #fff;
  font-family: 'Noto Sans TC', sans-serif;
  color: #0a0a0a;
  max-width: 1400px;
  margin: 0 auto;
  padding: 5.5rem 2rem 4rem;
}
.mono { font-family: 'Space Mono', monospace; }

/* ===== Navbar ===== */
.navbar {
  position: fixed; top: 0; left: 0; right: 0; z-index: 100;
  padding: 1rem 0;
  background: rgba(255,255,255,0.88);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(0,0,0,0.08);
  transform: translateY(0); transition: transform 0.35s cubic-bezier(0.4,0,0.2,1);
}
.navbar-hidden { transform: translateY(-100%); }
.nav-container { max-width: 1400px; margin: 0 auto; padding: 0 2rem; display: flex; justify-content: space-between; align-items: center; }
.nav-logo {  font-size: 1.25rem; font-weight: 600; color: #1a1a1a; margin: 0; }
.nav-logo:hover { color: #ff2d6b; }
.nav-right { display: flex; gap: 1.5rem; align-items: center; }
.nav-link { font-family: 'Space Mono', monospace; font-size: 0.75rem; letter-spacing: 0.08em; color: #666; text-decoration: none; transition: color 0.2s; }
.nav-link:hover { color: #ff2d6b; }

/* ===== Header ===== */
.al-header {
  display: flex; justify-content: space-between; align-items: flex-end;
  margin-bottom: 2rem; padding-bottom: 1.5rem; border-bottom: 2px solid #0a0a0a;
  flex-wrap: wrap; gap: 1rem;
}
.al-eyebrow { display: block; font-family: 'Space Mono', monospace; font-size: 0.6rem; letter-spacing: 0.2em; color: #ff2d6b; font-weight: 700; margin-bottom: 0.4rem; }
.al-title { font-family: 'Bebas Neue', sans-serif; font-size: 3.5rem; line-height: 0.95; margin: 0 0 0.5rem; letter-spacing: 0.02em; }
.al-count { font-family: 'Space Mono', monospace; font-size: 0.7rem; color: #aaa; margin: 0; letter-spacing: 0.08em; }
.al-count .mono { color: #0a0a0a; font-size: 0.8rem; }

.btn-create {
  background: #0a0a0a; color: #fff; border: none;
  padding: 0.65rem 1.4rem; font-family: 'Space Mono', monospace;
  font-size: 0.9rem; letter-spacing: 0.08em; cursor: pointer; transition: background 0.2s;
}
.btn-create:hover { background: #ff2d6b; }

/* ===== Filter Bar ===== */
.filter-bar {
  display: flex; align-items: center; gap: 0.75rem;
  margin-bottom: 2rem; flex-wrap: wrap;
}
.search-wrap { position: relative; width: 220px; flex-shrink: 0; }
.search-input {
  width: 100%; padding: 0.6rem 2rem 0.6rem 0.9rem;
  border: 1px solid #0a0a0a; border-radius: 0;
  font-family: 'Space Mono', monospace; font-size: 0.9rem; letter-spacing: 0.04em;
  background: #fff; color: #0a0a0a; outline: none; transition: border-color 0.2s; box-sizing: border-box;
}
.search-input:focus { border-color: #ff2d6b; }
.search-input::placeholder { color: #bbb; }
.search-icon { position: absolute; right: 0.7rem; top: 50%; transform: translateY(-50%); color: #bbb; font-size: 0.8rem; pointer-events: none; }

.filter-selects { display: flex; gap: 0.6rem; flex-wrap: wrap; }
.filter-select {
  padding: 0.55rem 0.9rem; border: 1px solid #e0e0e0; background: #fff; color: #555;
  font-family: 'Space Mono', monospace; font-size: 0.9rem; letter-spacing: 0.04em;
  border-radius: 0; outline: none; cursor: pointer; transition: border-color 0.2s;
}
.filter-select:focus { border-color: #0a0a0a; }

.filter-right { display: flex; gap: 0.6rem; align-items: center; margin-left: auto; }
.filter-btn {
  background: #fff; border: 1px solid #e0e0e0; color: #555;
  padding: 0.55rem 1rem; font-family: 'Space Mono', monospace;
  font-size: 0.9rem; letter-spacing: 0.06em; cursor: pointer; border-radius: 0; transition: all 0.2s; display: flex; align-items: center; gap: 0.4rem;
}
.filter-btn:hover, .filter-btn.active { border-color: #0a0a0a; color: #0a0a0a; }
.filter-badge { background: #ff2d6b; color: #fff; border-radius: 50%; width: 16px; height: 16px; font-size: 0.6rem; display: flex; align-items: center; justify-content: center; }
.clear-btn {
  background: transparent; border: 1px solid #ff2d6b; color: #ff2d6b;
  padding: 0.5rem 0.9rem; font-family: 'Space Mono', monospace;
  font-size: 0.9rem; letter-spacing: 0.08em; cursor: pointer; transition: all 0.2s;
}
.clear-btn:hover { background: #ff2d6b; color: #fff; }

/* ===== State ===== */
.state-box { display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 40vh; gap: 0.75rem; }
.loading-bars { display: flex; gap: 4px; }
.loading-bars span { display: block; width: 3px; height: 28px; background: #ff2d6b; border-radius: 2px; animation: barPulse 0.8s ease-in-out infinite alternate; }
@keyframes barPulse { from { transform: scaleY(0.3); opacity: 0.3; } to { transform: scaleY(1); opacity: 1; } }
.empty-text { font-family: 'Bebas Neue', sans-serif; font-size: 7rem; color: transparent; -webkit-text-stroke: 1px #e0e0e0; line-height: 1; }
.empty-desc { font-size: 0.85rem; color: #aaa; margin: 0; }
.cta-btn { background: #ff2d6b; color: #fff; border: none; padding: 0.65rem 1.5rem; font-family: 'Space Mono', monospace; font-size: 0.72rem; cursor: pointer; }

/* ===== Table ===== */
.table-wrap { overflow-x: auto; border: 1px solid #e0e0e0; }
.al-table { width: 100%; border-collapse: collapse; background: #fff; }
.al-table thead tr { background: #0a0a0a; }
.al-table th {
  padding: 0.75rem 0.9rem; text-align: left;
  font-family: 'Space Mono', monospace; font-size: 0.8rem;
  letter-spacing: 0.12em; color: #fff; font-weight: 400; white-space: nowrap;
}
.al-table tbody tr {
  border-bottom: 1px solid #f0f0f0; transition: background 0.15s;
}
.al-table tbody tr:hover { background: #fafafa; }
.al-table td { padding: 0.85rem 0.9rem; font-size: 0.8rem; color: #333; vertical-align: middle; }

/* row status tint */
.row-cancelled td { color: #bbb; }
.row-completed td { color: #555; }

.col-id { width: 50px; font-size: 0.8rem !important; color: #ccc !important; }
.col-title { min-width: 180px; }
.activity-title { font-weight: 600; color: #0a0a0a; }
.col-time { white-space: nowrap; font-size: 0.8rem !important; color: #666 !important; }
.col-loc { font-size: 0.78rem !important; color: #888 !important; }
.col-type { white-space: nowrap; }
.type-tag { font-family: 'Space Mono', monospace; font-size: 0.6rem; letter-spacing: 0.08em; color: #888; border: 1px solid #e0e0e0; padding: 0.15rem 0.45rem; }
.col-fee { white-space: nowrap; }
.fee-amt { font-family: 'Space Mono', monospace; font-size: 0.72rem; color: #ff2d6b; font-weight: 700; }
.fee-free { font-family: 'Space Mono', monospace; font-size: 0.68rem; color: #bbb; }
.col-status { white-space: nowrap; }
.status-badge { font-family: 'Space Mono', monospace; font-size: 0.6rem; letter-spacing: 0.08em; padding: 0.2rem 0.55rem; }
.s-draft     { background: #f5f5f5; color: #888; }
.s-published { background: #e8f5e9; color: #2e7d32; }
.s-completed { background: #e3f2fd; color: #1565c0; }
.s-cancelled { background: #ffebee; color: #c62828; }
.s-schedule  { background: #fff3e0; color: #e65100; }

.col-actions { width: 200px; }
.action-group { display: flex; gap: 0.4rem; flex-wrap: wrap; }
.act-btn {
  padding: 0.3rem 0.65rem; font-family: 'Space Mono', monospace;
  font-size: 0.7rem; letter-spacing: 0.06em; border: 1px solid; cursor: pointer; transition: all 0.15s; background: transparent;
}
.act-btn.edit   { border-color: #0a0a0a; color: #0a0a0a; }
.act-btn.edit:hover { background: #0a0a0a; color: #fff; }
.act-btn.publish { border-color: #2e7d32; color: #2e7d32; }
.act-btn.publish:hover { background: #2e7d32; color: #fff; }
.act-btn.cancel { border-color: #e65100; color: #e65100; }
.act-btn.cancel:hover { background: #e65100; color: #fff; }
.act-btn.delete { border-color: #c62828; color: #c62828; }
.act-btn.delete:hover { background: #c62828; color: #fff; }

/* ===== Modal shared ===== */
.modal-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,0.5); backdrop-filter: blur(4px);
  display: flex; align-items: center; justify-content: center; z-index: 200;
}
.modal-box {
  background: #fff; width: 560px; max-width: 95vw;
  max-height: 90vh; display: flex; flex-direction: column; overflow: hidden;
}
.modal-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 1.25rem 1.5rem; border-bottom: 1px solid #e0e0e0;
}
.modal-title { font-family: 'Bebas Neue', sans-serif; font-size: 1.5rem; letter-spacing: 0.05em; margin: 0; }
.modal-close { background: none; border: none; font-size: 1.3rem; cursor: pointer; color: #aaa; line-height: 1; }
.modal-close:hover { color: #0a0a0a; }
.modal-body { padding: 1.5rem; overflow-y: auto; flex: 1; }
.modal-footer { display: flex; justify-content: space-between; align-items: center; padding: 1rem 1.5rem; border-top: 1px solid #e0e0e0; }
.modal-footer-right { display: flex; gap: 0.6rem; }

.modal-btn { padding: 0.55rem 1.2rem; font-family: 'Space Mono', monospace; font-size: 0.72rem; letter-spacing: 0.06em; cursor: pointer; border: 1px solid; transition: all 0.15s; }
.modal-btn.reset  { border-color: #e0e0e0; color: #555; background: #fff; }
.modal-btn.reset:hover { background: #f5f5f5; }
.modal-btn.cancel { border-color: #e0e0e0; color: #555; background: #fff; }
.modal-btn.cancel:hover { background: #f5f5f5; }
.modal-btn.apply  { border-color: #0a0a0a; color: #fff; background: #0a0a0a; }
.modal-btn.apply:hover { background: #ff2d6b; border-color: #ff2d6b; }
.modal-btn.danger { border-color: #c62828; color: #fff; background: #c62828; }
.modal-btn.danger:hover { background: #b71c1c; border-color: #b71c1c; }
.modal-btn:disabled { opacity: 0.5; cursor: not-allowed; }

/* ===== Advanced filter modal content ===== */
.modal-section { margin-bottom: 1.75rem; }
.section-label { font-family: 'Space Mono', monospace; font-size: 0.6rem; letter-spacing: 0.2em; color: #ff2d6b; font-weight: 700; margin-bottom: 0.75rem; }
.quick-pills { display: flex; flex-wrap: wrap; gap: 0.4rem; margin-bottom: 1rem; }
.pill { padding: 0.3rem 0.75rem; border: 1px solid #e0e0e0; background: #fff; font-family: 'Space Mono', monospace; font-size: 0.65rem; cursor: pointer; transition: all 0.15s; }
.pill:hover { border-color: #0a0a0a; }
.pill.active { background: #0a0a0a; border-color: #0a0a0a; color: #fff; }
.date-row { display: flex; align-items: flex-end; gap: 0.75rem; margin-bottom: 1rem; }
.date-field { flex: 1; }
.field-label { display: block; font-family: 'Space Mono', monospace; font-size: 0.6rem; letter-spacing: 0.1em; color: #aaa; margin-bottom: 0.35rem; }
.date-input { width: 100%; padding: 0.55rem 0.75rem; border: 1px solid #e0e0e0; border-radius: 0; font-size: 0.82rem; outline: none; box-sizing: border-box; }
.date-input:focus { border-color: #0a0a0a; }
.date-sep { color: #ccc; padding-bottom: 0.3rem; }
.radio-row { display: flex; align-items: center; gap: 1rem; flex-wrap: wrap; }
.radio-label { font-family: 'Space Mono', monospace; font-size: 0.65rem; color: #888; }
.radio-opt { display: flex; align-items: center; gap: 0.35rem; font-size: 0.8rem; cursor: pointer; }
.sort-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 0.5rem; }
.sort-card { display: flex; flex-direction: column; padding: 0.75rem; border: 1px solid #e0e0e0; cursor: pointer; transition: all 0.15s; }
.sort-card:hover { border-color: #0a0a0a; }
.sort-card.active { border-color: #0a0a0a; background: #0a0a0a; }
.sort-title { font-family: 'Space Mono', monospace; font-size: 0.68rem; font-weight: 700; }
.sort-desc { font-size: 0.7rem; color: #aaa; margin-top: 0.2rem; }
.sort-card.active .sort-title, .sort-card.active .sort-desc { color: #fff; }
.check-group { display: flex; flex-direction: column; gap: 0.6rem; }
.check-opt { display: flex; align-items: center; gap: 0.5rem; font-size: 0.82rem; cursor: pointer; }

/* ===== Dialog ===== */
.dialog-box { background: #fff; width: 480px; max-width: 95vw; }
.dialog-sm { width: 380px; }
.dialog-header { display: flex; justify-content: space-between; align-items: center; padding: 1.25rem 1.5rem; border-bottom: 2px solid #0a0a0a; }
.dialog-title { font-family: 'Bebas Neue', sans-serif; font-size: 1.5rem; letter-spacing: 0.05em; margin: 0; }
.dialog-body { padding: 1.5rem; }
.dialog-footer { display: flex; justify-content: flex-end; gap: 0.6rem; padding: 1rem 1.5rem; border-top: 1px solid #e0e0e0; }
.dialog-warn { font-size: 0.85rem; color: #555; margin: 0 0 1rem; }
.activity-info-card { background: #f5f5f5; border-left: 3px solid #0a0a0a; padding: 0.85rem 1rem; }
.info-name { font-weight: 700; font-size: 0.95rem; margin: 0 0 0.3rem; }
.info-meta { font-size: 0.72rem; color: #888; margin: 0; }
.reason-textarea { width: 100%; padding: 0.65rem 0.8rem; border: 1px solid #e0e0e0; font-family: 'Noto Sans TC', sans-serif; font-size: 0.85rem; resize: vertical; outline: none; box-sizing: border-box; margin-top: 0.35rem; }
.reason-textarea:focus { border-color: #0a0a0a; }
.notice-list { margin-top: 1rem; padding: 0.75rem 1rem; background: #fff3e0; font-size: 0.78rem; color: #e65100; }
.notice-list p { margin: 0.2rem 0; }

/* ===== Responsive ===== */
@media (max-width: 768px) {
  .al-wrap { padding: 5rem 1rem 3rem; }
  .al-title { font-size: 2.5rem; }
  .filter-bar { flex-direction: column; align-items: flex-start; }
  .filter-right { margin-left: 0; }
  .sort-grid { grid-template-columns: 1fr; }
}
</style>