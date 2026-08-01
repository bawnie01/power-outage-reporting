package com.poweroutage.outage.messaging;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitPublisherConfiguration {

    public static final String EVENTS_EXCHANGE = "power.outage.events";
    public static final String OUTAGE_REPORTED_KEY = "outage.reported";

    @Bean
    TopicExchange outageEventsExchange() {
        return new TopicExchange(EVENTS_EXCHANGE, true, false);
    }

    @Bean
    JacksonJsonMessageConverter rabbitMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
