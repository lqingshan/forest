package com.forest.starter.sms;

/**
 * Provider-neutral SMS send result.
 */
public record SmsSendResult(
    SmsProvider provider,
    boolean success,
    String requestId,
    String bizId,
    String responseCode,
    String responseMessage
) {
    public static SmsSendResult success(SmsProvider provider, String requestId, String bizId, String code, String message) {
        return new SmsSendResult(provider, true, requestId, bizId, code, message);
    }

    public static SmsSendResult failed(SmsProvider provider, String requestId, String bizId, String code, String message) {
        return new SmsSendResult(provider, false, requestId, bizId, code, message);
    }
}
