package com.forest.verification.service;

import java.time.LocalDateTime;

/**
 * Verification ticket payload stored in Redis.
 */
public record VerificationTicket(
    String ticketNo,
    String scene,
    String businessAppCode,
    String clientAppCode,
    String clientType,
    Long userId,
    String target,
    String targetType,
    LocalDateTime issuedAt
) {
}
