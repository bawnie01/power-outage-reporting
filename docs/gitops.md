# GitOps Delivery

## Repository

The desired Kubernetes state is stored separately from application source code:

```text
https://github.com/bawnie01/power-outage-reporting-gitops
```

This separation allows the application repository to build artifacts while the
GitOps repository records exactly which release should run in each environment.

## Release Flow

```text
Developer pushes application source
              |
              v
GitHub Actions tests all services
              |
              v
GitHub Actions publishes immutable GHCR images
              |
              v
GitOps environment selects an image SHA tag
              |
              v
Argo CD detects the Git change
              |
              v
Argo CD synchronizes Kubernetes
```

The current `dev` release uses image tag `sha-775e5a1`. This tag corresponds to
the application source commit that successfully completed all test and publish
jobs.

## Argo CD Policy

The Application enables:

- Automated synchronization
- Pruning of resources removed from Git
- Self-healing when cluster state drifts from Git
- Automatic creation of the destination namespace
- A five-revision deployment history

## Security Boundary

The application CI workflow does not receive Kubernetes credentials. It only
tests code and publishes images. Argo CD is the component authorized to pull the
desired state from Git and reconcile Kubernetes.

## Promotion

For this mini project, promotion is explicit: update the immutable image tags in
`environments/dev/kustomization.yaml`, review the Git diff, and commit it. A
future extension may automate this change using a pull request.

