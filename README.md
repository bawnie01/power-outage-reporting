# Power Outage Reporting System

## 1. Project Overview

The Power Outage Reporting System is a mini integration project that allows a customer to submit a power outage report.

After receiving the report, the system stores the information, publishes an event through RabbitMQ, and sends a simulated SMS confirmation through a partner mock service.

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

## 5. Integration Patterns

- Synchronous: the Notification Service calls the SMS Partner Mock using REST.
- Asynchronous: the Outage Service publishes an `outage.reported` event to RabbitMQ.

## 6. Services

- `outage-service`
- `notification-service`
- `sms-partner-mock`

## 7. Main API

### Create an outage report

```http
POST /api/v1/outage-reports
Content-Type: application/json
```

```json
{
  "customerCode": "CUST00001",
  "servicePointCode": "SP00001",
  "reporterName": "Nguyen Van A",
  "phoneNumber": "0901234567",
  "address": "123 Tran Thai Tong, Hanoi",
  "description": "Power outage in the entire house"
}
```

Example response (`201 Created`):

```json
{
  "id": "ee26f857-220c-46e7-8c99-f66ea9a041ef",
  "reportCode": "OUT-20260801-00001",
  "status": "RECEIVED",
  "message": "The power outage report has been received."
}
```

## 8. Partner Mock API

```http
POST /partner/v1/sms-messages
Content-Type: application/json
```

```json
{
  "phoneNumber": "0901234567",
  "templateCode": "OUTAGE_REPORT_RECEIVED",
  "parameters": {
    "reportCode": "OUT-20260801-00001"
  }
}
```

## 9. Technology Stack

- Java 21 and Spring Boot
- PostgreSQL
- RabbitMQ
- Kong API Gateway
- Keycloak
- Docker and Kubernetes
- Argo CD
- Prometheus and Grafana
- GitHub Actions

## 10. Architecture

```text
Customer / Postman
        |
        v
       Kong
        |
        v
  Outage Service ------> PostgreSQL
        |
        v
     RabbitMQ
        |
        v
Notification Service
        |
        v
 SMS Partner Mock
```

## 11. Security

- Keycloak provides authentication and access tokens.
- Kong acts as the API Gateway.
- The backend validates JWT access tokens.
- Secrets are not stored in the source repository.

## 12. Observability

- Prometheus collects application metrics.
- Grafana displays dashboards.
- Spring Boot Actuator exposes health-check endpoints.

## 13. Documentation

- [Business workflow](docs/business-workflow.md)
- [Architecture](docs/architecture.md)
- [Integration patterns](docs/integration-pattern.md)
- [Security design](docs/security.md)
- [Technical stack](docs/technical-stack.md)
- [OpenAPI specification](docs/openapi.yaml)

## 14. Run the Current Version Locally

Requirements:

- Java 21
- Docker with Docker Compose

Start PostgreSQL from the repository root:

```powershell
docker compose up -d postgres
```

Start the Outage Service:

```powershell
cd services/outage-service
.\mvnw.cmd spring-boot:run
```

Check its health:

```powershell
curl.exe http://localhost:8080/actuator/health
```

Submit a report:

```powershell
curl.exe -X POST http://localhost:8080/api/v1/outage-reports `
  -H "Content-Type: application/json" `
  -H "Idempotency-Key: c1203d7d-a58f-45ea-b9ec-469d77b24871" `
  -d '{"customerCode":"CUST00001","servicePointCode":"SP00001","reporterName":"Nguyen Van A","phoneNumber":"0901234567","address":"123 Tran Thai Tong, Hanoi","description":"Power outage in the entire house"}'
```

The credentials in `compose.yaml` are for local development only.

## 15. Project Status

- [x] Define the business domain
- [x] Define the business workflow
- [x] Define the integration patterns
- [x] Define the technical stack
- [x] Complete the OpenAPI specification
- [x] Implement the Outage Service with PostgreSQL persistence
- [ ] Implement the SMS Partner Mock
- [ ] Add RabbitMQ and the Notification Service
- [ ] Add Kong and Keycloak
- [ ] Deploy to Kubernetes using GitOps
- [ ] Add Prometheus and Grafana
