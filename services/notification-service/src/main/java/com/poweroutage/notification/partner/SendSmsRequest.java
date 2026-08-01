package com.poweroutage.notification.partner;

import java.util.Map;

public record SendSmsRequest(
        String phoneNumber,
        String templateCode,
        Map<String, String> parameters) {
}
