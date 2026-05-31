import { createPaymentOrder, notifyMockPayment } from '@forest/payment/wechat-miniapp/payment'
import { fetchPointBalance } from '@forest/point/wechat-miniapp/balance'
import {
  createRechargePageState,
  createRechargeOrder,
  failRechargePageLoading,
  failRechargePayment,
  fetchRechargePackages,
  RECHARGE_COPY,
  finishRechargePayment,
  resolveRechargePage,
  selectRechargePackage as selectRechargePackageState,
  startRechargePageLoading,
  startRechargePayment
} from '@forest/recharge/wechat-miniapp/recharge'
import { appConfig } from '../../app.config'
import { miniappAuth, miniappPayment, miniappRouter } from '../../miniapp-app'
import { toLocalDateTimeString } from '../../utils/format'

Page({
  data: createRechargePageState(),

  async onLoad() {
    // 充值页必须登录，因为创建充值单和支付单都绑定当前用户。
    const ready = await miniappAuth.ensureClientSession({
      redirect: '/pages/recharge/index'
    })
    if (!ready) {
      return
    }
    await this.loadPage()
  },

  async loadPage() {
    this.setData(startRechargePageLoading())

    try {
      // 套餐和余额并行加载，页面同时展示“买多少”和“当前有多少”。
      const [balance, packages] = await Promise.all([
        fetchPointBalance(),
        fetchRechargePackages()
      ])
      this.setData(resolveRechargePage(this.data, balance, packages))
    } catch (error) {
      this.setData(failRechargePageLoading(error))
    }
  },

  selectPackage(event: { detail?: { code?: string }; currentTarget?: { dataset?: { code?: string } } }) {
    const code = event.detail?.code || event.currentTarget?.dataset?.code || ''
    if (!code) {
      return
    }

    this.setData(selectRechargePackageState(code))
  },

  async handlePay() {
    if (this.data.paying || !this.data.selectedPackageCode) {
      return
    }

    this.setData(startRechargePayment())

    try {
      // 先创建充值主单，再为这笔业务单创建一次微信支付尝试。
      const rechargeOrder = await createRechargeOrder(this.data.selectedPackageCode)
      const paymentOrder = await createPaymentOrder('RECHARGE', rechargeOrder.id)

      if (!paymentOrder.paymentParams) {
        throw new Error(RECHARGE_COPY.paymentParamsFailedText)
      }

      if (paymentOrder.paymentParams.paySign === appConfig.mockPaySignature) {
        // 本地 mock 模式没有微信服务器回调，前端主动打同一个回调入口来验证后端到账链路。
        await notifyMockPayment({
          outTradeNo: paymentOrder.outTradeNo,
          transactionId: `mock-miniapp-${Date.now()}`,
          amountCents: paymentOrder.amountCents,
          paidTime: toLocalDateTimeString(new Date())
        })
        wx.redirectTo({
          url: miniappRouter.appendQuery('/pages/payment-result/index', {
            rechargeOrderId: rechargeOrder.id,
            paymentOrderId: paymentOrder.id,
            result: 'success'
          })
        })
        return
      }

      await miniappPayment.requestWechatMiniappPayment(paymentOrder.paymentParams)

      wx.redirectTo({
        url: miniappRouter.appendQuery('/pages/payment-result/index', {
          rechargeOrderId: rechargeOrder.id,
          paymentOrderId: paymentOrder.id,
          result: 'processing'
        })
      })
    } catch (error) {
      const nextState = failRechargePayment(error)
      const message = nextState.errorMessage
      if (message.includes('cancel')) {
        wx.redirectTo({
          url: '/pages/payment-result/index?result=cancelled'
        })
      } else {
        this.setData(nextState)
      }
    } finally {
      this.setData(finishRechargePayment())
    }
  },

  goBack() {
    miniappRouter.goBackOr('/pages/me/index')
  }
})
