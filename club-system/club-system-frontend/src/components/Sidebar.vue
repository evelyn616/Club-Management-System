<script setup>
import { RouterLink } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useUiStore } from '@/stores/ui'
import { computed, ref } from 'vue'
import { adminLogin, sendAdminMfa, verifyAdminMfa } from '@/api/auth'

const userStore = useUserStore()
const uiStore = useUiStore()

const isAdminAccount = computed(() => userStore.userRole?.toUpperCase() === 'ADMIN')
const adminElevated = computed(() => userStore.adminElevated)

// 自動收合 timer
const autoCloseTimer = ref(null)
const onMouseLeave = () => {
  autoCloseTimer.value = setTimeout(() => uiStore.setSidebar(false), 2000)
}
const onMouseEnter = () => {
  if (autoCloseTimer.value) {
    clearTimeout(autoCloseTimer.value)
    autoCloseTimer.value = null
  }
}

// ===== Modal state =====
const showModal = ref(false)
const step = ref(1)           // 1: 密碼  2: 選 MFA 方法  3: 輸驗證碼

// Step 1
const elevatePassword = ref('')
const passwordError = ref('')
const passwordLoading = ref(false)

// Step 2
const mfaMethod = ref('EMAIL') // 'EMAIL' | 'PHONE'
const maskedTarget = ref('')
const sendLoading = ref(false)
const sendError = ref('')

// Step 3
const mfaCode = ref('')
const codeError = ref('')
const codeLoading = ref(false)
const codeInputs = ref(['', '', '', '', '', ''])

const openModal = () => {
  step.value = 1
  elevatePassword.value = ''
  passwordError.value = ''
  mfaMethod.value = 'EMAIL'
  maskedTarget.value = ''
  sendError.value = ''
  mfaCode.value = ''
  codeError.value = ''
  codeInputs.value = ['', '', '', '', '', '']
  showModal.value = true
}
const closeModal = () => { showModal.value = false }

// Step 1: 密碼驗證
const handlePasswordVerify = async () => {
  if (!elevatePassword.value) { passwordError.value = '請輸入密碼'; return }
  passwordLoading.value = true
  passwordError.value = ''
  try {
    await adminLogin({ email: userStore.userEmail, password: elevatePassword.value })
    step.value = 2
  } catch {
    passwordError.value = '密碼錯誤，請重試'
  } finally {
    passwordLoading.value = false
  }
}

// Step 2: 發送驗證碼
const handleSendCode = async () => {
  sendLoading.value = true
  sendError.value = ''
  try {
    const res = await sendAdminMfa(mfaMethod.value)
    maskedTarget.value = res.maskedTarget || ''
    mfaCode.value = ''
    codeInputs.value = ['', '', '', '', '', '']
    codeError.value = ''
    step.value = 3
  } catch (e) {
    const msg = e?.response?.data?.message || '發送失敗，請稍後再試'
    if (e?.response?.data?.code === 'SMS_NOT_CONFIGURED') {
      sendError.value = 'SMS 服務尚未設定，請選擇信箱驗證'
    } else {
      sendError.value = msg
    }
  } finally {
    sendLoading.value = false
  }
}

// Step 3: 驗證碼輸入處理
const handleCodeInput = (index, event) => {
  const val = event.target.value.replace(/\D/g, '')
  codeInputs.value[index] = val ? val[0] : ''
  if (val && index < 5) {
    const next = event.target.closest('.code-fields').querySelectorAll('.code-input')[index + 1]
    next?.focus()
  }
  mfaCode.value = codeInputs.value.join('')
}
const handleCodeKeydown = (index, event) => {
  if (event.key === 'Backspace' && !codeInputs.value[index] && index > 0) {
    const prev = event.target.closest('.code-fields').querySelectorAll('.code-input')[index - 1]
    prev?.focus()
  }
}
const handleCodePaste = (event) => {
  const pasted = event.clipboardData.getData('text').replace(/\D/g, '').substring(0, 6)
  pasted.split('').forEach((c, i) => { codeInputs.value[i] = c })
  mfaCode.value = codeInputs.value.join('')
  event.preventDefault()
}

