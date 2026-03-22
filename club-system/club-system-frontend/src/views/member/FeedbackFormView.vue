<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getFormById, getFormByActivity, submitFeedback } from '@/api/feedback'
import router from '@/router'

const route = useRoute()
const userStore = useUserStore()

const form = ref(null)
const loading = ref(true)
const error = ref('')
const submitted = ref(false)
const pointsAwarded = ref(0)
const submitting = ref(false)
const submitError = ref('')

// answers: { [questionId]: { textAnswer?, ratingAnswer?, selectedOptionIds: [] } }
const answers = ref({})

onMounted(async () => {
  try {
    // 支援兩種路由：/feedback/:formId 或 /feedback/activity/:activityId
    if (route.params.activityId) {
      form.value = await getFormByActivity(Number(route.params.activityId))
    } else {
      form.value = await getFormById(Number(route.params.formId))
    }

    if (!form.value.isOpen) {
      error.value = '此表單目前未開放填寫'
    } else if (form.value.hasSubmitted) {
      submitted.value = true
      pointsAwarded.value = 0
    }

    // 初始化 answers
    if (form.value.questions) {
      form.value.questions.forEach(q => {
        answers.value[q.id] = {
          textAnswer: '',
          ratingAnswer: null,
          selectedOptionIds: []
        }
      })
    }
  } catch (e) {
    error.value = e?.response?.data?.error || '載入表單失敗'
  } finally {
    loading.value = false
  }
})

function toggleOption(questionId, optionId, isSingle) {
  const ans = answers.value[questionId]
  if (isSingle) {
    ans.selectedOptionIds = [optionId]
  } else {
    const idx = ans.selectedOptionIds.indexOf(optionId)
    if (idx >= 0) {
      ans.selectedOptionIds.splice(idx, 1)
    } else {
      ans.selectedOptionIds.push(optionId)
    }
  }
}

function isOptionSelected(questionId, optionId) {
  return answers.value[questionId]?.selectedOptionIds?.includes(optionId)
}

function setRating(questionId, score) {
  answers.value[questionId].ratingAnswer = score
}

function validateAnswers() {
  for (const q of form.value.questions) {
    if (!q.isRequired) continue
    const ans = answers.value[q.id]
    if (q.questionType === 'TEXT' && !ans.textAnswer?.trim()) return `「${q.questionText}」為必填題目`
    if (q.questionType === 'RATING' && !ans.ratingAnswer) return `「${q.questionText}」為必填題目`
    if ((q.questionType === 'SINGLE_CHOICE' || q.questionType === 'MULTIPLE_CHOICE')
        && ans.selectedOptionIds.length === 0) return `「${q.questionText}」為必填題目`
  }
  return null
}

async function handleSubmit() {
  submitError.value = ''
  const validErr = validateAnswers()
  if (validErr) { submitError.value = validErr; return }

  submitting.value = true
  try {
    const payload = {
      answers: form.value.questions.map(q => {
        const ans = answers.value[q.id]
        return {
          questionId: q.id,
          textAnswer: q.questionType === 'TEXT' ? (ans.textAnswer || null) : null,
          ratingAnswer: q.questionType === 'RATING' ? ans.ratingAnswer : null,
          selectedOptionIds: (q.questionType === 'SINGLE_CHOICE' || q.questionType === 'MULTIPLE_CHOICE')
            ? ans.selectedOptionIds : []
        }
      })
    }
    const result = await submitFeedback(form.value.id, payload)
    submitted.value = true
    pointsAwarded.value = result.pointsAwarded || 0
    setTimeout(() => router.push('/dashboard'), 2500)

  } catch (e) {
    submitError.value = e?.response?.data?.error || '提交失敗，請稍後再試'
  } finally {
    submitting.value = false
  }
}

const isLoggedIn = computed(() => userStore.isLoggedIn)
</script>

