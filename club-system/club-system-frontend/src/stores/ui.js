import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUiStore = defineStore('ui', () => {
  const sidebarOpen = ref(false) // 永遠預設收起

  function toggleSidebar() {
    sidebarOpen.value = !sidebarOpen.value
  }

  function setSidebar(val) {
    sidebarOpen.value = val
  }

  return { sidebarOpen, toggleSidebar, setSidebar }
})
