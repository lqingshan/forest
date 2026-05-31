export type WebLoginHistoryRecord =
  | { mode: 'phone_password'; identifier: string; password?: string }
  | { mode: 'phone_sms'; identifier: string }

export interface WebLoginHistory {
  records: WebLoginHistoryRecord[]
}

export const DEFAULT_LOGIN_HISTORY_KEY = 'forest.user.web.loginHistory'

export function loginHistoryStorageKey(storagePrefix?: string) {
  const safePrefix = storagePrefix?.trim()
  return safePrefix ? `${safePrefix}.loginHistory` : DEFAULT_LOGIN_HISTORY_KEY
}

export function readWebLoginHistory(storageKey: string): WebLoginHistory {
  if (typeof window === 'undefined') {
    return { records: [] }
  }

  try {
    const rawValue = window.localStorage.getItem(storageKey)
    const parsedValue = rawValue ? JSON.parse(rawValue) : null
    const records = Array.isArray(parsedValue?.records)
      ? parsedValue.records.filter(isLoginHistoryRecord)
      : []

    return { records }
  } catch {
    return { records: [] }
  }
}

export function recordPhonePasswordLoginHistory(
  storageKey: string,
  identifier: string,
  password: string,
  savePassword: boolean
) {
  const safeIdentifier = identifier.trim()
  if (!safeIdentifier) {
    return
  }

  recordLoginHistory(storageKey, savePassword
    ? { mode: 'phone_password', identifier: safeIdentifier, password }
    : { mode: 'phone_password', identifier: safeIdentifier }
  )
}

export function recordPhoneSmsLoginHistory(storageKey: string, identifier: string) {
  const safeIdentifier = identifier.trim()
  if (!safeIdentifier) {
    return
  }

  recordLoginHistory(storageKey, { mode: 'phone_sms', identifier: safeIdentifier })
}

function recordLoginHistory(storageKey: string, record: WebLoginHistoryRecord) {
  if (typeof window === 'undefined') {
    return
  }

  const history = readWebLoginHistory(storageKey)
  const records = [
    record,
    ...history.records.filter((item) => item.mode !== record.mode || item.identifier !== record.identifier)
  ]

  try {
    window.localStorage.setItem(storageKey, JSON.stringify({ records }))
  } catch {
    // Login history is best-effort and must not block a successful login.
  }
}

function isLoginHistoryRecord(value: unknown): value is WebLoginHistoryRecord {
  if (!value || typeof value !== 'object') {
    return false
  }

  const record = value as {
    mode?: unknown
    identifier?: unknown
    password?: unknown
  }

  if (record.mode !== 'phone_password' && record.mode !== 'phone_sms') {
    return false
  }
  if (typeof record.identifier !== 'string' || !record.identifier.trim()) {
    return false
  }

  return record.mode === 'phone_sms'
    || record.password === undefined
    || typeof record.password === 'string'
}
