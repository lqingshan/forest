package com.forest.payment.channel.wechat;

import com.forest.payment.channel.PaymentChannelGateway;
import com.forest.payment.config.WechatPayProperties;
import com.forest.starter.exception.BusinessException;
import com.forest.starter.json.ForestObjectMappers;
import com.forest.starter.time.ForestTime;
import com.wechat.pay.java.core.RSAPublicKeyConfig;
import com.wechat.pay.java.core.exception.WechatPayException;
import com.wechat.pay.java.core.http.DefaultHttpClientBuilder;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.jsapi.model.Amount;
import com.wechat.pay.java.service.payments.jsapi.model.Payer;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;
import com.wechat.pay.java.service.payments.model.Transaction;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * 实现微信支付渠道适配。
 *
 * <p>该类是 payment 域的外部渠道适配器，负责微信小程序支付下单、支付参数生成和微信回调验签。</p>
 */
@Component
public class WechatPayGateway implements PaymentChannelGateway {
    private static final String SIGN_TYPE_RSA = "RSA";
    private static final String CURRENCY_CNY = "CNY";

    private final WechatPayProperties properties;
    private final JsonMapper objectMapper;
    private volatile RSAPublicKeyConfig realConfig;
    private volatile JsapiServiceExtension jsapiServiceExtension;
    private volatile NotificationParser notificationParser;

    public WechatPayGateway(WechatPayProperties properties, JsonMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = ForestObjectMappers.copyForHttpClient(objectMapper);
    }

    @Override
    public WechatMiniappPaymentResult createWechatMiniappPaymentOrder(WechatMiniappPaymentRequest request) {
        if (properties.isMockEnabled()) {
            // 本地 mock 模式返回固定可识别签名，便于小程序绕过真实收银台测试后端到账链路。
            String prepayId = "mock-prepay-" + UUID.randomUUID().toString().replace("-", "");
            String nonce = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
            return new WechatMiniappPaymentResult(
                prepayId,
                String.valueOf(System.currentTimeMillis() / 1000),
                nonce,
                "prepay_id=" + prepayId,
                "RSA",
                "mock-pay-sign"
            );
        }

        if (!properties.isEnabled()) {
            throw new BusinessException("微信支付未启用");
        }

        try {
            // SDK 返回的参数可以直接传给 wx.requestPayment，不在业务层重新签名。
            PrepayWithRequestPaymentResponse response = getJsapiServiceExtension()
                .prepayWithRequestPayment(buildPrepayRequest(request));
            return new WechatMiniappPaymentResult(
                extractPrepayId(response.getPackageVal()),
                response.getTimeStamp(),
                response.getNonceStr(),
                response.getPackageVal(),
                response.getSignType(),
                response.getPaySign()
            );
        } catch (BusinessException ex) {
            throw ex;
        } catch (WechatPayException ex) {
            throw new BusinessException("微信支付下单失败: " + firstNonBlank(ex.getMessage(), "未知错误"));
        } catch (RuntimeException ex) {
            throw new BusinessException("微信支付下单失败: " + firstNonBlank(ex.getMessage(), "未知错误"));
        }
    }

    @Override
    public WechatPayNotifyResult parseAndVerifyNotify(String requestBody, Map<String, String> headers) {
        if (properties.isMockEnabled()) {
            try {
                // mock 回调只服务本地联调；真实模式必须走微信支付验签与解密。
                MockWechatNotifyRequest request = objectMapper.readValue(requestBody, MockWechatNotifyRequest.class);
                LocalDateTime paidTime = request.paidTime() == null ? ForestTime.now() : request.paidTime();
                String transactionId = request.transactionId() == null || request.transactionId().isBlank()
                    ? "mock-transaction-" + UUID.randomUUID().toString().replace("-", "")
                    : request.transactionId();
                return new WechatPayNotifyResult(
                    request.outTradeNo(),
                    transactionId,
                    request.amountCents(),
                    paidTime
                );
            } catch (JacksonException ex) {
                throw new BusinessException("微信支付回调解析失败");
            }
        }

        if (!properties.isEnabled()) {
            throw new BusinessException("微信支付未启用");
        }

        try {
            // 公钥模式下 SDK 使用微信支付公钥校验通知签名，并使用 APIv3 密钥解密资源报文。
            Transaction transaction = getNotificationParser().parse(buildRequestParam(requestBody, headers), Transaction.class);
            if (transaction.getTradeState() != Transaction.TradeStateEnum.SUCCESS) {
                throw new BusinessException("微信支付交易未成功");
            }
            if (transaction.getAmount() == null || transaction.getAmount().getTotal() == null) {
                throw new BusinessException("微信支付回调缺少金额信息");
            }
            return new WechatPayNotifyResult(
                transaction.getOutTradeNo(),
                transaction.getTransactionId(),
                transaction.getAmount().getTotal(),
                parsePaidTime(transaction.getSuccessTime())
            );
        } catch (WechatPayException ex) {
            throw new BusinessException("微信支付回调验签失败: " + firstNonBlank(ex.getMessage(), "未知错误"));
        } catch (RuntimeException ex) {
            if (ex instanceof BusinessException businessException) {
                throw businessException;
            }
            throw new BusinessException("微信支付回调处理失败: " + firstNonBlank(ex.getMessage(), "未知错误"));
        }
    }

    private synchronized JsapiServiceExtension getJsapiServiceExtension() {
        initializeRealClientsIfNecessary();
        return jsapiServiceExtension;
    }

    private synchronized NotificationParser getNotificationParser() {
        initializeRealClientsIfNecessary();
        return notificationParser;
    }

