import { POINT_LOGS_COPY } from '../view-model'

Component({
  properties: {
    logs: {
      type: Array,
      value: []
    },
    empty: {
      type: Boolean,
      value: false
    }
  },

  data: {
    emptyText: POINT_LOGS_COPY.emptyText
  }
})
