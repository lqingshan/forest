import type { HttpClient } from '@forest/http-client'

export type SmsScene = 'LOGIN'

export interface SendSmsCodePayload {
  phone: string
  clientType: string
  appCode: string
  accessScope: string
}

export interface SmsCodeSendResult {
  phone: string
  ttlMinutes?: number
  ttlSeconds?: number
}

export interface VerificationApiOptions {
  httpClient?: HttpClient
}
