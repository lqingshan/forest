import { RECHARGE_COPY } from '../view-model'

Component({
  properties: {
    paying: {
      type: Boolean,
      value: false
    }
  },

  data: {
    payButtonText: RECHARGE_COPY.payButtonText
  },

  methods: {
    handlePay() {
      this.triggerEvent('pay')
    }
  }
})
