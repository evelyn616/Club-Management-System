<template>
  <div class="landing">
    <!-- 未登入才顯示此 navbar -->
    <nav class="navbar" v-if="!userStore.isLoggedIn">
      <div class="nav-container">
        <h2 class="logo">社團管理系統</h2>
        <div class="nav-links">
          <router-link to="/login" class="nav-link">登入</router-link>
          <router-link to="/register" class="btn-nav">註冊</router-link>
        </div>
      </div>
    </nav>

    <main class="hero">
      <div class="hero-content">
        <h1 class="hero-title">輕鬆管理你的社團</h1>
        <p class="hero-subtitle">
          簡單、高效的社團活動管理平台<br />
          讓社團運作更順暢
        </p>
        <div class="hero-actions">
          <router-link to="/register" class="btn-hero-primary">開始使用</router-link>
          <router-link to="/login" class="btn-hero-secondary">會員登入</router-link>
        </div>
      </div>
    </main>

    <section class="features">
      <div class="features-container">
        <div class="feature-card">
          <h3>活動管理</h3>
          <p>輕鬆建立和管理社團活動，追蹤報名狀況</p>
        </div>
        <div class="feature-card">
          <h3>線上報名</h3>
          <p>會員可以快速報名活動，即時查看報名狀態</p>
        </div>
        <div class="feature-card">
          <h3>繳費管理</h3>
          <p>整合線上金流，簡化繳費流程和對帳作業</p>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { useUserStore } from '@/stores/user'
const userStore = useUserStore()
</script>

<style scoped>
.landing {
  min-height: 100vh;
  background: white;
}

/* Navbar */
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 800;
  border-bottom: none;
  background: transparent;
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
  color: #fff;
  margin: 0;
}

.nav-links {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.nav-link {
  color: rgba(255,255,255,0.8);
  text-decoration: none;
  font-weight: 500;
  transition: color 0.2s;
}

.nav-link:hover {
  color: #fff;
}

.btn-nav {
  padding: 0.5rem 1.25rem;
  background: rgba(255,255,255,0.15);
  border: 1px solid rgba(255,255,255,0.4);
  color: white;
  text-decoration: none;
  border-radius: 6px;
  font-weight: 500;
  transition: background 0.2s;
  backdrop-filter: blur(4px);
}

.btn-nav:hover {
  background: rgba(255,255,255,0.25);
}

/* Hero Section */
.hero {
  max-width: 1200px;
  margin: 0 auto;
  padding: 6rem 2rem;
  text-align: center;
}

.hero-content {
  max-width: 700px;
  margin: 0 auto;
}

.hero-title {
  font-size: 3.5rem;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 1.5rem 0;
  line-height: 1.2;
}

.hero-subtitle {
  font-size: 1.25rem;
  color: #666;
  margin: 0 0 3rem 0;
  line-height: 1.6;
}

.hero-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
}

.btn-hero-primary {
  padding: 1rem 2rem;
  background: #1a1a1a;
  color: white;
  text-decoration: none;
  border-radius: 8px;
  font-weight: 600;
  font-size: 1.1rem;
  transition: background 0.2s;
}

.btn-hero-primary:hover {
  background: #333;
}

.btn-hero-secondary {
  padding: 1rem 2rem;
  background: white;
  color: #1a1a1a;
  text-decoration: none;
  border-radius: 8px;
  font-weight: 600;
  font-size: 1.1rem;
  border: 2px solid #1a1a1a;
  transition: all 0.2s;
}

.btn-hero-secondary:hover {
  background: #1a1a1a;
  color: white;
}

/* Features Section */
.features {
  background: #fafafa;
  padding: 5rem 2rem;
}

.features-container {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 2rem;
}

.feature-card {
  background: white;
  padding: 2.5rem;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.feature-card h3 {
  font-size: 1.5rem;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 1rem 0;
}

.feature-card p {
  color: #666;
  line-height: 1.6;
  margin: 0;
}

/* Responsive */
@media (max-width: 768px) {
  .hero-title {
    font-size: 2.5rem;
  }

  .hero-subtitle {
    font-size: 1.1rem;
  }

  .hero-actions {
    flex-direction: column;
  }

  .btn-hero-primary,
  .btn-hero-secondary {
    width: 100%;
  }
}
</style>
