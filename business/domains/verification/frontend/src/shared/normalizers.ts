export function normalizePhone(phone: string) {
  return phone.replace(/\s+/g, '').trim()
}

export function normalizeSmsCode(code: string, maxLength = 6) {
  return code.replace(/\D/g, '').slice(0, maxLength)
}
