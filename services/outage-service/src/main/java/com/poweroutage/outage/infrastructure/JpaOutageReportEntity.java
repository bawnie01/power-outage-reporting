package com.poweroutage.outage.infrastructure;

import com.poweroutage.outage.domain.OutageReport;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "outage_report")
public class JpaOutageReportEntity {

    @Id
    private UUID id;

    @Column(name = "report_code", nullable = false, unique = true, length = 30)
    private String reportCode;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private UUID idempotencyKey;

    @Column(name = "request_fingerprint", nullable = false, length = 64)
    private String requestFingerprint;

    @Column(name = "customer_code", nullable = false, length = 30)
    private String customerCode;

    @Column(name = "service_point_code", nullable = false, length = 30)
    private String servicePointCode;

    @Column(name = "reporter_name", nullable = false, length = 200)
    private String reporterName;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(nullable = false, length = 500)
    private String address;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected JpaOutageReportEntity() {
    }

    private JpaOutageReportEntity(OutageReport report) {
        this.id = report.id();
        this.reportCode = report.reportCode();
        this.idempotencyKey = report.idempotencyKey();
        this.requestFingerprint = report.requestFingerprint();
        this.customerCode = report.customerCode();
        this.servicePointCode = report.servicePointCode();
        this.reporterName = report.reporterName();
        this.phoneNumber = report.phoneNumber();
        this.address = report.address();
        this.description = report.description();
        this.status = report.status();
        this.createdAt = report.createdAt();
    }

    public static JpaOutageReportEntity from(OutageReport report) {
        return new JpaOutageReportEntity(report);
    }

    public OutageReport toDomain() {
        return new OutageReport(
                id,
                reportCode,
                idempotencyKey,
                requestFingerprint,
                customerCode,
                servicePointCode,
                reporterName,
                phoneNumber,
                address,
                description,
                status,
                createdAt);
    }
}
