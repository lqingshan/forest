package com.forest.starter.carrierauth.aliyun;

import com.aliyun.dypnsapi20170525.Client;
import com.aliyun.dypnsapi20170525.models.GetMobileRequest;
import com.aliyun.dypnsapi20170525.models.GetMobileResponse;
import com.aliyun.dypnsapi20170525.models.GetMobileResponseBody;
import com.aliyun.teaopenapi.models.Config;
import com.forest.starter.carrierauth.CarrierAuthClient;
import com.forest.starter.carrierauth.CarrierAuthProvider;
import com.forest.starter.carrierauth.CarrierPhoneResolveRequest;
import com.forest.starter.carrierauth.CarrierPhoneResolveResult;
import com.forest.starter.carrierauth.config.ForestCarrierAuthProperties;
import com.forest.starter.exception.BusinessException;

/**
 * Aliyun PNVS GetMobile implementation for native app one-click-login.
 */
public class AliyunCarrierAuthClient implements CarrierAuthClient {
    private static final String OK = "OK";

    private final ForestCarrierAuthProperties properties;
    private Client client;

    public AliyunCarrierAuthClient(ForestCarrierAuthProperties properties) {
        this.properties = properties;
    }

    @Override
    public CarrierPhoneResolveResult resolvePhone(CarrierPhoneResolveRequest request) {
        requireText("号码认证 carrierToken", request.carrierToken());
        try {
            GetMobileRequest aliyunRequest = new GetMobileRequest()
                .setAccessToken(request.carrierToken())
                .setOutId(request.outId());
            GetMobileResponse response = client().getMobile(aliyunRequest);
            GetMobileResponseBody body = response.getBody();
            if (body == null) {
                return CarrierPhoneResolveResult.failed(CarrierAuthProvider.ALIYUN, null, "EMPTY_RESPONSE", "阿里云号码认证响应为空");
            }
            if (!OK.equals(body.getCode())) {
                return CarrierPhoneResolveResult.failed(
                    CarrierAuthProvider.ALIYUN,
                    body.getRequestId(),
                    body.getCode(),
                    body.getMessage()
                );
            }
            String phone = body.getGetMobileResultDTO() == null ? null : body.getGetMobileResultDTO().getMobile();
            if (phone == null || phone.isBlank()) {
                return CarrierPhoneResolveResult.failed(
                    CarrierAuthProvider.ALIYUN,
                    body.getRequestId(),
                    "EMPTY_MOBILE",
                    "阿里云号码认证未返回手机号"
                );
            }
            return CarrierPhoneResolveResult.success(
                CarrierAuthProvider.ALIYUN,
                phone,
                body.getRequestId(),
                body.getCode(),
                body.getMessage()
            );
        } catch (Exception ex) {
            throw new BusinessException("阿里云号码认证取号失败", ex);
        }
    }

    private Client client() throws Exception {
        if (client == null) {
            ForestCarrierAuthProperties.Aliyun aliyun = properties.getAliyun();
            requireText("阿里云号码认证 AccessKeyId", aliyun.getAccessKeyId());
            requireText("阿里云号码认证 AccessKeySecret", aliyun.getAccessKeySecret());
            requireText("阿里云号码认证 endpoint", aliyun.getEndpoint());
            Config config = new Config()
                .setAccessKeyId(aliyun.getAccessKeyId())
                .setAccessKeySecret(aliyun.getAccessKeySecret());
            config.endpoint = aliyun.getEndpoint();
            client = new Client(config);
        }
        return client;
    }

    private void requireText(String name, String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(name + "不能为空");
        }
    }
}
