import { createRouter, createWebHistory } from 'vue-router'
import WorkspaceLayout from './layouts/WorkspaceLayout.vue'
import LeadManagementPage from './pages/LeadManagementPage.vue'
import LoginPage from './pages/LoginPage.vue'
import UserManagementPage from './pages/UserManagementPage.vue'
import UserPointPage from './pages/UserPointPage.vue'
import { userSession } from './session'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/users'
    },
    {
      path: '/login',
      name: 'login',
      component: LoginPage,
      meta: { public: true }
    },
    {
      path: '/point',
      redirect: '/user-point'
    },
    {
      path: '/',
      component: WorkspaceLayout,
      children: [
        {
          path: '/users',
          name: 'users',
          component: UserManagementPage
        },
        {
          path: '/user-point',
          name: 'user-point',
          component: UserPointPage
        },
        {
          path: '/lead',
          name: 'lead',
          component: LeadManagementPage
        }
      ]
    }
  ]
})

router.beforeEach(async (to) => {
  const hasToken = Boolean(userSession.getAccessToken())
  const isPublic = Boolean(to.meta.public)

  if (isPublic) {
    if (!hasToken) {
      return true
    }
    const restored = await userSession.restore()
    return restored ? '/users' : true
  }

  if (!hasToken) {
    return '/login'
  }

  const restored = await userSession.restore()
  if (!restored) {
    userSession.clear()
    return '/login'
  }
  return true
})

export default router
