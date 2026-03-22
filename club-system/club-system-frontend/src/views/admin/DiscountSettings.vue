<script setup>
import { ref, onMounted } from 'vue'
import { discountApi } from '@/api/discount'
import { promoCodeApi } from '@/api/promoCode'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const loading = ref(false)
const saving = ref(false)
const successMsg = ref('')
const errorMsg = ref('')

const form = ref({
  earlyBirdDefaultDays: 3,
  earlyBirdRate: 0.8,
  loyaltyThreshold: 5,
  loyaltyRate: 0.5,
  welcomeCouponEnabled: true,
  welcomeCouponRate: 0.8,
})

const loadConfig = async () => {
  loading.value = true
  errorMsg.value = ''
  try {
    const res = await discountApi.getConfig()
    form.value = {
      earlyBirdDefaultDays: res.data.earlyBirdDefaultDays ?? 3,
      earlyBirdRate: Number(res.data.earlyBirdRate ?? 0.8),
      loyaltyThreshold: res.data.loyaltyThreshold,
      loyaltyRate: Number(res.data.loyaltyRate),
      welcomeCouponEnabled: res.data.welcomeCouponEnabled ?? true,
      welcomeCouponRate: Number(res.data.welcomeCouponRate ?? 0.8),
    }
  } catch (e) {
    errorMsg.value = '載入設定失敗：' + (e.response?.data?.message || e.message)
  } finally {
    loading.value = false
  }
}

const save = async () => {
  saving.value = true
  successMsg.value = ''
  errorMsg.value = ''
  try {
    await discountApi.updateConfig(form.value, userStore.userId || 'admin')
    successMsg.value = '✅ 設定已儲存'
    setTimeout(() => { successMsg.value = '' }, 3000)
  } catch (e) {
    errorMsg.value = '❌ 儲存失敗：' + (e.response?.data?.message || e.message)
  } finally {
    saving.value = false
  }
}

const loyaltyDiscount   = () => Math.round((1 - form.value.loyaltyRate) * 100)
const welcomeDiscount   = () => Math.round((1 - form.value.welcomeCouponRate) * 100)

// ===== 手動發放優惠券 =====
const issueTarget = ref('SINGLE') // 'SINGLE' | 'OFFICERS' | 'ALL_MEMBERS'
const issueForm = ref({ userId: '', discountType: 'RATE', discountRate: 0.8, discountAmount: null, description: '' })
const issuing = ref(false)
const issueMsg = ref('')
const issueError = ref('')

const targetOptions = [
  { value: 'SINGLE',      label: '指定會員' },
  { value: 'OFFICERS',    label: '全部幹部' },
  { value: 'ALL_MEMBERS', label: '全部會員' },
]

const issueCoupon = async () => {
  issuing.value = true
  issueMsg.value = ''
  issueError.value = ''
  const payload = {
    discountRate: issueForm.value.discountType === 'RATE' ? issueForm.value.discountRate : null,
    discountAmount: issueForm.value.discountType === 'AMOUNT' ? issueForm.value.discountAmount : null,
    description: issueForm.value.description.trim() || null,
  }
  const issuedBy = userStore.userId || 'admin'
  try {
    if (issueTarget.value === 'SINGLE') {
      if (!issueForm.value.userId.trim()) {
        issueError.value = '請輸入會員 ID'
        issuing.value = false
        return
      }
      await discountApi.issueCoupon(
        { ...payload, userId: issueForm.value.userId.trim() },
        issuedBy
      )
      issueMsg.value = `✅ 已成功發放給 ${issueForm.value.userId.trim()}`
    } else {
      const res = await discountApi.issueBatchCoupons(issueTarget.value, payload, issuedBy)
      issueMsg.value = `✅ ${res.data.message}`
    }
    issueForm.value = { userId: '', discountType: 'RATE', discountRate: 0.8, discountAmount: null, description: '' }
    loadAllCoupons()
    setTimeout(() => { issueMsg.value = '' }, 4000)
  } catch (e) {
    issueError.value = '❌ ' + (e.response?.data?.message || e.message)
  } finally {
    issuing.value = false
  }
}

