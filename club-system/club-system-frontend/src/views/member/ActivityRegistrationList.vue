<template>
  <div class="activity-list">


    <!-- Search & Filter -->
    <div class="search-filter-bar">
      <div class="search-wrap">
        <input v-model="searchKeyword" type="text" placeholder="搜尋活動..." class="search-input" />
        <span class="search-icon">→</span>
      </div>
      <div class="filter-group">
        <select v-model="selectedActivityType" class="filter-select" @change="handleFilterChange">
          <option value="">TYPE: ALL</option>
          <option value="REGULAR">社課</option>
          <option value="SPECIAL">特殊活動</option>
          <option value="TRAINING">團練</option>
          <option value="PERFORMANCE">演出</option>
          <option value="COMPETITION">比賽</option>
        </select>
        <select v-model="selectedDanceStyle" class="filter-select" @change="handleFilterChange">
          <option value="">STYLE: ALL</option>
          <option v-for="style in danceStyles" :key="style" :value="style">{{ style }}</option>
        </select>
        <button v-if="hasActiveFilters()" @click="clearFilters" class="clear-btn">CLEAR ×</button>
      </div>
      <div class="tab-group">
        <button :class="['tab-btn', { active: activeTab === 'all' }]" @click="activeTab = 'all'">ALL</button>
        <button :class="['tab-btn', { active: activeTab === 'upcoming' }]" @click="activeTab = 'upcoming'">UPCOMING</button>
        <button :class="['tab-btn', { active: activeTab === 'news' }]" @click="activeTab = 'news'">NEW</button>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="state-container">
      <div class="loading-wrapper">
        <div class="loading-bars">
          <span v-for="i in 5" :key="i" :style="{ animationDelay: (i * 0.1) + 's' }"></span>
        </div>
        <p class="loading-text">LOADING...</p>
      </div>
    </div>

    <!-- Empty -->
    <div v-else-if="filteredActivities.length === 0" class="state-container">
      <div class="empty-card">
        <div class="empty-big-text">EMPTY</div>
        <p class="empty-desc">找不到符合條件的活動</p>
        <button v-if="hasActiveFilters()" @click="clearFilters" class="cta-btn">清除篩選 ×</button>
      </div>
    </div>

    <!-- Cards -->
    <div v-else>
      <div class="cards-container">
        <div
          v-for="(activity, index) in filteredActivities"
          :key="activity.id"
          class="activity-card"
          :class="{ 'is-registered': isRegistered(activity.id), 'is-full': isActivityFull(activity) && !isRegistered(activity.id) }"
          :style="{ animationDelay: (index * 0.06) + 's' }"
          @click="openRegisterModal(activity)"
        >
          <!-- Cover Image -->
          <div class="card-cover">
            <div v-if="activity.coverImageUrl" class="cover-img-wrap">
              <div class="cover-shimmer" :class="{ hidden: loadedImgs.has(activity.id) }"></div>
              <img
                :src="activity.coverImageUrl"
                :alt="activity.title"
                class="cover-img"
                :class="{ 'img-loaded': loadedImgs.has(activity.id) }"
                loading="lazy"
                decoding="async"
                @load="onImgLoad(activity.id)"
              />
            </div>
            <div v-else class="cover-placeholder">
              <span class="placeholder-type">{{ getActivityTypeLabel(activity.activityType) }}</span>
            </div>
            <!-- Overlay badges -->
            <div class="cover-badges">
              <span class="type-badge">{{ getActivityTypeLabel(activity.activityType) }}</span>
              <span v-if="isActivityFull(activity) && !isRegistered(activity.id)" class="full-badge">FULL</span>
              <span v-else-if="isRegistered(activity.id)" class="registered-badge">✓ 已報名</span>
            </div>
            <div class="capacity-bar" v-if="activity.maxParticipants">
              <div
                class="capacity-fill"
                :style="{ width: Math.min((activity.registrationCount || 0) / activity.maxParticipants * 100, 100) + '%' }"
                :class="{ 'fill-full': isActivityFull(activity) }"
              ></div>
            </div>
          </div>

          <!-- Card Info -->
          <div class="card-info">
            <div class="card-date">{{ formatEventDate(activity.startTime) }}</div>
            <h3 class="activity-title">{{ activity.title }}</h3>
            <div class="card-meta">
              <span v-if="activity.location" class="meta-item">{{ activity.location }}</span>
              <span v-if="activity.feeAmount > 0" class="meta-fee">NT$ {{ activity.feeAmount }}</span>
              <span v-else class="meta-free">免費</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal -->
    <div v-if="showRegisterModal" class="modal-overlay" @click="closeRegisterModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <div class="modal-label">CONFIRM REGISTRATION</div>
          <button class="modal-close" @click="closeRegisterModal">×</button>
        </div>
        <div class="modal-body">
          <h2 class="modal-title">{{ selectedActivity?.title }}</h2>
          <div class="modal-info-grid">
            <div class="modal-info-item">
              <span class="info-label">DATE</span>
              <span class="info-value">{{ formatDateTime(selectedActivity?.startTime) }}</span>
            </div>
            <div class="modal-info-item">
              <span class="info-label">LOCATION</span>
              <span class="info-value">{{ selectedActivity?.location }}</span>
            </div>
            <div class="modal-info-item">
              <span class="info-label">REGISTRANT</span>
              <span class="info-value">{{ userStore.userName }}</span>
            </div>
          </div>

          <!-- 折扣區塊 -->
          <div v-if="selectedActivity?.feeAmount > 0" class="discount-section">
            <div class="discount-section-title">FEE & DISCOUNT</div>

            <!-- 載入中 -->
            <div v-if="discountLoading" class="discount-loading">載入折扣資訊...</div>

            <!-- 幹部免費 -->
            <div v-else-if="discountPreview?.officerFree" class="discount-badge officer">
              幹部免費 ✓
            </div>

            <!-- 可選折扣 -->
            <div v-else-if="discountPreview" class="discount-options">
              <!-- 無折扣選項 -->
              <label class="discount-option" :class="{ selected: selectedDiscount === 'NONE' }">
                <input type="radio" v-model="selectedDiscount" value="NONE" />
                <span class="opt-label">原價</span>
                <span class="opt-amount">NT$ {{ discountPreview.originalAmount }}</span>
              </label>

              <!-- 早鳥優惠 -->
              <label
                v-if="discountPreview.earlyBirdActive"
                class="discount-option"
                :class="{ selected: selectedDiscount === 'EARLY_BIRD' }"
              >
                <input type="radio" v-model="selectedDiscount" value="EARLY_BIRD" />
                <span class="opt-label">
                  🐣 早鳥優惠
                  <small>截止 {{ formatDateTime(discountPreview.earlyBirdDeadline) }}</small>
                </span>
                <span class="opt-amount discount">NT$ {{ discountPreview.earlyBirdAmount }}</span>
              </label>

              <!-- 優惠券 -->
              <template v-if="discountPreview.availableCoupons?.length > 0">
                <label
                  class="discount-option"
                  :class="{ selected: selectedDiscount === 'COUPON' }"
                >
                  <input type="radio" v-model="selectedDiscount" value="COUPON" />
                  <span class="opt-label">
                    🎟 優惠券
                    <small>可用 {{ discountPreview.availableCoupons.length }} 張</small>
                  </span>
                  <span class="opt-amount discount">NT$ {{ calcFinalAmount() }}</span>
                </label>
                <!-- 選了 COUPON 才顯示券選擇 -->
                <select
                  v-if="selectedDiscount === 'COUPON'"
                  v-model="selectedCouponId"
                  class="coupon-select"
                >
                  <option
                    v-for="c in discountPreview.availableCoupons"
                    :key="c.id"
                    :value="c.id"
                  >
                    {{ c.description || (c.couponType === 'WELCOME' ? '新人優惠券' : c.couponType === 'COUPON' ? '忠誠優惠券' : '優惠券') }} — {{ c.discountAmount ? 'NT$ ' + c.discountAmount + ' 折抵' : c.discountRate ? (c.discountRate * 10).toFixed(1).replace(/\.0$/, '') + '折' : '' }}
                  </option>
                </select>
              </template>

              <!-- 優惠碼輸入 -->
              <div class="promo-row">
                <div class="promo-input-wrap">
                  <input
                    v-model="promoCodeInput"
                    type="text"
                    placeholder="輸入優惠碼"
                    class="promo-input"
                    @keyup.enter="validatePromoCode"
                    @input="promoCodeResult = null; promoCodeError = ''; if (selectedDiscount === 'PROMO_CODE') selectedDiscount = 'NONE'"
                  />
                  <button
                    class="promo-btn"
                    @click="validatePromoCode"
                    :disabled="promoValidating || !promoCodeInput.trim()"
                  >{{ promoValidating ? '...' : '套用' }}</button>
                </div>
                <p v-if="promoCodeError" class="promo-error">{{ promoCodeError }}</p>

                <!-- 驗證成功 → 顯示為可選選項 -->
                <label
                  v-if="promoCodeResult?.valid"
                  class="discount-option promo-option"
                  :class="{ selected: selectedDiscount === 'PROMO_CODE' }"
                >
                  <input type="radio" v-model="selectedDiscount" value="PROMO_CODE" />
                  <span class="opt-label">
                    🏷 優惠碼
                    <small>{{ promoCodeResult.description || promoCodeInput.toUpperCase() }}
                      · 省 NT$ {{ promoCodeResult.savedAmount }}</small>
                  </span>
                  <span class="opt-amount discount">NT$ {{ promoCodeResult.finalAmount }}</span>
                </label>
              </div>
            </div>

            <!-- 最終金額 -->
            <div v-if="!discountPreview?.officerFree && discountPreview" class="final-amount">
              <span>實付金額</span>
              <span class="final-price">
                NT$ {{ calcFinalAmount() }}
              </span>
            </div>
          </div>

          <!-- 免費活動 -->
          <div v-else class="discount-section">
            <div class="free-badge">免費活動 🎉</div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="modal-cancel-btn" @click="closeRegisterModal">取消</button>
          <button class="modal-confirm-btn" @click="confirmRegister" :disabled="isRegistering">
            {{ isRegistering ? '報名中...' : '確定報名 →' }}
          </button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { activityApi } from '@/api/activity'
