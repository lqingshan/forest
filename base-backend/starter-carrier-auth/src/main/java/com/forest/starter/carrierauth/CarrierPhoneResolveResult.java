package com.forest.starter.carrierauth;

/**
 * Provider-neutral carrier phone resolving result.
 */
public record CarrierPhoneResolveResult(
    CarrierAuthProvider provider,
    boolean success,
    String phone,
    String requestId,
    String responseCode,
    String responseMessage
) {
    public static CarrierPhoneResolveResult success(
        CarrierAuthProvider provider,
        String phone,
        String requestId,
        String code,
        String message
    ) {
        return new CarrierPhoneResolveResult(provider, true, phone, requestId, code, message);
    }

    public static CarrierPhoneResolveResult failed(
        CarrierAuthProvider provider,
        String requestId,
        String code,
        String message
    ) {
        return new CarrierPhoneResolveResult(provider, false, null, requestId, code, message);
    }
}
