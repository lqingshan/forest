Component({
  properties: {
    keyword: {
      type: String,
      value: ''
    }
  },

  methods: {
    handleInput(event: { detail: { value?: string } }) {
      this.triggerEvent('inputkeyword', {
        keyword: event.detail.value || ''
      })
    },

    handleSearch() {
      this.triggerEvent('search')
    }
  }
})