import { useUserStore } from '@/stores/user'
import { registrationApi } from '@/api/registration'
import { discountApi } from '@/api/discount'
import { promoCodeApi } from '@/api/promoCode'

const router = useRouter()
const userStore = useUserStore()

const activities = ref([])
const loadedImgs = ref(new Set())
const loading = ref(false)
const searchKeyword = ref('')
const selectedActivityType = ref('')
const selectedDanceStyle = ref('')
const activeTab = ref('all')
const tableKey = ref(0)
const isFirstLoad = ref(true)
const showRegisterModal = ref(false)
const selectedActivity = ref(null)
const isRegistering = ref(false)
const registrations = ref([])

// 折扣相關
const discountPreview = ref(null)
const discountLoading = ref(false)
const selectedDiscount = ref('NONE')   // 'NONE' | 'EARLY_BIRD' | 'COUPON' | 'PROMO_CODE'
const selectedCouponId = ref(null)

// 優惠碼
const promoCodeInput = ref('')
const promoCodeResult = ref(null)
const promoValidating = ref(false)
const promoCodeError = ref('')

const validatePromoCode = async () => {
  if (!promoCodeInput.value.trim() || !selectedActivity.value) return
  promoValidating.value = true
  promoCodeError.value = ''
  promoCodeResult.value = null
  try {
    const res = await promoCodeApi.validate(promoCodeInput.value.trim(), selectedActivity.value.id)
    if (res.data.valid) {
      promoCodeResult.value = res.data
      selectedDiscount.value = 'PROMO_CODE'
    } else {
      promoCodeError.value = res.data.errorMessage
    }
  } catch {
    promoCodeError.value = '驗證失敗，請稍後再試'
  } finally {
    promoValidating.value = false
  }
}

