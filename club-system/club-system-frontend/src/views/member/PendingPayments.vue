<template>
  <div class="pending-payments-page">
    <nav class="navbar">
      <div class="nav-container">
        <h2 class="logo">社團管理系統</h2>
        <div class="nav-right">
          <router-link to="/dashboard" class="nav-link">返回首頁</router-link>
          <button @click="handleLogout" class="btn-logout">登出</button>
        </div>
      </div>
    </nav>

    <main class="main-content">
      <div class="container">
        <div class="page-header">
          <h1>我的待繳款</h1>
          <p class="subtitle">查看並處理您的待繳費項目</p>
        </div>

        <div v-if="loading" class="loading">載入中...</div>

        <div v-else-if="errorMessage" class="error-message">
          {{ errorMessage }}
        </div>

        <div v-else-if="pendingPayments.length === 0" class="empty-state">
          <p>您目前沒有任何待繳款項目</p>
          <router-link to="/activity-registration-list-container" class="btn-primary">
            瀏覽活動
          </router-link>
        </div>

        <div v-else class="payments-grid">
          <div
            v-for="payment in pendingPayments"
            :key="payment.id"
            class="payment-card"
          >
            <div class="card-header">
              <span class="payment-type">{{ getPaymentTypeText(payment.paymentType) }}</span>
              <span :class="['status-badge', getStatusClass(payment.status)]">
                {{ getStatusText(payment.status) }}
              </span>
            </div>

            <div class="card-body">
              <div class="info-row">
                <span class="label">繳費編號：</span>
                <span class="value">#{{ payment.id }}</span>
              </div>

              <!-- registration 是一個物件，取其 id -->
              <div class="info-row">
                <span class="label">報名編號：</span>
                <span class="value">#{{ payment.registration?.id }}</span>
              </div>

              <div class="info-row" v-if="payment.originalAmount !== payment.amount">
                <span class="label">原價：</span>
                <span class="value original-amount">NT$ {{ payment.originalAmount }}</span>
              </div>

              <div class="info-row amount-row">
                <span class="label">應繳金額：</span>
                <span class="value amount">NT$ {{ payment.amount }}</span>
              </div>

              <div class="info-row" v-if="payment.discountAmount > 0">
                <span class="label">折扣：</span>
                <span class="value discount">-NT$ {{ payment.discountAmount }}</span>
              </div>

              <div class="info-row" v-if="payment.discountReason">
                <span class="label">折扣原因：</span>
                <span class="value">{{ payment.discountReason }}</span>
              </div>

              <div class="info-row" v-if="payment.paymentDeadline">
                <span class="label">繳費期限：</span>
                <span class="value deadline">{{ formatDate(payment.paymentDeadline) }}</span>
              </div>

              <div class="info-row" v-if="payment.note">
                <span class="label">備註：</span>
                <span class="value">{{ payment.note }}</span>
              </div>
            </div>

            <div class="card-footer">
              <template v-if="payment.status === 'PENDING'">
                <button @click="showPaymentModal(payment)" class="btn-pay">
                  前往繳費
                </button>
                <button @click="cancelPaymentConfirm(payment)" class="btn-cancel">
                  取消繳費
                </button>
              </template>
              <template v-else-if="payment.status === 'PROCESSING'">
                <span class="processing-hint">已進入付款流程，等待付款完成</span>
              </template>
              <template v-else-if="payment.status === 'PENDING_REVIEW'">
                <span class="review-hint">已選擇現金付款，等待管理員審核</span>
              </template>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- 繳費方式選擇 Modal -->
    <div v-if="showModal" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h2>選擇繳費方式</h2>
          <button @click="closeModal" class="btn-close">×</button>
        </div>

        <div class="modal-body">
          <div class="payment-info">
            <p><strong>繳費編號：</strong>#{{ selectedPayment?.id }}</p>
            <p><strong>應繳金額：</strong>NT$ {{ selectedPayment?.amount }}</p>
          </div>

          <div class="payment-methods">
            <button
              @click="selectPaymentMethod('credit_card')"
              class="method-btn"
            >
              <span class="method-icon">💳</span>
              <span class="method-name">信用卡</span>
            </button>

            <button
              @click="selectPaymentMethod('atm')"
              class="method-btn"
            >
              <span class="method-icon">🏧</span>
              <span class="method-name">ATM 轉帳</span>
            </button>

            <button
              @click="selectPaymentMethod('cvs')"
              class="method-btn"
            >
              <span class="method-icon">🏪</span>
              <span class="method-name">超商繳費</span>
            </button>

            <button
              @click="selectCashPayment"
              class="method-btn method-cash"
            >
              <span class="method-icon">💵</span>
              <span class="method-name">現金付款</span>
            </button>
          </div>

          <p class="payment-note">
            ⚠️ 線上付款將跳轉至綠界金流頁面<br>
            💵 現金付款需等待管理員審核
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  getMyPendingPayments,
  cancelPayment,
  createEcpayCheckout,
  selectCashPayment as selectCashPaymentAPI,
} from '@/api/payment'

