import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'landing',
      component: () => import('../views/LandingView.vue'),
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue'),
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/RegisterView.vue'),
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: () => import('../views/DashboardView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('../views/ProfileView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/about',
      name: 'about',
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import('../views/AboutView.vue'),
    },
    {
      path: '/my-registrations',
      name: 'my-registrations',
      component: () =>import('../views/member/MyRegistrations.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/pending-payments',
      name: 'pending-payments',
      component: () =>import('../views/member/PendingPayments.vue'),
      meta: { requiresAuth: true },
    },
    
    {
      path: '/admin/create-activity-container',
      name: 'create-activity-container',
      component: () =>import('../views/admin/CreateActivity.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/admin/update-activity-container/:activityId',
      name: 'update-activity-container',
      component: () =>import('../views/admin/UpdateActivity.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/admin/activity-list-container',
      name: 'activity-list-container',
      component: () =>import('../views/admin/ActivityList.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/admin/activity-management-container',
      name: 'activity-management-container',
      component: () =>import('../views/admin/ActivityManagement.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/activity-registration-list-container',
      name: 'activity-registration-list-container',
      component: () =>import('../views/member/ActivityRegistrationList.vue'),
    },
    {
      path: '/admin/publish-activity-container/:activityId?',
      name: 'publish-activity-container',
      component: () =>import('../views/admin/PublishActivity.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/admin/draft-box-container',
      name: 'draft-box-container',
      component: () => import('@/views/admin/DraftBox.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/admin/draft-box-container',
      name: 'draft-box-container',
      component: () => import('@/views/admin/DraftBox.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/admin/registrations-overview-container',
      name: 'registrations-overview-container',
      component: () => import('@/views/admin/RegistrationsOverview.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/admin/registration-detail-container/:activityId',
      name: 'registration-detail-container',
      component: () => import('@/views/admin/RegistrationDetail.vue'),
      meta: { requiresAuth: true },
    },
  ],
})

router.beforeEach((to) => {
  const userStore = useUserStore()

  // 如果已登入且要去登入/註冊頁，重定向到 dashboard
  if (userStore.isLoggedIn && (to.name === 'login' || to.name === 'register')) {
    return { name: 'dashboard' }
  }

  // 頁面需要登入，但沒有登入的情況
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    return { name: 'login' }
  }
})

export default router
