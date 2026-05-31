import { buildUserLeadListSummary } from '../view-model'

Component({
  properties: {
    totalElements: {
      type: Number,
      value: 0
    }
  },

  data: {
    summary: buildUserLeadListSummary(0)
  },

  observers: {
    totalElements(value: number) {
      this.setData({
        summary: buildUserLeadListSummary(value)
      })
    }
  }
})
