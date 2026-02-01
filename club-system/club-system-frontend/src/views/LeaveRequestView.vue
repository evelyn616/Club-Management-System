<template>
  <div class="container">
    <div class="header">
      <h1>社團請假管理系統</h1>
      <p class="status-info">後端狀態：<span class="online-tag">已串接 User 實體</span></p>
    </div>

    <div class="nav-grid">
      <router-link to="/leave-activity" class="nav-card">
        <div class="icon-box apply">
          <i class="fas fa-edit"></i>
        </div>
        <h3>申請請假</h3>
        <p>會員提交新的請假申請單</p>
      </router-link>

      <router-link to="/leave-request" class="nav-card">
        <div class="icon-box admin">
          <i class="fas fa-user-shield"></i>
        </div>
        <h3>管理員審核</h3>
        <p>進入審核後台處理申請單</p>
      </router-link>

      <router-link to="/leave-request-member" class="nav-card">
        <div class="icon-box view">
          <i class="fas fa-search"></i>
        </div>
        <h3>查看請假紀錄</h3>
        <p>快速查詢個人歷史請假單</p>
      </router-link>
    </div>

    <div class="footer-note">
      <router-link to="/origin-leave" class="btn-text">查看原始備份頁面</router-link>
    </div>
  </div>
</template>

<style scoped>
.container { 
  padding: 60px 20px; 
  background-color: #f8fafc; 
  min-height: 100vh; 
  text-align: center;
}

.header h1 { color: #1e293b; font-size: 2.5rem; margin-bottom: 10px; }
.status-info { color: #94a3b8; margin-bottom: 40px; }
.online-tag { color: #10b981; font-weight: bold; }

/* 導覽卡片佈局 */
.nav-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 25px;
  max-width: 1000px;
  margin: 0 auto;
}

/* 卡片樣式 (router-link 本質是 <a> 標籤) */
.nav-card {
  background: white;
  padding: 40px 20px;
  border-radius: 20px;
  text-decoration: none;
  color: inherit;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid #e2e8f0;
}

.nav-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
  border-color: #3b82f6;
}

/* 圖示方塊 */
.icon-box {
  width: 70px;
  height: 70px;
  border-radius: 18px;
  margin: 0 auto 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.8rem;
}

.icon-box.apply { background: #eff6ff; color: #3b82f6; }
.icon-box.admin { background: #fef2f2; color: #ef4444; }
.icon-box.view { background: #ecfdf5; color: #10b981; }

.nav-card h3 { margin-bottom: 10px; color: #1e293b; }
.nav-card p { color: #64748b; font-size: 0.9rem; line-height: 1.5; }

.footer-note { margin-top: 50px; }
.btn-text { color: #94a3b8; text-decoration: none; font-size: 0.8rem; }
.btn-text:hover { color: #64748b; text-decoration: underline; }
</style>