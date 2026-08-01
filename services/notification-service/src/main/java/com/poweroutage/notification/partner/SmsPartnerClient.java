package com.poweroutage.notification.partner;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class SmsPartnerClient {

    private final RestClient restClient;
    private final String mockScenario;

    public SmsPartnerClient(
            @Value("${sms.partner.base-url}") String baseUrl,
            @Value("${sms.partner.mock-scenario}") String mockScenario) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.mockScenario = mockScenario;
    }

    public SendSmsResponse send(SendSmsRequest request, String correlationId) {
        return restClient.post()
                .uri("/partner/v1/sms-messages")
                .header("X-Mock-Scenario", mockScenario)
                .header("X-Correlation-Id", correlationId)
                .body(request)
                .retrieve()
                .body(SendSmsResponse.class);
    }
}
