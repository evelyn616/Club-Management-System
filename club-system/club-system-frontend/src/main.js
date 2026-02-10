import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import { useUserStore } from './stores/user'

const app = createApp(App)

app.use(createPinia())
app.use(router)

// 在 mount 之前還原用戶狀態
const userStore = useUserStore()
await userStore.restoreFromStorage()

app.mount('#app') 