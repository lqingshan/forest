package com.forest.verification.service;

/**
 * Manages Redis-backed SMS verification codes.
 */
public interface VerificationCodeService {
    SendSmsCodeResult sendSmsCode(SendSmsCodeCommand command);

    VerifySmsCodeResult verifySmsCode(VerifySmsCodeCommand command);
}
