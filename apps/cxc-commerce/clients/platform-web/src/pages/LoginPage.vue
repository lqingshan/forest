<template>
  <div class="platform-login">
    <section class="platform-login__hero workspace-card workspace-card--paper">
      <div class="workspace-card__body">
        <p class="workspace-page__eyebrow">CXC Platform</p>
        <h1>平台审核台</h1>
        <p>平台端使用手机号密码或手机验证码登录，负责企业认证审核等跨商家运营能力。</p>
      </div>
    </section>

    <section class="platform-login__panel workspace-card workspace-card--paper">
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
  await router.replace('/organization-certifications')
}
</script>

<style scoped>
.platform-login {
  min-height: 100vh;
  display: grid;
  align-items: center;
  grid-template-columns: minmax(0, 1.1fr) minmax(360px, 440px);
  gap: 22px;
  padding: 28px;
}

.platform-login__hero {
  align-self: stretch;
  display: grid;
  align-items: center;
}

.platform-login__hero h1 {
  margin: 0;
  font-size: clamp(48px, 6vw, 80px);
  line-height: 0.96;
}

.platform-login__hero p:last-child {
  margin: 16px 0 0;
  max-width: 540px;
  line-height: 1.8;
  color: var(--workspace-text-secondary);
}

.platform-login__panel {
  display: flex;
  align-items: center;
  justify-content: center;
}

.platform-login__panel :deep(.forest-web-pc-login-panel) {
  width: min(100%, 420px);
  padding: 28px;
  --forest-web-pc-login-tabs-border: 1px solid var(--workspace-border);
  --forest-web-pc-login-tabs-background: var(--workspace-surface-muted);
  --forest-web-pc-login-tab-color: var(--workspace-text-secondary);
  --forest-web-pc-login-tab-active-background: var(--workspace-button-primary-background);
  --forest-web-pc-login-tab-active-color: var(--workspace-button-primary-text);
}

.platform-login__panel :deep(.forest-password-login-panel__heading h2),
.platform-login__panel :deep(.forest-phone-sms-login-panel__heading h2) {
  margin: 10px 0 0;
  font-size: 34px;
}

.platform-login__panel :deep(.forest-password-login-panel__heading p),
.platform-login__panel :deep(.forest-phone-sms-login-panel__heading p) {
  margin-top: 10px;
  color: var(--workspace-text-secondary);
  line-height: 1.6;
}

.platform-login__panel :deep(.forest-password-login-panel__field),
.platform-login__panel :deep(.forest-phone-sms-login-panel__field) {
  margin-top: 18px;
}

.platform-login__panel :deep(input) {
  padding: 14px 16px;
}

.platform-login__panel :deep(.workspace-button),
.platform-login__panel :deep(.forest-phone-sms-login-panel__submit) {
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

@media (max-width: 900px) {
  .platform-login {
    grid-template-columns: 1fr;
    padding: 18px;
  }
}
</style>
