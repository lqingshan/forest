<template>
  <div class="platform-layout">
    <aside class="platform-layout__sidebar">
      <div class="platform-layout__brand">
        <p class="workspace-page__eyebrow">CXC Commerce</p>
        <h1>平台端</h1>
        <p>企业认证审核与平台治理入口。</p>
      </div>

      <nav class="platform-layout__nav">
        <RouterLink to="/organization-certifications" class="platform-layout__link" active-class="is-active">
          企业认证
        </RouterLink>
      </nav>

      <CurrentUserBadge :session="userSession" />
    </aside>

    <div class="platform-layout__main">
      <header class="platform-layout__header">
        <div>
          <p>Platform Console</p>
          <h2>企业认证审核</h2>
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
import { RouterLink, useRouter } from 'vue-router'
import { CurrentUserBadge } from '@forest/user/web/me'
import { userSession } from '../session'

const router = useRouter()

async function handleLogout() {
  await userSession.logout()
  await router.replace('/login')
}
</script>

<style scoped>
.platform-layout {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 276px minmax(0, 1fr);
}

.platform-layout__sidebar {
  min-height: 100vh;
  padding: 28px 22px;
  border-right: 1px solid var(--workspace-border-soft);
  background: linear-gradient(180deg, var(--workspace-sidebar-start), var(--workspace-sidebar-end));
  display: grid;
  align-content: start;
  gap: 26px;
}

.platform-layout__brand {
  display: grid;
  gap: 10px;
}

.platform-layout__brand h1 {
  margin: 0;
  font-size: 34px;
  line-height: 1;
}

.platform-layout__brand p {
  margin: 0;
  color: var(--workspace-text-secondary);
}

.platform-layout__nav {
  display: grid;
  gap: 8px;
}

.platform-layout__link {
  padding: 12px 14px;
  border-radius: var(--workspace-radius-lg);
  color: var(--workspace-text-secondary);
  border: 1px solid transparent;
  background: var(--workspace-nav-item-background);
}

.platform-layout__link:hover,
.platform-layout__link.is-active {
  color: var(--workspace-text-primary);
  border-color: var(--workspace-nav-item-active-border);
  background: var(--workspace-nav-item-active-background);
}

.platform-layout__main {
  padding: 22px 26px 34px;
}

.platform-layout__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--workspace-border-soft);
}

.platform-layout__header p {
  margin: 0;
  font-size: 11px;
  font-family: var(--workspace-font-mono);
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: var(--workspace-text-tertiary);
}

.platform-layout__header h2 {
  margin: 8px 0 0;
  font-size: 32px;
  line-height: 1;
}
</style>
