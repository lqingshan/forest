export interface NativeCarrierLoginTokenResult {
  carrierToken: string
  provider?: string
}

export interface NativeCarrierAuthBridge {
  requestCarrierLoginToken(): Promise<NativeCarrierLoginTokenResult | string>
}

declare global {
  interface Window {
    ForestNative?: {
      auth?: Partial<NativeCarrierAuthBridge>
    }
  }
}

let configuredBridge: NativeCarrierAuthBridge | null = null

export function configureNativeCarrierAuthBridge(bridge: NativeCarrierAuthBridge | null) {
  configuredBridge = bridge
}

export async function requestCarrierLoginToken(bridge: NativeCarrierAuthBridge | null = configuredBridge) {
  const runtimeBridge = bridge ?? window.ForestNative?.auth
  if (!runtimeBridge?.requestCarrierLoginToken) {
    throw new Error('当前 APP 不支持本机号一键登录')
  }
  const result = await runtimeBridge.requestCarrierLoginToken()
  if (typeof result === 'string') {
    return normalizeCarrierTokenResult({ carrierToken: result })
  }
  return normalizeCarrierTokenResult(result)
}

function normalizeCarrierTokenResult(result: NativeCarrierLoginTokenResult) {
  const carrierToken = result.carrierToken?.trim()
  if (!carrierToken) {
    throw new Error('本机号一键登录未返回 carrierToken')
  }
  return {
    carrierToken,
    provider: result.provider?.trim() || undefined
  }
}
