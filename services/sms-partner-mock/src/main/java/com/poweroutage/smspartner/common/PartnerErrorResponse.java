package com.poweroutage.smspartner.common;

import java.time.OffsetDateTime;
import java.util.List;

public record PartnerErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String code,
        String message,
        List<FieldErrorDetail> details,
        String path) {
}
