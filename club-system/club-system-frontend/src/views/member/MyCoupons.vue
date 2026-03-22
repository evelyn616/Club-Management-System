<script setup>
import { ref, computed, onMounted } from 'vue'
import { discountApi } from '@/api/discount'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const loading = ref(false)
const coupons = ref([])
const activeTab = ref('available') // 'available' | 'used'

const loadCoupons = async () => {
  loading.value = true
  try {
    const res = await discountApi.getAllCoupons(userStore.userId)
    coupons.value = res.data
  } catch (e) {
    console.error('載入優惠券失敗', e)
  } finally {
    loading.value = false
  }
}

const availableCoupons = computed(() => coupons.value.filter(c => !c.isUsed))
const usedCoupons = computed(() => coupons.value.filter(c => c.isUsed))
const displayedCoupons = computed(() => activeTab.value === 'available' ? availableCoupons.value : usedCoupons.value)

const couponLeftClass = (t) => {
  if (t === 'WELCOME') return 'left-welcome'
  if (t === 'CUSTOM')  return 'left-custom'
  return ''  // default green = COUPON
}

const couponTypeName = (t) => {
  if (t === 'COUPON') return '忠誠優惠券'
  if (t === 'WELCOME') return '新人優惠券'
  if (t === 'CUSTOM')  return '特別優惠券'
  return '優惠券'
}

const formatDate = (dt) => {
  if (!dt) return '—'
  return new Date(dt).toLocaleString('zh-TW', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit'
  })
}

onMounted(loadCoupons)
</script>

<template>
  <div class="my-coupons">

    <div class="page-header">
      <h1 class="page-title">我的優惠券</h1>
      <p class="page-desc">累積出席活動自動獲得，報名時可選擇使用</p>
    </div>

    <!-- 統計 -->
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-num">{{ availableCoupons.length }}</div>
        <div class="stat-label">可使用</div>
      </div>
      <div class="stat-card used">
        <div class="stat-num">{{ usedCoupons.length }}</div>
        <div class="stat-label">已使用</div>
      </div>
      <div class="stat-card total">
        <div class="stat-num">{{ coupons.length }}</div>
        <div class="stat-label">累積獲得</div>
      </div>
    </div>

    <!-- Tab -->
    <div class="tab-bar">
      <button
        :class="['tab-btn', { active: activeTab === 'available' }]"
        @click="activeTab = 'available'"
      >
        可使用 <span class="tab-count">{{ availableCoupons.length }}</span>
      </button>
      <button
        :class="['tab-btn', { active: activeTab === 'used' }]"
        @click="activeTab = 'used'"
      >
        已使用 <span class="tab-count">{{ usedCoupons.length }}</span>
      </button>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="empty-state">載入中...</div>

    <!-- 空狀態 -->
    <div v-else-if="displayedCoupons.length === 0" class="empty-state">
      <div class="empty-icon">🎟</div>
      <p v-if="activeTab === 'available'">目前沒有可使用的優惠券<br><span class="empty-hint">累積出席活動即可獲得！</span></p>
      <p v-else>目前沒有已使用的優惠券</p>
    </div>

    <!-- 優惠券列表 -->
    <div v-else class="coupon-list">
      <div
        v-for="coupon in displayedCoupons"
        :key="coupon.id"
        class="coupon-card"
        :class="{ used: coupon.isUsed }"
      >
        <!-- 左側票根 -->
        <div class="coupon-left" :class="couponLeftClass(coupon.couponType)">
          <div class="coupon-icon">{{ coupon.couponType === 'WELCOME' ? '🎁' : coupon.couponType === 'CUSTOM' ? '⭐' : '🎟' }}</div>
          <div class="coupon-id">#{{ coupon.id }}</div>
        </div>

        <!-- 分隔線 -->
        <div class="coupon-divider">
          <span class="notch top"></span>
          <span class="notch bottom"></span>
        </div>

        <!-- 右側資訊 -->
        <div class="coupon-right">
          <div class="coupon-name">{{ couponTypeName(coupon.couponType) }}</div>
          <div class="coupon-discount">
            {{ coupon.discountRate ? `打 ${Math.round(coupon.discountRate * 10)} 折（省 ${Math.round((1 - coupon.discountRate) * 100)}%）` : '折扣率依當前設定' }}
          </div>
          <div class="coupon-meta">
            <span class="meta-item">獲得：{{ formatDate(coupon.earnedAt) }}</span>
          </div>
          <div v-if="coupon.isUsed" class="coupon-used-info">
            <span class="meta-item used-tag">已使用</span>
            <span class="meta-item">{{ formatDate(coupon.usedAt) }}</span>
          </div>
          <div v-else class="coupon-status available-tag">可使用</div>
        </div>
      </div>
    </div>

    <!-- 說明 -->
    <div class="info-box">
      <h3 class="info-title">如何獲得優惠券？</h3>
      <p class="info-text">每累積出席一定場次的活動（以簽到確認），系統會自動發放忠誠優惠券。<br>報名付費活動時，在確認頁面選擇「忠誠優惠券」即可使用。</p>
    </div>

  </div>
