package com.forest.starter.sms.mock;

import com.forest.starter.sms.SmsProvider;
import com.forest.starter.sms.SmsSendRequest;
import com.forest.starter.sms.SmsSendResult;
import com.forest.starter.sms.SmsSender;

/**
 * Mock SMS sender for local and test environments.
 */
public class MockSmsSender implements SmsSender {
    @Override
    public SmsSendResult send(SmsSendRequest request) {
        return SmsSendResult.success(SmsProvider.MOCK, "mock-request-id", "mock-biz-id", "OK", "mock sms sent");
    }
}
