package com.forest.starter.carrierauth.disabled;

import com.forest.starter.carrierauth.CarrierAuthClient;
import com.forest.starter.carrierauth.CarrierAuthProvider;
import com.forest.starter.carrierauth.CarrierPhoneResolveRequest;
import com.forest.starter.carrierauth.CarrierPhoneResolveResult;

/**
 * Carrier authentication client used when one-click-login is disabled.
 */
public class DisabledCarrierAuthClient implements CarrierAuthClient {
    @Override
    public CarrierPhoneResolveResult resolvePhone(CarrierPhoneResolveRequest request) {
        return CarrierPhoneResolveResult.failed(CarrierAuthProvider.DISABLED, null, "DISABLED", "本机号一键登录能力已关闭");
    }
}
