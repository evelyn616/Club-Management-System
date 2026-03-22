<script setup>
import { RouterView, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { computed } from 'vue'
import Sidebar from './components/Sidebar.vue'
import Navbar from './components/Navbar.vue'

const userStore = useUserStore()
const route = useRoute()

const showLayout = computed(() => userStore.isLoggedIn)

// dashboard 不推下去（navbar 透明懸空）
const contentStyle = computed(() => ({
  marginTop: route.name === 'dashboard' ? '0' : '56px'
}))
</script>

<template>
  <template v-if="showLayout">
    <Navbar />
    <Sidebar />
    <main class="content" :style="contentStyle">
      <RouterView />
    </main>
  </template>

  <template v-else>
    <RouterView />
  </template>
</template>

<style scoped>
.content {
  flex: 1;
}
</style>
