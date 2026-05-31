export function getVerificationErrorMessage(error: unknown) {
  if (error instanceof Error && error.message) {
    return error.message
  }
  return '验证码处理失败'
}
