import {
  getWechatLoginErrorMessage,
  getWechatPhoneLoginErrorMessage,
  WECHAT_LOGIN_COPY
} from '@forest/user/wechat-miniapp/auth'
import { miniappAuth, miniappRouter } from '../../miniapp-app'

Page({
  data: {
    submitting: false,
    phoneSubmitting: false,
    errorMessage: '',
    phoneErrorMessage: '',
    redirect: '/pages/me/index'
  },

  onLoad(options: { redirect?: string }) {
    this.setData({
      redirect: options.redirect ? decodeURIComponent(options.redirect) : '/pages/me/index'
    })
  },

  async handleWechatLogin() {
    await this.loginWithWechat({})
  },

  async handleWechatPhoneLogin(event: MiniappRecord) {
    const phoneCode = event.detail && typeof event.detail.phoneCode === 'string'
      ? event.detail.phoneCode.trim()
      : ''
    if (!phoneCode) {
      this.setData({
        phoneErrorMessage: getWechatPhoneLoginErrorMessage(new Error('需要授权手机号后才能登录'))
      })
      return
    }

    await this.loginWithWechat({ phoneCode }, true)
  },

  handleWechatPhoneError(event: MiniappRecord) {
    const message = event.detail && typeof event.detail.message === 'string'
      ? event.detail.message
      : '需要授权手机号后才能登录'
    this.setData({
      phoneErrorMessage: message
    })
  },

  async loginWithWechat(context: Record<string, unknown>, phoneLogin = false) {
    if (this.data.submitting || this.data.phoneSubmitting) {
      return
    }

    this.setData({
      submitting: !phoneLogin,
      phoneSubmitting: phoneLogin,
      errorMessage: '',
      phoneErrorMessage: ''
    })

    wx.showLoading({
      title: WECHAT_LOGIN_COPY.loadingText
    })

    let loadingVisible = true
    try {
      await miniappAuth.loginWithWechat(context)
      wx.hideLoading()
      loadingVisible = false
      wx.showToast({
        title: WECHAT_LOGIN_COPY.successText,
        icon: 'success'
      })
      this.openRedirect()
    } catch (error) {
      this.setData(phoneLogin
        ? { phoneErrorMessage: getWechatPhoneLoginErrorMessage(error) }
        : { errorMessage: getWechatLoginErrorMessage(error) })
    } finally {
      if (loadingVisible) {
        wx.hideLoading()
      }
      this.setData({
        submitting: false,
        phoneSubmitting: false
      })
    }
  },

  openRedirect() {
    const redirect = this.data.redirect || '/pages/me/index'
    if (redirect.startsWith('/pages/me/index')) {
      miniappRouter.openPrimaryPage(redirect)
      return
    }
    miniappRouter.replacePage(redirect)
  }
})
