import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import PointLogTable from './PointLogTable.vue'

describe('PointLogTable', () => {
  it('formats log timestamps to minute precision', () => {
    const wrapper = mount(PointLogTable, {
      props: {
        logs: [
          {
            id: 1,
            userId: 7,
            direction: 'INCOME',
            amount: 20,
            balanceAfter: 20,
            sourceType: 'RECHARGE',
            sourceId: 1,
            bizKey: 'recharge:1',
            createdTime: '2026-04-14T12:34:56'
          }
        ]
      }
    })

    expect(wrapper.text()).toContain('2026-04-14 12:34')
    expect(wrapper.text()).not.toContain('2026-04-14T12:34:56')
  })
})
