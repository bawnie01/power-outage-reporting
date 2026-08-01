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
- [Event design](docs/event-design.md)
- [Security design](docs/security.md)
- [Technical stack](docs/technical-stack.md)
- [OpenAPI specification](docs/openapi.yaml)
- [SMS Partner Mock OpenAPI specification](docs/sms-partner-openapi.yaml)
- [Local deployment guide](docs/local-deployment.md)
- [API Gateway design](docs/api-gateway.md)
- [Kubernetes deployment guide](docs/kubernetes-deployment.md)
- [Continuous integration and container images](docs/continuous-integration.md)
- [GitOps delivery](docs/gitops.md)

GitOps repository: [bawnie01/power-outage-reporting-gitops](https://github.com/bawnie01/power-outage-reporting-gitops)

## 14. Run the Current Version Locally

The simplest option requires only Docker with Docker Compose:

```powershell
docker compose up --build -d
```

This starts all three application services, PostgreSQL, RabbitMQ, Keycloak, and Kong. See the
[local deployment guide](docs/local-deployment.md) for verification and shutdown steps.

To run the Java services outside containers, the requirements are:

- Java 21
- Docker with Docker Compose

Start PostgreSQL from the repository root:

```powershell
docker compose up -d postgres
```

Start RabbitMQ:

```powershell
docker compose up -d rabbitmq
```

RabbitMQ Management is available at `http://localhost:15672` with the local
development username `outage_user` and password `outage_password`.

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

First obtain a token using the instructions in the
[local deployment guide](docs/local-deployment.md), then run:

```powershell
curl.exe -X POST http://localhost:8000/api/v1/outage-reports `
  -H "Authorization: Bearer $accessToken" `
  -H "Content-Type: application/json" `
  -H "Idempotency-Key: c1203d7d-a58f-45ea-b9ec-469d77b24871" `
  -d '{"customerCode":"CUST00001","servicePointCode":"SP00001","reporterName":"Nguyen Van A","phoneNumber":"0901234567","address":"123 Tran Thai Tong, Hanoi","description":"Power outage in the entire house"}'
```

The credentials in `compose.yaml` are for local development only.

Start the SMS Partner Mock in a second terminal:

```powershell
cd services/sms-partner-mock
.\mvnw.cmd spring-boot:run
```

The partner mock runs at `http://localhost:8081`. Select its behavior with the
`X-Mock-Scenario` header:

```text
success       Returns 202 Accepted immediately
timeout       Waits five seconds before returning 202 Accepted
server-error  Returns 503 Service Unavailable
```

Example success request:

```powershell
curl.exe -X POST http://localhost:8081/partner/v1/sms-messages `
  -H "Content-Type: application/json" `
  -H "X-Mock-Scenario: success" `
  -d '{"phoneNumber":"0901234567","templateCode":"OUTAGE_REPORT_RECEIVED","parameters":{"reportCode":"OUT-20260801-00001"}}'
```

Start the Notification Service in a third terminal:

```powershell
cd services/notification-service
.\mvnw.cmd spring-boot:run
```

The complete local flow is now:

```text
Outage Service -> RabbitMQ -> Notification Service -> SMS Partner Mock
```

## 15. Project Status

- [x] Define the business domain
- [x] Define the business workflow
- [x] Define the integration patterns
- [x] Define the technical stack
- [x] Complete the OpenAPI specification
- [x] Implement the Outage Service with PostgreSQL persistence
- [x] Implement the SMS Partner Mock
- [x] Add RabbitMQ and the Notification Service
- [x] Containerize all services and run the complete platform with Docker Compose
- [x] Add Keycloak authentication and role-based authorization
- [x] Add Kong API Gateway
- [x] Add Kubernetes manifests with Kustomize
- [x] Add CI testing and container image publishing to GHCR
- [x] Create a separate GitOps repository and Argo CD Application
- [ ] Install Argo CD on a disposable learning cluster and synchronize the application
- [ ] Add Prometheus and Grafana
