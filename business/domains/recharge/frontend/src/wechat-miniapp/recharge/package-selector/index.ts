import type { RechargePackageOption } from '../view-model'

Component({
  properties: {
    packages: {
      type: Array,
      value: []
    },
    selectedCode: {
      type: String,
      value: ''
    },
    emptyText: {
      type: String,
      value: '暂无可选充值套餐'
    }
  },

  methods: {
    handleSelect(event: { currentTarget: { dataset: { code?: string } } }) {
      const code = event.currentTarget.dataset.code
      const packages = (this.properties.packages || []) as RechargePackageOption[]
      const selected = packages.find((item) => item.code === code)
      if (!selected) {
        return
      }

      this.triggerEvent('selectpackage', {
        code: selected.code,
        package: selected
      })
    }
  }
})
