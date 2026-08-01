package com.poweroutage.notification.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConsumerConfiguration {

    public static final String EVENTS_EXCHANGE = "power.outage.events";
    public static final String DEAD_LETTER_EXCHANGE = "power.outage.dlx";
    public static final String NOTIFICATION_QUEUE = "notification.outage-reported.queue";
    public static final String DEAD_LETTER_QUEUE = "notification.outage-reported.dlq";
    public static final String OUTAGE_REPORTED_KEY = "outage.reported";
    public static final String DEAD_LETTER_KEY = "notification.outage-reported.dead";

    @Bean
    TopicExchange outageEventsExchange() {
        return new TopicExchange(EVENTS_EXCHANGE, true, false);
    }

    @Bean
    TopicExchange deadLetterExchange() {
        return new TopicExchange(DEAD_LETTER_EXCHANGE, true, false);
    }

    @Bean
    Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE)
                .deadLetterExchange(DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(DEAD_LETTER_KEY)
                .build();
    }

    @Bean
    Queue notificationDeadLetterQueue() {
        return QueueBuilder.durable(DEAD_LETTER_QUEUE).build();
    }

    @Bean
    Binding notificationBinding(Queue notificationQueue, TopicExchange outageEventsExchange) {
        return BindingBuilder.bind(notificationQueue)
                .to(outageEventsExchange)
                .with(OUTAGE_REPORTED_KEY);
    }

    @Bean
    Binding deadLetterBinding(Queue notificationDeadLetterQueue, TopicExchange deadLetterExchange) {
        return BindingBuilder.bind(notificationDeadLetterQueue)
                .to(deadLetterExchange)
                .with(DEAD_LETTER_KEY);
    }

    @Bean
    JacksonJsonMessageConverter rabbitMessageConverter() {
        return new JacksonJsonMessageConverter("com.poweroutage.notification.messaging");
    }
}
