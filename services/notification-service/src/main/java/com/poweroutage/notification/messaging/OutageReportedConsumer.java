package com.poweroutage.notification.messaging;

import com.poweroutage.notification.partner.SendSmsRequest;
import com.poweroutage.notification.partner.SmsPartnerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OutageReportedConsumer {

    private static final Logger log = LoggerFactory.getLogger(OutageReportedConsumer.class);

    private final SmsPartnerClient smsPartnerClient;

    public OutageReportedConsumer(SmsPartnerClient smsPartnerClient) {
        this.smsPartnerClient = smsPartnerClient;
    }

    @RabbitListener(queues = RabbitConsumerConfiguration.NOTIFICATION_QUEUE)
    public void consume(OutageReportedEvent event) {
        var response = smsPartnerClient.send(
                new SendSmsRequest(
                        event.data().phoneNumber(),
                        "OUTAGE_REPORT_RECEIVED",
                        Map.of("reportCode", event.data().reportCode())),
                event.eventId().toString());
        log.info("SMS notification accepted: eventId={}, reportCode={}, messageId={}",
                event.eventId(), event.data().reportCode(), response.messageId());
    }
}
