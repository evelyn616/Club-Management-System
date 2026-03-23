<template>
  <div class="payment-management">

    <main class="main-content">
      <div class="container">

        <!-- 繳費統計 -->
        <section class="section">
          <div class="section-header">
            <h2>繳費統計</h2>
            <button @click="loadStats" class="btn-refresh">🔄</button>
          </div>
          <div v-if="loadingStats" class="loading">載入中...</div>
          <div v-else class="stats-grid">
            <div class="stat-card stat-green">
              <div class="stat-value">NT$ {{ formatAmount(stats.totalPaid) }}</div>
              <div class="stat-label">已收金額</div>
            </div>
            <div class="stat-card stat-orange">
              <div class="stat-value">NT$ {{ formatAmount(stats.totalPending) }}</div>
              <div class="stat-label">待繳金額</div>
            </div>
            <div class="stat-card stat-blue">
              <div class="stat-value">{{ stats.countPaid || 0 }}</div>
              <div class="stat-label">已繳筆數</div>
            </div>
            <div class="stat-card stat-yellow">
              <div class="stat-value">{{ stats.countPending || 0 }}</div>
              <div class="stat-label">待繳筆數</div>
            </div>
            <div class="stat-card stat-gray">
              <div class="stat-value">{{ stats.countCancelled || 0 }}</div>
              <div class="stat-label">已取消</div>
            </div>
          </div>
        </section>

        <!-- 待審核現金付款 -->
        <section class="section">
          <div class="section-header">
            <h2>待審核現金付款</h2>
            <button @click="loadPendingReview" class="btn-refresh">🔄 重新整理</button>
          </div>
          <div v-if="loadingPending" class="loading">載入中...</div>
          <div v-else-if="pendingPayments.length === 0" class="empty-state">
            <p>目前沒有待審核的現金付款</p>
          </div>
          <div v-else class="payments-table">
            <table>
              <thead>
                <tr>
                  <th>編號</th>
                  <th>繳費人</th>
                  <th>費用類型</th>
                  <th>金額</th>
                  <th>申請時間</th>
                  <th>備註</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="p in pendingPayments" :key="p.id">
                  <td>#{{ p.id }}</td>
                  <td>{{ p.userName || '-' }}</td>
                  <td>{{ getPaymentTypeText(p.paymentType) }}</td>
                  <td class="amount">NT$ {{ p.amount }}</td>
                  <td>{{ formatDate(p.createdAt) }}</td>
                  <td>{{ p.note || '-' }}</td>
                  <td class="actions">
                    <button @click="showApproveModal(p)" class="btn-approve">✓ 通過</button>
                    <button @click="showRejectModal(p)" class="btn-reject">✗ 拒絕</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>

        <!-- 所有付款記錄 -->
        <section class="section">
          <div class="section-header">
            <h2>所有付款記錄</h2>
            <button @click="loadAllPayments" class="btn-refresh">🔄 重新整理</button>
          </div>

          <!-- 篩選列 -->
          <div class="filter-bar">
            <div class="filter-group">
              <label>狀態</label>
              <select v-model="filterStatus" @change="loadAllPayments">
                <option value="">全部</option>
                <option value="PENDING">待繳費</option>
                <option value="PROCESSING">付款中</option>
                <option value="PENDING_REVIEW">審核中</option>
                <option value="PAID">已繳費</option>
                <option value="CANCELLED">已取消</option>
                <option value="REFUNDED">已退款</option>
              </select>
            </div>
            <div class="filter-group">
              <label>付款方式</label>
              <select v-model="filterMethod" @change="loadAllPayments">
                <option value="">全部</option>
                <option value="CASH">現金</option>
                <option value="CREDIT_CARD">信用卡</option>
                <option value="BANK_TRANSFER">銀行轉帳</option>
                <option value="CVS">超商代碼</option>
                <option value="BARCODE">超商條碼</option>
              </select>
            </div>
            <div class="filter-group">
              <label>搜尋</label>
              <input
                v-model="searchKeyword"
                type="text"
                placeholder="姓名 / 編號"
              />
            </div>
          </div>

          <div v-if="loadingAll" class="loading">載入中...</div>
          <div v-else-if="filteredPayments.length === 0" class="empty-state">
            <p>沒有符合條件的付款記錄</p>
          </div>
          <div v-else class="payments-table">
            <table>
              <thead>
                <tr>
                  <th>編號</th>
                  <th>繳費人</th>
                  <th>費用類型</th>
                  <th>金額</th>
                  <th>狀態</th>
                  <th>付款方式</th>
                  <th>付款時間</th>
                  <th>建立時間</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="p in filteredPayments" :key="p.id">
                  <td>#{{ p.id }}</td>
                  <td>{{ p.userName || '-' }}</td>
                  <td>{{ getPaymentTypeText(p.paymentType) }}</td>
                  <td class="amount">NT$ {{ p.amount }}</td>
                  <td>
                    <span :class="['status-badge', getStatusClass(p.status)]">
                      {{ getStatusText(p.status) }}
                    </span>
                  </td>
                  <td>{{ getMethodText(p.method) }}</td>
                  <td>{{ formatDate(p.paidAt) }}</td>
                  <td>{{ formatDate(p.createdAt) }}</td>
                  <td>
                    <button
                      v-if="p.merchantTradeNo"
                      @click="handleQueryEcpay(p)"
                      class="btn-query"
                      :disabled="queryingId === p.id"
                    >
                      {{ queryingId === p.id ? '查詢中...' : '查詢綠界' }}
                    </button>
                    <span v-else class="text-muted">-</span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>

      </div>
    </main>

    <!-- 審核通過 Modal -->
    <div v-if="showApproveDialog" class="modal-overlay" @click="closeModals">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h2>審核通過</h2>
          <button @click="closeModals" class="btn-close">&times;</button>
        </div>
        <div class="modal-body">
          <p>繳費編號：#{{ selectedPayment?.id }}</p>
          <p>繳費人：{{ selectedPayment?.userName }}</p>
          <p>金額：NT$ {{ selectedPayment?.amount }}</p>
          <div class="form-group">
            <label>審核備註（可選）</label>
            <textarea v-model="approveNote" placeholder="例如：已確認收到現金" rows="3"></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="closeModals" class="btn-cancel">取消</button>
          <button @click="confirmApprove" class="btn-confirm">確認通過</button>
        </div>
      </div>
    </div>

    <!-- 拒絕 Modal -->
    <div v-if="showRejectDialog" class="modal-overlay" @click="closeModals">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h2>拒絕審核</h2>
          <button @click="closeModals" class="btn-close">&times;</button>
        </div>
        <div class="modal-body">
          <p>繳費編號：#{{ selectedPayment?.id }}</p>
          <p>繳費人：{{ selectedPayment?.userName }}</p>
          <p>金額：NT$ {{ selectedPayment?.amount }}</p>
          <div class="form-group">
            <label>拒絕原因（必填）</label>
            <textarea v-model="rejectReason" placeholder="請說明拒絕原因" rows="3" required></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="closeModals" class="btn-cancel">取消</button>
          <button @click="confirmReject" class="btn-confirm btn-danger" :disabled="!rejectReason">確認拒絕</button>
        </div>
      </div>
    </div>

    <!-- 綠界查詢結果 Modal -->
    <div v-if="showEcpayResult" class="modal-overlay" @click="showEcpayResult = false">
      <div class="modal-content modal-wide" @click.stop>
        <div class="modal-header">
          <h2>綠界訂單查詢結果</h2>
          <button @click="showEcpayResult = false" class="btn-close">&times;</button>
        </div>
        <div class="modal-body">
          <div class="query-result">
            <div class="result-row">
              <span class="result-label">繳費編號</span>
              <span class="result-value">#{{ ecpayResult.paymentId }}</span>
            </div>
            <div class="result-row">
              <span class="result-label">綠界訂單編號</span>
              <span class="result-value">{{ ecpayResult.merchantTradeNo }}</span>
            </div>
            <div class="result-row">
              <span class="result-label">綠界交易編號</span>
              <span class="result-value">{{ ecpayResult.ecpayTradeNo || '-' }}</span>
            </div>
            <div class="result-row">
              <span class="result-label">綠界付款狀態</span>
              <span :class="['result-value', ecpayResult.ecpayTradeStatus === '1' ? 'text-success' : 'text-warning']">
                {{ ecpayResult.ecpayTradeStatusText }}
              </span>
            </div>
            <div class="result-row">
              <span class="result-label">本地狀態</span>
              <span class="result-value">
                <span :class="['status-badge', getStatusClass(ecpayResult.localStatus)]">
                  {{ getStatusText(ecpayResult.localStatus) }}
                </span>
              </span>
            </div>
            <div class="result-row">
              <span class="result-label">交易金額</span>
              <span class="result-value">NT$ {{ ecpayResult.tradeAmt || '-' }}</span>
            </div>
            <div class="result-row">
              <span class="result-label">付款方式</span>
              <span class="result-value">{{ ecpayResult.paymentType || '-' }}</span>
            </div>
            <div class="result-row">
              <span class="result-label">付款時間</span>
              <span class="result-value">{{ ecpayResult.paymentDate || '-' }}</span>
            </div>
            <div v-if="ecpayResult.synced" class="sync-notice">
              ✓ 已自動同步：綠界顯示已付款，本地狀態已更新為「已繳費」
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="showEcpayResult = false" class="btn-cancel">關閉</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  getPendingReviewPayments,
  approveCashPayment,
  rejectCashPayment,
  getAllPayments,
  getPaymentStats,
  queryEcpayStatus
} from '@/api/payment'

