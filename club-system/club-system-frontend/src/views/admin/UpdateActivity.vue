<template>
  <div class="ua-wrap">


    <!-- Loading -->
    <div v-if="loading" class="state-wrap">
      <div class="loading-bars">
        <span v-for="i in 5" :key="i" :style="{ animationDelay: (i * 0.1) + 's' }"></span>
      </div>
      <p class="mono state-text">LOADING...</p>
    </div>

    <!-- Loaded -->
    <template v-else>

      <!-- Header -->
      <div class="ua-header">
        <div class="header-left">
          <span class="ua-eyebrow">EDIT ACTIVITY #{{ activityId }}</span>
          <h1 class="ua-title">編輯活動</h1>
        </div>
        <!-- 有報名警告 -->
        <div v-if="hasRegistrations" class="reg-warning">
          <span class="warn-dot">!</span>
          <span>已有 <strong>{{ registrationCount }}</strong> 筆報名，修改時間或地點將通知所有報名者</span>
        </div>
      </div>

      <div class="ua-layout">

        <!-- ===== 表單 ===== -->
        <form class="ua-form" @submit.prevent="handlesubmit">

          <!-- 基本資訊 -->
          <section class="form-section">
            <p class="section-label">BASIC INFO</p>

            <div class="field">
              <label class="field-label">活動標題 <span class="req">*</span></label>
              <input
                v-model="form.title" type="text"
                class="field-input" :class="{ 'input-error': errors.title }"
                maxlength="100" placeholder="輸入活動名稱"
                @blur="touch('title')"
              />
              <p class="err-msg" v-if="errors.title">{{ errors.title }}</p>
              <p class="char-count" v-else>{{ (form.title || '').length }} / 100</p>
            </div>

            <div class="field">
              <label class="field-label">活動描述</label>
              <textarea
                v-model="form.description"
                class="field-textarea" :class="{ 'input-error': errors.description }"
                rows="4" maxlength="5000" placeholder="輸入活動說明、注意事項等..."
                @blur="touch('description')"
              ></textarea>
              <p class="err-msg" v-if="errors.description">{{ errors.description }}</p>
              <p class="char-count" v-else-if="form.description">{{ form.description.length }} / 5000</p>
            </div>

            <div class="field">
              <label class="field-label">封面圖片</label>
              <ImageUploader v-model="form.coverImageUrl" />
            </div>
          </section>

          <!-- 時間與地點 -->
          <section class="form-section">
            <p class="section-label">TIME & PLACE</p>

            <div class="field-row">
              <div class="field">
                <label class="field-label">
                  開始時間 <span class="req">*</span>
                  <span v-if="hasRegistrations && isTimeChanged" class="changed-tag">已修改</span>
                </label>
                <input
                  v-model="form.startTime" type="datetime-local"
                  class="field-input"
                  :class="{ modified: hasRegistrations && isTimeChanged, 'input-error': errors.startTime }"
                  @blur="touch('startTime')"
                />
                <p class="err-msg" v-if="errors.startTime">{{ errors.startTime }}</p>
              </div>
              <div class="field">
                <label class="field-label">結束時間 <span class="req">*</span></label>
                <input
                  v-model="form.endTime" type="datetime-local"
                  class="field-input"
                  :class="{ modified: hasRegistrations && isTimeChanged, 'input-error': errors.endTime }"
                  @blur="touch('endTime')"
                />
                <p class="err-msg" v-if="errors.endTime">{{ errors.endTime }}</p>
              </div>
            </div>

            <div class="field-row">
              <div class="field">
                <label class="field-label">報名截止時間 <span class="req">*</span></label>
                <input
                  v-model="form.registrationDeadline" type="datetime-local"
                  class="field-input" :class="{ 'input-error': errors.registrationDeadline }"
                  @blur="touch('registrationDeadline')"
                />
                <p class="err-msg" v-if="errors.registrationDeadline">{{ errors.registrationDeadline }}</p>
              </div>
              <div class="field">
                <label class="field-label">
                  活動地點 <span class="req">*</span>
                  <span v-if="hasRegistrations && isLocationChanged" class="changed-tag">已修改</span>
                </label>
                <input
                  v-model="form.location" type="text"
                  class="field-input"
                  :class="{ modified: hasRegistrations && isLocationChanged, 'input-error': errors.location }"
                  maxlength="200" placeholder="輸入地點"
                  @blur="touch('location')"
                />
                <p class="err-msg" v-if="errors.location">{{ errors.location }}</p>
              </div>
            </div>

            <!-- 通知提示 -->
            <div v-if="hasRegistrations && hasImportantChanges" class="notify-banner">
              <span class="notify-icon">⚑</span>
              儲存後將自動通知 {{ registrationCount }} 位已報名的人員
            </div>
          </section>

          <!-- 活動設定 -->
          <section class="form-section">
            <p class="section-label">SETTINGS</p>

            <div class="field-row">
              <div class="field">
                <label class="field-label">活動類型 <span class="req">*</span></label>
                <select
                  v-model="form.activityType" class="field-select"
                  :class="{ 'input-error': errors.activityType }"
                  @change="touch('activityType')"
                >
                  <option value="">請選擇</option>
                  <option value="REGULAR">社課</option>
                  <option value="SPECIAL">特別活動</option>
                  <option value="TRAINING">團練</option>
                  <option value="PERFORMANCE">演出</option>
                  <option value="COMPETITION">比賽</option>
                </select>
                <p class="err-msg" v-if="errors.activityType">{{ errors.activityType }}</p>
              </div>
              <div class="field">
                <label class="field-label">目標對象 <span class="req">*</span></label>
                <select
                  v-model="form.targetAudience" class="field-select"
                  :class="{ 'input-error': errors.targetAudience }"
                  @change="touch('targetAudience')"
                >
                  <option value="">請選擇</option>
                  <option value="ALL">所有人</option>
                  <option value="MEMBER_ONLY">社員限定</option>
                  <option value="MANAGER_ONLY">幹部限定</option>
                </select>
                <p class="err-msg" v-if="errors.targetAudience">{{ errors.targetAudience }}</p>
              </div>
            </div>

            <div class="field-row">
              <div class="field">
                <label class="field-label">費用金額 <span class="req">*</span></label>
                <div class="input-prefix-wrap">
                  <span class="input-prefix">NT$</span>
                  <input
                    v-model.number="form.feeAmount" type="number"
                    class="field-input has-prefix" :class="{ 'input-error': errors.feeAmount }"
                    min="0" max="999999" placeholder="0"
                    @blur="touch('feeAmount')"
                  />
                </div>
                <p class="err-msg" v-if="errors.feeAmount">{{ errors.feeAmount }}</p>
              </div>
              <div class="field">
                <label class="field-label">最大參與人數</label>
                <input
                  v-model.number="form.maxParticipants" type="number"
                  class="field-input" :class="{ 'input-error': errors.maxParticipants }"
                  min="1" max="9999" placeholder="不填則無上限"
                  @blur="touch('maxParticipants')"
                />
                <p class="err-msg" v-if="errors.maxParticipants">{{ errors.maxParticipants }}</p>
              </div>
            </div>
          </section>

          <!-- 早鳥優惠 -->
          <section class="form-section">
            <p class="section-label">EARLY BIRD</p>

            <div class="field toggle-field">
              <label class="toggle-label">
                <input type="checkbox" v-model="form.earlyBirdEnabled" class="toggle-checkbox" />
                <span class="toggle-text">啟用早鳥優惠</span>
                <span class="toggle-hint">（未啟用則使用全域折扣設定）</span>
              </label>
            </div>

            <div v-if="form.earlyBirdEnabled" class="field">
              <label class="field-label">早鳥截止時間 <span class="req">*</span></label>
              <input
                v-model="form.earlyBirdDeadline"
                type="datetime-local"
                class="field-input"
                :class="{ 'input-error': errors.earlyBirdDeadline }"
                @blur="touch('earlyBirdDeadline')"
              />
              <p class="err-msg" v-if="errors.earlyBirdDeadline">{{ errors.earlyBirdDeadline }}</p>
              <p class="field-hint-text" v-else>此時間之前報名可享早鳥優惠（折扣率請至「折扣設定」調整）</p>
            </div>
          </section>

          <!-- 送出 -->
          <div class="form-actions">
            <button type="button" class="btn-cancel" @click="handleCancel">取消</button>
            <button type="submit" class="btn-submit" :disabled="submitting">
              {{ submitting ? '更新中...' : (hasRegistrations && hasImportantChanges ? '更新並通知報名者 →' : '儲存變更 →') }}
            </button>
          </div>

        </form>

        <!-- ===== 右側：預覽 + 變更摘要 ===== -->
        <aside class="ua-sidebar">

          <!-- 預覽卡 -->
          <div class="sidebar-block">
            <p class="section-label">PREVIEW</p>
            <div class="preview-card">
              <div class="preview-cover" :style="previewCoverStyle">
                <span v-if="!form.coverImageUrl || imgError" class="preview-cover-placeholder">NO IMAGE</span>
                <span v-if="form.activityType" class="preview-type-badge">{{ getTypeLabel(form.activityType) }}</span>
              </div>
              <div class="preview-body">
                <p class="preview-title">{{ form.title || '活動標題' }}</p>
                <p class="preview-meta">{{ form.location || '— 地點未填 —' }}</p>
                <p class="preview-meta" v-if="form.startTime">{{ formatDT(form.startTime) }}</p>
                <div class="preview-tags">
                  <span v-if="form.targetAudience" class="preview-tag">{{ getAudienceLabel(form.targetAudience) }}</span>
                  <span v-if="form.feeAmount > 0" class="preview-tag fee">NT$ {{ form.feeAmount }}</span>
                  <span v-else-if="form.feeAmount === 0" class="preview-tag free">免費</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 變更摘要（有報名且有修改時顯示） -->
          <div v-if="hasRegistrations && hasImportantChanges" class="sidebar-block changes-block">
            <p class="section-label">CHANGES</p>
            <div v-if="isTimeChanged" class="change-row">
              <span class="change-label">時間</span>
              <div class="change-vals">
                <span class="change-old">{{ formatDT(originalData.startTime) }}</span>
                <span class="change-arrow">→</span>
                <span class="change-new">{{ formatDT(form.startTime) }}</span>
              </div>
            </div>
            <div v-if="isLocationChanged" class="change-row">
              <span class="change-label">地點</span>
              <div class="change-vals">
                <span class="change-old">{{ originalData.location }}</span>
                <span class="change-arrow">→</span>
                <span class="change-new">{{ form.location }}</span>
              </div>
            </div>
          </div>

          <!-- Checklist -->
          <div class="sidebar-block">
            <p class="section-label">CHECKLIST</p>
            <div class="hint-item" :class="{ done: form.title }">
              <span class="hint-dot"></span>活動標題
            </div>
            <div class="hint-item" :class="{ done: form.startTime && form.endTime }">
              <span class="hint-dot"></span>時間設定
            </div>
            <div class="hint-item" :class="{ done: form.location }">
              <span class="hint-dot"></span>活動地點
            </div>
            <div class="hint-item" :class="{ done: form.activityType }">
              <span class="hint-dot"></span>活動類型
            </div>
            <div class="hint-item" :class="{ done: form.targetAudience }">
              <span class="hint-dot"></span>目標對象
            </div>
            <div class="hint-item" :class="{ done: form.registrationDeadline }">
              <span class="hint-dot"></span>報名截止
            </div>
          </div>

        </aside>

      </div>
    </template>

    <!-- Cancel confirm dialog -->
    <Teleport to="body">
      <div v-if="showCancelDialog" class="modal-overlay" @click.self="showCancelDialog = false">
        <div class="dialog-box">
          <div class="dialog-header">
            <h2 class="dialog-title">放棄編輯？</h2>
            <button class="dialog-close" @click="showCancelDialog = false">×</button>
          </div>
          <div class="dialog-body">
            <p class="dialog-msg">所有未儲存的變更將會丟失，確定要離開嗎？</p>
          </div>
          <div class="dialog-footer">
            <button class="modal-btn cancel" @click="showCancelDialog = false">繼續編輯</button>
            <button class="modal-btn confirm" @click="confirmCancel">確定離開</button>
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
import { useUserStore } from '@/stores/user'
import ImageUploader from '@/components/ImageUploader.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activityId = computed(() => route.params.activityId)
const loading = ref(false)
const submitting = ref(false)
const imgError = ref(false)
const showCancelDialog = ref(false)
const hasRegistrations = ref(false)
const registrationCount = ref(0)
const originalData = ref({})

