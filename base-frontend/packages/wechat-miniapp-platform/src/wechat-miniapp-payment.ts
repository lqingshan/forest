import { getRuntime, type MiniappRecord, type MiniappRuntimeOptions } from './runtime'

/**
 * 微信小程序支付面板适配。
 *
 * 这个模块只包装 wx.requestPayment。它不创建支付单、不确认到账、
 * 不处理充值或积分，只负责调起微信客户端原生支付面板。
 */

/**
 * wx.requestPayment 需要的字段。
 *
 * 注意：微信 API 字段名叫 package，但 package 在 JS/TS 里容易和保留字混淆，
 * 所以对外参数叫 packageValue，真正调用 wx.requestPayment 时再映射成 package。
 */
export interface WechatMiniappPaymentParams {
  timeStamp: string
  nonceStr: string
  packageValue: string
  signType: string
  paySign: string
}

/**
 * Promise 风格的 wx.requestPayment。
 *
 * 后端创建微信小程序支付预支付单后，会返回 timeStamp、nonceStr、
 * package、signType、paySign。前端把这些参数原样传给微信支付面板。
 */
export function requestWechatMiniappPayment(params: WechatMiniappPaymentParams, options: MiniappRuntimeOptions = {}) {
  return new Promise<void>((resolve, reject) => {
    getRuntime(options.runtime).wx.requestPayment({
      timeStamp: params.timeStamp,
      nonceStr: params.nonceStr,
      package: params.packageValue,
      signType: params.signType,
      paySign: params.paySign,
      success: () => resolve(),
      fail: (error: MiniappRecord) => {
        reject(new Error(error?.errMsg || '微信支付失败'))
      }
    })
  })
}
