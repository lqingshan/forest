import type { UserLeadListCardModel } from '../view-model'

Component({
  properties: {
    lead: {
      type: Object,
      value: null
    }
  },

  methods: {
    handleTap() {
      const lead = this.properties.lead as UserLeadListCardModel | null
      if (!lead?.id) {
        return
      }

      this.triggerEvent('taplead', {
        id: lead.id
      })
    }
  }
})
