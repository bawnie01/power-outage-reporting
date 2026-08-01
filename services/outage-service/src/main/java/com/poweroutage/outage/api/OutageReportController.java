package com.poweroutage.outage.api;

import com.poweroutage.outage.application.OutageReportService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
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
}
