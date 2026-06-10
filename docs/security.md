# Security

## Authentication

The application is a stateless OAuth 2.0 resource server. JWT validation is
enabled when `JWT_ISSUER_URI` or `JWT_JWK_SET_URI` is configured.

If neither property is configured, the application remains usable for explicit
public reads, but all other routes are denied. Security configuration therefore
fails closed instead of silently exposing protected operations.

JWT roles are converted to Spring authorities such as:

- `ROLE_USER`
- `ROLE_PROVIDER`
- `ROLE_ADMIN`

## Authorization

- Catalog GET endpoints, search, dashboard and rankings are public.
- API writes require provider or administrator authority.
- Administrative catalogs require administrator authority for writes.
- Reviews and collections require an authenticated user for mutations.
- Collection/API mutations require collection ownership or administrator authority.
- User listing and lookup require administrator authority.

## Secrets

Database credentials and JWT endpoints are supplied through environment variables.
The repository contains only `.env.example`; a real `.env` must remain ignored.

## Operational Endpoints

- `/actuator/health/**` and `/actuator/info` are public.
- `/actuator/metrics/**` requires `ROLE_ADMIN`.
- Swagger UI is available at `/swagger-ui/index.html`.

OpenAPI endpoints can be disabled with `SWAGGER_ENABLED=false`.

The API does not return password hashes, JPA entities or stack traces.
