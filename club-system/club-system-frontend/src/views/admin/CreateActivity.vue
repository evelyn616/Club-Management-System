<template>
  <div class="ca-wrap">

    <!-- Navbar -->
    <nav class="navbar" :class="{ 'navbar-hidden': navHidden }">
      <div class="nav-container">
        <router-link to="/admin/activity-management-container" class="nav-logo">CLUB SYSTEM</router-link>
        <span class="nav-crumb">ADMIN / <span class="nav-crumb-active">建立活動</span></span>
      </div>
    </nav>

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
            <input v-model="form.title" type="text" class="field-input" required maxlength="100" placeholder="輸入活動名稱" />
          </div>

          <div class="field">
            <label class="field-label">活動描述</label>
            <textarea v-model="form.description" class="field-textarea" rows="4" maxlength="5000" placeholder="輸入活動說明、注意事項等..."></textarea>
          </div>

          <div class="field">
            <label class="field-label">封面圖片 URL</label>
            <input v-model="form.coverImageUrl" type="text" class="field-input" maxlength="500" placeholder="https://example.com/image.jpg" />
            <!-- 預覽 -->
            <div v-if="form.coverImageUrl" class="img-preview">
              <img :src="form.coverImageUrl" alt="封面預覽" @error="imgError = true" @load="imgError = false" />
              <p v-if="imgError" class="img-error">圖片載入失敗，請確認 URL</p>
            </div>
          </div>
        </section>

        <!-- 時間與地點 -->
        <section class="form-section">
          <p class="section-label">TIME & PLACE</p>

          <div class="field-row">
            <div class="field">
              <label class="field-label">開始時間 <span class="req">*</span></label>
              <DateTimePicker v-model="form.startTime" @update:modelValue="onStartTimeChange" />
            </div>
            <div class="field">
              <label class="field-label">
                結束時間 <span class="req">*</span>
                <span v-if="endAutoSet" class="auto-tag">自動 +1hr</span>
              </label>
              <DateTimePicker v-model="form.endTime" :minDateTime="form.startTime" @update:modelValue="endAutoSet = false" />
            </div>
          </div>

          <div class="field-row">
            <div class="field">
              <label class="field-label">報名截止時間 <span class="req">*</span></label>
              <DateTimePicker v-model="form.registrationDeadline" :minDateTime="todayStr" />
            </div>
            <div class="field">
              <label class="field-label">活動地點 <span class="req">*</span></label>
              <input v-model="form.location" type="text" class="field-input" required maxlength="200" placeholder="輸入地點" />
            </div>
          </div>
        </section>

        <!-- 活動設定 -->
        <section class="form-section">
          <p class="section-label">SETTINGS</p>

          <div class="field-row">
            <div class="field">
              <label class="field-label">活動類型 <span class="req">*</span></label>
              <select v-model="form.activityType" class="field-select" required>
                <option value="">請選擇</option>
                <option value="REGULAR">社課</option>
                <option value="SPECIAL">特別活動</option>
                <option value="TRAINING">團練</option>
                <option value="PERFORMANCE">演出</option>
                <option value="COMPETITION">比賽</option>
              </select>
            </div>
            <div class="field">
              <label class="field-label">目標對象 <span class="req">*</span></label>
              <select v-model="form.targetAudience" class="field-select" required>
                <option value="">請選擇</option>
                <option value="ALL">所有人</option>
                <option value="MEMBER_ONLY">社員限定</option>
                <option value="MANAGER_ONLY">幹部限定</option>
              </select>
            </div>
          </div>

          <div class="field-row">
            <div class="field">
              <label class="field-label">費用金額 <span class="req">*</span></label>
              <div class="input-prefix-wrap">
                <span class="input-prefix">NT$</span>
                <input v-model.number="form.feeAmount" type="number" class="field-input has-prefix" min="0" required placeholder="0" />
              </div>
            </div>
            <div class="field">
              <label class="field-label">最大參與人數</label>
              <input v-model.number="form.maxParticipants" type="number" class="field-input" min="1" placeholder="不填則無上限" />
            </div>
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
})

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
  submitting.value = true
  try {
    const response = await activityApi.createActivity(form.value)
    const newActivityId = response.data.id
    router.push({ name: 'publish-activity-container', params: { activityId: newActivityId } })
  } catch (error) {
    console.error('建立活動失敗:', error)
    console.error('後端錯誤詳情:', error.response?.data)  // ← 加這行
    alert('建立活動失敗，請稍後再試')
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
.navbar {
  position: fixed; top: 0; left: 0; right: 0; z-index: 100;
  padding: 1rem 2rem;
  background: rgba(255,255,255,0.9);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(0,0,0,0.08);
  display: flex; align-items: center;
  transform: translateY(0); transition: transform 0.35s ease;
}
.navbar-hidden { transform: translateY(-100%); }
.nav-container { max-width: 1300px; margin: 0 auto; width: 100%; display: flex; justify-content: space-between; align-items: center; }
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
.field-textarea { resize: vertical; min-height: 100px; }
.field-select { appearance: none; cursor: pointer; background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='8' viewBox='0 0 12 8'%3E%3Cpath d='M1 1l5 5 5-5' stroke='%23999' stroke-width='1.5' fill='none'/%3E%3C/svg%3E"); background-repeat: no-repeat; background-position: right 0.8rem center; padding-right: 2.2rem; }

.input-prefix-wrap { position: relative; display: flex; align-items: center; }
.input-prefix { position: absolute; left: 0.9rem; font-family: 'Space Mono', monospace; font-size: 0.72rem; color: #aaa; pointer-events: none; }
.field-input.has-prefix { padding-left: 2.8rem; }

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