// Navbar scroll hide/show
const navHidden = ref(false)
let lastScrollY = 0
const handleScroll = () => {
  const currentY = window.scrollY
  navHidden.value = currentY > lastScrollY && currentY > 60
  lastScrollY = currentY
}
onUnmounted(() => window.removeEventListener('scroll', handleScroll))

const handleLogout = () => {
  userStore.logout()
  router.push('/')
}

const loadActivities = async () => {
  loading.value = true
  try {
    const response = await activityApi.getRegistrableActivities()
    activities.value = response.data
    for (let activity of activities.value) {
      try {
        const countResponse = await registrationApi.getActivityRegistrationCount(activity.id)
        activity.registrationCount = countResponse.data
      } catch {
        activity.registrationCount = 0
      }
    }
  } catch (error) {
    console.error('載入活動資料失敗:', error)
  } finally {
    loading.value = false
  }
}

const normalizeText = (text) => {
  if (!text) return ''
  return text.toLowerCase().replace(/[\s-_]+/g, '').trim()
}

const hasActiveFilters = () => searchKeyword.value || selectedActivityType.value || selectedDanceStyle.value

watch([activeTab, searchKeyword, selectedActivityType, selectedDanceStyle], () => {
  if (isFirstLoad.value) { isFirstLoad.value = false; return }
  tableKey.value++
})

const filteredActivities = computed(() => {
  let result = [...activities.value]
  const now = new Date()

  if (activeTab.value === 'upcoming') {
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
    const thirtyDaysLater = new Date(today)
    thirtyDaysLater.setDate(thirtyDaysLater.getDate() + 30)
    result = result.filter(a => {
      if (!a.startTime) return false
      const d = new Date(a.startTime)
      return d >= today && d <= thirtyDaysLater
    })
  } else if (activeTab.value === 'news') {
    result = result.filter(a => {
      if (!a.publishedAt) return false
      return (now - new Date(a.publishedAt)) / (1000 * 60 * 60 * 24) <= 7
    })
  }

  if (searchKeyword.value) {
    const keyword = normalizeText(searchKeyword.value)
    result = result.filter(a =>
      normalizeText(a.title).includes(keyword) ||
      (a.description && normalizeText(a.description).includes(keyword))
    )
  }
  if (selectedActivityType.value)
    result = result.filter(a => a.activityType === selectedActivityType.value)
  if (selectedDanceStyle.value) {
    const style = normalizeText(selectedDanceStyle.value)
    result = result.filter(a =>
      normalizeText(a.title).includes(style) ||
      (a.description && normalizeText(a.description).includes(style))
    )
  }
  return result
})

