Component({
  properties: {
    order: {
      type: Object,
      value: null
    },
    status: {
      type: String,
      value: 'processing'
    },
    title: {
      type: String,
      value: '支付处理中'
    },
    description: {
      type: String,
      value: '正在确认充值结果，请稍候刷新状态。'
    }
  }
})