// ===== 優惠券清單 =====
const allCoupons = ref([])
const couponsLoading = ref(false)

const loadAllCoupons = async () => {
  couponsLoading.value = true
  try {
    const res = await discountApi.adminListAllCoupons()
    allCoupons.value = res.data
  } catch (e) {
    console.error('載入優惠券清單失敗', e)
  } finally {
    couponsLoading.value = false
  }
}

const couponTypeLabel = (t) => {
  if (t === 'COUPON') return '累積出席'
  if (t === 'WELCOME') return '新人'
  if (t === 'CUSTOM')  return '手動發放'
  return t || '—'
}

const couponTypeClass = (t) => {
  if (t === 'COUPON') return 'tag-coupon'
  if (t === 'WELCOME') return 'tag-welcome'
  return 'tag-custom'
}

const formatDt = (dt) => {
  if (!dt) return '—'
  return new Date(dt).toLocaleString('zh-TW', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit'
  })
}

// ===== 優惠碼管理 =====
const promoCodes = ref([])
const promoForm = ref({ code: '', discountType: 'FIXED_AMOUNT', discountValue: 100, description: '', maxUsage: null, expiresAt: '' })
const promoCreating = ref(false)
const promoCreateMsg = ref('')
const promoCreateError = ref('')

const loadPromoCodes = async () => {
  try {
    const res = await promoCodeApi.listAll()
    promoCodes.value = res.data
  } catch (e) {
    console.error('載入優惠碼失敗', e)
  }
}

const createPromoCode = async () => {
  if (!promoForm.value.code.trim()) { promoCreateError.value = '請輸入優惠碼'; return }
  promoCreating.value = true
  promoCreateMsg.value = ''
  promoCreateError.value = ''
  try {
    await promoCodeApi.create(
      {
        code: promoForm.value.code.trim().toUpperCase(),
        discountType: promoForm.value.discountType,
        discountValue: promoForm.value.discountValue,
        description: promoForm.value.description.trim() || null,
        maxUsage: promoForm.value.maxUsage || null,
        expiresAt: promoForm.value.expiresAt || null,
      },
      userStore.userId || 'admin'
    )
    promoCreateMsg.value = '✅ 優惠碼建立成功'
    promoForm.value = { code: '', discountType: 'FIXED_AMOUNT', discountValue: 100, description: '', maxUsage: null, expiresAt: '' }
    loadPromoCodes()
    setTimeout(() => { promoCreateMsg.value = '' }, 3000)
  } catch (e) {
    promoCreateError.value = '❌ ' + (e.response?.data?.message || e.message)
  } finally {
    promoCreating.value = false
  }
}

const togglePromoCode = async (id) => {
  try {
    await promoCodeApi.toggle(id)
    loadPromoCodes()
  } catch (e) {
    alert('操作失敗：' + (e.response?.data?.message || e.message))
  }
}

const deletePromoCode = async (id) => {
  if (!confirm('確定要刪除此優惠碼？')) return
  try {
    await promoCodeApi.remove(id)
    loadPromoCodes()
  } catch (e) {
    alert('刪除失敗：' + (e.response?.data?.message || e.message))
  }
}

onMounted(() => {
  loadConfig()
  loadAllCoupons()
  loadPromoCodes()
})
</script>

