# EVNICT / Course Standards Mapping

This table maps every course deliverable to concrete repository evidence. The project is a training simulation and does not connect to production EVN systems.

| Course capability | Project implementation | Evidence | Status |
|---|---|---|---|
| Distinct business domain | Electric-utility outage reporting | `README.md`, `docs/business-workflow.md` | Pass |
| Main partner workflow | Report received, event emitted, confirmation SMS requested | `docs/business-workflow.md` | Pass |
| REST API design | Versioned resource API, correct status codes, standard errors | `docs/openapi.yaml` | Pass |
| Idempotency | Required key plus request fingerprint and 409 mismatch response | Outage Service application layer | Pass |
| Pagination and filtering | Operator list API with `page`, `size`, and `status` | `GET /api/v1/outage-reports` | Pass |
| Partner contract | Separate SMS OpenAPI contract and failure scenarios | `docs/sms-partner-openapi.yaml` | Pass |
| Synchronous integration | Notification Service calls SMS Partner Mock | `SmsPartnerClient` | Pass |
| Asynchronous integration | Topic exchange, durable queue, retry, DLQ | `docs/event-design.md` | Pass |
| Dual-write reliability | Transactional outbox persisted with report | `outbox_event` migration and publisher | Pass |
| Consumer idempotency | Persistent processed-event table | Notification Service migration and repository | Pass |
| Timeout and retry | HTTP connect/read timeouts plus listener retry | Notification properties | Pass |
| Identity provider | Keycloak realm, users, and roles | `infrastructure/keycloak` | Pass |
| Authorization | Method-specific RBAC in Spring Security | `SecurityConfiguration` | Pass |
| API gateway | Kong DB-less routing and traffic policies | `infrastructure/kong` | Pass |
| Partner authentication | API key from environment/Kubernetes Secret | Security and deployment manifests | Pass for lab |
| Kubernetes | Deployments, Services, probes, and limits | `deployments/kubernetes` | Pass |
| GitOps | Separate desired-state repository watched by Argo CD | `power-outage-reporting-gitops` | Pass |
| Continuous integration | Test matrix and immutable GHCR image tags | `.github/workflows/build-images.yml` | Pass |
| Metrics | Spring, Kong, and RabbitMQ scraped by Prometheus | `docs/observability.md` | Pass |
| Dashboard | Provisioned Grafana platform dashboard | `deployments/kubernetes/monitoring.yaml` | Pass |
| API catalog / portal | OpenAPI contracts are catalog-ready; no AMP product deployed | OpenAPI files | N/A for lab |
| Centralized logging | Structured container logs only; no EFK deployment | Architecture review | Optional gap |