const openRegisterModal = async (activity) => {
  if (!userStore.token) return null
  if (isRegistered(activity.id)) return  // 已報名不開 modal
  if (isActivityFull(activity)) return   // 已額滿不開 modal
  selectedActivity.value = activity
  selectedDiscount.value = 'NONE'
  selectedCouponId.value = null
  discountPreview.value = null
  promoCodeInput.value = ''
  promoCodeResult.value = null
  promoCodeError.value = ''
  showRegisterModal.value = true

  // 載入折扣資訊
  if (activity.feeAmount > 0) {
    discountLoading.value = true
    try {
      const res = await discountApi.preview(activity.id, userStore.userId)
      discountPreview.value = res.data
      // 自動選最優折扣
      if (res.data.officerFree) {
        selectedDiscount.value = 'OFFICER'
      } else if (res.data.earlyBirdActive) {
        selectedDiscount.value = 'EARLY_BIRD'
      } else if (res.data.availableCoupons?.length > 0) {
        selectedDiscount.value = 'COUPON'
        selectedCouponId.value = res.data.availableCoupons[0].id
      }
    } catch (e) {
      console.error('折扣預覽載入失敗', e)
    } finally {
      discountLoading.value = false
    }
  }
}

const closeRegisterModal = () => {
  showRegisterModal.value = false
  selectedActivity.value = null
  discountPreview.value = null
}

const calcFinalAmount = () => {
  if (!discountPreview.value) return selectedActivity.value?.feeAmount ?? 0
  switch (selectedDiscount.value) {
    case 'EARLY_BIRD': return discountPreview.value.earlyBirdAmount
    case 'COUPON': {
      const coupon = discountPreview.value.availableCoupons?.find(c => c.id === selectedCouponId.value)
      if (!coupon) return discountPreview.value.loyaltyAmount
      const original = discountPreview.value.originalAmount
      if (coupon.discountAmount != null) return Math.max(0, original - coupon.discountAmount)
      const rate = coupon.discountRate ?? discountPreview.value.loyaltyRate
      return Math.round(original * rate * 100) / 100
    }
    case 'PROMO_CODE': return promoCodeResult.value?.finalAmount ?? discountPreview.value.originalAmount
    default:           return discountPreview.value.originalAmount
  }
}

const confirmRegister = async () => {
  if (!selectedActivity.value) return

  // COUPON 需要選了券
  if (selectedDiscount.value === 'COUPON' && !selectedCouponId.value) {
    alert('⚠️ 請選擇要使用的優惠券')
    return
  }
  // PROMO_CODE 需要有驗證結果
  if (selectedDiscount.value === 'PROMO_CODE' && !promoCodeResult.value?.valid) {
    alert('⚠️ 請輸入並套用有效的優惠碼')
    return
  }

  isRegistering.value = true
  try {
    const payload = {
      userId: userStore.userId,
      activityId: selectedActivity.value.id,
      requestedDiscount: selectedDiscount.value === 'OFFICER' || selectedDiscount.value === 'PROMO_CODE'
        ? 'NONE' : selectedDiscount.value,
      loyaltyCouponId: selectedDiscount.value === 'COUPON' ? selectedCouponId.value : null,
      promoCode: selectedDiscount.value === 'PROMO_CODE' ? promoCodeInput.value.trim() : null,
    }
    const response = await registrationApi.createRegistration(payload)
    const finalAmt = calcFinalAmount()
    const discountNote = selectedDiscount.value !== 'NONE' && selectedDiscount.value !== 'OFFICER'
      ? `\n折扣後金額：NT$ ${finalAmt}` : ''
    alert(`✅ 報名成功！\n活動：${selectedActivity.value.title}${discountNote}`)
    closeRegisterModal()
    router.push({name: 'pending-payments'})
  } catch (error) {
    if (error.response?.status === 409) alert('⚠️ 您已經報名過此活動。')
    else if (error.response?.status === 400) alert(`❌ ${error.response.data?.message || '報名資料有誤'}`)
    else if (error.response?.status === 404) alert('❌ 找不到此活動，可能已被取消。')
    else alert('❌ 報名失敗，請稍後再試。')
  } finally {
    isRegistering.value = false
  }
}

const loadRegistrations = async () => {
  try {
    const response = await registrationApi.getMyRegistrations(userStore.userId)
    registrations.value = response.data
  } catch (error) {
    console.error('載入報名紀錄失敗:', error)
  }
}

const onImgLoad = (id) => {
  loadedImgs.value = new Set([...loadedImgs.value, id])
}

