# power-outage-reporting
Description: A mini project for reporting power outages and sending simulated SMS notifications.
# Power Outage Reporting System

## 1. Project Overview

The Power Outage Reporting System is a mini integration project that allows a
customer to submit a power outage report.

After receiving the report, the system stores the information, publishes an
event through RabbitMQ, and sends a simulated SMS confirmation through a
partner mock service.

This project uses simulated data only and does not connect to real EVN systems.

## 2. Business Domain

Electric utility customer service.

## 3. Main Business Function

Submit a power outage report and send an SMS confirmation.

## 4. Business Workflow

1. A customer submits a power outage report.
2. The system validates the request.
3. The outage report is stored in PostgreSQL.
4. The system publishes an `outage.reported` event to RabbitMQ.
5. The Notification Service consumes the event.
6. The Notification Service calls the SMS Partner Mock.
7. The customer receives a simulated SMS confirmation.

## 5. Integration Pattern

The project uses two integration patterns:

### Synchronous integration

The Notification Service calls the SMS Partner Mock using a REST API.

### Asynchronous integration

The Outage Service publishes an `outage.reported` event to RabbitMQ.

The Notification Service consumes the event and processes the notification.

## 6. Services

- `outage-service`
- `notification-service`
- `sms-partner-mock`

## 7. Main API

### Create an outage report

```http
POST /api/v1/outage-reports
{
  "customerCode": "CUST00001",
  "servicePointCode": "SP00001",
  "reporterName": "Nguyen Van A",
  "phoneNumber": "0901234567",
  "address": "123 Tran Thai Tong, Hanoi",
  "description": "Power outage in the entire house"
}

{
  "id": "ee26f857-220c-46e7-8c99-f66ea9a041ef",
  "reportCode": "OUT-20260801-00001",
  "status": "RECEIVED",
  "message": "The power outage report has been received."
}

POST /partner/v1/sms-messages
{
  "phoneNumber": "0901234567",
  "templateCode": "OUTAGE_REPORT_RECEIVED",
  "parameters": {
    "reportCode": "OUT-20260801-00001"
  }
}
9. Technology Stack
Java 21
Spring Boot
PostgreSQL
RabbitMQ
Kong API Gateway
Keycloak
Docker
Kubernetes
Argo CD
Prometheus
Grafana
GitHub Actions
10. Architecture
Customer / Postman
        |
        v
       Kong
        |
        v
  Outage Service
        |
        +------> PostgreSQL
        |
        v
     RabbitMQ
        |
        v
Notification Service
        |
        v
 SMS Partner Mock
11. Security
Keycloak provides authentication and access tokens.
Kong acts as the API Gateway.
The backend validates JWT access tokens.
Secrets are not stored in the source repository.
12. Observability
Prometheus collects application metrics.
Grafana displays dashboards.
Health-check endpoints are exposed by Spring Boot Actuator.
