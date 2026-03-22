<script setup>
import { computed } from 'vue'
import { useRouter, useRoute, RouterLink } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useUiStore } from '@/stores/ui'
import { Bars3Icon, UserCircleIcon, ArrowRightOnRectangleIcon } from '@heroicons/vue/24/outline'

const userStore = useUserStore()
const uiStore = useUiStore()
const router = useRouter()
const route = useRoute()

const isAdmin = computed(() => userStore.userRole?.toUpperCase() === 'ADMIN')
const isLanding = computed(() => route.name === 'dashboard')

function handleLogout() {
  userStore.logout()
  router.push('/')
}
</script>

<template>
  <nav class="navbar" :class="{ transparent: isLanding }">

    <!-- 左：漢堡 -->
    <div class="nav-left">
      <button class="icon-btn" @click="uiStore.toggleSidebar">
        <Bars3Icon class="icon" />
      </button>
    </div>

    <!-- 中：品牌名 -->
    <div class="nav-center">
      <RouterLink
        :to="userStore.isLoggedIn ? '/dashboard' : '/'"
        class="brand"
      >
        CLUB SYSTEM
      </RouterLink>
    </div>

    <!-- 右：user icon + logout -->
    <div class="nav-right">
      <router-link to="/profile" class="icon-btn" title="個人資料">
        <UserCircleIcon class="icon" />
      </router-link>
      <button class="icon-btn" @click="handleLogout" title="登出">
        <ArrowRightOnRectangleIcon class="icon" />
      </button>
    </div>

  </nav>
</template>

<style scoped>
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 800;
  height: 56px;
  background-color: #0f0f1a;
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: center;
  padding: 0 1.2rem;
  border-bottom: 1px solid #2e2e45;
  transition: background 0.3s ease, border-color 0.3s ease;
}

.navbar.transparent {
  background-color: transparent;
  border-bottom: none;
}

/* 左 */
.nav-left {
  display: flex;
  align-items: center;
}

/* 中 */
.nav-center {
  display: flex;
  justify-content: center;
}

.brand {
  font-size: 0.85rem;
  font-weight: 700;
  letter-spacing: 0.25em;
  text-transform: uppercase;
  text-decoration: none;
  background: linear-gradient(90deg, #ff6b6b, #ff4d8d, #c084fc);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  transition: filter 0.2s;
}

.brand:hover {
  filter: brightness(1.3) drop-shadow(0 0 8px rgba(255, 100, 150, 0.7));
}

/* 右 */
.nav-right {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 0.5rem;
}

/* Icon 按鈕 */
.icon-btn {
  background: transparent;
  border: none;
  cursor: pointer;
  padding: 0.4rem;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ccc;
  text-decoration: none;
  transition: color 0.2s, background 0.2s;
}

.icon-btn:hover {
  color: #fff;
  background: rgba(255,255,255,0.08);
}

.icon {
  width: 22px;
  height: 22px;
}
</style>
