<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  createFeedbackForm, getFormByActivity,
  updateFeedbackForm, toggleFeedbackForm,
  addQuestion, updateQuestion, deleteQuestion
} from '@/api/feedback'

const route = useRoute()
const router = useRouter()
const activityId = computed(() => Number(route.params.activityId))

const form = ref(null)
const loading = ref(true)
const saving = ref(false)
const error = ref('')

// 建立表單用
const createTitle = ref('')
const createDesc = ref('')
const createPoints = ref(5)
const creating = ref(false)

// 編輯題目
const editingQuestion = ref(null) // { id?, questionText, questionType, isRequired, maxRating, options: [] }
const showQuestionModal = ref(false)
const questionSaving = ref(false)

const QUESTION_TYPES = [
  { value: 'SINGLE_CHOICE', label: '單選題' },
  { value: 'MULTIPLE_CHOICE', label: '多選題' },
  { value: 'RATING', label: '評分題' },
  { value: 'TEXT', label: '文字題' },
]

onMounted(async () => {
  await loadForm()
})

async function loadForm() {
  loading.value = true
  error.value = ''
  try {
    form.value = await getFormByActivity(activityId.value)
  } catch (e) {
    if (e?.response?.status === 404) {
      form.value = null
    } else {
      error.value = '載入失敗'
    }
  } finally {
    loading.value = false
  }
}

async function handleCreate() {
  if (!createTitle.value.trim()) { error.value = '請輸入表單標題'; return }
  creating.value = true
  try {
    form.value = await createFeedbackForm({
      activityId: activityId.value,
      title: createTitle.value.trim(),
      description: createDesc.value.trim() || null,
      pointsReward: createPoints.value
    })
  } catch (e) {
    error.value = e?.response?.data?.error || '建立失敗'
  } finally {
    creating.value = false
  }
}

async function handleToggle() {
  if (!form.value) return
  try {
    form.value = await toggleFeedbackForm(form.value.id)
  } catch (e) {
    error.value = '操作失敗'
  }
}

async function handleUpdateForm() {
  if (!form.value) return
  saving.value = true
  try {
    form.value = await updateFeedbackForm(form.value.id, {
      title: form.value.title,
      description: form.value.description,
      pointsReward: form.value.pointsReward
    })
  } catch (e) {
    error.value = '儲存失敗'
  } finally {
    saving.value = false
  }
}

// ── 題目編輯 ──────────────────────────────────────────────────────────

function openAddQuestion() {
  editingQuestion.value = {
    id: null,
    questionText: '',
    questionType: 'SINGLE_CHOICE',
    isRequired: false,
    maxRating: 10,
    options: ['', '']
  }
  showQuestionModal.value = true
}

function openEditQuestion(q) {
  editingQuestion.value = {
    id: q.id,
    questionText: q.questionText,
    questionType: q.questionType,
    isRequired: q.isRequired,
    maxRating: q.maxRating || 10,
    options: q.options ? q.options.map(o => o.optionText) : ['', '']
  }
  showQuestionModal.value = true
}

function addOption() {
  editingQuestion.value.options.push('')
}

function removeOption(idx) {
  editingQuestion.value.options.splice(idx, 1)
}

async function saveQuestion() {
  const eq = editingQuestion.value
  if (!eq.questionText.trim()) { error.value = '請輸入題目內容'; return }
  questionSaving.value = true
  try {
    const payload = {
      questionText: eq.questionText.trim(),
      questionType: eq.questionType,
      isRequired: eq.isRequired,
      maxRating: eq.maxRating,
      options: (eq.questionType === 'SINGLE_CHOICE' || eq.questionType === 'MULTIPLE_CHOICE')
        ? eq.options.filter(o => o.trim())
        : undefined
    }
    if (eq.id) {
      form.value = await updateQuestion(form.value.id, eq.id, payload)
    } else {
      form.value = await addQuestion(form.value.id, payload)
    }
    showQuestionModal.value = false
  } catch (e) {
    error.value = e?.response?.data?.error || '儲存失敗'
  } finally {
    questionSaving.value = false
  }
}

async function handleDeleteQuestion(questionId) {
  if (!confirm('確定刪除此題目？')) return
  try {
    form.value = await deleteQuestion(form.value.id, questionId)
  } catch (e) {
    error.value = '刪除失敗'
  }
}

const typeLabel = (type) => QUESTION_TYPES.find(t => t.value === type)?.label || type

