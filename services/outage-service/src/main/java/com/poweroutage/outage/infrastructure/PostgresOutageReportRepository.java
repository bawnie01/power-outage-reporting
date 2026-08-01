package com.poweroutage.outage.infrastructure;

import com.poweroutage.outage.domain.OutageReport;
import com.poweroutage.outage.domain.OutageReportRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.Optional;
import java.util.UUID;

@Repository
public class PostgresOutageReportRepository implements OutageReportRepository {

    private final SpringDataOutageReportRepository repository;
    private final JdbcClient jdbcClient;

    public PostgresOutageReportRepository(
            SpringDataOutageReportRepository repository,
            JdbcClient jdbcClient) {
        this.repository = repository;
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Optional<OutageReport> findByIdempotencyKey(UUID idempotencyKey) {
        return repository.findByIdempotencyKey(idempotencyKey)
                .map(JpaOutageReportEntity::toDomain);
    }

    @Override
    public Optional<OutageReport> findById(UUID id) {
        return repository.findById(id).map(JpaOutageReportEntity::toDomain);
    }

    @Override
    public Page<OutageReport> findAll(String status, Pageable pageable) {
        Page<JpaOutageReportEntity> result = status == null
                ? repository.findAll(pageable)
                : repository.findByStatus(status, pageable);
        return result.map(JpaOutageReportEntity::toDomain);
    }

    @Override
    public long nextReportSequence() {
        return jdbcClient.sql("SELECT nextval('outage_report_code_seq')")
                .query(Long.class)
                .single();
    }

    @Override
    public OutageReport save(OutageReport report) {
        return repository.save(JpaOutageReportEntity.from(report)).toDomain();
    }
}
