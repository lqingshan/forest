import type { CurrentWebUser } from './types'
import type { WebUserSession } from '../auth/session-factory'

export function fetchCurrentWebUser(session: WebUserSession) {
  return session.fetchCurrentUser()
}

export function updateCurrentWebUserAvatar(session: WebUserSession, fileNo: string) {
  return session.updateAvatar(fileNo)
}

export function uploadCurrentWebUserAvatar(session: WebUserSession, file: File) {
  return session.uploadAvatar(file)
}
