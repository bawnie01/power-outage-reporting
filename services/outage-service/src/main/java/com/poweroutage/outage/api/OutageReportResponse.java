package com.poweroutage.outage.api;

import com.poweroutage.outage.domain.OutageReport;

import java.time.OffsetDateTime;
import java.util.UUID;

public record OutageReportResponse(
        UUID id,
        String reportCode,
        String status,
        String message,
        OffsetDateTime createdAt) {

    public static OutageReportResponse from(OutageReport report) {
        return new OutageReportResponse(
                report.id(),
                report.reportCode(),
                report.status(),
                "The power outage report has been received.",
                report.createdAt());
    }
}
