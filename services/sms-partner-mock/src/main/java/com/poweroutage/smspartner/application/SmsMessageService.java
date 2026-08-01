package com.poweroutage.smspartner.application;

import com.poweroutage.smspartner.api.SendSmsRequest;
import com.poweroutage.smspartner.api.SendSmsResponse;
import com.poweroutage.smspartner.common.InvalidMockScenarioException;
import com.poweroutage.smspartner.common.PartnerUnavailableException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class SmsMessageService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;

    private final long timeoutDelayMs;
    private final Clock clock;
    private final AtomicLong sequence = new AtomicLong();

    @Autowired
    public SmsMessageService(@Value("${sms.mock.timeout-delay-ms}") long timeoutDelayMs) {
        this(timeoutDelayMs, Clock.system(ZoneOffset.UTC));
    }

    SmsMessageService(long timeoutDelayMs, Clock clock) {
        this.timeoutDelayMs = timeoutDelayMs;
        this.clock = clock;
    }

    public SendSmsResponse send(SendSmsRequest request, String scenario) {
        return switch (scenario.toLowerCase(Locale.ROOT)) {
            case "success" -> accepted();
            case "timeout" -> timeoutThenAccept();
            case "server-error" -> throw new PartnerUnavailableException();
            default -> throw new InvalidMockScenarioException(scenario);
        };
    }

    private SendSmsResponse timeoutThenAccept() {
        try {
            Thread.sleep(timeoutDelayMs);
            return accepted();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new PartnerUnavailableException();
        }
    }

    private SendSmsResponse accepted() {
        String messageId = "SMS-%s-%05d".formatted(
                LocalDate.now(clock).format(DATE_FORMAT),
                sequence.incrementAndGet());
        return new SendSmsResponse(messageId, "ACCEPTED", OffsetDateTime.now(clock));
    }
}
