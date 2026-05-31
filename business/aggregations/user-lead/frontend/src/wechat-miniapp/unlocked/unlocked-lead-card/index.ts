import type { UnlockedLeadCardModel } from '../view-model'

Component({
  properties: {
    lead: {
      type: Object,
      value: null
    }
  },

  methods: {
    handleTap() {
      const lead = this.properties.lead as UnlockedLeadCardModel | null
      if (!lead?.id) {
        return
      }

      this.triggerEvent('taplead', {
        id: lead.id
      })
    }
  }
})
