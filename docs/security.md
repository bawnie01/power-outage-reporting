# Security Design

## Authentication

Keycloak authenticates users and issues OAuth 2.0/OpenID Connect access tokens.

## Authorization

The project defines three roles:

- `CUSTOMER`: submit and view the customer's outage reports.
- `OPERATOR`: submit reports and view all reports.
- `ADMIN`: access all project APIs and administration functions.

## Request Flow

1. The client obtains an access token from Keycloak.
2. The client sends the token in the `Authorization: Bearer <token>` header.
3. Kong routes the request to the Outage Service.
4. The Outage Service validates the token and required role.

## Secret Management

Passwords, access tokens, and private keys must not be committed to this repository. Local values use environment variables, while Kubernetes values use Secrets.
