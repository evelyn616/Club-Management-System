<template>
  <div class="profile-page">

    <main class="main-content">
      <div class="container">
        <div class="profile-header">
          <h1>個人資料</h1>
          <p class="subtitle">管理您的個人資訊</p>
        </div>

        <div v-if="loading" class="loading">載入中...</div>

        <div v-else-if="errorMessage && !userData" class="error-container">
          <div class="error-message">
            {{ errorMessage }}
          </div>
          <div class="debug-info">
            <p><strong>Debug 資訊：</strong></p>
            <p>User ID: {{ userStore.userId || '未設定' }}</p>
            <p>User Name: {{ userStore.userName || '未設定' }}</p>
            <p>已登入: {{ userStore.isLoggedIn ? '是' : '否' }}</p>
            <p>Token: {{ userStore.token ? '有' : '無' }}</p>
          </div>
          <button @click="loadUserData" class="btn-retry">重新載入</button>
        </div>

        <div v-else-if="userData" class="profile-content">
          <!-- 基本資料卡片 -->
          <div class="profile-card">
            <div class="card-header">
              <h2>基本資料</h2>
              <button v-if="!isEditing" @click="startEdit" class="btn-edit">編輯</button>
              <div v-else class="edit-actions">
                <button @click="handleSave" class="btn-save" :disabled="saving">
                  {{ saving ? '儲存中...' : '儲存' }}
                </button>
                <button @click="cancelEdit" class="btn-cancel">取消</button>
              </div>
            </div>

            <div v-if="errorMessage" class="error-message">
              {{ errorMessage }}
            </div>

            <div v-if="successMessage" class="success-message">
              {{ successMessage }}
            </div>

            <div class="profile-form">
              <div class="form-row">
                <div class="form-group">
                  <label>會員編號</label>
                  <input type="text" :value="userData.id" disabled />
                </div>
                <div class="form-group">
                  <label>角色</label>
                  <input type="text" :value="getRoleText(userData.role)" disabled />
                </div>
              </div>

              <div class="form-group">
                <label>信用積分</label>
                <div class="credit-points-display">
                  <span class="credit-icon">🎁</span>
                  <span class="credit-value">{{ userData.creditPoints ?? 0 }}</span>
                  <span class="credit-label">點</span>
                </div>
              </div>

              <div class="form-group">
                <label>姓名</label>
                <input
                  v-model="formData.name"
                  type="text"
                  :disabled="!isEditing"
                  :class="{ editing: isEditing }"
                />
              </div>

              <div class="form-group">
                <label>電子郵件</label>
                <input type="email" :value="userData.email" disabled />
              </div>

              <div class="form-group">
                <label>電話號碼</label>
                <input
                  v-model="formData.phone"
                  type="tel"
                  :disabled="!isEditing"
                  :class="{ editing: isEditing }"
                />
              </div>

              <div class="form-group">
                <label>生日</label>
                <input
                  v-model="formData.birthday"
                  type="date"
                  :disabled="!isEditing"
                  :class="{ editing: isEditing }"
                />
              </div>

              <div class="form-group">
                <label>學校</label>
                <input
                  v-model="formData.school"
                  type="text"
                  :disabled="!isEditing"
                  :class="{ editing: isEditing }"
                />
              </div>

              <div class="form-row">
                <div class="form-group">
                  <label>註冊時間</label>
                  <input type="text" :value="formatDate(userData.createdAt)" disabled />
                </div>
                <div class="form-group">
                  <label>最後更新</label>
                  <input type="text" :value="formatDate(userData.updatedAt)" disabled />
                </div>
              </div>
            </div>
          </div>

          <!-- 修改密碼卡片 -->
          <div class="profile-card">
            <div class="card-header">
              <h2>修改密碼</h2>
            </div>

            <div v-if="passwordError" class="error-message">
              {{ passwordError }}
            </div>

            <div v-if="passwordSuccess" class="success-message">
              {{ passwordSuccess }}
            </div>

            <div class="profile-form">
              <div class="form-group">
                <label>舊密碼</label>
                <input
                  v-model="passwordForm.oldPassword"
                  type="password"
                  placeholder="請輸入舊密碼"
                  class="editing"
                />
              </div>

              <div class="form-group">
                <label>新密碼</label>
                <input
                  v-model="passwordForm.newPassword"
                  type="password"
                  placeholder="請輸入新密碼（至少6個字符）"
                  class="editing"
                />
              </div>

              <div class="form-group">
                <label>確認新密碼</label>
                <input
                  v-model="passwordForm.confirmPassword"
                  type="password"
                  placeholder="請再次輸入新密碼"
                  class="editing"
                />
              </div>

              <button
                @click="handleChangePassword"
                class="btn-change-password"
                :disabled="changingPassword"
              >
                {{ changingPassword ? '修改中...' : '修改密碼' }}
              </button>
            </div>
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
.profile-page {
  min-height: 100vh;
  background: #fafafa;
}