const handleVerifyCode = async () => {
  mfaCode.value = codeInputs.value.join('')
  if (mfaCode.value.length < 6) { codeError.value = '請輸入 6 位驗證碼'; return }
  codeLoading.value = true
  codeError.value = ''
  try {
    await verifyAdminMfa(mfaCode.value)
    userStore.elevateAdmin()
    closeModal()
  } catch (e) {
    const code = e?.response?.data?.code
    if (code === 'MFA_BLOCKED') {
      codeError.value = '嘗試次數過多，請重新發送驗證碼'
      step.value = 2
    } else {
      codeError.value = '驗證碼錯誤或已過期'
    }
    codeInputs.value = ['', '', '', '', '', '']
    mfaCode.value = ''
  } finally {
    codeLoading.value = false
  }
}

const handleRevoke = () => { userStore.revokeAdmin() }

const backToStep = (s) => {
  sendError.value = ''
  codeError.value = ''
  step.value = s
}

// 活動管理子選單展開/收合
const activityMenuOpen = ref(false)
const toggleActivityMenu = () => { activityMenuOpen.value = !activityMenuOpen.value }
</script>

<template>
  <!-- 遮罩 -->
  <div
    v-if="!adminElevated && uiStore.sidebarOpen"
    class="overlay"
    @click="uiStore.setSidebar(false)"
  />

  <aside class="sidebar" :class="{ open: uiStore.sidebarOpen }" @mouseenter="onMouseEnter" @mouseleave="onMouseLeave">
    <div class="sidebar-header">
      <button class="toggle-btn" @click="uiStore.toggleSidebar">
        <span class="hamburger-icon" :class="{ open: uiStore.sidebarOpen }">
          <span></span><span></span><span></span>
        </span>
      </button>
      <span class="user-name">{{ userStore.userName || '' }}</span>
    </div>

    <nav>
      <!-- 管理員已提升 -->
      <div v-if="isAdminAccount && adminElevated">
        <div class="admin-mode-badge">
          <span class="badge-dot"></span>管理員模式
        </div>
        <p class="menu-label">管理員</p>
        <ul>
          <li><RouterLink :to="{ name: 'admin-dashboard' }">待處理事項</RouterLink></li>
          <li class="submenu-parent" :class="{ open: activityMenuOpen }">
            <button class="submenu-toggle" @click="toggleActivityMenu">
              <span>活動管理</span>
              <span class="submenu-arrow">▸</span>
            </button>
            <ul class="submenu" v-show="activityMenuOpen">
              <li><RouterLink :to="{ name: 'create-activity-container' }">新增活動</RouterLink></li>
              <li><RouterLink :to="{ name: 'activity-list-container' }">活動列表</RouterLink></li>
              <li><RouterLink :to="{ name: 'publish-activity-container' }">發布活動</RouterLink></li>
              <li><RouterLink :to="{ name: 'checkin-dashboard' }">簽到管理</RouterLink></li>
              <li><RouterLink :to="{ name: 'draft-box-container' }">草稿箱</RouterLink></li>
              <li><RouterLink :to="{ name: 'admin-discount-settings' }">折扣設定</RouterLink></li>
              <li><RouterLink :to="{ name: 'registrations-overview-container' }">查看活動報名表單</RouterLink></li>
            </ul>
          </li>
          <li><RouterLink :to="{ name: 'leave-request' }">請假管理</RouterLink></li>
          <li><RouterLink :to="{ name: 'admin-payments' }">繳費管理</RouterLink></li>
          <li><RouterLink :to="{ name: 'admin-users' }">用戶管理</RouterLink></li>
        </ul>
        <button class="revoke-btn" @click="handleRevoke">退出管理模式</button>
        <hr class="divider" />
        <p class="menu-label">會員功能</p>
      </div>

      <!-- 管理員帳號未提升 -->
      <div v-else-if="isAdminAccount && !adminElevated">
        <button class="elevate-btn" @click="openModal">
          <span class="lock-icon">🔒</span>進入管理模式
        </button>
        <hr class="divider" />
      </div>

      <!-- 會員選單 -->
      <ul>
        <li><RouterLink :to="{ name: 'activity-registration-list-container' }">我要報名</RouterLink></li>
        <li><RouterLink :to="{ name: 'pending-payments' }">我要繳費</RouterLink></li>
        <li><RouterLink :to="{ name: 'leave-request-member' }">我要請假</RouterLink></li>
        <li><RouterLink :to="{ name: 'my-registrations' }">我的活動</RouterLink></li>
        <li><RouterLink :to="{ name: 'my-coupons' }">我的優惠券</RouterLink></li>
      </ul>
    </nav>
  </aside>

  <!-- MFA Modal -->
  <Teleport to="body">
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="mfa-modal">

        <!-- Progress bar -->
        <div class="progress-bar">
          <div class="progress-step" :class="{ active: step >= 1, done: step > 1 }">
            <span class="step-num">{{ step > 1 ? '✓' : '1' }}</span>
            <span class="step-label">密碼驗證</span>
          </div>
          <div class="progress-line" :class="{ active: step > 1 }"></div>
          <div class="progress-step" :class="{ active: step >= 2, done: step > 2 }">
            <span class="step-num">{{ step > 2 ? '✓' : '2' }}</span>
            <span class="step-label">選擇方式</span>
          </div>
          <div class="progress-line" :class="{ active: step > 2 }"></div>
          <div class="progress-step" :class="{ active: step >= 3 }">
            <span class="step-num">3</span>
            <span class="step-label">輸入驗證碼</span>
          </div>
        </div>

        <!-- ── Step 1: 密碼 ── -->
        <div v-if="step === 1" class="modal-content">
          <div class="modal-icon">🔐</div>
          <h3>管理員身份驗證</h3>
          <p class="modal-sub">請先確認您的登入密碼</p>
          <div class="field-group">
            <label class="field-label">帳號</label>
            <div class="field-static">{{ userStore.userEmail }}</div>
          </div>
          <div class="field-group">
            <label class="field-label">密碼</label>
            <input
              v-model="elevatePassword"
              type="password"
              class="field-input"
              placeholder="輸入登入密碼"
              @keyup.enter="handlePasswordVerify"
              autofocus
            />
          </div>
          <p v-if="passwordError" class="error-msg">{{ passwordError }}</p>
          <div class="modal-actions">
            <button class="btn-cancel" @click="closeModal">取消</button>
            <button class="btn-primary" @click="handlePasswordVerify" :disabled="passwordLoading">
              {{ passwordLoading ? '驗證中...' : '下一步 →' }}
            </button>
          </div>
        </div>

        <!-- ── Step 2: 選擇 MFA 方式 ── -->
        <div v-if="step === 2" class="modal-content">
          <div class="modal-icon">📨</div>
          <h3>選擇驗證方式</h3>
          <p class="modal-sub">選擇接收驗證碼的方式</p>
          <div class="method-options">
            <label class="method-card" :class="{ selected: mfaMethod === 'EMAIL' }">
              <input type="radio" v-model="mfaMethod" value="EMAIL" />
              <div class="method-content">
                <span class="method-icon">✉️</span>
                <div>
                  <div class="method-title">電子信箱</div>
                  <div class="method-desc">驗證碼將寄至您的信箱</div>
                </div>
              </div>
            </label>
            <label class="method-card disabled" title="需要 SMS 服務設定">
              <input type="radio" v-model="mfaMethod" value="PHONE" disabled />
              <div class="method-content">
                <span class="method-icon">📱</span>
                <div>
                  <div class="method-title">手機簡訊 <span class="badge-unavail">需 SMS 設定</span></div>
                  <div class="method-desc">驗證碼將以簡訊傳送</div>
                </div>
              </div>
            </label>
          </div>
          <p v-if="sendError" class="error-msg">{{ sendError }}</p>
          <div class="modal-actions">
            <button class="btn-cancel" @click="backToStep(1)">← 上一步</button>
            <button class="btn-primary" @click="handleSendCode" :disabled="sendLoading">
              {{ sendLoading ? '發送中...' : '發送驗證碼' }}
            </button>
          </div>
        </div>

        <!-- ── Step 3: 輸入驗證碼 ── -->
        <div v-if="step === 3" class="modal-content">
          <div class="modal-icon">🔑</div>
          <h3>輸入驗證碼</h3>
          <p class="modal-sub">
            驗證碼已發送至
            <span class="target-hint">{{ maskedTarget }}</span>
            <br/><small>5 分鐘內有效</small>
          </p>
          <div class="code-fields" @paste="handleCodePaste">
            <input
              v-for="(_, i) in codeInputs"
              :key="i"
              v-model="codeInputs[i]"
              type="text"
              maxlength="1"
              inputmode="numeric"
              class="code-input"
              :class="{ filled: codeInputs[i] }"
              @input="handleCodeInput(i, $event)"
              @keydown="handleCodeKeydown(i, $event)"
            />
          </div>
          <p v-if="codeError" class="error-msg">{{ codeError }}</p>
          <button class="resend-link" @click="backToStep(2)">重新選擇發送方式</button>
          <div class="modal-actions">
            <button class="btn-cancel" @click="closeModal">取消</button>
            <button class="btn-primary" @click="handleVerifyCode" :disabled="codeLoading || mfaCode.length < 6">
              {{ codeLoading ? '驗證中...' : '確認登入' }}
            </button>
          </div>
        </div>

      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.overlay {
  position: fixed; inset: 0;
  background: rgba(0,0,0,0.4); z-index: 900;
}
.sidebar {
  width: 220px; height: 100vh;
  background-color: rgba(0,0,0,0.7);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  color: #fff;
  display: flex; flex-direction: column;
  padding: 0; position: fixed;
  left: 0; top: 0; z-index: 1000;
  transform: translateX(-220px);
  transition: transform 0.3s ease;
  overflow-y: auto;
  border-right: 1px solid rgba(255,255,255,0.08);
}
.sidebar.open { transform: translateX(0); }
.sidebar-header {
  display: flex; align-items: center; gap: 0.6rem;
  height: 56px; padding: 0 1rem;
  border-bottom: 1px solid rgba(255,255,255,0.08); flex-shrink: 0;
}
.admin-mode-badge {
  display: flex; align-items: center; gap: 0.5rem;
  margin: 0.75rem 1rem 0.25rem;
  padding: 0.35rem 0.7rem;
  background: rgba(255,45,107,0.15);
  border: 1px solid rgba(255,45,107,0.35);
  border-radius: 4px; font-size: 0.72rem;
  font-weight: 600; letter-spacing: 0.05em; color: #ff6b9d;
}
.badge-dot {
  width: 6px; height: 6px; border-radius: 50%;
  background: #ff2d6b; box-shadow: 0 0 6px #ff2d6b;
  animation: pulse 1.5s infinite;
}
@keyframes pulse { 0%,100%{opacity:1} 50%{opacity:0.4} }
.elevate-btn {
  display: flex; align-items: center; gap: 0.5rem;
  width: calc(100% - 2rem); margin: 0.75rem 1rem 0.5rem;
  padding: 0.6rem 1rem;
  background: rgba(255,255,255,0.06);
  border: 1px dashed rgba(255,255,255,0.25);
  border-radius: 4px; color: rgba(255,255,255,0.6);
  font-size: 0.8rem; cursor: pointer; transition: all 0.2s; text-align: left;
}
.elevate-btn:hover { background: rgba(255,45,107,0.12); border-color: rgba(255,45,107,0.4); color: #ff6b9d; }
.lock-icon { font-size: 0.9rem; }
.revoke-btn {
  display: block; width: calc(100% - 2rem);
  margin: 0.5rem 1rem 0.75rem; padding: 0.45rem 1rem;
  background: transparent; border: 1px solid rgba(255,255,255,0.15);
  border-radius: 4px; color: rgba(255,255,255,0.4);
  font-size: 0.72rem; cursor: pointer; transition: all 0.2s; text-align: center;
}
.revoke-btn:hover { border-color: rgba(255,255,255,0.35); color: rgba(255,255,255,0.7); }
.divider { border: none; border-top: 1px solid rgba(255,255,255,0.08); margin: 0.5rem 1rem; }
.toggle-btn { background: transparent; border: none; cursor: pointer; padding: 0.25rem; display: flex; align-items: center; }
.hamburger-icon { display: flex; flex-direction: column; gap: 4px; width: 20px; }
.hamburger-icon span { display: block; height: 2px; background: #fff; border-radius: 2px; transition: all 0.3s; }
.hamburger-icon.open span:nth-child(1) { transform: rotate(45deg) translate(4px,4px); }
.hamburger-icon.open span:nth-child(2) { opacity: 0; }
.hamburger-icon.open span:nth-child(3) { transform: rotate(-45deg) translate(4px,-4px); }
.user-name { font-size: 0.85rem; color: rgba(255,255,255,0.7); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
nav { padding: 0.75rem 0; flex: 1; }
.menu-label { font-size: 0.65rem; letter-spacing: 0.12em; text-transform: uppercase; color: rgba(255,255,255,0.35); padding: 0.5rem 1rem 0.25rem; margin: 0; }
nav ul { list-style: none; padding: 0; margin: 0; }
nav ul li a { display: block; padding: 0.6rem 1rem; color: rgba(255,255,255,0.75); text-decoration: none; font-size: 0.88rem; transition: all 0.2s; border-left: 2px solid transparent; }
nav ul li a:hover, nav ul li a.router-link-active { color: #fff; background: rgba(255,255,255,0.06); border-left-color: #ff2d6b; }

/* 活動管理子選單 */
.submenu-toggle {
  display: flex; align-items: center; justify-content: space-between;
  width: 100%; padding: 0.6rem 1rem;
  background: transparent; border: none; border-left: 2px solid transparent;
  color: rgba(255,255,255,0.75); font-size: 0.88rem; cursor: pointer;
  transition: all 0.2s; text-align: left;
}
.submenu-toggle:hover { color: #fff; background: rgba(255,255,255,0.06); border-left-color: #ff2d6b; }
.submenu-parent.open > .submenu-toggle { color: #fff; border-left-color: rgba(255,45,107,0.5); }
.submenu-arrow {
  font-size: 0.65rem; color: rgba(255,255,255,0.35);
  transition: transform 0.25s;
  display: inline-block;
}
.submenu-parent.open .submenu-arrow { transform: rotate(90deg); color: #ff6b9d; }
.submenu {
  list-style: none; padding: 0; margin: 0;
  background: rgba(0,0,0,0.2);
  border-left: 2px solid rgba(255,45,107,0.25);
  margin-left: 1rem;
}
.submenu li a {
  padding: 0.45rem 0.85rem; font-size: 0.82rem;
  color: rgba(255,255,255,0.55); border-left: none;
}
.submenu li a:hover, .submenu li a.router-link-active {
  color: #ff6b9d; background: rgba(255,45,107,0.07); border-left: none;
}

/* ===== MFA Modal ===== */
.modal-overlay {
  position: fixed; inset: 0;
  background: rgba(0,0,0,0.7);
  backdrop-filter: blur(6px);
  z-index: 2000;
  display: flex; align-items: center; justify-content: center; padding: 1rem;
}
.mfa-modal {
  background: #161616;
  border: 1px solid rgba(255,255,255,0.1);
  border-radius: 14px; width: 100%; max-width: 400px;
  overflow: hidden;
  box-shadow: 0 32px 80px rgba(0,0,0,0.6);
}

/* Progress */
.progress-bar {
  display: flex; align-items: center;
  padding: 1.25rem 1.5rem 0;
  gap: 0;
}
.progress-step {
  display: flex; flex-direction: column; align-items: center; gap: 0.3rem;
  flex-shrink: 0;
}
.step-num {
  width: 26px; height: 26px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 0.72rem; font-weight: 700;
  background: rgba(255,255,255,0.08);
  color: rgba(255,255,255,0.3);
  border: 1.5px solid rgba(255,255,255,0.1);
  transition: all 0.3s;
}
.progress-step.active .step-num {
  background: #ff2d6b; color: #fff;
  border-color: #ff2d6b;
  box-shadow: 0 0 12px rgba(255,45,107,0.4);
}
.progress-step.done .step-num {
  background: rgba(255,45,107,0.2); color: #ff6b9d;
  border-color: rgba(255,45,107,0.4);
}
.step-label {
  font-size: 0.58rem; color: rgba(255,255,255,0.3);
  letter-spacing: 0.04em; white-space: nowrap;
}
.progress-step.active .step-label { color: rgba(255,255,255,0.7); }
.progress-line {
  flex: 1; height: 1.5px;
  background: rgba(255,255,255,0.1);
  margin: 0 0.4rem; margin-bottom: 1.25rem;
  transition: background 0.3s;
}
.progress-line.active { background: rgba(255,45,107,0.5); }

/* Modal content */
.modal-content {
  padding: 1.25rem 1.5rem 1.5rem;
  display: flex; flex-direction: column; gap: 1rem;
  text-align: center;
}
.modal-icon { font-size: 2rem; }
.modal-content h3 {
  margin: 0; font-size: 1.05rem; font-weight: 700; color: #fff; letter-spacing: 0.02em;
}
.modal-sub { margin: 0; font-size: 0.8rem; color: rgba(255,255,255,0.45); line-height: 1.6; }
.target-hint { color: rgba(255,255,255,0.75); font-weight: 600; }

/* Fields */
.field-group { display: flex; flex-direction: column; gap: 0.35rem; text-align: left; }
.field-label { font-size: 0.65rem; letter-spacing: 0.1em; color: rgba(255,255,255,0.4); text-transform: uppercase; }
.field-static {
  font-size: 0.85rem; color: rgba(255,255,255,0.55);
  padding: 0.5rem 0.75rem;
  background: rgba(255,255,255,0.04); border: 1px solid rgba(255,255,255,0.08); border-radius: 6px;
}
.field-input {
  background: rgba(255,255,255,0.06); border: 1px solid rgba(255,255,255,0.15);
  border-radius: 6px; padding: 0.55rem 0.75rem; color: #fff;
  font-size: 0.88rem; outline: none; transition: border-color 0.2s;
  width: 100%; box-sizing: border-box;
}
.field-input:focus { border-color: #ff2d6b; }
.field-input::placeholder { color: rgba(255,255,255,0.22); }

/* Method options */
.method-options { display: flex; flex-direction: column; gap: 0.6rem; text-align: left; }
.method-card {
  display: flex; align-items: center;
  padding: 0.75rem 1rem;
  background: rgba(255,255,255,0.04);
  border: 1.5px solid rgba(255,255,255,0.1);
  border-radius: 8px; cursor: pointer; transition: all 0.2s;
}
.method-card:hover:not(.disabled) { border-color: rgba(255,45,107,0.4); background: rgba(255,45,107,0.06); }
.method-card.selected { border-color: #ff2d6b; background: rgba(255,45,107,0.1); }
.method-card.disabled { opacity: 0.45; cursor: not-allowed; }
.method-card input[type="radio"] { display: none; }
.method-content { display: flex; align-items: center; gap: 0.75rem; width: 100%; }
.method-icon { font-size: 1.4rem; flex-shrink: 0; }
.method-title { font-size: 0.88rem; font-weight: 600; color: #fff; display: flex; align-items: center; gap: 0.5rem; }
.method-desc { font-size: 0.72rem; color: rgba(255,255,255,0.4); margin-top: 0.15rem; }
.badge-unavail {
  font-size: 0.58rem; background: rgba(255,255,255,0.08);
  color: rgba(255,255,255,0.4); border: 1px solid rgba(255,255,255,0.12);
  padding: 0.1rem 0.4rem; border-radius: 3px; letter-spacing: 0.04em;
}

/* Code inputs */
.code-fields { display: flex; gap: 0.5rem; justify-content: center; }
.code-input {
  width: 44px; height: 52px;
  background: rgba(255,255,255,0.06); border: 1.5px solid rgba(255,255,255,0.15);
  border-radius: 8px; color: #fff; font-size: 1.4rem; font-weight: 700;
  text-align: center; outline: none; transition: all 0.2s;
  caret-color: #ff2d6b;
}
.code-input:focus { border-color: #ff2d6b; background: rgba(255,45,107,0.08); }
.code-input.filled { border-color: rgba(255,45,107,0.5); }

.resend-link {
  background: none; border: none; color: rgba(255,255,255,0.4);
  font-size: 0.75rem; cursor: pointer; text-decoration: underline;
  padding: 0; transition: color 0.2s;
}
.resend-link:hover { color: rgba(255,255,255,0.7); }

/* Error */
.error-msg {
  margin: 0; font-size: 0.78rem; color: #ff6b6b;
  padding: 0.4rem 0.6rem; text-align: left;
  background: rgba(255,60,60,0.1); border: 1px solid rgba(255,60,60,0.2); border-radius: 5px;
}

/* Actions */
.modal-actions { display: flex; gap: 0.75rem; }
.btn-cancel {
  flex: 1; padding: 0.62rem;
  background: transparent; border: 1px solid rgba(255,255,255,0.15);
  border-radius: 7px; color: rgba(255,255,255,0.5);
  font-size: 0.85rem; cursor: pointer; transition: all 0.2s;
}
.btn-cancel:hover { border-color: rgba(255,255,255,0.3); color: rgba(255,255,255,0.8); }
.btn-primary {
  flex: 2; padding: 0.62rem;
  background: #ff2d6b; border: none;
  border-radius: 7px; color: #fff;
  font-size: 0.85rem; font-weight: 700; cursor: pointer; transition: all 0.2s;
}
.btn-primary:hover:not(:disabled) { background: #e01f5a; }
.btn-primary:disabled { opacity: 0.45; cursor: not-allowed; }
</style>
