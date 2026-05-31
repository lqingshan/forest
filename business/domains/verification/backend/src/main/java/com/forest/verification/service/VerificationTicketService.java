package com.forest.verification.service;

/**
 * Issues and consumes one-time verification tickets.
 */
public interface VerificationTicketService {
    VerificationTicket issue(IssueVerificationTicketCommand command);

    VerificationTicket consume(ConsumeVerificationTicketCommand command);
}
