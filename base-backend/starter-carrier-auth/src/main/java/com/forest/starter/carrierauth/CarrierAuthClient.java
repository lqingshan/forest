package com.forest.starter.carrierauth;

/**
 * Resolves a phone number from a native carrier one-click-login token.
 */
public interface CarrierAuthClient {
    CarrierPhoneResolveResult resolvePhone(CarrierPhoneResolveRequest request);
}
