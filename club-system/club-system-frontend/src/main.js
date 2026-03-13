import './assets/main.css'
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)

// Pinia 掛載後才呼叫
import { useUserStore } from '@/stores/user'
const userStore = useUserStore()
userStore.restoreFromStorage()  // ← 加這行

app.mount('#app')