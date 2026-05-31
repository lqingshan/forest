import { mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import UserPointWorkspace from './UserPointWorkspace.vue'

const {
  fetchUserPointDetail,
  fetchUserPointLogs
} = vi.hoisted(() => ({
  fetchUserPointDetail: vi.fn(),
  fetchUserPointLogs: vi.fn()
}))

vi.mock('./api', () => ({
  fetchUserPointDetail,
  fetchUserPointLogs
}))

vi.mock('@forest/point/platform-web/point-display', () => ({
  PointPlatformBalanceCard: {
    props: ['balance'],
    template: '<div class="balance-card">{{ balance.userId }}</div>'
  },
  PointLogTable: {
    props: ['logs'],
    template: '<div class="log-table">{{ logs.length }}</div>'
  }
}))

const flushPromises = () => new Promise((resolve) => setTimeout(resolve, 0))

describe('UserPointWorkspace', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('loads detail once and paginates logs independently', async () => {
    fetchUserPointDetail.mockResolvedValue({
      user: {
        id: 8,
        name: 'Alice',
        avatar: null,
        phone: null,
        email: null,
        status: 'ACTIVE',
        adminUser: true,
        user: true
      },
      points: {
        balance: 20,
        totalIncome: 25,
        totalSpend: 5,
        updatedAt: '2026-04-14T12:00:00'
      }
    })
    fetchUserPointLogs
      .mockResolvedValueOnce({
        content: [{ id: 1 }],
        totalElements: 2,
        totalPages: 2,
        size: 20,
        number: 0
      })
      .mockResolvedValueOnce({
        content: [{ id: 2 }],
        totalElements: 2,
        totalPages: 2,
        size: 20,
        number: 1
      })

    const wrapper = mount(UserPointWorkspace, {
      props: {
        userId: 8
      }
    })

    await flushPromises()

    expect(fetchUserPointDetail).toHaveBeenCalledTimes(1)
    expect(fetchUserPointDetail).toHaveBeenCalledWith(8)
    expect(fetchUserPointLogs).toHaveBeenCalledTimes(1)
    expect(fetchUserPointLogs).toHaveBeenCalledWith(8, 0, 20)

    const pagerButtons = wrapper.findAll('.workspace-button-soft')
    await pagerButtons[1].trigger('click')
    await flushPromises()

    expect(fetchUserPointDetail).toHaveBeenCalledTimes(1)
    expect(fetchUserPointLogs).toHaveBeenCalledTimes(2)
    expect(fetchUserPointLogs).toHaveBeenLastCalledWith(8, 1, 20)
  })
})