<template>
  <div class="discount-settings">
    <div class="container">

      <div class="page-header">
        <h1>折扣設定</h1>
        <p class="subtitle">配置忠誠優惠券規則與新人優惠</p>
      </div>

      <!-- 載入中 -->
      <div v-if="loading" class="loading-state">載入中...</div>

      <form v-else @submit.prevent="save">
        <!-- ===== 規則說明 ===== -->
        <div class="notice-card">
          <h3 class="notice-title">📌 折扣規則</h3>
          <ul class="notice-list">
            <li>幹部（officer 角色）報名任何活動一律<strong>免費</strong></li>
            <li>折扣<strong>不疊加</strong>，每次報名只能選一種</li>
            <li>早鳥截止時間在新增／編輯活動時個別設定</li>
            <li>忠誠優惠券在每次活動<strong>簽到後</strong>自動計算是否達門檻</li>
            <li>手動發放的優惠券有自訂折扣率，優先於全域設定</li>
          </ul>
        </div>

        <!-- ===== 忠誠優惠券 ===== -->
        <div class="card">
          <div class="card-header">
            <div>
              <h2 class="card-title">忠誠優惠券</h2>
              <p class="card-desc">會員累積出席 N 場活動，自動獲得優惠券</p>
            </div>
          </div>

          <div class="field-row">
            <div class="field">
              <label class="label">累積出席門檻（場）</label>
              <div class="input-wrap">
                <input
                  v-model.number="form.loyaltyThreshold"
                  type="number" min="1" max="50"
                  class="input"
                />
                <span class="unit">場</span>
              </div>
              <p class="hint">每滿 {{ form.loyaltyThreshold }} 場發放一張優惠券</p>
            </div>

            <div class="field">
              <label class="label">忠誠折扣率</label>
              <div class="input-wrap">
                <input
                  v-model.number="form.loyaltyRate"
                  type="number" min="0.01" max="1" step="0.01"
                  class="input"
                />
                <span class="unit discount-tag">打 {{ Math.round(form.loyaltyRate * 10) }} 折（省 {{ loyaltyDiscount() }}%）</span>
              </div>
              <p class="hint">輸入 0.5 = 五折（半價）</p>
            </div>
          </div>
        </div>

        <!-- ===== 新人優惠券 ===== -->
        <div class="card">
          <div class="card-header">
            <div>
              <h2 class="card-title">新人優惠券</h2>
              <p class="card-desc">新會員完成註冊時自動發放一張歡迎優惠券</p>
            </div>
            <label class="toggle-wrap">
              <input type="checkbox" v-model="form.welcomeCouponEnabled" class="toggle-input" />
              <span class="toggle-slider"></span>
              <span class="toggle-label">{{ form.welcomeCouponEnabled ? '啟用' : '停用' }}</span>
            </label>
          </div>

          <div v-if="form.welcomeCouponEnabled" class="field-row">
            <div class="field">
              <label class="label">新人折扣率</label>
              <div class="input-wrap">
                <input
                  v-model.number="form.welcomeCouponRate"
                  type="number" min="0.01" max="1" step="0.01"
                  class="input"
                />
                <span class="unit discount-tag">打 {{ Math.round(form.welcomeCouponRate * 10) }} 折（省 {{ welcomeDiscount() }}%）</span>
              </div>
              <p class="hint">預設八折（0.8）</p>
            </div>
          </div>
          <p v-else class="disabled-hint">目前停用，新會員不會自動收到優惠券</p>
        </div>

       <!-- ===== 手動發放優惠券 ===== -->
      <div class="card section-gap">
        <div class="card-header">
          <div>
            <h2 class="card-title">手動發放優惠券</h2>
            <p class="card-desc">選擇發放對象與折扣率</p>
          </div>
        </div>

        <!-- 發放對象選擇 -->
        <div class="target-row">
          <label class="label">發放對象</label>
          <div class="target-options">
            <label
              v-for="opt in targetOptions"
              :key="opt.value"
              class="target-option"
              :class="{ active: issueTarget === opt.value }"
            >
              <input type="radio" v-model="issueTarget" :value="opt.value" class="radio-hidden" />
              {{ opt.label }}
            </label>
          </div>
        </div>

        <div class="issue-form">
          <!-- 指定會員才顯示 ID 輸入 -->
          <div v-if="issueTarget === 'SINGLE'" class="field">
            <label class="label">會員 ID</label>
            <input
              v-model="issueForm.userId"
              type="text"
              placeholder="例如 m0001"
              class="input input-full"
            />
          </div>
          <!-- 批次對象說明 -->
          <div v-else class="field batch-hint-field">
            <div class="batch-hint">
              <span v-if="issueTarget === 'OFFICERS'">🎯 將對所有角色為「幹部」的會員各發放一張</span>
              <span v-else>👥 將對所有一般會員各發放一張</span>
            </div>
          </div>

          <div class="field">
            <label class="label">折扣方式</label>
            <div class="input-wrap">
              <label style="margin-right:1rem">
                <input type="radio" v-model="issueForm.discountType" value="RATE" /> 折扣率
              </label>
              <label>
                <input type="radio" v-model="issueForm.discountType" value="AMOUNT" /> 折抵金額
              </label>
            </div>
          </div>
          <div v-if="issueForm.discountType === 'RATE'" class="field">
            <label class="label">折扣率</label>
            <div class="input-wrap">
              <input
                v-model.number="issueForm.discountRate"
                type="number" min="0.01" max="1" step="0.01"
                class="input"
              />
              <span class="unit discount-tag">打 {{ Math.round(issueForm.discountRate * 10) }} 折</span>
            </div>
          </div>
          <div v-else class="field">
            <label class="label">折抵金額（NT$）</label>
            <div class="input-wrap">
              <input
                v-model.number="issueForm.discountAmount"
                type="number" min="1" step="1"
                placeholder="例如 50"
                class="input"
              />
              <span class="unit">元</span>
            </div>
          </div>
          <div class="field field-full">
            <label class="label">備注說明（選填）</label>
            <input
              v-model="issueForm.description"
              type="text"
              placeholder="例如：期末活動特別獎勵"
              class="input input-full"
            />
          </div>
        </div>

        <div class="issue-actions">
          <p v-if="issueMsg"   class="msg success">{{ issueMsg }}</p>
          <p v-if="issueError" class="msg error">{{ issueError }}</p>
          <button class="save-btn" :disabled="issuing" @click="issueCoupon">
            {{ issuing ? '發放中...' : '發放優惠券' }}
          </button>
        </div>
      </div>

        

      </form>
