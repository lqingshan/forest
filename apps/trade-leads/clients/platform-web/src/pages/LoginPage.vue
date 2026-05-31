<template>
  <div class="login-page">
    <section class="login-page__hero workspace-card workspace-card--paper">
      <div class="workspace-card__body">
        <p class="login-page__eyebrow">Forest · Trade Leads</p>
        <h1>进入运营笔记本</h1>
        <p class="login-page__copy">
          这里不是一个喧闹的控制台，而是一张安静的工作台。用户、积分与线索会在同一份运营档案里被整理、检索与处理。
        </p>
      </div>
    </section>

    <section class="login-page__panel workspace-card workspace-card--paper">
      <WebPcLoginFlow
        :session="userSession"
        default-mode="password"
        @success="handleSuccess"
      />
    </section>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { WebPcLoginFlow } from '@forest/user/web/auth'
import { userSession } from '../session'

const router = useRouter()

async function handleSuccess() {
  await router.replace('/users')
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  align-items: center;
  grid-template-columns: minmax(0, 1.1fr) minmax(380px, 460px);
  gap: 22px;
  padding: 28px;
}

.login-page__hero {
  position: relative;
  align-self: stretch;
  display: grid;
  align-items: center;
  overflow: hidden;
  isolation: isolate;
}

.login-page__hero::after {
  content: '';
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 18% 20%, var(--workspace-page-decor-primary), transparent 28%),
    radial-gradient(circle at 84% 12%, var(--workspace-page-decor-secondary), transparent 24%);
  pointer-events: none;
  z-index: 0;
}

.login-page__hero :deep(.workspace-card__body) {
  position: relative;
  z-index: 1;
}

.login-page__eyebrow {
  margin: 0;
  font-size: 11px;
  font-family: var(--workspace-font-mono);
  letter-spacing: 0.15em;
  text-transform: uppercase;
  color: var(--workspace-text-tertiary);
}

.login-page__hero h1 {
  margin: 14px 0 0;
  font-size: clamp(44px, 6vw, 78px);
  line-height: 0.95;
  max-width: 520px;
}

.login-page__copy {
  margin: 16px 0 0;
  max-width: 520px;
  line-height: 1.8;
  color: var(--workspace-text-secondary);
}

.login-page__panel {
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-page__panel :deep(.forest-web-pc-login-panel) {
  width: min(100%, 420px);
  padding: 28px;
  --forest-web-pc-login-tabs-border: 1px solid var(--workspace-border);
  --forest-web-pc-login-tabs-background: var(--workspace-surface-muted);
  --forest-web-pc-login-tab-color: var(--workspace-text-secondary);
  --forest-web-pc-login-tab-active-background: var(--workspace-button-primary-background);
  --forest-web-pc-login-tab-active-color: var(--workspace-button-primary-text);
}

.login-page__panel :deep(.forest-password-login-panel__heading h2),
.login-page__panel :deep(.forest-phone-sms-login-panel__heading h2) {
  margin: 10px 0 0;
  font-size: 34px;
}

.login-page__panel :deep(.forest-password-login-panel__heading p),
.login-page__panel :deep(.forest-phone-sms-login-panel__heading p) {
  margin-top: 10px;
  color: var(--workspace-text-secondary);
  line-height: 1.6;
}

.login-page__panel :deep(.forest-password-login-panel__field),
.login-page__panel :deep(.forest-phone-sms-login-panel__field) {
  margin-top: 18px;
}

.login-page__panel :deep(input) {
  padding: 14px 16px;
}

.login-page__panel :deep(.workspace-button),
.login-page__panel :deep(.forest-phone-sms-login-panel__submit) {
  margin-top: 8px;
  width: 100%;
  border: 1px solid transparent;
  border-radius: var(--workspace-radius-pill);
  padding: 14px 18px;
  background: var(--workspace-button-primary-background);
  color: var(--workspace-button-primary-text);
  cursor: pointer;
  font-size: 16px;
  box-shadow: var(--workspace-button-primary-shadow);
}

@media (max-width: 960px) {
  .login-page {
    grid-template-columns: 1fr;
    padding: 18px;
  }

  .login-page__hero {
    order: 2;
  }
}
</style>
