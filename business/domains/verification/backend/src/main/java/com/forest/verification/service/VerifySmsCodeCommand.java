package com.forest.verification.service;

/**
 * Command for verifying an SMS code.
 */
public record VerifySmsCodeCommand(
    String businessAppCode,
    VerificationScene scene,
    String phone,
    String code
) {
}
