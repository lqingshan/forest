import type { CreateWechatMiniappClientAppOptions } from '@forest/wechat-miniapp-client-app'
import {
  createWechatMiniappPhoneAuthApi,
  type WechatLoginResult
} from '@forest/user/wechat-miniapp/auth'
import { createWechatMiniappMeApi, type CurrentUser } from '@forest/user/wechat-miniapp/me'
import { appConfig } from './app.config'

const wechatAuthApi = createWechatMiniappPhoneAuthApi({
  appCode: appConfig.appCode,
  clientType: appConfig.clientType,
  accessScope: appConfig.accessScope
})

export const tradeLeadsMiniappMeApi = createWechatMiniappMeApi({
  accessScope: appConfig.accessScope
})

export const tradeLeadsMiniappDefinition: CreateWechatMiniappClientAppOptions<CurrentUser, WechatLoginResult> = {
  storagePrefix: 'forest.miniapp',
  apiBaseUrl: appConfig.apiBaseUrl,
  accessScope: appConfig.accessScope,
  loginPage: '/pages/login/index',
  defaultPage: '/pages/leads/index',
  primaryPages: [
    '/pages/leads/index',
    '/pages/unlocked/index',
    '/pages/me/index'
  ],
  loginByWechat: wechatAuthApi.loginByWechat,
  refreshAccessToken: wechatAuthApi.refreshAccessToken,
  fetchCurrentUser: tradeLeadsMiniappMeApi.fetchCurrentUser
}
