<template>
  <div class="payment-management">
    <!-- 頂部導航 -->
    <nav class="admin-navbar">
      <div class="nav-container">
        <h2 class="logo">繳費管理</h2>
        <div class="nav-right">
          <router-link to="/admin/dashboard" class="nav-link">返回後台</router-link>
          <button @click="handleLogout" class="btn-logout">登出</button>
        </div>
      </div>
    </nav>

    <!-- 主要內容 -->
    <main class="main-content">
      <div class="container">
        <!-- 待審核區塊 -->
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
                  <th>繳費編號</th>
                  <th>報名編號</th>
                  <th>費用類型</th>
                  <th>金額</th>
                  <th>申請時間</th>
                  <th>備註</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="payment in pendingPayments" :key="payment.id">
                  <td>#{{ payment.id }}</td>
                  <td>#{{ payment.registration?.id }}</td>
                  <td>{{ getPaymentTypeText(payment.paymentType) }}</td>
                  <td class="amount">NT$ {{ payment.amount }}</td>
                  <td>{{ formatDate(payment.createdAt) }}</td>
                  <td>{{ payment.note || '-' }}</td>
                  <td class="actions">
                    <button
                      @click="showApproveModal(payment)"
                      class="btn-approve"
                    >
                      ✓ 通過
                    </button>
                    <button
                      @click="showRejectModal(payment)"
                      class="btn-reject"
                    >
                      ✗ 拒絕
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>

        <!-- 其他功能區塊（預留） -->
        <section class="section">
          <div class="section-header">
            <h2>其他功能</h2>
          </div>
          <div class="feature-grid">
            <div class="feature-box">
              <h3>查詢綠界訂單</h3>
              <p>查詢信用卡、ATM、超商付款狀態</p>
              <button class="btn-secondary" disabled>開發中</button>
            </div>
            <div class="feature-box">
              <h3>所有付款記錄</h3>
              <p>查看和篩選所有繳費記錄</p>
              <button class="btn-secondary" disabled>開發中</button>
            </div>
            <div class="feature-box">
              <h3>繳費統計</h3>
              <p>查看收入統計和報表</p>
              <button class="btn-secondary" disabled>開發中</button>
            </div>
          </div>
        </section>
      </div>
    </main>

    <!-- 審核通過 Modal -->
    <div v-if="showApproveDialog" class="modal-overlay" @click="closeModals">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h2>審核通過</h2>
          <button @click="closeModals" class="btn-close">×</button>
        </div>
        <div class="modal-body">
          <p><strong>繳費編號：</strong>#{{ selectedPayment?.id }}</p>
          <p><strong>金額：</strong>NT$ {{ selectedPayment?.amount }}</p>
          
          <div class="form-group">
            <label>審核備註（可選）</label>
            <textarea
              v-model="approveNote"
              placeholder="例如：已確認收到現金"
              rows="3"
            ></textarea>
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
          <button @click="closeModals" class="btn-close">×</button>
        </div>
        <div class="modal-body">
          <p><strong>繳費編號：</strong>#{{ selectedPayment?.id }}</p>
          <p><strong>金額：</strong>NT$ {{ selectedPayment?.amount }}</p>
          
          <div class="form-group">
            <label>拒絕原因（必填）</label>
            <textarea
              v-model="rejectReason"
              placeholder="請說明拒絕原因"
              rows="3"
              required
            ></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="closeModals" class="btn-cancel">取消</button>
          <button
            @click="confirmReject"
            class="btn-confirm btn-danger"
            :disabled="!rejectReason"
          >
            確認拒絕
          </button>
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
  getPendingReviewPayments,
  approveCashPayment,
  rejectCashPayment
} from '@/api/payment'

const router = useRouter()
const userStore = useUserStore()

const pendingPayments = ref([])
const loadingPending = ref(false)

const showApproveDialog = ref(false)
const showRejectDialog = ref(false)
const selectedPayment = ref(null)
const approveNote = ref('')
const rejectReason = ref('')

// 載入待審核列表
const loadPendingReview = async () => {
  try {
    loadingPending.value = true
    pendingPayments.value = await getPendingReviewPayments()
  } catch (error) {
    console.error('載入待審核列表失敗:', error)
    alert('載入失敗，請稍後再試')
  } finally {
    loadingPending.value = false
  }
}

