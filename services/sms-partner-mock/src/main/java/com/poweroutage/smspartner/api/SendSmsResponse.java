package com.poweroutage.smspartner.api;

import java.time.OffsetDateTime;

public record SendSmsResponse(
        String messageId,
        String status,
        OffsetDateTime acceptedAt) {
}
