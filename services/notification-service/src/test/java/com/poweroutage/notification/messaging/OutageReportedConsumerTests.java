package com.poweroutage.notification.messaging;

import com.poweroutage.notification.partner.SmsPartnerClient;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OutageReportedConsumerTests {

    @Test
    void ignoresAnAlreadyProcessedEvent() {
        SmsPartnerClient partnerClient = mock(SmsPartnerClient.class);
        ProcessedEventRepository processedEvents = mock(ProcessedEventRepository.class);
        OutageReportedConsumer consumer = new OutageReportedConsumer(partnerClient, processedEvents);
        UUID eventId = UUID.randomUUID();
        when(processedEvents.exists(eventId)).thenReturn(true);

        consumer.consume(new OutageReportedEvent(
                eventId,
                "outage.reported",
                "1.0",
                OffsetDateTime.now(),
                "outage-service",
                new OutageReportedData(UUID.randomUUID(), "OUT-20260801-00001", "0901234567", "RECEIVED")));

        verify(partnerClient, never()).send(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }
}
