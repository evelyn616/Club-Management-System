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

       
        <div class="hero-section" @mousemove="onMouseMove" @mouseleave="onMouseLeave">
          <!-- 黑白底圖 -->
          <div class="hero-bg hero-bw"></div>
          <!-- 滑鼠跟隨彩色顯色層 -->
          <div
            class="hero-bg hero-color"
            :style="{
              maskImage: `radial-gradient(circle ${reveal}px at ${mx}px ${my}px, black 0%, transparent 100%)`,
              WebkitMaskImage: `radial-gradient(circle ${reveal}px at ${mx}px ${my}px, black 0%, transparent 100%)`
            }"
          ></div>
          <!-- 頂部漸層（navbar 下方） -->
          <div class="hero-grad-top"></div>
          <!-- 底部漸層：圖片自然融入下方白色區域 -->
          <div class="hero-grad-bot"></div>

          <!-- 文字區（凸顯在圖片上，靠右） -->
          <div class="hero-text">
            <div class="hero-eyebrow">
              <span class="eyebrow-line"></span>
              <span class="eyebrow-word">WELCOME BACK</span>
              <span class="eyebrow-line"></span>
            </div>
            <h1 class="hero-name">
              <span
                v-for="(ch, i) in nameChars"
                :key="i"
                class="name-ch"
                :style="{ animationDelay: (i * 0.038 + 0.15) + 's' }"
              >{{ ch === ' ' ? '\u00A0' : ch }}</span>
            </h1>
            <p class="hero-clock">{{ clock }}</p>

            <router-link to="/activity-registration-list-container" class="hero-cta">
              <span class="cta-pulse"></span>
              <span class="cta-text">查看熱門活動</span>
              <span class="cta-arrow">↓</span>
            </router-link>
          </div>

          <!-- ACTIVITIES 大標題浮在圖片底部 / 重疊進內容區 -->
          <div class="hero-activities-title">
            <h2 class="section-title">ACTIVITIES</h2>
          </div>
        </div>
        <!-- ══ HERO END ══ -->

       
        <div class="quick-actions">

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
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// Hero 滑鼠效果
const mx = ref(0)
const my = ref(0)
const reveal = ref(0)
const onMouseMove = (e) => {
  const r = e.currentTarget.getBoundingClientRect()
  mx.value = e.clientX - r.left
  my.value = e.clientY - r.top
  reveal.value = 260
}
const onMouseLeave = () => { reveal.value = 0 }

// 即時時鐘
const clock = ref('')
let timer = null
const tick = () => {
  const n = new Date()
  const days = ['SUN','MON','TUE','WED','THU','FRI','SAT']
  clock.value = `${days[n.getDay()]}  ${String(n.getHours()).padStart(2,'0')}:${String(n.getMinutes()).padStart(2,'0')}:${String(n.getSeconds()).padStart(2,'0')}`
}

// 名字拆字動畫
const nameChars = computed(() => `歡迎回來，${userStore.userName || ''}`.split(''))

// 原始 logout
const handleLogout = () => {
  userStore.logout()
  router.push('/')
}

onMounted(() => { tick(); timer = setInterval(tick, 1000) })
onUnmounted(() => clearInterval(timer))
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Bebas+Neue&family=Space+Mono:wght@400;700&family=Noto+Sans+TC:wght@300;400;700&display=swap');


