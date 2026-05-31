import { afterEach, describe, expect, it, vi } from 'vitest'
import type { Organization, OrganizationPermissions, OrganizationWorkspaceEntry } from '../shared/types'
import { createOrganizationWorkspaceState } from './organization-workspace-state'
import { enterOrganizationWorkspace, fetchMyOrganizationPermissions, listMyOrganizations } from './api'

vi.mock('./api', () => ({
  enterOrganizationWorkspace: vi.fn(),
  fetchMyOrganizationPermissions: vi.fn(),
  listMyOrganizations: vi.fn()
}))

const organizations: Organization[] = [
  {
    id: 1,
    organizationNo: 'ORG_A',
    organizationName: '企业 A',
    status: 'ACTIVE',
    certificationStatus: 'APPROVED',
    currentCertificationId: null,
    ownerUserId: 101,
    createdTime: '2026-05-29T00:00:00'
  },
  {
    id: 2,
    organizationNo: 'ORG_B',
    organizationName: '企业 B',
    status: 'ACTIVE',
    certificationStatus: 'PENDING',
    currentCertificationId: 10,
    ownerUserId: 102,
    createdTime: '2026-05-29T00:00:00'
  }
]

const workspaceEntry: OrganizationWorkspaceEntry = {
  organizationId: 1,
  organizationNo: 'ORG_A',
  memberId: 1001,
  workspaceMode: 'FULL',
  certified: true
}

const permissions: OrganizationPermissions = {
  organizationNo: 'ORG_A',
  workspaceMode: 'FULL',
  certified: true,
  permissions: ['organization.read', 'access.role.read']
}

