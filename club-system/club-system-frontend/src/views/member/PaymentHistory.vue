<template>
  <div class="payment-history-page">

    <main class="main-content">
      <div class="container">
        <div class="page-header">
          <h1>繳費歷史</h1>
          <p class="subtitle">查看您的所有繳費記錄</p>
        </div>

        <!-- 篩選器 -->
        <div class="filters">
          <select v-model="filterStatus" @change="applyFilter" class="filter-select">
            <option value="">全部狀態</option>
            <option value="PENDING">待付款</option>
            <option value="PAID">已付款</option>
            <option value="CANCELLED">已取消</option>
            <option value="REFUNDED">已退款</option>
          </select>

          <select v-model="filterType" @change="applyFilter" class="filter-select">
            <option value="">全部類型</option>
            <option value="ACTIVITY_FEE">活動費用</option>
            <option value="MEMBERSHIP_FEE">會費</option>
            <option value="MATERIAL_FEE">材料費</option>
            <option value="ANNUAL_FEE">年費</option>
            <option value="OTHER">其他</option>
          </select>
        </div>

        <div v-if="loading" class="loading">載入中...</div>

        <div v-else-if="errorMessage" class="error-message">
          {{ errorMessage }}
        </div>

        <div v-else-if="filteredPayments.length === 0" class="empty-state">
          <p>沒有找到符合條件的繳費記錄</p>
        </div>

        <div v-else class="payments-table">
          <table>
            <thead>
              <tr>
                <th>繳費編號</th>
                <th>類型</th>
                <th>金額</th>
                <th>狀態</th>
                <th>付款方式</th>
                <th>付款時間</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="payment in filteredPayments" :key="payment.id">
                <td>#{{ payment.id }}</td>
                <td>{{ getPaymentTypeText(payment.paymentType) }}</td>
                <td class="amount">NT$ {{ payment.amount }}</td>
                <td>
                  <span :class="['status-badge', getStatusClass(payment.status)]">
                    {{ getStatusText(payment.status) }}
                  </span>
                </td>
                <td>{{ getMethodText(payment.method) }}</td>
                <td>{{ formatDate(payment.paidAt) }}</td>
                <td>
                  <button @click="viewDetail(payment)" class="btn-detail">
                    詳情
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- 統計資訊 -->
        <div class="statistics">
          <div class="stat-card">
            <div class="stat-label">總繳費金額</div>
            <div class="stat-value">NT$ {{ totalPaid }}</div>
          </div>
          <div class="stat-card">
            <div class="stat-label">待繳金額</div>
            <div class="stat-value pending">NT$ {{ totalPending }}</div>
          </div>
          <div class="stat-card">
            <div class="stat-label">繳費次數</div>
            <div class="stat-value">{{ paidCount }} 次</div>
          </div>
        </div>
      </div>
    </main>

    <!-- 詳情 Modal -->
    <div v-if="showDetailModal" class="modal-overlay" @click="closeDetailModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h2>繳費詳情</h2>
          <button @click="closeDetailModal" class="btn-close">×</button>
        </div>

        <div class="modal-body" v-if="selectedPayment">
          <div class="detail-section">
            <h3>基本資訊</h3>
            <div class="detail-row">
              <span class="detail-label">繳費編號：</span>
              <span class="detail-value">#{{ selectedPayment.id }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">報名編號：</span>
              <span class="detail-value">#{{ selectedPayment.registrationId }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">費用類型：</span>
              <span class="detail-value">{{ getPaymentTypeText(selectedPayment.paymentType) }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">狀態：</span>
              <span :class="['status-badge', getStatusClass(selectedPayment.status)]">
                {{ getStatusText(selectedPayment.status) }}
              </span>
            </div>
          </div>

          <div class="detail-section">
            <h3>金額資訊</h3>
            <div class="detail-row" v-if="selectedPayment.originalAmount">
              <span class="detail-label">原價：</span>
              <span class="detail-value">NT$ {{ selectedPayment.originalAmount }}</span>
            </div>
            <div class="detail-row" v-if="selectedPayment.discountAmount > 0">
              <span class="detail-label">折扣：</span>
              <span class="detail-value discount">-NT$ {{ selectedPayment.discountAmount }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">實付金額：</span>
              <span class="detail-value amount">NT$ {{ selectedPayment.amount }}</span>
            </div>
            <div class="detail-row" v-if="selectedPayment.discountReason">
              <span class="detail-label">折扣原因：</span>
              <span class="detail-value">{{ selectedPayment.discountReason }}</span>
            </div>
          </div>

          <div class="detail-section" v-if="selectedPayment.status === 'PAID'">
            <h3>付款資訊</h3>
            <div class="detail-row">
              <span class="detail-label">付款方式：</span>
              <span class="detail-value">{{ getMethodText(selectedPayment.method) }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">付款時間：</span>
              <span class="detail-value">{{ formatDate(selectedPayment.paidAt) }}</span>
            </div>
            <div class="detail-row" v-if="selectedPayment.merchantTradeNo">
              <span class="detail-label">訂單編號：</span>
              <span class="detail-value">{{ selectedPayment.merchantTradeNo }}</span>
            </div>
            <div class="detail-row" v-if="selectedPayment.ecpayTradeNo">
              <span class="detail-label">綠界交易編號：</span>
              <span class="detail-value">{{ selectedPayment.ecpayTradeNo }}</span>
            </div>
          </div>

          <div class="detail-section" v-if="selectedPayment.note">
            <h3>備註</h3>
            <p class="note-text">{{ selectedPayment.note }}</p>
          </div>

          <div class="detail-section">
            <h3>時間記錄</h3>
            <div class="detail-row">
              <span class="detail-label">創建時間：</span>
              <span class="detail-value">{{ formatDate(selectedPayment.createdAt) }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">更新時間：</span>
              <span class="detail-value">{{ formatDate(selectedPayment.updatedAt) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getMyPayments } from '@/api/payment'

const router = useRouter()
const userStore = useUserStore()

const payments = ref([])
const loading = ref(true)
const errorMessage = ref('')

const filterStatus = ref('')
const filterType = ref('')

const showDetailModal = ref(false)
const selectedPayment = ref(null)

const loadPayments = async () => {
  try {
    loading.value = true
    errorMessage.value = ''
    const data = await getMyPayments()
    payments.value = data
  } catch (error) {
    console.error('載入繳費記錄失敗:', error)
    errorMessage.value = '載入繳費記錄失敗，請稍後再試'
  } finally {
    loading.value = false
  }
}

const filteredPayments = computed(() => {
  let result = payments.value

  if (filterStatus.value) {
    result = result.filter((p) => p.status === filterStatus.value)
  }

  if (filterType.value) {
    result = result.filter((p) => p.paymentType === filterType.value)
  }

  return result
})

const totalPaid = computed(() => {
  return payments.value
    .filter((p) => p.status === 'PAID')
    .reduce((sum, p) => sum + parseFloat(p.amount), 0)
    .toFixed(2)
})

const totalPending = computed(() => {
  return payments.value
    .filter((p) => p.status === 'PENDING')
    .reduce((sum, p) => sum + parseFloat(p.amount), 0)
    .toFixed(2)
})

const paidCount = computed(() => {
  return payments.value.filter((p) => p.status === 'PAID').length
})

const applyFilter = () => {
  // 篩選會自動透過 computed 更新
}

const viewDetail = (payment) => {
  selectedPayment.value = payment
  showDetailModal.value = true
}

const closeDetailModal = () => {
  showDetailModal.value = false
  selectedPayment.value = null
}

const handleLogout = () => {
  userStore.logout()
  router.push('/')
}

const getPaymentTypeText = (type) => {
  const types = {
    ACTIVITY_FEE: '活動費用',
    MEMBERSHIP_FEE: '會費',
    MATERIAL_FEE: '材料費',
    ANNUAL_FEE: '年費',
    OTHER: '其他'
  }
  return types[type] || type
}

const getStatusText = (status) => {
  const statuses = {
    PENDING: '待付款',
    PAID: '已付款',
    CANCELLED: '已取消',
    REFUNDED: '已退款',
    PARTIAL_REFUNDED: '部分退款'
  }
  return statuses[status] || status
}

const getStatusClass = (status) => {
  return status.toLowerCase()
}

const getMethodText = (method) => {
  if (!method) return '-'
  const methods = {
    CASH: '現金',
    BANK_TRANSFER: '銀行轉帳',
    CREDIT_CARD: '信用卡',
    LINE_PAY: 'LINE Pay',
    OTHER: '其他'
  }
  return methods[method] || method
}

const formatDate = (dateString) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleString('zh-TW', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  loadPayments()
})
</script>

<style scoped>
.payment-history-page {
  min-height: 100vh;
  background: #fafafa;
}

/* Navbar - 與 PendingPayments 相同 */




.logo {
  font-size: 1.25rem;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
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

/* Filters */
.filters {
  display: flex;
  gap: 1rem;
  margin-bottom: 2rem;
}

.filter-select {
  padding: 0.75rem 1rem;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  background: white;
  font-size: 1rem;
  cursor: pointer;
}

.filter-select:focus {
  outline: none;
  border-color: #1a1a1a;
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

/* Table */
.payments-table {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  overflow: hidden;
  margin-bottom: 2rem;
}

table {
  width: 100%;
  border-collapse: collapse;
}

thead {
  background: #f5f5f5;
}

th {
  padding: 1rem;
  text-align: left;
  font-weight: 600;
  color: #1a1a1a;
  border-bottom: 2px solid #e0e0e0;
}

td {
  padding: 1rem;
  border-bottom: 1px solid #e0e0e0;
}

tr:last-child td {
  border-bottom: none;
}

tr:hover {
  background: #fafafa;
}

.amount {
  font-weight: 600;
  color: #1a1a1a;
}

.status-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.875rem;
  font-weight: 500;
  display: inline-block;
}

.status-badge.pending {
  background: #fff3cd;
  color: #856404;
}

.status-badge.paid {
  background: #d4edda;
  color: #155724;
}

.status-badge.cancelled {
  background: #f8d7da;
  color: #721c24;
}

.status-badge.refunded {
  background: #d1ecf1;
  color: #0c5460;
}

.btn-detail {
  padding: 0.5rem 1rem;
  background: #1a1a1a;
  color: white;
  border: none;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-detail:hover {
  background: #333;
}

/* Statistics */
.statistics {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1.5rem;
}

.stat-card {
  background: white;
  padding: 1.5rem;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.stat-label {
  color: #666;
  font-size: 0.9rem;
  margin-bottom: 0.5rem;
}

.stat-value {
  font-size: 1.75rem;
  font-weight: 700;
  color: #1a1a1a;
}

.stat-value.pending {
  color: #dc3545;
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
  max-width: 600px;
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

.detail-section {
  margin-bottom: 2rem;
}

.detail-section:last-child {
  margin-bottom: 0;
}

.detail-section h3 {
  font-size: 1.125rem;
  font-weight: 600;
  margin: 0 0 1rem 0;
  color: #1a1a1a;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.75rem;
  padding-bottom: 0.75rem;
  border-bottom: 1px solid #f0f0f0;
}

.detail-row:last-child {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

.detail-label {
  color: #666;
  font-weight: 500;
}

.detail-value {
  color: #1a1a1a;
  font-weight: 500;
}

.detail-value.amount {
  font-size: 1.25rem;
  font-weight: 700;
}

.detail-value.discount {
  color: #28a745;
}

.note-text {
  color: #666;
  line-height: 1.6;
  margin: 0;
}

/* Responsive */
@media (max-width: 768px) {
  .filters {
    flex-direction: column;
  }

  .payments-table {
    overflow-x: auto;
  }

  table {
    min-width: 800px;
  }

  .statistics {
    grid-template-columns: 1fr;
  }
}
</style>
