package com.poweroutage.outage.common;

import java.time.OffsetDateTime;
import java.util.List;

public record ErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String code,
        String message,
        List<ValidationErrorDetail> details,
        String path,
        String correlationId) {
}
