# Kubernetes Deployment

## Purpose

This guide deploys the complete mini project to a local Kubernetes cluster
using Kustomize. It is the manual deployment milestone before Argo CD GitOps is
introduced.

## Resources

The manifests create:

- One `power-outage` namespace
- Seven Deployments and Services
- PersistentVolumeClaims for PostgreSQL and RabbitMQ
- Generated ConfigMaps for the Keycloak realm and Kong configuration
- A generated Secret for local demonstration credentials
- Readiness and liveness probes
- CPU and memory requests and limits

## Prerequisites

- Docker or Rancher Desktop
- A local Kubernetes cluster
- `kubectl`
- Kustomize support built into `kubectl`

The example credentials are intentionally limited to local demonstration. Use
an external secret manager or encrypted secrets outside a learning environment.

## 1. Verify the Container Images

First verify that `kubectl` points to a disposable local learning cluster:

```powershell
kubectl config current-context
kubectl get nodes
```

Do not apply these learning manifests to a shared, corporate, staging, or
production cluster. Switch to a local Rancher Desktop, Docker Desktop, kind, or
Minikube context before continuing.

The manifests reference these images:

```text
ghcr.io/bawnie01/outage-service:main
ghcr.io/bawnie01/notification-service:main
ghcr.io/bawnie01/sms-partner-mock:main
```

The GitHub Actions workflow builds and publishes these images. Ensure all three
packages are public before deployment, or configure an `imagePullSecret`.

## 2. Preview and Validate the Manifests

```powershell
kubectl kustomize deployments/kubernetes
kubectl apply --dry-run=client -k deployments/kubernetes
```

## 3. Deploy

```powershell
kubectl apply -k deployments/kubernetes
```

Wait for the workloads:

```powershell
kubectl get pods -n power-outage -w
```

All Pods should eventually show `Running` and `1/1` ready.

## 4. Open Local Access

Keep each command running in a separate terminal:

```powershell
kubectl port-forward -n power-outage service/kong 8000:8000
kubectl port-forward -n power-outage service/keycloak 8083:8080
kubectl port-forward -n power-outage service/rabbitmq 15672:15672
```

Addresses:

| Component | Address |
|---|---|
| Public API through Kong | `http://localhost:8000` |
| Keycloak | `http://localhost:8083` |
| RabbitMQ Management | `http://localhost:15672` |

Use the token and API request from the [local deployment guide](local-deployment.md).

## 5. Troubleshooting

List resources:

```powershell
kubectl get all,pvc -n power-outage
```

Inspect a failing Pod:

```powershell
kubectl describe pod -n power-outage POD_NAME
kubectl logs -n power-outage POD_NAME
```

Inspect a container that follows an init container:

```powershell
kubectl logs -n power-outage POD_NAME -c wait-for-rabbitmq
```

## 6. Remove the Deployment

```powershell
kubectl delete -k deployments/kubernetes
```

This also removes the PostgreSQL and RabbitMQ claims created by this learning
environment. Export any data that must be retained before running the command.
