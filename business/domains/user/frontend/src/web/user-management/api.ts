import { apiPaths, platformHttp } from '@forest/http-client'
import type { UserManagementUser, UserPage, UserPageQuery } from './types'

// user-management 是 Web 运行时下的用户管理能力。
// 当前业务只在平台后台使用，所以这里暂时固定走 /api/platform/user 和 platformHttp。
// 后续如果商家后台也需要用户管理，再抽 createUserManagementApi(accessScope)。
const API_BASE = `${apiPaths.platform}/user`
const PAGE_API = `${API_BASE}/page`

export function fetchUsers(query: UserPageQuery = {}) {
  return platformHttp.get<UserPage>(PAGE_API, {
    params: query
  })
}

export function fetchUser(id: number) {
  return platformHttp.get<UserManagementUser>(`${API_BASE}/${id}`)
}

export function freezeUser(id: number) {
  return platformHttp.post<UserManagementUser>(`${API_BASE}/${id}/freeze`)
}

export function activateUser(id: number) {
  return platformHttp.post<UserManagementUser>(`${API_BASE}/${id}/activate`)
}
