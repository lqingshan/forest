import { buildRechargeSummary } from '../view-model'

Component({
  properties: {
    balance: {
      type: Object,
      value: null
    }
  },

  data: {
    summary: buildRechargeSummary(null)
  },

  observers: {
    balance(value: { balance?: number } | null) {
      this.setData({
        summary: buildRechargeSummary(value && typeof value.balance === 'number' ? { balance: value.balance } : null)
      })
    }
  }
})
