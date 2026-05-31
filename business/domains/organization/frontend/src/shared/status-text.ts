import type {
  CertificationStatus,
  MemberStatus,
  OrganizationCertificationStatus,
  OrganizationStatus
} from './types'

export function organizationStatusText(status: OrganizationStatus) {
  return status === 'ACTIVE' ? '启用' : '禁用'
}

export function organizationCertificationStatusText(status: OrganizationCertificationStatus) {
  switch (status) {
    case 'APPROVED':
      return '已认证'
    case 'PENDING':
      return '待审核'
    case 'REJECTED':
      return '已驳回'
    case 'NOT_SUBMITTED':
      return '未认证'
    default:
      return '未认证'
  }
}

export function certificationReviewStatusText(status: CertificationStatus) {
  switch (status) {
    case 'APPROVED':
      return '已通过'
    case 'REJECTED':
      return '已驳回'
    case 'PENDING':
      return '待审核'
    default:
      return '待审核'
  }
}

export function memberStatusText(status: MemberStatus) {
  return status === 'ACTIVE' ? '启用' : '停用'
}
