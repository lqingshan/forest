import type { LeadDetailCardModel } from '../view-model'

Component({
  properties: {
    lead: {
      type: Object,
      value: null
    }
  },

  methods: {
    copyField(event: { currentTarget: { dataset: { field?: string; value?: string } } }) {
      const lead = this.properties.lead as LeadDetailCardModel | null
      const value = event.currentTarget.dataset.value
      if (!lead?.unlocked || !value) {
        return
      }

      this.triggerEvent('copyfield', {
        field: event.currentTarget.dataset.field || '',
        value
      })
    }
  }
})
