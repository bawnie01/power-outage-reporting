package com.poweroutage.outage.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public record OutageReport(
        UUID id,
        String reportCode,
        UUID idempotencyKey,
        String requestFingerprint,
        String customerCode,
        String servicePointCode,
        String reporterName,
        String phoneNumber,
        String address,
        String description,
        String status,
        OffsetDateTime createdAt) {
}