const router = useRouter()
const userStore = useUserStore()

// 統計
const stats = ref({})
const loadingStats = ref(false)

// 待審核
const pendingPayments = ref([])
const loadingPending = ref(false)

// 所有記錄
const allPayments = ref([])
const loadingAll = ref(false)
const filterStatus = ref('')
const filterMethod = ref('')
const searchKeyword = ref('')

// Modal
const showApproveDialog = ref(false)
const showRejectDialog = ref(false)
const selectedPayment = ref(null)
const approveNote = ref('')
const rejectReason = ref('')

// 綠界查詢
const showEcpayResult = ref(false)
const ecpayResult = ref({})
const queryingId = ref(null)

// 前端搜尋過濾
const filteredPayments = computed(() => {
  if (!searchKeyword.value.trim()) return allPayments.value
  const kw = searchKeyword.value.trim().toLowerCase()
  return allPayments.value.filter(p =>
    (p.userName && p.userName.toLowerCase().includes(kw)) ||
    String(p.id).includes(kw)
  )
})

const loadStats = async () => {
  try {
    loadingStats.value = true
    stats.value = await getPaymentStats()
  } catch (e) {
    console.error('載入統計失敗:', e)
  } finally {
    loadingStats.value = false
  }
}

const loadPendingReview = async () => {
  try {
    loadingPending.value = true
    pendingPayments.value = await getPendingReviewPayments()
  } catch (e) {
    console.error('載入待審核列表失敗:', e)
    alert('載入失敗，請稍後再試')
  } finally {
    loadingPending.value = false
  }
}

