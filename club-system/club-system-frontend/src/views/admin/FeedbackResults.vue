<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getFeedbackStats, getFormByActivity } from '@/api/feedback'

const route = useRoute()
const router = useRouter()
const activityId = computed(() => Number(route.params.activityId))

const stats = ref(null)
const loading = ref(true)
const error = ref('')

onMounted(async () => {
  try {
    // 先取得 formId
    const form = await getFormByActivity(activityId.value)
    stats.value = await getFeedbackStats(form.id)
  } catch (e) {
    error.value = e?.response?.data?.error || '載入失敗'
  } finally {
    loading.value = false
  }
})

const maxBarWidth = (dist) => {
  const vals = Object.values(dist)
  return vals.length ? Math.max(...vals) : 1
}

const ratingKeys = (dist) => Object.keys(dist).map(Number).sort((a, b) => a - b)
</script>

<template>
  <div class="results-page">
    <div class="results-header">
      <button class="back-btn" @click="router.back()">← 返回表單設定</button>
      <h2>回饋統計摘要</h2>
    </div>

    <div v-if="loading" class="loading">載入中...</div>
    <p v-if="error" class="err-msg">{{ error }}</p>

    <template v-if="stats">
      <div class="summary-bar">
        <span class="summary-title">{{ stats.formTitle }}</span>
        <span class="summary-count">共 <strong>{{ stats.totalResponses }}</strong> 份回覆</span>
      </div>

      <div v-if="!stats.questions || stats.questions.length === 0" class="empty">
        此表單尚無題目
      </div>

      <div v-for="(q, idx) in stats.questions" :key="q.questionId" class="q-card">
        <div class="q-header">
          <span class="q-num">Q{{ idx + 1 }}</span>
          <div>
            <div class="q-text">{{ q.questionText }}</div>
            <div class="q-meta">
              <span class="tag">{{ { SINGLE_CHOICE: '單選', MULTIPLE_CHOICE: '多選', RATING: '評分', TEXT: '文字' }[q.questionType] }}</span>
              <span class="answered">{{ q.answeredCount }} 人作答</span>
            </div>
          </div>
        </div>

        <!-- RATING -->
        <template v-if="q.questionType === 'RATING'">
          <div class="rating-avg">平均分數：<strong>{{ q.averageRating }}</strong> 分</div>
          <div class="bar-chart" v-if="q.ratingDistribution">
            <div v-for="key in ratingKeys(q.ratingDistribution)" :key="key" class="bar-row">
              <span class="bar-label">{{ key }}</span>
              <div class="bar-track">
                <div class="bar-fill rating"
                     :style="{ width: q.ratingDistribution[key] > 0 && maxBarWidth(q.ratingDistribution) > 0
                       ? (q.ratingDistribution[key] / maxBarWidth(q.ratingDistribution) * 100) + '%'
                       : '0%' }">
                </div>
              </div>
              <span class="bar-count">{{ q.ratingDistribution[key] }}</span>
            </div>
          </div>
        </template>

        <!-- SINGLE / MULTIPLE -->
        <template v-else-if="q.questionType === 'SINGLE_CHOICE' || q.questionType === 'MULTIPLE_CHOICE'">
          <div class="bar-chart" v-if="q.optionCounts">
            <div v-for="opt in q.optionCounts" :key="opt.optionId" class="bar-row">
              <span class="bar-label opt-label">{{ opt.optionText }}</span>
              <div class="bar-track">
                <div class="bar-fill choice"
                     :style="{ width: opt.percentage + '%' }">
                </div>
              </div>
              <span class="bar-count">{{ opt.count }} <small>({{ opt.percentage }}%)</small></span>
            </div>
          </div>
        </template>

        <!-- TEXT -->
        <template v-else-if="q.questionType === 'TEXT'">
          <div v-if="!q.textAnswers || q.textAnswers.length === 0" class="no-text">尚無文字回答</div>
          <div v-for="(t, i) in q.textAnswers" :key="i" class="text-answer">
            <span class="text-num">{{ i + 1 }}.</span>
            <span>{{ t }}</span>
          </div>
        </template>
      </div>
    </template>
  </div>
</template>

