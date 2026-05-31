import {
  createCurrentUserAvatarChangeHandler,
  createCurrentUserLogoutHandler,
  createCurrentUserPageShowHandler,
  createCurrentUserProfileLoader,
  createCurrentUserPullDownRefreshHandler
} from '@forest/user/wechat-miniapp/me'
import { cxcCommerceBuyerMiniappMeApi } from '../../app-definition'
import { miniappAuth, miniappRouter } from '../../miniapp-app'

Page({
  data: {
    user: null,
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
    fetchCurrentUser: cxcCommerceBuyerMiniappMeApi.fetchCurrentUser
  }),

  handleChangeAvatar: createCurrentUserAvatarChangeHandler({
    chooseAndUploadCurrentUserAvatar: cxcCommerceBuyerMiniappMeApi.chooseAndUploadCurrentUserAvatar
  }),

  handleLogout: createCurrentUserLogoutHandler({
    auth: miniappAuth,
    router: miniappRouter,
    loginPath: '/pages/login/index'
  })
})
