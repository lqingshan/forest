package com.forest.notification.sms.service;

import com.forest.starter.sms.config.ForestSmsProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Renders SMS content snapshots according to configured audit mode.
 */
@Component
public class SmsSendLogContentRenderer {
    public String render(String template, Map<String, String> params, ForestSmsProperties.Log log) {
        ForestSmsProperties.ContentMode mode = log.getContentMode();
        if (mode == ForestSmsProperties.ContentMode.NONE) {
            return null;
        }
        String content = renderPlain(template, params);
        if (mode == ForestSmsProperties.ContentMode.PLAIN) {
            return content;
        }
        String code = params == null ? null : params.get("code");
        if (code == null || code.isBlank()) {
            return content;
        }
        String masked = mode == ForestSmsProperties.ContentMode.MASKED
            ? "*".repeat(code.length())
            : maskPartial(code, log.getMaskVisibleTail());
        return content.replace(code, masked);
    }

    private String renderPlain(String template, Map<String, String> params) {
        String content = template == null || template.isBlank() ? "" : template;
        if (params == null || params.isEmpty()) {
            return content;
        }
        for (Map.Entry<String, String> entry : params.entrySet()) {
            content = content.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return content;
    }

    private String maskPartial(String value, int visibleTail) {
        int safeTail = Math.max(0, Math.min(visibleTail, value.length()));
        int maskLength = value.length() - safeTail;
        if (maskLength <= 0) {
            return value;
        }
        return "*".repeat(maskLength) + value.substring(maskLength);
    }
}