.navbar {
  position: fixed; top: 0; left: 0; right: 0; z-index: 100;
  padding: 1rem 0;
  background: transparent;
  transition: background 0.4s;
}
.nav-container {
  max-width: 1200px; margin: 0 auto; padding: 0 2rem;
  display: flex; justify-content: space-between; align-items: center;
}
.logo { font-size: 1.25rem; font-weight: 600; color: #fff; margin: 0; text-shadow: 0 1px 10px rgba(0,0,0,0.6); }
.nav-right { display: flex; gap: 1.5rem; align-items: center; }
.user-name { color: rgba(255,255,255,0.7); font-weight: 500; }
.nav-link { color: rgba(255,255,255,0.75); text-decoration: none; font-weight: 500; transition: color 0.2s; }
.nav-link:hover { color: #fff; }
.btn-logout {
  padding: 0.5rem 1.25rem; background: rgba(255,255,255,0.1);
  color: #fff; border: 1px solid rgba(255,255,255,0.35);
  border-radius: 6px; font-weight: 500; cursor: pointer; transition: all 0.2s;
}
.btn-logout:hover { background: rgba(255,255,255,0.22); }

/* main-content：移除頂部 padding，讓 hero 從最頂部開始 */
.main-content { padding: 0; }
.container { max-width: 1200px; margin: 0 auto; padding: 0 2rem; }

/* Hero 容器：全版高度，overflow 允許讓 ACTIVITIES 標題溢出重疊 */
.hero-section {
  position: relative;
  /* 破出 container 的左右 padding，真正左右滿版 */
  margin-left: calc(-50vw + 50%);
  margin-right: calc(-50vw + 50%);
  width: 100vw;
  height: 78vh;       /* 縮短讓 ACTIVITIES 露出更多 */
  min-height: 480px;
  overflow: visible;  /* 讓 ACTIVITIES 標題可以溢出往下重疊 */
  cursor: crosshair;
}

/* 圖片層 */
.hero-bg {
  position: absolute; inset: 0;
  background-image: url('https://scontent.ftpe3-1.fna.fbcdn.net/v/t39.30808-6/486824286_1066644962160551_318350841127563953_n.jpg?_nc_cat=104&ccb=1-7&_nc_sid=dd6889&_nc_ohc=7R9doMjhnm0Q7kNvwEtyY-a&_nc_oc=AdnDRQ_LhDz3lmcUncrkZ5EcWJAFdzVPqUc_ZYCM2IRYn_0x9HdbhKCHGZCtTaujepY&_nc_zt=23&_nc_ht=scontent.ftpe3-1.fna&_nc_gid=RTDgD4aZgC_v39vGFdK-mA&_nc_ss=8&oh=00_AfzUvfnvjx6m3TSqSK8i779rqgCm2jx-14TndyHM8RG88A&oe=69B9EC95');
  background-size: cover;
  background-position: center 20%;
  transition: transform 7s ease;
  /* 確保圖片不溢出到 ACTIVITIES 文字那邊 */
  overflow: hidden;
}
/* 讓 hover 縮放只在圖片上，用 clip */
.hero-section:hover .hero-bg { transform: scale(1.03); }
.hero-bw    { filter: grayscale(100%) brightness(0.48); }
.hero-color { filter: brightness(0.75) saturate(1.1); }

/* 頂部漸層 */
.hero-grad-top {
  position: absolute; top: 0; left: 0; right: 0; height: 160px;
  background: linear-gradient(to bottom, rgba(0,0,0,0.55), transparent);
  pointer-events: none; z-index: 2;
}
/* 底部漸層：圖片下半段淡出成白色，ACTIVITIES 標題從這裡凸顯 */
.hero-grad-bot {
  position: absolute; bottom: 0; left: 0; right: 0; height: 38%;
  background: linear-gradient(
    to bottom,
    transparent 0%,
    rgba(10,10,10,0.2) 30%,
    rgba(250,250,250,0.75) 70%,
    #fafafa 100%
  );
  pointer-events: none; z-index: 2;
}

/* 歡迎文字：絕對定位在圖片右下方 */
.hero-text {
  position: absolute;
  bottom: 50%;
  right: 5vw;
  z-index: 3;
  display: flex; flex-direction: column; gap: 0.55rem;
  align-items: flex-end;  /* 靠右對齊 */
  text-align: right;
  max-width: 55%;
}

.hero-eyebrow {
  display: flex; align-items: center; gap: 0.6rem;
  animation: hFadeUp 0.5s ease 0.1s both;
}
.eyebrow-line { display: block; width: 24px; height: 1px; background: #ff2d6b; }
.eyebrow-word {
  font-family: 'Space Mono', monospace;
  font-size: 0.7rem; letter-spacing: 0.3em; color: #ff2d6b;
}

/* 主標題：比原本 welcome-section 稍大，白字在深色圖片上凸顯 */
.hero-name {
  font-family: 'Bebas Neue', sans-serif;
  font-size: clamp(1rem, 5.7vw, 3rem);
  line-height: 0.92; letter-spacing: 0.14em;
  color: #fff; margin: 0;
  text-shadow: 0 2px 24px rgba(0,0,0,0.5);
}
.name-ch {
  display: inline-block;
  opacity: 0; transform: translateY(36px);
  animation: charUp 0.48s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}
@keyframes charUp { to { opacity: 1; transform: translateY(0); } }

.hero-clock {
  font-family: 'Space Mono', monospace;
  font-size: 0.6rem; letter-spacing: 0.16em;
  color: rgba(255,255,255,0.42); margin: 0;
  animation: hFadeUp 0.5s ease 0.5s both;
}
@keyframes hFadeUp {
  from { opacity: 0; transform: translateY(12px); }
  to   { opacity: 1; transform: translateY(0); }
}

.hero-cta {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  padding: 0.6rem 1.2rem 0.6rem 0.9rem;
  border: 1px solid rgba(255, 255, 255, 0.3);
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(6px);
  text-decoration: none;
  color: #fff;
  font-family: 'Space Mono', monospace;
  font-size: 0.9rem;
  letter-spacing: 0.12em;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  animation: hFadeUp 0.5s ease 0.75s both;
  transition: background 0.25s, border-color 0.25s;
  border-radius: 9px;
}
.hero-cta:hover {
  background: rgba(255, 45, 107, 0.2);
  border-color: #ff2d6b;
}
/* 左側跳動小點 */
.cta-pulse {
  width: 6px; height: 6px;
  border-radius: 50%;
  background: #ff2d6b;
  flex-shrink: 0;
  animation: pulse 1.6s ease-in-out infinite;
}
@keyframes pulse {
  0%, 100% { transform: scale(1); opacity: 1; }
  50%       { transform: scale(1.6); opacity: 0.5; }
}
.cta-text { flex: 1; }
.cta-arrow {
  font-size: 0.75rem;
  transition: transform 0.25s;
}
.hero-cta:hover .cta-arrow { transform: translateY(3px); }
/* ACTIVITIES 大標題：絕對定位在 hero 底部，跨越圖片與內容區的邊界 */
.hero-activities-title {
  position: absolute;
  bottom: -2.4rem;   /* 往下溢出更多，讓字母露出約一半 */
  left: 0; right: 0;
  z-index: 4;
  text-align: center;
  margin-bottom: 3rem;
}

/* quick-actions 移除原本的 section-title（已移到 hero 底部） */
.quick-actions {
  padding-top: 1rem;  /* 給 ACTIVITIES 標題留出重疊空間 */
  position: relative;
  z-index: 1;
}

/* ══════════════════════════════════
   以下完全複製原始 CSS，一字不改
   ══════════════════════════════════ */

.dashboard {
  min-height: 100vh;
  background: #fafafa;
}

/* ACTIVITIES 標題 - 減少行高讓重疊更緊湊 */
.section-title {
  font-family: 'Impact', sans-serif;
  font-size: 4rem;
  color: #1a1a1a;
  text-align: center;
  margin: 0;
  line-height: 0.8;
  position: relative;
  z-index: 2;
  text-transform: uppercase;
}

/* 英雄區塊容器 */
.hero-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.5rem;
  max-width: 1000px;
  margin: 1rem auto 2.5rem;
  padding: 0 1rem;
  position: relative;
  z-index: 1;
}

.hero-card {
  min-height: 240px;
  position: relative;
  background: #1a1a1a !important;
  border: none !important;
  text-decoration: none !important;
  overflow: hidden;
  cursor: pointer !important;
  border-radius: 0 !important;
}

.card-with-img {
  padding: 0 !important;
}

.card-image-wrapper {
  position: absolute;
  top: 0; left: 0;
  width: 100%; height: 100%;
  z-index: 1;
  transition: filter 0.3s ease, transform 0.3s ease;
}

.card-img {
  width: 100%; height: 100%;
  object-fit: cover;
  filter: grayscale(100%);
}

.card-content-overlay {
  position: absolute;
  top: 0; left: 0;
  width: 100%; height: 100%;
  z-index: 2;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
  padding: 2rem;
  box-sizing: border-box;
  opacity: 0;
  transition: opacity 0.3s ease, transform 0.3s ease;
  transform: translateY(10px);
}

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

.card-with-img:hover .card-image-wrapper {
  filter: brightness(0.3) grayscale(100%);
  transform: scale(1.05);
}

.card-with-img:hover .card-content-overlay {
  opacity: 1;
  transform: translateY(0);
}

.card-text-only .card-content {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: flex-start;
  text-align: left;
  position: relative;
  z-index: 5;
}

.card-text-only {
  background: #1a1a1a !important;
  display: block;
  position: relative;
  border-radius: 0 !important;
}

.card-text-only .card-content h3 {
  color: #ffffff !important;
  font-size: 1.8rem !important;
  font-weight: 700 !important;
  margin: 0 0 0.5rem 0 !important;
  display: flex;
  flex-direction: column;
}

.en-title {
  font-family: 'Inter', sans-serif;
  font-size: 0.9rem;
  text-transform: uppercase;
  letter-spacing: 0.1rem;
  color: #888888;
  margin-top: 0.2rem;
  font-weight: 400;
}

.card-text-only .card-content p {
  color: #ffffff !important;
  font-size: 1.1rem !important;
  margin-top: 1rem !important;
  display: flex;
  flex-direction: column;
}

.en-subtitle {
  font-size: 0.8rem;
  color: #888888;
  margin-top: 0.2rem;
  font-weight: 300;
  letter-spacing: 0.05rem;
}

.info-divider {
  max-width: 1000px;
  margin: 3rem auto 1.5rem;
  padding: 0 1rem;
  border-bottom: 1px solid #1a1a1a;
  display: flex;
  align-items: flex-end;
  padding-bottom: 8px;
}

.info-text {
  font-family: 'Copperplate', 'Copperplate Gothic Light', serif;
  font-size: 1.2rem;
  font-weight: bold;
  color: #1a1a1a;
  letter-spacing: 0.5rem;
  text-transform: uppercase;
  display: inline-block;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1.5rem;
  max-width: 1000px;
  margin: 0 auto 4rem;
  padding: 0 1rem;
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
  background: #1a1a1a;
  color: #ffffff;
  padding: 4rem 0 2rem;
  margin-top: 5rem;
  border-top: 4px solid #ff2d6b;
}

.footer-container {
  max-width: 1000px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr;
  gap: 3rem;
  padding: 0 1rem;
}

.footer-section h4.footer-label {
  font-family: 'Copperplate', serif;
  font-size: 1rem;
  letter-spacing: 0.2rem;
  text-transform: uppercase;
  color: #ff2d6b;
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

.social-links { display: flex; gap: 1rem; }

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

.info-gallery {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 0.5rem;
  max-width: 1000px;
  margin: 0 auto 5rem;
  padding: 1.5rem 1rem;
}

.info-item {
  display: flex;
  flex-direction: column;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.info-item:hover { transform: translateY(-5px); }

.info-img-box {
  width: 100%;
  aspect-ratio: 1/1;
  overflow: hidden;
  background: #eee;
  border-radius: 0;
}

.info-img-box img {
  width: 100%; height: 100%;
  object-fit: cover;
  filter: grayscale(100%);
  transition: filter 0.3s ease;
}

.info-item:hover .info-img-box img { filter: grayscale(0%); }

.info-caption {
  margin-top: 0.8rem;
  font-size: 0.95rem;
  color: #1a1a1a;
  font-weight: 500;
  text-align: left;
  letter-spacing: 0.05rem;
  border-left: 2px solid #000;
  padding-left: 0.8rem;
}

/* Responsive */
@media (max-width: 768px) {
  .nav-right { gap: 1rem; }
  .user-name { display: none; }
  .action-grid { grid-template-columns: 1fr; }
  .footer-container { grid-template-columns: 1fr; gap: 2rem; text-align: center; }
  .social-links { justify-content: center; }
  .section-title { font-size: 2.5rem; }
  .hero-actions { grid-template-columns: 1fr; margin-top: -0.1rem; }
  .hero-card { min-height: 180px; }
  .card-with-img .card-image-wrapper,
  .card-with-img .card-content { width: 100%; flex: none; }
  .card-with-img .card-image-wrapper { height: 100px; }
  .card-content { padding: 1rem; align-items: center; text-align: center; }
  .info-gallery { grid-template-columns: repeat(2, 1fr); gap: 0.5rem; }
  /* Hero responsive */
  .hero-section { height: 70vh; }
  .hero-name { font-size: 2.2rem; }
}
</style>