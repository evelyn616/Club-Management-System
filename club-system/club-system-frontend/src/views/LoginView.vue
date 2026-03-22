<template>
  <div class="auth-container">

    <!-- ── 左側圖片 Gallery ── -->
    <div class="gallery-grid">
      <div class="img-block" v-for="n in 4" :key="n">
        <div class="img-inner" :class="`img-${n}`"></div>
        <div class="img-overlay"></div>
      </div>
    </div>

    <!-- ── 右側表單 ── -->
    <div class="auth-form-wrapper">
      <div class="auth-card">
        <h1 class="auth-title">登入</h1>
        <p class="auth-subtitle">歡迎回來</p>

        <form @submit.prevent="handleLogin" class="auth-form">
          <div class="form-group">
            <label for="email">電子郵件</label>
            <input
              id="email"
              v-model="formData.email"
              type="email"
              placeholder="your@email.com"
              required
            />
          </div>

          <div class="form-group">
            <label for="password">密碼</label>
            <input
              id="password"
              v-model="formData.password"
              type="password"
              placeholder="••••••••"
              required
            />
          </div>

          <div v-if="errorMessage" class="error-message">
            {{ errorMessage }}
          </div>

          <button type="submit" class="btn-primary" :disabled="loading">
            {{ loading ? '登入中...' : '登入' }}
          </button>
        </form>

        <div class="auth-footer">
          <p>還沒有帳號？ <router-link to="/register">立即註冊</router-link></p>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { login } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()

const formData = ref({ email: '', password: '' })
const loading = ref(false)
const errorMessage = ref('')

const handleLogin = async () => {
  loading.value = true
  errorMessage.value = ''
  try {
    const response = await login(formData.value)
    userStore.login(response)
    router.push('/dashboard')
  } catch (error) {
    errorMessage.value = error.response?.data?.message || '登入失敗，請檢查您的電子郵件和密碼'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>

.auth-container {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  overflow: hidden;
  background: #000;
  padding: 2rem 24px 2rem 2rem;
}

/* ── 梯形白底（右側） ── */
.auth-container::before {
  content: '';
  position: absolute;
  top: 0; right: 0;
  width: 100%; height: 100%;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  z-index: 5;
  clip-path: polygon(75% 0%, 100% 0%, 100% 100%, 60% 100%);
}

/* ── 左側 Gallery ── */
.gallery-grid {
  position: absolute;
  inset: 0;
  width: 72%;
  display: grid;
  grid-template-columns: 1fr 1fr;
  grid-template-rows: 1fr 1fr;
  background: #000;
  z-index: 1;
}

.img-block {
  overflow: hidden;
  position: relative;
}

/* 圖片層：比方塊稍寬，預留右移空間 */
.img-inner {
  position: absolute;
  top: 0; bottom: 0;
  left: 0;
  right: -8%;
  background-size: cover;
  background-position: center;
  filter: grayscale(100%);
  transition: filter 0.5s ease, transform 0.55s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  transform: translateX(0);
  will-change: transform, filter;
}

.img-block:hover .img-inner {
  filter: none;
  
}

/* 各方塊圖片來源（同一張圖，填入你的圖片路徑或 URL） */
.img-inner {
  background-image: url('https://i.pinimg.com/1200x/7a/13/a0/7a13a019c901112a7d69ef4f59713346.jpg');
}

/* 模糊邊界 overlay（獨立層，不干擾 img-inner 的 transition） */
.img-overlay {
  position: absolute;
  inset: 0;
  box-shadow: inset 0 0 18px rgba(0, 0, 0, 0.65);
  pointer-events: none;
  z-index: 2;
}

/* ── 右側表單 ── */
.auth-form-wrapper {
  position: relative;
  z-index: 10;
  width: 100%;
  max-width: 380px;
  display: flex;
  flex-direction: column;
  margin-right: 5rem;
  align-items: flex-end;
  text-align: right;
}

.auth-card {
  background: transparent;
  box-shadow: none;
  padding: 0;
  border-radius: 0;
  border: none;
  width: 100%;
}

.auth-title {
  font-size: 2rem;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 0.5rem 0;
  text-align: center;
}

.auth-subtitle {
  color: #666;
  text-align: center;
  margin: 0 0 2rem 0;
  font-size: 0.95rem;
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-group label {
  font-size: 0.9rem;
  font-weight: 500;
  color: #333;
}

.form-group input {
  padding: 0.75rem 1rem;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  font-size: 1rem;
  transition: border-color 0.2s;
}

.form-group input:focus {
  outline: none;
  border-color: #333;
}

.error-message {
  padding: 0.75rem;
  background: #fee;
  border: 1px solid #fcc;
  border-radius: 6px;
  color: #c33;
  font-size: 0.9rem;
}

.btn-primary {
  padding: 0.875rem;
  background: #1a1a1a;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-primary:hover:not(:disabled) {
  background: #333;
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.auth-footer {
  margin-top: 2rem;
  text-align: center;
  color: #666;
  font-size: 0.9rem;
}

.auth-footer a {
  color: #1a1a1a;
  text-decoration: none;
  font-weight: 500;
}

.auth-footer a:hover {
  text-decoration: underline;
}
</style>