<template>
  <div class="form-page">
    <div v-if="loading" class="loading">載入中...</div>

    <!-- 錯誤狀態 -->
    <div v-else-if="error" class="error-state">
      <div class="state-icon">🔒</div>
      <h3>{{ error }}</h3>
    </div>

    <!-- 已提交 -->
    <div v-else-if="submitted" class="success-state">
      <div class="state-icon">✅</div>
      <h3>感謝您的回饋！</h3>
      <p v-if="pointsAwarded > 0" class="points-notice">
        您獲得了 <strong>{{ pointsAwarded }}</strong> 點信用積分 🎉
      </p>
      <p class="sub">您的回饋將幫助我們持續改善活動品質。</p>
    </div>

    <!-- 表單 -->
    <template v-else-if="form">
      <div class="form-header">
        <h2>{{ form.title }}</h2>
        <p v-if="form.description" class="form-desc">{{ form.description }}</p>
        <div v-if="isLoggedIn && form.pointsReward > 0" class="points-hint">
          🎁 提交後可獲得 <strong>{{ form.pointsReward }}</strong> 點信用積分
        </div>
        <div v-else-if="!isLoggedIn && form.pointsReward > 0" class="points-hint guest">
          💡 登入後提交可獲得 {{ form.pointsReward }} 點信用積分
        </div>
      </div>

      <div v-for="(q, idx) in form.questions" :key="q.id" class="question-block">
        <div class="q-label">
          <span class="q-idx">{{ idx + 1 }}</span>
          <span class="q-text">{{ q.questionText }}</span>
          <span v-if="q.isRequired" class="req-mark">*</span>
        </div>

        <!-- 單選 -->
        <div v-if="q.questionType === 'SINGLE_CHOICE'" class="options-list">
          <label v-for="opt in q.options" :key="opt.id"
                 class="opt-item" :class="{ selected: isOptionSelected(q.id, opt.id) }"
                 @click="toggleOption(q.id, opt.id, true)">
            <span class="radio-dot" :class="{ active: isOptionSelected(q.id, opt.id) }"></span>
            {{ opt.optionText }}
          </label>
        </div>

        <!-- 多選 -->
        <div v-else-if="q.questionType === 'MULTIPLE_CHOICE'" class="options-list">
          <label v-for="opt in q.options" :key="opt.id"
                 class="opt-item" :class="{ selected: isOptionSelected(q.id, opt.id) }"
                 @click="toggleOption(q.id, opt.id, false)">
            <span class="checkbox-box" :class="{ active: isOptionSelected(q.id, opt.id) }">
              <span v-if="isOptionSelected(q.id, opt.id)">✓</span>
            </span>
            {{ opt.optionText }}
          </label>
        </div>

        <!-- 評分 -->
        <div v-else-if="q.questionType === 'RATING'" class="rating-row">
          <button
            v-for="n in q.maxRating" :key="n"
            class="score-btn"
            :class="{ active: answers[q.id]?.ratingAnswer === n }"
            @click="setRating(q.id, n)"
          >{{ n }}</button>
          <span class="rating-hint">
            {{ answers[q.id]?.ratingAnswer ? answers[q.id].ratingAnswer + ' 分' : '請選擇' }}
          </span>
        </div>

        <!-- 文字 -->
        <div v-else-if="q.questionType === 'TEXT'">
          <textarea
            v-model="answers[q.id].textAnswer"
            class="txt-area"
            placeholder="請輸入您的回覆..."
            rows="3"
          />
        </div>
      </div>

      <p v-if="submitError" class="err-msg">{{ submitError }}</p>

      <button class="submit-btn" @click="handleSubmit" :disabled="submitting">
        {{ submitting ? '提交中...' : '提交回饋' }}
      </button>
    </template>
  </div>
</template>

