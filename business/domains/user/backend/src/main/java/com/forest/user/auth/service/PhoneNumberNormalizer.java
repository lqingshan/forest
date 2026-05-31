package com.forest.user.auth.service;

import com.forest.starter.exception.BusinessException;
import org.springframework.stereotype.Component;

/**
 * 规范化中国大陆手机号。
 */
@Component
public class PhoneNumberNormalizer {
    public String normalizeChinaPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new BusinessException("手机号不能为空");
        }
        String value = phone.trim().replace(" ", "").replace("-", "");
        if (value.startsWith("+86")) {
            value = value.substring(3);
        } else if (value.startsWith("86") && value.length() == 13) {
            value = value.substring(2);
        }
        if (!value.matches("1\\d{10}")) {
            throw new BusinessException("手机号格式不正确");
        }
        return "+86" + value;
    }

    public String mask(String normalizedPhone) {
        if (normalizedPhone == null || normalizedPhone.length() < 8) {
            return normalizedPhone;
        }
        return normalizedPhone.substring(0, 5) + "****" + normalizedPhone.substring(normalizedPhone.length() - 4);
    }
}
