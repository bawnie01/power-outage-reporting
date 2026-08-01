package com.poweroutage.notification.messaging;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.UUID;

@Repository
public class ProcessedEventRepository {

    private final JdbcClient jdbcClient;

    public ProcessedEventRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public boolean exists(UUID eventId) {
        return jdbcClient.sql("SELECT EXISTS (SELECT 1 FROM processed_notification_event WHERE event_id = :eventId)")
                .param("eventId", eventId)
                .query(Boolean.class)
                .single();
    }

    public void save(UUID eventId, String reportCode, String partnerMessageId) {
        jdbcClient.sql("""
                        INSERT INTO processed_notification_event
                            (event_id, report_code, partner_message_id, processed_at)
                        VALUES (:eventId, :reportCode, :partnerMessageId, :processedAt)
                        """)
                .param("eventId", eventId)
                .param("reportCode", reportCode)
                .param("partnerMessageId", partnerMessageId)
                .param("processedAt", OffsetDateTime.now())
                .update();
    }
}
