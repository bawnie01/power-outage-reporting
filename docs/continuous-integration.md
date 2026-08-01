# Continuous Integration and Container Images

## Purpose

GitHub Actions tests all three Java services and publishes their container
images to GitHub Container Registry (GHCR). Kubernetes can then pull the images
without depending on the developer's local Docker runtime.

## Workflow

The workflow is stored at `.github/workflows/build-images.yml`.

```text
Push to main
    |
    v
Test three services in parallel
    |
    v
Build three container images in parallel
    |
    v
Push main and immutable SHA tags to GHCR
```

Pull requests run tests but do not publish images.

## Published Images

```text
ghcr.io/bawnie01/outage-service:main
ghcr.io/bawnie01/notification-service:main
ghcr.io/bawnie01/sms-partner-mock:main
```

Each successful build also publishes an immutable tag such as:

```text
ghcr.io/bawnie01/outage-service:sha-a1b2c3d
```

The `main` tag is convenient for the first Kubernetes exercise. The GitOps
milestone will update deployments to immutable SHA tags for auditable releases.

## Permissions

The workflow uses GitHub's automatically generated `GITHUB_TOKEN`. It requires:

```yaml
permissions:
  contents: read
  packages: write
```

No personal access token or registry password is stored in the repository.

## Package Visibility

After the first successful workflow, open the package settings for each image
on GitHub and make it public for this public learning project. If an image stays
private, Kubernetes requires an `imagePullSecret` with registry credentials.

## Verification

Open the repository's **Actions** tab and select **Test and publish container
images**. All test and publish jobs should be green.

Test a published image locally:

```powershell
docker pull ghcr.io/bawnie01/outage-service:main
```