const copied = ref(false)
const shareUrl = computed(() =>
  form.value ? `${window.location.origin}/feedback/activity/${activityId.value}` : ''
)
async function copyShareUrl() {
  try {
    await navigator.clipboard.writeText(shareUrl.value)
    copied.value = true
    setTimeout(() => { copied.value = false }, 2000)
  } catch {
    // fallback
    const el = document.createElement('input')
    el.value = shareUrl.value
    document.body.appendChild(el)
    el.select()
    document.execCommand('copy')
    document.body.removeChild(el)
    copied.value = true
    setTimeout(() => { copied.value = false }, 2000)
  }
}
</script>

<template>
  <div class="builder-page">
    <div class="builder-header">
      <button class="back-btn" @click="router.back()">← 返回</button>
      <h2>活動回饋表單</h2>
      <button
        v-if="form"
        class="results-btn"
        @click="router.push({ name: 'feedback-results', params: { activityId } })"
      >查看結果 →</button>
    </div>

    <div v-if="loading" class="loading">載入中...</div>
    <p v-if="error" class="err-msg">{{ error }}</p>

    <!-- 尚無表單 → 建立 -->
    <div v-if="!loading && !form" class="create-wrap">
      <!-- 左：說明欄 -->
      <div class="create-intro">
        <h3>建立回饋表單</h3>
        <p>為這場活動建立專屬問卷，收集成員的回饋與建議。</p>
        <ul class="feature-list">
          <li>✦ 支援單選、多選、評分、文字四種題型</li>
          <li>✦ 手動開放／關閉填寫</li>
          <li>✦ 登入會員填寫可獲得積分獎勵</li>
          <li>✦ 查看統計摘要與文字回覆</li>
        </ul>
      </div>

      <!-- 右：建立表單 -->
      <div class="create-form-card">
        <div class="cfc-title">表單基本設定</div>
        <div class="field-row">
          <label>表單標題 <span class="req-mark">*</span></label>
          <input v-model="createTitle" type="text" placeholder="例：活動滿意度調查" class="inp" />
        </div>
        <div class="field-row">
          <label>說明（選填）</label>
          <textarea v-model="createDesc" placeholder="向填寫者說明這份問卷的目的..." class="inp ta" rows="3" />
        </div>
        <div class="field-row">
          <label>填寫積分獎勵</label>
          <div class="inp-row">
            <input v-model.number="createPoints" type="number" min="0" max="100" class="inp sm" />
            <span class="unit">點（0 = 不給積分）</span>
          </div>
        </div>
        <button class="btn-primary full" @click="handleCreate" :disabled="creating">
          {{ creating ? '建立中...' : '✦ 建立表單' }}
        </button>
      </div>
    </div>

    <!-- 已有表單 → 編輯 -->
    <div v-if="!loading && form" class="form-editor">

      <!-- 表單設定 -->
      <div class="settings-card">
        <div class="settings-row">
          <div class="field-row inline">
            <label>標題</label>
            <input v-model="form.title" type="text" class="inp" />
          </div>
          <div class="field-row inline">
            <label>積分獎勵</label>
            <input v-model.number="form.pointsReward" type="number" min="0" class="inp sm" />
            <span class="unit">點</span>
          </div>
          <button class="btn-secondary sm" @click="handleUpdateForm" :disabled="saving">
            {{ saving ? '儲存中...' : '儲存設定' }}
          </button>
        </div>
        <div class="field-row mt">
          <label>說明</label>
          <textarea v-model="form.description" class="inp ta" rows="2" />
        </div>

        <!-- 開關 -->
        <div class="toggle-row">
          <span class="toggle-label">
            <span class="status-dot" :class="form.isOpen ? 'open' : 'closed'"></span>
            {{ form.isOpen ? '表單開放中' : '表單已關閉' }}
          </span>
          <span class="resp-count">已收到 {{ form.responseCount || 0 }} 份回覆</span>
          <button class="toggle-btn" :class="form.isOpen ? 'btn-danger' : 'btn-success'" @click="handleToggle">
            {{ form.isOpen ? '關閉表單' : '開放填寫' }}
          </button>
        </div>

        <!-- 分享連結 -->
        <div v-if="form.isOpen" class="share-row">
          <span class="share-label">📎 分享連結</span>
          <div class="share-link-wrap">
            <input class="share-link-input" readonly :value="shareUrl" />
            <button class="share-copy-btn" @click="copyShareUrl">{{ copied ? '已複製 ✓' : '複製' }}</button>
          </div>
        </div>
      </div>

      <!-- 題目列表 -->
      <div class="questions-section">
        <div class="section-header">
          <h3>題目列表</h3>
          <button class="btn-primary sm" @click="openAddQuestion">＋ 新增題目</button>
        </div>

        <div v-if="!form.questions || form.questions.length === 0" class="empty-questions">
          尚無題目，點擊「新增題目」開始建立
        </div>

        <div v-for="(q, idx) in form.questions" :key="q.id" class="question-card">
          <div class="q-left">
            <span class="q-num">Q{{ idx + 1 }}</span>
            <div class="q-info">
              <div class="q-text">{{ q.questionText }}</div>
              <div class="q-meta">
                <span class="tag">{{ typeLabel(q.questionType) }}</span>
                <span v-if="q.isRequired" class="tag req">必填</span>
                <span v-if="q.questionType === 'RATING'" class="tag">最高 {{ q.maxRating }} 分</span>
              </div>
              <div v-if="q.options && q.options.length" class="q-options">
                <span v-for="o in q.options" :key="o.id" class="opt-chip">{{ o.optionText }}</span>
              </div>
            </div>
          </div>
          <div class="q-actions">
            <button class="icon-btn" @click="openEditQuestion(q)">✏️</button>
            <button class="icon-btn del" @click="handleDeleteQuestion(q.id)">🗑️</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 題目編輯 Modal -->
    <Teleport to="body">
      <div v-if="showQuestionModal" class="modal-overlay" @click.self="showQuestionModal = false">
        <div class="q-modal">
          <h3>{{ editingQuestion?.id ? '編輯題目' : '新增題目' }}</h3>

          <div class="field-row">
            <label>題目內容 <span class="req-mark">*</span></label>
            <textarea v-model="editingQuestion.questionText" class="inp ta" rows="2" placeholder="請輸入題目..." />
          </div>

          <div class="field-row">
            <label>題型</label>
            <select v-model="editingQuestion.questionType" class="inp sel"
                    :disabled="!!editingQuestion.id">
              <option v-for="t in QUESTION_TYPES" :key="t.value" :value="t.value">{{ t.label }}</option>
            </select>
            <span v-if="editingQuestion.id" class="hint">題型建立後不可更改</span>
          </div>

          <div class="field-row" v-if="editingQuestion.questionType === 'RATING'">
            <label>最高分數</label>
            <input v-model.number="editingQuestion.maxRating" type="number" min="2" max="20" class="inp sm" />
          </div>

          <div class="field-row">
            <label class="checkbox-label">
              <input type="checkbox" v-model="editingQuestion.isRequired" />
              必填
            </label>
          </div>

          <!-- 選項（單選/多選）-->
          <div v-if="editingQuestion.questionType === 'SINGLE_CHOICE' || editingQuestion.questionType === 'MULTIPLE_CHOICE'"
               class="options-editor">
            <label>選項</label>
            <div v-for="(opt, i) in editingQuestion.options" :key="i" class="opt-row">
              <input v-model="editingQuestion.options[i]" type="text" class="inp opt-inp" :placeholder="`選項 ${i + 1}`" />
              <button class="icon-btn del sm" @click="removeOption(i)" :disabled="editingQuestion.options.length <= 1">✕</button>
            </div>
            <button class="add-opt-btn" @click="addOption">＋ 新增選項</button>
          </div>

          <div class="modal-actions">
            <button class="btn-cancel" @click="showQuestionModal = false">取消</button>
            <button class="btn-primary" @click="saveQuestion" :disabled="questionSaving">
              {{ questionSaving ? '儲存中...' : '儲存' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
.builder-page { max-width: 760px; margin: 0 auto; padding: 1.5rem 1rem; min-height: 100vh; background: #ffffff; }
.builder-header { display: flex; align-items: center; gap: 1rem; margin-bottom: 1.5rem; }
.builder-header h2 { flex: 1; margin: 0; font-size: 1.3rem; color: #1a1a1a; }
.back-btn { background: transparent; border: 1px solid rgba(0,0,0,0.18); color: rgba(0,0,0,0.55); padding: 0.4rem 0.8rem; border-radius: 6px; cursor: pointer; font-size: 0.85rem; transition: all 0.2s; }
.back-btn:hover { border-color: #ff2d6b; color: #ff2d6b; }
.results-btn { background: rgba(0,0,0,0.04); border: 1px solid rgba(0,0,0,0.15); color: rgba(0,0,0,0.6); padding: 0.4rem 0.9rem; border-radius: 6px; cursor: pointer; font-size: 0.85rem; transition: all 0.2s; }
.results-btn:hover { background: rgba(255,45,107,0.06); border-color: #ff2d6b; color: #ff2d6b; }

.loading { color: rgba(0,0,0,0.4); padding: 2rem; text-align: center; }
.err-msg { color: #c0392b; font-size: 0.85rem; margin-bottom: 0.75rem; padding: 0.5rem 0.75rem; background: rgba(255,60,60,0.06); border-radius: 5px; border: 1px solid rgba(255,60,60,0.2); }

/* Create layout */
@keyframes fadeSlideUp {
  from { opacity: 0; transform: translateY(18px); }
  to   { opacity: 1; transform: translateY(0); }
}
@keyframes fadeIn {
  from { opacity: 0; }
  to   { opacity: 1; }
}

.create-wrap {
  display: grid; grid-template-columns: 1fr 1fr; gap: 1.5rem; align-items: start;
  background-color: rgba(255,255,255,0.6);
  border: 1px solid #ff2d6b; border-radius: 1rem;
  transition: background-color 0.4s ease, box-shadow 0.4s ease;
  animation: fadeSlideUp 0.5s ease both;
}
.create-wrap:hover {
  background-color: rgba(210,210,210,0.4);
  box-shadow: 0 8px 32px rgba(255,45,107,0.1);
}
@media (max-width: 640px) { .create-wrap { grid-template-columns: 1fr; } }

.create-intro { padding: 1.5rem; animation: fadeIn 0.6s ease 0.1s both; }
.create-intro h3 { color: #393939; font-size: 1.8rem; margin: 0 0 0.8rem; font-weight: 500; letter-spacing: 0.3rem; }
.create-intro p { color: rgba(44,44,44,0.5); font-size: 0.88rem; line-height: 1.6; margin: 3rem 0 1.25rem; }
.feature-list { list-style: none; padding: 0; margin: 0; display: flex; flex-direction: column; gap: 0.55rem; }
.feature-list li {
  font-size: 0.83rem; color: rgba(0,0,0,0.55);
  display: flex; align-items: flex-start; gap: 0.4rem;
  opacity: 0;
  animation: fadeSlideUp 0.4s ease both;
}
.feature-list li:nth-child(1) { animation-delay: 0.2s; }
.feature-list li:nth-child(2) { animation-delay: 0.32s; }
.feature-list li:nth-child(3) { animation-delay: 0.44s; }
.feature-list li:nth-child(4) { animation-delay: 0.56s; }

.create-form-card {
  background: #fff; border: 1px solid rgba(0,0,0,0.08); border-radius: 12px; padding: 1.5rem;
  animation: fadeSlideUp 0.5s ease 0.15s both;
}
.cfc-title { font-size: 0.75rem; letter-spacing: 0.1em; text-transform: uppercase; color: rgba(0,0,0,0.35); margin-bottom: 1.25rem; }
.inp-row { display: flex; align-items: center; gap: 0.6rem; }
.btn-primary.full { width: 100%; margin-top: 0.5rem; padding: 0.7rem; }

/* create-form-card 內的 label/input 用深色 */
.create-form-card .field-row label { color: rgba(0,0,0,0.5); }
.create-form-card .inp { background: #f5f5f5; border: 1px solid rgba(0,0,0,0.12); color: #1a1a1a; }
.create-form-card .inp::placeholder { color: rgba(0,0,0,0.3); }
.create-form-card .inp:focus { border-color: #ff2d6b; background: #fff; }
.create-form-card .unit { color: rgba(0,0,0,0.4); }

/* Field styles */
.field-row { display: flex; flex-direction: column; gap: 0.35rem; margin-bottom: 1rem; }
.field-row.inline { flex-direction: row; align-items: center; gap: 0.5rem; margin-bottom: 0; }
.field-row label { font-size: 0.72rem; letter-spacing: 0.08em; color: rgba(0,0,0,0.4); text-transform: uppercase; }
.inp { background: #f5f5f5; border: 1px solid rgba(0,0,0,0.1); border-radius: 6px; padding: 0.5rem 0.75rem; color: #1a1a1a; font-size: 0.9rem; outline: none; width: 100%; box-sizing: border-box; transition: border-color 0.2s; }
.inp::placeholder { color: rgba(0,0,0,0.28); }
.inp:focus { border-color: #ff2d6b; background: #fff; }
.inp.sm { width: 80px; }
.inp.ta { resize: vertical; min-height: 60px; font-family: inherit; }
.inp.sel { appearance: none; cursor: pointer; }
.unit { color: rgba(0,0,0,0.4); font-size: 0.85rem; white-space: nowrap; }
.hint { color: rgba(0,0,0,0.3); font-size: 0.75rem; }
.mt { margin-top: 0.75rem; }

/* Settings card */
.settings-card { background: #fff; border: 1px solid rgba(0,0,0,0.08); border-radius: 10px; padding: 1.25rem; margin-bottom: 1.25rem; box-shadow: 0 2px 8px rgba(0,0,0,0.04); }
.settings-row { display: flex; align-items: center; gap: 1rem; flex-wrap: wrap; margin-bottom: 0.5rem; }

.toggle-row { display: flex; align-items: center; gap: 1rem; padding-top: 0.75rem; border-top: 1px solid rgba(0,0,0,0.07); margin-top: 0.75rem; }
.toggle-label { display: flex; align-items: center; gap: 0.5rem; font-size: 0.88rem; color: rgba(0,0,0,0.65); flex: 1; }
.status-dot { width: 8px; height: 8px; border-radius: 50%; }
.status-dot.open { background: #22c55e; box-shadow: 0 0 6px rgba(34,197,94,0.5); }
.status-dot.closed { background: rgba(0,0,0,0.2); }
.resp-count { font-size: 0.8rem; color: rgba(0,0,0,0.35); }

.share-row { display: flex; align-items: center; gap: 0.75rem; margin-top: 0.75rem; padding-top: 0.75rem; border-top: 1px solid rgba(0,0,0,0.07); flex-wrap: wrap; }
.share-label { font-size: 0.8rem; color: rgba(0,0,0,0.5); white-space: nowrap; }
.share-link-wrap { display: flex; flex: 1; gap: 0.4rem; min-width: 0; }
.share-link-input { flex: 1; min-width: 0; background: #f5f5f5; border: 1px solid rgba(0,0,0,0.1); border-radius: 6px; padding: 0.35rem 0.65rem; font-size: 0.78rem; color: rgba(0,0,0,0.6); font-family: monospace; outline: none; cursor: text; }
.share-copy-btn { padding: 0.35rem 0.85rem; background: #1a1a1a; border: none; border-radius: 6px; color: #fff; font-size: 0.78rem; cursor: pointer; white-space: nowrap; transition: background 0.2s; }
.share-copy-btn:hover { background: #ff2d6b; }

/* Questions */
.questions-section { }
.section-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 0.75rem; }
.section-header h3 { margin: 0; color: #1a1a1a; font-size: 1rem; }
.empty-questions { text-align: center; padding: 2rem; color: rgba(0,0,0,0.3); font-size: 0.88rem; border: 1px dashed rgba(0,0,0,0.15); border-radius: 8px; }
.question-card { display: flex; align-items: flex-start; gap: 1rem; background: #fff; border: 1px solid rgba(0,0,0,0.08); border-radius: 8px; padding: 0.9rem 1rem; margin-bottom: 0.6rem; transition: box-shadow 0.2s; }
.question-card:hover { box-shadow: 0 4px 16px rgba(255,45,107,0.08); border-color: rgba(255,45,107,0.2); }
.q-left { display: flex; gap: 0.75rem; flex: 1; }
.q-num { font-size: 0.75rem; font-weight: 700; color: #ff2d6b; min-width: 24px; padding-top: 2px; }
.q-info { flex: 1; }
.q-text { color: #1a1a1a; font-size: 0.9rem; margin-bottom: 0.35rem; }
.q-meta { display: flex; gap: 0.4rem; flex-wrap: wrap; margin-bottom: 0.35rem; }
.tag { font-size: 0.7rem; padding: 0.15rem 0.5rem; border-radius: 3px; background: rgba(0,0,0,0.06); color: rgba(0,0,0,0.5); }
.tag.req { background: rgba(255,45,107,0.1); color: #e01f5a; }
.q-options { display: flex; gap: 0.4rem; flex-wrap: wrap; }
.opt-chip { font-size: 0.75rem; padding: 0.15rem 0.6rem; background: rgba(0,0,0,0.04); border: 1px solid rgba(0,0,0,0.08); border-radius: 12px; color: rgba(0,0,0,0.5); }
.q-actions { display: flex; gap: 0.35rem; align-items: flex-start; }

/* Buttons */
.btn-primary { background: #ff2d6b; border: none; color: #fff; padding: 0.55rem 1.2rem; border-radius: 7px; font-size: 0.88rem; font-weight: 700; cursor: pointer; transition: background 0.2s, transform 0.1s; }
.btn-primary:hover:not(:disabled) { background: #e01f5a; transform: translateY(-1px); }
.btn-primary:active:not(:disabled) { transform: translateY(0); }
.btn-primary:disabled { opacity: 0.4; cursor: not-allowed; }
.btn-primary.sm { padding: 0.4rem 0.8rem; font-size: 0.8rem; }
.btn-secondary { background: #f5f5f5; border: 1px solid rgba(0,0,0,0.12); color: rgba(0,0,0,0.65); padding: 0.5rem 1rem; border-radius: 7px; font-size: 0.85rem; cursor: pointer; transition: all 0.2s; }
.btn-secondary:hover:not(:disabled) { background: #ececec; }
.btn-secondary.sm { padding: 0.4rem 0.8rem; font-size: 0.8rem; }
.btn-success { background: rgba(34,197,94,0.1); border: 1px solid rgba(34,197,94,0.35); color: #16a34a; padding: 0.4rem 0.9rem; border-radius: 6px; font-size: 0.82rem; cursor: pointer; transition: all 0.2s; }
.btn-success:hover { background: rgba(34,197,94,0.18); }
.btn-danger { background: rgba(255,60,60,0.08); border: 1px solid rgba(255,60,60,0.25); color: #dc2626; padding: 0.4rem 0.9rem; border-radius: 6px; font-size: 0.82rem; cursor: pointer; transition: all 0.2s; }
.btn-danger:hover { background: rgba(255,60,60,0.15); }
.toggle-btn { white-space: nowrap; }
.btn-cancel { background: transparent; border: 1px solid rgba(0,0,0,0.15); color: rgba(0,0,0,0.45); padding: 0.55rem 1rem; border-radius: 7px; font-size: 0.88rem; cursor: pointer; transition: all 0.2s; }
.btn-cancel:hover { border-color: rgba(0,0,0,0.3); color: rgba(0,0,0,0.7); }
.icon-btn { background: transparent; border: none; cursor: pointer; font-size: 1rem; padding: 0.25rem; opacity: 0.45; transition: opacity 0.15s; }
.icon-btn:hover { opacity: 1; }
.icon-btn.del:hover { color: #dc2626; }
.icon-btn.sm { font-size: 0.8rem; }

/* Modal — 保留深色浮層增加對比 */
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.45); backdrop-filter: blur(8px); z-index: 3000; display: flex; align-items: center; justify-content: center; padding: 1rem; }
.q-modal { background: #fff; border: 1px solid rgba(0,0,0,0.1); border-radius: 14px; padding: 1.5rem; width: 100%; max-width: 480px; max-height: 85vh; overflow-y: auto; box-shadow: 0 20px 60px rgba(0,0,0,0.15); animation: fadeSlideUp 0.25s ease; }
.q-modal h3 { color: #1a1a1a; margin: 0 0 1.25rem; font-size: 1rem; font-weight: 600; }
.req-mark { color: #ff2d6b; }
.checkbox-label { display: flex; align-items: center; gap: 0.5rem; color: rgba(0,0,0,0.6); font-size: 0.88rem; cursor: pointer; }
.options-editor { margin-bottom: 1rem; }
.options-editor label { font-size: 0.72rem; letter-spacing: 0.08em; color: rgba(0,0,0,0.4); text-transform: uppercase; display: block; margin-bottom: 0.5rem; }
.opt-row { display: flex; gap: 0.5rem; align-items: center; margin-bottom: 0.4rem; }
.opt-inp { flex: 1; }
.add-opt-btn { background: none; border: 1px dashed rgba(0,0,0,0.18); color: rgba(0,0,0,0.4); padding: 0.35rem 0.8rem; border-radius: 6px; cursor: pointer; font-size: 0.82rem; width: 100%; margin-top: 0.25rem; transition: all 0.2s; }
.add-opt-btn:hover { border-color: #ff2d6b; color: #ff2d6b; }
.modal-actions { display: flex; gap: 0.75rem; margin-top: 1.25rem; }
.modal-actions .btn-cancel { flex: 1; }
.modal-actions .btn-primary { flex: 2; }
</style>
