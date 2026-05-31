/**
 * @forest/wechat-miniapp-platform 的唯一 public facade。
 *
 * 外部 app 继续只从 @forest/wechat-miniapp-platform 导入能力；
 * 包内实现按职责拆到 runtime/storage/session/router/http/login/payment。
 * 这样既保持公共 API 简单，又让源码内部高内聚、容易学习。
 */

export type { MiniappPage, MiniappRecord, MiniappRuntime, MiniappRuntimeOptions } from './runtime'

export { createMiniappStorage } from './storage'
export type { CreateMiniappStorageOptions, MiniappStorage } from './storage'

export { createSessionStore } from './session-store'
export type {
  ClearSessionStoreOptions,
  CreateSessionStoreOptions,
  MiniappSessionStore
} from './session-store'

export { createMiniappRouter } from './router'
export type { CreateMiniappRouterOptions, MiniappRouteCallbacks, MiniappRouter } from './router'

export {
  configureMiniappHttpSession,
  createWxHttpTransport
} from './http-transport'
export type { ConfigureMiniappHttpSessionOptions } from './http-transport'

export { requestWechatLoginCode } from './wechat-login'

export { requestWechatMiniappPayment } from './wechat-miniapp-payment'
export type { WechatMiniappPaymentParams } from './wechat-miniapp-payment'