<!-- ===== 優惠碼管理 ===== -->
      <div class="card section-gap">
        <div class="card-header">
          <div>
            <h2 class="card-title">優惠碼管理</h2>
            <p class="card-desc">建立專屬優惠碼，指定折扣金額或折扣比例</p>
          </div>
        </div>

        <!-- 建立表單 -->
        <div class="promo-create-form">
          <div class="field">
            <label class="label">優惠碼</label>
            <input v-model="promoForm.code" type="text" placeholder="例如 DANCE2026" class="input input-full" style="text-transform:uppercase" />
          </div>

          <div class="field">
            <label class="label">折扣方式</label>
            <div class="target-options">
              <label class="target-option" :class="{ active: promoForm.discountType === 'FIXED_AMOUNT' }">
                <input type="radio" v-model="promoForm.discountType" value="FIXED_AMOUNT" class="radio-hidden" />
                折扣金額（元）
              </label>
              <label class="target-option" :class="{ active: promoForm.discountType === 'PERCENTAGE' }">
                <input type="radio" v-model="promoForm.discountType" value="PERCENTAGE" class="radio-hidden" />
                折扣比例（%）
              </label>
            </div>
          </div>

          <div class="field">
            <label class="label">{{ promoForm.discountType === 'FIXED_AMOUNT' ? '折扣金額（元）' : '折扣率（0.01~1.0）' }}</label>
            <div class="input-wrap">
              <input
                v-model.number="promoForm.discountValue"
                type="number"
                :min="promoForm.discountType === 'FIXED_AMOUNT' ? 1 : 0.01"
                :max="promoForm.discountType === 'FIXED_AMOUNT' ? undefined : 1"
                :step="promoForm.discountType === 'FIXED_AMOUNT' ? 1 : 0.01"
                class="input"
              />
              <span class="unit discount-tag" v-if="promoForm.discountType === 'PERCENTAGE'">
                打 {{ Math.round(promoForm.discountValue * 10) }} 折
              </span>
              <span class="unit" v-else>元</span>
            </div>
          </div>

          <div class="field">
            <label class="label">說明（選填）</label>
            <input v-model="promoForm.description" type="text" placeholder="例如：春季特別優惠" class="input input-full" />
          </div>

          <div class="field">
            <label class="label">使用上限（選填，空白 = 無限）</label>
            <input v-model.number="promoForm.maxUsage" type="number" min="1" placeholder="例如 100" class="input" />
          </div>

          <div class="field">
            <label class="label">到期時間（選填）</label>
            <input v-model="promoForm.expiresAt" type="datetime-local" class="input input-full" />
          </div>
        </div>

        <div class="issue-actions">
          <p v-if="promoCreateMsg"   class="msg success">{{ promoCreateMsg }}</p>
          <p v-if="promoCreateError" class="msg error">{{ promoCreateError }}</p>
          <button class="save-btn" :disabled="promoCreating" @click="createPromoCode">
            {{ promoCreating ? '建立中...' : '建立優惠碼' }}
          </button>
        </div>

        <!-- 優惠碼列表 -->
        <div v-if="promoCodes.length > 0" class="promo-list-wrap">
          <hr style="margin: 1.5rem 0; border: none; border-top: 1px solid #f0f0f0;" />
          <table class="coupon-table">
            <thead>
              <tr>
                <th>優惠碼</th>
                <th>折扣方式</th>
                <th>折扣值</th>
                <th>說明</th>
                <th>使用次數</th>
                <th>到期時間</th>
                <th>狀態</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="p in promoCodes" :key="p.id" :class="{ 'row-used': !p.isActive }">
                <td><strong>{{ p.code }}</strong></td>
                <td>{{ p.discountType === 'FIXED_AMOUNT' ? '折扣金額' : '折扣比例' }}</td>
                <td class="unit discount-tag">
                  {{ p.discountType === 'FIXED_AMOUNT' ? `NT$ ${p.discountValue}` : `打 ${Math.round(p.discountValue * 10)} 折` }}
                </td>
                <td class="td-desc">{{ p.description || '—' }}</td>
                <td>{{ p.usedCount }}{{ p.maxUsage ? ` / ${p.maxUsage}` : '' }}</td>
                <td class="td-time">{{ p.expiresAt ? formatDt(p.expiresAt) : '永不過期' }}</td>
                <td>
                  <span v-if="p.isActive" class="status-avail">啟用</span>
                  <span v-else class="status-used">停用</span>
                </td>
                <td>
                  <div class="promo-actions">
                    <button class="action-sm" @click="togglePromoCode(p.id)">
                      {{ p.isActive ? '停用' : '啟用' }}
                    </button>
                    <button class="action-sm danger" @click="deletePromoCode(p.id)">刪除</button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <div v-else class="empty-state" style="padding: 1rem 0 0;">尚無優惠碼</div>
      </div>
      
      <!-- ===== 儲存 ===== -->
        <div class="actions">
          <p v-if="successMsg" class="msg success">{{ successMsg }}</p>
          <p v-if="errorMsg"   class="msg error">{{ errorMsg }}</p>
          <button type="submit" class="save-btn" :disabled="saving">
            {{ saving ? '儲存中...' : '儲存設定' }}
          </button>
        </div>

        

      <!-- ===== 優惠券清單 ===== -->
      <div class="card section-gap">
        <div class="card-header">
          <div>
            <h2 class="card-title">優惠券紀錄</h2>
            <p class="card-desc">所有已發放的優惠券（共 {{ allCoupons.length }} 張）</p>
          </div>
          <button class="refresh-btn" @click="loadAllCoupons" :disabled="couponsLoading">
            {{ couponsLoading ? '載入中...' : '重新整理' }}
          </button>
        </div>

        <div v-if="couponsLoading" class="empty-state">載入中...</div>
        <div v-else-if="allCoupons.length === 0" class="empty-state">目前尚無優惠券紀錄</div>
        <div v-else class="table-wrap">
          <table class="coupon-table">
            <thead>
              <tr>
                <th>#</th>
                <th>會員</th>
                <th>類型</th>
                <th>折扣率</th>
                <th>說明</th>
                <th>狀態</th>
                <th>發放時間</th>
                <th>發放者</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="c in allCoupons" :key="c.id" :class="{ 'row-used': c.isUsed }">
                <td class="td-id">{{ c.id }}</td>
                <td>
                  <span class="user-name-cell">{{ c.userName }}</span>
                  <span class="user-id-cell">{{ c.userId }}</span>
                </td>
                <td><span class="type-tag" :class="couponTypeClass(c.couponType)">{{ couponTypeLabel(c.couponType) }}</span></td>
                <td>{{ c.discountRate ? Math.round(c.discountRate * 10) + '折' : '依全域設定' }}</td>
                <td class="td-desc">{{ c.description || '—' }}</td>
                <td>
                  <span v-if="c.isUsed" class="status-used">已使用</span>
                  <span v-else class="status-avail">可使用</span>
                </td>
                <td class="td-time">{{ formatDt(c.earnedAt) }}</td>
                <td class="td-by">{{ c.issuedBy || '—' }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      

    </div>
  </div>
</template>

<style scoped>
.discount-settings {
  min-height: 100vh;
  background: #fafafa;
}

.container {
  max-width: 960px;
  margin: 0 auto;
  padding: 3rem 2rem 5rem;
}

/* Header */
.page-header { margin-bottom: 2.5rem; }
.page-header h1 { font-size: 2rem; font-weight: 700; color: #1a1a1a; margin: 0 0 0.5rem; }
.subtitle { color: #666; margin: 0; font-size: 1rem; }

.loading-state { color: #888; font-size: 0.95rem; padding: 2rem 0; }
.section-gap { margin-top: 2rem; }

/* Cards */
.card {
  background: #fff;
  border-radius: 12px;
  padding: 2rem;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  margin-bottom: 1.5rem;
}

.card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 1.75rem;
}
.card-title { font-size: 1.1rem; font-weight: 700; color: #1a1a1a; margin: 0 0 0.3rem; }
.card-desc { font-size: 0.82rem; color: #888; margin: 0; }

/* Toggle */
.toggle-wrap {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  flex-shrink: 0;
}
.toggle-input { display: none; }
.toggle-slider {
  width: 40px; height: 22px;
  background: #ddd;
  border-radius: 11px;
  position: relative;
  transition: background 0.2s;
  flex-shrink: 0;
}
.toggle-slider::after {
  content: '';
  position: absolute;
  left: 3px; top: 3px;
  width: 16px; height: 16px;
  background: #fff;
  border-radius: 50%;
  transition: transform 0.2s;
}
.toggle-input:checked + .toggle-slider { background: #16a34a; }
.toggle-input:checked + .toggle-slider::after { transform: translateX(18px); }
.toggle-label { font-size: 0.82rem; color: #444; font-weight: 600; }

.disabled-hint { font-size: 0.82rem; color: #aaa; margin: 0; }

/* Fields */
.field-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
}
.field { display: flex; flex-direction: column; gap: 0.5rem; }
.field-full { grid-column: 1 / -1; }

.label { font-size: 0.82rem; font-weight: 600; color: #444; }

.input-wrap { display: flex; align-items: center; gap: 0.6rem; }
.input {
  width: 90px;
  padding: 0.55rem 0.75rem;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 0.95rem;
  color: #1a1a1a;
  background: #fff;
  outline: none;
  transition: border-color 0.2s;
}
.input:focus { border-color: #888; }
.input-full { width: 100%; box-sizing: border-box; }

.unit { font-size: 0.82rem; color: #888; }
.unit.discount-tag { color: #e63950; font-weight: 600; }
.hint { font-size: 0.75rem; color: #aaa; margin: 0; }

/* Target selector */
.target-row {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
}
.target-options {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}
.target-option {
  display: inline-flex;
  align-items: center;
  padding: 0.45rem 1rem;
  border-radius: 20px;
  border: 1.5px solid #ddd;
  font-size: 0.85rem;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
  user-select: none;
}
.target-option:hover { border-color: #999; color: #1a1a1a; }
.target-option.active { border-color: #1a1a1a; background: #1a1a1a; color: #fff; font-weight: 600; }
.radio-hidden { display: none; }

.batch-hint-field { display: flex; align-items: center; }
.batch-hint {
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  border-radius: 8px;
  padding: 0.65rem 1rem;
  font-size: 0.82rem;
  color: #166534;
  width: 100%;
}

/* Issue form */
.issue-form {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
  margin-bottom: 1.25rem;
}
.issue-actions {
  display: flex;
  align-items: center;
  gap: 1rem;
}

/* Notice */
.notice-card {
  background: #fff8f0;
  border: 1px solid #ffe0b2;
  border-radius: 12px;
  padding: 1.5rem 2rem;
  margin-bottom: 2rem;
}
.notice-title { font-size: 0.88rem; font-weight: 700; color: #b45309; margin: 0 0 0.75rem; }
.notice-list { margin: 0; padding-left: 1.25rem; display: flex; flex-direction: column; gap: 0.4rem; }
.notice-list li { font-size: 0.82rem; color: #78350f; line-height: 1.6; }

/* Actions */
.actions { display: flex; align-items: center; justify-content: flex-end; gap: 1rem; }
.msg { font-size: 0.85rem; margin: 0; }
.msg.success { color: #16a34a; }
.msg.error   { color: #dc2626; }

.save-btn {
  padding: 0.7rem 2rem;
  background: #1a1a1a;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}
.save-btn:hover:not(:disabled) { background: #333; }
.save-btn:disabled { background: #ccc; cursor: not-allowed; }

.refresh-btn {
  padding: 0.45rem 1rem;
  background: transparent;
  border: 1px solid #ddd;
  border-radius: 7px;
  font-size: 0.82rem;
  color: #555;
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;
}
.refresh-btn:hover:not(:disabled) { border-color: #999; color: #1a1a1a; }
.refresh-btn:disabled { color: #bbb; cursor: not-allowed; }

/* Table */
.table-wrap { overflow-x: auto; }
.coupon-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.82rem;
}
.coupon-table th {
  text-align: left;
  padding: 0.6rem 0.75rem;
  border-bottom: 2px solid #f0f0f0;
  color: #888;
  font-weight: 600;
  white-space: nowrap;
}
.coupon-table td {
  padding: 0.7rem 0.75rem;
  border-bottom: 1px solid #f5f5f5;
  color: #1a1a1a;
}
.row-used td { opacity: 0.5; }

.td-id { color: #bbb; font-size: 0.75rem; }
.td-desc { max-width: 140px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; color: #666; }
.td-time { white-space: nowrap; color: #888; }
.td-by { color: #888; }

.user-name-cell { display: block; font-weight: 600; }
.user-id-cell { display: block; font-size: 0.72rem; color: #aaa; }

.type-tag {
  display: inline-block;
  padding: 0.1rem 0.5rem;
  border-radius: 4px;
  font-size: 0.72rem;
  font-weight: 600;
}
.tag-coupon { background: #eff6ff; color: #2563eb; }
.tag-welcome { background: #f0fdf4; color: #16a34a; }
.tag-custom  { background: #fdf4ff; color: #9333ea; }

.status-avail { color: #16a34a; font-weight: 600; font-size: 0.78rem; }
.status-used  { color: #9ca3af; font-size: 0.78rem; }

.empty-state { text-align: center; padding: 2rem 0; color: #bbb; font-size: 0.88rem; }

/* Promo code */
.promo-create-form {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
  margin-bottom: 1.25rem;
}
.promo-list-wrap { overflow-x: auto; }
.promo-actions { display: flex; gap: 0.4rem; }
.action-sm {
  padding: 0.25rem 0.6rem;
  font-size: 0.75rem;
  border-radius: 5px;
  border: 1px solid #ddd;
  background: #fff;
  cursor: pointer;
  transition: all 0.15s;
  color: #555;
}
.action-sm:hover { border-color: #999; color: #1a1a1a; }
.action-sm.danger { color: #dc2626; border-color: #fca5a5; }
.action-sm.danger:hover { background: #fef2f2; }

@media (max-width: 600px) {
  .field-row, .issue-form, .promo-create-form { grid-template-columns: 1fr; gap: 1.25rem; }
}
</style>