const isActivityFull = (activity) => {
  if (!activity.maxParticipants) return false
  return (activity.registrationCount || 0) >= activity.maxParticipants
}
const isRegistered = (activityId) =>
  registrations.value.some(r => r.activityId === activityId && r.status !== 'CANCELLED')
const isCancelled = (activityId) =>
  registrations.value.some(r => r.activityId === activityId && r.status === 'CANCELLED')

const getActivityTypeLabel = (type) => {
  const map = { REGULAR: '社課', SPECIAL: '特殊活動', TRAINING: '團練', PERFORMANCE: '演出', COMPETITION: '比賽' }
  return map[type] || type
}

const danceStyles = ['Hip Hop','Jazz','Popping','Locking','Breaking','House','Waacking','Voguing','Urban','K-pop','Freestyle']

const formatDate = (dateTime) => {
  if (!dateTime) return '-'
  const d = new Date(dateTime)
  return d.toLocaleDateString('zh-TW', { month: '2-digit', day: '2-digit' })
    + ' ' + d.toLocaleTimeString('zh-TW', { hour: '2-digit', minute: '2-digit' })
}
const formatTimeOnly = (dateTime) => {
  if (!dateTime) return ''
  return new Date(dateTime).toLocaleTimeString('zh-TW', { hour: '2-digit', minute: '2-digit' })
}
const formatEventDate = (dateTime) => {
  if (!dateTime) return '-'
  const d = new Date(dateTime)
  const yyyy = d.getFullYear()
  const mm = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  const days = ['Sun.', 'Mon.', 'Tue.', 'Wed.', 'Thu.', 'Fri.', 'Sat.']
  const hh = String(d.getHours()).padStart(2, '0')
  const min = String(d.getMinutes()).padStart(2, '0')
  return `${yyyy}/${mm}/${dd} (${days[d.getDay()]}) ${hh}:${min}`
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-TW', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit'
  })
}

const handleSearch = () => {}
const handleFilterChange = () => {}
const clearFilters = () => {
  searchKeyword.value = ''
  selectedActivityType.value = ''
  selectedDanceStyle.value = ''
}

