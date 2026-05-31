import { createRouter, createWebHistory } from 'vue-router'
import WorkspaceLayout from './layouts/WorkspaceLayout.vue'
import AccessPage from './pages/AccessPage.vue'
import CertificationPage from './pages/CertificationPage.vue'
import DepartmentsPage from './pages/DepartmentsPage.vue'
import LoginPage from './pages/LoginPage.vue'
import MembersPage from './pages/MembersPage.vue'
import OrganizationsPage from './pages/OrganizationsPage.vue'
import { can, refreshOrganizations, resetOrganizations, selectedOrganizationCertified } from './organization-state'
import { userSession } from './session'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/organizations'
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
          path: '/organizations',
          name: 'organizations',
          component: OrganizationsPage
        },
        {
          path: '/certification',
          name: 'certification',
          component: CertificationPage
        },
        {
          path: '/departments',
          name: 'departments',
          component: DepartmentsPage,
          meta: { requireCertified: true, permission: 'organization.department.read' }
        },
        {
          path: '/members',
          name: 'members',
          component: MembersPage,
          meta: { requireCertified: true, permission: 'organization.member.read' }
        },
        {
          path: '/access',
          name: 'access',
          component: AccessPage,
          meta: { requireCertified: true, permissionAny: ['access.role.read', 'access.assignment.read'] }
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
      resetOrganizations()
      return true
    }
    const restored = await userSession.restore()
    if (!restored) {
      resetOrganizations()
      return true
    }
    return '/organizations'
  }

  if (!hasToken) {
    resetOrganizations()
    return '/login'
  }

  const restored = await userSession.restore()
  if (!restored) {
    userSession.clear()
    resetOrganizations()
    return '/login'
  }
  await refreshOrganizations()
  if (to.meta.requireCertified && !selectedOrganizationCertified.value) {
    return '/certification'
  }
  const permission = typeof to.meta.permission === 'string' ? to.meta.permission : ''
  if (permission && !can(permission)) {
    return '/organizations'
  }
  const permissionAny = Array.isArray(to.meta.permissionAny) ? to.meta.permissionAny : []
  if (permissionAny.length && !permissionAny.some((item) => typeof item === 'string' && can(item))) {
    return '/organizations'
  }
  return true
})

export default router
