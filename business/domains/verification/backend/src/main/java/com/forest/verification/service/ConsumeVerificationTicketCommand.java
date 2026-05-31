package com.forest.verification.service;

/**
 * Command for consuming a short-lived verification ticket.
 */
public record ConsumeVerificationTicketCommand(
    String ticketNo,
    VerificationScene scene,
    String businessAppCode,
    String clientAppCode,
    String clientType,
    Long userId,
    String target,
    String targetType
) {
}
