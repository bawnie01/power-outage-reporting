package com.poweroutage.outage.domain;

import java.util.Optional;
import java.util.UUID;

public interface OutageReportRepository {

    Optional<OutageReport> findByIdempotencyKey(UUID idempotencyKey);

    OutageReport save(OutageReport report);
}
