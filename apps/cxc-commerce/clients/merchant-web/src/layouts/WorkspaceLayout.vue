<template>
  <div class="merchant-layout">
    <aside class="merchant-layout__sidebar">
      <div class="merchant-layout__brand">
        <p class="workspace-page__eyebrow">CXC Commerce</p>
        <h1>商家工作台</h1>
        <p>{{ currentOrganizationName }}</p>
      </div>

      <nav class="merchant-layout__nav">
        <RouterLink to="/organizations" class="merchant-layout__link" active-class="is-active">
          企业
        </RouterLink>
        <RouterLink to="/certification" class="merchant-layout__link" active-class="is-active">
          认证
        </RouterLink>
        <RouterLink
          v-if="selectedOrganizationCertified && can('organization.department.read')"
          to="/departments"
          class="merchant-layout__link"
          active-class="is-active"
        >
          部门
        </RouterLink>
        <RouterLink
          v-if="selectedOrganizationCertified && can('organization.member.read')"
          to="/members"
          class="merchant-layout__link"
          active-class="is-active"
        >
          员工
        </RouterLink>
        <RouterLink
          v-if="selectedOrganizationCertified && (can('access.role.read') || can('access.assignment.read'))"
          to="/access"
          class="merchant-layout__link"
          active-class="is-active"
        >
          角色权限
        </RouterLink>
      </nav>

      <div class="merchant-layout__user">
        <strong>{{ userSession.state.currentUser?.name || userSession.state.currentUser?.phone || '当前用户' }}</strong>
        <span>{{ userSession.state.currentUser?.phone || '已登录' }}</span>
      </div>
    </aside>

    <div class="merchant-layout__main">
      <header class="merchant-layout__header">
        <div>
          <p>Merchant Console</p>
          <h2>{{ pageTitle }}</h2>
        </div>
        <button type="button" class="workspace-button-soft" @click="handleLogout">
          退出登录
        </button>
      </header>

      <main>
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { userSession } from '../session'
import { can, resetOrganizations, selectedOrganization, selectedOrganizationCertified } from '../organization-state'

const route = useRoute()
const router = useRouter()

const pageTitle = computed(() => {
  switch (route.path) {
    case '/organizations':
      return '企业管理'
    case '/certification':
      return '企业认证'
    case '/departments':
      return '部门管理'
    case '/members':
      return '员工管理'
    case '/access':
      return '角色权限'
    default:
      return '商家端'
  }
})

const currentOrganizationName = computed(() => selectedOrganization.value?.organizationName || '请选择企业')

async function handleLogout() {
  await userSession.logout()
  resetOrganizations()
  await router.replace('/login')
}
</script>

<style scoped>
.merchant-layout {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 276px minmax(0, 1fr);
}

.merchant-layout__sidebar {
  min-height: 100vh;
  padding: 28px 22px;
  border-right: 1px solid var(--workspace-border-soft);
  background: linear-gradient(180deg, var(--workspace-sidebar-start), var(--workspace-sidebar-end));
  display: grid;
  align-content: start;
  gap: 26px;
}

.merchant-layout__brand {
  display: grid;
  gap: 10px;
}

.merchant-layout__brand h1 {
  margin: 0;
  font-size: 34px;
  line-height: 1;
}

.merchant-layout__brand p {
  margin: 0;
  color: var(--workspace-text-secondary);
}

.merchant-layout__nav {
  display: grid;
  gap: 8px;
}

.merchant-layout__link {
  padding: 12px 14px;
  border-radius: var(--workspace-radius-lg);
  color: var(--workspace-text-secondary);
  border: 1px solid transparent;
  background: var(--workspace-nav-item-background);
}

.merchant-layout__link:hover,
.merchant-layout__link.is-active {
  color: var(--workspace-text-primary);
  border-color: var(--workspace-nav-item-active-border);
  background: var(--workspace-nav-item-active-background);
}

.merchant-layout__user {
  display: grid;
  gap: 4px;
  padding: 14px;
  border: 1px solid var(--workspace-border-soft);
  border-radius: var(--workspace-radius-lg);
  background: var(--workspace-surface-strong);
}

.merchant-layout__user span {
  color: var(--workspace-text-secondary);
}

.merchant-layout__main {
  padding: 22px 26px 34px;
}

.merchant-layout__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--workspace-border-soft);
}

.merchant-layout__header p {
  margin: 0;
  font-size: 11px;
  font-family: var(--workspace-font-mono);
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: var(--workspace-text-tertiary);
}

.merchant-layout__header h2 {
  margin: 8px 0 0;
  font-size: 32px;
  line-height: 1;
}

@media (max-width: 960px) {
  .merchant-layout {
    grid-template-columns: 1fr;
  }

  .merchant-layout__sidebar {
    min-height: auto;
  }
}
</style>
