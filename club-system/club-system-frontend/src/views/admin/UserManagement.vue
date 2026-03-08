<template>
  <div class="user-management">
    <!-- 頂部導航 -->
    <nav class="admin-navbar">
      <div class="nav-container">
        <h2 class="logo">人員管理</h2>
        <div class="nav-right">
          <router-link to="/admin/dashboard" class="nav-link">返回後台</router-link>
          <button @click="handleLogout" class="btn-logout">登出</button>
        </div>
      </div>
    </nav>

    <!-- 主要內容 -->
    <main class="main-content">
      <div class="container">
        <div class="placeholder-content">
          <div class="icon">👥</div>
          <h1>人員管理</h1>
          <p>此功能正在開發中，請稍後提供詳細需求</p>
          <div class="feature-list">
            <h3>預計功能：</h3>
            <ul>
              <li>查看所有社團成員</li>
              <li>編輯成員資料</li>
              <li>管理成員權限</li>
              <li>匯出成員名單</li>
            </ul>
          </div>
          <router-link to="/admin/dashboard" class="btn-back">返回後台首頁</router-link>
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
  if (confirm('確定要登出嗎？')) {
    userStore.logout()
    router.push('/admin/login')
  }
}
</script>

<style scoped>
.user-management {
  min-height: 100vh;
  background: #fafafa;
}

.admin-navbar {
  background: white;
  border-bottom: 1px solid #e0e0e0;
  padding: 1rem 0;
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

.main-content {
  padding: 3rem 0;
}

.container {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 2rem;
}

.placeholder-content {
  background: white;
  border-radius: 12px;
  padding: 4rem 2rem;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.icon {
  font-size: 5rem;
  margin-bottom: 1.5rem;
}

.placeholder-content h1 {
  margin: 0 0 1rem 0;
  font-size: 2rem;
  font-weight: 700;
  color: #1a1a1a;
}

.placeholder-content > p {
  color: #666;
  font-size: 1.1rem;
  margin-bottom: 2rem;
}

.feature-list {
  background: #fafafa;
  border-radius: 8px;
  padding: 2rem;
  margin: 2rem 0;
  text-align: left;
}

.feature-list h3 {
  margin: 0 0 1rem 0;
  font-size: 1.25rem;
  color: #1a1a1a;
}

.feature-list ul {
  margin: 0;
  padding-left: 1.5rem;
}

.feature-list li {
  margin: 0.75rem 0;
  color: #666;
  font-size: 1.05rem;
}

.btn-back {
  display: inline-block;
  padding: 0.875rem 2rem;
  background: #667eea;
  color: white;
  text-decoration: none;
  border-radius: 6px;
  font-weight: 500;
  transition: background 0.2s;
  margin-top: 1rem;
}

.btn-back:hover {
  background: #5568d3;
}
</style>
