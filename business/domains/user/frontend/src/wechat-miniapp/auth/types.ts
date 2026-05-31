export interface WechatLoginContext {
  phoneCode?: string
}

export interface WechatMiniappAuthApiOptions {
  appCode: string
  clientType?: string
  accessScope?: string
}

export interface WechatMiniappAuthApi {
  loginByWechat(code: string, context?: WechatLoginContext): Promise<WechatLoginResult>
  refreshAccessToken(refreshToken: string): Promise<RefreshTokenResult>
}

export interface WechatLoginResult {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
  refreshExpiresIn: number
  clientType: string
  appCode: string
  accessScope: string
  firstLogin: boolean
}

export interface RefreshTokenResult {
  accessToken: string
  tokenType: string
  expiresIn: number
  clientType: string
  appCode: string
  accessScope: string
}
