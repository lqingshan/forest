import { apiPathForAccessScope, httpForAccessScope, type HttpClient } from '@forest/http-client'
import {
  chooseWechatMiniappFile,
  uploadWechatMiniappFile,
  type WechatMiniappUploadOptions
} from '@forest/file/wechat-miniapp/upload'
import type { CurrentUser } from './types'

const DEFAULT_WECHAT_MINIAPP_ACCESS_SCOPE = 'CLIENT'

export interface WechatMiniappMeApiOptions {
  accessScope?: string
}

export interface WechatMiniappMeApi {
  fetchCurrentUser(): Promise<CurrentUser>
  updateCurrentUserAvatar(fileNo: string): Promise<CurrentUser>
  chooseAndUploadCurrentUserAvatar(options?: WechatMiniappUploadOptions): Promise<CurrentUser>
}

export function createWechatMiniappMeApi(options: WechatMiniappMeApiOptions = {}): WechatMiniappMeApi {
  const accessScope = options.accessScope || DEFAULT_WECHAT_MINIAPP_ACCESS_SCOPE
  const httpClient = httpForAccessScope(accessScope)
  const apiBase = `${apiPathForAccessScope(accessScope)}/user/me`

  return {
    fetchCurrentUser() {
      return fetchCurrentUserWith(httpClient, apiBase)
    },
    updateCurrentUserAvatar(fileNo: string) {
      return updateCurrentUserAvatarWith(httpClient, apiBase, fileNo)
    },
    async chooseAndUploadCurrentUserAvatar(options: WechatMiniappUploadOptions = {}) {
      const input = await chooseWechatMiniappFile('IMAGE')
      const file = await uploadWechatMiniappFile(input, {
        ...options,
        httpClient,
        scope: fileScope(accessScope)
      })
      return updateCurrentUserAvatarWith(httpClient, apiBase, file.fileNo)
    }
  }
}

const defaultMeApi = createWechatMiniappMeApi()

// /me 是小程序恢复会话的服务端校验点；默认保持 CLIENT，app 可通过 createWechatMiniappMeApi 显式选择访问面。
export function fetchCurrentUser() {
  return defaultMeApi.fetchCurrentUser()
}

export function updateCurrentUserAvatar(fileNo: string) {
  return defaultMeApi.updateCurrentUserAvatar(fileNo)
}

export async function chooseAndUploadCurrentUserAvatar(options: WechatMiniappUploadOptions = {}) {
  return defaultMeApi.chooseAndUploadCurrentUserAvatar(options)
}

function fetchCurrentUserWith(httpClient: HttpClient, apiBase: string) {
  return httpClient.get<CurrentUser>(apiBase)
}

function updateCurrentUserAvatarWith(httpClient: HttpClient, apiBase: string, fileNo: string) {
  return httpClient.post<CurrentUser>(`${apiBase}/avatar`, { fileNo })
}

function fileScope(accessScope: string): 'client' | 'admin' | 'platform' {
  const normalizedScope = accessScope.trim().toUpperCase()
  if (normalizedScope === 'ADMIN') {
    return 'admin'
  }
  if (normalizedScope === 'PLATFORM') {
    return 'platform'
  }
  return 'client'
}
