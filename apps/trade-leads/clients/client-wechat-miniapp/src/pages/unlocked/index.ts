import {
  createUnlockedLeadListState,
  failUnlockedLeadList,
  fetchUnlockedUserLeadList,
  resolveUnlockedLeadList,
  startUnlockedLeadListLoading
} from '@forest/user-lead/wechat-miniapp/unlocked'
import { miniappAuth, miniappRouter } from '../../miniapp-app'

Page({
  data: createUnlockedLeadListState(),

  async onShow() {
    // “我的已解锁线索”只展示历史解锁结果，不承担再次解锁或扣积分职责。
    const ready = await miniappAuth.ensureClientSession({
      redirect: '/pages/unlocked/index'
    })
    if (!ready) {
      return
    }
    await this.loadFirstPage()
  },

  onPullDownRefresh() {
    this.loadFirstPage().finally(() => {
      wx.stopPullDownRefresh()
    })
  },

  onReachBottom() {
    if (!this.data.loading && !this.data.loadingMore && this.data.hasMore) {
      this.loadPage(this.data.page + 1, true)
    }
  },

  async loadFirstPage() {
    return this.loadPage(0, false)
  },

  async loadPage(page: number, append: boolean) {
    this.setData(startUnlockedLeadListLoading(this.data, append))

    try {
      const result = await fetchUnlockedUserLeadList(page, this.data.size)
      this.setData(resolveUnlockedLeadList(this.data, result, page, append))
    } catch (error) {
      this.setData(failUnlockedLeadList(error))
    }
  },

  openDetail(event: { detail?: { id?: number }; currentTarget?: { dataset?: { id?: number } } }) {
    const leadId = event.detail?.id || event.currentTarget?.dataset?.id
    if (!leadId) {
      return
    }

    wx.navigateTo({
      url: miniappRouter.appendQuery('/pages/lead-detail/index', { id: leadId })
    })
  }
})
