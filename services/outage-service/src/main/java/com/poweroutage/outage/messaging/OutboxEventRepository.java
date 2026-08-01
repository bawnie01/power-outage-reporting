package com.poweroutage.outage.messaging;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public class OutboxEventRepository {

    private final JdbcClient jdbcClient;

    public OutboxEventRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public void save(OutboxEvent event) {
        jdbcClient.sql("""
                        INSERT INTO outbox_event
                            (event_id, aggregate_id, event_type, payload, occurred_at)
                        VALUES (:eventId, :aggregateId, :eventType, CAST(:payload AS jsonb), :occurredAt)
                        """)
                .param("eventId", event.eventId())
                .param("aggregateId", event.aggregateId())
                .param("eventType", event.eventType())
                .param("payload", event.payload())
                .param("occurredAt", event.occurredAt())
                .update();
    }

    public List<OutboxEvent> findPending(int limit) {
        return jdbcClient.sql("""
                        SELECT event_id, aggregate_id, event_type, payload::text, occurred_at
                        FROM outbox_event
                        WHERE published_at IS NULL
                        ORDER BY occurred_at
                        LIMIT :limit
                        """)
                .param("limit", limit)
                .query((rs, rowNum) -> new OutboxEvent(
                        rs.getObject("event_id", UUID.class),
                        rs.getObject("aggregate_id", UUID.class),
                        rs.getString("event_type"),
                        rs.getString("payload"),
                        rs.getObject("occurred_at", OffsetDateTime.class)))
                .list();
    }

    public void markPublished(UUID eventId) {
        jdbcClient.sql("""
                        UPDATE outbox_event
                        SET published_at = :publishedAt, publish_attempts = publish_attempts + 1, last_error = NULL
                        WHERE event_id = :eventId
                        """)
                .param("eventId", eventId)
                .param("publishedAt", OffsetDateTime.now())
                .update();
    }

    public void markFailed(UUID eventId, String error) {
        jdbcClient.sql("""
                        UPDATE outbox_event
                        SET publish_attempts = publish_attempts + 1, last_error = :error
                        WHERE event_id = :eventId
                        """)
                .param("eventId", eventId)
                .param("error", error == null ? "Unknown publish error" : error.substring(0, Math.min(error.length(), 1000)))
                .update();
    }
}
