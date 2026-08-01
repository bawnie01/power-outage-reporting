package com.poweroutage.outage.messaging;

import java.util.UUID;

public record OutageReportedData(
        UUID reportId,
        String reportCode,
        String phoneNumber,
        String status) {
}
