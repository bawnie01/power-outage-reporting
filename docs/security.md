# Security Design

## Authentication

Keycloak authenticates users and issues OAuth 2.0/OpenID Connect access tokens.

Local Keycloak configuration:

| Property | Value |
|---|---|
| Realm | `power-outage` |
| Client | `power-outage-api` |
| Base URL | `http://localhost:8083` |
| Admin console | `http://localhost:8083/admin` |

## Authorization

The project defines three roles:

- `CUSTOMER`: submit and view the customer's outage reports.
- `OPERATOR`: submit reports and view all reports.
- `ADMIN`: access all project APIs and administration functions.

Authorization rules:

| Operation | Roles |
|---|---|
| Submit a report | `CUSTOMER`, `OPERATOR` |
| Get a report by ID | `CUSTOMER`, `OPERATOR`, `ADMIN` |
| List and filter reports | `OPERATOR`, `ADMIN` |

## Demo Users

| Username | Password | Role |
|---|---|---|
| `customer01` | `customer123` | `CUSTOMER` |
| `operator01` | `operator123` | `OPERATOR` |

These accounts and passwords are for local demonstration only.

## Request Flow

1. The client obtains an access token from Keycloak.
2. The client sends the token in the `Authorization: Bearer <token>` header.
3. Kong routes the request to the Outage Service.
4. The Outage Service validates the token and required role.

The application validates the token issuer, signature, and expiration. Keycloak
realm roles from `realm_access.roles` are mapped to Spring Security authorities.

Kong OSS performs routing, rate limiting, correlation IDs, and request-size controls. JWT validation remains in the resource server because Kong's OIDC plugin is not assumed to be available in every OSS lab distribution. Therefore, a request without a valid token can reach the service network endpoint but cannot reach business logic. In production, use an OIDC-capable gateway plugin or an external authorization service for defense in depth.

## Service-to-Partner Authentication

The Notification Service authenticates to the SMS Partner Mock with the `X-Partner-Api-Key` header. The value comes from an environment variable locally and a Kubernetes Secret in the cluster. This is a deliberately small lab implementation. Production integration should prefer OAuth 2.0 client credentials or mutual TLS, with keys stored and rotated by a secret manager.

The password grant exists only to make the Postman demonstration short. Browser and mobile clients must use Authorization Code with PKCE in production.

## Secret Management

Passwords, access tokens, and private keys must not be committed to this repository. Local values use environment variables, while Kubernetes values use Secrets.
