package com.poweroutage.notification.messaging;

import com.poweroutage.notification.partner.SendSmsRequest;
import com.poweroutage.notification.partner.SmsPartnerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Component
public class OutageReportedConsumer {

    private static final Logger log = LoggerFactory.getLogger(OutageReportedConsumer.class);

    private final SmsPartnerClient smsPartnerClient;
    private final ProcessedEventRepository processedEventRepository;

    public OutageReportedConsumer(
            SmsPartnerClient smsPartnerClient,
            ProcessedEventRepository processedEventRepository) {
        this.smsPartnerClient = smsPartnerClient;
        this.processedEventRepository = processedEventRepository;
    }

    @RabbitListener(queues = RabbitConsumerConfiguration.NOTIFICATION_QUEUE)
    @Transactional
    public void consume(OutageReportedEvent event) {
        if (processedEventRepository.exists(event.eventId())) {
            log.info("Duplicate outage event ignored: eventId={}, reportCode={}",
                    event.eventId(), event.data().reportCode());
            return;
        }
        var response = smsPartnerClient.send(
                new SendSmsRequest(
                        event.data().phoneNumber(),
                        "OUTAGE_REPORT_RECEIVED",
                        Map.of("reportCode", event.data().reportCode())),
                event.eventId().toString());
        processedEventRepository.save(
                event.eventId(), event.data().reportCode(), response.messageId());
        log.info("SMS notification accepted: eventId={}, reportCode={}, messageId={}",
                event.eventId(), event.data().reportCode(), response.messageId());
    }
}
