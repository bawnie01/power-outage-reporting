# Local Deployment

## Prerequisites

- Docker Desktop or Rancher Desktop
- Docker Compose v2

Java and Maven are not required when the entire platform runs in containers.

## Start the Platform

From the repository root, run:

```powershell
docker compose up --build -d
```

This starts:

| Component | Address |
|---|---|
| Outage Service | `http://localhost:8080` |
| SMS Partner Mock | `http://localhost:8081` |
| Notification Service | `http://localhost:8082` |
| PostgreSQL | `localhost:5432` |
| RabbitMQ | `localhost:5672` |
| RabbitMQ Management | `http://localhost:15672` |

Local RabbitMQ credentials:

```text
Username: outage_user
Password: outage_password
```

These credentials are for local development only.

## Verify Container Health

```powershell
docker compose ps
```

All five containers should display `healthy`.

## Test the Complete Workflow

```powershell
curl.exe -X POST http://localhost:8080/api/v1/outage-reports `
  -H "Content-Type: application/json" `
  -H "Idempotency-Key: c1203d7d-a58f-45ea-b9ec-469d77b24871" `
  -H "X-Correlation-Id: a7a5a3ee-1a43-40aa-a285-ed13bc822612" `
  -d '{"customerCode":"CUST00001","servicePointCode":"SP00001","reporterName":"Nguyen Van A","phoneNumber":"0901234567","address":"123 Tran Thai Tong, Hanoi","description":"Power outage in the entire house"}'
```

Expected result:

```text
HTTP 201 Created
status: RECEIVED
```

Verify the notification:

```powershell
docker compose logs notification-service
```

Look for:

```text
SMS notification accepted
```

## Stop the Platform

Stop containers while retaining database and RabbitMQ data:

```powershell
docker compose down
```

To also delete local database and broker data:

```powershell
docker compose down --volumes
```
