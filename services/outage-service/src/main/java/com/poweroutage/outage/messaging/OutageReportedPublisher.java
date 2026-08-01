package com.poweroutage.outage.messaging;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OutageReportedPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutageReportedPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final OutboxEventRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public OutageReportedPublisher(
            RabbitTemplate rabbitTemplate,
            OutboxEventRepository outboxRepository,
            ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelayString = "${outbox.publish-interval:2s}")
    public void publishPending() {
        for (OutboxEvent outboxEvent : outboxRepository.findPending(20)) {
            try {
                OutageReportedEvent event = objectMapper.readValue(
                        outboxEvent.payload(), OutageReportedEvent.class);
                rabbitTemplate.convertAndSend(
                        RabbitPublisherConfiguration.EVENTS_EXCHANGE,
                        RabbitPublisherConfiguration.OUTAGE_REPORTED_KEY,
                        event);
                outboxRepository.markPublished(outboxEvent.eventId());
            } catch (JacksonException exception) {
                outboxRepository.markFailed(outboxEvent.eventId(), exception.getMessage());
                log.error("Invalid outbox payload: eventId={}", outboxEvent.eventId(), exception);
            } catch (RuntimeException exception) {
                outboxRepository.markFailed(outboxEvent.eventId(), exception.getMessage());
                log.warn("Outbox publish will be retried: eventId={}", outboxEvent.eventId(), exception);
            }
        }
    }
}
