# Operations Runbook

## Quick Health Check

1. Confirm Argo CD is `Synced` and `Healthy`.
2. Confirm all Pods are Ready in the `power-outage` namespace.
3. Check Prometheus targets and the Grafana overview dashboard.
4. Submit a synthetic outage report through Kong.

## Outbox Backlog

Symptom: reports are created but no SMS event reaches RabbitMQ.

1. Check RabbitMQ health and network connectivity.
2. Inspect unpublished rows:
   `SELECT event_id, publish_attempts, last_error FROM outbox_event WHERE published_at IS NULL;`
3. Restore RabbitMQ. The scheduled publisher retries pending rows automatically.
4. Do not manually delete pending events.

## Notification Dead-Letter Queue

Symptom: `notification.outage-reported.dlq` contains messages.

1. Check partner availability, API-key configuration, and timeout logs.
2. Correct the partner configuration or restore the partner.
3. Inspect the event ID and confirm it is not already present in `processed_notification_event`.
4. Republish only after the dependency is healthy; consumer deduplication prevents a second successful send for the same event ID.

## Authentication Failures

- A public API `401` usually means a missing, invalid, or expired Keycloak token.
- A partner `401` means the `X-Partner-Api-Key` Secret does not match.
- A `403` means the token is valid but its realm role is insufficient.

## Rollback

Revert the GitOps commit that changed the application manifest revision or image tag. Do not run `kubectl apply` against application resources. Argo CD will reconcile the reverted desired state.

Database migrations are forward-only. If a release contains a destructive migration, restore from a tested backup instead of attempting an ad-hoc schema rollback.

## Escalation Evidence

Capture the correlation ID, event ID, report code, affected image SHA, Argo CD revision, relevant application logs, Prometheus graph, and RabbitMQ queue state. Never include access tokens, passwords, or API keys in an incident ticket.