    private void initializeRealClientsIfNecessary() {
        if (realConfig != null && jsapiServiceExtension != null && notificationParser != null) {
            return;
        }

        validateRealModeConfig();
        int timeoutMs = properties.getTimeoutSeconds() == null ? 10_000 : properties.getTimeoutSeconds() * 1000;
        // privateKeyPath/publicKeyPath 是应用运行时路径；Docker 场景下通常是容器内 /run/secrets。
        RSAPublicKeyConfig config = new RSAPublicKeyConfig.Builder()
            .merchantId(properties.getMchId())
            .merchantSerialNumber(properties.getMerchantSerialNumber())
            .privateKeyFromPath(properties.getPrivateKeyPath())
            .apiV3Key(properties.getApiV3Key())
            .publicKeyId(properties.getPublicKeyId())
            .publicKeyFromPath(properties.getPublicKeyPath())
            .build();

        this.realConfig = config;
        this.jsapiServiceExtension = new JsapiServiceExtension.Builder()
            .config(config)
            .httpClient(new DefaultHttpClientBuilder()
                .config(config)
                .connectTimeoutMs(timeoutMs)
                .readTimeoutMs(timeoutMs)
                .writeTimeoutMs(timeoutMs)
                .build())
            .build();
        this.notificationParser = new NotificationParser(config);
    }

    private void validateRealModeConfig() {
        List<String> missingFields = new ArrayList<>();
        requireProperty(properties.getMchId(), "wechat.pay.mch-id", missingFields);
        requireProperty(properties.getAppid(), "wechat.pay.appid", missingFields);
        requireProperty(properties.getMerchantSerialNumber(), "wechat.pay.merchant-serial-number", missingFields);
        requireProperty(properties.getApiV3Key(), "wechat.pay.api-v3-key", missingFields);
        requireProperty(properties.getPrivateKeyPath(), "wechat.pay.private-key-path", missingFields);
        requireProperty(properties.getPublicKeyId(), "wechat.pay.public-key-id", missingFields);
        requireProperty(properties.getPublicKeyPath(), "wechat.pay.public-key-path", missingFields);
        requireProperty(properties.getNotifyUrl(), "wechat.pay.notify-url", missingFields);
        if (!missingFields.isEmpty()) {
            throw new BusinessException("微信支付配置不完整: " + String.join(", ", missingFields));
        }
    }

    private void requireProperty(String value, String fieldName, List<String> missingFields) {
        if (!StringUtils.hasText(value)) {
            missingFields.add(fieldName);
        }
    }

    private PrepayRequest buildPrepayRequest(WechatMiniappPaymentRequest request) {
        PrepayRequest prepayRequest = new PrepayRequest();
        prepayRequest.setAppid(properties.getAppid());
        prepayRequest.setMchid(properties.getMchId());
        prepayRequest.setDescription(request.description());
        prepayRequest.setOutTradeNo(request.outTradeNo());
        prepayRequest.setNotifyUrl(request.notifyUrl());

        Amount amount = new Amount();
        amount.setTotal(request.amountCents());
        amount.setCurrency(CURRENCY_CNY);
        prepayRequest.setAmount(amount);

        Payer payer = new Payer();
        payer.setOpenid(request.openId());
        prepayRequest.setPayer(payer);
        return prepayRequest;
    }

    private RequestParam buildRequestParam(String requestBody, Map<String, String> headers) {
        // 微信支付回调请求头大小写可能被网关改写，取值时按大小写不敏感处理。
        return new RequestParam.Builder()
            .serialNumber(requiredHeader(headers, "Wechatpay-Serial"))
            .timestamp(requiredHeader(headers, "Wechatpay-Timestamp"))
            .nonce(requiredHeader(headers, "Wechatpay-Nonce"))
            .signature(requiredHeader(headers, "Wechatpay-Signature"))
            .signType(headerValue(headers, "Wechatpay-Signature-Type").orElse(SIGN_TYPE_RSA))
            .body(requestBody)
            .build();
    }

    private String requiredHeader(Map<String, String> headers, String name) {
        return headerValue(headers, name)
            .filter(StringUtils::hasText)
            .orElseThrow(() -> new BusinessException("微信支付回调缺少请求头: " + name));
    }

    private Optional<String> headerValue(Map<String, String> headers, String name) {
        String exactMatch = headers.get(name);
        if (StringUtils.hasText(exactMatch)) {
            return Optional.of(exactMatch);
        }
        return headers.entrySet().stream()
            .filter(entry -> name.equalsIgnoreCase(entry.getKey()))
            .map(Map.Entry::getValue)
            .filter(StringUtils::hasText)
            .findFirst();
    }

    private String extractPrepayId(String packageValue) {
        if (!StringUtils.hasText(packageValue)) {
            throw new BusinessException("微信支付返回的预支付参数为空");
        }
        String prefix = "prepay_id=";
        return packageValue.startsWith(prefix) ? packageValue.substring(prefix.length()) : packageValue;
    }

    LocalDateTime parsePaidTime(String successTime) {
        if (!StringUtils.hasText(successTime)) {
            return ForestTime.now();
        }
        try {
            return ForestTime.fromOffsetDateTime(OffsetDateTime.parse(successTime));
        } catch (DateTimeParseException ex) {
            throw new BusinessException("微信支付回调成功时间格式不正确");
        }
    }

    private String firstNonBlank(String first, String fallback) {
        return StringUtils.hasText(first) ? first : fallback;
    }

    private record MockWechatNotifyRequest(
        String outTradeNo,
        String transactionId,
        Integer amountCents,
        LocalDateTime paidTime
    ) {
    }
}
