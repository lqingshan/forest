declare const process: {
  env: {
    MINIAPP_API_BASE_URL?: string
    MINIAPP_APP_CODE?: string
  }
}

const DEFAULT_API_BASE_URL = 'https://localleads.haitunai.cn'
const DEFAULT_APP_CODE = 'trade-leads-miniapp'

export const appConfig = {
  // 小程序所有 API 在构建时注入；默认连本地 gateway，体验版/prod 显式注入生产域名。
  apiBaseUrl: process.env.MINIAPP_API_BASE_URL || DEFAULT_API_BASE_URL,
  appCode: process.env.MINIAPP_APP_CODE || DEFAULT_APP_CODE,
  clientType: 'WECHAT_MINIAPP',
  accessScope: 'CLIENT',
  unlockCost: 5,
  // 后端 mock 支付会返回这个签名，页面据此绕过 wx.requestPayment 并直接触发 mock 回调。
  mockPaySignature: 'mock-pay-sign'
}
