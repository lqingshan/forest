export const WECHAT_LOGIN_COPY = {
  title: '一键完成微信登录',
  description: '使用微信身份完成登录，不会在小程序侧存储你的微信密码。',
  steps: [
    '调用微信登录凭证',
    '创建登录会话',
    '本机保存登录状态'
  ],
  buttonText: '微信登录',
  loadingText: '登录中',
  successText: '登录成功',
  failedText: '登录失败，请稍后重试'
}

export const WECHAT_PHONE_LOGIN_COPY = {
  title: '微信绑定手机号登录',
  description: '授权手机号后完成登录，后续可识别为同一个用户。',
  buttonText: '微信手机号登录',
  authorizationRequiredText: '需要授权手机号后才能登录',
  failedText: '获取手机号失败，请重试'
}

export function getWechatLoginErrorMessage(error: unknown) {
  return error instanceof Error ? error.message : WECHAT_LOGIN_COPY.failedText
}

export function getWechatPhoneLoginErrorMessage(error: unknown) {
  return error instanceof Error ? error.message : WECHAT_PHONE_LOGIN_COPY.failedText
}
