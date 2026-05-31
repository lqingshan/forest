package com.forest.starter.carrierauth;

/**
 * Provider-neutral carrier phone resolving request.
 *
 * @param carrierToken one-time token returned by the native carrier authentication SDK
 * @param provider provider name reported by the client, used only for diagnostics and future routing
 * @param outId optional external trace id
 */
public record CarrierPhoneResolveRequest(
    String carrierToken,
    String provider,
    String outId
) {
}
