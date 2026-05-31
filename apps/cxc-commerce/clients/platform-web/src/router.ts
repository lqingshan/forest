import { createRouter, createWebHistory } from 'vue-router'
import WorkspaceLayout from './layouts/WorkspaceLayout.vue'
import CertificationReviewPage from './pages/CertificationReviewPage.vue'
import LoginPage from './pages/LoginPage.vue'
import { userSession } from './session'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/organization-certifications'
    },
    {
      path: '/login',
      name: 'login',
      component: LoginPage,
      meta: { public: true }
    },
    {
      path: '/',
      component: WorkspaceLayout,
      children: [
        {
          path: '/organization-certifications',
          name: 'organization-certifications',
          component: CertificationReviewPage
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
    return restored ? '/organization-certifications' : true
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
