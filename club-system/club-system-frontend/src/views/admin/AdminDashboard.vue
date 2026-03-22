<template>
  <div class="admin-dashboard">

    <!-- 主要內容 -->
    <main class="main-content">
      <div class="container">
        <div class="page-header">
          <h1>歡迎回來</h1>
          <p class="subtitle">選擇功能開始管理</p>
        </div>

        <!-- 功能卡片網格 -->
        <div class="dashboard-grid">
          <!-- 你負責的功能 -->
          <div class="feature-card active" @click="navigateTo('/admin/users')">
            <div class="card-icon">👥</div>
            <h3>人員管理</h3>
            <p>管理社團成員資料</p>
          </div>

          <div class="feature-card active" @click="navigateTo('/admin/payments')">
            <div class="card-icon">💰</div>
            <h3>繳費管理</h3>
            <p>審核現金付款、查詢訂單</p>
            <span v-if="pendingCount > 0" class="badge">{{ pendingCount }}</span>
          </div>

         
          <div class="feature-card active" @click="navigateTo('/admin/activity-management-container')">
            <div class="card-icon">📅</div>
            <h3>活動上架</h3>
            <p>發布和管理活動</p>
            
          </div>

          <div class="feature-card active" @click="navigateTo('/admin/registrations-overview-container')">
            <div class="card-icon">📝</div>
            <h3>查看報名人</h3>
            <p>查看活動報名名單</p>
            
          </div>
           
          <div class="feature-card active" @click="navigateTo('/admin/checkin/dashboard')">
            <div class="card-icon">✅</div>
            <h3>查看點名</h3>
            <p>管理活動簽到記錄</p>
            
          </div>
          
          <div class="feature-card active" @click="navigateTo('/leave-request')">
            <div class="card-icon">🏖️</div>
            <h3>審核請假</h3>
            <p>審核社員請假申請</p>
          </div>
          
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getPendingReviewPayments } from '@/api/payment'

const router = useRouter()
const userStore = useUserStore()

const pendingCount = ref(0)

// 載入待審核數量
const loadPendingCount = async () => {
  try {
    const payments = await getPendingReviewPayments()
    pendingCount.value = payments.length
  } catch (error) {
    console.error('載入待審核數量失敗:', error)
  }
}

const navigateTo = (path) => {
  router.push(path)
}

const handleLogout = () => {
  if (confirm('確定要登出嗎？')) {
    userStore.logout()
    router.push('/admin/login')
  }
}

onMounted(() => {
  loadPendingCount()
})
</script>

<style scoped>
.admin-dashboard {
  min-height: 100vh;
  background: #fafafa;
}

/* 導航列 */














/* 主要內容 */
.main-content {
  padding: 3rem 0;
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 2rem;
}

.page-header {
  margin-bottom: 3rem;
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
  font-size: 1.05rem;
}

/* 功能卡片網格 */
.dashboard-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1.5rem;
}

.feature-card {
  background: white;
  border-radius: 12px;
  padding: 2rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s;
  position: relative;
  text-align: center;
}

.feature-card.active {
  cursor: pointer;
}

.feature-card.active:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
}

.feature-card.placeholder {
  opacity: 0.6;
  cursor: not-allowed;
}

.card-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.feature-card h3 {
  margin: 0 0 0.5rem 0;
  font-size: 1.25rem;
  font-weight: 600;
  color: #1a1a1a;
}

.feature-card p {
  margin: 0;
  color: #666;
  font-size: 0.95rem;
}

.badge {
  position: absolute;
  top: 1rem;
  right: 1rem;
  background: #dc3545;
  color: white;
  padding: 0.25rem 0.625rem;
  border-radius: 12px;
  font-size: 0.875rem;
  font-weight: 600;
}

.coming-soon {
  position: absolute;
  top: 1rem;
  right: 1rem;
  background: #ffc107;
  color: #333;
  padding: 0.25rem 0.625rem;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 600;
}

/* 響應式 */
@media (max-width: 768px) {
  .dashboard-grid {
    grid-template-columns: 1fr;
  }

  

  .container {
    padding: 0 1rem;
  }

  
}
</style>
