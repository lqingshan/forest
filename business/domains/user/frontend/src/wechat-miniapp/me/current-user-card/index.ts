import type { CurrentUser } from '../types'
import { buildCurrentUserSummary } from '../view-model'

Component({
  properties: {
    user: {
      type: Object,
      value: null
    },
    uploadingAvatar: {
      type: Boolean,
      value: false
    }
  },

  data: {
    summary: buildCurrentUserSummary(null)
  },

  observers: {
    user(value: MiniappRecord | null) {
      this.setData({
        summary: buildCurrentUserSummary(value as CurrentUser | null)
      })
    }
  },

  methods: {
    handleAvatarTap() {
      if (this.properties.uploadingAvatar) {
        return
      }
      this.triggerEvent('avatarclick')
    }
  }
})
