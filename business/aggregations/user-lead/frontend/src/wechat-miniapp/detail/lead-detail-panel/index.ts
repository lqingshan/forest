Component({
  properties: {
    lead: {
      type: Object,
      value: null
    },
    unlocking: {
      type: Boolean,
      value: false
    },
    unlockCost: {
      type: Number,
      value: 0
    },
    errorMessage: {
      type: String,
      value: ''
    }
  },

  methods: {
    handleCopyField(event: { detail: { field?: string; value?: string } }) {
      this.triggerEvent('copyfield', event.detail)
    },

    handleUnlock() {
      this.triggerEvent('unlock')
    }
  }
})
