package com.poweroutage.outage.application;

import com.poweroutage.outage.api.CreateOutageReportRequest;
import com.poweroutage.outage.common.IdempotencyConflictException;
import com.poweroutage.outage.domain.OutageReport;
import com.poweroutage.outage.domain.OutageReportRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class OutageReportService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;

    private final OutageReportRepository repository;
    private final Clock clock;
    private final AtomicLong sequence = new AtomicLong();

    public OutageReportService(OutageReportRepository repository) {
        this(repository, Clock.system(ZoneOffset.UTC));
    }

    OutageReportService(OutageReportRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public OutageReport create(UUID idempotencyKey, CreateOutageReportRequest request) {
        String fingerprint = Integer.toHexString(Objects.hash(
                request.customerCode(),
                request.servicePointCode(),
                request.reporterName(),
                request.phoneNumber(),
                request.address(),
                request.description()));

        return repository.findByIdempotencyKey(idempotencyKey)
                .map(existing -> {
                    if (!existing.requestFingerprint().equals(fingerprint)) {
                        throw new IdempotencyConflictException();
                    }
                    return existing;
                })
                .orElseGet(() -> repository.save(new OutageReport(
                        UUID.randomUUID(),
                        nextReportCode(),
                        idempotencyKey,
                        fingerprint,
                        request.customerCode(),
                        request.servicePointCode(),
                        request.reporterName(),
                        request.phoneNumber(),
                        request.address(),
                        request.description(),
                        "RECEIVED",
                        OffsetDateTime.now(clock))));
    }

    private String nextReportCode() {
        return "OUT-%s-%05d".formatted(
                LocalDate.now(clock).format(DATE_FORMAT),
                sequence.incrementAndGet());
    }
}
