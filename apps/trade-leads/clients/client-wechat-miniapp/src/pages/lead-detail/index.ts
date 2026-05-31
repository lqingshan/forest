import {
  assignLeadDetailId,
  createLeadDetailPageState,
  failLeadDetailLoading,
  failLeadUnlock,
  fetchUserLeadDetail,
  finishLeadUnlocking,
  isInsufficientPointsMessage,
  LEAD_DETAIL_COPY,
  resolveLeadDetail,
  startLeadDetailLoading,
  startLeadUnlocking,
  unlockUserLead
} from '@forest/user-lead/wechat-miniapp/detail'
import { appConfig } from '../../app.config'
import { miniappAuth, miniappRouter } from '../../miniapp-app'

Page({
  data: createLeadDetailPageState(appConfig.unlockCost),

  async onLoad(options: { id?: string; resumeUnlock?: string }) {
    const leadId = Number(options.id || 0)
    // redirect 保留 resumeUnlock，用户登录回来后可以继续刚才的“去解锁”动作。
    const redirect = miniappRouter.appendQuery('/pages/lead-detail/index', {
      id: leadId,
      resumeUnlock: options.resumeUnlock || ''
    })

    const ready = await miniappAuth.ensureClientSession({ redirect })
    if (!ready || !leadId) {
      return
    }

    this.setData(assignLeadDetailId(leadId))
    await this.loadDetail()

    // 登录后恢复动作只在仍未解锁时触发，避免重复扣积分。
    if (String(options.resumeUnlock || '') === '1' && this.data.lead && !this.data.lead.unlocked) {
      await this.handleUnlock()
    }
  },

  async loadDetail() {
    this.setData(startLeadDetailLoading())

    try {
      const lead = await fetchUserLeadDetail(this.data.leadId)
      this.setData(resolveLeadDetail(lead))
    } catch (error) {
      this.setData(failLeadDetailLoading(error))
    }
  },

  async handleUnlock() {
    if (this.data.unlocking || !this.data.leadId) {
      return
    }

    this.setData(startLeadUnlocking())

    try {
      // 后端聚合层会在一个事务里完成扣积分和创建解锁记录。
      await unlockUserLead(this.data.leadId)
      wx.showToast({
        title: LEAD_DETAIL_COPY.unlockSuccessText,
        icon: 'success'
      })
      await this.loadDetail()
    } catch (error) {
      const nextState = failLeadUnlock(error)
      const message = nextState.errorMessage
      this.setData(nextState)

      if (isInsufficientPointsMessage(message)) {
        // 积分不足是最常见的可恢复失败，直接引导到充值页形成闭环。
        wx.showModal({
          title: LEAD_DETAIL_COPY.insufficientPointsDialog.title,
          content: LEAD_DETAIL_COPY.insufficientPointsDialog.content,
          confirmText: LEAD_DETAIL_COPY.insufficientPointsDialog.confirmText,
          success: (result: { confirm: boolean }) => {
            if (!result.confirm) {
              return
            }
            wx.navigateTo({
              url: '/pages/recharge/index'
            })
          }
        })
      }
    }
    this.setData(finishLeadUnlocking())
  },

  copyField(event: { detail?: { value?: string }; currentTarget?: { dataset?: { value?: string } } }) {
    const value = event.detail?.value || event.currentTarget?.dataset?.value
    if (!value) {
      return
    }
    wx.setClipboardData({
      data: value
    })
  },

  goBack() {
    miniappRouter.goBackOr('/pages/leads/index')
  }
})
