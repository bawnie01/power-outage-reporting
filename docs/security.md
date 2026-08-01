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

The outage report submission endpoint allows `CUSTOMER` and `OPERATOR`.

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

## Secret Management

Passwords, access tokens, and private keys must not be committed to this repository. Local values use environment variables, while Kubernetes values use Secrets.
