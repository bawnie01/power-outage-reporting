package com.poweroutage.smspartner.application;

import com.poweroutage.smspartner.api.SendSmsRequest;
import com.poweroutage.smspartner.common.InvalidMockScenarioException;
import com.poweroutage.smspartner.common.PartnerUnavailableException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SmsMessageServiceTests {

    private final SendSmsRequest request = new SendSmsRequest(
            "0901234567",
            "OUTAGE_REPORT_RECEIVED",
            Map.of("reportCode", "OUT-20260801-00001"));

    private final SmsMessageService service = new SmsMessageService(
            1,
            Clock.fixed(Instant.parse("2026-08-01T02:30:05Z"), ZoneOffset.UTC));

    @Test
    void successScenarioReturnsAcceptedResponse() {
        var response = service.send(request, "success");

        assertEquals("SMS-20260801-00001", response.messageId());
        assertEquals("ACCEPTED", response.status());
    }

    @Test
    void serverErrorScenarioReturnsPartnerFailure() {
        assertThrows(PartnerUnavailableException.class,
                () -> service.send(request, "server-error"));
    }

    @Test
    void unknownScenarioIsRejected() {
        assertThrows(InvalidMockScenarioException.class,
                () -> service.send(request, "unknown"));
    }
}
