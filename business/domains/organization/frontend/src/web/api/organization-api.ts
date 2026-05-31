import { adminHttp, apiPaths } from '@forest/http-client'
import type {
  ActionResult,
  Certification,
  Department,
  Member,
  Organization,
  OrganizationPermissions,
  OrganizationWorkspaceEntry
} from '../../shared/types'

const ORGANIZATION_BASE = `${apiPaths.admin}/organization`
const WORKSPACE_BASE = `${apiPaths.admin}/workspace`
const ORGANIZATION_NO_HEADER = 'X-Organization-No'

export interface CreateOrganizationPayload {
  organizationName: string
}

export interface UpdateOrganizationPayload {
  organizationName: string
}

export interface SubmitCertificationPayload {
  companyName: string
  unifiedSocialCreditCode: string
  legalRepresentativeName: string
  businessLicenseFileNo: string
  contactName: string
  contactPhone: string
}

export interface SaveDepartmentPayload {
  parentId?: number | null
  departmentName: string
  sortOrder?: number | null
}

export interface AddMemberPayload {
  phone: string
  name: string
  initialPassword: string
  departmentId?: number | null
}

export interface UpdateMemberPayload {
  departmentId: number
}

export interface MemberMutationResult {
  memberId: number
  memberNo: string
  departmentId: number | null
  status: Member['status']
}

function workspaceConfig(organizationNo: string) {
  return {
    headers: {
      [ORGANIZATION_NO_HEADER]: organizationNo
    }
  }
}

export function listMyOrganizations() {
  return adminHttp.get<Organization[]>(`${ORGANIZATION_BASE}/my`)
}

export function enterOrganizationWorkspace(organizationNo: string) {
  return adminHttp.post<OrganizationWorkspaceEntry>(
    `${ORGANIZATION_BASE}/${encodeURIComponent(organizationNo)}/enter`
  )
}

export function fetchMyOrganizationPermissions(organizationNo: string) {
  return adminHttp.get<OrganizationPermissions>(
    `${WORKSPACE_BASE}/access/my-permissions`,
    workspaceConfig(organizationNo)
  )
}

export function createOrganization(payload: CreateOrganizationPayload) {
  return adminHttp.post<Organization>(ORGANIZATION_BASE, payload)
}

export function fetchOrganization(organizationNo: string) {
  return adminHttp.get<Organization>(`${WORKSPACE_BASE}/organization`, workspaceConfig(organizationNo))
}

export function updateOrganization(organizationNo: string, payload: UpdateOrganizationPayload) {
  return adminHttp.put<Organization>(`${WORKSPACE_BASE}/organization`, payload, workspaceConfig(organizationNo))
}

export function submitCertification(organizationNo: string, payload: SubmitCertificationPayload) {
  return adminHttp.post<Certification>(
    `${WORKSPACE_BASE}/certification`,
    payload,
    workspaceConfig(organizationNo)
  )
}

export function fetchLatestCertification(organizationNo: string) {
  return adminHttp.get<Certification | null>(
    `${WORKSPACE_BASE}/certification/latest`,
    workspaceConfig(organizationNo)
  )
}

export function listDepartments(organizationNo: string) {
  return adminHttp.get<Department[]>(`${WORKSPACE_BASE}/department`, workspaceConfig(organizationNo))
}

export function createDepartment(organizationNo: string, payload: SaveDepartmentPayload) {
  return adminHttp.post<Department>(`${WORKSPACE_BASE}/department`, payload, workspaceConfig(organizationNo))
}

export function updateDepartment(organizationNo: string, departmentId: number, payload: SaveDepartmentPayload) {
  return adminHttp.put<Department>(
    `${WORKSPACE_BASE}/department/${departmentId}`,
    payload,
    workspaceConfig(organizationNo)
  )
}

export function deleteDepartment(organizationNo: string, departmentId: number) {
  return adminHttp.delete<ActionResult>(
    `${WORKSPACE_BASE}/department/${departmentId}`,
    workspaceConfig(organizationNo)
  )
}

export function listMembers(organizationNo: string) {
  return adminHttp.get<Member[]>(`${WORKSPACE_BASE}/member`, workspaceConfig(organizationNo))
}

export function addMember(organizationNo: string, payload: AddMemberPayload) {
  return adminHttp.post<MemberMutationResult>(`${WORKSPACE_BASE}/member`, payload, workspaceConfig(organizationNo))
}

export function updateMember(organizationNo: string, memberId: number, payload: UpdateMemberPayload) {
  return adminHttp.put<MemberMutationResult>(`${WORKSPACE_BASE}/member/${memberId}`, payload, workspaceConfig(organizationNo))
}

export function disableMember(organizationNo: string, memberId: number) {
  return adminHttp.post<MemberMutationResult>(
    `${WORKSPACE_BASE}/member/${memberId}/disable`,
    undefined,
    workspaceConfig(organizationNo)
  )
}

export function activateMember(organizationNo: string, memberId: number) {
  return adminHttp.post<MemberMutationResult>(
    `${WORKSPACE_BASE}/member/${memberId}/activate`,
    undefined,
    workspaceConfig(organizationNo)
  )
}
