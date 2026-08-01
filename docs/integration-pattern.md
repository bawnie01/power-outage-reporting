# Integration Patterns

## Asynchronous Integration

After an outage report is stored, the Outage Service publishes an `outage.reported` event to RabbitMQ. The Notification Service consumes this event independently.

## Synchronous Integration

The Notification Service calls the SMS Partner Mock through `POST /partner/v1/sms-messages` and waits for the partner response.

## Failure Handling

- Requests to the SMS partner use a timeout.
- Failed notification attempts are retried.
- Messages that still fail after the retry limit are moved to a dead-letter queue.
- Consumers use the event identifier to prevent duplicate processing.
