<template>
  <div class="my-registrations">
    <nav class="navbar" :class="{ 'navbar-hidden': navHidden }">
      <div class="nav-container">
        <router-link to="/dashboard" class="nav-logo">CLUB SYSTEM</router-link>
        <div class="nav-right">
          <span class="nav-username">{{ userStore.userName }}</span>
          <router-link to="/profile" class="nav-link">個人資料</router-link>
          <button @click="handleLogout" class="nav-logout">登出</button>
        </div>
      </div>
    </nav>

    <div class="page-header">
      <div class="header-left">
        <div class="header-label">
          <span class="label-line"></span>
          <span class="label-text">LEAVE APPLICATION</span>
        </div>
        <h1 class="page-title">
          <span class="title-line title-line-1">提交</span>
          <span class="title-line title-line-2"><span class="title-accent">請假申請單</span></span>
        </h1>
        <p class="page-subtitle">
          <span class="subtitle-inner">請確認申請資訊，提交後將進入審核流程</span>
        </p>
      </div>
      
      <div class="header-right">
        <div class="status-badge status-registered">
          <span class="status-dot"></span>
          填寫模式 ONLINE
        </div>
      </div>
    </div>

    <section class="form-container-custom">
      <div class="registration-card form-card-padding">
        <div class="card-index">REQUEST_INFO</div>
        
        <div class="form-grid">
          <div class="input-group">
            <label class="info-label">MEMBER NAME</label>
            <div class="input-wrapper">
              <input 
                :value="userStore.userName || '訪客'" 
                type="text" 
                readonly 
                class="custom-input readonly-style name-accent"
              >
              <input v-model="form.userId" type="hidden">
            </div>
          </div>

          <div class="input-group">
            <label class="info-label">LEAVE TYPE</label>
            <div class="input-wrapper">
              <select v-model="form.leaveType" class="custom-input select-style">
                <option value="事假">事假 PERSONAL</option>
                <option value="病假">病假 MEDICAL</option>
                <option value="公假">公假 OFFICIAL</option>
              </select>
            </div>
          </div>

          <div class="input-group full-width">
            <label class="info-label">TARGET ACTIVITY</label>
            <div class="input-wrapper">
              <input 
                :value="targetActivityTitle || '未取得活動名稱'" 
                type="text" 
                readonly 
                class="custom-input readonly-style activity-highlight"
              >
              <input v-model="form.activityId" type="hidden">
            </div>
          </div>

          <div class="input-group full-width">
            <label class="info-label">REASON FOR LEAVE</label>
            <div class="input-wrapper">
              <textarea 
                v-model="form.reason" 
                rows="5" 
                class="custom-input textarea-style" 
                placeholder="請輸入詳細請假原因..."
              ></textarea>
            </div>
          </div>
        </div>

        <div class="form-footer">
          <button @click="handleSubmit" class="submit-btn-cyber">
            SUBMIT APPLICATION <span class="btn-arrow">→</span>
          </button>
          <button @click="router.back()" class="cancel-btn-minimal">
            CANCEL
          </button>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

// 1. 接收從上一頁傳過來的資料
const targetActivityTitle = ref(route.query.activityTitle || '');

// 2. ✅ 修正：使用傳過來的值，而不是鎖死
const form = ref({
  userId: userStore.userId || route.query.userId || '',      // 優先使用傳過來的值
  activityId: Number(route.query.activityId) || '',   // 這個應該會正確接收
  leaveType: '事假',
  reason: ''
});

// ✅ 新增：頁面載入時檢查資料
onMounted(() => {
  console.log('=== AddLeave 頁面載入 ===');
  console.log('route.query:', route.query);
  console.log('form 初始值:', form.value);
  console.log('activityId 型別:', typeof form.value.activityId);

  if (!form.value.userId) {
    console.warn('⚠️ 警告：沒有接收到 activityId！');
    alert('請先登入後再進行請假申請');
    router.push('/login');
    return;
  }
  
  // 如果沒有 activityId，顯示警告
  if (!form.value.activityId) {
    console.warn('⚠️ 警告：沒有接收到 activityId！');
    alert('錯誤：缺少活動 ID，請從報名列表重新進入');
  }
});

// 提交邏輯
const handleSubmit = async () => {
  // ✅ 加強驗證
  console.log('準備提交的資料:', JSON.stringify(form.value));
  
  if (!form.value.userId) {
    return alert('缺少會員 ID');
  }
  
  if (!form.value.activityId) {
    return alert('缺少活動 ID，請從報名列表重新選擇活動');
  }
  
  if (!form.value.reason || form.value.reason.trim() === '') {
    return alert('請填寫請假原因');
  }
  if (!userStore.token) {
    alert('登入資訊已過期，請重新登入');
    router.push('/login');
    return;
  }

try {
    const response = await axios.post(
      'http://localhost:8080/api/leaves', 
      form.value, 
      {
        headers: { 'Authorization': `Bearer ${userStore.token}` }
      }
    );
    
    if (response.status === 201 || response.status === 200) {
      alert('請假申請已成功送出！');
      router.push('/dashboard'); 
    }
  } catch (error) {
    console.error("提交失敗:", error);
    
    // 1. 處理驗證過期
    if (error.response?.status === 401) {
      alert('驗證失敗，請重新登入');
      return;
    }

    // 2. 處理重複請假 (唯一性約束衝突)
    // 判斷後端傳回的錯誤字串是否包含 "duplicate" 或 "already exists"
    const responseData = JSON.stringify(error.response?.data || '');
    if (responseData.includes('duplicate') || responseData.includes('already exists')) {
      alert('⚠️ 您已經提交過此活動的請假申請，請勿重複提交！');
      router.push('/dashboard'); // 或是引導回列表頁
      return;
    }
    
    // 3. 一般錯誤處理
    let errorMsg = '提交失敗';
    if (error.response) {
      errorMsg = error.response.data?.message || error.response.data || `錯誤代碼: ${error.response.status}`;
    }
    alert(errorMsg);
  }
};
</script>

