import { WECHAT_LOGIN_COPY } from '../../view-model'

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
    loginText: WECHAT_LOGIN_COPY
  },

  methods: {
    handleLogin() {
      this.triggerEvent('login')
    }
  }
})
