<template>
  <div class="landing">
    <!-- 未登入才顯示此 navbar -->
    <nav class="navbar" v-if="!userStore.isLoggedIn">
      <div class="nav-container">
        <h2 class="logo">社團管理系統</h2>
        <div class="nav-right">
          <router-link to="/login" class="nav-link">會員登入</router-link>
          <router-link to="/register" class="btn-logout">立即註冊</router-link>
        </div>
      </div>
    </nav>

    <main class="main-content">
      <div class="container">
        <div class="hero-section" @mousemove="onMouseMove" @mouseleave="onMouseLeave">
          <div class="hero-bg hero-bw"></div>
          <div
            class="hero-bg hero-color"
            :style="{
              maskImage: `radial-gradient(circle ${reveal}px at ${mx}px ${my}px, black 0%, transparent 100%)`,
              WebkitMaskImage: `radial-gradient(circle ${reveal}px at ${mx}px ${my}px, black 0%, transparent 100%)`
            }"
          ></div>
          <div class="hero-grad-top"></div>
          <div class="hero-grad-bot"></div>

          <div class="hero-text">
            <div class="hero-eyebrow">
              <span class="eyebrow-line"></span>
              <span class="eyebrow-word">CLUB MANAGEMENT SYSTEM</span>
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
            <p class="hero-clock">SIMPLIFY YOUR CLUB OPERATIONS</p>

            <div class="landing-actions">
              <router-link to="/register" class="hero-cta">
                <span class="cta-pulse"></span>
                <span class="cta-text">開始使用</span>
                <span class="cta-arrow">→</span>
              </router-link>
            </div>
          </div>

          <div class="hero-activities-title">
            <h2 class="section-title">FEATURES</h2>
          </div>
        </div>
        <div class="quick-actions">


          <div class="info-divider">
            <span class="info-text">Overview</span>
          </div>

          <div class="action-grid">
            <div class="action-card">
              <h3>線上報名</h3>
              <p>一鍵完成報名，並隨時查看審核狀態。</p>
            </div>
            <div class="action-card">
              <h3>請假管理</h3>
              <p>彈性的請假流程，幹部可快速進行審核。</p>
            </div>
            <div class="action-card">
              <h3>資訊看板</h3>
              <p>重要公告不漏接，社團動態一手掌握。</p>
            </div>
          </div>

          <div class="info-divider">
            <span class="info-text">Gallery</span>
          </div>

          <div class="info-gallery">
            <div class="info-item">
              <div class="info-img-box"><img src="@/assets/d2.jpg" alt="Gallery 1"></div>
              <p class="info-caption">優雅的介面設計</p>
            </div>
            <div class="info-item">
              <div class="info-img-box"><img src="@/assets/d3.jpg" alt="Gallery 2"></div>
              <p class="info-caption">跨平台操作體驗</p>
            </div>
            <div class="info-item">
              <div class="info-img-box"><img src="@/assets/d4.jpg" alt="Gallery 3"></div>
              <p class="info-caption">專業的技術支援</p>
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
        </div>
        <div class="footer-section hours">
          <h4 class="footer-label">Service Hours</h4>
          <p>週一至週五：09:00 - 18:00</p>
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

import { useUserStore } from '@/stores/user'
import { ref, computed } from 'vue'

// Hero 滑鼠效果
const mx = ref(0)
const my = ref(0)
const reveal = ref(0)
const userStore = useUserStore()
const onMouseMove = (e) => {
  const r = e.currentTarget.getBoundingClientRect()
  mx.value = e.clientX - r.left
  my.value = e.clientY - r.top
  reveal.value = 260
}
const onMouseLeave = () => { reveal.value = 0 }

// 標題動畫文字
const nameChars = computed(() => "輕鬆管理你的社團".split(''))

</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Bebas+Neue&family=Space+Mono:wght@400;700&family=Noto+Sans+TC:wght@300;400;700&display=swap');

