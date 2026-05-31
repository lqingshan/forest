import { WECHAT_PHONE_LOGIN_COPY } from '../../view-model'

function resolvePhoneCode(detail: MiniappRecord | null | undefined) {
  const code = detail && typeof detail.code === 'string' ? detail.code.trim() : ''
  return code || null
}

Component({
  properties: {
    submitting: {
      type: Boolean,
      value: false
    },
    errorMessage: {
      type: String,
      value: ''
    }
  },

  data: {
    loginText: WECHAT_PHONE_LOGIN_COPY
  },

  methods: {
    handleGetPhoneNumber(event: MiniappRecord) {
      const phoneCode = resolvePhoneCode(event.detail)
      if (!phoneCode) {
        this.triggerEvent('phoneerror', {
          message: WECHAT_PHONE_LOGIN_COPY.authorizationRequiredText
        })
        return
      }
      this.triggerEvent('phonelogin', {
        phoneCode
      })
    }
  }
})
