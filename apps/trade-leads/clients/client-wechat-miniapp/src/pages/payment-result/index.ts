import {
  assignRechargeResultOrderId,
  createRechargeResultPageState,
  resolveRechargePaymentResult,
  resolveRechargeResultState,
  startRechargeResultLoading,
  type RechargeResultStatus
} from '@forest/recharge/wechat-miniapp/recharge'
import { miniappAuth, miniappRouter } from '../../miniapp-app'

Page({
  data: createRechargeResultPageState(),

  async onLoad(options: { rechargeOrderId?: string; result?: string }) {
    const rechargeOrderId = Number(options.rechargeOrderId || 0)
    // 结果页也受登录保护，避免支付后冷启动时无法查询充值单状态。
    const redirect = options.rechargeOrderId
      ? `/pages/payment-result/index?rechargeOrderId=${rechargeOrderId}&result=${options.result || ''}`
      : '/pages/payment-result/index'

    const ready = await miniappAuth.ensureClientSession({ redirect })
    if (!ready) {
      return
    }

    this.setData(assignRechargeResultOrderId(rechargeOrderId))

    await this.resolveResult((options.result || 'processing') as RechargeResultStatus)
  },

  async resolveResult(initialStatus: RechargeResultStatus) {
    this.setData(startRechargeResultLoading())

    const result = await resolveRechargePaymentResult({
      rechargeOrderId: this.data.rechargeOrderId,
      initialStatus
    })

    this.setData(resolveRechargeResultState(result.status, result.order))
  },

  async refreshStatus() {
    await this.resolveResult('processing')
  },

  goMe() {
    this.openPaymentResultTarget('/pages/me/index')
  },

  goLeads() {
    this.openPaymentResultTarget('/pages/leads/index')
  },

  goRechargeAgain() {
    this.openPaymentResultTarget('/pages/recharge/index')
  },

  openPaymentResultTarget(url: string) {
    // 支付结果页是支付流程终点，跳走时清理旧页面栈，避免返回到旧充值/支付上下文。
    miniappRouter.reLaunchPage(url, {
      fail(error: unknown) {
        console.error('[payment-result] navigate failed', { url, error })
        wx.showToast({
          title: '跳转失败，请重试',
          icon: 'none'
        })
      }
    })
  }
})
