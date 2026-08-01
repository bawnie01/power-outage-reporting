package com.poweroutage.outage.application;

import com.poweroutage.outage.api.CreateOutageReportRequest;
import com.poweroutage.outage.common.IdempotencyConflictException;
import com.poweroutage.outage.common.OutageReportNotFoundException;
import com.poweroutage.outage.domain.OutageReport;
import com.poweroutage.outage.domain.OutageReportRepository;
import com.poweroutage.outage.messaging.OutboxEventRepository;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OutageReportServiceTests {

    private final OutageReportRepository repository = mock(OutageReportRepository.class);
    private final OutboxEventRepository outboxRepository = mock(OutboxEventRepository.class);
    private final OutageReportService service = new OutageReportService(
            repository,
            outboxRepository,
            new ObjectMapper(),
            Clock.fixed(Instant.parse("2026-08-01T02:30:00Z"), ZoneOffset.UTC));

    @Test
    void rejectsReusedIdempotencyKeyWithDifferentPayload() {
        UUID key = UUID.randomUUID();
        when(repository.findByIdempotencyKey(key)).thenReturn(Optional.of(existingReport(key)));

        assertThrows(IdempotencyConflictException.class, () -> service.create(key, request()));
    }

    @Test
    void returnsNotFoundForUnknownReport() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(OutageReportNotFoundException.class, () -> service.get(id));
    }

    private OutageReport existingReport(UUID key) {
        return new OutageReport(
                UUID.randomUUID(), "OUT-20260801-00001", key, "different-fingerprint",
                "CUST00001", "SP00001", "Nguyen Van A", "0901234567",
                "123 Tran Thai Tong, Hanoi", "Existing report", "RECEIVED",
                OffsetDateTime.parse("2026-08-01T02:30:00Z"));
    }

    private CreateOutageReportRequest request() {
        return new CreateOutageReportRequest(
                "CUST00001", "SP00001", "Nguyen Van A", "0901234567",
                "123 Tran Thai Tong, Hanoi", "Power outage in the entire house");
    }
}