const loadAllPayments = async () => {
  try {
    loadingAll.value = true
    const params = {}
    if (filterStatus.value) params.status = filterStatus.value
    if (filterMethod.value) params.method = filterMethod.value
    allPayments.value = await getAllPayments(params)
  } catch (e) {
    console.error('載入付款記錄失敗:', e)
    alert('載入失敗，請稍後再試')
  } finally {
    loadingAll.value = false
  }
}

const showApproveModal = (payment) => {
  selectedPayment.value = payment
  approveNote.value = ''
  showApproveDialog.value = true
}

const showRejectModal = (payment) => {
  selectedPayment.value = payment
  rejectReason.value = ''
  showRejectDialog.value = true
}

const closeModals = () => {
  showApproveDialog.value = false
  showRejectDialog.value = false
  selectedPayment.value = null
  approveNote.value = ''
  rejectReason.value = ''
}

const confirmApprove = async () => {
  try {
    await approveCashPayment(selectedPayment.value.id, approveNote.value)
    alert('審核通過！')
    closeModals()
    await Promise.all([loadPendingReview(), loadAllPayments(), loadStats()])
  } catch (e) {
    console.error('審核失敗:', e)
    alert(e.response?.data?.message || '審核失敗')
  }
}

const confirmReject = async () => {
  if (!rejectReason.value.trim()) {
    alert('請填寫拒絕原因')
    return
  }
  try {
    await rejectCashPayment(selectedPayment.value.id, rejectReason.value)
    alert('已拒絕審核')
    closeModals()
    await Promise.all([loadPendingReview(), loadAllPayments(), loadStats()])
  } catch (e) {
    console.error('拒絕失敗:', e)
    alert(e.response?.data?.message || '操作失敗')
  }
}

const handleLogout = () => {
  if (confirm('確定要登出嗎？')) {
    userStore.logout()
    router.push('/')
  }
}

// 查詢綠界訂單狀態
const handleQueryEcpay = async (payment) => {
  try {
    queryingId.value = payment.id
    const result = await queryEcpayStatus(payment.id)
    ecpayResult.value = result
    showEcpayResult.value = true

    // 如果有同步，重新載入列表和統計
    if (result.synced) {
      await Promise.all([loadAllPayments(), loadStats()])
    }
  } catch (e) {
    console.error('查詢綠界失敗:', e)
    alert(e.response?.data?.message || '查詢綠界訂單失敗')
  } finally {
    queryingId.value = null
  }
}

