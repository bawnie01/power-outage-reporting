package com.poweroutage.outage.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface SpringDataOutageReportRepository extends JpaRepository<JpaOutageReportEntity, UUID> {

    Optional<JpaOutageReportEntity> findByIdempotencyKey(UUID idempotencyKey);
}
