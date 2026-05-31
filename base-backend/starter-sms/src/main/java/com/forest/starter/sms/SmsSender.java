package com.forest.starter.sms;

/**
 * Sends SMS messages through a configured technical provider.
 */
public interface SmsSender {
    SmsSendResult send(SmsSendRequest request);
}
