import { miniappLifecycle } from './miniapp-app'

App({
  globalData: {
    currentUser: null
  },

  onLaunch() {
    miniappLifecycle.configureClientSession()
    // 启动时只恢复本地会话，不主动打断页面；具体页面再用登录门禁决定是否跳转。
    miniappLifecycle.hydrateClientSession()

    miniappLifecycle.restoreClientSession().catch(() => {
      // 首次启动不打断页面流，受保护页面会自己做登录门禁。
    })
  }
})
