package com.forest.payment.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * 支付渠道持久化转换器。
 *
 * <p>兼容历史库中的 WECHAT_JSAPI，同时统一把新值写成 WECHAT_MINIAPP_PAYMENT。</p>
 */
@Converter(autoApply = false)
public class PaymentOrderChannelConverter implements AttributeConverter<PaymentOrderPO.Channel, String> {
    @Override
    public String convertToDatabaseColumn(PaymentOrderPO.Channel attribute) {
        return attribute == null ? null : attribute.getPersistedValue();
    }

    @Override
    public PaymentOrderPO.Channel convertToEntityAttribute(String dbData) {
        return PaymentOrderPO.Channel.fromPersistedValue(dbData);
    }
}