/* Navbar */




.logo {
  font-size: 1.25rem;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
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





/* Main Content */
.main-content {
  padding: 3rem 0;
}

.container {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 2rem;
}

.profile-header {
  margin-bottom: 2rem;
}

.profile-header h1 {
  font-size: 2rem;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 0.5rem 0;
}

.subtitle {
  color: #666;
  margin: 0;
}

.loading {
  text-align: center;
  padding: 3rem;
  color: #666;
}

.error-container {
  text-align: center;
  padding: 3rem;
}

.debug-info {
  margin-top: 1.5rem;
  padding: 1rem;
  background: #f5f5f5;
  border-radius: 6px;
  text-align: left;
  max-width: 400px;
  margin-left: auto;
  margin-right: auto;
}

.debug-info p {
  margin: 0.5rem 0;
  font-size: 0.9rem;
  color: #666;
}

.btn-retry {
  margin-top: 1.5rem;
  padding: 0.75rem 1.5rem;
  background: #1a1a1a;
  color: white;
  border: none;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-retry:hover {
  background: #333;
}

.profile-content {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.profile-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  padding: 2rem;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #e0e0e0;
}

.card-header h2 {
  font-size: 1.5rem;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}

.btn-edit {
  padding: 0.5rem 1.25rem;
  background: #1a1a1a;
  color: white;
  border: none;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-edit:hover {
  background: #333;
}

.edit-actions {
  display: flex;
  gap: 0.75rem;
}

.btn-save {
  padding: 0.5rem 1.25rem;
  background: #1a1a1a;
  color: white;
  border: none;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-save:hover:not(:disabled) {
  background: #333;
}

.btn-save:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-cancel {
  padding: 0.5rem 1.25rem;
  background: white;
  color: #666;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-cancel:hover {
  background: #f5f5f5;
  border-color: #ccc;
}

.error-message {
  padding: 0.75rem;
  background: #fee;
  border: 1px solid #fcc;
  border-radius: 6px;
  color: #c33;
  font-size: 0.9rem;
  margin-bottom: 1.5rem;
}

.success-message {
  padding: 0.75rem;
  background: #efe;
  border: 1px solid #cfc;
  border-radius: 6px;
  color: #3c3;
  font-size: 0.9rem;
  margin-bottom: 1.5rem;
}

.profile-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
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
  background: #f9f9f9;
  color: #666;
}

.form-group input:disabled {
  cursor: not-allowed;
}

.form-group input.editing {
  background: white;
  border-color: #ccc;
  color: #1a1a1a;
}

.form-group input.editing:focus {
  outline: none;
  border-color: #333;
}

.credit-points-display {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  background: #fffbea;
  border: 1px solid #fde68a;
  border-radius: 6px;
}

.credit-icon {
  font-size: 1.1rem;
}

.credit-value {
  font-size: 1.4rem;
  font-weight: 700;
  color: #b45309;
}

.credit-label {
  font-size: 0.9rem;
  color: #92400e;
}

.btn-change-password {
  padding: 0.75rem 1.5rem;
  background: #1a1a1a;
  color: white;
  border: none;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.2s;
  margin-top: 0.5rem;
}

.btn-change-password:hover:not(:disabled) {
  background: #333;
}

.btn-change-password:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* Responsive */
@media (max-width: 768px) {
  .form-row {
    grid-template-columns: 1fr;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }

  .edit-actions {
    width: 100%;
  }

  .btn-save,
  .btn-cancel {
    flex: 1;
  }
}
</style>
