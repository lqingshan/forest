declare const process: {
  env: {
    MINIAPP_API_BASE_URL?: string
    MINIAPP_APP_CODE?: string
  }
}

const DEFAULT_API_BASE_URL = 'http://localhost:8082'
const DEFAULT_APP_CODE = 'cxc-commerce-buyer-wechat-miniapp'

export const appConfig = {
  apiBaseUrl: process.env.MINIAPP_API_BASE_URL || DEFAULT_API_BASE_URL,
  appCode: process.env.MINIAPP_APP_CODE || DEFAULT_APP_CODE,
  clientType: 'WECHAT_MINIAPP',
  accessScope: 'CLIENT'
}
