import { createWechatMiniappClientApp } from '@forest/wechat-miniapp-client-app'
import { tradeLeadsMiniappDefinition } from './app-definition'

export const miniappApp = createWechatMiniappClientApp(tradeLeadsMiniappDefinition)

export const miniappLifecycle = miniappApp.lifecycle
export const miniappAuth = miniappApp.auth
export const miniappRouter = miniappApp.router
export const miniappPayment = miniappApp.payment
export const miniappPlatform = miniappApp.platform
