<template>
  <div class="workspace-layout">
    <aside class="workspace-layout__sidebar">
      <div class="workspace-layout__brand">
        <p class="workspace-layout__eyebrow">Forest · Trade Leads</p>
        <h1>运营工作台</h1>
        <p class="workspace-layout__copy">
          这是一套偏文档化的运营后台。用户像档案，积分像账本，线索像待整理的贸易记录。
        </p>
      </div>

      <nav class="workspace-layout__nav">
        <RouterLink to="/users" class="workspace-layout__link" active-class="is-active">
          用户管理
        </RouterLink>
        <RouterLink to="/user-point" class="workspace-layout__link" active-class="is-active">
          积分查询
        </RouterLink>
        <RouterLink to="/lead" class="workspace-layout__link" active-class="is-active">
          线索管理
        </RouterLink>
      </nav>

      <CurrentUserBadge :session="userSession" />
    </aside>

    <div class="workspace-layout__main">
      <header class="workspace-layout__header">
        <div>
          <p>Forest Workspace</p>
          <h2>{{ pageTitle }}</h2>
        </div>
        <button type="button" class="workspace-layout__logout" @click="handleLogout">
          退出登录
        </button>
      </header>

      <main class="workspace-layout__content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { CurrentUserBadge } from '@forest/user/web/me'
import { userSession } from '../session'

const route = useRoute()
const router = useRouter()

const pageTitle = computed(() => {
  switch (route.path) {
    case '/users':
      return '用户管理'
    case '/user-point':
      return '积分查询'
    case '/lead':
      return '线索管理'
    default:
      return '运营后台'
  }
})

async function handleLogout() {
  await userSession.logout()
  await router.replace('/login')
}
</script>

<style scoped>
.workspace-layout {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 292px minmax(0, 1fr);
}

.workspace-layout__sidebar {
  padding: 28px 22px;
  background: linear-gradient(180deg, var(--workspace-sidebar-start), var(--workspace-sidebar-end));
  color: var(--workspace-text-primary);
  position: sticky;
  top: 0;
  min-height: 100vh;
  border-right: 1px solid var(--workspace-border-soft);
  display: grid;
  align-content: start;
  gap: 28px;
}

.workspace-layout__eyebrow {
  margin: 0;
  font-size: 11px;
  font-family: var(--workspace-font-mono);
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: var(--workspace-text-tertiary);
}

.workspace-layout__brand {
  display: grid;
  gap: 12px;
}

.workspace-layout__sidebar h1 {
  margin: 0;
  font-size: 38px;
  line-height: 1.04;
}

.workspace-layout__copy {
  margin: 0;
  line-height: 1.7;
  color: var(--workspace-text-secondary);
}

.workspace-layout__nav {
  display: grid;
  gap: 8px;
}

.workspace-layout__link {
  padding: 13px 14px;
  border-radius: var(--workspace-radius-lg);
  border: 1px solid transparent;
  color: var(--workspace-text-secondary);
  background: var(--workspace-nav-item-background);
  transition: var(--workspace-motion-normal) var(--workspace-ease-standard);
}

.workspace-layout__link:hover,
.workspace-layout__link.is-active {
  background: var(--workspace-nav-item-active-background);
  border-color: var(--workspace-nav-item-active-border);
  color: var(--workspace-text-primary);
  transform: translateX(2px);
}

.workspace-layout__main {
  padding: 22px 26px 32px;
}

.workspace-layout__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--workspace-border-soft);
}

.workspace-layout__header p {
  margin: 0;
  font-size: 11px;
  font-family: var(--workspace-font-mono);
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: var(--workspace-text-tertiary);
}

.workspace-layout__header h2 {
  margin: 8px 0 0;
  font-size: 34px;
  line-height: 1.02;
}

.workspace-layout__logout {
  border: 1px solid var(--workspace-border-soft);
  border-radius: var(--workspace-radius-pill);
  padding: 11px 16px;
  background: var(--workspace-surface-strong);
  color: var(--workspace-text-primary);
  cursor: pointer;
  box-shadow: var(--workspace-shadow-soft);
}

.workspace-layout__content {
  display: grid;
}

@media (max-width: 980px) {
  .workspace-layout {
    grid-template-columns: 1fr;
  }

  .workspace-layout__sidebar {
    min-height: auto;
    position: relative;
  }

  .workspace-layout__header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
