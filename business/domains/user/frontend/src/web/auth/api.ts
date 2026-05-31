import { apiPaths, clientHttp, type HttpClient } from '@forest/http-client'
import type {
  CarrierOneClickLoginOptions,
  LoginResult,
  LogoutResult,
  PasswordLoginOptions,
  PhoneSmsLoginOptions,
  RefreshTokenResult
} from './types'

const API_BASE = apiPaths.auth
export function loginByPhone(
  phone: string,
  smsCode: string,
  options: PhoneSmsLoginOptions,
  httpClient: HttpClient = clientHttp
) {
  return httpClient.post<LoginResult>(`${API_BASE}/phone/login`, {
    phone: phone.trim(),
    smsCode: smsCode.trim(),
    clientType: normalizeRequiredText(options.clientType, 'clientType'),
    appCode: normalizeRequiredText(options.appCode, 'appCode'),
    accessScope: normalizeRequiredText(options.accessScope, 'accessScope')
  }, {
    withAuth: false,
    retryOn401: false
  })
}

export function loginByPassword(
  phone: string,
  password: string,
  options: PasswordLoginOptions,
  httpClient: HttpClient = clientHttp
) {
  return httpClient.post<LoginResult>(`${API_BASE}/password/login`, {
    phone: phone.trim(),
    password,
    clientType: normalizeRequiredText(options.clientType, 'clientType'),
    appCode: normalizeRequiredText(options.appCode, 'appCode'),
    accessScope: normalizeRequiredText(options.accessScope, 'accessScope')
  }, {
    withAuth: false,
    retryOn401: false
  })
}

export function loginByCarrierToken(
  carrierToken: string,
  options: CarrierOneClickLoginOptions,
  httpClient: HttpClient = clientHttp
) {
  return httpClient.post<LoginResult>(`${API_BASE}/carrier/one-click-login`, {
    carrierToken: carrierToken.trim(),
    provider: options.provider,
    clientType: normalizeRequiredText(options.clientType, 'clientType'),
    appCode: normalizeRequiredText(options.appCode, 'appCode'),
    accessScope: normalizeRequiredText(options.accessScope, 'accessScope')
  }, {
    withAuth: false,
    retryOn401: false
  })
}

export function refreshAccessToken(refreshToken: string, httpClient: HttpClient = clientHttp) {
  return httpClient.post<RefreshTokenResult>(`${API_BASE}/refresh`, { refreshToken }, {
    withAuth: false,
    retryOn401: false
  })
}

export function logout(httpClient: HttpClient = clientHttp) {
  return httpClient.post<LogoutResult>(`${API_BASE}/logout`)
}

function normalizeRequiredText(value: string, fieldName: string) {
  const normalizedValue = value.trim()
  if (!normalizedValue) {
    throw new Error(`用户端认证 ${fieldName} 未配置`)
  }
  return normalizedValue
}
