package com.forest.verification.service;

/**
 * Command for issuing a short-lived verification ticket after code verification.
 */
public record IssueVerificationTicketCommand(
    VerificationScene scene,
    String businessAppCode,
    String clientAppCode,
    String clientType,
    Long userId,
    String target,
    String targetType
) {
}
