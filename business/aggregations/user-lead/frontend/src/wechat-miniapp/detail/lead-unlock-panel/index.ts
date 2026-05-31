import type { LeadDetailCardModel } from '../view-model'

Component({
  properties: {
    lead: {
      type: Object,
      value: null
    },
    unlockCost: {
      type: Number,
      value: 0
    },
    unlocking: {
      type: Boolean,
      value: false
    }
  },

  methods: {
    handleUnlock() {
      const lead = this.properties.lead as LeadDetailCardModel | null
      if (!lead || lead.unlocked) {
        return
      }

      this.triggerEvent('unlock')
    }
  }
})
