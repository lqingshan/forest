package com.forest.verification.service;

/**
 * Result of verifying an SMS code.
 */
public record VerifySmsCodeResult(
    String phone,
    VerificationCodeMode mode
) {
}
