import {
  buildUserLeadListQuery,
  createUserLeadListPageState,
  failUserLeadListPage,
  fetchUserLeadList,
  resolveUserLeadListPage,
  startUserLeadListLoading,
  updateUserLeadListKeyword
} from '@forest/user-lead/wechat-miniapp/list'
import { miniappAuth, miniappRouter } from '../../miniapp-app'

Page({
  data: createUserLeadListPageState(),

  async onShow() {
    // 线索列表是一级入口，展示前先确认会话，避免未登录用户看到可解锁动作。
    const ready = await miniappAuth.ensureClientSession({
      redirect: '/pages/leads/index'
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

  handleKeywordInput(event: { detail: { keyword?: string; value?: string } }) {
    this.setData(updateUserLeadListKeyword(this.data, event.detail.keyword || event.detail.value || ''))
  },

  async handleSearch() {
    await this.loadFirstPage()
  },

  async loadFirstPage() {
    return this.loadPage(0, false)
  },

  async loadPage(page: number, append: boolean) {
    this.setData(startUserLeadListLoading(this.data, append))

    try {
      const result = await fetchUserLeadList({
        ...buildUserLeadListQuery({
          page,
          size: this.data.size,
          keyword: this.data.keyword
        })
      })

      this.setData(resolveUserLeadListPage(this.data, result, page, append))
    } catch (error) {
      this.setData(failUserLeadListPage(error))
    }
  },

  openDetail(event: { detail?: { id?: number }; currentTarget?: { dataset?: { id?: number } } }) {
    const leadId = event.detail?.id || event.currentTarget?.dataset?.id
    if (!leadId) {
      return
    }

    // 详情页会继续做登录门禁，并在登录返回时支持 resumeUnlock。
    wx.navigateTo({
      url: miniappRouter.appendQuery('/pages/lead-detail/index', { id: leadId })
    })
  }
})
