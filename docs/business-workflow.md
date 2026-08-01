# Business Workflow

## Workflow Name

Power Outage Report Submission

## Actor

Customer

## Preconditions

- The customer has a valid customer code.
- The customer has a valid service point code.
- The customer provides a valid phone number.

## Main Flow

1. The customer submits a power outage report.
2. The Outage Service validates the request.
3. The Outage Service stores the report in PostgreSQL.
4. The Outage Service publishes an `outage.reported` event.
5. RabbitMQ delivers the event to the Notification Service.
6. The Notification Service calls the SMS Partner Mock.
7. The SMS Partner Mock returns an accepted result.
8. The notification result is logged.

## Alternative Flow

If the request is invalid:

1. The system rejects the request.
2. The system returns HTTP status `400 Bad Request`.

If the SMS Partner Mock is unavailable:

1. The Notification Service retries the request.
2. After the maximum retries, the message is moved to a dead-letter queue.
