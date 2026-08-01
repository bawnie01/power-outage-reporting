package com.poweroutage.smspartner.api;

import com.poweroutage.smspartner.application.SmsMessageService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/partner/v1/sms-messages")
public class SmsMessageController {

    private final SmsMessageService service;

    public SmsMessageController(SmsMessageService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<SendSmsResponse> send(
            @RequestHeader(name = "X-Mock-Scenario", defaultValue = "success") String scenario,
            @RequestHeader(name = "X-Correlation-Id", required = false) String correlationId,
            @Valid @RequestBody SendSmsRequest request) {
        String resolvedCorrelationId = correlationId == null || correlationId.isBlank()
                ? UUID.randomUUID().toString()
                : correlationId;
        return ResponseEntity.accepted()
                .header("X-Correlation-Id", resolvedCorrelationId)
                .body(service.send(request, scenario));
    }
}