const form = ref({
  title: '',
  description: '',
  coverImageUrl: '',
  startTime: '',
  endTime: '',
  location: '',
  maxParticipants: null,
  registrationDeadline: '',
  feeAmount: 0,
  activityType: '',
  targetAudience: '',
  updatedBy: '',
  earlyBirdEnabled: false,
  earlyBirdDeadline: '',
})

// ===== Validation =====
const touched = ref({})
const touch = (field) => { touched.value[field] = true }

const validate = (field, val) => {
  const f = form.value
  switch (field) {
    case 'title':
      if (!val || !val.trim()) return '活動標題為必填'
      if (val.trim().length > 100) return '活動標題最多 100 字'
      if (/<[^>]*>/.test(val)) return '標題不可包含 HTML 標籤'
      return null
    case 'description':
      if (val && val.length > 5000) return '活動描述最多 5000 字'
      return null
    case 'startTime':
      if (!val) return '開始時間為必填'
      return null
    case 'endTime':
      if (!val) return '結束時間為必填'
      if (f.startTime && new Date(val) <= new Date(f.startTime)) return '結束時間必須晚於開始時間'
      return null
    case 'registrationDeadline':
      if (!val) return '報名截止時間為必填'
      if (f.startTime && new Date(val) >= new Date(f.startTime)) return '報名截止必須早於活動開始時間'
      return null
    case 'location':
      if (!val || !val.trim()) return '活動地點為必填'
      if (val.trim().length > 200) return '地點最多 200 字'
      return null
    case 'activityType':
      if (!val) return '請選擇活動類型'
      return null
    case 'targetAudience':
      if (!val) return '請選擇目標對象'
      return null
    case 'feeAmount':
      if (val === null || val === undefined || val === '') return '費用金額為必填，免費請填 0'
      if (isNaN(val) || val < 0) return '費用不可為負數'
      if (val > 999999) return '費用最多 NT$ 999,999'
      if (!Number.isInteger(Number(val))) return '費用請填整數'
      return null
    case 'maxParticipants':
      if (val === null || val === '' || val === undefined) return null
      if (isNaN(val) || val < 1) return '人數上限至少為 1 人'
      if (val > 9999) return '人數上限最多 9999 人'
      if (!Number.isInteger(Number(val))) return '人數請填整數'
      return null
    case 'earlyBirdDeadline':
      if (!form.value.earlyBirdEnabled) return null
      if (!val) return '請設定早鳥截止時間'
      if (form.value.registrationDeadline && new Date(val) > new Date(form.value.registrationDeadline))
        return '早鳥截止時間必須早於報名截止時間'
      return null
    default:
      return null
  }
}

