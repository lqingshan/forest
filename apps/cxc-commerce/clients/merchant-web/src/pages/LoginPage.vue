<template>
  <div class="merchant-login">
    <section class="merchant-login__intro workspace-card workspace-card--paper">
      <div class="workspace-card__body">
        <p class="workspace-page__eyebrow">CXC Commerce</p>
        <h1>企业协作从账号开始</h1>
        <p>商家员工使用手机号和企业管理员设置的密码登录，登录后再选择要进入的企业。</p>
      </div>
    </section>

    <section class="merchant-login__panel workspace-card workspace-card--paper">
      <div class="workspace-card__body">
        <WebPcLoginFlow
          :session="userSession"
          default-mode="password"
          password-title="商家端密码登录"
          password-submit-text="密码登录"
          sms-title="商家端验证码登录"
          sms-submit-text="验证码登录"
          @success="handleSuccess"
        />
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { WebPcLoginFlow } from '@forest/user/web/auth'
import { resetOrganizations } from '../organization-state'
import { userSession } from '../session'

const router = useRouter()

async function handleSuccess() {
  resetOrganizations()
  await router.replace('/organizations')
}
</script>

<style scoped>
.merchant-login {
  min-height: 100vh;
  display: grid;
  align-items: center;
  grid-template-columns: minmax(0, 1.1fr) minmax(360px, 440px);
  gap: 22px;
  padding: 28px;
}

.merchant-login__intro {
  align-self: stretch;
  display: grid;
  align-items: center;
}

.merchant-login__intro h1 {
  margin: 0;
  max-width: 560px;
  font-size: clamp(44px, 6vw, 76px);
  line-height: 0.98;
}

.merchant-login__intro p:last-child {
  margin: 18px 0 0;
  max-width: 540px;
  line-height: 1.8;
  color: var(--workspace-text-secondary);
}

.merchant-login__panel :deep(.forest-web-pc-login-panel) {
  --forest-web-pc-login-tabs-border: 1px solid var(--workspace-border);
  --forest-web-pc-login-tabs-background: var(--workspace-surface-muted);
  --forest-web-pc-login-tab-color: var(--workspace-text-secondary);
  --forest-web-pc-login-tab-active-background: var(--workspace-button-primary-background);
  --forest-web-pc-login-tab-active-color: var(--workspace-button-primary-text);
}

.merchant-login__panel :deep(.forest-password-login-panel__heading h2),
.merchant-login__panel :deep(.forest-phone-sms-login-panel__heading h2) {
  margin: 10px 0 0;
  font-size: 30px;
}

.merchant-login__panel :deep(.forest-password-login-panel__heading p),
.merchant-login__panel :deep(.forest-phone-sms-login-panel__heading p) {
  margin-top: 10px;
  color: var(--workspace-text-secondary);
  line-height: 1.6;
}

.merchant-login__panel :deep(.forest-password-login-panel__field),
.merchant-login__panel :deep(.forest-phone-sms-login-panel__field) {
  margin-top: 16px;
}

.merchant-login__panel :deep(input) {
  padding: 13px 15px;
}

.merchant-login__panel :deep(.workspace-button),
.merchant-login__panel :deep(.forest-phone-sms-login-panel__submit) {
  margin-top: 8px;
  width: 100%;
  border: 1px solid transparent;
  border-radius: var(--workspace-radius-pill);
  padding: 13px 18px;
  background: var(--workspace-button-primary-background);
  color: var(--workspace-button-primary-text);
  cursor: pointer;
  font-size: 16px;
  box-shadow: var(--workspace-button-primary-shadow);
}

@media (max-width: 900px) {
  .merchant-login {
    grid-template-columns: 1fr;
    padding: 18px;
  }
}
</style>
