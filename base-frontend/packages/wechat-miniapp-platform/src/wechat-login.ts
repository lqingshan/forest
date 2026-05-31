import { getRuntime, type MiniappRuntimeOptions } from './runtime'

/**
 * 微信登录 code 适配。
 *
 * 这个模块只包装 wx.login，不做业务登录。
 * wx.login 返回的是短期 code，具体 app 会把 code 交给后端换业务 token。
 */

/**
 * Promise 风格的 wx.login。
 *
 * wx.login 只返回一个短期 code。这个 code 不能当登录态使用；
 * app auth/session 层会把 code 交给后端，后端再换取/绑定微信身份，
 * 最终返回业务 accessToken/refreshToken。
 */
export function requestWechatLoginCode(options: MiniappRuntimeOptions = {}) {
  return new Promise<string>((resolve, reject) => {
    getRuntime(options.runtime).wx.login({
      success: (result: { code?: string }) => {
        if (!result.code) {
          reject(new Error('获取微信登录凭证失败'))
          return
        }
        resolve(result.code)
      },
      fail: () => {
        reject(new Error('微信登录失败，请稍后重试'))
      }
    })
  })
}