const errors = computed(() => {
  const e = {}
  const fields = ['title','description','startTime','endTime','registrationDeadline','location','activityType','targetAudience','feeAmount','maxParticipants']
  for (const f of fields) {
    if (touched.value[f]) {
      const err = validate(f, form.value[f])
      if (err) e[f] = err
    }
  }
  return e
})

const validateAll = () => {
  const fields = ['title','description','startTime','endTime','registrationDeadline','location','activityType','targetAudience','feeAmount','maxParticipants','earlyBirdDeadline']
  fields.forEach(f => { touched.value[f] = true })
  for (const f of fields) {
    if (validate(f, form.value[f])) return false
  }
  return true
}

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
  if (!activityId.value) {
    alert('缺少活動 ID')
    router.push({ name: 'activity-list-container' })
    return
  }
  loading.value = true
  try {
    const response = await activityApi.getActivityDetails(activityId.value)
    const earlyBirdDeadlineRaw = response.data.earlyBirdDeadline
    const data = {
      ...response.data,
      startTime: response.data.startTime?.slice(0, 16) || '',
      endTime: response.data.endTime?.slice(0, 16) || '',
      registrationDeadline: response.data.registrationDeadline?.slice(0, 16) || '',
      earlyBirdDeadline: earlyBirdDeadlineRaw ? earlyBirdDeadlineRaw.slice(0, 16) : '',
      earlyBirdEnabled: !!earlyBirdDeadlineRaw,
    }
    form.value = data
    originalData.value = { ...data }
    hasRegistrations.value = (response.data.registrationCount || response.data.hasRegistrations || 0) > 0
    registrationCount.value = response.data.registrationCount || response.data.hasRegistrations || 0
    if (userStore.userId) form.value.updatedBy = userStore.userId
  } catch (e) {
    const s = e.response?.status
    alert(s === 404 ? `找不到活動 (ID: ${activityId.value})` : s === 403 ? '沒有權限訪問此活動' : '載入活動失敗，請稍後再試')
    router.push({ name: 'activity-list-container' })
  } finally {
    loading.value = false
  }
}

