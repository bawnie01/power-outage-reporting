package com.poweroutage.outage.api;

import com.poweroutage.outage.application.OutageReportService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/outage-reports")
public class OutageReportController {

    private final OutageReportService service;

    public OutageReportController(OutageReportService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<OutageReportResponse> create(
            @RequestHeader("Idempotency-Key") UUID idempotencyKey,
            @Valid @RequestBody CreateOutageReportRequest request) {
        var report = service.create(idempotencyKey, request);
        return ResponseEntity
                .created(URI.create("/api/v1/outage-reports/" + report.id()))
                .body(OutageReportResponse.from(report));
    }

    @GetMapping("/{reportId}")
    public OutageReportResponse get(@PathVariable UUID reportId) {
        return OutageReportResponse.from(service.get(reportId));
    }

    @GetMapping
    public OutageReportPageResponse list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Page must be non-negative and size must be between 1 and 100.");
        }
        return OutageReportPageResponse.from(service.list(status, page, size));
    }
}