<style scoped>
.results-page { max-width: 720px; margin: 0 auto; padding: 1.5rem 1rem; min-height: 100vh; background: #ffffff; }
.results-header { display: flex; align-items: center; gap: 1rem; margin-bottom: 1.5rem; }
.results-header h2 { flex: 1; margin: 0; font-size: 1.3rem; color: #1a1a1a; }
.back-btn { background: transparent; border: 1px solid rgba(0,0,0,0.18); color: rgba(0,0,0,0.55); padding: 0.4rem 0.8rem; border-radius: 6px; cursor: pointer; font-size: 0.85rem; transition: all 0.2s; }
.back-btn:hover { border-color: #ff2d6b; color: #ff2d6b; }
.loading { color: rgba(0,0,0,0.4); padding: 2rem; text-align: center; }
.err-msg { color: #c0392b; font-size: 0.85rem; padding: 0.5rem 0.75rem; background: rgba(255,60,60,0.06); border-radius: 5px; border: 1px solid rgba(255,60,60,0.2); }

.summary-bar { display: flex; align-items: center; background: rgba(255,45,107,0.05); border: 1px solid rgba(255,45,107,0.18); border-radius: 8px; padding: 0.75rem 1rem; margin-bottom: 1.25rem; gap: 1rem; }
.summary-title { flex: 1; color: #1a1a1a; font-weight: 600; }
.summary-count { color: rgba(0,0,0,0.45); font-size: 0.88rem; }
.summary-count strong { color: #ff2d6b; }

.empty { text-align: center; padding: 2rem; color: rgba(0,0,0,0.3); }

.q-card { background: #fff; border: 1px solid rgba(0,0,0,0.08); border-radius: 10px; padding: 1.1rem 1.25rem; margin-bottom: 1rem; box-shadow: 0 2px 8px rgba(0,0,0,0.04); transition: box-shadow 0.2s, border-color 0.2s; }
.q-card:hover { box-shadow: 0 4px 16px rgba(255,45,107,0.08); border-color: rgba(255,45,107,0.15); }
.q-header { display: flex; gap: 0.75rem; margin-bottom: 1rem; }
.q-num { font-size: 0.75rem; font-weight: 700; color: #ff2d6b; min-width: 24px; padding-top: 3px; }
.q-text { color: #1a1a1a; font-size: 0.95rem; margin-bottom: 0.3rem; }
.q-meta { display: flex; align-items: center; gap: 0.5rem; }
.tag { font-size: 0.7rem; padding: 0.15rem 0.5rem; background: rgba(0,0,0,0.06); color: rgba(0,0,0,0.5); border-radius: 3px; }
.answered { font-size: 0.75rem; color: rgba(0,0,0,0.35); }

/* Rating */
.rating-avg { font-size: 0.88rem; color: rgba(0,0,0,0.5); margin-bottom: 0.75rem; }
.rating-avg strong { color: #d97706; font-size: 1.1rem; }

/* Bar chart */
.bar-chart { display: flex; flex-direction: column; gap: 0.4rem; }
.bar-row { display: flex; align-items: center; gap: 0.6rem; }
.bar-label { font-size: 0.8rem; color: rgba(0,0,0,0.45); min-width: 24px; text-align: right; }
.bar-label.opt-label { min-width: 100px; max-width: 180px; text-align: left; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.bar-track { flex: 1; height: 12px; background: rgba(0,0,0,0.06); border-radius: 6px; overflow: hidden; }
.bar-fill { height: 100%; border-radius: 6px; transition: width 0.5s ease; }
.bar-fill.rating { background: linear-gradient(90deg, #fbbf24, #f59e0b); }
.bar-fill.choice { background: linear-gradient(90deg, #ff2d6b, #ff6b9d); }
.bar-count { font-size: 0.78rem; color: rgba(0,0,0,0.4); min-width: 60px; }
.bar-count small { color: rgba(0,0,0,0.28); }

/* Text answers */
.no-text { color: rgba(0,0,0,0.3); font-size: 0.85rem; padding: 0.5rem 0; }
.text-answer { display: flex; gap: 0.6rem; padding: 0.6rem 0.75rem; background: #f9f9f9; border-radius: 6px; margin-bottom: 0.4rem; font-size: 0.88rem; color: rgba(0,0,0,0.7); border-left: 2px solid rgba(255,45,107,0.35); }
.text-num { color: rgba(0,0,0,0.3); min-width: 20px; }
</style>
