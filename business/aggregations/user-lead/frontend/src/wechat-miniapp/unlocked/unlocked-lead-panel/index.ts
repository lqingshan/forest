Component({
  properties: {
    leads: {
      type: Array,
      value: []
    },
    loading: {
      type: Boolean,
      value: false
    },
    loadingMore: {
      type: Boolean,
      value: false
    },
    errorMessage: {
      type: String,
      value: ''
    }
  },

  methods: {
    handleTapLead(event: { detail: { id?: number } }) {
      this.triggerEvent('taplead', event.detail)
    }
  }
})
