package com.poweroutage.outage.application;

import com.poweroutage.outage.api.CreateOutageReportRequest;
import com.poweroutage.outage.common.IdempotencyConflictException;
import com.poweroutage.outage.domain.OutageReport;
import com.poweroutage.outage.domain.OutageReportRepository;
import com.poweroutage.outage.messaging.OutageReportedData;
import com.poweroutage.outage.messaging.OutageReportedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class OutageReportService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;

    private final OutageReportRepository repository;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;
    private final AtomicLong sequence = new AtomicLong();

    @Autowired
    public OutageReportService(
            OutageReportRepository repository,
            ApplicationEventPublisher eventPublisher) {
        this(repository, eventPublisher, Clock.system(ZoneOffset.UTC));
    }

    OutageReportService(
            OutageReportRepository repository,
            ApplicationEventPublisher eventPublisher,
            Clock clock) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    @Transactional
    public OutageReport create(UUID idempotencyKey, CreateOutageReportRequest request) {
        String fingerprint = fingerprint(request);

        return repository.findByIdempotencyKey(idempotencyKey)
                .map(existing -> {
                    if (!existing.requestFingerprint().equals(fingerprint)) {
                        throw new IdempotencyConflictException();
                    }
                    return existing;
                })
                .orElseGet(() -> createAndPublish(idempotencyKey, fingerprint, request));
    }

    private OutageReport createAndPublish(
            UUID idempotencyKey,
            String fingerprint,
            CreateOutageReportRequest request) {
        OutageReport report = repository.save(new OutageReport(
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
                        OffsetDateTime.now(clock)));
        eventPublisher.publishEvent(new OutageReportedEvent(
                UUID.randomUUID(),
                "outage.reported",
                "1.0",
                OffsetDateTime.now(clock),
                "outage-service",
                new OutageReportedData(
                        report.id(),
                        report.reportCode(),
                        report.phoneNumber(),
                        report.status())));
        return report;
    }

    private String nextReportCode() {
        return "OUT-%s-%05d".formatted(
                LocalDate.now(clock).format(DATE_FORMAT),
                sequence.incrementAndGet());
    }

    private String fingerprint(CreateOutageReportRequest request) {
        String canonicalRequest = String.join("\u001f",
                request.customerCode(),
                request.servicePointCode(),
                request.reporterName(),
                request.phoneNumber(),
                request.address(),
                request.description() == null ? "" : request.description());
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(canonicalRequest.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
