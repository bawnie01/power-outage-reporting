# Power Outage Reporting System

## 1. Project Overview

The Power Outage Reporting System is a mini integration project that allows a customer to submit a power outage report.

After receiving the report, the system stores the information, publishes an event through RabbitMQ, and sends a simulated SMS confirmation through a partner mock service.

This project uses simulated data only and does not connect to real EVN systems.

## Related Repositories

This project is delivered through two repositories:

| Repository | Purpose |
|---|---|
| [power-outage-reporting](https://github.com/bawnie01/power-outage-reporting) | Application source code, API contracts, documentation, Docker Compose, Kubernetes manifests, and CI workflow |
| [power-outage-reporting-gitops](https://github.com/bawnie01/power-outage-reporting-gitops) | GitOps desired state, immutable image tags, and the Argo CD Application |

Start with this repository for the design and implementation. Use the GitOps
repository to review how Argo CD deploys the tested application images.

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
- [Local kind and Argo CD lab](docs/local-gitops-lab.md)

GitOps repository: [bawnie01/power-outage-reporting-gitops](https://github.com/bawnie01/power-outage-reporting-gitops)

## 14. Run the Current Version Locally

This is the recommended path for first-time users. Java and Maven are not
required because Docker builds and runs every component.

### 14.1 Prerequisites

Install and start:

- [Git](https://git-scm.com/downloads)
- [Docker Desktop](https://www.docker.com/products/docker-desktop/)

Docker Desktop must show **Engine running**. Verify the installation in
PowerShell:

```powershell
git --version
docker --version
docker compose version
```

### 14.2 Download the project

```powershell
git clone https://github.com/bawnie01/power-outage-reporting.git
cd power-outage-reporting
```

If the repository was downloaded as a ZIP file, extract it, open PowerShell in
the extracted `power-outage-reporting` directory, and continue below.

### 14.3 Start the complete platform

```powershell
docker compose up --build -d
```

The first build may take several minutes because Docker downloads base images
and Java dependencies. Wait until all services become healthy:

```powershell
docker compose ps
```

The platform is ready when the ten containers are running and the services
with health checks display `healthy`.

### 14.4 Open the customer interface

Open this address in a browser:

```text
http://localhost:3000
```

The page signs in automatically with the local demo operator account through
Keycloak. Wait for **Hệ thống hoạt động** in the top-right corner.

To test the business workflow:

1. Enter the customer code, reporter name, phone number, address, and outage description.
2. Select **Gửi báo mất điện**.
3. Wait for the generated report code, such as `OUT-20260801-00001`.
4. Find the report under **Lịch sử báo mất điện**.
5. Select **Xem chi tiết** to view the registered customer information.
6. Open **Giám sát hệ thống** in the navigation bar to view Grafana.

The customer does not need to enter a service-point code. For this mini project,
the interface automatically uses the customer code as the internal service-point
identifier required by the API contract.

### 14.5 Local service addresses

| Interface | Address | Local credentials |
|---|---|---|
| Customer web interface | `http://localhost:3000` | Automatic demo login |
| Grafana dashboard | `http://localhost:3001/d/power-outage-overview/power-outage-platform-overview` | `admin` / `admin` |
| Prometheus targets | `http://localhost:9090/targets` | None |
| Kong API Gateway | `http://localhost:8000` | JWT required |
| RabbitMQ Management | `http://localhost:15672` | `outage_user` / `outage_password` |
| Keycloak Admin | `http://localhost:8083` | `admin` / `admin` |
| SMS Partner Mock | `http://localhost:8081` | API key used internally |
| Notification health | `http://localhost:8082/actuator/health` | None |

Local demo users imported into Keycloak:

| User | Password | Role |
|---|---|---|
| `customer01` | `customer123` | `CUSTOMER` |
| `operator01` | `operator123` | `OPERATOR` |

All credentials are for local development only.

### 14.6 Verify the complete integration flow

After submitting a report from the interface, verify the notification consumer:

```powershell
docker compose logs --tail 50 notification-service
```

Look for:

```text
SMS notification accepted
```

The successful flow is:

```text
Browser UI -> Keycloak -> Kong -> Outage Service -> PostgreSQL
                                           |
                                           v
                                        RabbitMQ
                                           |
                                           v
                              Notification Service -> SMS Partner Mock
```

### 14.7 Stop, restart, or update

Stop the platform while retaining local reports and RabbitMQ data:

```powershell
docker compose down
```

Start it again:

```powershell
docker compose up -d
```

Download the latest source and rebuild:

```powershell
git pull
docker compose up --build -d
```

To reset all local project data and start with an empty database, run the
following only when the stored reports are no longer needed:

```powershell
docker compose down --volumes
docker compose up --build -d
```

### 14.8 Troubleshooting

**The page shows `Không thể đăng nhập Keycloak`:**

```powershell
docker compose ps
docker compose logs --tail 100 keycloak demo-ui
docker compose up --build -d
```

Then reload `http://localhost:3000` with `Ctrl+F5`.

**A container is not healthy:**

```powershell
docker compose logs --tail 100 <service-name>
```

Replace `<service-name>` with `outage-service`, `keycloak`, `kong`,
`notification-service`, `sms-partner-mock`, `postgres`, or `rabbitmq`.

**A port is already in use:** stop the other program using ports `3000`, `5432`,
`8000`, `8001`, `8081`, `8082`, `8083`, `5672`, or `15672`, then run
`docker compose up -d` again.

**The browser displays an old interface:** press `Ctrl+F5`, or open a private
browsing window at `http://localhost:3000`.

For API-level testing, operational verification, and shutdown details, see the
[local deployment guide](docs/local-deployment.md).

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
- [x] Install Argo CD on a disposable kind cluster and synchronize the application
- [x] Verify the Kubernetes workflow end to end through Kong and Keycloak
- [x] Add Prometheus and Grafana with a provisioned platform dashboard
- [x] Add transactional outbox and persistent consumer idempotency
- [x] Add report retrieval, pagination, and status filtering
- [x] Add EVNICT mapping, architecture review, and Postman collection