// 顯示審核通過對話框
const showApproveModal = (payment) => {
  selectedPayment.value = payment
  approveNote.value = ''
  showApproveDialog.value = true
}

// 顯示拒絕對話框
const showRejectModal = (payment) => {
  selectedPayment.value = payment
  rejectReason.value = ''
  showRejectDialog.value = true
}

// 關閉對話框
const closeModals = () => {
  showApproveDialog.value = false
  showRejectDialog.value = false
  selectedPayment.value = null
  approveNote.value = ''
  rejectReason.value = ''
}

// 確認審核通過
const confirmApprove = async () => {
  try {
    await approveCashPayment(selectedPayment.value.id, approveNote.value)
    alert('審核通過！')
    closeModals()
    await loadPendingReview()
  } catch (error) {
    console.error('審核失敗:', error)
    alert(error.response?.data?.message || '審核失敗，請稍後再試')
  }
}

// 確認拒絕
const confirmReject = async () => {
  if (!rejectReason.value.trim()) {
    alert('請填寫拒絕原因')
    return
  }

  try {
    await rejectCashPayment(selectedPayment.value.id, rejectReason.value)
    alert('已拒絕審核')
    closeModals()
    await loadPendingReview()
  } catch (error) {
    console.error('拒絕失敗:', error)
    alert(error.response?.data?.message || '操作失敗，請稍後再試')
  }
}

// 登出
const handleLogout = () => {
  if (confirm('確定要登出嗎？')) {
    userStore.logout()
    router.push('/admin/login')
  }
}

// 輔助函式
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
  loadPendingReview()
})
</script>

<style scoped>
.payment-management {
  min-height: 100vh;
  background: #fafafa;
}

/* 導航列 */
.admin-navbar {
  background: white;
  border-bottom: 1px solid #e0e0e0;
  padding: 1rem 0;
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav-container {
  max-width: 1400px;
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

/* 主要內容 */
.main-content {
  padding: 3rem 0;
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 2rem;
}

.section {
  background: white;
  border-radius: 12px;
  padding: 2rem;
  margin-bottom: 2rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid #f0f0f0;
}

.section-header h2 {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 600;
  color: #1a1a1a;
}

.btn-refresh {
  padding: 0.5rem 1rem;
  background: white;
  color: #666;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-refresh:hover {
  background: #f5f5f5;
  border-color: #ccc;
}

.loading {
  text-align: center;
  padding: 3rem;
  color: #666;
}

.empty-state {
  text-align: center;
  padding: 3rem;
  color: #999;
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
  padding: 1rem;
  text-align: left;
  font-weight: 600;
  color: #666;
  font-size: 0.9rem;
  border-bottom: 2px solid #e0e0e0;
}

td {
  padding: 1rem;
  border-bottom: 1px solid #f0f0f0;
  color: #333;
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
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 0.9rem;
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

/* 功能網格 */
.feature-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
}

.feature-box {
  padding: 1.5rem;
  border: 2px dashed #e0e0e0;
  border-radius: 8px;
  text-align: center;
}

.feature-box h3 {
  margin: 0 0 0.5rem 0;
  font-size: 1.1rem;
  color: #1a1a1a;
}

.feature-box p {
  margin: 0 0 1rem 0;
  color: #666;
  font-size: 0.9rem;
}

.btn-secondary {
  padding: 0.5rem 1.25rem;
  background: #f5f5f5;
  color: #999;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  font-weight: 500;
  cursor: not-allowed;
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

.modal-body p {
  margin: 0.5rem 0;
}

.form-group {
  margin-top: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: #333;
}

.form-group textarea {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-family: inherit;
  font-size: 1rem;
  resize: vertical;
  box-sizing: border-box;
}

.form-group textarea:focus {
  outline: none;
  border-color: #667eea;
}

.modal-footer {
  padding: 1.5rem;
  border-top: 1px solid #e0e0e0;
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
}

.btn-cancel {
  padding: 0.75rem 1.5rem;
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
}

.btn-confirm {
  padding: 0.75rem 1.5rem;
  background: #28a745;
  color: white;
  border: none;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.2s;
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

/* 響應式 */
@media (max-width: 768px) {
  .payments-table {
    font-size: 0.9rem;
  }

  th,
  td {
    padding: 0.75rem 0.5rem;
  }

  .actions {
    flex-direction: column;
  }

  .btn-approve,
  .btn-reject {
    width: 100%;
  }
}
</style>
