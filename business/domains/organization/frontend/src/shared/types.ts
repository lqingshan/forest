export type OrganizationStatus = 'ACTIVE' | 'DISABLED'
export type OrganizationCertificationStatus = 'NOT_SUBMITTED' | 'PENDING' | 'APPROVED' | 'REJECTED'
export type DepartmentStatus = 'ACTIVE' | 'DISABLED'
export type MemberStatus = 'ACTIVE' | 'DISABLED'
export type CertificationStatus = 'PENDING' | 'APPROVED' | 'REJECTED'
export type OrganizationWorkspaceMode = 'CERTIFICATION_ONLY' | 'FULL'

export interface Organization {
  id: number
  organizationNo: string
  organizationName: string
  status: OrganizationStatus
  certificationStatus: OrganizationCertificationStatus
  currentCertificationId: number | null
  ownerUserId: number
  createdTime: string
}

export interface Department {
  id: number
  departmentNo: string
  organizationId: number
  parentId: number | null
  departmentName: string
  defaultDepartment: boolean
  sortOrder: number | null
  status: DepartmentStatus
}

export interface Member {
  memberId: number
  memberNo: string
  name: string | null
  phone: string | null
  departmentId: number | null
  status: MemberStatus
}

export interface OrganizationPermissions {
  organizationNo: string
  workspaceMode: OrganizationWorkspaceMode
  certified: boolean
  permissions: string[]
}

export interface OrganizationWorkspaceEntry {
  organizationId: number
  organizationNo: string
  memberId: number
  workspaceMode: OrganizationWorkspaceMode
  certified: boolean
}

export interface Certification {
  id: number
  certificationNo: string
  organizationId: number
  companyName: string
  unifiedSocialCreditCode: string
  legalRepresentativeName: string
  businessLicenseFileNo: string
  contactName: string
  contactPhone: string
  status: CertificationStatus
  submittedByUserId: number
  reviewedByUserId: number | null
  reviewedTime: string | null
  reviewRemark: string | null
  createdTime: string
}

export interface ActionResult {
  success: boolean
}
