package com.forest.notification.sms.service;

import com.forest.starter.time.ForestTime;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

/**
 * Generates SMS business numbers.
 */
@Component
public class SmsNumberGenerator {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    public String nextSmsNo() {
        String date = ForestTime.now().format(DATE_FORMAT);
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase(Locale.ROOT);
        return "SMS" + date + random;
    }
}
