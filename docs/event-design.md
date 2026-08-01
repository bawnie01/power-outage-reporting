# Event Design

## Outage Reported Event

The Outage Service writes this event to a transactional outbox in the same database transaction as the outage report.

| Property | Value |
|---|---|
| Exchange | `power.outage.events` |
| Exchange type | Topic |
| Routing key | `outage.reported` |
| Consumer queue | `notification.outage-reported.queue` |
| Dead-letter exchange | `power.outage.dlx` |
| Dead-letter queue | `notification.outage-reported.dlq` |

## Event Envelope

```json
{
  "eventId": "aa97a1d9-d717-48ba-b28f-a78ad4975759",
  "eventType": "outage.reported",
  "eventVersion": "1.0",
  "occurredAt": "2026-08-01T09:30:00+07:00",
  "producer": "outage-service",
  "data": {
    "reportId": "ee26f857-220c-46e7-8c99-f66ea9a041ef",
    "reportCode": "OUT-20260801-00001",
    "phoneNumber": "0901234567",
    "status": "RECEIVED"
  }
}
```

## Processing Flow

1. The Outage Service stores the report in PostgreSQL.
2. The transaction commits the report and its outbox event atomically.
3. The outbox publisher sends pending `outage.reported` events and marks successful publications.
4. RabbitMQ routes the event to the notification queue.
5. The Notification Service consumes the event.
6. The Notification Service calls the SMS Partner Mock.
7. The message is acknowledged when the partner accepts the request.

## Failure Handling

- Notification processing is attempted a maximum of three times.
- Retry delays begin at one second and use a multiplier of two.
- A message that still fails is rejected without requeueing.
- RabbitMQ routes the rejected message to `notification.outage-reported.dlq`.
- Operators can inspect the dead-letter queue through RabbitMQ Management.

## Delivery Semantics

RabbitMQ and the outbox publisher provide at-least-once delivery. A crash after publishing but before marking the outbox row can produce a duplicate. The Notification Service therefore stores each successfully processed `eventId` in `processed_notification_event`. A duplicate delivery is acknowledged without calling the SMS partner again.
