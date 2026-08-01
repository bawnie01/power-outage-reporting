# Architecture Review

Review date: 2026-08-01

Status definitions: **Pass** is implemented and evidenced, **Gap** needs future work, and **N/A** is intentionally outside this mini-project.

| # | Review question | Status | Evidence / decision |
|---:|---|---|---|
| 1 | Is the business scope explicit and bounded? | Pass | One workflow: submit outage report and send confirmation. |
| 2 | Are real EVN data and systems excluded? | Pass | Disclaimer in README and workflow documents. |
| 3 | Are resource URIs and HTTP semantics consistent? | Pass | Versioned create, get, and list operations in OpenAPI. |
| 4 | Are API errors standardized? | Pass | Reusable `ErrorResponse`, validation details, correlation ID. |
| 5 | Is create idempotent? | Pass | UUID key, payload fingerprint, replay, and 409 conflict. |
| 6 | Does the list API support bounded pagination? | Pass | Page >= 0 and size 1–100, newest first, optional status filter. |
| 7 | Is API evolution governed? | Pass | Compatibility policy in `api-versioning.md`. |
| 8 | Is dual-write failure handled? | Pass | Report and outbox event commit atomically. |
| 9 | Can an event be delivered more than once safely? | Pass | Persistent `processed_notification_event` deduplication. |
| 10 | Are partner latency and failures bounded? | Pass | Connect/read timeouts, three listener attempts, and DLQ. |
| 11 | Is there a circuit breaker? | Gap | Retry and DLQ are sufficient for this single-consumer lab; production should add a circuit breaker. |
| 12 | Are users authenticated by an IdP? | Pass | Keycloak OIDC tokens and issuer/signature/expiry validation. |
| 13 | Is least-privilege RBAC applied? | Pass | POST, detail GET, and list GET have different role rules. |
| 14 | Is authentication enforced at the gateway edge? | Gap | Resource server enforces JWT; Kong OSS provides traffic controls only. |
| 15 | Is service-to-partner authentication present? | Pass for lab | Secret-backed API key; production recommendation is client credentials or mTLS. |
| 16 | Are secrets excluded from images and source production config? | Pass | Values injected through env/Secret; checked-in values explicitly lab-only. |
| 17 | Are workload health and resource bounds declared? | Pass | Readiness/liveness probes and requests/limits. |
| 18 | Is deployment pull-based and auditable? | Pass | Argo CD watches immutable GitOps revisions and image tags. |
| 19 | Are metrics and dashboards automatically provisioned? | Pass | Five Prometheus targets and Grafana dashboard. |
| 20 | Is centralized log search available? | Gap | Logs go to stdout with correlation IDs; EFK is optional future work. |
| 21 | Is a user interface required? | N/A | Postman and OpenAPI are the approved demonstration clients. |
| 22 | Is an enterprise API management portal required? | N/A | Contracts are portal-ready; AMP is not deployed in this course lab. |

## Accepted Risks

- Kong edge OIDC is not enabled because the portable OSS lab configuration cannot assume the enterprise OIDC plugin.
- The API key is suitable only for the mock partner; production requires rotation and stronger workload identity.
- Prometheus retention is three days because the local kind cluster is disposable.
- Outbox delivery is at least once, so consumer deduplication remains mandatory.