// 輔助函式
const getPaymentTypeText = (type) => {
  const map = { ACTIVITY_FEE: '活動費用', MEMBERSHIP_FEE: '會費', MATERIAL_FEE: '材料費', ANNUAL_FEE: '年費', OTHER: '其他' }
  return map[type] || type || '-'
}

const getStatusText = (status) => {
  const map = { PENDING: '待繳費', PROCESSING: '付款中', PENDING_REVIEW: '審核中', PAID: '已繳費', CANCELLED: '已取消', REFUNDED: '已退款', PARTIAL_REFUNDED: '部分退款', NOT_REQUIRED: '無須繳費' }
  return map[status] || status || '-'
}

const getStatusClass = (status) => {
  const map = { PENDING: 'status-pending', PROCESSING: 'status-processing', PENDING_REVIEW: 'status-review', PAID: 'status-paid', CANCELLED: 'status-cancelled', REFUNDED: 'status-refunded' }
  return map[status] || ''
}

const getMethodText = (method) => {
  const map = { CASH: '現金', BANK_TRANSFER: '銀行轉帳', CREDIT_CARD: '信用卡', CVS: '超商代碼', BARCODE: '超商條碼', LINE_PAY: 'LINE Pay', OTHER: '其他' }
  return map[method] || method || '-'
}

