# Test Plan

## Automated Tests

GitHub Actions runs `mvn verify` for every service before any image is published.

| Test area | Scenario | Expected result |
|---|---|---|
| Validation | Missing or malformed request property | HTTP 400 with standard error body |
| Authentication | Missing, expired, or invalid JWT | HTTP 401 |
| Authorization | CUSTOMER calls operator list | HTTP 403 |
| Create | Valid report and unique idempotency key | HTTP 201 and outbox row |
| Idempotency replay | Same key and same body | Existing report returned; no second event |
| Idempotency conflict | Same key and different body | HTTP 409 |
| Detail query | Existing/unknown ID | HTTP 200 / 404 |
| List query | Page, size, and status filter | Bounded paginated response |
| Outbox | RabbitMQ unavailable, then restored | Event remains pending, then publishes |
| Consumer dedupe | Deliver same event twice | One SMS call and one processed-event row |
| Partner authentication | Missing or wrong API key | HTTP 401 |
| Partner timeout | Mock delay exceeds read timeout | Three attempts, then DLQ |
| Partner server error | Mock returns 503 | Three attempts, then DLQ |
| Observability | Application receives requests | Prometheus targets UP and dashboard data visible |
| GitOps | Desired image tag changes | Argo CD rolls out and becomes Synced/Healthy |

## Postman / Newman

Import `docs/postman/power-outage-reporting.postman_collection.json`. Run requests in numbered order. The collection obtains a token, creates and reads a report, validates pagination and 401 behavior, and calls the authenticated partner mock.

For a CLI run after installing Newman:

```powershell
newman run docs/postman/power-outage-reporting.postman_collection.json
```

## Exit Criteria

- All Maven test jobs pass.
- All Postman assertions pass.
- All Kubernetes Pods are Ready.
- Argo CD reports Synced and Healthy.
- All five Prometheus scrape targets report UP.
- Happy path produces exactly one processed-event row for one event ID.
