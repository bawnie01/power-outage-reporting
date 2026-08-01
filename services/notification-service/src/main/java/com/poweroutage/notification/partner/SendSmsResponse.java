package com.poweroutage.notification.partner;

import java.time.OffsetDateTime;

public record SendSmsResponse(
        String messageId,
        String status,
        OffsetDateTime acceptedAt) {
}
