package com.forest.starter.carrierauth.mock;

import com.forest.starter.carrierauth.CarrierAuthClient;
import com.forest.starter.carrierauth.CarrierAuthProvider;
import com.forest.starter.carrierauth.CarrierPhoneResolveRequest;
import com.forest.starter.carrierauth.CarrierPhoneResolveResult;
import com.forest.starter.carrierauth.config.ForestCarrierAuthProperties;
import com.forest.starter.exception.BusinessException;

/**
 * Mock carrier authentication client for local and test environments.
 */
public class MockCarrierAuthClient implements CarrierAuthClient {
    private final ForestCarrierAuthProperties properties;

    public MockCarrierAuthClient(ForestCarrierAuthProperties properties) {
        this.properties = properties;
    }

    @Override
    public CarrierPhoneResolveResult resolvePhone(CarrierPhoneResolveRequest request) {
        if (request.carrierToken() == null || request.carrierToken().isBlank()) {
            throw new BusinessException("号码认证 carrierToken 不能为空");
        }
        return CarrierPhoneResolveResult.success(
            CarrierAuthProvider.MOCK,
            properties.getMockPhone(),
            "mock-carrier-request-id",
            "OK",
            "mock carrier auth resolved"
        );
    }
}
