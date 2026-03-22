<template>
  <div class="my-registrations">
    <nav class="navbar" :class="{ 'navbar-hidden': navHidden }">
      <div class="nav-container">
        <router-link to="/dashboard" class="nav-logo">CLUB SYSTEM</router-link>
        <div class="nav-right">
          <span class="nav-username">{{ userStore.userName }}</span>

          <button @click="handleLogout" class="nav-logout">登出</button>
        </div>
      </div>
    </nav>

    <div class="page-header">
      <div class="header-left">
        <div class="header-label">
          <span class="label-line"></span>
          <span class="label-text">USER ACCOUNT</span>
          <span class="label-num">PROFILE_01</span>
        </div>
        <h1 class="page-title">
          <span class="title-line title-line-1">管理</span>
          <span class="title-line title-line-2"><span class="title-accent">個人帳戶資料</span></span>
        </h1>
        <p class="page-subtitle">
          <span class="subtitle-inner">更新您的基本資訊並維護帳戶安全性</span>
        </p>
      </div>
      
      <div class="header-right">
        <div class="status-badge status-registered" v-if="userData">
          <span class="status-dot"></span>
          {{ getRoleText(userData.role) }} ID: {{ userData.id }}
        </div>
      </div>
    </div>

    <main class="form-container-custom">
      
      <div v-if="loading" class="state-loading">
        <div class="loading-bar-scan"></div>
        <p>SCANNING DATA...</p>
      </div>

      <div v-else-if="errorMessage && !userData" class="error-card-custom">
        <h3 class="error-title">CONNECTION ERROR</h3>
        <p>{{ errorMessage }}</p>
        <button @click="loadUserData" class="submit-btn-cyber">RETRY CONNECTION</button>
      </div>

      <div v-else-if="userData" class="profile-section-wrapper">
        
        <div class="registration-card profile-card-gap">
          <div class="card-index">BASIC_INFO</div>
          <div class="card-header-flex">
            <h2 class="section-title">基本資料</h2>
            <div class="action-zone">
              <button v-if="!isEditing" @click="startEdit" class="edit-trigger">EDIT PROFILE</button>
              <div v-else class="edit-actions-group">
                <button @click="handleSave" class="save-btn" :disabled="saving">SAVE</button>
                <button @click="cancelEdit" class="cancel-btn-text">CANCEL</button>
              </div>
            </div>
          </div>

          <transition name="fade">
            <div v-if="successMessage" class="msg-box success">{{ successMessage }}</div>
          </transition>

          <div class="form-grid">
            <div class="input-group">
              <label class="info-label">FULL NAME</label>
              <input v-model="formData.name" type="text" :disabled="!isEditing" :class="{ 'editing-mode': isEditing }" class="custom-input">
            </div>
            <div class="input-group">
              <label class="info-label">EMAIL ADDRESS</label>
              <input :value="userData.email" type="email" disabled class="custom-input readonly-style">
            </div>
            <div class="input-group">
              <label class="info-label">PHONE NUMBER</label>
              <input v-model="formData.phone" type="tel" :disabled="!isEditing" :class="{ 'editing-mode': isEditing }" class="custom-input">
            </div>
            <div class="input-group">
              <label class="info-label">BIRTHDAY</label>
              <input v-model="formData.birthday" type="date" :disabled="!isEditing" :class="{ 'editing-mode': isEditing }" class="custom-input">
            </div>
            <div class="input-group full-width">
              <label class="info-label">INSTITUTION / SCHOOL</label>
              <input v-model="formData.school" type="text" :disabled="!isEditing" :class="{ 'editing-mode': isEditing }" class="custom-input">
            </div>
            
            <div class="form-footer-meta">
              <div class="meta-item">CREATED AT: {{ formatDate(userData.createdAt) }}</div>
              <div class="meta-item">LAST UPDATED: {{ formatDate(userData.updatedAt) }}</div>
            </div>
          </div>
        </div>

        <div class="registration-card">
          <div class="card-index">SECURITY_ENCRYPTION</div>
          <h2 class="section-title">修改密碼</h2>
          
          <div v-if="passwordError" class="msg-box error">{{ passwordError }}</div>
          <div v-if="passwordSuccess" class="msg-box success">{{ passwordSuccess }}</div>

          <div class="form-grid">
            <div class="input-group full-width">
              <label class="info-label">OLD PASSWORD</label>
              <input v-model="passwordForm.oldPassword" type="password" placeholder="ENTER CURRENT PASSWORD" class="custom-input editing-mode">
            </div>
            <div class="input-group">
              <label class="info-label">NEW PASSWORD</label>
              <input v-model="passwordForm.newPassword" type="password" placeholder="MIN. 6 CHARACTERS" class="custom-input editing-mode">
            </div>
            <div class="input-group">
              <label class="info-label">CONFIRM NEW PASSWORD</label>
              <input v-model="passwordForm.confirmPassword" type="password" placeholder="RE-ENTER NEW PASSWORD" class="custom-input editing-mode">
            </div>
          </div>

          <button @click="handleChangePassword" class="submit-btn-cyber password-btn-margin" :disabled="changingPassword">
            {{ changingPassword ? 'ENCRYPTING...' : 'UPDATE PASSWORD' }}
          </button>
        </div>
      </div>
    </main>
  </div>
