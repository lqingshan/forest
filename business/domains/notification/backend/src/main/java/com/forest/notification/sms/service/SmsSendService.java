package com.forest.notification.sms.service;

/**
 * Sends SMS through the configured provider and records the send attempt.
 */
public interface SmsSendService {
    SmsSendResult send(SmsSendCommand command);
}
