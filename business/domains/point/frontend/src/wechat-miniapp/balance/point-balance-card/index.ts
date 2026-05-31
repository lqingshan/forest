import type { PointBalance } from '../types'

Component({
  properties: {
    balance: {
      type: Object,
      value: null
    }
  },

  data: {
    emptyBalance: {
      balance: 0,
      totalIncome: 0,
      totalSpend: 0
    } satisfies PointBalance
  }
})
