import { miniappRouter } from '../../miniapp-app'

Component({
  properties: {
    current: {
      type: String,
      value: 'leads'
    }
  },

  methods: {
    handleTap(event: { currentTarget: { dataset: { target?: string } } }) {
      const target = event.currentTarget.dataset.target
      if (!target || target === this.properties.current) {
        return
      }

      const urlMapping: Record<string, string> = {
        leads: '/pages/leads/index',
        unlocked: '/pages/unlocked/index',
        me: '/pages/me/index'
      }
      miniappRouter.openPrimaryPage(urlMapping[target] || '/pages/leads/index')
    }
  }
})
