package com.forest.notification.sms.service;

import java.util.Map;

/**
 * Command for sending and recording one SMS message.
 */
public record SmsSendCommand(
    String businessAppCode,
    String clientAppCode,
    String clientType,
    String scene,
    String phone,
    String templateCode,
    String signName,
    Map<String, String> templateParams,
    String contentTemplate,
    String sendIp
) {
}
