<template>
  <div class="pa-wrap">


    <!-- ── Loading ── -->
    <div v-if="loading" class="state-wrap">
      <div class="loading-bars">
        <span v-for="i in 5" :key="i" :style="{ animationDelay: (i * 0.1) + 's' }"></span>
      </div>
      <p class="mono">LOADING...</p>
    </div>

    <!-- ══════════════════════════════════════
         BATCH MODE（無 activityId）
    ══════════════════════════════════════ -->
    <div v-else-if="!activityId" class="pa-content">

      <div class="pa-header">
        <div class="header-left">
          <div class="eyebrow"><span class="eyebrow-line"></span><span class="eyebrow-text">BATCH PUBLISH</span></div>
          <h1 class="pa-title">批量<span class="title-accent">發布</span></h1>
          <p class="pa-subtitle">選取草稿活動，一次完成發布設定</p>
        </div>
        <div class="header-right" v-if="draftActivities.length > 0">
          <div class="selection-badge">
            <span class="sel-num">{{ selectedIds.size }}</span>
            <span class="sel-label">已選取</span>
          </div>
          <button class="select-all-btn" @click="toggleSelectAll">
            {{ selectedIds.size === draftActivities.length ? '取消全選' : '全選' }}
          </button>
        </div>
      </div>

      <!-- 無草稿 -->
      <div v-if="draftActivities.length === 0" class="batch-empty">
        <div class="empty-big">EMPTY</div>
        <p class="empty-desc">目前沒有草稿活動可以發布</p>
        <button class="cta-btn" @click="router.push({ name: 'create-activity-container' })">＋ 建立新活動</button>
      </div>

      <!-- 草稿列表 + 選項 -->
      <div v-else class="batch-layout">

        <!-- 左：草稿選擇 -->
        <div class="batch-list">
          <p class="section-label">DRAFT ACTIVITIES — {{ draftActivities.length }} 個草稿</p>
          <div
            v-for="(act, index) in draftActivities"
            :key="act.id"
            class="draft-card"
            :class="{ selected: selectedIds.has(act.id) }"
            :style="{ animationDelay: (index * 0.05) + 's' }"
            @click="toggleSelect(act.id)"
          >
            <div class="draft-check">
              <div class="check-box" :class="{ checked: selectedIds.has(act.id) }">
                <span v-if="selectedIds.has(act.id)">✓</span>
              </div>
            </div>
            <div class="draft-info">
              <span class="draft-title">{{ act.title }}</span>
              <span class="draft-meta mono">
                {{ getActivityTypeLabel(act.activityType) }}
                <span v-if="act.startTime"> · {{ formatDateTime(act.startTime) }}</span>
                <span v-if="act.location"> · {{ act.location }}</span>
              </span>
            </div>
            <div class="draft-fee mono" :class="{ free: !(act.feeAmount > 0) }">
              {{ act.feeAmount > 0 ? `NT$ ${act.feeAmount}` : '免費' }}
            </div>
          </div>
        </div>

        <!-- 右：發布選項（有選取才顯示） -->
        <div class="batch-options" v-if="selectedIds.size > 0">
          <p class="section-label">PUBLISH METHOD</p>

          <div class="publish-options">
            <div class="option-card" :class="{ active: publishType === 'immediate' }" @click="publishType = 'immediate'">
              <div class="option-top">
                <div class="option-radio"><div class="radio-inner" :class="{ checked: publishType === 'immediate' }"></div></div>
                <div class="option-text">
                  <span class="option-title">立即發布</span>
                  <span class="option-desc">所選活動立即對外開放</span>
                </div>
                <span class="option-icon">⚡</span>
              </div>
            </div>
            <div class="option-card" :class="{ active: publishType === 'scheduled' }" @click="publishType = 'scheduled'">
              <div class="option-top">
                <div class="option-radio"><div class="radio-inner" :class="{ checked: publishType === 'scheduled' }"></div></div>
                <div class="option-text">
                  <span class="option-title">預約發布</span>
                  <span class="option-desc">指定時間自動發布</span>
                </div>
                <span class="option-icon">⏰</span>
              </div>
            </div>
          </div>

          <!-- 預約時間 -->
          <div v-if="publishType === 'scheduled'" class="schedule-inputs">
            <div class="field">
              <label class="field-label">日期</label>
              <input type="date" v-model="scheduledDate" :min="minDate" class="field-input" />
            </div>
            <div class="field">
              <label class="field-label">時間</label>
              <input type="time" v-model="scheduledTime" class="field-input" />
            </div>
            <div v-if="scheduledDate && scheduledTime" class="schedule-preview" :class="{ valid: isScheduleValid, invalid: !isScheduleValid }">
              <span>{{ isScheduleValid ? '✓' : '✕' }}</span>
              <span class="mono">{{ formatSchedulePreview }}</span>
              <span v-if="!isScheduleValid" class="warn-text">必須是未來時間</span>
            </div>
          </div>

          <!-- 批量結果 -->
          <div v-if="batchResult" class="batch-result">
            <div class="result-row success" v-if="batchResult.success > 0">✓ {{ batchResult.success }} 個活動發布成功</div>
            <div class="result-row fail" v-if="batchResult.failed > 0">
              ✕ {{ batchResult.failed }} 個失敗
              <div v-for="e in batchResult.errors" :key="e.id" class="error-item mono">· {{ e.title }}：{{ e.message }}</div>
            </div>
          </div>

          <!-- 確認按鈕 -->
          <div class="batch-actions">
            <button class="btn-outline" @click="clearSelection">清除選取</button>
            <button
              class="btn-publish"
              :disabled="isPublishing || (publishType === 'scheduled' && !isScheduleValid)"
              @click="confirmBatchPublish"
            >
              {{ isPublishing
                ? `發布中 ${publishedCount}/${selectedIds.size}...`
                : `發布 ${selectedIds.size} 個活動 →` }}
            </button>
          </div>
        </div>

        <!-- 尚未選取提示 -->
        <div class="batch-hint" v-else>
          <div class="hint-arrow">←</div>
          <p class="mono">點選左側草稿活動以開始設定發布</p>
        </div>

      </div>
    </div>

    <!-- ══════════════════════════════════════
         SINGLE MODE ERROR（有 activityId 但載入失敗）
    ══════════════════════════════════════ -->
    <div v-else-if="activityId && !activity && !loading" class="state-wrap">
      <div class="empty-text">ERROR</div>
      <p class="empty-desc">{{ errorMsg || '無法載入活動資訊' }}</p>
      <button class="cta-btn" @click="router.push({ name: 'activity-list-container' })">← 返回活動列表</button>
    </div>

    <!-- ══════════════════════════════════════
         SINGLE MODE（有 activityId，載入成功）
    ══════════════════════════════════════ -->
    <div v-else-if="activity" class="pa-content">

      <div class="pa-header">
        <div class="header-left">
          <div class="eyebrow"><span class="eyebrow-line"></span><span class="eyebrow-text">PUBLISH SETTINGS</span></div>
          <h1 class="pa-title">發布<span class="title-accent">設定</span></h1>
        </div>
        <span class="status-badge" :class="'s-' + activity.status?.toLowerCase()">
          {{ getStatusLabel(activity.status) }}
        </span>
      </div>

      <div class="pa-layout">

        <!-- 左：選項 -->
        <div class="pa-main">
          <section class="pa-section">
            <p class="section-label">SELECT PUBLISH METHOD</p>
            <div class="publish-options">
              <div class="option-card" :class="{ active: publishType === 'immediate' }" @click="publishType = 'immediate'">
                <div class="option-top">
                  <div class="option-radio"><div class="radio-inner" :class="{ checked: publishType === 'immediate' }"></div></div>
                  <div class="option-text">
                    <span class="option-title">立即發布</span>
                    <span class="option-desc">活動將立即對外開放，並開始接受報名</span>
                  </div>
                  <span class="option-icon">⚡</span>
                </div>
              </div>
              <div class="option-card" :class="{ active: publishType === 'scheduled' }" @click="publishType = 'scheduled'">
                <div class="option-top">
                  <div class="option-radio"><div class="radio-inner" :class="{ checked: publishType === 'scheduled' }"></div></div>
                  <div class="option-text">
                    <span class="option-title">預約發布</span>
                    <span class="option-desc">選擇未來時間，屆時自動發布活動</span>
                  </div>
                  <span class="option-icon">⏰</span>
                </div>
                <div v-if="publishType === 'scheduled'" class="schedule-body">
                  <div class="schedule-inputs">
                    <div class="field">
                      <label class="field-label">日期</label>
                      <input type="date" v-model="scheduledDate" :min="minDate" class="field-input" />
                    </div>
                    <div class="field">
                      <label class="field-label">時間</label>
                      <input type="time" v-model="scheduledTime" class="field-input" />
                    </div>
                  </div>
                  <div v-if="scheduledDate && scheduledTime" class="schedule-preview" :class="{ valid: isScheduleValid, invalid: !isScheduleValid }">
                    <span class="preview-icon">{{ isScheduleValid ? '✓' : '✕' }}</span>
                    <div>
                      <p class="preview-time">{{ formatSchedulePreview }}</p>
                      <p v-if="!isScheduleValid" class="preview-warn">發布時間必須是未來的時間</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </section>

          <div class="pa-actions">
            <button class="btn-outline" @click="saveDraft" :disabled="isPublishing">儲存為草稿</button>
            <div class="actions-right">
              <button class="btn-edit" @click="goToEdit">← 返回編輯</button>
              <button v-if="publishType === 'immediate'" class="btn-publish" @click="confirmPublish" :disabled="isPublishing">
                {{ isPublishing ? '發布中...' : '確認立即發布 →' }}
              </button>
              <button v-else class="btn-publish" @click="confirmSchedulePublish" :disabled="isPublishing || !isScheduleValid">
                {{ isPublishing ? '設定中...' : '確認預約發布 →' }}
              </button>
            </div>
          </div>
        </div>

        <!-- 右：預覽 -->
        <aside class="pa-sidebar">
          <p class="section-label">ACTIVITY PREVIEW</p>
          <div class="preview-cover" :style="coverStyle">
            <span v-if="!activity.coverImageUrl" class="cover-placeholder">NO IMAGE</span>
            <div class="cover-overlay"><span class="cover-type">{{ getActivityTypeLabel(activity.activityType) }}</span></div>
          </div>
          <div class="preview-info">
            <p class="info-title">{{ activity.title }}</p>
            <div class="info-grid">
              <div class="info-row"><span class="info-label">地點</span><span class="info-val">{{ activity.location || '—' }}</span></div>
              <div class="info-row"><span class="info-label">開始</span><span class="info-val mono">{{ formatDateTime(activity.startTime) }}</span></div>
              <div class="info-row"><span class="info-label">結束</span><span class="info-val mono">{{ formatDateTime(activity.endTime) }}</span></div>
              <div class="info-row"><span class="info-label">截止</span><span class="info-val mono">{{ formatDateTime(activity.registrationDeadline) }}</span></div>
              <div class="info-row"><span class="info-label">人數</span><span class="info-val mono">{{ activity.maxParticipants || '無上限' }}</span></div>
              <div class="info-row">
                <span class="info-label">費用</span>
                <span class="info-val mono" :class="{ 'val-pink': activity.feeAmount > 0 }">
                  {{ activity.feeAmount > 0 ? `NT$ ${activity.feeAmount}` : '免費' }}
                </span>
              </div>
            </div>
            <p v-if="activity.description" class="info-desc">{{ activity.description }}</p>
          </div>
        </aside>

      </div>
    </div>

    <!-- ── Confirm Dialog（單一模式） ── -->
    <Teleport to="body">
      <div v-if="showConfirmDialog" class="modal-overlay" @click.self="showConfirmDialog = false">
        <div class="dialog-box">
          <div class="dialog-header">
            <h2 class="dialog-title">{{ dialogTitle }}</h2>
            <button class="dialog-close" @click="showConfirmDialog = false">×</button>
          </div>
          <div class="dialog-body">
            <p class="dialog-msg">{{ dialogMsg }}</p>
            <div class="dialog-activity">
              <p class="d-act-title">{{ activity?.title }}</p>
              <p class="d-act-meta mono" v-if="publishType === 'scheduled'">預定發布：{{ formatSchedulePreview }}</p>
            </div>
          </div>
          <div class="dialog-footer">
            <button class="modal-btn cancel" @click="showConfirmDialog = false" :disabled="isPublishing">取消</button>
            <button class="modal-btn confirm" @click="executeAction" :disabled="isPublishing">
              {{ isPublishing ? '處理中...' : '確認' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { activityApi } from '@/api/activity'

const router = useRouter()
const route = useRoute()


const activityId = route.params.activityId

// ── 共用狀態 ──
const loading = ref(false)
const isPublishing = ref(false)
const publishType = ref('immediate')
const scheduledDate = ref('')
const scheduledTime = ref('')
const errorMsg = ref('')

// ── 單一模式 ──
const activity = ref(null)
const showConfirmDialog = ref(false)
const dialogTitle = ref('')
const dialogMsg = ref('')
let pendingAction = null

// ── 批量模式 ──
const draftActivities = ref([])
const selectedIdsSet = ref(new Set())
const publishedCount = ref(0)
const batchResult = ref(null)

// 用 computed 讓 selectedIds.size 是響應式的
const selectedIds = computed(() => selectedIdsSet.value)

const toggleSelect = (id) => {
  const s = new Set(selectedIdsSet.value)
  if (s.has(id)) s.delete(id)
  else s.add(id)
  selectedIdsSet.value = s
}

const toggleSelectAll = () => {
  if (selectedIdsSet.value.size === draftActivities.value.length) {
    selectedIdsSet.value = new Set()
  } else {
    selectedIdsSet.value = new Set(draftActivities.value.map(a => a.id))
  }
}

const clearSelection = () => { selectedIdsSet.value = new Set() }

// ── Navbar ──
const navHidden = ref(false)
let lastY = 0
const onScroll = () => {
  const y = window.scrollY
  navHidden.value = y > lastY && y > 60
  lastY = y
}
onMounted(() => {
  window.addEventListener('scroll', onScroll, { passive: true })
  if (activityId) loadActivity()
  else loadDraftActivities()
})
onUnmounted(() => window.removeEventListener('scroll', onScroll))

// ── 載入單一活動 ──
const loadActivity = async () => {
  loading.value = true
  try {
    const response = await activityApi.getActivityDetails(activityId)
    activity.value = response.data
    if (activity.value.status !== 'DRAFT') {
      alert('此活動已發布，無需重複設定')
      router.push({ name: 'activity-list-container' })
    }
  } catch (e) {
    const s = e.response?.status
    errorMsg.value = s === 404 ? `找不到活動（ID: ${activityId}）`
      : s === 403 ? '沒有權限查看此活動'
      : s === 401 ? '登入已過期，請重新登入'
      : `載入失敗（${s ?? '網路錯誤'}）`
    activity.value = null
    console.error('載入活動失敗:', e)
  } finally {
    loading.value = false
  }
}

// ── 載入草稿列表 ──
const loadDraftActivities = async () => {
  loading.value = true
  try {
    const response = await activityApi.getDraftActivities()
    draftActivities.value = response.data
  } catch (e) {
    console.error('載入草稿失敗:', e)
    draftActivities.value = []
  } finally {
    loading.value = false
  }
}

// ── 批量發布 ──
const confirmBatchPublish = async () => {
  const count = selectedIdsSet.value.size
  if (count === 0) return
  const method = publishType.value === 'immediate' ? '立即發布' : `預約發布（${formatSchedulePreview.value}）`
  if (!confirm(`確定要將 ${count} 個活動進行「${method}」嗎？`)) return

  isPublishing.value = true
  publishedCount.value = 0
  batchResult.value = { success: 0, failed: 0, errors: [] }

  const ids = Array.from(selectedIdsSet.value)
  for (const id of ids) {
    try {
      if (publishType.value === 'immediate') {
        await activityApi.publishActivity(id)
      } else {
        const publishAt = new Date(`${scheduledDate.value}T${scheduledTime.value}`).toISOString()
        await activityApi.schedulePublishActivity(id, { publishAt })
      }
      batchResult.value.success++
    } catch (e) {
      batchResult.value.failed++
      const act = draftActivities.value.find(a => a.id === id)
      batchResult.value.errors.push({
        id,
        title: act?.title || `活動 ${id}`,
        message: e.response?.data?.message || `HTTP ${e.response?.status ?? '錯誤'}`
      })
    }
    publishedCount.value++
  }

  isPublishing.value = false
  await loadDraftActivities()
  selectedIdsSet.value = new Set()

  if (batchResult.value.failed === 0) {
    alert(`✓ ${batchResult.value.success} 個活動全部發布成功！`)
    batchResult.value = null
  }
}

// ── Computed ──
const minDate = computed(() => new Date().toISOString().split('T')[0])

const isScheduleValid = computed(() => {
  if (!scheduledDate.value || !scheduledTime.value) return false
  return new Date(`${scheduledDate.value}T${scheduledTime.value}`) > new Date()
})

const formatSchedulePreview = computed(() => {
  if (!scheduledDate.value || !scheduledTime.value) return ''
  const d = new Date(`${scheduledDate.value}T${scheduledTime.value}`)
  const days = ['Sun','Mon','Tue','Wed','Thu','Fri','Sat']
  return `${d.getFullYear()}/${String(d.getMonth()+1).padStart(2,'0')}/${String(d.getDate()).padStart(2,'0')} (${days[d.getDay()]}) ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
})

const coverStyle = computed(() => {
  if (activity.value?.coverImageUrl) {
    return { backgroundImage: `url(${activity.value.coverImageUrl})`, backgroundSize: 'cover', backgroundPosition: 'center' }
  }
  return {}
})

// ── Helpers ──
const getStatusLabel = (s) =>
  ({ DRAFT:'草稿', PUBLISHED:'已發布', COMPLETED:'已完成', CANCELLED:'已取消', SCHEDULED:'已預約', ONGOING:'進行中' })[s] || s

const getActivityTypeLabel = (t) =>
  ({ REGULAR:'社課', SPECIAL:'特別活動', TRAINING:'團練', PERFORMANCE:'演出', COMPETITION:'比賽' })[t] || t

const formatDateTime = (dt) => {
  if (!dt) return '—'
  const d = new Date(dt)
  const days = ['Sun','Mon','Tue','Wed','Thu','Fri','Sat']
  return `${d.getFullYear()}/${String(d.getMonth()+1).padStart(2,'0')}/${String(d.getDate()).padStart(2,'0')} (${days[d.getDay()]}) ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
}

// ── 單一模式 Actions ──
const confirmPublish = () => {
  dialogTitle.value = '確認立即發布'
  dialogMsg.value = '活動將立即對外公開，並開始接受報名，確定嗎？'
  pendingAction = executePublish
  showConfirmDialog.value = true
}

const confirmSchedulePublish = () => {
  if (!isScheduleValid.value) return
  dialogTitle.value = '確認預約發布'
  dialogMsg.value = '確定在以下時間自動發布此活動嗎？'
  pendingAction = executeSchedulePublish
  showConfirmDialog.value = true
}

const executeAction = () => { if (pendingAction) pendingAction() }

const executePublish = async () => {
  isPublishing.value = true
  try {
    await activityApi.publishActivity(activityId)
    showConfirmDialog.value = false
    alert(`活動「${activity.value.title}」已成功發布！`)
    router.push({ name: 'activity-list-container' })
  } catch (e) {
    const s = e.response?.status
    alert(s === 400 ? '發布失敗：活動資料不完整，請檢查必填欄位'
      : s === 409 ? '此活動已發布過了'
      : `發布失敗（${s ?? '網路錯誤'}），請稍後再試`)
  } finally {
    isPublishing.value = false
  }
}

const executeSchedulePublish = async () => {
  isPublishing.value = true
  try {
    const publishAt = new Date(`${scheduledDate.value}T${scheduledTime.value}`).toISOString()
    await activityApi.schedulePublishActivity(activityId, { publishAt })
    showConfirmDialog.value = false
    alert(`活動「${activity.value.title}」已預約於 ${formatSchedulePreview.value} 發布！`)
    router.push({ name: 'activity-list-container' })
  } catch (e) {
    alert(`預約發布失敗（${e.response?.status ?? '網路錯誤'}），請稍後再試`)
  } finally {
    isPublishing.value = false
  }
}

const saveDraft = () => {
  alert(`活動「${activity.value.title}」已保留為草稿`)
  router.push({ name: 'activity-list-container' })
}

const goToEdit = () => {
  router.push(`/admin/update-activity-container/${activityId}`)
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Bebas+Neue&family=Space+Mono:wght@400;700&family=Noto+Sans+TC:wght@300;400;500;700&display=swap');

* { box-sizing: border-box; }
.pa-wrap { min-height: 100vh; background: #fff; font-family: 'Noto Sans TC', sans-serif; color: #0a0a0a; }
.mono { font-family: 'Space Mono', monospace; }

/* ── Navbar ── */

.navbar-hidden { transform: translateY(-100%); }
.nav-inner { max-width: 1300px; margin: 0 auto; display: flex; justify-content: space-between; align-items: center; }
.nav-logo { font-family: 'Space Mono', monospace; font-size: 0.9rem; font-weight: 700; letter-spacing: 0.18em; color: #0a0a0a; text-decoration: none; }
.nav-logo:hover { color: #ff2d6b; }
.nav-crumb { font-family: 'Space Mono', monospace; font-size: 0.62rem; letter-spacing: 0.15em; color: #aaa; }
.nav-crumb-active { color: #ff2d6b; }

/* ── Content ── */
.pa-content { max-width: 1300px; margin: 0 auto; padding: 8rem 3rem 4rem; }

/* ── Header ── */
.pa-header {
  display: flex; justify-content: space-between; align-items: flex-end;
  padding-bottom: 2rem; margin-bottom: 2.5rem;
  border-bottom: 2px solid #0a0a0a;
}
.eyebrow { display: flex; align-items: center; gap: 0.6rem; margin-bottom: 0.6rem; }
.eyebrow-line { display: block; width: 24px; height: 2px; background: #ff2d6b; }
.eyebrow-text { font-family: 'Space Mono', monospace; font-size: 0.6rem; letter-spacing: 0.2em; color: #ff2d6b; }
.pa-title { font-family: 'Bebas Neue', sans-serif; font-size: 4rem; letter-spacing: 0.06em; line-height: 1; margin: 0; }
.title-accent { color: #ff2d6b; }
.pa-subtitle { font-size: 0.85rem; color: #888; margin: 0.5rem 0 0; }
.header-right { display: flex; align-items: center; gap: 1rem; padding-bottom: 0.25rem; }
.selection-badge { display: flex; flex-direction: column; align-items: center; }
.sel-num { font-family: 'Bebas Neue', sans-serif; font-size: 2.5rem; line-height: 1; color: #ff2d6b; }
.sel-label { font-family: 'Space Mono', monospace; font-size: 0.55rem; letter-spacing: 0.15em; color: #aaa; }
.select-all-btn {
  font-family: 'Space Mono', monospace; font-size: 0.65rem; letter-spacing: 0.1em;
  border: 1px solid #0a0a0a; background: transparent; padding: 0.5rem 1rem; cursor: pointer; transition: all 0.2s;
}
.select-all-btn:hover { background: #0a0a0a; color: #fff; }
.status-badge {
  font-family: 'Space Mono', monospace; font-size: 0.62rem; letter-spacing: 0.1em;
  padding: 0.35rem 0.9rem; font-weight: 600;
}
.s-draft { background: #f5f5f5; color: #888; }
.s-published { background: #f0fdf4; color: #15803d; border: 1px solid #bbf7d0; }
.s-completed { background: #fafafa; color: #aaa; }
.s-cancelled { background: #fff1f2; color: #be123c; }
.s-scheduled { background: #fefce8; color: #a16207; }

/* ── States ── */
.state-wrap { display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 80vh; gap: 0.75rem; }
.loading-bars { display: flex; gap: 4px; }
.loading-bars span { display: block; width: 3px; height: 28px; background: #ff2d6b; border-radius: 2px; animation: barPulse 0.8s ease-in-out infinite alternate; }
@keyframes barPulse { from { transform: scaleY(0.3); opacity: 0.3; } to { transform: scaleY(1); opacity: 1; } }
.empty-text { font-family: 'Bebas Neue', sans-serif; font-size: 6rem; color: transparent; -webkit-text-stroke: 1px #e0e0e0; line-height: 1; }
.empty-desc { font-size: 0.85rem; color: #aaa; text-align: center; }

/* ── Batch ── */
.batch-empty { text-align: center; padding: 5rem 0; }
.empty-big { font-family: 'Bebas Neue', sans-serif; font-size: 8rem; color: transparent; -webkit-text-stroke: 1px #eee; line-height: 1; }
.batch-layout { display: grid; grid-template-columns: 1fr 360px; gap: 3rem; align-items: start; }
.section-label { font-family: 'Space Mono', monospace; font-size: 0.6rem; letter-spacing: 0.18em; color: #aaa; text-transform: uppercase; margin: 0 0 1rem; }

@keyframes draftFadeUp { from { opacity: 0; transform: translateY(8px); } to { opacity: 1; transform: translateY(0); } }
.draft-card {
  display: flex; align-items: center; gap: 1rem;
  padding: 1rem 1.25rem; border: 1px solid #f0f0f0;
  margin-bottom: 0.5rem; cursor: pointer;
  animation: draftFadeUp 0.35s ease both;
  transition: border-color 0.2s, background 0.2s;
}
.draft-card:hover { border-color: #0a0a0a; background: #fafafa; }
.draft-card.selected { border-color: #ff2d6b; background: #fff5f7; }
.check-box {
  width: 20px; height: 20px; border: 1.5px solid #ccc;
  display: flex; align-items: center; justify-content: center;
  font-size: 0.7rem; flex-shrink: 0; transition: all 0.15s;
}
.check-box.checked { border-color: #ff2d6b; background: #ff2d6b; color: #fff; }
.draft-info { flex: 1; display: flex; flex-direction: column; gap: 3px; min-width: 0; }
.draft-title { font-weight: 700; font-size: 0.95rem; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.draft-meta { font-size: 0.68rem; color: #999; letter-spacing: 0.04em; }
.draft-fee { font-size: 0.72rem; color: #888; flex-shrink: 0; }
.draft-fee.free { color: #16a34a; }

.batch-options { position: sticky; top: 6rem; }
.batch-hint { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 4rem 2rem; text-align: center; color: #ccc; }
.hint-arrow { font-family: 'Bebas Neue', sans-serif; font-size: 5rem; line-height: 1; color: #eee; margin-bottom: 0.5rem; }

/* ── Publish options ── */
.publish-options { display: flex; flex-direction: column; gap: 0.75rem; margin-bottom: 1.5rem; }
.option-card { border: 1px solid #e8e8e8; cursor: pointer; transition: border-color 0.2s; }
.option-card.active { border-color: #0a0a0a; }
.option-top { display: flex; align-items: center; gap: 1rem; padding: 1rem 1.25rem; }
.option-radio { width: 18px; height: 18px; border: 1.5px solid #ccc; border-radius: 50%; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.radio-inner { width: 8px; height: 8px; border-radius: 50%; background: transparent; transition: background 0.15s; }
.radio-inner.checked { background: #0a0a0a; }
.option-card.active .option-radio { border-color: #0a0a0a; }
.option-text { flex: 1; display: flex; flex-direction: column; gap: 2px; }
.option-title { font-weight: 700; font-size: 0.9rem; }
.option-desc { font-size: 0.78rem; color: #888; }
.option-icon { font-size: 1.2rem; }

/* schedule */
.schedule-inputs { display: grid; grid-template-columns: 1fr 1fr; gap: 0.75rem; margin-top: 0.75rem; }
.schedule-body { padding: 1rem 1.25rem 1.25rem; border-top: 1px solid #f0f0f0; }
.field { display: flex; flex-direction: column; gap: 0.35rem; }
.field-label { font-family: 'Space Mono', monospace; font-size: 0.6rem; letter-spacing: 0.12em; color: #aaa; }
.field-input { border: 1px solid #e0e0e0; padding: 0.55rem 0.75rem; font-family: 'Space Mono', monospace; font-size: 0.78rem; outline: none; transition: border-color 0.2s; background: #fff; color: #0a0a0a; }
.field-input:focus { border-color: #0a0a0a; }
.schedule-preview { display: flex; align-items: center; gap: 0.6rem; margin-top: 0.75rem; padding: 0.65rem 0.9rem; font-size: 0.72rem; }
.schedule-preview.valid { background: #f0fdf4; color: #15803d; }
.schedule-preview.invalid { background: #fff1f2; color: #be123c; }
.preview-icon { flex-shrink: 0; }
.preview-time { margin: 0; font-family: 'Space Mono', monospace; font-size: 0.7rem; }
.preview-warn { margin: 2px 0 0; font-size: 0.65rem; opacity: 0.8; }
.warn-text { font-size: 0.62rem; font-family: 'Space Mono', monospace; }

/* batch result */
.batch-result { margin-bottom: 1rem; }
.result-row { padding: 0.75rem 1rem; font-size: 0.82rem; margin-bottom: 0.5rem; }
.result-row.success { background: #f0fdf4; color: #15803d; border-left: 3px solid #16a34a; }
.result-row.fail { background: #fff1f2; color: #be123c; border-left: 3px solid #dc2626; }
.error-item { font-size: 0.7rem; margin-top: 4px; opacity: 0.85; }

/* actions */
.batch-actions { display: flex; gap: 0.75rem; margin-top: 1.5rem; }
.pa-section { margin-bottom: 1.5rem; }
.pa-layout { display: grid; grid-template-columns: 1fr 320px; gap: 3rem; }
.pa-actions { display: flex; justify-content: space-between; align-items: center; margin-top: 2rem; padding-top: 1.5rem; border-top: 1px solid #f0f0f0; }
.actions-right { display: flex; gap: 0.75rem; }

.btn-outline { border: 1px solid #ccc; background: transparent; font-family: 'Space Mono', monospace; font-size: 0.68rem; letter-spacing: 0.08em; padding: 0.65rem 1.25rem; cursor: pointer; transition: all 0.2s; }
.btn-outline:hover { border-color: #0a0a0a; }
.btn-edit { border: 1px solid #e0e0e0; background: transparent; font-family: 'Space Mono', monospace; font-size: 0.68rem; letter-spacing: 0.08em; padding: 0.65rem 1.25rem; cursor: pointer; color: #888; transition: all 0.2s; }
.btn-edit:hover { color: #0a0a0a; border-color: #0a0a0a; }
.btn-publish { background: #0a0a0a; color: #fff; border: none; font-family: 'Space Mono', monospace; font-size: 0.72rem; font-weight: 700; letter-spacing: 0.08em; padding: 0.75rem 1.75rem; cursor: pointer; transition: background 0.2s; flex: 1; }
.btn-publish:hover:not(:disabled) { background: #ff2d6b; }
.btn-publish:disabled { opacity: 0.45; cursor: not-allowed; }
.cta-btn { background: #0a0a0a; color: #fff; border: none; font-family: 'Space Mono', monospace; font-size: 0.72rem; letter-spacing: 0.08em; padding: 0.75rem 1.75rem; cursor: pointer; margin-top: 1rem; transition: background 0.2s; }
.cta-btn:hover { background: #ff2d6b; }

/* ── Preview (single mode) ── */
.preview-cover { height: 180px; background: #f5f5f5; display: flex; align-items: flex-end; position: relative; overflow: hidden; margin-bottom: 1.25rem; }
.cover-placeholder { position: absolute; top: 50%; left: 50%; transform: translate(-50%,-50%); font-family: 'Space Mono', monospace; font-size: 0.7rem; letter-spacing: 0.2em; color: #ccc; }
.cover-overlay { position: absolute; bottom: 0; left: 0; right: 0; padding: 0.5rem 0.75rem; background: rgba(0,0,0,0.5); }
.cover-type { font-family: 'Space Mono', monospace; font-size: 0.62rem; letter-spacing: 0.12em; color: rgba(255,255,255,0.8); }
.info-title { font-weight: 700; font-size: 1rem; margin: 0 0 1rem; line-height: 1.4; }
.info-grid { display: flex; flex-direction: column; gap: 0.5rem; border-top: 1px solid #f0f0f0; padding-top: 0.75rem; }
.info-row { display: flex; gap: 0.75rem; }
.info-label { font-family: 'Space Mono', monospace; font-size: 0.6rem; letter-spacing: 0.1em; color: #aaa; width: 48px; flex-shrink: 0; margin-top: 2px; }
.info-val { font-size: 0.82rem; }
.info-val.mono { font-family: 'Space Mono', monospace; font-size: 0.72rem; }
.val-pink { color: #ff2d6b; font-weight: 700; }
.info-desc { font-size: 0.82rem; color: #666; line-height: 1.6; margin-top: 0.75rem; border-top: 1px solid #f0f0f0; padding-top: 0.75rem; }

/* ── Dialog ── */
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.4); z-index: 200; display: flex; align-items: center; justify-content: center; }
.dialog-box { background: #fff; width: 420px; max-width: 90vw; }
.dialog-header { display: flex; justify-content: space-between; align-items: center; padding: 1.25rem 1.5rem; border-bottom: 1px solid #f0f0f0; }
.dialog-title { font-family: 'Bebas Neue', sans-serif; font-size: 1.5rem; letter-spacing: 0.06em; margin: 0; }
.dialog-close { background: none; border: none; font-size: 1.3rem; cursor: pointer; color: #aaa; }
.dialog-close:hover { color: #0a0a0a; }
.dialog-body { padding: 1.5rem; }
.dialog-msg { font-size: 0.88rem; color: #555; margin: 0 0 1rem; }
.dialog-activity { background: #fafafa; padding: 0.75rem 1rem; border-left: 3px solid #ff2d6b; }
.d-act-title { font-weight: 700; font-size: 0.9rem; margin: 0 0 4px; }
.d-act-meta { font-family: 'Space Mono', monospace; font-size: 0.68rem; color: #888; margin: 0; }
.dialog-footer { display: flex; gap: 0.75rem; padding: 1rem 1.5rem; border-top: 1px solid #f0f0f0; justify-content: flex-end; }
.modal-btn { font-family: 'Space Mono', monospace; font-size: 0.68rem; letter-spacing: 0.08em; padding: 0.6rem 1.25rem; cursor: pointer; border: none; transition: all 0.2s; }
.modal-btn.cancel { background: #f5f5f5; color: #555; }
.modal-btn.cancel:hover { background: #e8e8e8; }
.modal-btn.confirm { background: #0a0a0a; color: #fff; }
.modal-btn.confirm:hover:not(:disabled) { background: #ff2d6b; }
.modal-btn:disabled { opacity: 0.4; cursor: not-allowed; }

/* ── Responsive ── */
@media (max-width: 960px) {
  .pa-layout, .batch-layout { grid-template-columns: 1fr; }
  .batch-options { position: static; }
  .pa-content { padding-left: 1.5rem; padding-right: 1.5rem; }
  
}
</style>