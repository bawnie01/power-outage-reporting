package com.poweroutage.outage.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

interface SpringDataOutageReportRepository extends JpaRepository<JpaOutageReportEntity, UUID> {

    Optional<JpaOutageReportEntity> findByIdempotencyKey(UUID idempotencyKey);

    Page<JpaOutageReportEntity> findByStatus(String status, Pageable pageable);
}
