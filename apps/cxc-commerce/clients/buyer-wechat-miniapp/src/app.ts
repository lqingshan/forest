import { miniappLifecycle } from './miniapp-app'

App({
  globalData: {
    currentUser: null
  },

  onLaunch() {
    miniappLifecycle.configureClientSession()
    miniappLifecycle.hydrateClientSession()
    miniappLifecycle.restoreClientSession().catch(() => {
      // 受保护页面会在进入时自行处理登录门禁。
    })
  }
})
