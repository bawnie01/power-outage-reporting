package com.poweroutage.outage.infrastructure;

import com.poweroutage.outage.domain.OutageReport;
import com.poweroutage.outage.domain.OutageReportRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class InMemoryOutageReportRepository implements OutageReportRepository {

    private final ConcurrentMap<UUID, OutageReport> reportsByIdempotencyKey = new ConcurrentHashMap<>();

    @Override
    public Optional<OutageReport> findByIdempotencyKey(UUID idempotencyKey) {
        return Optional.ofNullable(reportsByIdempotencyKey.get(idempotencyKey));
    }

    @Override
    public OutageReport save(OutageReport report) {
        reportsByIdempotencyKey.put(report.idempotencyKey(), report);
        return report;
    }
}
