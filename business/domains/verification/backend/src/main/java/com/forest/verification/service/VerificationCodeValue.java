package com.forest.verification.service;

import java.time.LocalDateTime;

/**
 * Redis value for an SMS verification code.
 */
record VerificationCodeValue(
    String businessAppCode,
    String scene,
    String phone,
    String codeHash,
    LocalDateTime issuedAt
) {
}
