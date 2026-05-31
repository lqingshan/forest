import { apiPaths, platformHttp } from '@forest/http-client'
import type { Certification } from '../shared/types'

const API_BASE = `${apiPaths.platform}/organization-certification`

export interface ReviewCertificationPayload {
  reviewRemark?: string | null
}

export function listPendingOrganizationCertifications() {
  return platformHttp.get<Certification[]>(API_BASE)
}

export function fetchOrganizationCertification(certificationId: number) {
  return platformHttp.get<Certification>(`${API_BASE}/${certificationId}`)
}

export function approveOrganizationCertification(certificationId: number, payload: ReviewCertificationPayload = {}) {
  return platformHttp.post<Certification>(`${API_BASE}/${certificationId}/approve`, payload)
}

export function rejectOrganizationCertification(certificationId: number, payload: ReviewCertificationPayload = {}) {
  return platformHttp.post<Certification>(`${API_BASE}/${certificationId}/reject`, payload)
}
