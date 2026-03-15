<template>
  <div class="auth-container" @mousemove="onMouseMove" @mouseleave="onMouseLeave">
    
    <div class="hero-bg hero-bw"></div>
    <div
      class="hero-bg hero-color"
      :style="{
        maskImage: `radial-gradient(circle ${reveal}px at ${mx}px ${my}px, black 0%, transparent 100%)`,
        WebkitMaskImage: `radial-gradient(circle ${reveal}px at ${mx}px ${my}px, black 0%, transparent 100%)`
      }"
    ></div>
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

// --- 滑鼠互動邏輯 ---
const mx = ref(0)
const my = ref(0)
const reveal = ref(0)

const onMouseMove = (e) => {
  // 因為是滿版，直接用 clientX/Y 即可，或是使用相對容器的座標
  const r = e.currentTarget.getBoundingClientRect()
  mx.value = e.clientX - r.left
  my.value = e.clientY - r.top
  reveal.value = 300 // 登入頁可以讓範圍大一點，效果更明顯
}
const onMouseLeave = () => { reveal.value = 0 }

// --- 登入邏輯 ---
const formData = ref({
  email: '',
  password: ''
})

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

/* 容器變成相對定位，並隱藏溢出 */
/*.auth-container {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  overflow: hidden; 
  background: #000; 
  padding: 2rem 8% 2rem 2rem;
}
*/

.auth-container {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: flex-end; /* 將內容（表單）推向右側 */
  overflow: hidden;
  background: #000; /* 底色保持為黑 */
  /* padding 調整，為梯形右側留出空間 */
  padding: 2rem 24px 2rem 2rem; 
}

.auth-container::before {
  content: '';
  position: absolute;
  top: 0;
  right: 0;
  width: 100%; /* 初始佔滿全寬，然後裁切 */
  height: 100%;
  background: rgba(255, 255, 255, 0.95); /* 白色背景，帶點透明感 */
  backdrop-filter: blur(10px); /* 保持磨砂玻璃效果 */
  z-index: 5; /* 介於背景圖 (z-index: 1) 和表單 (z-index: 10) 之間 */
  
  /* 重點：使用 clip-path 裁切成梯形 */
  /* 你的需求：
     螢幕上面的右四分之一 -> (75% 0%)
     螢幕下邊的右三分之一 -> (66.6% 100%)
     拉起來的右半部分 fill -> (100% 100%) 和 (100% 0%)
  */
  clip-path: polygon(75% 0%, 100% 0%, 100% 100%, 60% 100%);
}

.auth-form-wrapper {
  position: relative;
  z-index: 10; /* 確保表單在梯形背景之上 */
  width: 100%;
  max-width: 380px; /* 限制表單寬度 */
  display: flex;
  flex-direction: column;
  /* 調整表單位置，使其更靠近梯形中心 */
  margin-right: 12px; 
  
  /* --- 重點：讓內部所有項目靠右 --- */
  align-items: flex-end; 
  text-align: right;
}

/* ══ 背景圖片樣式 (複製自 Dashboard) ══ */
.hero-bg {
  position: absolute;
  inset: 0;
  /* 這裡套用與 Dashboard 一樣的圖片連結 */
  background-image: url('https://scontent.ftpe3-1.fna.fbcdn.net/v/t39.30808-6/486824286_1066644962160551_318350841127563953_n.jpg?_nc_cat=104&ccb=1-7&_nc_sid=dd6889&_nc_ohc=7R9doMjhnm0Q7kNvwEtyY-a&_nc_oc=AdnDRQ_LhDz3lmcUncrkZ5EcWJAFdzVPqUc_ZYCM2IRYn_0x9HdbhKCHGZCtTaujepY&_nc_zt=23&_nc_ht=scontent.ftpe3-1.fna&_nc_gid=RTDgD4aZgC_v39vGFdK-mA&_nc_ss=8&oh=00_AfzUvfnvjx6m3TSqSK8i779rqgCm2jx-14TndyHM8RG88A&oe=69B9EC95');
  background-size: cover;
  background-position: center;
  z-index: 1;
}

.hero-bw {
  filter: grayscale(100%) brightness(0.8); /* 讓背景暗一點，凸顯中間卡片 */
}

.hero-color {
  filter: brightness(0.8) saturate(1.2);
}



/*.auth-card {
  background: white;
  padding: 3rem;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  width: 100%;
  max-width: 420px;
  z-index: 10;
  background: rgba(255, 255, 255, 0.9); 
  backdrop-filter: blur(10px); 
}*/

.auth-card {
  /* 這裡的樣式大多都可以刪除，只保留佈局 */
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
