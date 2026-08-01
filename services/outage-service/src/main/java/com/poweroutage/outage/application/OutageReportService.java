package com.poweroutage.outage.application;

import com.poweroutage.outage.api.CreateOutageReportRequest;
import com.poweroutage.outage.common.IdempotencyConflictException;
import com.poweroutage.outage.common.OutageReportNotFoundException;
import com.poweroutage.outage.domain.OutageReport;
import com.poweroutage.outage.domain.OutageReportRepository;
import com.poweroutage.outage.messaging.OutageReportedData;
import com.poweroutage.outage.messaging.OutageReportedEvent;
import com.poweroutage.outage.messaging.OutboxEvent;
import com.poweroutage.outage.messaging.OutboxEventRepository;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

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

@Service
public class OutageReportService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;

    private final OutageReportRepository repository;
    private final OutboxEventRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Autowired
    public OutageReportService(
            OutageReportRepository repository,
            OutboxEventRepository outboxRepository,
            ObjectMapper objectMapper) {
        this(repository, outboxRepository, objectMapper, Clock.system(ZoneOffset.UTC));
    }

    OutageReportService(
            OutageReportRepository repository,
            OutboxEventRepository outboxRepository,
            ObjectMapper objectMapper,
            Clock clock) {
        this.repository = repository;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
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

    @Transactional(readOnly = true)
    public OutageReport get(UUID id) {
        return repository.findById(id).orElseThrow(() -> new OutageReportNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Page<OutageReport> list(String status, int page, int size) {
        return repository.findAll(
                status,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
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
        OutageReportedEvent event = new OutageReportedEvent(
                UUID.randomUUID(),
                "outage.reported",
                "1.0",
                OffsetDateTime.now(clock),
                "outage-service",
                new OutageReportedData(
                        report.id(),
                        report.reportCode(),
                        report.phoneNumber(),
                        report.status()));
        try {
            outboxRepository.save(new OutboxEvent(
                    event.eventId(),
                    report.id(),
                    event.eventType(),
                    objectMapper.writeValueAsString(event),
                    event.occurredAt()));
        } catch (JacksonException exception) {
            throw new IllegalStateException("Could not serialize the outage event", exception);
        }
        return report;
    }

    private String nextReportCode() {
        return "OUT-%s-%05d".formatted(
                LocalDate.now(clock).format(DATE_FORMAT),
                repository.nextReportSequence());
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
