<template>
  <div class="dashboard">
    <nav class="navbar">
      <div class="nav-container">
        <h2 class="logo">社團管理系統</h2>
        <div class="nav-right">
          <span class="user-name">{{ userStore.userName }}</span>
          <router-link to="/profile" class="nav-link">個人資料</router-link>
          <button @click="handleLogout" class="btn-logout">登出</button>
        </div>
      </div>
    </nav>

    <main class="main-content">
      <div class="container">
        <div class="welcome-section">
          <h1>歡迎回來，{{ userStore.userName }}</h1>
          <p class="subtitle">這是您的社團管理儀表板</p>
        </div>

        <div class="quick-actions">
          <h2 class="section-title">快速功能</h2>
          <div class="action-grid">
            <router-link to="/activity-registration-list-container" class="action-card">
              <h3>活動報名</h3>
              <p>查看並報名最新活動</p>
            </router-link>

            <router-link to="/my-registrations" class="action-card">
              <h3>我的報名</h3>
              <p>查看已報名的活動</p>
            </router-link>

            <router-link to="/pending-payments" class="action-card">
              <h3>待繳費用</h3>
              <p>查看待繳費項目</p>
            </router-link>

            <router-link to="/profile" class="action-card">
              <h3>個人資料</h3>
              <p>管理您的個人資訊</p>
            </router-link>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const handleLogout = () => {
  userStore.logout()
  router.push('/')
}
</script>

<style scoped>
.dashboard {
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
  gap: 1.5rem;
  align-items: center;
}

.user-name {
  color: #666;
  font-weight: 500;
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

.welcome-section {
  margin-bottom: 3rem;
}

.welcome-section h1 {
  font-size: 2.5rem;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 0.5rem 0;
}

.subtitle {
  color: #666;
  font-size: 1.1rem;
  margin: 0;
}

.section-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 1.5rem 0;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 1.5rem;
}

.action-card {
  background: white;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  text-decoration: none;
  transition: all 0.2s;
  border: 1px solid transparent;
}

.action-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-color: #e0e0e0;
  transform: translateY(-2px);
}

.action-card h3 {
  font-size: 1.25rem;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 0.5rem 0;
}

.action-card p {
  color: #666;
  margin: 0;
  line-height: 1.5;
}

/* Responsive */
@media (max-width: 768px) {
  .nav-right {
    gap: 1rem;
  }

  .user-name {
    display: none;
  }

  .welcome-section h1 {
    font-size: 2rem;
  }

  .action-grid {
    grid-template-columns: 1fr;
  }
}
</style>
