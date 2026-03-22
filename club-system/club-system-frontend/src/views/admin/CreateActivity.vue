<template>
  <div class="ca-wrap">


    <!-- Header -->
    <div class="ca-header">
      <span class="ca-eyebrow">NEW ACTIVITY</span>
      <h1 class="ca-title">建立活動</h1>
    </div>

    <div class="ca-layout">

      <!-- ===== 表單 ===== -->
      <form class="ca-form" @submit.prevent="handlesubmit">

        <!-- 基本資訊 -->
        <section class="form-section">
          <p class="section-label">BASIC INFO</p>

          <div class="field">
            <label class="field-label">活動標題 <span class="req">*</span></label>
            <input
              v-model="form.title"
              type="text"
              class="field-input"
              :class="{ 'input-error': errors.title }"
              maxlength="100"
              placeholder="輸入活動名稱"
              @blur="touch('title')"
            />
            <p class="err-msg" v-if="errors.title">{{ errors.title }}</p>
            <p class="char-count" v-else>{{ form.title.length }} / 100</p>
          </div>

          <div class="field">
            <label class="field-label">活動描述</label>
            <textarea
              v-model="form.description"
              class="field-textarea"
              :class="{ 'input-error': errors.description }"
              rows="4"
              maxlength="5000"
              placeholder="輸入活動說明、注意事項等..."
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
              <label class="field-label">開始時間 <span class="req">*</span></label>
              <DateTimePicker
                v-model="form.startTime"
                @update:modelValue="val => { onStartTimeChange(val); touch('startTime') }"
              />
              <p class="err-msg" v-if="errors.startTime">{{ errors.startTime }}</p>
            </div>
            <div class="field">
              <label class="field-label">
                結束時間 <span class="req">*</span>
                <span v-if="endAutoSet" class="auto-tag">自動 +1hr</span>
              </label>
              <DateTimePicker
                v-model="form.endTime"
                :minDateTime="form.startTime"
                @update:modelValue="val => { endAutoSet = false; touch('endTime') }"
              />
              <p class="err-msg" v-if="errors.endTime">{{ errors.endTime }}</p>
            </div>
          </div>

          <div class="field-row">
            <div class="field">
              <label class="field-label">報名截止時間 <span class="req">*</span></label>
              <DateTimePicker
                v-model="form.registrationDeadline"
                :minDateTime="todayStr"
                @update:modelValue="() => touch('registrationDeadline')"
              />
              <p class="err-msg" v-if="errors.registrationDeadline">{{ errors.registrationDeadline }}</p>
            </div>
            <div class="field">
              <label class="field-label">活動地點 <span class="req">*</span></label>
              <input
                v-model="form.location"
                type="text"
                class="field-input"
                :class="{ 'input-error': errors.location }"
                maxlength="200"
                placeholder="輸入地點"
                @blur="touch('location')"
              />
              <p class="err-msg" v-if="errors.location">{{ errors.location }}</p>
            </div>
          </div>
        </section>

        <!-- 活動設定 -->
        <section class="form-section">
          <p class="section-label">SETTINGS</p>

          <div class="field-row">
            <div class="field">
              <label class="field-label">活動類型 <span class="req">*</span></label>
              <select
                v-model="form.activityType"
                class="field-select"
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
                v-model="form.targetAudience"
                class="field-select"
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
                  v-model.number="form.feeAmount"
                  type="number"
                  class="field-input has-prefix"
                  :class="{ 'input-error': errors.feeAmount }"
                  min="0"
                  max="999999"
                  placeholder="0"
                  @blur="touch('feeAmount')"
                />
              </div>
              <p class="err-msg" v-if="errors.feeAmount">{{ errors.feeAmount }}</p>
            </div>
            <div class="field">
              <label class="field-label">最大參與人數</label>
              <input
                v-model.number="form.maxParticipants"
                type="number"
                class="field-input"
                :class="{ 'input-error': errors.maxParticipants }"
                min="1"
                max="9999"
                placeholder="不填則無上限"
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
            <DateTimePicker
              v-model="form.earlyBirdDeadline"
              :minDateTime="todayStr"
              @update:modelValue="() => touch('earlyBirdDeadline')"
            />
            <p class="err-msg" v-if="errors.earlyBirdDeadline">{{ errors.earlyBirdDeadline }}</p>
            <p class="field-hint-text" v-else>此時間之前報名可享早鳥優惠（折扣率請至「折扣設定」調整）</p>
          </div>
        </section>

        <!-- 送出 -->
        <div class="form-actions">
          <button type="button" class="btn-cancel" @click="handleCancel">取消</button>
          <button type="submit" class="btn-submit" :disabled="submitting">
            {{ submitting ? '建立中...' : '建立活動 →' }}
          </button>
        </div>

      </form>

      <!-- ===== 右側預覽 ===== -->
      <aside class="ca-preview">
        <p class="section-label">PREVIEW</p>
        <div class="preview-card">
          <div class="preview-cover" :style="previewCoverStyle">
            <span v-if="!form.coverImageUrl" class="preview-cover-placeholder">NO IMAGE</span>
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

        <div class="preview-hints">
          <p class="hint-label">CHECKLIST</p>
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
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { activityApi } from '@/api/activity'
import { useUserStore } from '@/stores/user'
import DateTimePicker from '@/components/DateTimePicker.vue'
import ImageUploader from '@/components/ImageUploader.vue'