// ===== Computed =====
const isTimeChanged = computed(() =>
  form.value.startTime !== originalData.value.startTime ||
  form.value.endTime !== originalData.value.endTime
)
const isLocationChanged = computed(() =>
  form.value.location !== originalData.value.location
)
const hasImportantChanges = computed(() => isTimeChanged.value || isLocationChanged.value)

const previewCoverStyle = computed(() => {
  if (form.value.coverImageUrl && !imgError.value) {
    return { backgroundImage: `url(${form.value.coverImageUrl})`, backgroundSize: 'cover', backgroundPosition: 'center' }
  }
  return {}
})

// ===== Helpers =====
const getTypeLabel = (t) =>
  ({ REGULAR:'社課', SPECIAL:'特別活動', TRAINING:'團練', PERFORMANCE:'演出', COMPETITION:'比賽' })[t] || t
const getAudienceLabel = (a) =>
  ({ ALL:'所有人', MEMBER_ONLY:'社員限定', MANAGER_ONLY:'幹部限定' })[a] || a
const formatDT = (dt) => {
  if (!dt) return '—'
  const d = new Date(dt)
  const days = ['Sun','Mon','Tue','Wed','Thu','Fri','Sat']
  return `${d.getFullYear()}/${String(d.getMonth()+1).padStart(2,'0')}/${String(d.getDate()).padStart(2,'0')} (${days[d.getDay()]}) ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
}

// ===== Submit =====
const handlesubmit = async () => {
  if (!validateAll()) {
    const firstError = document.querySelector('.input-error, .err-msg')
    firstError?.scrollIntoView({ behavior: 'smooth', block: 'center' })
    return
  }
  submitting.value = true
  try {
    const payload = {
      ...form.value,
      earlyBirdDeadline: form.value.earlyBirdEnabled ? form.value.earlyBirdDeadline : null
    }
    await activityApi.updateActivity(activityId.value, payload)
    const msg = hasImportantChanges.value ? '活動已更新，已通知所有報名者。' : '活動已更新。'
    alert(msg)
    router.push({ name: 'activity-list-container' })
  } catch (e) {
    const s = e.response?.status
    alert(s === 400 ? '更新失敗：資料驗證錯誤' : s === 404 ? '更新失敗：找不到此活動' : '更新失敗，請稍後再試')
  } finally {
    submitting.value = false
  }
}

const handleCancel = () => { showCancelDialog.value = true }
const confirmCancel = () => { router.push({ name: 'activity-list-container' }) }
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Bebas+Neue&family=Noto+Sans+TC:wght@300;400;500;700&family=Space+Mono:wght@400;700&display=swap');

/* ===== Base ===== */
.ua-wrap { min-height: 100vh; background: #fff; font-family: 'Noto Sans TC', sans-serif; color: #0a0a0a; }
.mono { font-family: 'Space Mono', monospace; }

/* ===== Navbar ===== */

.navbar-hidden { transform: translateY(-100%); }

.nav-logo { font-family: 'Space Mono', monospace; font-size: 1rem; font-weight: 700; letter-spacing: 0.15em; color: #0a0a0a; text-decoration: none; }
.nav-logo:hover { color: #ff2d6b; }
.nav-crumb { font-family: 'Space Mono', monospace; font-size: 0.65rem; letter-spacing: 0.12em; color: #aaa; }
.nav-crumb-active { color: #0a0a0a; }

/* ===== State ===== */
.state-wrap { display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 80vh; gap: 0.75rem; }
.state-text { font-family: 'Space Mono', monospace; font-size: 0.75rem; letter-spacing: 0.1em; color: #aaa; }
.loading-bars { display: flex; gap: 4px; }
.loading-bars span { display: block; width: 3px; height: 28px; background: #ff2d6b; border-radius: 2px; animation: barPulse 0.8s ease-in-out infinite alternate; }
@keyframes barPulse { from { transform: scaleY(0.3); opacity: 0.3; } to { transform: scaleY(1); opacity: 1; } }

/* ===== Header ===== */
.ua-header {
  max-width: 1300px; margin: 0 auto;
  padding: 6.5rem 2rem 2rem;
  border-bottom: 2px solid #0a0a0a;
  display: flex; justify-content: space-between; align-items: flex-end; flex-wrap: wrap; gap: 1rem;
}
.ua-eyebrow { display: block; font-family: 'Space Mono', monospace; font-size: 0.6rem; letter-spacing: 0.25em; color: #ff2d6b; font-weight: 700; margin-bottom: 0.4rem; }
.ua-title { font-family: 'Bebas Neue', sans-serif; font-size: 4rem; line-height: 1; letter-spacing: 0.02em; margin: 0; }

/* 報名警告 */
.reg-warning {
  display: flex; align-items: center; gap: 0.6rem;
  background: #fff3e0; border: 1px solid #ff9800;
  padding: 0.65rem 1rem; font-size: 0.8rem; color: #e65100;
  align-self: flex-end; margin-bottom: 0.5rem;
}
.warn-dot {
  width: 20px; height: 20px; background: #ff9800; color: #fff;
  border-radius: 50%; display: flex; align-items: center; justify-content: center;
  font-family: 'Space Mono', monospace; font-size: 0.7rem; font-weight: 700; flex-shrink: 0;
}

/* ===== Layout ===== */
.ua-layout {
  max-width: 1300px; margin: 0 auto;
  padding: 2.5rem 2rem 4rem;
  display: grid; grid-template-columns: 1fr 300px; gap: 3rem; align-items: start;
}

/* ===== Form ===== */
.ua-form { display: flex; flex-direction: column; gap: 2.5rem; }
.form-section { display: flex; flex-direction: column; gap: 1.25rem; }

/* ── Early Bird Toggle ── */
.toggle-field { margin-bottom: 0.25rem; }
.toggle-label { display: flex; align-items: center; gap: 0.6rem; cursor: pointer; }
.toggle-checkbox { width: 16px; height: 16px; accent-color: #ff2d6b; cursor: pointer; }
.toggle-text { font-family: 'Space Mono', monospace; font-size: 0.72rem; letter-spacing: 0.06em; color: #0a0a0a; }
.toggle-hint { font-family: 'Space Mono', monospace; font-size: 0.62rem; color: #aaa; letter-spacing: 0.04em; }
.field-hint-text { font-family: 'Space Mono', monospace; font-size: 0.62rem; color: #aaa; margin: 0.25rem 0 0; }
.section-label {
  font-family: 'Space Mono', monospace; font-size: 0.58rem;
  letter-spacing: 0.22em; color: #ff2d6b; font-weight: 700;
  padding-bottom: 0.6rem; border-bottom: 1px solid #f0f0f0; margin-bottom: 0.25rem;
}
.field { display: flex; flex-direction: column; gap: 0.4rem; }
.field-row { display: grid; grid-template-columns: 1fr 1fr; gap: 1.25rem; }
.field-label {
  font-family: 'Space Mono', monospace; font-size: 0.65rem;
  letter-spacing: 0.1em; color: #555; display: flex; align-items: center; gap: 0.4rem;
}
.req { color: #ff2d6b; }

.changed-tag {
  font-size: 0.55rem; letter-spacing: 0.08em; background: #ff9800;
  color: #fff; padding: 0.1rem 0.4rem; font-weight: 700;
}

.field-input, .field-select, .field-textarea {
  width: 100%; padding: 0.7rem 0.9rem;
  border: 1px solid #e0e0e0; border-radius: 0;
  font-family: 'Noto Sans TC', sans-serif; font-size: 0.88rem;
  color: #0a0a0a; background: #fff; outline: none;
  transition: border-color 0.2s, box-shadow 0.2s; box-sizing: border-box;
}
.field-input:focus, .field-select:focus, .field-textarea:focus { border-color: #0a0a0a; }
.field-input.modified { border-color: #ff9800; box-shadow: inset 2px 0 0 #ff9800; }

/* ── Validation ── */
.input-error { border-color: #ff2d6b !important; }
.input-error:focus { border-color: #ff2d6b !important; box-shadow: 0 0 0 2px rgba(255,45,107,0.12); }
.err-msg {
  font-family: 'Space Mono', monospace;
  font-size: 0.62rem; letter-spacing: 0.06em;
  color: #ff2d6b; margin: 0.3rem 0 0;
}
.char-count {
  font-family: 'Space Mono', monospace;
  font-size: 0.58rem; color: #ccc; margin: 0.25rem 0 0; text-align: right;
}
.field-textarea { resize: vertical; min-height: 100px; }
.field-select {
  appearance: none; cursor: pointer;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='8' viewBox='0 0 12 8'%3E%3Cpath d='M1 1l5 5 5-5' stroke='%23999' stroke-width='1.5' fill='none'/%3E%3C/svg%3E");
  background-repeat: no-repeat; background-position: right 0.8rem center; padding-right: 2.2rem;
}
.input-prefix-wrap { position: relative; display: flex; align-items: center; }
.input-prefix { position: absolute; left: 0.9rem; font-family: 'Space Mono', monospace; font-size: 0.72rem; color: #aaa; pointer-events: none; }
.field-input.has-prefix { padding-left: 2.8rem; }

/* 封面預覽 */
.img-preview { margin-top: 0.5rem; border: 1px solid #e0e0e0; overflow: hidden; max-height: 160px; }
.img-preview img { width: 100%; height: 160px; object-fit: cover; display: block; }
.img-error { font-family: 'Space Mono', monospace; font-size: 0.65rem; color: #ff2d6b; padding: 0.5rem; margin: 0; }

/* 通知 banner */
.notify-banner {
  display: flex; align-items: center; gap: 0.6rem;
  background: #e3f2fd; border: 1px solid #1565c0;
  padding: 0.7rem 1rem; font-size: 0.78rem; color: #1565c0;
}
.notify-icon { font-size: 0.9rem; }

/* 送出按鈕 */
.form-actions { display: flex; justify-content: flex-end; gap: 0.75rem; padding-top: 1rem; border-top: 1px solid #e0e0e0; }
.btn-cancel {
  background: transparent; border: 1px solid #e0e0e0; color: #888;
  padding: 0.7rem 1.5rem; font-family: 'Space Mono', monospace;
  font-size: 0.75rem; letter-spacing: 0.08em; cursor: pointer; transition: all 0.2s;
}
.btn-cancel:hover { border-color: #0a0a0a; color: #0a0a0a; }
.btn-submit {
  background: #0a0a0a; color: #fff; border: none;
  padding: 0.75rem 2rem; font-family: 'Space Mono', monospace;
  font-size: 0.78rem; letter-spacing: 0.1em; cursor: pointer; transition: background 0.2s;
}
.btn-submit:hover:not(:disabled) { background: #ff2d6b; }
.btn-submit:disabled { opacity: 0.5; cursor: not-allowed; }

/* ===== Sidebar ===== */
.ua-sidebar { position: sticky; top: 5rem; display: flex; flex-direction: column; gap: 1.5rem; }
.sidebar-block { display: flex; flex-direction: column; gap: 0.5rem; }

/* Preview */
.preview-card { border: 1px solid #e0e0e0; overflow: hidden; }
.preview-cover {
  height: 140px; background: #f5f5f5;
  display: flex; align-items: center; justify-content: center; position: relative;
}
.preview-cover-placeholder { font-family: 'Bebas Neue', sans-serif; font-size: 1.3rem; color: #ccc; letter-spacing: 0.1em; }
.preview-type-badge {
  position: absolute; top: 0.5rem; left: 0.5rem;
  background: rgba(0,0,0,0.65); backdrop-filter: blur(4px);
  color: #fff; font-family: 'Space Mono', monospace;
  font-size: 0.52rem; letter-spacing: 0.1em; padding: 0.18rem 0.45rem;
}
.preview-body { padding: 0.85rem; }
.preview-title { font-weight: 700; font-size: 0.88rem; margin: 0 0 0.3rem; line-height: 1.3; color: #0a0a0a; }
.preview-meta { font-family: 'Space Mono', monospace; font-size: 0.6rem; color: #888; margin: 0 0 0.2rem; }
.preview-tags { display: flex; gap: 0.35rem; flex-wrap: wrap; margin-top: 0.5rem; }
.preview-tag { font-family: 'Space Mono', monospace; font-size: 0.55rem; letter-spacing: 0.08em; border: 1px solid #e0e0e0; padding: 0.12rem 0.4rem; color: #888; }
.preview-tag.fee { border-color: #ff2d6b; color: #ff2d6b; }
.preview-tag.free { border-color: #2e7d32; color: #2e7d32; }

/* Changes */
.changes-block { border: 1px solid #ff9800; padding: 0.85rem; background: #fffde7; }
.change-row { display: flex; flex-direction: column; gap: 0.3rem; padding: 0.5rem 0; border-bottom: 1px solid #ffe0b2; }
.change-row:last-child { border-bottom: none; padding-bottom: 0; }
.change-label { font-family: 'Space Mono', monospace; font-size: 0.6rem; letter-spacing: 0.1em; color: #e65100; font-weight: 700; }
.change-vals { display: flex; align-items: baseline; gap: 0.4rem; flex-wrap: wrap; }
.change-old { font-family: 'Space Mono', monospace; font-size: 0.6rem; color: #aaa; text-decoration: line-through; }
.change-arrow { font-family: 'Space Mono', monospace; font-size: 0.6rem; color: #e65100; }
.change-new { font-family: 'Space Mono', monospace; font-size: 0.6rem; color: #0a0a0a; font-weight: 700; }

/* Checklist */
.hint-item {
  display: flex; align-items: center; gap: 0.6rem;
  font-family: 'Space Mono', monospace; font-size: 0.62rem;
  color: #bbb; transition: color 0.2s;
}
.hint-item.done { color: #0a0a0a; }
.hint-dot { width: 6px; height: 6px; border-radius: 50%; background: #e0e0e0; flex-shrink: 0; transition: background 0.2s; }
.hint-item.done .hint-dot { background: #ff2d6b; }

/* ===== Modal / Dialog ===== */
.modal-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,0.5);
  backdrop-filter: blur(4px); display: flex; align-items: center; justify-content: center; z-index: 200;
}
.dialog-box { background: #fff; width: 400px; max-width: 95vw; }
.dialog-header { display: flex; justify-content: space-between; align-items: center; padding: 1.25rem 1.5rem; border-bottom: 2px solid #0a0a0a; }
.dialog-title { font-family: 'Bebas Neue', sans-serif; font-size: 1.4rem; letter-spacing: 0.05em; margin: 0; }
.dialog-close { background: none; border: none; font-size: 1.3rem; cursor: pointer; color: #aaa; }
.dialog-close:hover { color: #0a0a0a; }
.dialog-body { padding: 1.5rem; }
.dialog-msg { font-size: 0.85rem; color: #555; margin: 0; }
.dialog-footer { display: flex; justify-content: flex-end; gap: 0.6rem; padding: 1rem 1.5rem; border-top: 1px solid #e0e0e0; }
.modal-btn { padding: 0.55rem 1.2rem; font-family: 'Space Mono', monospace; font-size: 0.72rem; letter-spacing: 0.06em; cursor: pointer; border: 1px solid; transition: all 0.15s; }
.modal-btn.cancel { border-color: #e0e0e0; color: #555; background: #fff; }
.modal-btn.cancel:hover { background: #f5f5f5; }
.modal-btn.confirm { border-color: #0a0a0a; color: #fff; background: #0a0a0a; }
.modal-btn.confirm:hover { background: #ff2d6b; border-color: #ff2d6b; }

/* ===== Responsive ===== */
@media (max-width: 900px) {
  .ua-layout { grid-template-columns: 1fr; }
  .ua-sidebar { position: static; }
  .ua-title { font-size: 2.8rem; }
  .ua-header { flex-direction: column; align-items: flex-start; }
}
@media (max-width: 600px) {
  .field-row { grid-template-columns: 1fr; }
}
</style>