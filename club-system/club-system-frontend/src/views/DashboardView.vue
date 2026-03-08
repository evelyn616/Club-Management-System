<template>
  <div class="dashboard">
    <nav class="navbar">
      <div class="nav-container">
        <h2 class="logo">社團管理系統</h2>
        <div class="nav-right">
          <span class="user-name">{{ userStore.userName }}</span>
          <router-link to="/profile" class="nav-link">個人資料</router-link>
          <button @click="handleLogout" class="btn-logout">登出</button>
        </div>
      </div>
    </nav>

    <main class="main-content">
      <div class="container">
        <div class="club-header">
          <h1 class="club-title">CLUB</h1>
        </div>
        <div class="welcome-section">
          <h1>歡迎回來，{{ userStore.userName }}</h1>
          <p class="subtitle">Welcome back, {{ userStore.userName}}</p>
        </div>

        <div class="quick-actions">
          <h2 class="section-title">ACTIVITIES</h2>

          <div class="hero-actions">
            <router-link to="/activity-registration-list-container" class="action-card hero-card card-with-img">
              <div class="card-image-wrapper">
                <img src="@/assets/d1.jpg" alt="活動報名" class="card-img" />
              </div>
              <div class="card-content-overlay">
                <h3>活動報名</h3>
                <p>查看並報名最新活動</p>
              </div>
            </router-link>

            <router-link to="/my-registrations" class="action-card hero-card card-text-only">
              <div class="card-content">
                <h3>
                  我的報名
                  <span class="en-title">My Registrations</span>
                </h3>
                <p>
                  查看已報名的活動
                  <span class="en-subtitle">Check out my activities</span>
                </p>
              </div>
            </router-link>

          </div>
            
          <div class="info-divider">
            <span class="info-text">Mine</span>
          </div>

          <div class="action-grid">

            <router-link to="/pending-payments" class="action-card">
              <h3>待繳費用</h3>
              <p>查看待繳費項目</p>
            </router-link>

            <router-link to="/payment-history" class="action-card">
              <h3>繳費歷史</h3>
              <p>查看所有繳費記錄</p>
            </router-link>

            <router-link to="/profile" class="action-card">
              <h3>個人資料</h3>
              <p>管理您的個人資訊</p>
            </router-link>

            <router-link to="/leave-activity" class="action-card">
              <h3>我要請假</h3>
              <p>活動請假</p>
            </router-link>

            <router-link to="/leave-request-member" class="action-card">
              <h3>請假紀錄</h3>
              <p>查看您的請假紀錄</p>
            </router-link>

            <router-link to="/leave-request" class="action-card">
              <h3>管理請假</h3>
              <p>管理所有請假紀錄</p>
            </router-link>

          </div>

          <div class="info-divider">
            <span class="info-text">Info</span>
          </div>

          <div class="info-gallery">
            <div class="info-item">
              <div class="info-img-box"><img src="@/assets/d2.jpg" alt="Info 1"></div>
              <p class="info-caption">社團最新公告事項</p>
            </div>
            <div class="info-item">
              <div class="info-img-box"><img src="@/assets/d3.jpg" alt="Info 2"></div>
              <p class="info-caption">年度活動回顧特輯</p>
            </div>
            <div class="info-item">
              <div class="info-img-box"><img src="@/assets/d4.jpg" alt="Info 3"></div>
              <p class="info-caption">場地租借規範說明</p>
            </div>

            <div class="info-item">
              <div class="info-img-box"><img src="@/assets/d5.jpg" alt="Info 4"></div>
              <p class="info-caption">幹部招募面試名單</p>
            </div>
            <div class="info-item">
              <div class="info-img-box"><img src="@/assets/d6.jpg" alt="Info 5"></div>
              <p class="info-caption">社費收支透明報表</p>
            </div>
            <div class="info-item">
              <div class="info-img-box"><img src="@/assets/d7.jpg" alt="Info 6"></div>
              <p class="info-caption">常見問題 FAQ 指南</p>
            </div>
          </div>

        </div>
      </div>
    </main>
    <footer class="footer">
      <div class="footer-container">
        <div class="footer-section brand">
          <h2 class="footer-logo">CLUB SYSTEM</h2>
          <p class="footer-desc">專業的社團管理解決方案，致力於簡化流程，提升社團運作效率。</p>
        </div>

        <div class="footer-section contact">
          <h4 class="footer-label">Contact</h4>
          <p>電話：(02) 2345-6789</p>
          <p>信箱：service@club.edu.tw</p>
          <p>地址：台北市信義區校園路 100 號</p>
        </div>

        <div class="footer-section hours">
          <h4 class="footer-label">Service Hours</h4>
          <p>週一至週五：09:00 - 18:00</p>
          <p>週六、週日：休息</p>
        </div>

        <div class="footer-section links">
          <h4 class="footer-label">Follow Us</h4>
          <div class="social-links">
            <a href="#">FB</a>
            <a href="#">IG</a>
            <a href="#">LINE</a>
          </div>
        </div>
      </div>
      <div class="footer-bottom">
        <p>&copy; 2026 CLUB MANAGEMENT SYSTEM. DESIGNED FOR EXCELLENCE.</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const handleLogout = () => {
  userStore.logout()
  router.push('/')
}
</script>

