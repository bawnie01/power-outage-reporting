package com.poweroutage.outage.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface OutageReportRepository {

    Optional<OutageReport> findByIdempotencyKey(UUID idempotencyKey);

    Optional<OutageReport> findById(UUID id);

    Page<OutageReport> findAll(String status, Pageable pageable);

    long nextReportSequence();

    OutageReport save(OutageReport report);
}
