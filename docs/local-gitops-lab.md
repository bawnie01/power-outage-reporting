# Local GitOps Lab with kind and Argo CD

## Safety

This project uses a dedicated kind cluster named `power-outage-lab`. Its
kubeconfig is kept separate from the user's normal Kubernetes configuration,
so corporate contexts are never selected or modified.

Always pass the dedicated kubeconfig explicitly:

```powershell
kubectl --kubeconfig work/power-outage-lab.kubeconfig get nodes
```

## 1. Create the Cluster

Install kind from its official release page, then create the cluster:

```powershell
kind create cluster `
  --name power-outage-lab `
  --image kindest/node:v1.34.3@sha256:08497ee19eace7b4b5348db5c6a1591d7752b164530a36f855cb0f2bdcbadd48 `
  --kubeconfig work/power-outage-lab.kubeconfig
```

## 2. Install Argo CD

```powershell
$config = "work/power-outage-lab.kubeconfig"

kubectl --kubeconfig $config create namespace argocd

kubectl --kubeconfig $config apply `
  -n argocd `
  --server-side `
  --force-conflicts `
  -f https://raw.githubusercontent.com/argoproj/argo-cd/v3.4.2/manifests/install.yaml

kubectl --kubeconfig $config wait `
  --for=condition=Ready pods `
  --all `
  -n argocd `
  --timeout=240s
```

## 3. Bootstrap the Application

Clone the GitOps repository and run:

```powershell
kubectl --kubeconfig $config apply `
  -f bootstrap/power-outage-dev.yaml
```

Argo CD reads the public GitOps repository and creates the complete platform.

## 4. Verify GitOps Status

```powershell
kubectl --kubeconfig $config get application `
  -n argocd power-outage-dev

kubectl --kubeconfig $config get pods `
  -n power-outage
```

Expected result:

```text
SYNC: Synced
HEALTH: Healthy
All seven application Pods: 1/1 Running
```

## 5. Open Local Access

Run these commands in separate terminals:

```powershell
kubectl --kubeconfig $config port-forward `
  -n power-outage service/kong 8000:8000

kubectl --kubeconfig $config port-forward `
  -n power-outage service/keycloak 8083:8080

kubectl --kubeconfig $config port-forward `
  -n argocd service/argocd-server 8084:443
```

Use `localhost`, not `127.0.0.1`, when requesting a Keycloak token. The token
issuer must match `http://localhost:8083/realms/power-outage` exactly.

## 6. Local Endpoints

| Component | Address |
|---|---|
| Kong API Gateway | `http://localhost:8000` |
| Keycloak | `http://localhost:8083` |
| Argo CD | `https://localhost:8084` |

## 7. Lessons from the First Deployment

- RabbitMQ CLI health checks need more than the Kubernetes default one-second timeout on a small single-node cluster.
- Kubernetes service-link variables can conflict with application variables. `RABBITMQ_PORT` is therefore set explicitly to `5672`.
- Kong worker count is limited to two for the lab's memory limit.
- Kong 3.9 uses `/status` for both readiness and liveness in this DB-less setup.

All fixes were committed to Git and deployed by Argo CD. No application
Deployment was patched manually in the cluster.

## 8. Delete the Lab

Deleting the kind cluster removes all lab resources and persistent data:

```powershell
kind delete cluster --name power-outage-lab
```