const router = useRouter()
const userStore = useUserStore()
const submitting = ref(false)
const imgError = ref(false)
const endAutoSet = ref(false)
const todayStr = new Date().toISOString().slice(0, 16)

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
  createdBy: '',
  earlyBirdEnabled: false,
  earlyBirdDeadline: '',
})

// ===== Validation =====
const touched = ref({})  // 記錄哪些欄位被 blur 過

const touch = (field) => {
  touched.value[field] = true
}

// 單一欄位驗證規則
const validate = (field, val) => {
  const f = form.value
  switch (field) {
    case 'title':
      if (!val || !val.trim()) return '活動標題為必填'
      if (val.trim().length > 100) return '活動標題最多 100 字'
      // 防止 XSS：不允許 HTML 標籤
      if (/<[^>]*>/.test(val)) return '標題不可包含 HTML 標籤'
      return null

    case 'description':
      if (val && val.length > 5000) return '活動描述最多 5000 字'
      return null

    case 'startTime':
      if (!val) return '開始時間為必填'
      if (new Date(val) < new Date()) return '開始時間必須是未來的時間'
      return null

    case 'endTime':
      if (!val) return '結束時間為必填'
      if (f.startTime && new Date(val) <= new Date(f.startTime)) return '結束時間必須晚於開始時間'
      return null

    case 'registrationDeadline':
      if (!val) return '報名截止時間為必填'
      if (new Date(val) < new Date()) return '報名截止時間必須是未來的時間'
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
      if (val === null || val === '' || val === undefined) return null  // 選填
      if (isNaN(val) || val < 1) return '人數上限至少為 1 人'
      if (val > 9999) return '人數上限最多 9999 人'
      if (!Number.isInteger(Number(val))) return '人數請填整數'
      return null

    case 'earlyBirdDeadline':
      if (!form.value.earlyBirdEnabled) return null  // 未啟用不驗
      if (!val) return '請設定早鳥截止時間'
      if (form.value.registrationDeadline && new Date(val) > new Date(form.value.registrationDeadline))
        return '早鳥截止時間必須早於報名截止時間'
      return null

    default:
      return null
  }
}

// 所有欄位的即時錯誤（只顯示已 touched 的欄位）
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

// submit 前 touch 全部欄位，回傳是否全部通過
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
  if (userStore.userId) form.value.createdBy = userStore.userId
})
onUnmounted(() => window.removeEventListener('scroll', onScroll))

// ===== Auto end time =====
const onStartTimeChange = (val) => {
  if (!val) return
  if (!form.value.endTime || endAutoSet.value) {
    const d = new Date(val)
    d.setHours(d.getHours() + 1)
    form.value.endTime = d.toISOString().slice(0, 16)
    endAutoSet.value = true
  }
}

// ===== Helpers =====
const getTypeLabel = (type) =>
  ({ REGULAR:'社課', SPECIAL:'特別活動', TRAINING:'團練', PERFORMANCE:'演出', COMPETITION:'比賽' })[type] || type
const getAudienceLabel = (a) =>
  ({ ALL:'所有人', MEMBER_ONLY:'社員限定', MANAGER_ONLY:'幹部限定' })[a] || a
