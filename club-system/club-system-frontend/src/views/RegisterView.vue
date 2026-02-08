<template>
  <div class="auth-container">
    <div class="auth-card">
      <h1 class="auth-title">註冊</h1>
      <p class="auth-subtitle">加入我們的社團</p>

      <form @submit.prevent="handleRegister" class="auth-form">
        <div class="form-group">
          <label for="name">姓名</label>
          <input
            id="name"
            v-model="formData.name"
            type="text"
            placeholder="請輸入您的姓名"
            required
          />
        </div>

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
            placeholder="至少 6 個字元"
            required
            minlength="6"
          />
        </div>

        <div class="form-group">
          <label for="phone">電話號碼</label>
          <input
            id="phone"
            v-model="formData.phone"
            type="tel"
            placeholder="0912345678"
            required
          />
        </div>

        <div class="form-group">
          <label for="birthday">生日</label>
          <input
            id="birthday"
            v-model="formData.birthday"
            type="date"
            required
          />
        </div>

        <div class="form-group">
          <label for="school">學校（選填）</label>
          <input
            id="school"
            v-model="formData.school"
            type="text"
            placeholder="請輸入您的學校"
          />
        </div>

        <div v-if="errorMessage" class="error-message">
          {{ errorMessage }}
        </div>

        <button type="submit" class="btn-primary" :disabled="loading">
          {{ loading ? '註冊中...' : '註冊' }}
        </button>
      </form>

      <div class="auth-footer">
        <p>已經有帳號？ <router-link to="/login">立即登入</router-link></p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { register } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()

const formData = ref({
  name: '',
  email: '',
  password: '',
  phone: '',
  birthday: '',
  school: ''
})

const loading = ref(false)
const errorMessage = ref('')

const handleRegister = async () => {
  loading.value = true
  errorMessage.value = ''

  try {
    const response = await register(formData.value)
    userStore.login(response)
    router.push('/dashboard')
  } catch (error) {
    errorMessage.value = error.response?.data?.message || '註冊失敗，請檢查您的資料'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fafafa;
  padding: 2rem;
}

.auth-card {
  background: white;
  padding: 3rem;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  width: 100%;
  max-width: 420px;
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
  gap: 1.25rem;
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
  margin-top: 0.5rem;
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
