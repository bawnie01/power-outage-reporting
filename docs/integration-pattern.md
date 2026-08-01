# Integration Patterns

## Asynchronous Integration

The Outage Service stores the report and an outbox record in one PostgreSQL transaction. A scheduled outbox publisher sends pending `outage.reported` events to RabbitMQ and records successful publication. A broker failure leaves the event pending for a later retry.

## Synchronous Integration

The Notification Service calls the SMS Partner Mock through `POST /partner/v1/sms-messages` and waits for the partner response.

## Failure Handling

- SMS partner calls use a two-second connect timeout and a three-second read timeout.
- Failed notification attempts are retried.
- Messages that still fail after the retry limit are moved to a dead-letter queue.
- The Notification Service stores processed `eventId` values in PostgreSQL. Duplicate deliveries are acknowledged without sending another SMS.
- The partner mock requires an API key supplied through environment variables or a Kubernetes Secret.
