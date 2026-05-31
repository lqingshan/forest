import {
  createPointLogsPageState,
  failPointLogsPage,
  fetchPointLogs,
  resolvePointLogsPage,
  startPointLogsLoading
} from '@forest/point/wechat-miniapp/logs'
import { miniappAuth, miniappRouter } from '../../miniapp-app'

Page({
  data: createPointLogsPageState(),

  async onLoad() {
    // 积分流水用于核对充值入账和线索解锁扣减，必须登录后才能查询。
    const ready = await miniappAuth.ensureClientSession({
      redirect: '/pages/point-logs/index'
    })
    if (!ready) {
      return
    }
    await this.loadPage(0, false)
  },

  onPullDownRefresh() {
    this.loadPage(0, false).finally(() => {
      wx.stopPullDownRefresh()
    })
  },

  onReachBottom() {
    if (!this.data.loading && !this.data.loadingMore && this.data.hasMore) {
      this.loadPage(this.data.page + 1, true)
    }
  },

  async loadPage(page: number, append: boolean) {
    this.setData(startPointLogsLoading(this.data, append))

    try {
      const result = await fetchPointLogs(page, this.data.size)
      this.setData(resolvePointLogsPage(this.data, result, page, append))
    } catch (error) {
      this.setData(failPointLogsPage(error))
    }
  },

  goBack() {
    miniappRouter.goBackOr('/pages/me/index')
  }
})
