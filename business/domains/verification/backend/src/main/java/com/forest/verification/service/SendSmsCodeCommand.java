package com.forest.verification.service;

/**
 * Command for sending an SMS verification code.
 */
public record SendSmsCodeCommand(
    String businessAppCode,
    String clientAppCode,
    String clientType,
    VerificationScene scene,
    String phone,
    String sendIp
) {
}