const formatDate = (dateString) => {
  if (!dateString) return '-'
  const d = new Date(dateString)
  return d.toLocaleString('zh-TW', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

const formatAmount = (val) => {
  if (val == null) return '0'
  return Number(val).toLocaleString()
}

onMounted(() => {
  loadStats()
  loadPendingReview()
  loadAllPayments()
})
</script>

<style scoped>
.payment-management {
  min-height: 100vh;
  background: #fafafa;
}

<<<<<<< HEAD
/* 導航列 */
=======
.admin-navbar {
  background: white;
  border-bottom: 1px solid #e0e0e0;
  padding: 1rem 0;
  position: sticky;
  top: 0;
  z-index: 100;
}
>>>>>>> d7e923a03892427eed4a3e6fba0a38f23ee21e9b








.nav-link {
  color: #666;
  text-decoration: none;
  font-weight: 500;
}

.nav-link:hover {
  color: #1a1a1a;
}

<<<<<<< HEAD



=======
.btn-logout {
  padding: 0.5rem 1.25rem;
  background: white;
  color: #1a1a1a;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
}

.btn-logout:hover {
  background: #f5f5f5;
}
>>>>>>> d7e923a03892427eed4a3e6fba0a38f23ee21e9b

.main-content {
  padding: 2rem 0;
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 2rem;
}

.section {
  background: white;
  border-radius: 12px;
  padding: 1.5rem 2rem;
  margin-bottom: 1.5rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.25rem;
  padding-bottom: 0.75rem;
  border-bottom: 2px solid #f0f0f0;
}

.section-header h2 {
  margin: 0;
  font-size: 1.35rem;
  font-weight: 600;
  color: #1a1a1a;
}

.btn-refresh {
  padding: 0.4rem 0.8rem;
  background: white;
  color: #666;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  cursor: pointer;
}

.btn-refresh:hover {
  background: #f5f5f5;
}

/* 統計卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 1rem;
}

.stat-card {
  padding: 1.25rem;
  border-radius: 10px;
  text-align: center;
}

.stat-value {
  font-size: 1.5rem;
  font-weight: 700;
  margin-bottom: 0.25rem;
}

.stat-label {
  font-size: 0.9rem;
  opacity: 0.8;
}

.stat-green {
  background: #e8f5e9;
  color: #2e7d32;
}

.stat-orange {
  background: #fff3e0;
  color: #e65100;
}

.stat-blue {
  background: #e3f2fd;
  color: #1565c0;
}

.stat-yellow {
  background: #fffde7;
  color: #f57f17;
}

.stat-gray {
  background: #f5f5f5;
  color: #616161;
}

/* 篩選列 */
.filter-bar {
  display: flex;
  gap: 1rem;
  margin-bottom: 1.25rem;
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.filter-group label {
  font-size: 0.8rem;
  color: #666;
  font-weight: 500;
}

.filter-group select,
.filter-group input {
  padding: 0.5rem 0.75rem;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 0.9rem;
  min-width: 140px;
}

.filter-group select:focus,
.filter-group input:focus {
  outline: none;
  border-color: #667eea;
}

/* 表格 */
.payments-table {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
}

thead {
  background: #fafafa;
}

th {
  padding: 0.75rem 1rem;
  text-align: left;
  font-weight: 600;
  color: #666;
  font-size: 0.85rem;
  border-bottom: 2px solid #e0e0e0;
  white-space: nowrap;
}

td {
  padding: 0.75rem 1rem;
  border-bottom: 1px solid #f0f0f0;
  color: #333;
  font-size: 0.9rem;
}

.amount {
  font-weight: 600;
  color: #1a1a1a;
}

.actions {
  display: flex;
  gap: 0.5rem;
}

.btn-approve,
.btn-reject {
  padding: 0.4rem 0.75rem;
  border: none;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  font-size: 0.85rem;
}

.btn-approve {
  background: #28a745;
  color: white;
}

.btn-approve:hover {
  background: #218838;
}

.btn-reject {
  background: #dc3545;
  color: white;
}

.btn-reject:hover {
  background: #c82333;
}

/* 狀態徽章 */
.status-badge {
  display: inline-block;
  padding: 0.2rem 0.6rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 500;
  white-space: nowrap;
}

.status-pending {
  background: #fff3e0;
  color: #e65100;
}

.status-review {
  background: #e3f2fd;
  color: #1565c0;
}

.status-processing {
  background: #f3e5f5;
  color: #7b1fa2;
}

.status-paid {
  background: #e8f5e9;
  color: #2e7d32;
}

.status-cancelled {
  background: #f5f5f5;
  color: #616161;
}

.status-refunded {
  background: #fce4ec;
  color: #c62828;
}

.loading {
  text-align: center;
  padding: 2rem;
  color: #666;
}

.empty-state {
  text-align: center;
  padding: 2rem;
  color: #999;
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
  border-radius: 12px;
  max-width: 480px;
  width: 90%;
}

.modal-header {
  padding: 1.25rem 1.5rem;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h2 {
  margin: 0;
  font-size: 1.25rem;
}

.btn-close {
  background: none;
  border: none;
  font-size: 1.75rem;
  color: #666;
  cursor: pointer;
  line-height: 1;
}

.modal-body {
  padding: 1.5rem;
}

.modal-body p {
  margin: 0.4rem 0;
  color: #333;
}

.form-group {
  margin-top: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.4rem;
  font-weight: 500;
  color: #333;
  font-size: 0.9rem;
}

.form-group textarea {
  width: 100%;
  padding: 0.6rem;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-family: inherit;
  font-size: 0.9rem;
  resize: vertical;
  box-sizing: border-box;
}

.form-group textarea:focus {
  outline: none;
  border-color: #667eea;
}

.modal-footer {
  padding: 1rem 1.5rem;
  border-top: 1px solid #e0e0e0;
  display: flex;
  gap: 0.75rem;
  justify-content: flex-end;
}

.btn-cancel {
  padding: 0.6rem 1.25rem;
  background: white;
  color: #666;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  cursor: pointer;
}

.btn-confirm {
  padding: 0.6rem 1.25rem;
  background: #28a745;
  color: white;
  border: none;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
}

.btn-confirm:hover:not(:disabled) {
  background: #218838;
}

.btn-confirm:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.btn-danger {
  background: #dc3545;
}

.btn-danger:hover:not(:disabled) {
  background: #c82333;
}

.btn-query {
  padding: 0.35rem 0.7rem;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 0.8rem;
  font-weight: 500;
  cursor: pointer;
  white-space: nowrap;
}

.btn-query:hover:not(:disabled) {
  background: #5568d3;
}

.btn-query:disabled {
  background: #aaa;
  cursor: wait;
}

.text-muted {
  color: #999;
  font-size: 0.85rem;
}

/* 查詢結果 */
.modal-wide {
  max-width: 560px;
}

.query-result {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.result-row {
  display: flex;
  justify-content: space-between;
  padding: 0.6rem 0;
  border-bottom: 1px solid #f0f0f0;
}

.result-label {
  color: #666;
  font-weight: 500;
  font-size: 0.9rem;
}

.result-value {
  color: #1a1a1a;
  font-weight: 500;
  font-size: 0.9rem;
}

.text-success {
  color: #2e7d32;
}

.text-warning {
  color: #e65100;
}

.sync-notice {
  margin-top: 1rem;
  padding: 0.75rem 1rem;
  background: #e8f5e9;
  color: #2e7d32;
  border-radius: 8px;
  font-size: 0.9rem;
  font-weight: 500;
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .filter-bar {
    flex-direction: column;
  }

  .actions {
    flex-direction: column;
  }

  .nav-container,
  .container {
    padding: 0 1rem;
  }
}
</style>
