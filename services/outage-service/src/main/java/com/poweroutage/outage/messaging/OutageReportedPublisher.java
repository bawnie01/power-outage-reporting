package com.poweroutage.outage.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class OutageReportedPublisher {

    private final RabbitTemplate rabbitTemplate;

    public OutageReportedPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(OutageReportedEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitPublisherConfiguration.EVENTS_EXCHANGE,
                RabbitPublisherConfiguration.OUTAGE_REPORTED_KEY,
                event);
    }
}
