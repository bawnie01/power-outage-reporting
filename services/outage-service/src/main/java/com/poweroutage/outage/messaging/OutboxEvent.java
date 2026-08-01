package com.poweroutage.outage.messaging;

import java.time.OffsetDateTime;
import java.util.UUID;

public record OutboxEvent(
        UUID eventId,
        UUID aggregateId,
        String eventType,
        String payload,
        OffsetDateTime occurredAt) {
}