<style scoped>
/* CLUB 標誌樣式 - 極致緊湊規整版 */
.club-header {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 0.25rem;  /* 極小間距 */
  padding: 0;              /* 完全移除內距 */
}

.club-title {
  font-family: 'Inter', 'Segoe UI', system-ui, -apple-system, sans-serif;
  font-size: 4rem;       /* 稍微縮小一點點 */
  font-weight: 900;        /* 極粗體，增加份量感 */
  color: #1a1a1a;
  letter-spacing: 0.6rem;  /* 保持規整的字母間距 */
  margin: 0;
  line-height: 0.9;        /* 緊縮行高，移除字體上下的預留空間 */
  text-transform: uppercase;
  text-align: center;
}

/* 針對行動裝置 */


.dashboard {
  min-height: 100vh;
  background: #fafafa;
}

/* Navbar */
.navbar {
  background: white;
  border-bottom: 1px solid #e0e0e0;
  padding: 1rem 0;
}

.nav-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  font-size: 1.25rem;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}

.nav-right {
  display: flex;
  gap: 1.5rem;
  align-items: center;
}

.user-name {
  color: #666;
  font-weight: 500;
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

.btn-logout {
  padding: 0.5rem 1.25rem;
  background: white;
  color: #1a1a1a;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-logout:hover {
  background: #f5f5f5;
  border-color: #ccc;
}

/* Main Content */
.main-content {
  padding: 3rem 0;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
}

.welcome-section {
  margin-bottom: 3rem;
}

.welcome-section h1 {
  font-size: 2.5rem;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 0.5rem 0;
}

.subtitle {
  color: #666;
  font-size: 1.1rem;
  margin: 0;
}

/* ACTIVITIES 標題 - 減少行高讓重疊更緊湊 */
.section-title {
  font-family: 'Impact', sans-serif;
  font-size: 4rem;
  color: #1a1a1a;
  text-align: center;
  margin: 0;
  line-height: 0.8;         /* 緊縮行高 */
  position: relative;
  z-index: 2;
  text-transform: uppercase;
}

/* 英雄區塊容器 - 重疊量極少，並增加最大寬度 */
.hero-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.5rem;
  max-width: 1000px;
  margin: -0.1rem auto 2.5rem; /* 維持微小重疊 */
  padding: 0 1rem;
  position: relative;
  z-index: 1;
}

/* --- 英雄卡片基礎樣式 --- */
.hero-card {
  min-height: 240px;        /* 增加一點高度，不扁 */
  position: relative;        /* 關鍵：作為重疊層的定位基準 */
  background: #1a1a1a !important; /* 黑色背景，防止圖片未載入時的突兀 */
  border: none !important;
  text-decoration: none !important;
  overflow: hidden;         /* 關鍵：裁切圖片 */
  cursor: pointer !important; /* 恢復游標手型 */
  border-radius: 0 !important;
}

/* --- 活動報名：加圖卡片特殊樣式 --- */
.card-with-img {
  padding: 0 !important;    /* 圖片需填滿 */
}

/* 1. 圖片容器 (絕對定位填滿) */
.card-image-wrapper {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;               /* 圖片在下方 */
  transition: filter 0.3s ease, transform 0.3s ease; /* 變暗與縮放動畫 */
}

.card-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  /* 如果原本就是黑白圖可以不加灰階濾鏡，但加上去更顯規整 */
  filter: grayscale(100%); 
}

/* 2. 文字覆蓋層 (預設隱藏) */
.card-content-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 2;               /* 文字在圖片上方 */
  display: flex;
  flex-direction: column;
  justify-content: center;  /* 文字置中 */
  align-items: center;      /* 文字置中 */
  text-align: center;
  padding: 2rem;
  box-sizing: border-box;
  
  /* 預設隱藏文字 */
  opacity: 0; 
  transition: opacity 0.3s ease, transform 0.3s ease; /* 淡入動畫 */
  /* 讓文字稍微往下移一點點，hover 時會往上浮動 */
  transform: translateY(10px); 
}

