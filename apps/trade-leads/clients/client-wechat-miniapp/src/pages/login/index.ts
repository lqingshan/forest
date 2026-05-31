// user domain 提供“登录相关文案”和“错误转用户可读文案”的能力。
// 页面层可以使用这些文案，但不应该自己硬编码业务登录文案。
import { createWechatMiniappPhoneErrorHandler, createWechatMiniappPhoneLoginHandler } from '@forest/user/wechat-miniapp/auth'
import { miniappAuth, miniappRouter } from '../../miniapp-app'

const DEFAULT_REDIRECT = '/pages/leads/index'
const PRIMARY_PAGES = [
  '/pages/leads/index',
  '/pages/unlocked/index',
  '/pages/me/index'
]

// Page(...) 是微信小程序的页面注册 API。
// 这里定义的是“登录页”这个完整路由页面，而不是一个可复用子组件。
Page({
  data: {
    // submitting 控制按钮是否处于“登录中”状态，避免用户重复点击。
    submitting: false,
    // errorMessage 存当前要展示给用户的错误提示；空字符串表示当前无错误。
    errorMessage: '',
    // redirect 表示：登录成功后应该回到哪个页面。
    // 默认回线索首页；如果是从受保护页面被跳来登录，则会在 onLoad 里被路由参数覆盖。
    redirect: DEFAULT_REDIRECT
  },

  // onLoad 是页面第一次加载时的生命周期。
  // options 是路由参数对象；这里我们只关心 redirect。
  onLoad(options: { redirect?: string }) {
    // 登录页 URL 里的 redirect 往往经过 URL 编码，所以这里先 decode。
    // 如果没有传 redirect，就使用默认线索首页。
    this.setData({
      redirect: options.redirect ? decodeURIComponent(options.redirect) : DEFAULT_REDIRECT
    })
  },

  handleWechatPhoneLogin: createWechatMiniappPhoneLoginHandler({
    auth: miniappAuth,
    router: miniappRouter,
    defaultRedirect: DEFAULT_REDIRECT,
    primaryPages: PRIMARY_PAGES
  }),

  handleWechatPhoneError: createWechatMiniappPhoneErrorHandler()
})