<style scoped>
.form-page { max-width: 640px; margin: 0 auto; padding: 2rem 1rem; min-height: 100vh; background: #ffffff; }
.loading { text-align: center; color: rgba(0,0,0,0.4); padding: 3rem; }

.error-state, .success-state { text-align: center; padding: 3rem 1rem; }
.state-icon { font-size: 3rem; margin-bottom: 1rem; }
.error-state h3 { color: rgba(0,0,0,0.5); margin: 0 0 0.5rem; }
.success-state h3 { color: #1a1a1a; margin: 0 0 0.5rem; }
.points-notice { color: #d97706; font-size: 1rem; margin: 0.5rem 0; }
.points-notice strong { font-size: 1.3rem; }
.sub { color: rgba(0,0,0,0.4); font-size: 0.88rem; }

.form-header { margin-bottom: 1.75rem; }
.form-header h2 { color: #1a1a1a; font-size: 1.4rem; margin: 0 0 0.5rem; }
.form-desc { color: rgba(0,0,0,0.5); font-size: 0.9rem; margin: 0 0 0.75rem; line-height: 1.6; }
.points-hint { display: inline-block; padding: 0.4rem 0.9rem; background: rgba(217,119,6,0.08); border: 1px solid rgba(217,119,6,0.25); border-radius: 6px; color: #b45309; font-size: 0.82rem; }
.points-hint.guest { background: rgba(0,0,0,0.04); border-color: rgba(0,0,0,0.1); color: rgba(0,0,0,0.4); }
.points-hint strong { font-size: 0.95rem; }

.question-block { background: #fff; border: 1px solid rgba(0,0,0,0.08); border-radius: 10px; padding: 1.1rem 1.25rem; margin-bottom: 1rem; box-shadow: 0 2px 8px rgba(0,0,0,0.04); transition: box-shadow 0.2s, border-color 0.2s; }
.question-block:hover { box-shadow: 0 4px 16px rgba(255,45,107,0.07); border-color: rgba(255,45,107,0.12); }
.q-label { display: flex; align-items: flex-start; gap: 0.6rem; margin-bottom: 0.9rem; }
.q-idx { display: flex; align-items: center; justify-content: center; width: 22px; height: 22px; background: rgba(255,45,107,0.1); border: 1px solid rgba(255,45,107,0.25); border-radius: 50%; font-size: 0.7rem; font-weight: 700; color: #ff2d6b; flex-shrink: 0; margin-top: 1px; }
.q-text { color: #1a1a1a; font-size: 0.95rem; line-height: 1.5; flex: 1; }
.req-mark { color: #ff2d6b; font-size: 1rem; flex-shrink: 0; }

/* Options */
.options-list { display: flex; flex-direction: column; gap: 0.5rem; }
.opt-item { display: flex; align-items: center; gap: 0.65rem; padding: 0.6rem 0.85rem; background: #f9f9f9; border: 1.5px solid rgba(0,0,0,0.08); border-radius: 7px; cursor: pointer; color: rgba(0,0,0,0.65); font-size: 0.9rem; transition: all 0.2s; user-select: none; }
.opt-item:hover { border-color: rgba(255,45,107,0.3); background: rgba(255,45,107,0.04); }
.opt-item.selected { border-color: #ff2d6b; background: rgba(255,45,107,0.07); color: #1a1a1a; }
.radio-dot { width: 16px; height: 16px; border-radius: 50%; border: 2px solid rgba(0,0,0,0.2); flex-shrink: 0; transition: all 0.2s; }
.radio-dot.active { border-color: #ff2d6b; background: #ff2d6b; box-shadow: inset 0 0 0 3px #fff; }
.checkbox-box { width: 16px; height: 16px; border-radius: 3px; border: 2px solid rgba(0,0,0,0.2); flex-shrink: 0; display: flex; align-items: center; justify-content: center; font-size: 0.65rem; font-weight: 700; transition: all 0.2s; }
.checkbox-box.active { border-color: #ff2d6b; background: #ff2d6b; color: #fff; }

/* Rating */
.rating-row { display: flex; align-items: center; gap: 0.35rem; flex-wrap: wrap; }
.score-btn { width: 38px; height: 38px; border-radius: 7px; border: 1.5px solid rgba(0,0,0,0.1); background: #f5f5f5; color: rgba(0,0,0,0.55); font-size: 0.88rem; font-weight: 600; cursor: pointer; transition: all 0.15s; }
.score-btn:hover { border-color: rgba(255,45,107,0.4); background: rgba(255,45,107,0.06); color: #ff2d6b; }
.score-btn.active { border-color: #ff2d6b; background: #ff2d6b; color: #fff; box-shadow: 0 2px 8px rgba(255,45,107,0.3); }
.rating-hint { font-size: 0.8rem; color: rgba(0,0,0,0.35); margin-left: 0.25rem; }

/* Text */
.txt-area { width: 100%; background: #f5f5f5; border: 1px solid rgba(0,0,0,0.1); border-radius: 7px; padding: 0.65rem 0.85rem; color: #1a1a1a; font-size: 0.9rem; resize: vertical; outline: none; font-family: inherit; box-sizing: border-box; transition: border-color 0.2s, background 0.2s; }
.txt-area:focus { border-color: #ff2d6b; background: #fff; }
.txt-area::placeholder { color: rgba(0,0,0,0.28); }

.err-msg { color: #c0392b; font-size: 0.85rem; padding: 0.5rem 0.75rem; background: rgba(255,60,60,0.06); border-radius: 5px; border: 1px solid rgba(255,60,60,0.2); margin-bottom: 0.75rem; }

.submit-btn { width: 100%; padding: 0.85rem; background: #ff2d6b; border: none; border-radius: 9px; color: #fff; font-size: 1rem; font-weight: 700; cursor: pointer; margin-top: 0.5rem; transition: background 0.2s, transform 0.1s; }
.submit-btn:hover:not(:disabled) { background: #e01f5a; transform: translateY(-1px); }
.submit-btn:active:not(:disabled) { transform: translateY(0); }
.submit-btn:disabled { opacity: 0.4; cursor: not-allowed; }
</style>
