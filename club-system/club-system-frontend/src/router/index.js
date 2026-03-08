import { createRouter, createWebHistory } from 'vue-router'
// import HomeView from '../views/HomeView.vue'
import { useUserStore } from '@/stores/user'
import Leave from '../views/LeaveRequestView.vue'

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
      // name: 'home',
      // component: Leave,
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
      path: '/payment-history',
      name: 'payment-history',
      component: () =>import('../views/member/PaymentHistory.vue'),
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
    {
      path: '/leave-request-member',
      name: 'leave-request-member',
      component: () => import('@/views/member/LeaveRequestMember.vue')
    },
    {
      path: '/leave-request',
      name: 'leave-request',
      component: () => import('@/views/admin/LeaveRequest.vue')
    },
    {
      path: '/leave-activity',
      name: 'leave-activity',
      component: () => import('@/views/member/LeaveActivity.vue')
    },
    {
      path: '/leave-request-view',
      name: 'leave-request-vue',
      component: () => import('@/views/LeaveRequestView.vue')
    },
    {
      path: '/add-leave',
      name: 'add-leave',
      component: () => import('@/views/member/AddLeave.vue')
    },
      // 管理員簽到 Dashboard
    {
      path: '/admin/checkin/dashboard',
      name: 'checkin-dashboard',
      component: () => import('@/views/admin/CheckInDashboard.vue'),
      meta: { requiresAuth: true },
    },
    // 會員自助簽到頁（QR Code 掃碼後導入此頁）
  // URL 格式：/checkin/member?activityId=123
    {
      path: '/checkin/member',
      name: 'member-checkin',
      component: () => import('@/views/member/MemberCheckIn.vue'),
    },
    // 管理後台路由
    {
      path: '/admin/login',
      name: 'admin-login',
      component: () => import('@/views/admin/AdminLogin.vue'),
    },
    {
      path: '/admin/dashboard',
      name: 'admin-dashboard',
      component: () => import('@/views/admin/AdminDashboard.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
    },
    {
      path: '/admin/users',
      name: 'admin-users',
      component: () => import('@/views/admin/UserManagement.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
    },
    {
      path: '/admin/payments',
      name: 'admin-payments',
      component: () => import('@/views/admin/PaymentManagement.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
    },
    {
      path: '/my-registration-history',
      name: 'my-registration-history',
      component: () => import('@/views/member/RegistrationHistory.vue'),
    }
  ],
})

router.beforeEach((to) => {
  const userStore = useUserStore()

  // 如果已登入且要去登入/註冊頁，根據角色重定向
  if (userStore.isLoggedIn && (to.name === 'login' || to.name === 'register')) {
    if (userStore.userRole?.toUpperCase() === 'ADMIN') {
      return { name: 'admin-dashboard' }
    }
    return { name: 'dashboard' }
  }

  // 如果已登入且要去管理員登入頁，檢查是否為管理員
  if (userStore.isLoggedIn && to.name === 'admin-login') {
    if (userStore.userRole?.toUpperCase() === 'ADMIN') {
      return { name: 'admin-dashboard' }
    } else {
      return { name: 'dashboard' }
    }
  }

  // 頁面需要登入，但沒有登入的情況
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    // 如果是管理後台頁面，導向管理員登入
    if (to.meta.requiresAdmin) {
      return { name: 'admin-login' }
    }
    return { name: 'login' }
  }

  // 頁面需要管理員權限，但不是管理員
  if (to.meta.requiresAdmin && userStore.userRole?.toUpperCase() !== 'ADMIN') {
    return { name: 'dashboard' }
  }
})

export default router
