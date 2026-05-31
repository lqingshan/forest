package com.forest.starter.sms.disabled;

import com.forest.starter.sms.SmsProvider;
import com.forest.starter.sms.SmsSendRequest;
import com.forest.starter.sms.SmsSendResult;
import com.forest.starter.sms.SmsSender;

/**
 * SMS sender used when SMS capability is disabled.
 */
public class DisabledSmsSender implements SmsSender {
    @Override
    public SmsSendResult send(SmsSendRequest request) {
        return SmsSendResult.failed(SmsProvider.DISABLED, null, null, "DISABLED", "短信发送能力已关闭");
    }
}
