export interface LoginResult {
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

export interface LogoutResult {
  success: boolean
}

export interface PhoneSmsLoginOptions {
  appCode: string
  clientType: string
  accessScope: string
}

export interface PasswordLoginOptions {
  appCode: string
  clientType: string
  accessScope: string
}

export interface CarrierOneClickLoginOptions {
  appCode: string
  clientType: string
  accessScope: string
  provider?: string
}
