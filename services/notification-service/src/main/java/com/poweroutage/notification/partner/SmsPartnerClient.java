package com.poweroutage.notification.partner;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.time.Duration;

@Component
public class SmsPartnerClient {

    private final RestClient restClient;
    private final String mockScenario;
    private final String partnerApiKey;

    public SmsPartnerClient(
            @Value("${sms.partner.base-url}") String baseUrl,
            @Value("${sms.partner.mock-scenario}") String mockScenario,
            @Value("${sms.partner.api-key}") String partnerApiKey,
            @Value("${sms.partner.connect-timeout}") Duration connectTimeout,
            @Value("${sms.partner.read-timeout}") Duration readTimeout) {
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
        this.mockScenario = mockScenario;
        this.partnerApiKey = partnerApiKey;
    }

    public SendSmsResponse send(SendSmsRequest request, String correlationId) {
        return restClient.post()
                .uri("/partner/v1/sms-messages")
                .header("X-Mock-Scenario", mockScenario)
                .header("X-Correlation-Id", correlationId)
                .header("X-Partner-Api-Key", partnerApiKey)
                .body(request)
                .retrieve()
                .body(SendSmsResponse.class);
    }
}
