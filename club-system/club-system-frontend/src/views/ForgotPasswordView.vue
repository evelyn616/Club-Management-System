<template>
  <div class="auth-container">

    <!-- 左側圖片 Gallery -->
    <div class="gallery-grid">
      <div class="img-block" v-for="n in 4" :key="n">
        <div class="img-inner"></div>
        <div class="img-overlay"></div>
      </div>
    </div>

    <!-- 右側表單 -->
    <div class="auth-form-wrapper">
      <div class="auth-card">
        <h1 class="auth-title">忘記密碼</h1>
        <p class="auth-subtitle">輸入你的信箱，我們會寄送驗證碼</p>

        <form @submit.prevent="handleSubmit" class="auth-form">
          <div class="form-group">
            <label for="email">電子郵件</label>
            <input
              id="email"
              v-model="email"
              type="email"
              placeholder="your@email.com"
              required
            />
          </div>

          <div v-if="errorMsg" class="error-message">{{ errorMsg }}</div>

          <button type="submit" class="btn-primary" :disabled="loading">
            {{ loading ? '發送中...' : '發送驗證碼' }}
          </button>
        </form>

        <div class="auth-footer">
          <p><router-link to="/login">← 返回登入</router-link></p>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { requestPasswordReset } from '@/api/auth'

const router = useRouter()
const email = ref('')
const loading = ref(false)
const errorMsg = ref('')
const devCode = ref('')  // TODO: 正式環境移除

const handleSubmit = async () => {
  loading.value = true
  errorMsg.value = ''
  devCode.value = ''
  try {
    const res = await requestPasswordReset(email.value)
    devCode.value = res.code || ''  // TODO: 正式環境移除
    router.push({ path: '/reset-password', query: { email: email.value } })
  } catch (err) {
    errorMsg.value = err.response?.data?.message || '發送失敗，請稍後再試'
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

.img-inner {
  position: absolute;
  top: 0; bottom: 0;
  left: 0; right: -8%;
  background-size: cover;
  background-position: center;
  filter: grayscale(100%);
  transition: filter 0.5s ease, transform 0.55s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  transform: translateX(0);
  background-image: url('https://i.pinimg.com/1200x/7a/13/a0/7a13a019c901112a7d69ef4f59713346.jpg');
}

.img-block:hover .img-inner {
  filter: none;
}

.img-overlay {
  position: absolute;
  inset: 0;
  box-shadow: inset 0 0 18px rgba(0, 0, 0, 0.65);
  pointer-events: none;
  z-index: 2;
}

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

.btn-primary:hover:not(:disabled) { background: #333; }
.btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }

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

.auth-footer a:hover { text-decoration: underline; }
</style>