/* 文字層顏色 */
.card-content-overlay h3 {
  color: #ffffff !important;
  font-size: 2rem;
  margin: 0 0 0.5rem 0;
  font-weight: 700;
}

.card-content-overlay p {
  color: #cccccc !important;
  font-size: 1.1rem;
  margin: 0;
}

/* --- ★核心：Hover 時的互動效果 ★ --- */
.card-with-img:hover .card-image-wrapper {
  /* 圖片變暗 */
  filter: brightness(0.3) grayscale(100%);
  /* 可選：圖片稍微放大，增加張力 */
  transform: scale(1.05); 
}

.card-with-img:hover .card-content-overlay {
  /* 顯示文字 */
  opacity: 1; 
  /* 文字回到原位 */
  transform: translateY(0); 
}

/* --- 我的報名：純文字卡片 (維持黑底白字) --- */
.card-text-only .card-content {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: flex-start;
  text-align: left;
  position: relative;
  z-index: 5; /* 提高層級，確保字在最上面 */
}

.card-text-only {
  background: #1a1a1a !important; /* 確保底色是黑的 */
  display: block;                /* 確保它是區塊顯示 */
  position: relative;
  border-radius: 0 !important;
}

.card-text-only .card-content h3 {
  color: #ffffff !important;
  font-size: 1.8rem !important;
  font-weight: 700 !important;
  margin: 0 0 0.5rem 0 !important;
  display: flex;
  flex-direction: column; /* 讓中文和英文分行顯示 */
}

/* 英文標題樣式 (My Registrations) */
.en-title {
  font-family: 'Inter', sans-serif; /* 建議用簡單的無襯線字體 */
  font-size: 0.9rem;              /* 英文縮小一點 */
  text-transform: uppercase;      /* 全大寫更有規整感 */
  letter-spacing: 0.1rem;
  color: #888888;                 /* 灰色，做出層次感 */
  margin-top: 0.2rem;
  font-weight: 400;
}

.card-text-only .card-content p {
  color: #ffffff !important;      /* 中文說明改為純白 */
  font-size: 1.1rem !important;
  margin-top: 1rem !important;
  display: flex;
  flex-direction: column;
}

/* 英文副標題樣式 (Check out my activities) */
.en-subtitle {
  font-size: 0.8rem;
  color: #888888;
  margin-top: 0.2rem;
  font-weight: 300;
  letter-spacing: 0.05rem;
}

/* Info 小標題：放在英雄卡片下方 */
.info-divider {
  max-width: 1000px;        /* 與上方卡片區域完全同寬 */
  margin: 3rem auto 1.5rem; /* 增加上方間距，與大卡片拉開；下方留空給功能格 */
  padding: 0 1rem;          /* 保持左右對齊縮排 */
  
  /* 製作那一長條線 */
  border-bottom: 1px solid #1a1a1a; 
  display: flex;            /* 使用 flex 讓文字對齊 */
  align-items: flex-end;    /* 讓文字貼在線上 */
  padding-bottom: 8px;      /* 文字與長線的間距 */
}

.info-text {
  font-family: 'Copperplate', 'Copperplate Gothic Light', serif;
  font-size: 1.2rem;
  font-weight: bold;
  color: #1a1a1a;
  
  /* 寬扁規整感 */
  letter-spacing: 0.5rem;   /* 字距拉大，產生寬扁的質感 */
  text-transform: uppercase;
  display: inline-block;
  

}

.action-grid {
  display: grid;
  /* 強制三列，每列等寬 */
  grid-template-columns: repeat(3, 1fr); 
  gap: 1.5rem;
  max-width: 1000px;        /* 確保與 Info 長線對齊 */
  margin: 0 auto 4rem;      /* 置中並留底部空間 */
  padding: 0 1rem;          /* 與 Info 邊距一致 */
}


.action-card {
  background: white;
  padding: 2rem;
  border-radius: 0px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  text-decoration: none;
  transition: all 0.2s;
  border: 1px solid transparent;
}

.action-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-color: #e0e0e0;
  transform: translateY(-2px);
}

.action-card h3 {
  font-size: 1.25rem;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 0.5rem 0;
}

.action-card p {
  color: #666;
  margin: 0;
  line-height: 1.5;
}

.footer {
  background: #1a1a1a; /* 使用深黑色背景 */
  color: #ffffff;
  padding: 4rem 0 2rem;
  margin-top: 5rem;
  border-top: 4px solid #ff0000; /* 加入一條紅線呼應活動報名的紅色 */
}