const formatDT = (dt) => {
  if (!dt) return ''
  const d = new Date(dt)
  const days = ['Sun','Mon','Tue','Wed','Thu','Fri','Sat']
  return `${d.getFullYear()}/${String(d.getMonth()+1).padStart(2,'0')}/${String(d.getDate()).padStart(2,'0')} (${days[d.getDay()]}) ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
}

const previewCoverStyle = computed(() => {
  if (form.value.coverImageUrl && !imgError.value) {
    return { backgroundImage: `url(${form.value.coverImageUrl})`, backgroundSize: 'cover', backgroundPosition: 'center' }
  }
  return {}
})

// ===== Submit =====
const handlesubmit = async () => {
  if (!validateAll()) {
    // 捲到第一個錯誤欄位
    const firstError = document.querySelector('.input-error, .err-msg')
    firstError?.scrollIntoView({ behavior: 'smooth', block: 'center' })
    return
  }

  submitting.value = true
  try {
    // 未啟用早鳥時，送出 null
    const payload = {
      ...form.value,
      earlyBirdDeadline: form.value.earlyBirdEnabled ? form.value.earlyBirdDeadline : null
    }
    const response = await activityApi.createActivity(payload)
    const newActivityId = response.data.id
    router.push({ name: 'publish-activity-container', params: { activityId: newActivityId } })
  } catch (error) {
    console.error('建立活動失敗:', error)
    const serverErrors = error.response?.data?.errors
    if (serverErrors) {
      // 把後端錯誤也塞進 touched + 顯示
      Object.keys(serverErrors).forEach(f => { touched.value[f] = true })
      alert('部分欄位有誤，請檢查標紅的欄位')
    } else {
      alert(error.response?.data?.message || '建立活動失敗，請稍後再試')
    }
  } finally {
    submitting.value = false
  }
}

const handleCancel = () => window.history.back()
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Bebas+Neue&family=Noto+Sans+TC:wght@300;400;500;700&family=Space+Mono:wght@400;700&display=swap');

/* ===== Base ===== */
.ca-wrap {
  min-height: 100vh;
  background: #fff;
  font-family: 'Noto Sans TC', sans-serif;
  color: #0a0a0a;
}

/* ===== Navbar ===== */

.navbar-hidden { transform: translateY(-100%); }

.nav-logo { font-family: 'Space Mono', monospace; font-size: 1rem; font-weight: 700; letter-spacing: 0.15em; color: #0a0a0a; text-decoration: none; }
.nav-logo:hover { color: #ff2d6b; }
.nav-crumb { font-family: 'Space Mono', monospace; font-size: 0.65rem; letter-spacing: 0.12em; color: #aaa; }
.nav-crumb-active { color: #0a0a0a; }

/* ===== Header ===== */
.ca-header {
  max-width: 1300px; margin: 0 auto;
  padding: 6.5rem 2rem 2rem;
  border-bottom: 2px solid #0a0a0a;
}
.ca-eyebrow { display: block; font-family: 'Space Mono', monospace; font-size: 0.6rem; letter-spacing: 0.25em; color: #ff2d6b; font-weight: 700; margin-bottom: 0.5rem; }
.ca-title { font-family: 'Bebas Neue', sans-serif; font-size: 4rem; line-height: 1; letter-spacing: 0.02em; margin: 0; }

/* ===== Layout ===== */
.ca-layout {
  max-width: 1300px; margin: 0 auto;
  padding: 2.5rem 2rem 4rem;
  display: grid;
  grid-template-columns: 1fr 340px;
  gap: 3rem;
  align-items: start;
}

/* ===== Form ===== */
.ca-form { display: flex; flex-direction: column; gap: 2.5rem; }

.form-section { display: flex; flex-direction: column; gap: 1.25rem; }

.section-label {
  font-family: 'Space Mono', monospace; font-size: 0.58rem;
  letter-spacing: 0.22em; color: #ff2d6b; font-weight: 700;
  padding-bottom: 0.6rem; border-bottom: 1px solid #f0f0f0;
  margin-bottom: 0.25rem;
}

.field { display: flex; flex-direction: column; gap: 0.4rem; }
.field-row { display: grid; grid-template-columns: 1fr 1fr; gap: 1.25rem; }

.field-label {
  font-family: 'Space Mono', monospace; font-size: 0.65rem;
  letter-spacing: 0.1em; color: #555;
}
.req { color: #ff2d6b; }
.auto-tag {
  font-size: 0.52rem; letter-spacing: 0.06em; background: #0a0a0a;
  color: #fff; padding: 0.1rem 0.4rem; font-weight: 700; margin-left: 0.2rem;
}

.field-input, .field-select, .field-textarea {
  width: 100%; padding: 0.7rem 0.9rem;
  border: 1px solid #e0e0e0; border-radius: 0;
  font-family: 'Noto Sans TC', sans-serif; font-size: 0.88rem;
  color: #0a0a0a; background: #fff; outline: none;
  transition: border-color 0.2s; box-sizing: border-box;
}
.field-input:focus, .field-select:focus, .field-textarea:focus { border-color: #0a0a0a; }

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
.field-select { appearance: none; cursor: pointer; background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='8' viewBox='0 0 12 8'%3E%3Cpath d='M1 1l5 5 5-5' stroke='%23999' stroke-width='1.5' fill='none'/%3E%3C/svg%3E"); background-repeat: no-repeat; background-position: right 0.8rem center; padding-right: 2.2rem; }

.input-prefix-wrap { position: relative; display: flex; align-items: center; }
.input-prefix { position: absolute; left: 0.9rem; font-family: 'Space Mono', monospace; font-size: 0.72rem; color: #aaa; pointer-events: none; }
.field-input.has-prefix { padding-left: 2.8rem; }

/* ── Early Bird Toggle ── */
.toggle-field { margin-bottom: 0.25rem; }
.toggle-label {
  display: flex; align-items: center; gap: 0.6rem; cursor: pointer;
}
.toggle-checkbox {
  width: 16px; height: 16px; accent-color: #ff2d6b; cursor: pointer;
}
.toggle-text {
  font-family: 'Space Mono', monospace; font-size: 0.72rem;
  letter-spacing: 0.06em; color: #0a0a0a;
}
.toggle-hint {
  font-family: 'Space Mono', monospace; font-size: 0.62rem;
  color: #aaa; letter-spacing: 0.04em;
}
.field-hint-text {
  font-family: 'Space Mono', monospace; font-size: 0.62rem;
  color: #aaa; margin: 0.25rem 0 0;
}

/* 圖片預覽 */
.img-preview { margin-top: 0.5rem; border: 1px solid #e0e0e0; overflow: hidden; max-height: 180px; }
.img-preview img { width: 100%; height: 180px; object-fit: cover; display: block; }
.img-error { font-family: 'Space Mono', monospace; font-size: 0.65rem; color: #ff2d6b; padding: 0.5rem; margin: 0; }

/* 表單底部按鈕 */
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

/* ===== Preview Sidebar ===== */
.ca-preview { position: sticky; top: 5rem; display: flex; flex-direction: column; gap: 1.5rem; }

.preview-card { border: 1px solid #e0e0e0; overflow: hidden; }
.preview-cover {
  height: 160px; background: #f5f5f5;
  display: flex; align-items: center; justify-content: center;
  position: relative;
}
.preview-cover-placeholder { font-family: 'Bebas Neue', sans-serif; font-size: 1.5rem; color: #ccc; letter-spacing: 0.1em; }
.preview-type-badge {
  position: absolute; top: 0.6rem; left: 0.6rem;
  background: rgba(0,0,0,0.65); backdrop-filter: blur(4px);
  color: #fff; font-family: 'Space Mono', monospace;
  font-size: 0.55rem; letter-spacing: 0.1em; padding: 0.2rem 0.5rem;
}
.preview-body { padding: 1rem; }
.preview-title { font-weight: 700; font-size: 0.95rem; margin: 0 0 0.4rem; line-height: 1.3; color: #0a0a0a; }
.preview-meta { font-family: 'Space Mono', monospace; font-size: 0.65rem; color: #888; margin: 0 0 0.25rem; }
.preview-tags { display: flex; gap: 0.4rem; flex-wrap: wrap; margin-top: 0.6rem; }
.preview-tag { font-family: 'Space Mono', monospace; font-size: 0.58rem; letter-spacing: 0.08em; border: 1px solid #e0e0e0; padding: 0.15rem 0.5rem; color: #888; }
.preview-tag.fee { border-color: #ff2d6b; color: #ff2d6b; }
.preview-tag.free { border-color: #2e7d32; color: #2e7d32; }

/* Checklist */
.preview-hints { display: flex; flex-direction: column; gap: 0.5rem; }
.hint-label { font-family: 'Space Mono', monospace; font-size: 0.58rem; letter-spacing: 0.2em; color: #ff2d6b; font-weight: 700; margin-bottom: 0.25rem; }
.hint-item {
  display: flex; align-items: center; gap: 0.6rem;
  font-family: 'Space Mono', monospace; font-size: 0.65rem;
  color: #bbb; transition: color 0.2s;
}
.hint-item.done { color: #0a0a0a; }
.hint-dot {
  width: 6px; height: 6px; border-radius: 50%;
  background: #e0e0e0; flex-shrink: 0; transition: background 0.2s;
}
.hint-item.done .hint-dot { background: #ff2d6b; }

/* ===== Responsive ===== */
@media (max-width: 900px) {
  .ca-layout { grid-template-columns: 1fr; }
  .ca-preview { position: static; }
  .ca-title { font-size: 2.8rem; }
}
@media (max-width: 600px) {
  .field-row { grid-template-columns: 1fr; }
}
</style>