</template>
 
<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getUserById, updateUser, changePassword } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()

const userData = ref(null)
const formData = ref({
  name: '',
  phone: '',
  birthday: '',
  school: ''
})

const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const loading = ref(true)
const isEditing = ref(false)
const saving = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

const changingPassword = ref(false)
const passwordError = ref('')
const passwordSuccess = ref('')

const loadUserData = async () => {
  try {
    loading.value = true
    errorMessage.value = ''
    
    // 檢查 userId 是否存在
    if (!userStore.userId) {
      console.error('userId 不存在，userStore:', {
        userId: userStore.userId,
        userName: userStore.userName,
        isLoggedIn: userStore.isLoggedIn,
        token: userStore.token ? '有 token' : '無 token'
      })
      errorMessage.value = '無法取得用戶 ID，請重新登入'
      router.push('/login')
      return
    }
    
    console.log('正在載入用戶資料，userId:', userStore.userId)
    const data = await getUserById(userStore.userId)
    console.log('成功載入用戶資料:', data)
    userData.value = data

    // 初始化表單資料
    formData.value = {
      name: data.name,
      phone: data.phone || '',
      birthday: data.birthday || '',
      school: data.school || ''
    }
  } catch (error) {
    console.error('載入失敗，錯誤詳情:', error)
    console.error('錯誤回應:', error.response?.data)
    errorMessage.value = error.response?.data?.message || '載入個人資料失敗，請稍後再試'
  } finally {
    loading.value = false
  }
}

const startEdit = () => {
  isEditing.value = true
  errorMessage.value = ''
  successMessage.value = ''
}

const cancelEdit = () => {
  isEditing.value = false
  // 還原資料
  formData.value = {
    name: userData.value.name,
    phone: userData.value.phone || '',
    birthday: userData.value.birthday || '',
    school: userData.value.school || ''
  }
  errorMessage.value = ''
  successMessage.value = ''
}

const handleSave = async () => {
  saving.value = true
  errorMessage.value = ''
  successMessage.value = ''

  try {
    const updatedData = await updateUser(userStore.userId, formData.value)
    userData.value = updatedData
    userStore.userName = updatedData.name // 更新 store 中的名字
    isEditing.value = false
    successMessage.value = '個人資料更新成功'

    // 3 秒後清除成功訊息
    setTimeout(() => {
      successMessage.value = ''
    }, 3000)
  } catch (error) {
    errorMessage.value = error.response?.data?.message || '更新失敗，請稍後再試'
  } finally {
    saving.value = false
  }
}

const handleChangePassword = async () => {
  passwordError.value = ''
  passwordSuccess.value = ''

  // 驗證表單
  if (!passwordForm.value.oldPassword) {
    passwordError.value = '請輸入舊密碼'
    return
  }

  if (!passwordForm.value.newPassword) {
    passwordError.value = '請輸入新密碼'
    return
  }

  if (passwordForm.value.newPassword.length < 6) {
    passwordError.value = '新密碼長度必須至少6個字符'
    return
  }

  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    passwordError.value = '兩次輸入的新密碼不一致'
    return
  }

  changingPassword.value = true

  try {
    await changePassword(userStore.userId, {
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword
    })

    passwordSuccess.value = '密碼修改成功'

    // 清空表單
    passwordForm.value = {
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    }

    // 3 秒後清除成功訊息
    setTimeout(() => {
      passwordSuccess.value = ''
    }, 3000)
  } catch (error) {
    passwordError.value = error.response?.data?.message || '密碼修改失敗，請稍後再試'
  } finally {
    changingPassword.value = false
  }
}

