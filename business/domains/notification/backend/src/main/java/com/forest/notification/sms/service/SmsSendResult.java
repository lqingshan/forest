package com.forest.notification.sms.service;

import com.forest.notification.sms.entity.SmsSendStatus;

/**
 * Business-level SMS send result.
 */
public record SmsSendResult(
    String smsNo,
    SmsSendStatus status,
    String providerResponseCode,
    String providerResponseMessage
) {
    public boolean successful() {
        return status == SmsSendStatus.SUCCESS;
    }
}
