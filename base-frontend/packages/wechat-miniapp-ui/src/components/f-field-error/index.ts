function normalizeFieldErrorMessage(value: unknown): string {
  return typeof value === 'string' ? value.trim() : ''
}

Component({
  externalClasses: ['error-class'],

  properties: {
    message: {
      type: String,
      value: ''
    }
  },

  data: {
    normalizedMessage: ''
  },

  observers: {
    message(value: string) {
      this.setData({
        normalizedMessage: normalizeFieldErrorMessage(value)
      })
    }
  }
})
