import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import {
  createWechatMiniappPhoneErrorHandler,
  createWechatMiniappPhoneLoginHandler,
  type WechatMiniappPhoneLoginPageContext
} from './wechat-phone-login-flow'

describe('wechat miniapp phone login flow', () => {
  beforeEach(() => {
    vi.stubGlobal('wx', {
      showLoading: vi.fn(),
      hideLoading: vi.fn(),
      showToast: vi.fn()
    })
  })

  afterEach(() => {
    vi.restoreAllMocks()
    vi.unstubAllGlobals()
  })

  it('logs in with phone code and opens primary redirect', async () => {
    const auth = {
      loginWithWechat: vi.fn().mockResolvedValue({})
    }
    const router = {
      openPrimaryPage: vi.fn(),
      replacePage: vi.fn()
    }
    const context = createPageContext('/pages/leads/index?keyword=a')
    const handler = createWechatMiniappPhoneLoginHandler({
      auth,
      router,
      defaultRedirect: '/pages/leads/index',
      primaryPages: ['/pages/leads/index', '/pages/me/index']
    })

    await handler.call(context, {
      detail: {
        phoneCode: ' phone-code '
      }
    })

    expect(auth.loginWithWechat).toHaveBeenCalledWith({ phoneCode: 'phone-code' })
    expect(router.openPrimaryPage).toHaveBeenCalledWith('/pages/leads/index?keyword=a')
    expect(router.replacePage).not.toHaveBeenCalled()
    expect(context.data.submitting).toBe(false)
    expect(context.data.errorMessage).toBe('')
  })

  it('sets authorization error when phone code is missing', async () => {
    const auth = {
      loginWithWechat: vi.fn()
    }
    const router = {
      openPrimaryPage: vi.fn(),
      replacePage: vi.fn()
    }
    const context = createPageContext('/pages/leads/index')
    const handler = createWechatMiniappPhoneLoginHandler({
      auth,
      router,
      defaultRedirect: '/pages/leads/index',
      primaryPages: ['/pages/leads/index']
    })

    await handler.call(context, {
      detail: {}
    })

    expect(auth.loginWithWechat).not.toHaveBeenCalled()
    expect(context.data.errorMessage).toBe('需要授权手机号后才能登录')
  })

  it('uses phone error event message', () => {
    const context = createPageContext('/pages/leads/index')
    const handler = createWechatMiniappPhoneErrorHandler()

    handler.call(context, {
      detail: {
        message: '用户取消授权'
      }
    })

    expect(context.data.errorMessage).toBe('用户取消授权')
  })
})

function createPageContext(redirect: string): WechatMiniappPhoneLoginPageContext & {
  data: {
    submitting: boolean
    redirect: string
    errorMessage: string
  }
} {
  return {
    data: {
      submitting: false,
      redirect,
      errorMessage: ''
    },
    setData(patch) {
      this.data = {
        ...this.data,
        ...patch
      }
    }
  }
}
