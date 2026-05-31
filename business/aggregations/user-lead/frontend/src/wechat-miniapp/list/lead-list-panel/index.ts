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
    keyword: {
      type: String,
      value: ''
    },
    totalElements: {
      type: Number,
      value: 0
    },
    errorMessage: {
      type: String,
      value: ''
    }
  },

  methods: {
    handleInputKeyword(event: { detail: { keyword?: string; value?: string } }) {
      this.triggerEvent('inputkeyword', event.detail)
    },

    handleSearch() {
      this.triggerEvent('search')
    },

    handleTapLead(event: { detail: { id?: number } }) {
      this.triggerEvent('taplead', event.detail)
    }
  }
})