<style scoped>
/* 載入與之前頁面相同的字體 */
@import url('https://fonts.googleapis.com/css2?family=Bebas+Neue&family=Noto+Sans+TC:wght@400;700&family=Space+Mono:wght@400;700&display=swap');

/* 1. 修改最外層容器：移除 max-width，背景填滿 */
.my-registrations {
  min-height: 100vh;
  background: #ffffff; /* 確保純白 */
  font-family: 'Noto Sans TC', sans-serif;
  color: #0a0a0a;
  width: 100%;       /* 強制寬度 100% */
  margin: 0;         /* 移除置中的 auto */
  padding: 6rem 5% 5rem; /* 使用百分比 padding，讓左右留白隨螢幕縮放 */
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

/* --- Header Style (與列表頁同步) --- */
.page-header {
  display: flex; justify-content: space-between; align-items: flex-end;
  margin-bottom: 3rem; padding-bottom: 2rem;
  border-bottom: 2px solid #0a0a0a; flex-wrap: wrap; gap: 2rem;
}

.header-label { display: flex; align-items: center; gap: 0.6rem; margin-bottom: 1rem; }
.label-line { width: 2rem; height: 1px; background: #ff2d6b; }
.label-text { font-family: 'Space Mono', monospace; font-size: 0.65rem; color: #ff2d6b; letter-spacing: 0.2em; }
.label-num { font-family: 'Space Mono', monospace; font-size: 0.6rem; color: #ccc; margin-left: auto; }

.page-title {
  font-family: 'Bebas Neue', sans-serif; font-size: 5rem;
  line-height: 0.92; margin: 0; letter-spacing: 0.02em;
}
.title-accent { color: #ff2d6b; }
.page-subtitle { font-size: 0.8rem; color: #999; margin-top: 1rem; letter-spacing: 0.05em; }

/* --- Form Card Style --- */
.form-container-custom {
  max-width: 800px;
  margin: 0 auto;
}

.registration-card {
  background: #ffffff;
  border: 1px solid #eeeeee;
  position: relative;
  padding: 3rem;
  box-shadow: 0 10px 30px rgba(0,0,0,0.02);
}

.registration-card::before {
  content: ''; position: absolute; left: 0; top: 0; bottom: 0; width: 4px; background: #0a0a0a;
}

.card-index { font-family: 'Space Mono', monospace; font-size: 0.65rem; color: #444; margin-bottom: 2rem; letter-spacing: 0.1em; }

.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 2.5rem; }
.full-width { grid-column: span 2; }

/* --- Input Style --- */
.info-label {
  display: block;
  font-family: 'Space Mono', monospace;
  font-size: 0.7rem;
  color: #444;
  letter-spacing: 0.15em;
  margin-bottom: 0.8rem;
  text-transform: uppercase;
}

.custom-input {
  width: 100%;
  background: #fff;
  border: 1px solid #e0e0e0;
  padding: 1rem;
  font-size: 0.95rem;
  font-family: 'Noto Sans TC', sans-serif;
  transition: all 0.2s;
  outline: none;
}

.custom-input:focus {
  border-color: #ff2d6b;
  box-shadow: 0 0 0 4px rgba(255, 45, 107, 0.05);
}

.readonly-style {
  background: #fafafa;
  border-color: #f0f0f0;
  color: #888;
  cursor: not-allowed;
}

.name-accent { border-left: 3px solid #ff2d6b; font-weight: 700; color: #0a0a0a; }
.activity-highlight { font-weight: 700; color: #0a0a0a; }

.select-style { cursor: pointer; appearance: none; background-image: url("data:image/svg+xml,..."); }
.textarea-style { resize: vertical; min-height: 120px; }

/* --- Footer Buttons --- */
.form-footer {
  margin-top: 3rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.submit-btn-cyber {
  background: #0a0a0a;
  color: #fff;
  border: none;
  padding: 1.2rem;
  font-family: 'Space Mono', monospace;
  font-size: 0.9rem;
  font-weight: 700;
  letter-spacing: 0.1em;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
}

.submit-btn-cyber:hover {
  background: #ff2d6b;
  transform: translateY(-2px);
}

.cancel-btn-minimal {
  background: transparent;
  border: none;
  color: #aaa;
  font-family: 'Space Mono', monospace;
  font-size: 0.7rem;
  cursor: pointer;
  letter-spacing: 0.1em;
  padding: 0.5rem;
}

.cancel-btn-minimal:hover { color: #ff2d6b; }

/* Status Badge */
.status-badge {
  display: flex; align-items: center; gap: 0.5rem;
  font-family: 'Space Mono', monospace; font-size: 0.65rem;
  padding: 0.4rem 1rem; border-radius: 20px;
}
.status-registered { background: #f5f5f5; color: #0a0a0a; border: 1px solid #e0e0e0; }
.status-dot { width: 6px; height: 6px; background: #ff2d6b; border-radius: 50%; animation: pulse 1.5s infinite; }

@keyframes pulse {
  0% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.5); opacity: 0.5; }
  100% { transform: scale(1); opacity: 1; }
}

@media (max-width: 640px) {
  .page-title { font-size: 3rem; }
  .form-grid { grid-template-columns: 1fr; }
  .registration-card { padding: 1.5rem; }
}
</style>