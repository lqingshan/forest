import { fetchPointBalance } from '@forest/point/wechat-miniapp/balance'
import {
  createCurrentUserAvatarChangeHandler,
  createCurrentUserLogoutHandler,
  createCurrentUserPageShowHandler,
  createCurrentUserProfileLoader,
  createCurrentUserPullDownRefreshHandler
} from '@forest/user/wechat-miniapp/me'
import { tradeLeadsMiniappMeApi } from '../../app-definition'
import { miniappAuth, miniappRouter } from '../../miniapp-app'

Page({
  data: {
    user: null,
    balance: null,
    avatarUploading: false,
    loading: false,
    errorMessage: ''
  },

  onShow: createCurrentUserPageShowHandler({
    auth: miniappAuth,
    redirect: '/pages/me/index'
  }),

  onPullDownRefresh: createCurrentUserPullDownRefreshHandler(),

  loadProfile: createCurrentUserProfileLoader({
    auth: miniappAuth,
    fetchCurrentUser: tradeLeadsMiniappMeApi.fetchCurrentUser,
    loadExtras: async () => ({
      balance: await fetchPointBalance()
    })
  }),

  openRecharge() {
    wx.navigateTo({
      url: '/pages/recharge/index'
    })
  },

  openPointLogs() {
    wx.navigateTo({
      url: '/pages/point-logs/index'
    })
  },

  handleChangeAvatar: createCurrentUserAvatarChangeHandler({
    chooseAndUploadCurrentUserAvatar: tradeLeadsMiniappMeApi.chooseAndUploadCurrentUserAvatar
  }),

  handleLogout: createCurrentUserLogoutHandler({
    auth: miniappAuth,
    router: miniappRouter,
    loginPath: '/pages/login/index',
    resetData: {
      balance: null
    }
  })
})
