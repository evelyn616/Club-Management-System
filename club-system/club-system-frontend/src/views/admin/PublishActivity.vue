<template>
  <div class="pa-wrap">

    <!-- Navbar -->
    <nav class="navbar" :class="{ 'navbar-hidden': navHidden }">
      <div class="nav-container">
        <router-link to="/admin/activity-management-container" class="nav-logo">CLUB SYSTEM</router-link>
        <span class="nav-crumb">ADMIN / <span class="nav-crumb-active">發布設定</span></span>
      </div>
    </nav>

    <!-- Loading -->
    <div v-if="loading" class="state-wrap">
      <div class="loading-bars">
        <span v-for="i in 5" :key="i" :style="{ animationDelay: (i * 0.1) + 's' }"></span>
      </div>
      <p class="mono">LOADING...</p>
    </div>

    <!-- Error -->
    <div v-else-if="!activity" class="state-wrap">
      <div class="empty-text">ERROR</div>
      <p class="empty-desc">無法載入活動資訊</p>
      <button class="cta-btn" @click="router.push({ name: 'activity-list-container' })">← 返回活動列表</button>
    </div>

    <!-- Main -->
    <div v-else class="pa-content">

      <!-- Header -->
      <div class="pa-header">
        <div class="header-left">
          <span class="pa-eyebrow">PUBLISH SETTINGS</span>
          <h1 class="pa-title">發布活動設定</h1>
        </div>
        <span class="status-badge" :class="'s-' + activity.status?.toLowerCase()">
          {{ getStatusLabel(activity.status) }}
        </span>
      </div>

      <div class="pa-layout">

        <!-- ===== 左：發布選項 ===== -->
        <div class="pa-main">

          <!-- 發布方式選擇 -->
          <section class="pa-section">
            <p class="section-label">SELECT PUBLISH METHOD</p>

            <div class="publish-options">

              <!-- 立即發布 -->
              <div class="option-card" :class="{ active: publishType === 'immediate' }" @click="publishType = 'immediate'">
                <div class="option-top">
                  <div class="option-radio">
                    <div class="radio-inner" :class="{ checked: publishType === 'immediate' }"></div>
                  </div>
                  <div class="option-text">
                    <span class="option-title">立即發布</span>
                    <span class="option-desc">活動將立即對外開放，並開始接受報名</span>
                  </div>
                  <span class="option-icon">⚡</span>
                </div>
              </div>

              <!-- 預約發布 -->
              <div class="option-card" :class="{ active: publishType === 'scheduled' }" @click="publishType = 'scheduled'">
                <div class="option-top">
                  <div class="option-radio">
                    <div class="radio-inner" :class="{ checked: publishType === 'scheduled' }"></div>
                  </div>
                  <div class="option-text">
                    <span class="option-title">預約發布</span>
                    <span class="option-desc">選擇未來時間，屆時自動發布活動</span>
                  </div>
                  <span class="option-icon">⏰</span>
                </div>

                <!-- 預約時間設定（展開） -->
                <div v-if="publishType === 'scheduled'" class="schedule-body">
                  <div class="schedule-inputs">
                    <div class="field">
                      <label class="field-label">預約發布日期</label>
                      <input type="date" v-model="scheduledDate" :min="minDate" class="field-input" />
                    </div>
                    <div class="field">
                      <label class="field-label">預約發布時間</label>
                      <input type="time" v-model="scheduledTime" class="field-input" />
                    </div>
                  </div>

                  <!-- 時間預覽 -->
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

          <!-- 操作按鈕 -->
          <div class="pa-actions">
            <button class="btn-outline" @click="saveDraft" :disabled="isPublishing">
              儲存為草稿
            </button>
            <div class="actions-right">
              <button class="btn-edit" @click="goToEdit">← 返回編輯</button>
              <button
                v-if="publishType === 'immediate'"
                class="btn-publish"
                @click="confirmPublish"
                :disabled="isPublishing"
              >
                {{ isPublishing ? '發布中...' : '確認立即發布 →' }}
              </button>
              <button
                v-else
                class="btn-publish"
                @click="confirmSchedulePublish"
                :disabled="isPublishing || !isScheduleValid"
              >
                {{ isPublishing ? '設定中...' : '確認預約發布 →' }}
              </button>
            </div>
          </div>

        </div>

        <!-- ===== 右：活動預覽 ===== -->
        <aside class="pa-sidebar">
          <p class="section-label">ACTIVITY PREVIEW</p>

          <!-- 封面 -->
          <div class="preview-cover" :style="coverStyle">
            <span v-if="!activity.coverImageUrl" class="cover-placeholder">NO IMAGE</span>
            <div class="cover-overlay">
              <span class="cover-type">{{ getActivityTypeLabel(activity.activityType) }}</span>
            </div>
          </div>

          <!-- 資訊 -->
          <div class="preview-info">
            <p class="info-title">{{ activity.title }}</p>

            <div class="info-grid">
              <div class="info-row">
                <span class="info-label">地點</span>
                <span class="info-val">{{ activity.location || '—' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">開始</span>
                <span class="info-val mono">{{ formatDateTime(activity.startTime) }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">結束</span>
                <span class="info-val mono">{{ formatDateTime(activity.endTime) }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">報名截止</span>
                <span class="info-val mono">{{ formatDateTime(activity.registrationDeadline) }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">人數上限</span>
                <span class="info-val mono">{{ activity.maxParticipants || '無上限' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">費用</span>
                <span class="info-val mono" :class="{ 'val-pink': activity.feeAmount > 0 }">
                  {{ activity.feeAmount > 0 ? `NT$ ${activity.feeAmount}` : '免費' }}
                </span>
              </div>
              <div class="info-row">
                <span class="info-label">建立者</span>
                <span class="info-val mono">{{ activity.createdBy }}</span>
              </div>
            </div>

            <p v-if="activity.description" class="info-desc">{{ activity.description }}</p>
          </div>
        </aside>

      </div>
    </div>

    <!-- ===== 確認 Dialog ===== -->
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

// ✅ Fix 1: route.params.activityId 是字串，不是 ref，不能用 .value
// 原本 loadActivity / executePublish 都用 activityId.value → undefined → API 路徑錯誤
const activityId = route.params.activityId

// ===== State =====
const activity = ref(null)
const loading = ref(false)
const isPublishing = ref(false)
const publishType = ref('immediate')
const scheduledDate = ref('')
const scheduledTime = ref('')

// ===== Dialog =====
const showConfirmDialog = ref(false)
const dialogTitle = ref('')
const dialogMsg = ref('')
let pendingAction = null

// ===== Navbar scroll =====
const navHidden = ref(false)
let lastY = 0
const onScroll = () => {
  const y = window.scrollY
  navHidden.value = y > lastY && y > 60
  lastY = y
}
onMounted(() => {
  window.addEventListener('scroll', onScroll, { passive: true })
  loadActivity()
})
onUnmounted(() => window.removeEventListener('scroll', onScroll))

// ===== Load =====
const loadActivity = async () => {
  loading.value = true
  try {
    // ✅ Fix 1 applied: activityId (not activityId.value)
    const response = await activityApi.getActivityDetails(activityId)
    activity.value = response.data

    // ✅ Fix 5: SCHEDULED 不是 SCHEDULE
    if (activity.value.status !== 'DRAFT') {
      alert('此活動已發布，無需重複設定')
      router.push({ name: 'activity-list-container' })
    }
  } catch (e) {
    console.error('載入活動失敗:', e)
    // ✅ Fix 4: catch 要把 activity 設 null，讓 UI 顯示 Error 狀態而非卡住
    activity.value = null
  } finally {
    loading.value = false
  }
}

// ===== Computed =====
const minDate = computed(() => new Date().toISOString().split('T')[0])

const isScheduleValid = computed(() => {
  if (!scheduledDate.value || !scheduledTime.value) return false
  return new Date(`${scheduledDate.value}T${scheduledTime.value}`) > new Date()
})

// ✅ Fix 2: 改成 computed（原始版定義成 function 但用 .value 取值 → undefined）
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

// ===== Helpers =====
const getStatusLabel = (s) =>
  // ✅ Fix 5: SCHEDULED（有 D）才是正確的 enum 值
  ({ DRAFT:'草稿', PUBLISHED:'已發布', COMPLETED:'已完成', CANCELLED:'已取消', SCHEDULED:'已預約' })[s] || s

const getActivityTypeLabel = (t) =>
  ({ REGULAR:'社課', SPECIAL:'特別活動', TRAINING:'團練', PERFORMANCE:'演出', COMPETITION:'比賽' })[t] || t

const formatDateTime = (dt) => {
  if (!dt) return '—'
  const d = new Date(dt)
  const days = ['Sun','Mon','Tue','Wed','Thu','Fri','Sat']
  return `${d.getFullYear()}/${String(d.getMonth()+1).padStart(2,'0')}/${String(d.getDate()).padStart(2,'0')} (${days[d.getDay()]}) ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
}

// ===== Actions =====
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

const executeAction = () => {
  if (pendingAction) pendingAction()
}

const executePublish = async () => {
  isPublishing.value = true
  try {
    // ✅ Fix 1 applied: activityId (not activityId.value)
    await activityApi.publishActivity(activityId)
    showConfirmDialog.value = false
    alert(`活動「${activity.value.title}」已成功發布！`)
    // ✅ Fix 3: route name 統一為 activity-list-container
    router.push({ name: 'activity-list-container' })
  } catch (e) {
    const s = e.response?.status
    if (s === 401) {
      // 401 不應該觸發登出，只顯示訊息（token 還在）
      alert('操作失敗：權限不足，請確認您已登入管理員帳號')
    } else if (s === 400) {
      alert('發布失敗：活動資料不完整，請檢查必填欄位')
    } else if (s === 409) {
      alert('此活動已發布過了')
    } else {
      alert(`發布失敗（${s ?? '網路錯誤'}），請稍後再試`)
    }
  } finally {
    isPublishing.value = false
  }
}

const executeSchedulePublish = async () => {
  isPublishing.value = true
  try {
    const publishAt = new Date(`${scheduledDate.value}T${scheduledTime.value}`).toISOString()
    // ✅ Fix 1 applied: activityId (not activityId.value)
    await activityApi.schedulePublishActivity(activityId, { publishAt })
    showConfirmDialog.value = false
    alert(`活動「${activity.value.title}」已預約於 ${formatSchedulePreview.value} 發布！`)
    // ✅ Fix 3: route name 統一
    router.push({ name: 'activity-list-container' })
  } catch (e) {
    const s = e.response?.status
    if (s === 401) {
      alert('操作失敗：權限不足，請確認您已登入管理員帳號')
    } else {
      alert(`預約發布失敗（${s ?? '網路錯誤'}），請稍後再試`)
    }
  } finally {
    isPublishing.value = false
  }
}

const saveDraft = () => {
  alert(`活動「${activity.value.title}」已保留為草稿`)
  // ✅ Fix 3: route name 統一
  router.push({ name: 'activity-list-container' })
}

const goToEdit = () => {
  // ✅ Fix 1 applied: activityId (not activityId.value)
  router.push(`/admin/update-activity-container/${activityId}`)
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Bebas+Neue&family=Noto+Sans+TC:wght@300;400;500;700&family=Space+Mono:wght@400;700&display=swap');

/* ===== Base ===== */
.pa-wrap { min-height: 100vh; background: #fff; font-family: 'Noto Sans TC', sans-serif; color: #0a0a0a; }
.mono { font-family: 'Space Mono', monospace; }

/* ===== Navbar ===== */
.navbar {
  position: fixed; top: 0; left: 0; right: 0; z-index: 100;
  padding: 1rem 2rem; background: rgba(255,255,255,0.9);
  backdrop-filter: blur(12px); border-bottom: 1px solid rgba(0,0,0,0.08);
  transform: translateY(0); transition: transform 0.35s ease;
}
.navbar-hidden { transform: translateY(-100%); }
.nav-container { max-width: 1300px; margin: 0 auto; display: flex; justify-content: space-between; align-items: center; }
.nav-logo { font-family: 'Space Mono', monospace; font-size: 1rem; font-weight: 700; letter-spacing: 0.15em; color: #0a0a0a; text-decoration: none; }
.nav-logo:hover { color: #ff2d6b; }
.nav-crumb { font-family: 'Space Mono', monospace; font-size: 0.65rem; letter-spacing: 0.12em; color: #aaa; }
.nav-crumb-active { color: #0a0a0a; }

/* ===== State ===== */
.state-wrap { display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 80vh; gap: 0.75rem; }
.loading-bars { display: flex; gap: 4px; }
.loading-bars span { display: block; width: 3px; height: 28px; background: #ff2d6b; border-radius: 2px; animation: barPulse 0.8s ease-in-out infinite alternate; }
@keyframes barPulse { from { transform: scaleY(0.3); opacity: 0.3; } to { transform: scaleY(1); opacity: 1; } }
.empty-text { font-family: 'Bebas Neue', sans-serif; font-size: 6rem; color: transparent; -webkit-text-stroke: 1px #e0e0e0; line-height: 1; }
.empty-desc { font-size: 0.85rem; color: #aaa; }
.cta-btn { background: #0a0a0a; color: #fff; border: none; padding: 0.65rem 1.5rem; font-family: 'Space Mono', monospace; font-size: 0.72rem; cursor: pointer; }
.cta-btn:hover { background: #ff2d6b; }

/* ===== Content ===== */
.pa-content { max-width: 1300px; margin: 0 auto; padding: 0 2rem 4rem; }

/* ===== Header ===== */
.pa-header {
  display: flex; justify-content: space-between; align-items: flex-end;
  padding: 6.5rem 0 2rem; border-bottom: 2px solid #0a0a0a;
  margin-bottom: 2.5rem; flex-wrap: wrap; gap: 1rem;
}
.pa-eyebrow { display: block; font-family: 'Space Mono', monospace; font-size: 0.6rem; letter-spacing: 0.25em; color: #ff2d6b; font-weight: 700; margin-bottom: 0.4rem; }
.pa-title { font-family: 'Bebas Neue', sans-serif; font-size: 4rem; line-height: 1; margin: 0; letter-spacing: 0.02em; }
.status-badge { font-family: 'Space Mono', monospace; font-size: 0.62rem; letter-spacing: 0.1em; padding: 0.3rem 0.75rem; align-self: flex-end; margin-bottom: 0.5rem; }
.s-draft     { background: #f5f5f5; color: #888; }
.s-published { background: #e8f5e9; color: #2e7d32; }
.s-completed { background: #e3f2fd; color: #1565c0; }
.s-cancelled { background: #ffebee; color: #c62828; }
.s-schedule  { background: #fff3e0; color: #e65100; }

/* ===== Layout ===== */
.pa-layout { display: grid; grid-template-columns: 1fr 320px; gap: 3rem; align-items: start; }

/* ===== Section ===== */
.pa-section { margin-bottom: 2rem; }
.section-label { font-family: 'Space Mono', monospace; font-size: 0.58rem; letter-spacing: 0.22em; color: #ff2d6b; font-weight: 700; padding-bottom: 0.6rem; border-bottom: 1px solid #f0f0f0; margin-bottom: 1rem; }

/* ===== Publish Options ===== */
.publish-options { display: flex; flex-direction: column; gap: 1rem; }
.option-card {
  border: 1px solid #e0e0e0; padding: 1.25rem;
  cursor: pointer; transition: border-color 0.2s, box-shadow 0.2s;
}
.option-card:hover { border-color: #bbb; }
.option-card.active { border-color: #0a0a0a; box-shadow: inset 3px 0 0 #0a0a0a; }

.option-top { display: flex; align-items: flex-start; gap: 1rem; }
.option-radio {
  width: 18px; height: 18px; border-radius: 50%; border: 2px solid #e0e0e0;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0; margin-top: 1px; transition: border-color 0.2s;
}
.option-card.active .option-radio { border-color: #0a0a0a; }
.radio-inner { width: 8px; height: 8px; border-radius: 50%; background: transparent; transition: background 0.2s; }
.radio-inner.checked { background: #0a0a0a; }

.option-text { flex: 1; }
.option-title { display: block; font-weight: 700; font-size: 0.95rem; margin-bottom: 0.2rem; }
.option-desc { font-size: 0.78rem; color: #888; }
.option-icon { font-size: 1.4rem; }

/* 預約設定展開 */
.schedule-body { margin-top: 1.25rem; padding-top: 1.25rem; border-top: 1px solid #f0f0f0; }
.schedule-inputs { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin-bottom: 1rem; }
.field { display: flex; flex-direction: column; gap: 0.4rem; }
.field-label { font-family: 'Space Mono', monospace; font-size: 0.62rem; letter-spacing: 0.1em; color: #888; }
.field-input {
  padding: 0.65rem 0.85rem; border: 1px solid #e0e0e0; border-radius: 0;
  font-family: 'Noto Sans TC', sans-serif; font-size: 0.85rem;
  outline: none; transition: border-color 0.2s; box-sizing: border-box; width: 100%;
}
.field-input:focus { border-color: #0a0a0a; }

.schedule-preview {
  display: flex; align-items: flex-start; gap: 0.75rem;
  padding: 0.85rem 1rem; background: #f5f5f5;
}
.schedule-preview.valid { background: #e8f5e9; }
.schedule-preview.invalid { background: #ffebee; }
.preview-icon { font-family: 'Space Mono', monospace; font-size: 0.85rem; font-weight: 700; margin-top: 1px; }
.schedule-preview.valid .preview-icon { color: #2e7d32; }
.schedule-preview.invalid .preview-icon { color: #c62828; }
.preview-time { font-family: 'Space Mono', monospace; font-size: 0.78rem; margin: 0; }
.preview-warn { font-size: 0.72rem; color: #c62828; margin: 0.2rem 0 0; }

/* ===== Actions ===== */
.pa-actions {
  display: flex; justify-content: space-between; align-items: center;
  padding-top: 1.5rem; border-top: 1px solid #e0e0e0;
  margin-top: 0.5rem; flex-wrap: wrap; gap: 0.75rem;
}
.actions-right { display: flex; gap: 0.75rem; }
.btn-outline {
  background: transparent; border: 1px solid #e0e0e0; color: #888;
  padding: 0.65rem 1.25rem; font-family: 'Space Mono', monospace;
  font-size: 0.72rem; letter-spacing: 0.08em; cursor: pointer; transition: all 0.2s;
}
.btn-outline:hover:not(:disabled) { border-color: #0a0a0a; color: #0a0a0a; }
.btn-outline:disabled { opacity: 0.4; cursor: not-allowed; }
.btn-edit {
  background: transparent; border: 1px solid #e0e0e0; color: #555;
  padding: 0.65rem 1.25rem; font-family: 'Space Mono', monospace;
  font-size: 0.72rem; letter-spacing: 0.08em; cursor: pointer; transition: all 0.2s;
}
.btn-edit:hover { border-color: #0a0a0a; color: #0a0a0a; }
.btn-publish {
  background: #0a0a0a; color: #fff; border: none;
  padding: 0.7rem 1.75rem; font-family: 'Space Mono', monospace;
  font-size: 0.78rem; letter-spacing: 0.1em; cursor: pointer; transition: background 0.2s;
}
.btn-publish:hover:not(:disabled) { background: #ff2d6b; }
.btn-publish:disabled { opacity: 0.4; cursor: not-allowed; }

/* ===== Sidebar Preview ===== */
.pa-sidebar { position: sticky; top: 5rem; }
.preview-cover {
  height: 200px; background: #f0f0f0;
  display: flex; align-items: center; justify-content: center;
  position: relative; overflow: hidden; margin-bottom: 0;
}
.cover-placeholder { font-family: 'Bebas Neue', sans-serif; font-size: 1.5rem; color: #ccc; }
.cover-overlay { position: absolute; inset: 0; background: linear-gradient(to top, rgba(0,0,0,0.5) 0%, transparent 60%); display: flex; align-items: flex-end; padding: 0.75rem; }
.cover-type { font-family: 'Space Mono', monospace; font-size: 0.58rem; letter-spacing: 0.12em; color: #fff; }

.preview-info { border: 1px solid #e0e0e0; border-top: none; padding: 1.25rem; }
.info-title { font-weight: 700; font-size: 1rem; margin: 0 0 1rem; line-height: 1.3; }
.info-grid { display: flex; flex-direction: column; gap: 0; }
.info-row { display: flex; justify-content: space-between; align-items: baseline; padding: 0.45rem 0; border-bottom: 1px solid #f5f5f5; gap: 0.5rem; }
.info-row:last-child { border-bottom: none; }
.info-label { font-family: 'Space Mono', monospace; font-size: 0.6rem; letter-spacing: 0.08em; color: #aaa; flex-shrink: 0; }
.info-val { font-family: 'Space Mono', monospace; font-size: 0.68rem; color: #333; text-align: right; }
.val-pink { color: #ff2d6b; font-weight: 700; }
.info-desc { font-size: 0.78rem; color: #888; margin: 1rem 0 0; line-height: 1.6; border-top: 1px solid #f5f5f5; padding-top: 1rem; }

/* ===== Modal / Dialog ===== */
.modal-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,0.5);
  backdrop-filter: blur(4px); display: flex; align-items: center; justify-content: center; z-index: 200;
}
.dialog-box { background: #fff; width: 440px; max-width: 95vw; }
.dialog-header { display: flex; justify-content: space-between; align-items: center; padding: 1.25rem 1.5rem; border-bottom: 2px solid #0a0a0a; }
.dialog-title { font-family: 'Bebas Neue', sans-serif; font-size: 1.5rem; letter-spacing: 0.05em; margin: 0; }
.dialog-close { background: none; border: none; font-size: 1.3rem; cursor: pointer; color: #aaa; line-height: 1; }
.dialog-close:hover { color: #0a0a0a; }
.dialog-body { padding: 1.5rem; }
.dialog-msg { font-size: 0.85rem; color: #555; margin: 0 0 1rem; }
.dialog-activity { background: #f5f5f5; border-left: 3px solid #0a0a0a; padding: 0.85rem 1rem; }
.d-act-title { font-weight: 700; font-size: 0.95rem; margin: 0 0 0.25rem; }
.d-act-meta { font-family: 'Space Mono', monospace; font-size: 0.68rem; color: #888; margin: 0; }
.dialog-footer { display: flex; justify-content: flex-end; gap: 0.6rem; padding: 1rem 1.5rem; border-top: 1px solid #e0e0e0; }
.modal-btn { padding: 0.55rem 1.2rem; font-family: 'Space Mono', monospace; font-size: 0.72rem; letter-spacing: 0.06em; cursor: pointer; border: 1px solid; transition: all 0.15s; }
.modal-btn.cancel { border-color: #e0e0e0; color: #555; background: #fff; }
.modal-btn.cancel:hover { background: #f5f5f5; }
.modal-btn.confirm { border-color: #0a0a0a; color: #fff; background: #0a0a0a; }
.modal-btn.confirm:hover:not(:disabled) { background: #ff2d6b; border-color: #ff2d6b; }
.modal-btn:disabled { opacity: 0.5; cursor: not-allowed; }

/* ===== Responsive ===== */
@media (max-width: 900px) {
  .pa-layout { grid-template-columns: 1fr; }
  .pa-sidebar { position: static; }
  .pa-title { font-size: 2.8rem; }
}
@media (max-width: 600px) {
  .schedule-inputs { grid-template-columns: 1fr; }
  .pa-actions { flex-direction: column; align-items: stretch; }
  .actions-right { flex-direction: column; }
}
</style>