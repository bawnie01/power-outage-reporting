package com.poweroutage.notification.messaging;

import java.time.OffsetDateTime;
import java.util.UUID;

public record OutageReportedEvent(
        UUID eventId,
        String eventType,
        String eventVersion,
        OffsetDateTime occurredAt,
        String producer,
        OutageReportedData data) {
}
