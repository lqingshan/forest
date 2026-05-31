import { createWechatMiniappClientApp } from '@forest/wechat-miniapp-client-app'
import { cxcCommerceBuyerMiniappDefinition } from './app-definition'

export const miniappApp = createWechatMiniappClientApp(cxcCommerceBuyerMiniappDefinition)

export const miniappLifecycle = miniappApp.lifecycle
export const miniappAuth = miniappApp.auth
export const miniappRouter = miniappApp.router
export const miniappPlatform = miniappApp.platform
