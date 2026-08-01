# Architecture

## 1. Purpose

This document describes the architecture of the Power Outage Reporting System. The system implements one business function: accepting a power outage report and sending a simulated SMS confirmation.

The project uses test data only and does not connect to real EVN systems.

## 2. System Context

```mermaid
flowchart LR
    Customer["Customer / Operator"]
    System["Power Outage Reporting System"]
    Keycloak["Keycloak"]
    SmsPartner["SMS Partner Mock"]

    Customer -->|"Submit outage report"| System
    Customer -->|"Request access token"| Keycloak
    Keycloak -->|"JWT access token"| Customer
    System -->|"Send SMS request"| SmsPartner
    SmsPartner -->|"Accepted or error response"| System
```

### External actors and systems

| Actor or system | Responsibility |
|---|---|
| Customer | Submits a power outage report. |
| Operator | May submit a report on behalf of a customer. |
| Keycloak | Authenticates users and issues JWT access tokens. |
| SMS Partner Mock | Simulates an external SMS gateway. |

## 3. Container View

```mermaid
flowchart LR
    Client["Postman / API Client"]
    Keycloak["Keycloak"]
    Kong["Kong API Gateway"]
    Outage["Outage Service"]
    Database[("PostgreSQL")]
    Rabbit[("RabbitMQ")]
    Notification["Notification Service"]
    Sms["SMS Partner Mock"]

    Client -->|"1. Authenticate"| Keycloak
    Keycloak -->|"2. JWT token"| Client
    Client -->|"3. POST /api/v1/outage-reports"| Kong
    Kong -->|"4. Route request"| Outage
    Outage -->|"5. Store report"| Database
    Outage -->|"6. Publish outage.reported"| Rabbit
    Rabbit -->|"7. Deliver event"| Notification
    Notification -->|"8. POST /partner/v1/sms-messages"| Sms
```

## 4. Component Responsibilities

### Kong API Gateway

- Exposes the public API endpoint.
- Routes requests to the Outage Service.
- Adds or forwards the correlation identifier.
- Applies rate limiting and request-size limits.
- Exposes gateway metrics to Prometheus.

### Outage Service

- Validates the outage report request.
- Validates the JWT and required user role.
- Enforces idempotency using the `Idempotency-Key` header.
- Stores the outage report in PostgreSQL.
- Returns `201 Created` to the client.
- Publishes an `outage.reported` event to RabbitMQ.

### PostgreSQL

- Stores outage reports.
- Stores idempotency keys to prevent duplicate reports.
- Is owned logically by the Outage Service.

### RabbitMQ

- Provides asynchronous communication between the Outage Service and Notification Service.
- Routes the `outage.reported` event to the notification queue.
- Moves messages to a dead-letter queue after the retry limit is reached.

### Notification Service

- Consumes `outage.reported` events.
- Prevents duplicate event processing.
- Calls the SMS Partner Mock through REST.
- Applies timeout and retry rules.
- Records notification success or failure in logs and metrics.

### SMS Partner Mock

- Simulates an external partner API.
- Supports success, timeout, and server-error scenarios.
- Returns a simulated SMS message identifier.

### Keycloak

- Authenticates users.
- Issues JWT access tokens.
- Provides the `CUSTOMER`, `OPERATOR`, and `ADMIN` roles.

## 5. Main Processing Sequence

```mermaid
sequenceDiagram
    actor Customer
    participant Keycloak
    participant Kong
    participant Outage as Outage Service
    participant DB as PostgreSQL
    participant MQ as RabbitMQ
    participant Notification as Notification Service
    participant SMS as SMS Partner Mock

    Customer->>Keycloak: Request access token
    Keycloak-->>Customer: JWT access token
    Customer->>Kong: POST /api/v1/outage-reports
    Kong->>Outage: Forward authenticated request
    Outage->>Outage: Validate request and idempotency key
    Outage->>DB: Insert outage report
    DB-->>Outage: Report stored
    Outage->>MQ: Publish outage.reported
    Outage-->>Kong: 201 Created
    Kong-->>Customer: 201 Created
    MQ-->>Notification: Deliver outage.reported
    Notification->>SMS: POST /partner/v1/sms-messages
    SMS-->>Notification: 202 Accepted
```

## 6. Communication Patterns

| Source | Destination | Protocol | Pattern | Purpose |
|---|---|---|---|---|
| Client | Kong | HTTPS/REST | Synchronous | Submit an outage report. |
| Kong | Outage Service | HTTP/REST | Synchronous | Route the API request. |
| Outage Service | PostgreSQL | TCP/SQL | Synchronous | Store the report. |
| Outage Service | RabbitMQ | AMQP | Asynchronous | Publish the outage event. |
| RabbitMQ | Notification Service | AMQP | Asynchronous | Deliver the outage event. |
| Notification Service | SMS Partner Mock | HTTP/REST | Synchronous | Request an SMS notification. |

## 7. Deployment View

```mermaid
flowchart TB
    subgraph Cluster["Kubernetes Cluster"]
        subgraph AppNamespace["power-outage namespace"]
            Kong["Kong"]
            Outage["Outage Service"]
            Notification["Notification Service"]
            Sms["SMS Partner Mock"]
        end

        subgraph InfraNamespace["power-outage-infra namespace"]
            PostgreSQL[("PostgreSQL")]
            RabbitMQ[("RabbitMQ")]
            Keycloak["Keycloak"]
        end

        subgraph MonitoringNamespace["monitoring namespace"]
            Prometheus["Prometheus"]
            Grafana["Grafana"]
        end

        Outage --> PostgreSQL
        Outage --> RabbitMQ
        RabbitMQ --> Notification
        Notification --> Sms
        Kong --> Outage
        Prometheus -.-> Kong
        Prometheus -.-> Outage
        Prometheus -.-> Notification
        Grafana --> Prometheus
    end

    ArgoCD["Argo CD"] -->|"Synchronize desired state"| Cluster
```

The first implementation will run locally before Kubernetes is introduced. Kubernetes and Argo CD are later project milestones.

## 8. Security Boundaries

- Public clients can access the application only through Kong.
- Keycloak issues tokens; application services validate them.
- PostgreSQL and RabbitMQ are not publicly exposed.
- Only the Notification Service calls the SMS Partner Mock.
- Passwords and keys are supplied through environment variables or Kubernetes Secrets.

## 9. Observability

- All services expose health and Prometheus metrics through Spring Boot Actuator.
- Kong exposes gateway request metrics.
- Logs include the service name and correlation identifier.
- Grafana displays request rate, error rate, latency, outage report count, and notification results.

## 10. Key Design Decisions

| Decision | Reason |
|---|---|
| Keep one business workflow | Makes the project suitable for a beginner and a short final-course demonstration. |
| Use RabbitMQ | Lightweight and suitable for event delivery, retry, and dead-letter queues. |
| Use an SMS partner mock | Demonstrates partner integration without requiring a real external account. |
| Validate JWT in the service | Works with Kong OSS and clearly separates gateway and identity responsibilities. |
| Use an idempotency key | Prevents duplicate reports when a client retries a request. |
| Build locally before Kubernetes | Allows each application function to be tested before platform complexity is added. |

## 11. Out of Scope

- Real EVN customer and grid data.
- Real SMS delivery.
- Incident dispatch and repair workflows.
- SCADA, GIS, OMS, or CMIS integration.
- A frontend web or mobile application.
