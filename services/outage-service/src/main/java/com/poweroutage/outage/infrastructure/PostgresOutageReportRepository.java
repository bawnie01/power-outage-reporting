package com.poweroutage.outage.infrastructure;

import com.poweroutage.outage.domain.OutageReport;
import com.poweroutage.outage.domain.OutageReportRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class PostgresOutageReportRepository implements OutageReportRepository {

    private final SpringDataOutageReportRepository repository;

    public PostgresOutageReportRepository(SpringDataOutageReportRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<OutageReport> findByIdempotencyKey(UUID idempotencyKey) {
        return repository.findByIdempotencyKey(idempotencyKey)
                .map(JpaOutageReportEntity::toDomain);
    }

    @Override
    public OutageReport save(OutageReport report) {
        return repository.save(JpaOutageReportEntity.from(report)).toDomain();
    }
}
