# API Gateway

## Purpose

Kong is the only public entry point for the Outage Report API. The Outage
Service remains reachable by other containers, but its port is not published
to the host machine.

## Local Addresses

| Interface | Address | Purpose |
|---|---|---|
| Kong proxy | `http://localhost:8000` | Public application API |
| Kong Admin API | `http://localhost:8001` | Local inspection only |

The Admin API is bound to `127.0.0.1` and must not be exposed publicly.

## Route

```text
POST /api/v1/outage-reports -> http://outage-service:8080/api/v1/outage-reports
```

Kong preserves the complete request path because `strip_path` is disabled.

## Enabled Plugins

| Plugin | Configuration | Purpose |
|---|---|---|
| Correlation ID | `X-Correlation-Id` | Generates an identifier when the client does not provide one and returns it in the response. |
| Rate limiting | 30 requests per minute | Protects the API from excessive traffic in the demonstration environment. |
| Request size limiting | 1 MB | Rejects unexpectedly large request bodies. |
| CORS | Selected request and response headers | Allows browser clients to call the API in a future extension. |
| Prometheus | Default metrics | Exposes Kong request metrics for the monitoring milestone. |

## Authentication Responsibility

Kong routes and protects traffic at the gateway level. Keycloak issues the
access token, and the Outage Service validates the JWT signature, issuer, and
roles. This design works with the open-source Kong distribution and keeps the
identity and gateway responsibilities clear.

## DB-less Configuration

Kong runs without a database. Its complete desired configuration is stored in
`infrastructure/kong/kong.yml`, so routes and plugins are version-controlled in
Git and can later be deployed using GitOps.

## Verification

Inspect the loaded route:

```powershell
Invoke-RestMethod http://localhost:8001/routes
```

Inspect gateway metrics:

```powershell
curl.exe http://localhost:8001/metrics
```

Calling the public API without a token should return `401 Unauthorized`:

```powershell
curl.exe -i -X POST http://localhost:8000/api/v1/outage-reports `
  -H "Content-Type: application/json" `
  -H "Idempotency-Key: gateway-no-token-test" `
  -d '{"customerCode":"CUST00001","servicePointCode":"SP00001","reporterName":"Nguyen Van A","phoneNumber":"0901234567","address":"Hanoi"}'
```
