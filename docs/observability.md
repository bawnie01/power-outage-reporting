# Observability

## Purpose

Prometheus collects technical metrics from every application service, Kong,
and RabbitMQ. Grafana provides a preconfigured dashboard for inspecting the
health and behavior of the platform.

## Metrics Flow

```text
Spring Boot /actuator/prometheus --+
Kong /metrics ---------------------+--> Prometheus --> Grafana
RabbitMQ /metrics -----------------+
```

## Scrape Targets

| Component | Metrics endpoint |
|---|---|
| Outage Service | `http://outage-service:8080/actuator/prometheus` |
| Notification Service | `http://notification-service:8082/actuator/prometheus` |
| SMS Partner Mock | `http://sms-partner-mock:8081/actuator/prometheus` |
| Kong | `http://kong:8001/metrics` |
| RabbitMQ | `http://rabbitmq:15692/metrics` |

## Grafana Dashboard

The `Power Outage Platform Overview` dashboard is provisioned automatically.
It contains:

- Number of healthy Prometheus targets
- HTTP request rate by application
- JVM heap memory usage by application
- HTTP 5xx response rate by application

## Local Kubernetes Access

Prometheus and Grafana are internal Kubernetes Services. Access them from a
development machine with temporary port forwarding:

```powershell
kubectl --kubeconfig <LAB_KUBECONFIG> port-forward -n power-outage service/prometheus 9090:9090
kubectl --kubeconfig <LAB_KUBECONFIG> port-forward -n power-outage service/grafana 3000:3000
```

Open:

- Prometheus: `http://localhost:9090/targets`
- Grafana: `http://localhost:3000`

The development-only Grafana credentials are `admin` / `admin`. Production
credentials must be stored in a dedicated secret manager and rotated.

## Verification

1. Confirm every Prometheus target is `UP`.
2. Submit an outage report through Kong.
3. Open the provisioned Grafana dashboard.
4. Confirm that the request-rate and JVM panels contain data.

The Kubernetes manifests use a three-day Prometheus retention period to keep
this training environment small. Long-term production retention is outside
the scope of this mini project.
