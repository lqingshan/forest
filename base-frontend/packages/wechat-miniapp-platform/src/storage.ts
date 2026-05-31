import { getRuntime, type MiniappRuntimeOptions } from './runtime'

/**
 * 微信小程序 storage 适配。
 *
 * 这个模块只封装 wx storage：
 * - 强制 storagePrefix，避免 accessToken/currentUser 等通用 key 冲突
 * - 统一空值语义：空字符串/null/undefined 表示删除 key
 * - 让 session store 不直接依赖 wx.getStorageSync/setStorageSync
 */

/**
 * 对 wx storage 的一层小封装。
 *
 * 为什么不让业务直接调用 wx.getStorageSync？
 * - 可以统一加 storagePrefix，避免多个小程序/多个环境 key 冲突。
 * - 可以在测试里替换成内存 storage。
 * - session store 不需要知道 wx API 的具体细节。
 */
export interface MiniappStorage {
  getString(key: string): string
  setString(key: string, value: string): void
  getJson<T>(key: string): T | null
  setJson(key: string, value: unknown): void
}

/**
 * storagePrefix 必填。
 *
 * 例子：
 * - forest.trade-leads
 * - forest.another-miniapp
 *
 * 最终 key 会变成：
 * - forest.trade-leads.accessToken
 * - forest.trade-leads.currentUser
 */
export interface CreateMiniappStorageOptions extends MiniappRuntimeOptions {
  storagePrefix: string
}

/**
 * 创建带命名空间的 storage。
 *
 * 微信小程序 storage 是全局 key-value，如果多个小程序、多个业务环境、
 * 或未来多个账号共用同一套 key，很容易互相污染。
 *
 * 因此这里强制要求 storagePrefix，把 accessToken 之类的 key 变成：
 * `${storagePrefix}.accessToken`
 */
export function createMiniappStorage(options: CreateMiniappStorageOptions): MiniappStorage {
  assertStoragePrefix(options.storagePrefix)

  return {
    getString(key: string) {
      // wx.getStorageSync 取不到时通常返回空字符串；这里也统一对外返回字符串。
      const value = getRuntime(options.runtime).wx.getStorageSync(toStorageKey(options.storagePrefix, key))
      return value || ''
    },
    setString(key: string, value: string) {
      const storageKey = toStorageKey(options.storagePrefix, key)
      const wxApi = getRuntime(options.runtime).wx
      if (!value) {
        // 空字符串表示清理 token，直接删除 storage key，避免保留无意义值。
        wxApi.removeStorageSync(storageKey)
        return
      }
      wxApi.setStorageSync(storageKey, value)
    },
    getJson<T>(key: string) {
      // wx storage 可以直接存对象；这里不做 JSON.parse/stringify。
      const value = getRuntime(options.runtime).wx.getStorageSync(toStorageKey(options.storagePrefix, key))
      return (value || null) as T | null
    },
    setJson(key: string, value: unknown) {
      const storageKey = toStorageKey(options.storagePrefix, key)
      const wxApi = getRuntime(options.runtime).wx
      if (value == null) {
        // null/undefined 表示清理对象型缓存，例如 currentUser。
        wxApi.removeStorageSync(storageKey)
        return
      }
      wxApi.setStorageSync(storageKey, value)
    }
  }
}

/**
 * 统一生成带命名空间的 storage key。
 */
function toStorageKey(storagePrefix: string, key: string) {
  return `${storagePrefix}.${key}`
}

/**
 * storagePrefix 是强约束，不允许空。
 * 否则 accessToken/currentUser 这种通用 key 很容易和其它小程序或测试环境冲突。
 */
function assertStoragePrefix(storagePrefix: string) {
  if (!storagePrefix) {
    throw new Error('storagePrefix is required')
  }
}