const router = useRouter()
const userStore = useUserStore()

const pendingPayments = ref([])
const loading = ref(true)
const errorMessage = ref('')

const showModal = ref(false)
const selectedPayment = ref(null)

// ===== 載入資料 =====
const loadPendingPayments = async () => {
  try {
    loading.value = true
    errorMessage.value = ''
    // getMyPendingPayments 直接回傳 data（見 payment.js）
    pendingPayments.value = await getMyPendingPayments()
  } catch (error) {
    console.error('載入待繳費記錄失敗:', error)
    errorMessage.value = '載入待繳費記錄失敗，請稍後再試'
  } finally {
    loading.value = false
  }
}

// ===== Modal =====
const showPaymentModal = (payment) => {
  selectedPayment.value = payment
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
  selectedPayment.value = null
}

// ===== 選擇繳費方式（綠界金流）=====
const selectPaymentMethod = async (method) => {
  const choosePaymentMap = {
    credit_card: 'Credit',
    atm: 'ATM',
    cvs: 'CVS',
  }

  try {
    const checkoutData = {
      paymentId: selectedPayment.value.id,
      totalAmount: selectedPayment.value.amount,
      tradeDesc: getPaymentTypeText(selectedPayment.value.paymentType),
      itemName: `繳費 #${selectedPayment.value.id}`,
      choosePayment: choosePaymentMap[method],
    }

    const response = await createEcpayCheckout(checkoutData)
    submitEcpayForm(response)
  } catch (error) {
    console.error('創建綠界訂單失敗:', error)
    alert('創建付款訂單失敗，請稍後再試')
  }
}

// 建立隱藏表單並 POST 到綠界
const submitEcpayForm = (response) => {
  const form = document.createElement('form')
  form.method = 'POST'
  form.action = response.actionUrl

  const formData = {
    ...response.formData,
    CheckMacValue: response.checkMacValue,
  }

  Object.keys(formData).forEach((key) => {
    const input = document.createElement('input')
    input.type = 'hidden'
    input.name = key
    input.value = formData[key]
    form.appendChild(input)
  })

  document.body.appendChild(form)
  form.submit()
}

// ===== 取消繳費 =====
const cancelPaymentConfirm = async (payment) => {
  if (!confirm('確定要取消此繳費嗎？')) return

  try {
    await cancelPayment(payment.id, '用戶取消')
    alert('繳費已取消')
    await loadPendingPayments()
  } catch (error) {
    console.error('取消繳費失敗:', error)
    alert(error.response?.data?.message || '取消失敗，請稍後再試')
  }
}

// ===== 現金付款 =====
const selectCashPayment = async () => {
  if (!confirm('選擇現金付款後，需要等待管理員審核。確定要繼續嗎？')) {
    return
  }

  try {
    await selectCashPaymentAPI(selectedPayment.value.id, {
      note: '用戶選擇現金付款'
    })
    alert('已選擇現金付款，請找管理員確認並完成付款。\n狀態將變更為「審核中」。')
    closeModal()
    await loadPendingPayments()
  } catch (error) {
    console.error('選擇現金付款失敗:', error)
    alert(error.response?.data?.message || '操作失敗，請稍後再試')
  }
}

// ===== 登出 =====
const handleLogout = () => {
  userStore.logout()
  router.push('/')
}

// ===== 輔助函式 =====
const getPaymentTypeText = (type) => {
  const types = {
    ACTIVITY_FEE: '活動費用',
    MEMBERSHIP_FEE: '會費',
    MATERIAL_FEE: '材料費',
    ANNUAL_FEE: '年費',
    OTHER: '其他',
  }
  return types[type] || type || '費用'
}

const getStatusText = (status) => {
  const statuses = {
    PENDING: '待付款',
    PROCESSING: '付款中',
    PENDING_REVIEW: '審核中',
    PAID: '已付款',
    CANCELLED: '已取消',
    REFUNDED: '已退款',
    PARTIAL_REFUNDED: '部分退款',
  }
  return statuses[status] || status
}

const getStatusClass = (status) => {
  return status?.toLowerCase() ?? ''
}

const formatDate = (dateString) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleString('zh-TW', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

onMounted(() => {
  loadPendingPayments()
})
</script>


<style scoped>
.pending-payments-page {
  min-height: 100vh;
  background: #fafafa;
}

/* Navbar */
.navbar {
  background: white;
  border-bottom: 1px solid #e0e0e0;
  padding: 1rem 0;
}

.nav-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  font-size: 1.25rem;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}