onMounted(() => {
  loadActivities()
  loadRegistrations()
  window.addEventListener('scroll', handleScroll, { passive: true })
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Bebas+Neue&family=Noto+Sans+TC:wght@300;400;500;700&family=Space+Mono:wght@400;700&display=swap');

.activity-list {
  min-height: 100vh; background: #ffffff;
  font-family: 'Noto Sans TC', sans-serif; color: #0a0a0a;
  max-width: 1400px; margin: 0 auto; padding: 5.5rem 2rem 5rem;
}

/* ===== Navbar ===== */

.navbar-hidden { transform: translateY(-100%); }

.nav-logo {
  font-family: 'Space Mono', monospace; font-size: 1.25rem;
  font-weight: 700; letter-spacing: 0.18em; color: #0a0a0a;
  text-decoration: none; transition: color 0.2s;
}
.nav-logo:hover { color: #ff2d6b; }

.nav-username { font-family: 'Space Mono', monospace; font-weight: 500; letter-spacing: 0.08em; color: #aaa; }
.nav-link {
  font-family: 'Space Mono', monospace; font-weight: 500; letter-spacing: 0.08em;
  color: #555; text-decoration: none; transition: color 0.2s;
}
.nav-link:hover { color: #0a0a0a; }
.nav-logout {
  background: transparent; border: 1px solid #e0e0e0; color: #555;
  padding: 0.5rem 1.25rem; font-family: 'Space Mono', monospace;
  letter-spacing: 0.08em; cursor: pointer; transition: all 0.2s;
  border-radius: 6px; font-weight: 500;
}
.nav-logout:hover { border-color: #ff2d6b; color: #ff2d6b; background: rgba(255,45,107,0.04); }

/* ===== Search & Filter ===== */
.search-filter-bar {
  display: flex; align-items: center; gap: 2rem;
  margin-bottom: 2.5rem; padding-bottom: 1.5rem;
  border-bottom: 1px solid #e0e0e0; flex-wrap: wrap;
}
.search-wrap { position: relative; width: 310px; flex-shrink: 0; }
.search-input {
  width: 100%; padding: 0.65rem 2rem 0.65rem 1rem;
  border: 1px solid #0a0a0a; border-radius: 0;
  font-family: 'Space Mono', monospace; font-size: 0.78rem;
  letter-spacing: 0.06em; background: #fff; color: #0a0a0a;
  outline: none; transition: border-color 0.2s; box-sizing: border-box;
}
.search-input::placeholder { color: #bbb; }
.search-input:focus { border-color: #ff2d6b; }
.search-icon {
  position: absolute; right: 0.75rem; top: 50%; transform: translateY(-50%);
  color: #bbb; font-size: 0.9rem; pointer-events: none;
}

.filter-group {
  display: flex; gap: 0.75rem; align-items: center;
  flex-wrap: wrap;
}
.filter-select {
  padding: 0.6rem 1rem; border: 1px solid #e0e0e0; background: #fff; color: #555;
  font-family: 'Space Mono', monospace; font-size: 0.72rem; letter-spacing: 0.04em;
  cursor: pointer; outline: none; transition: border-color 0.2s; border-radius: 0;
}
.filter-select:focus { border-color: #0a0a0a; }

.clear-btn {
  background: transparent; border: 1px solid #ff2d6b; color: #ff2d6b;
  padding: 0.55rem 1rem; font-family: 'Space Mono', monospace; font-size: 0.72rem;
  letter-spacing: 0.06em; cursor: pointer; transition: all 0.2s;
}
.clear-btn:hover { background: #ff2d6b; color: #fff; }

.tab-group { display: flex; border: 1px solid #0a0a0a; overflow: hidden; margin-left: auto; flex-shrink: 0; }
.tab-btn {
  background: transparent; border: none; border-right: 1px solid #0a0a0a;
  padding: 0.6rem 1.25rem; color: #aaa; font-size: 0.8rem;
  font-family: 'Space Mono', monospace; letter-spacing: 0.08em; cursor: pointer; transition: all 0.2s;
}
.tab-btn:last-child { border-right: none; }
.tab-btn:hover { background: #f5f5f5; color: #0a0a0a; }
.tab-btn.active { background: #0a0a0a; color: #fff; }

/* ===== State Containers ===== */
.state-container { display: flex; justify-content: center; align-items: center; min-height: 40vh; }
.loading-wrapper { text-align: center; }
.loading-bars { display: flex; gap: 4px; justify-content: center; margin-bottom: 1rem; }
.loading-bars span {
  display: block; width: 3px; height: 28px; background: #ff2d6b;
  border-radius: 2px; animation: barPulse 0.8s ease-in-out infinite alternate;
}
@keyframes barPulse {
  from { transform: scaleY(0.3); opacity: 0.3; }
  to   { transform: scaleY(1); opacity: 1; }
}
.loading-text { font-family: 'Space Mono', monospace; font-size: 0.7rem; letter-spacing: 0.2em; color: #aaa; }
.empty-card { text-align: center; }
.empty-big-text {
  font-family: 'Bebas Neue', sans-serif; font-size: 8rem; color: transparent;
  line-height: 1; letter-spacing: 0.05em; margin-bottom: 1rem; -webkit-text-stroke: 1px #e0e0e0;
}
.empty-desc { color: #aaa; font-size: 0.85rem; margin-bottom: 2rem; }
.cta-btn {
  display: inline-block; background: #ff2d6b; color: #fff; padding: 0.7rem 2rem;
  border: none; cursor: pointer; font-family: 'Space Mono', monospace;
  font-size: 0.75rem; letter-spacing: 0.1em; transition: all 0.2s;
}
.cta-btn:hover { background: #0a0a0a; }

/* ===== Result Hint ===== */
.result-hint { margin-bottom: 1.5rem; }
.mono { font-family: 'Space Mono', monospace; font-size: 0.65rem; letter-spacing: 0.15em; color: #bbb; }

/* ===== Cards Grid ===== */
.cards-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 2rem;
}

@keyframes cardFadeUp {
  from { opacity: 0; transform: translateY(20px); }
  to   { opacity: 1; transform: translateY(0); }
}

.activity-card {
  cursor: pointer; background: transparent;
  animation: cardFadeUp 0.5s cubic-bezier(0.16,1,0.3,1) both;
  transition: transform 0.2s;
}
.activity-card:hover { transform: translateY(-4px); }
.activity-card:hover .cover-img { filter: brightness(0.92); }

/* Cover */
.card-cover {
  position: relative;
  width: 100%;
  aspect-ratio: 3 / 2;
  overflow: hidden;
  background: #f0f0f0;
  margin-bottom: 0.85rem;
}
.cover-img-wrap {
  width: 100%; height: 100%; position: relative;
}
.cover-shimmer {
  position: absolute; inset: 0;
  background: linear-gradient(90deg, #ececec 25%, #f5f5f5 50%, #ececec 75%);
  background-size: 200% 100%;
  animation: shimmer 1.4s infinite;
  z-index: 1;
  transition: opacity 0.3s ease;
}
.cover-shimmer.hidden { opacity: 0; pointer-events: none; }
@keyframes shimmer {
  from { background-position: 200% 0; }
  to   { background-position: -200% 0; }
}
.cover-img {
  width: 100%; height: 100%; object-fit: cover;
  display: block; transition: filter 0.2s, opacity 0.35s ease;
  opacity: 0; position: relative; z-index: 2;
}
.cover-img.img-loaded { opacity: 1; }
.cover-placeholder {
  width: 100%; height: 100%;
  background: linear-gradient(135deg, #1a1a1a, #333);
  display: flex; align-items: center; justify-content: center;
}
.placeholder-type {
  font-family: 'Bebas Neue', sans-serif; font-size: 2rem;
  letter-spacing: 0.1em; color: rgba(255,255,255,0.15);
}

/* Overlay badges */
.cover-badges {
  position: absolute; top: 0.65rem; left: 0.65rem;
  display: flex; gap: 0.4rem; align-items: center;
  z-index: 3;
}
.type-badge {
  font-family: 'Space Mono', monospace; font-size: 0.56rem;
  letter-spacing: 0.1em; color: #fff;
  background: rgba(0,0,0,0.55); backdrop-filter: blur(4px);
  padding: 0.18rem 0.5rem; border-radius: 2px;
}
.registered-badge {
  font-family: 'Space Mono', monospace; font-size: 0.56rem;
  letter-spacing: 0.06em; color: #fff;
  background: rgba(26,122,60,0.85); backdrop-filter: blur(4px);
  padding: 0.18rem 0.5rem; border-radius: 2px;
}
.full-badge {
  font-family: 'Space Mono', monospace; font-size: 0.56rem;
  letter-spacing: 0.1em; color: #fff;
  background: rgba(0,0,0,0.55); backdrop-filter: blur(4px);
  padding: 0.18rem 0.5rem; border-radius: 2px;
}

/* Capacity bar */
.capacity-bar {
  position: absolute; bottom: 0; left: 0; right: 0;
  height: 2px; background: rgba(255,255,255,0.2);
}
.capacity-fill {
  height: 100%; background: #ff2d6b; transition: width 0.3s;
}
.capacity-fill.fill-full { background: #aaa; }

/* Card Info */
.card-info { padding: 0 0.1rem; }
.card-date {
  font-family: 'Space Mono', monospace; font-size: 0.75rem;
  letter-spacing: 0.06em; color: #999; margin-bottom: 0.5rem;
}
.activity-title {
  font-family: 'Noto Sans TC', sans-serif; font-size: 1.05rem;
  font-weight: 700; color: #0a0a0a; margin: 0 0 0.5rem; line-height: 1.45;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
}
.card-meta {
  display: flex; align-items: center; gap: 0.75rem; flex-wrap: wrap;
}
.meta-item {
  font-family: 'Space Mono', monospace; font-size: 0.7rem;
  letter-spacing: 0.04em; color: #999;
}
.meta-fee {
  font-family: 'Space Mono', monospace; font-size: 0.75rem;
  letter-spacing: 0.04em; color: #ff2d6b; font-weight: 700;
}
.meta-free {
  font-family: 'Space Mono', monospace; font-size: 0.75rem;
  letter-spacing: 0.04em; color: #bbb;
}

/* ===== Modal ===== */
.modal-overlay {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.5); display: flex; justify-content: center; align-items: center;
  z-index: 9999; backdrop-filter: blur(4px);
}
.modal-content {
  background: #fff; width: 90%; max-width: 520px; border: 1px solid #0a0a0a;
  animation: modalIn 0.2s cubic-bezier(0.16,1,0.3,1);
}
@keyframes modalIn {
  from { opacity: 0; transform: translateY(20px); }
  to   { opacity: 1; transform: translateY(0); }
}
.modal-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 1.25rem 1.5rem; border-bottom: 1px solid #e0e0e0;
}
.modal-label { font-family: 'Space Mono', monospace; font-size: 0.65rem; letter-spacing: 0.2em; color: #ff2d6b; }
.modal-close {
  background: transparent; border: none; font-size: 1.4rem; color: #aaa;
  cursor: pointer; line-height: 1; transition: color 0.2s; padding: 0;
}
.modal-close:hover { color: #0a0a0a; }
.modal-body { padding: 1.5rem; }
.modal-title {
  font-family: 'Bebas Neue', sans-serif; font-size: 2rem;
  letter-spacing: 0.04em; color: #0a0a0a; margin: 0 0 1.5rem; line-height: 1.1;
}
.modal-info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }
.modal-info-item { display: flex; flex-direction: column; gap: 0.3rem; }
.modal-footer {
  display: flex; justify-content: flex-end; gap: 0.75rem;
  padding: 1.25rem 1.5rem; border-top: 1px solid #e0e0e0;
}
.modal-cancel-btn {
  background: transparent; border: 1px solid #e0e0e0; color: #aaa;
  padding: 0.55rem 1.25rem; font-family: 'Space Mono', monospace; font-size: 0.72rem;
  letter-spacing: 0.08em; cursor: pointer; transition: all 0.2s;
}
.modal-cancel-btn:hover { border-color: #0a0a0a; color: #0a0a0a; }
.modal-confirm-btn {
  background: #ff2d6b; color: #fff; border: none; padding: 0.55rem 1.5rem;
  font-family: 'Space Mono', monospace; font-size: 0.72rem; letter-spacing: 0.08em;
  cursor: pointer; transition: all 0.2s;
}
.modal-confirm-btn:hover:not(:disabled) { background: #0a0a0a; }
.modal-confirm-btn:disabled { background: #e0e0e0; color: #bbb; cursor: not-allowed; }

/* ===== Discount Section ===== */
.discount-section {
  margin-top: 1.5rem;
  padding-top: 1.25rem;
  border-top: 1px solid #e0e0e0;
}
.discount-section-title {
  font-family: 'Space Mono', monospace;
  font-size: 0.6rem;
  letter-spacing: 0.2em;
  color: #aaa;
  margin-bottom: 0.75rem;
}
.discount-loading {
  font-size: 0.8rem;
  color: #aaa;
  font-family: 'Space Mono', monospace;
}
.discount-badge.officer {
  display: inline-block;
  background: #1a7a3c;
  color: #fff;
  padding: 0.35rem 0.85rem;
  border-radius: 4px;
  font-size: 0.82rem;
  font-weight: 600;
}
.free-badge {
  color: #1a7a3c;
  font-size: 0.88rem;
  font-weight: 600;
}
.discount-options {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}
.discount-option {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.6rem 0.9rem;
  border: 1px solid #e0e0e0;
  cursor: pointer;
  transition: border-color 0.15s;
  border-radius: 4px;
}
.discount-option:hover { border-color: #aaa; }
.discount-option.selected { border-color: #ff2d6b; background: rgba(255,45,107,0.03); }
.discount-option input[type="radio"] { accent-color: #ff2d6b; }
.opt-label {
  flex: 1;
  font-size: 0.85rem;
  display: flex;
  flex-direction: column;
  gap: 0.15rem;
}
.opt-label small { font-size: 0.68rem; color: #aaa; font-family: 'Space Mono', monospace; }
.opt-amount {
  font-family: 'Space Mono', monospace;
  font-size: 0.88rem;
  font-weight: 700;
  color: #aaa;
}
.opt-amount.discount { color: #ff2d6b; }
.coupon-select {
  margin-top: 0.25rem;
  padding: 0.4rem 0.75rem;
  border: 1px solid #e0e0e0;
  font-size: 0.78rem;
  font-family: 'Space Mono', monospace;
  outline: none;
  border-radius: 4px;
  width: 100%;
  cursor: pointer;
}
.coupon-select:focus { border-color: #ff2d6b; }

/* 優惠碼 */
.promo-row {
  margin-top: 0.5rem;
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}
.promo-input-wrap {
  display: flex;
  gap: 0.4rem;
}
.promo-input {
  flex: 1;
  padding: 0.45rem 0.75rem;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  font-size: 0.85rem;
  outline: none;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  transition: border-color 0.2s;
}
.promo-input:focus { border-color: #ff2d6b; }
.promo-btn {
  padding: 0.45rem 0.9rem;
  background: #1a1a1a;
  color: #fff;
  border: none;
  border-radius: 6px;
  font-size: 0.82rem;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
  transition: background 0.2s;
}
.promo-btn:hover:not(:disabled) { background: #333; }
.promo-btn:disabled { background: #ccc; cursor: not-allowed; }
.promo-error {
  font-size: 0.75rem;
  color: #dc2626;
  margin: 0;
}
.promo-option { margin-top: 0.25rem; }
.final-amount {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 1rem;
  padding-top: 0.75rem;
  border-top: 1px dashed #e0e0e0;
  font-size: 0.82rem;
  color: #555;
  font-family: 'Space Mono', monospace;
}
.final-price {
  font-weight: 700;
  font-size: 1.05rem;
  color: #ff2d6b;
}
.info-label {
  font-family: 'Space Mono', monospace;
  font-size: 0.6rem;
  letter-spacing: 0.15em;
  color: #aaa;
}
.info-value {
  font-size: 0.88rem;
  color: #0a0a0a;
}
.info-value.amount {
  color: #ff2d6b;
  font-weight: 700;
}

/* ===== Responsive ===== */
@media (max-width: 768px) {
  .cards-container { grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 1.25rem; }
}

@media (max-width: 640px) {
  .activity-list { padding: 5rem 1rem 4rem; }
  .page-title { font-size: 3.5rem; }
  .cards-container { grid-template-columns: 1fr; gap: 1.5rem; }
  .page-header { flex-direction: column; align-items: flex-start; }
  .header-right { align-items: flex-start; }
  .modal-info-grid { grid-template-columns: 1fr; }
  .search-filter-bar { flex-direction: column; align-items: flex-start; }
  .search-wrap { max-width: 100%; }
}
</style>