import type { CreateWechatMiniappClientAppOptions } from '@forest/wechat-miniapp-client-app'
import {
  createWechatMiniappAuthApi,
  createWechatMiniappPhoneAuthApi,
  type WechatLoginResult
} from '@forest/user/wechat-miniapp/auth'
import { createWechatMiniappMeApi, type CurrentUser } from '@forest/user/wechat-miniapp/me'
import { appConfig } from './app.config'

const directWechatAuthApi = createWechatMiniappAuthApi({
  appCode: appConfig.appCode,
  clientType: appConfig.clientType,
  accessScope: appConfig.accessScope
})

const phoneWechatAuthApi = createWechatMiniappPhoneAuthApi({
  appCode: appConfig.appCode,
  clientType: appConfig.clientType,
  accessScope: appConfig.accessScope
})

export const cxcCommerceBuyerMiniappMeApi = createWechatMiniappMeApi({
  accessScope: appConfig.accessScope
})

function loginByWechat(code: string, context?: Record<string, unknown>) {
  const phoneCode = typeof context?.phoneCode === 'string' ? context.phoneCode.trim() : ''
  if (phoneCode) {
    return phoneWechatAuthApi.loginByWechat(code, {
      phoneCode
    })
  }
  return directWechatAuthApi.loginByWechat(code)
}

export const cxcCommerceBuyerMiniappDefinition: CreateWechatMiniappClientAppOptions<CurrentUser, WechatLoginResult> = {
  storagePrefix: 'forest.cxc-commerce.buyer-miniapp',
  apiBaseUrl: appConfig.apiBaseUrl,
  accessScope: appConfig.accessScope,
  loginPage: '/pages/login/index',
  defaultPage: '/pages/me/index',
  primaryPages: [
    '/pages/me/index'
  ],
  loginByWechat,
  refreshAccessToken: directWechatAuthApi.refreshAccessToken,
  fetchCurrentUser: cxcCommerceBuyerMiniappMeApi.fetchCurrentUser
}
