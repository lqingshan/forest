import { apiPaths, clientHttp } from '@forest/http-client'
import { normalizePhone } from './normalizers'
import type { SendSmsCodePayload, SmsCodeSendResult, VerificationApiOptions } from './types'

const API_BASE = apiPaths.auth

export function sendSmsCode(payload: SendSmsCodePayload, options?: VerificationApiOptions) {
  return resolveHttpClient(options).post<SmsCodeSendResult>(`${API_BASE}/sms/send`, {
    phone: normalizePhone(payload.phone),
    clientType: normalizeRequiredText(payload.clientType, 'clientType'),
    appCode: normalizeRequiredText(payload.appCode, 'appCode'),
    accessScope: normalizeRequiredText(payload.accessScope, 'accessScope')
  }, {
    withAuth: false,
    retryOn401: false
  })
}

function resolveHttpClient(options?: VerificationApiOptions) {
  return options?.httpClient ?? clientHttp
}

function normalizeRequiredText(value: string, fieldName: string) {
  const normalized = value.trim()
  if (!normalized) {
    throw new Error(`${fieldName} 不能为空`)
  }
  return normalized
}
