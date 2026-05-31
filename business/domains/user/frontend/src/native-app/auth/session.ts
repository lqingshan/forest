import { requestCarrierLoginToken, type NativeCarrierAuthBridge } from './bridge'
import type { CarrierOneClickLoginOptions, WebUserSession } from '../../web/auth'

export async function loginWithNativeCarrier(
  session: WebUserSession,
  options: Pick<CarrierOneClickLoginOptions, 'provider'> = {},
  bridge?: NativeCarrierAuthBridge
) {
  const tokenResult = await requestCarrierLoginToken(bridge ?? null)
  const provider = options.provider ?? tokenResult.provider
  return session.loginWithCarrierToken(
    tokenResult.carrierToken,
    provider
      ? {
          appCode: session.appCode,
          clientType: session.clientType,
          accessScope: session.accessScope,
          provider
        }
      : undefined
  )
}