const handleLogout = () => {
  userStore.logout()
  router.push('/')
}

const getRoleText = (role) => {
  return role === 'admin' ? '管理員' : '會員'
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
  loadUserData()
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Bebas+Neue&family=Noto+Sans+TC:wght@400;700&family=Space+Mono:wght@400;700&display=swap');

/* --- 1. 滿版容器基礎 --- */
.my-registrations {
  min-height: 100vh;
  background: #ffffff;
  font-family: 'Noto Sans TC', sans-serif;
  color: #0a0a0a;
  width: 100%;
  padding: 8rem 5% 5rem;
  box-sizing: border-box;
}

/* ===== Navbar ===== */
.navbar {
  position: fixed;
  padding: 1rem 0;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
  box-shadow: 0 2px 20px rgba(0, 0, 0, 0.06);
  transform: translateY(0);
  transition: transform 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}
.navbar-hidden { transform: translateY(-100%); }
.nav-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.nav-logo {
  font-family: 'Space Mono', monospace;
  font-size: 1.25rem;
  font-weight: 700;
  letter-spacing: 0.18em;
  color: #0a0a0a;
  text-decoration: none;
  transition: color 0.2s;
}
.nav-logo:hover { color: #ff2d6b; }
.nav-right { display: flex; align-items: center; gap: 1.5rem; }
.nav-username {
  font-family: 'Space Mono', monospace;
  font-weight: 500;
  letter-spacing: 0.08em;
  color: #aaa;
}
.nav-link {
  font-family: 'Space Mono', monospace;
  font-weight: 500;
  letter-spacing: 0.08em;
  color: #555;
  text-decoration: none;
  transition: color 0.2s;
}
.nav-link:hover { color: #0a0a0a; }
.nav-logout {
  background: transparent;
  border: 1px solid #e0e0e0;
  color: #555;
  padding: 0.5rem 1.25rem;
  font-family: 'Space Mono', monospace;
  letter-spacing: 0.08em;
  cursor: pointer;
  transition: all 0.2s;
  border-radius: 6px;
  font-weight: 500;
}
.nav-logout:hover { border-color: #ff2d6b; color: #ff2d6b; background: rgba(255, 45, 107, 0.04); }

/* --- 3. Header 動畫 --- */
.page-header {
  display: flex; justify-content: space-between; align-items: flex-end;
  margin-bottom: 4rem; padding-bottom: 2rem; border-bottom: 2px solid #0a0a0a;
  animation: slideUpFade 0.8s ease-out;
}
.header-label { display: flex; align-items: center; gap: 0.6rem; margin-bottom: 1rem; }
.label-line { width: 2rem; height: 1px; background: #ff2d6b; animation: expandLine 1.2s ease-out; }
.label-text { font-family: 'Space Mono', monospace; font-size: 0.65rem; color: #ff2d6b; letter-spacing: 0.2em; animation: revealText 1s both; }

.page-title { font-family: 'Bebas Neue', sans-serif; font-size: 5rem; line-height: 0.92; margin: 0; }
.title-line-1 { display: block; animation: slideUpFade 0.8s 0.1s both; }
.title-line-2 { display: block; animation: slideUpFade 0.8s 0.2s both; }
.title-accent { color: #ff2d6b; }
.page-subtitle { font-size: 0.85rem; color: #999; margin-top: 1rem; animation: slideUpFade 0.8s 0.3s both; }

/* --- 4. 卡片與表單設計 --- */
.form-container-custom { max-width: 1000px; margin: 0 auto; animation: slideUpFade 1s 0.4s both; }
.registration-card {
  background: #ffffff; border: 1px solid #eeeeee; position: relative;
  padding: 3.5rem; margin-bottom: 3rem; box-shadow: 0 10px 40px rgba(0,0,0,0.03);
}
.registration-card::before { content: ''; position: absolute; left: 0; top: 0; bottom: 0; width: 4px; background: #0a0a0a; }

.section-title { font-family: 'Noto Sans TC', sans-serif; font-weight: 700; font-size: 1.5rem; margin: 0 0 2rem 0; }
.card-index { font-family: 'Space Mono', monospace; font-size: 0.65rem; color: #444; margin-bottom: 1rem; }

.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 2rem; }
.full-width { grid-column: span 2; }

/* 輸入框優化 */
.info-label { font-family: 'Space Mono', monospace; font-size: 0.7rem; color: #444; margin-bottom: 0.6rem; display: block; }
.custom-input {
  width: 100%; border: 1px solid #e0e0e0; padding: 1rem; font-size: 0.95rem; outline: none; transition: all 0.25s;
}
.readonly-style { background: #f9f9f9; color: #999; border-color: #f0f0f0; cursor: not-allowed; }
.editing-mode { border-color: #0a0a0a; }
.editing-mode:focus { border-color: #ff2d6b; box-shadow: 0 0 0 4px rgba(255, 45, 107, 0.05); }

/* 按鈕類別 */
.card-header-flex { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 1.5rem; }
.edit-trigger { 
  background: transparent; border: 1px solid #0a0a0a; padding: 0.6rem 1.5rem; 
  font-family: 'Space Mono', monospace; font-size: 0.75rem; cursor: pointer; transition: 0.3s;
}
.edit-trigger:hover { background: #0a0a0a; color: #fff; }



/* SAVE 按鈕 (主按鈕：桃紅) */
.save-btn {
  background: #ff2d6b;
  color: #fff;
  border: 1px solid #ff2d6b;
  padding: 0.6rem 2rem;
  cursor: pointer;
  font-family: 'Space Mono', monospace;
  font-size: 0.75rem;
  font-weight: 700;
  letter-spacing: 0.1em;
  transition: all 0.3s ease;
}

.save-btn:hover {
  background: #d41f53;
  border-color: #d41f53;
}

.save-btn:disabled {
  background: #ccc;
  border-color: #ccc;
  cursor: not-allowed;
}
.cancel-btn-text {
  background: transparent;
  color: #0a0a0a;
  border: 1px solid #0a0a0a;
  padding: 0.6rem 1.5rem;
  cursor: pointer;
  font-family: 'Space Mono', monospace;
  font-size: 0.75rem;
  font-weight: 700;
  letter-spacing: 0.1em;
  transition: all 0.3s ease;
}

.cancel-btn-text:hover {
  background: #0a0a0a;
  color: #fff;
}
.submit-btn-cyber {
  width: 100%; background: #0a0a0a; color: #fff; border: none; padding: 1.2rem;
  font-family: 'Space Mono', monospace; font-weight: 700; cursor: pointer; transition: 0.3s;
}
.submit-btn-cyber:hover { background: #ff2d6b; transform: translateY(-2px); }
.password-btn-margin { margin-top: 2.5rem; }

/* 訊息框 */
.msg-box { padding: 1rem; margin-bottom: 2rem; font-size: 0.9rem; font-weight: 700; border-left: 4px solid; }
.msg-box.success { background: #f0fff4; color: #2f855a; border-color: #48bb78; }
.msg-box.error { background: #fff5f5; color: #c53030; border-color: #f56565; }

.form-footer-meta { grid-column: span 2; display: flex; gap: 2rem; margin-top: 1rem; padding-top: 1.5rem; border-top: 1px dashed #eee; }
.meta-item { font-family: 'Space Mono', monospace; font-size: 0.65rem; color: #444; }

/* --- 5. 動畫定義 --- */
@keyframes slideUpFade { from { opacity: 0; transform: translateY(30px); } to { opacity: 1; transform: translateY(0); } }
@keyframes expandLine { from { width: 0; } to { width: 2rem; } }
@keyframes revealText { from { clip-path: inset(0 100% 0 0); } to { clip-path: inset(0 0% 0 0); } }

/* 狀態標籤 */
.status-badge { display: flex; align-items: center; gap: 0.5rem; font-family: 'Space Mono', monospace; font-size: 0.65rem; padding: 0.4rem 1.2rem; background: #f5f5f5; border-radius: 20px; }
.status-dot { width: 6px; height: 6px; background: #ff2d6b; border-radius: 50%; animation: pulse 1.5s infinite; }

@keyframes pulse { 0% { opacity: 1; } 50% { opacity: 0.3; } 100% { opacity: 1; } }

@media (max-width: 768px) {
  .page-title { font-size: 3rem; }
  .form-grid { grid-template-columns: 1fr; }
  .registration-card { padding: 2rem; }
}
</style>