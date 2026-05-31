Component({
  properties: {
    balance: {
      type: Object,
      value: null
    },
    packages: {
      type: Array,
      value: []
    },
    selectedCode: {
      type: String,
      value: ''
    },
    paying: {
      type: Boolean,
      value: false
    },
    errorMessage: {
      type: String,
      value: ''
    }
  },

  methods: {
    handleSelectPackage(event: { detail: { code?: string } }) {
      this.triggerEvent('selectpackage', event.detail)
    },

    handlePay() {
      this.triggerEvent('pay')
    }
  }
})