describe('createOrganizationWorkspaceState', () => {
  afterEach(() => {
    window.localStorage.clear()
    vi.clearAllMocks()
  })

  it('loads my organizations, selects the first valid organization and fetches permissions', async () => {
    vi.mocked(listMyOrganizations).mockResolvedValue(organizations)
    vi.mocked(enterOrganizationWorkspace).mockResolvedValue(workspaceEntry)
    vi.mocked(fetchMyOrganizationPermissions).mockResolvedValue(permissions)
    const workspaceState = createOrganizationWorkspaceState({
      storageKey: 'test.selectedOrganizationNo'
    })

    await workspaceState.refreshOrganizations()

    expect(listMyOrganizations).toHaveBeenCalledTimes(1)
    expect(enterOrganizationWorkspace).toHaveBeenCalledWith('ORG_A')
    expect(fetchMyOrganizationPermissions).toHaveBeenCalledWith('ORG_A')
    expect(workspaceState.organizationState.organizations).toEqual(organizations)
    expect(workspaceState.organizationState.selectedOrganizationNo).toBe('ORG_A')
    expect(workspaceState.selectedOrganization.value?.organizationName).toBe('企业 A')
    expect(workspaceState.selectedOrganizationCertified.value).toBe(true)
    expect(workspaceState.organizationState.permissions).toEqual(permissions)
    expect(window.localStorage.getItem('test.selectedOrganizationNo')).toBe('ORG_A')
  })

  it('can load organizations without auto-selecting the first organization', async () => {
    vi.mocked(listMyOrganizations).mockResolvedValue(organizations)
    const workspaceState = createOrganizationWorkspaceState({
      storageKey: 'test.selectedOrganizationNo',
      autoSelectFirstOrganization: false
    })

    await workspaceState.refreshOrganizations({ refreshPermissions: false })

    expect(listMyOrganizations).toHaveBeenCalledTimes(1)
    expect(enterOrganizationWorkspace).not.toHaveBeenCalled()
    expect(fetchMyOrganizationPermissions).not.toHaveBeenCalled()
    expect(workspaceState.organizationState.organizations).toEqual(organizations)
    expect(workspaceState.organizationState.selectedOrganizationNo).toBe('')
    expect(workspaceState.organizationState.permissions).toBeNull()
    expect(window.localStorage.getItem('test.selectedOrganizationNo')).toBeNull()
  })

  it('reuses loaded organizations and only refetches permissions when permissions are missing', async () => {
    vi.mocked(listMyOrganizations).mockResolvedValue(organizations)
    vi.mocked(enterOrganizationWorkspace).mockResolvedValue(workspaceEntry)
    vi.mocked(fetchMyOrganizationPermissions).mockResolvedValue(permissions)
    const workspaceState = createOrganizationWorkspaceState({
      storageKey: 'test.selectedOrganizationNo'
    })
    await workspaceState.refreshOrganizations()
    vi.clearAllMocks()

    workspaceState.organizationState.permissions = null
    await workspaceState.refreshOrganizations()

    expect(listMyOrganizations).not.toHaveBeenCalled()
    expect(enterOrganizationWorkspace).toHaveBeenCalledTimes(1)
    expect(fetchMyOrganizationPermissions).toHaveBeenCalledTimes(1)
    expect(workspaceState.organizationState.permissions).toEqual(permissions)
  })

  it('forces organization list refresh when force is true', async () => {
    vi.mocked(listMyOrganizations).mockResolvedValue(organizations)
    vi.mocked(enterOrganizationWorkspace).mockResolvedValue(workspaceEntry)
    vi.mocked(fetchMyOrganizationPermissions).mockResolvedValue(permissions)
    const workspaceState = createOrganizationWorkspaceState({
      storageKey: 'test.selectedOrganizationNo'
    })
    await workspaceState.refreshOrganizations()
    vi.clearAllMocks()

    await workspaceState.refreshOrganizations({ force: true })

    expect(listMyOrganizations).toHaveBeenCalledTimes(1)
    expect(enterOrganizationWorkspace).toHaveBeenCalledTimes(1)
    expect(fetchMyOrganizationPermissions).toHaveBeenCalledTimes(1)
  })

  it('merges concurrent organization refresh requests', async () => {
    const deferredOrganizations = createDeferred<Organization[]>()
    vi.mocked(listMyOrganizations).mockReturnValue(deferredOrganizations.promise)
    vi.mocked(enterOrganizationWorkspace).mockResolvedValue(workspaceEntry)
    vi.mocked(fetchMyOrganizationPermissions).mockResolvedValue(permissions)
    const workspaceState = createOrganizationWorkspaceState({
      storageKey: 'test.selectedOrganizationNo'
    })

    const firstRefresh = workspaceState.refreshOrganizations()
    const secondRefresh = workspaceState.refreshOrganizations()
    expect(listMyOrganizations).toHaveBeenCalledTimes(1)
    deferredOrganizations.resolve(organizations)
    await Promise.all([firstRefresh, secondRefresh])

    expect(workspaceState.organizationState.loaded).toBe(true)
    expect(enterOrganizationWorkspace).toHaveBeenCalledTimes(1)
    expect(fetchMyOrganizationPermissions).toHaveBeenCalledTimes(1)
  })

  it('merges concurrent permission refresh requests for the same organization', async () => {
    const deferredWorkspaceEntry = createDeferred<OrganizationWorkspaceEntry>()
    vi.mocked(enterOrganizationWorkspace).mockReturnValue(deferredWorkspaceEntry.promise)
    vi.mocked(fetchMyOrganizationPermissions).mockResolvedValue(permissions)
    const workspaceState = createOrganizationWorkspaceState({
      storageKey: 'test.selectedOrganizationNo'
    })
    workspaceState.organizationState.selectedOrganizationNo = 'ORG_A'

    const firstRefresh = workspaceState.refreshOrganizationPermissions()
    const secondRefresh = workspaceState.refreshOrganizationPermissions()
    expect(enterOrganizationWorkspace).toHaveBeenCalledTimes(1)
    deferredWorkspaceEntry.resolve(workspaceEntry)
    await Promise.all([firstRefresh, secondRefresh])

    expect(fetchMyOrganizationPermissions).toHaveBeenCalledTimes(1)
    expect(workspaceState.organizationState.permissions).toEqual(permissions)
  })

  it('resets workspace state and removes selected organization from localStorage', async () => {
    vi.mocked(listMyOrganizations).mockResolvedValue(organizations)
    vi.mocked(enterOrganizationWorkspace).mockResolvedValue(workspaceEntry)
    vi.mocked(fetchMyOrganizationPermissions).mockResolvedValue(permissions)
    const workspaceState = createOrganizationWorkspaceState({
      storageKey: 'test.selectedOrganizationNo'
    })
    await workspaceState.refreshOrganizations()

    workspaceState.resetOrganizations()

    expect(workspaceState.organizationState.organizations).toEqual([])
    expect(workspaceState.organizationState.selectedOrganizationNo).toBe('')
    expect(workspaceState.organizationState.permissions).toBeNull()
    expect(workspaceState.organizationState.loading).toBe(false)
    expect(workspaceState.organizationState.loaded).toBe(false)
    expect(window.localStorage.getItem('test.selectedOrganizationNo')).toBeNull()
  })

  it('checks permissions from backend-expanded exact permission codes', async () => {
    vi.mocked(listMyOrganizations).mockResolvedValue(organizations)
    vi.mocked(enterOrganizationWorkspace).mockResolvedValue(workspaceEntry)
    vi.mocked(fetchMyOrganizationPermissions).mockResolvedValue(permissions)
    const workspaceState = createOrganizationWorkspaceState({
      storageKey: 'test.selectedOrganizationNo'
    })
    await workspaceState.refreshOrganizations()

    expect(workspaceState.can('organization.read')).toBe(true)
    expect(workspaceState.can('organization.update')).toBe(false)
  })
})

function createDeferred<T>() {
  let resolve!: (value: T) => void
  let reject!: (reason?: unknown) => void
  const promise = new Promise<T>((promiseResolve, promiseReject) => {
    resolve = promiseResolve
    reject = promiseReject
  })
  return { promise, resolve, reject }
}