</template>

<style scoped>
.my-coupons {
  max-width: 680px;
  margin: 0 auto;
  padding: 2rem 1.5rem 5rem;
}

.page-header { margin-bottom: 2rem; }
.page-title { font-size: 1.75rem; font-weight: 700; color: #1a1a1a; margin: 0 0 0.35rem; }
.page-desc { font-size: 0.85rem; color: #888; margin: 0; }

/* Stats */
.stats-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
  margin-bottom: 2rem;
}
.stat-card {
  background: #fff;
  border-radius: 10px;
  padding: 1.25rem;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}
.stat-num {
  font-size: 2rem;
  font-weight: 700;
  color: #16a34a;
  line-height: 1;
  margin-bottom: 0.3rem;
}
.stat-card.used .stat-num { color: #9ca3af; }
.stat-card.total .stat-num { color: #1a1a1a; }
.stat-label { font-size: 0.78rem; color: #888; }

/* Tab */
.tab-bar {
  display: flex;
  border-bottom: 2px solid #f0f0f0;
  margin-bottom: 1.5rem;
}
.tab-btn {
  background: transparent;
  border: none;
  padding: 0.65rem 1.25rem;
  font-size: 0.88rem;
  color: #888;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  margin-bottom: -2px;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 0.4rem;
}
.tab-btn:hover { color: #1a1a1a; }
.tab-btn.active { color: #1a1a1a; font-weight: 600; border-bottom-color: #1a1a1a; }
.tab-count {
  background: #f0f0f0;
  border-radius: 10px;
  padding: 0.05rem 0.45rem;
  font-size: 0.72rem;
  color: #666;
}
.tab-btn.active .tab-count { background: #1a1a1a; color: #fff; }

/* Empty */
.empty-state {
  text-align: center;
  padding: 3rem 0;
  color: #aaa;
  font-size: 0.88rem;
  line-height: 1.8;
}
.empty-icon { font-size: 2.5rem; margin-bottom: 0.75rem; }
.empty-hint { font-size: 0.78rem; color: #bbb; }

/* Coupon Card */
.coupon-list { display: flex; flex-direction: column; gap: 1rem; margin-bottom: 2rem; }

.coupon-card {
  display: flex;
  align-items: stretch;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.07);
  overflow: hidden;
  border: 1px solid #f0f0f0;
  transition: transform 0.15s;
}
.coupon-card:hover { transform: translateY(-2px); }
.coupon-card.used { opacity: 0.6; }

.coupon-left {
  background: #16a34a;
  color: #fff;
  padding: 1.25rem 1rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.35rem;
  min-width: 72px;
}
.coupon-card.used .coupon-left { background: #9ca3af; }
.coupon-left.left-welcome { background: #2563eb; }
.coupon-left.left-custom  { background: #9333ea; }
.coupon-icon { font-size: 1.5rem; }
.coupon-id { font-size: 0.65rem; opacity: 0.8; font-weight: 600; }

.coupon-divider {
  width: 1px;
  background: repeating-linear-gradient(to bottom, #e0e0e0 0, #e0e0e0 6px, transparent 6px, transparent 12px);
  position: relative;
}
.notch {
  position: absolute;
  left: -8px;
  width: 16px; height: 16px;
  border-radius: 50%;
  background: #fafafa;
}
.notch.top { top: -8px; }
.notch.bottom { bottom: -8px; }

.coupon-right {
  flex: 1;
  padding: 1.1rem 1.25rem;
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
}
.coupon-name { font-size: 1rem; font-weight: 700; color: #1a1a1a; }
.coupon-discount { font-size: 0.8rem; color: #16a34a; font-weight: 600; }
.coupon-card.used .coupon-discount { color: #9ca3af; }
.coupon-meta { display: flex; flex-wrap: wrap; gap: 0.75rem; margin-top: 0.25rem; }
.meta-item { font-size: 0.72rem; color: #aaa; }
.coupon-used-info { display: flex; align-items: center; gap: 0.5rem; flex-wrap: wrap; }
.used-tag {
  background: #f3f4f6;
  color: #6b7280;
  padding: 0.1rem 0.5rem;
  border-radius: 4px;
  font-size: 0.7rem;
  font-weight: 600;
}
.coupon-status { margin-top: 0.1rem; }
.available-tag {
  display: inline-block;
  background: #dcfce7;
  color: #16a34a;
  padding: 0.1rem 0.6rem;
  border-radius: 4px;
  font-size: 0.72rem;
  font-weight: 600;
}

/* Info Box */
.info-box {
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  border-radius: 10px;
  padding: 1.25rem 1.5rem;
}
.info-title { font-size: 0.88rem; font-weight: 700; color: #15803d; margin: 0 0 0.5rem; }
.info-text { font-size: 0.8rem; color: #166534; line-height: 1.7; margin: 0; }
</style>