.nav-right {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.nav-link {
  color: #666;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.2s;
}

.nav-link:hover {
  color: #1a1a1a;
}

.btn-logout {
  padding: 0.5rem 1.25rem;
  background: white;
  color: #1a1a1a;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-logout:hover {
  background: #f5f5f5;
  border-color: #ccc;
}

/* Main Content */
.main-content {
  padding: 3rem 0;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
}

.page-header {
  margin-bottom: 2rem;
}

.page-header h1 {
  font-size: 2rem;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 0.5rem 0;
}

.subtitle {
  color: #666;
  margin: 0;
}

.loading {
  text-align: center;
  padding: 3rem;
  color: #666;
}

.error-message {
  padding: 1rem;
  background: #fee;
  border: 1px solid #fcc;
  border-radius: 6px;
  color: #c33;
  margin-bottom: 1.5rem;
}

.empty-state {
  text-align: center;
  padding: 4rem 2rem;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.empty-state p {
  color: #666;
  margin-bottom: 1.5rem;
}

.btn-primary {
  display: inline-block;
  padding: 0.75rem 1.5rem;
  background: #1a1a1a;
  color: white;
  text-decoration: none;
  border-radius: 6px;
  font-weight: 500;
  transition: background 0.2s;
}

.btn-primary:hover {
  background: #333;
}

/* Payments Grid */
.payments-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 1.5rem;
}

.payment-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  overflow: hidden;
  transition: box-shadow 0.2s;
}

.payment-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  padding: 1.25rem;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.payment-type {
  font-weight: 600;
  color: #1a1a1a;
}

.status-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.875rem;
  font-weight: 500;
}

.status-badge.pending {
  background: #fff3cd;
  color: #856404;
}

.status-badge.pending_review {
  background: #d1ecf1;
  color: #0c5460;
}

.status-badge.processing {
  background: #f3e5f5;
  color: #7b1fa2;
}

.status-badge.paid {
  background: #d4edda;
  color: #155724;
}

.status-badge.cancelled {
  background: #f8d7da;
  color: #721c24;
}

.card-body {
  padding: 1.25rem;
}

.info-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.75rem;
}

.info-row:last-child {
  margin-bottom: 0;
}

.label {
  color: #666;
  font-size: 0.9rem;
}

.value {
  color: #1a1a1a;
  font-weight: 500;
}

.original-amount {
  text-decoration: line-through;
  color: #999;
}

.amount-row .amount {
  font-size: 1.25rem;
  color: #1a1a1a;
  font-weight: 700;
}

.discount {
  color: #28a745;
}

.deadline {
  color: #dc3545;
}

.card-footer {
  padding: 1.25rem;
  border-top: 1px solid #e0e0e0;
  display: flex;
  gap: 0.75rem;
}

.btn-pay {
  flex: 1;
  padding: 0.75rem;
  background: #1a1a1a;
  color: white;
  border: none;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-pay:hover {
  background: #333;
}

.btn-cancel {
  padding: 0.75rem 1.25rem;
  background: white;
  color: #666;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-cancel:hover {
  background: #f5f5f5;
  border-color: #ccc;
}

.processing-hint,
.review-hint {
  width: 100%;
  text-align: center;
  color: #666;
  font-size: 0.9rem;
  padding: 0.5rem 0;
}

/* Modal */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 8px;
  max-width: 500px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  padding: 1.5rem;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h2 {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 600;
}

.btn-close {
  background: none;
  border: none;
  font-size: 2rem;
  color: #666;
  cursor: pointer;
  line-height: 1;
  padding: 0;
  width: 2rem;
  height: 2rem;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-close:hover {
  color: #1a1a1a;
}

.modal-body {
  padding: 1.5rem;
}

.payment-info {
  background: #f5f5f5;
  padding: 1rem;
  border-radius: 6px;
  margin-bottom: 1.5rem;
}

.payment-info p {
  margin: 0.5rem 0;
}

.payment-methods {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
  margin-bottom: 1rem;
}

.method-btn {
  padding: 1.5rem 1rem;
  background: white;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
}

.method-btn:hover {
  border-color: #1a1a1a;
  background: #fafafa;
}

.method-cash {
  grid-column: span 3;
  border-color: #28a745;
}

.method-cash:hover {
  border-color: #218838;
  background: #f0f8f4;
}

.method-icon {
  font-size: 2rem;
}

.method-name {
  font-weight: 500;
  color: #1a1a1a;
  font-size: 0.85rem;
}

.payment-note {
  margin-top: 1.25rem;
  font-size: 0.82rem;
  color: #888;
  text-align: center;
}

/* Responsive */
@media (max-width: 768px) {
  .payments-grid {
    grid-template-columns: 1fr;
  }

  .payment-methods {
    grid-template-columns: 1fr;
  }

  .card-footer {
    flex-direction: column;
  }

  .btn-pay,
  .btn-cancel {
    width: 100%;
  }
}
</style>