/* ── 未登入時的頂部 Navbar ── */
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
.nav-link { color: rgba(255,255,255,0.75); text-decoration: none; font-weight: 500; transition: color 0.2s; }
.nav-link:hover { color: #fff; }
.btn-logout {
  padding: 0.5rem 1.25rem; background: rgba(255,255,255,0.1);
  color: #fff; border: 1px solid rgba(255,255,255,0.35);
  border-radius: 6px; font-weight: 500; text-decoration: none; transition: all 0.2s;
}
.btn-logout:hover { background: rgba(255,255,255,0.22); }

/* ── Layout ── */
.main-content { padding: 0; }
.container { max-width: 1200px; margin: 0 auto; padding: 0 2rem; }

/* ── Hero ── */
.hero-section {
  position: relative;
  margin-left: calc(-50vw + 50%);
  margin-right: calc(-50vw + 50%);
  width: 100vw;
  height: 78vh;
  min-height: 480px;
  overflow: visible;
  cursor: crosshair;
}

.hero-bg {
  position: absolute; inset: 0;
  background-image: url('https://scontent.ftpe3-1.fna.fbcdn.net/v/t39.30808-6/486824286_1066644962160551_318350841127563953_n.jpg?_nc_cat=104&ccb=1-7&_nc_sid=dd6889&_nc_ohc=7R9doMjhnm0Q7kNvwEtyY-a&_nc_oc=AdnDRQ_LhDz3lmcUncrkZ5EcWJAFdzVPqUc_ZYCM2IRYn_0x9HdbhKCHGZCtTaujepY&_nc_zt=23&_nc_ht=scontent.ftpe3-1.fna&_nc_gid=RTDgD4aZgC_v39vGFdK-mA&_nc_ss=8&oh=00_AfzUvfnvjx6m3TSqSK8i779rqgCm2jx-14TndyHM8RG88A&oe=69B9EC95');
  background-size: cover;
  background-position: center 20%;
  transition: transform 7s ease;
  overflow: hidden;
}
.hero-section:hover .hero-bg { transform: scale(1.03); }
.hero-bw    { filter: grayscale(100%) brightness(0.48); }
.hero-color { filter: brightness(0.75) saturate(1.1); }

.hero-grad-top {
  position: absolute; top: 0; left: 0; right: 0; height: 160px;
  background: linear-gradient(to bottom, rgba(0,0,0,0.55), transparent);
  pointer-events: none; z-index: 2;
}
.hero-grad-bot {
  position: absolute; bottom: 0; left: 0; right: 0; height: 38%;
  background: linear-gradient(to bottom, transparent 0%, rgba(10,10,10,0.2) 30%, rgba(250,250,250,0.75) 70%, #fafafa 100%);
  pointer-events: none; z-index: 2;
}

.hero-text {
  position: absolute;
  bottom: 50%; right: 5vw;
  z-index: 3;
  display: flex; flex-direction: column; gap: 0.55rem;
  align-items: flex-end;
  text-align: right;
  max-width: 55%;
}

.hero-eyebrow {
  display: flex; align-items: center; gap: 0.6rem;
  animation: hFadeUp 0.5s ease 0.1s both;
}
.eyebrow-line { display: block; width: 24px; height: 1px; background: #ff2d6b; }
.eyebrow-word { font-family: 'Space Mono', monospace; font-size: 0.7rem; letter-spacing: 0.3em; color: #ff2d6b; }

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

.landing-actions { margin-top: 1rem; display: flex; gap: 1rem; }

.hero-cta {
  display: flex; align-items: center; gap: 0.6rem;
  padding: 0.6rem 1.2rem 0.6rem 0.9rem;
  border: 1px solid rgba(255,255,255,0.3);
  background: rgba(255,255,255,0.08);
  backdrop-filter: blur(6px);
  text-decoration: none; color: #fff;
  font-family: 'Space Mono', monospace;
  font-size: 0.9rem; letter-spacing: 0.12em;
  animation: hFadeUp 0.5s ease 0.75s both;
  transition: background 0.25s, border-color 0.25s;
  border-radius: 9px;
}
.hero-cta:hover { background: rgba(255,45,107,0.2); border-color: #ff2d6b; }
.cta-pulse {
  width: 6px; height: 6px; border-radius: 50%;
  background: #ff2d6b; flex-shrink: 0;
  animation: pulse 1.6s ease-in-out infinite;
}
@keyframes pulse {
  0%, 100% { transform: scale(1); opacity: 1; }
  50%       { transform: scale(1.6); opacity: 0.5; }
}
.cta-text { flex: 1; }
.cta-arrow { font-size: 0.75rem; transition: transform 0.25s; }
.hero-cta:hover .cta-arrow { transform: translateY(3px); }

.hero-activities-title {
  position: absolute;
  bottom: -2.4rem; left: 0; right: 0;
  z-index: 4; text-align: center; margin-bottom: 3rem;
}

/* ── 內容區 ── */
.quick-actions { padding-top: 1rem; position: relative; z-index: 1; }

.section-title {
  font-family: 'Impact', sans-serif;
  font-size: 4rem; color: #1a1a1a;
  text-align: center; margin: 0; line-height: 0.8;
  position: relative; z-index: 2; text-transform: uppercase;
}

.info-divider {
  max-width: 1000px; margin: 3rem auto 1.5rem; padding: 0 1rem 8px;
  border-bottom: 1px solid #1a1a1a;
  display: flex; align-items: flex-end;
}
.info-text {
  font-family: 'Copperplate', 'Copperplate Gothic Light', serif;
  font-size: 1.2rem; font-weight: bold; color: #1a1a1a;
  letter-spacing: 0.5rem; text-transform: uppercase;
}

.action-grid {
  display: grid; grid-template-columns: repeat(3, 1fr);
  gap: 1.5rem; max-width: 1000px;
  margin: 0 auto 4rem; padding: 0 1rem;
}
.action-card {
  background: white; padding: 2rem;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  transition: all 0.2s; border: 1px solid transparent;
}
.action-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.1); border-color: #e0e0e0; transform: translateY(-2px); }
.action-card h3 { font-size: 1.25rem; font-weight: 600; color: #1a1a1a; margin: 0 0 0.5rem 0; }
.action-card p { color: #666; margin: 0; line-height: 1.5; }

.info-gallery {
  display: grid; grid-template-columns: repeat(3, 1fr);
  gap: 0.5rem; max-width: 1000px;
  margin: 0 auto 5rem; padding: 1.5rem 1rem;
}
.info-item { display: flex; flex-direction: column; cursor: pointer; transition: transform 0.3s ease; }
.info-item:hover { transform: translateY(-5px); }
.info-img-box { width: 100%; aspect-ratio: 1/1; overflow: hidden; background: #eee; }
.info-img-box img { width: 100%; height: 100%; object-fit: cover; filter: grayscale(100%); transition: filter 0.3s ease; }
.info-item:hover .info-img-box img { filter: grayscale(0%); }
.info-caption {
  margin-top: 0.8rem; font-size: 0.95rem; color: #1a1a1a;
  font-weight: 500; letter-spacing: 0.05rem;
  border-left: 2px solid #000; padding-left: 0.8rem;
}

/* ── Footer ── */
.footer { background: #1a1a1a; color: #fff; padding: 4rem 0 2rem; margin-top: 5rem; border-top: 4px solid #ff2d6b; }
.footer-container {
  max-width: 1000px; margin: 0 auto;
  display: grid; grid-template-columns: 2fr 1fr 1fr 1fr;
  gap: 3rem; padding: 0 1rem;
}
.footer-section h4.footer-label {
  font-family: 'Copperplate', serif;
  font-size: 1rem; letter-spacing: 0.2rem; text-transform: uppercase;
  color: #ff2d6b; margin-bottom: 1.5rem;
}
.footer-logo { font-weight: 900; letter-spacing: 0.3rem; margin-bottom: 1rem; }
.footer-section p { color: #bbb; font-size: 0.9rem; line-height: 1.8; margin: 0.3rem 0; }
.social-links { display: flex; gap: 1rem; }
.social-links a { color: #fff; text-decoration: none; font-weight: bold; font-size: 0.8rem; border: 1px solid #444; padding: 0.5rem 0.8rem; transition: all 0.3s; }
.social-links a:hover { background: #fff; color: #1a1a1a; border-color: #fff; }
.footer-bottom { max-width: 1000px; margin: 4rem auto 0; padding: 2rem 1rem 0; border-top: 1px solid #333; text-align: center; }
.footer-bottom p { font-size: 0.75rem; letter-spacing: 0.1rem; color: #666; }

/* ── Responsive ── */
@media (max-width: 768px) {
  .nav-right { gap: 1rem; }
  .action-grid { grid-template-columns: 1fr; }
  .footer-container { grid-template-columns: 1fr; gap: 2rem; text-align: center; }
  .social-links { justify-content: center; }
  .section-title { font-size: 2.5rem; }
  .info-gallery { grid-template-columns: repeat(2, 1fr); }
  .hero-section { height: 70vh; }
  .hero-name { font-size: 2.2rem; }
}
</style>
