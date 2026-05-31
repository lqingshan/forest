import { createWebUserSession } from '@forest/user/web/auth'
import { appConfig } from './app.config'

export const userSession = createWebUserSession({
  appCode: appConfig.appCode,
  clientType: appConfig.clientType,
  accessScope: appConfig.accessScope,
  storagePrefix: 'forest.cxc-commerce.platform'
})