.footer-container {
  max-width: 1000px; /* 與上方所有內容對齊 */
  margin: 0 auto;
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr; /* 品牌佔比大一點 */
  gap: 3rem;
  padding: 0 1rem;
}

.footer-section h4.footer-label {
  font-family: 'Copperplate', serif;
  font-size: 1rem;
  letter-spacing: 0.2rem;
  text-transform: uppercase;
  color: #ff0000; /* 標籤用紅色增加亮點 */
  margin-bottom: 1.5rem;
}

.footer-logo {
  font-family: 'Inter', sans-serif;
  font-weight: 900;
  letter-spacing: 0.3rem;
  margin-bottom: 1rem;
}

.footer-section p {
  color: #bbbbbb;
  font-size: 0.9rem;
  line-height: 1.8;
  margin: 0.3rem 0;
}

.social-links {
  display: flex;
  gap: 1rem;
}

.social-links a {
  color: #ffffff;
  text-decoration: none;
  font-weight: bold;
  font-size: 0.8rem;
  border: 1px solid #444;
  padding: 0.5rem 0.8rem;
  transition: all 0.3s;
}

.social-links a:hover {
  background: #ffffff;
  color: #1a1a1a;
  border-color: #ffffff;
}

.footer-bottom {
  max-width: 1000px;
  margin: 4rem auto 0;
  padding: 2rem 1rem 0;
  border-top: 1px solid #333;
  text-align: center;
}

.footer-bottom p {
  font-size: 0.75rem;
  letter-spacing: 0.1rem;
  color: #666;
}

/* 圖文資訊展示容器 */
.info-gallery {
  display: grid;
  grid-template-columns: repeat(3, 1fr); /* 強制三個一行 */
  gap: 0.5rem;           /* 卡片之間的間距，大一點比較有雜誌感 */
  max-width: 1000px;
  margin: 0 auto 5rem; /* 與底部 Footer 留出空間 */
  padding: 1.5rem 1rem;
}

/* 單個卡片項目 */
.info-item {
  display: flex;
  flex-direction: column;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.info-item:hover {
  transform: translateY(-5px); /* 輕微浮動效果 */
}

/* 圖片包裝盒 - 確保比例統一 (例如 16:9 或 4:3) */
.info-img-box {
  width: 100%;
  aspect-ratio: 1/1; /* 強制所有圖片比例一致 */
  overflow: hidden;
  background: #eee;    /* 圖片載入前的底色 */
  border-radius: 0;    /* 銳利直角 */
}

.info-img-box img {
  width: 100%;
  height: 100%;
  object-fit: cover;   /* 圖片裁切填滿，不變形 */
  filter: grayscale(100%);
  transition: filter 0.3s ease;
}

.info-item:hover .info-img-box img {
  filter: grayscale(0%); /* 恢復彩色，增加互動感 */
}

/* 下方小字樣式 */
.info-caption {
  margin-top: 0.8rem;
  font-size: 0.95rem;
  color: #1a1a1a;
  font-weight: 500;
  text-align: left;    /* 配合你之前的靠左風格 */
  letter-spacing: 0.05rem;
  border-left: 2px solid #000; /* 加入一個紅色小標記，與主題呼應 */
  padding-left: 0.8rem;
}

/* Responsive */
@media (max-width: 768px) {
  .nav-right {
    gap: 1rem;
  }

  .user-name {
    display: none;
  }

  .welcome-section h1 {
    font-size: 2rem;
  }

  .action-grid {
    grid-template-columns: 1fr;
  }

  .footer-container {
    grid-template-columns: 1fr;
    gap: 2rem;
    text-align: center;
  }
  .social-links {
    justify-content: center;
  }

  .club-header {
    margin-bottom: 0;      /* 手機端直接貼合 */
  }
  .club-title {
    font-size: 2.2rem;
    letter-spacing: 0.3rem;
  }

  .section-title {
    font-size: 2.5rem;
  }
  .hero-actions {
    grid-template-columns: 1fr;
    margin-top: -0.1rem;
  }
  .hero-card {
    min-height: 180px;
  }
  .card-with-img .card-image-wrapper, 
  .card-with-img .card-content {
    width: 100%;
    flex: none;
  }
  .card-with-img .card-image-wrapper {
    height: 100px;
  }
  .card-content {
    padding: 1rem;
    align-items: center;
    text-align: center;
  }

  .info-gallery {
    grid-template-columns: repeat(2, 1fr); /* 手機改為兩列正方形較好看 */
    gap: 0.5rem;
  }
}
</style>
