<template>
  <div class="admin-login-page">
    <div class="login-container">
      <div class="login-card">
        <div class="login-header">
          <h1>管理後台</h1>
          <p>請使用管理員帳號登入</p>
        </div>

        <form @submit.prevent="handleLogin" class="login-form">
          <div class="form-group">
            <label for="email">電子郵件</label>
            <input
              id="email"
              v-model="loginForm.email"
              type="email"
              placeholder="請輸入電子郵件"
              required
            />
          </div>

          <div class="form-group">
            <label for="password">密碼</label>
            <input
              id="password"
              v-model="loginForm.password"
              type="password"
              placeholder="請輸入密碼"
              required
            />
          </div>

          <div v-if="errorMessage" class="error-message">
            {{ errorMessage }}
          </div>

          <button type="submit" class="btn-login" :disabled="loading">
            {{ loading ? '登入中...' : '登入' }}
          </button>
        </form>

        <div class="login-footer">
          <router-link to="/" class="link-back">返回首頁</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { adminLogin } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()

const loginForm = ref({
  email: '',
  password: ''
})

const loading = ref(false)
const errorMessage = ref('')

const handleLogin = async () => {
  try {
    loading.value = true
    errorMessage.value = ''

    // ✅ 改用 adminLogin，不再用一般 login
    const response = await adminLogin(loginForm.value)

    // 將回應傳給 userStore（存 token + user info）
    userStore.login(response)

    // 後端 adminLogin 已驗證 role，這裡做前端二次確認
    if (response.user?.role?.toUpperCase() !== 'ADMIN') {
      errorMessage.value = '您沒有管理員權限'
      userStore.logout()
      return
    }

    // 登入成功，導向管理後台
    router.push({ name: 'activity-management-container' })
  } catch (error) {
    console.error('登入失敗:', error)
    const status = error.response?.status
    if (status === 400) {
      errorMessage.value = error.response?.data?.message || '帳號或密碼錯誤'
    } else if (status === 403) {
      errorMessage.value = '您沒有管理員權限'
    } else {
      errorMessage.value = '登入失敗，請稍後再試'
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.admin-login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 2rem;
}

.login-container {
  width: 100%;
  max-width: 420px;
}

.login-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  overflow: hidden;
}

.login-header {
  padding: 2.5rem 2rem 1.5rem;
  text-align: center;
  background: #fafafa;
  border-bottom: 1px solid #e0e0e0;
}

.login-header h1 {
  margin: 0 0 0.5rem 0;
  font-size: 1.75rem;
  font-weight: 700;
  color: #1a1a1a;
}

.login-header p {
  margin: 0;
  color: #666;
  font-size: 0.95rem;
}

.login-form {
  padding: 2rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: #333;
  font-size: 0.95rem;
}

.form-group input {
  width: 100%;
  padding: 0.875rem;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 1rem;
  transition: border-color 0.2s;
  box-sizing: border-box;
}

.form-group input:focus {
  outline: none;
  border-color: #667eea;
}

.error-message {
  padding: 0.875rem;
  background: #fee;
  border: 1px solid #fcc;
  border-radius: 6px;
  color: #c33;
  font-size: 0.9rem;
  margin-bottom: 1.5rem;
}

.btn-login {
  width: 100%;
  padding: 1rem;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-login:hover:not(:disabled) {
  background: #5568d3;
}

.btn-login:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.login-footer {
  padding: 1.5rem 2rem;
  text-align: center;
  background: #fafafa;
  border-top: 1px solid #e0e0e0;
}

.link-back {
  color: #667eea;
  text-decoration: none;
  font-weight: 500;
  font-size: 0.95rem;
}

.link-back:hover {
  text-decoration: underline;
}
</style>