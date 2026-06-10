# Security and Public Release Audit

Audit date: June 10, 2026

## Scope

This review covered application architecture, REST controllers, method
authorization, JWT configuration, DTO validation, repositories, JPA
specifications, Flyway migrations, PostgreSQL usage, cache behavior, Actuator,
OpenAPI, Docker, Maven dependencies, tests, documentation, Git tracking, and
common secret file formats.

The audit is a point-in-time engineering assessment, not a guarantee against
future vulnerabilities.

## Executive Summary

The repository is suitable for public publication after the remediations listed
below. No credentials, private keys, certificates, access tokens, local IDE
state, build output, or database dumps are tracked by Git.

The application uses explicit public read routes, method-level authorization,
owner checks for user-controlled resources, parameterized JPA queries, DTO
validation, soft deletion, and Flyway-owned schema management.

## Findings Remediated

| Area | Finding | Resolution |
|---|---|---|
| Access control | Missing JWT configuration caused the HTTP layer to permit unmatched routes | Unmatched routes now fail closed with `denyAll()` |
| Actuator | Metrics could become public when JWT was not configured | `/actuator/metrics/**` now requires `ROLE_ADMIN` |
| Dependencies | Tomcat 11.0.21 had recently published advisories | Overridden to Tomcat 11.0.22 |
| Dependencies | pgJDBC 42.7.10 had a SCRAM CPU exhaustion advisory | Overridden to pgJDBC 42.7.11 |
| Performance | Rankings loaded rating statistics with two queries per API | Ratings are now aggregated in one grouped query |
| Cache consistency | Dashboard and ranking caches were not invalidated after writes | Relevant mutations now evict affected caches |
| Input validation | Large text fields had no application-level limits | Added bounded validation for descriptions |
| Input validation | Collection slugs relied only on the database constraint | Added request-level slug validation |
| Error handling | Unexpected exceptions returned a safe response but were not logged | Added server-side error logging without exposing details |
| Runtime hardening | Error details and graceful shutdown were implicit | Added explicit safe error settings and graceful shutdown |
| Docker | No application image existed and PostgreSQL had no health check | Added a non-root multi-stage Dockerfile and database health check |
| Supply chain | No automated dependency update configuration existed | Added Dependabot for Maven, Docker, and GitHub Actions |
| CI | No public CI workflow existed | Added GitHub Actions verification and JaCoCo artifact upload |
| Licensing | README referenced MIT but no license file existed | Added the MIT `LICENSE` file |
| Dead code | Empty shared response and Jackson configuration placeholders | Removed unused placeholders |

## OWASP Review

### A01: Broken Access Control

- Every implemented controller operation has explicit `@PreAuthorize`.
- Catalog reads, search, dashboard, rankings, health, info, and API
  documentation are intentionally public.
- Administrative writes require `ROLE_ADMIN`.
- API and pricing writes require `ROLE_PROVIDER` or `ROLE_ADMIN`.
- Review and collection mutations require authenticated roles.
- Review and collection services enforce owner-or-admin checks.
- Routes outside the public allowlist are denied when JWT is unavailable.

### A02: Cryptographic Failures

- No private keys, keystores, certificates, tokens, or real passwords were
  found.
- Database credentials and JWT endpoints are supplied through environment
  variables.
- JWT signature validation is delegated to Spring Security and the configured
  issuer or JWK Set.

### A03: Injection

- No native SQL or user-controlled query concatenation was found.
- JPQL uses named parameters.
- Specifications use the Criteria API.
- Repository methods use Spring Data parameter binding.

### A04: Insecure Design

- Request DTOs prevent entity mass assignment.
- Hibernate validates rather than creates the schema.
- Soft-delete rules are applied in active-record queries.
- Ownership checks are implemented in the service transaction boundary.

### A05: Security Misconfiguration

- The application is stateless and CSRF is disabled for the bearer-token API.
- CORS has no permissive wildcard configuration.
- Actuator exposure is limited to health, info, and metrics.
- Metrics require administrator authority.
- Swagger can be disabled with `SWAGGER_ENABLED=false`.
- Internal exception details and stack traces are not returned.

### A06: Vulnerable Components

The OSV API was queried for 137 resolved runtime components after remediation.
The final scan returned zero known vulnerabilities for the exact resolved
versions. Dependabot was added to keep this state monitored.

### A07: Authentication Failures

- JWT processing uses Spring Security OAuth 2.0 Resource Server.
- Roles, scopes, and direct authorities are extracted from trusted token claims.
- The JWT subject is treated as the application user UUID.
- Protected routes fail closed when JWT infrastructure is not configured.

### A08: Software and Data Integrity Failures

- Maven dependencies are version-managed and CI builds from the Maven Wrapper.
- Flyway validates migration checksums.
- GitHub Actions uses read-only repository permissions.
- Docker build output runs as an unprivileged user.

### A09: Security Logging and Monitoring Failures

- Tokens, authorization headers, passwords, and request payloads are not logged.
- Relationship and profile changes emit structured audit-oriented entries.
- Unexpected failures are logged server-side and return a generic response.

### A10: Server-Side Request Forgery

The application stores provider URLs but does not make outbound requests to
them. No SSRF execution path currently exists.

## Validation Results

- Maven tests: 153 passed, 0 failed, 0 errors
- JaCoCo line coverage: 64.26%
- JaCoCo branch coverage: 51.51%
- PostgreSQL: 17.10 through Testcontainers
- Flyway: V1 and V2 validated and applied
- Hibernate: schema validation passed
- Docker application image: built successfully
- OSV runtime dependency scan: 137 components, 0 known vulnerabilities
- Live application health: `UP`
- Swagger UI and OpenAPI JSON: available
- Public module endpoints: HTTP 200
- Protected POST, PUT, DELETE without JWT: HTTP 403
- Protected Actuator metrics without JWT: HTTP 403
- Invalid UUID input: HTTP 400

## Residual Risks and Future Recommendations

1. Add an expected JWT audience and enforce it when the identity provider is
   selected.
2. Prefer `JWT_ISSUER_URI` so issuer validation is always available.
3. Add gateway-level rate limiting and abuse protection before an internet
   deployment.
4. Increase direct controller coverage for dashboard, rankings, search, and the
   future code example API.
5. Complete or remove the current code example scaffolding before advertising
   that module as generally available.
6. Revisit whether public collections should expose owner UUIDs as the product
   privacy model evolves.
7. Replace the in-memory cache with a bounded distributed cache for
   multi-instance deployments.
8. Consider a query that uses the existing PostgreSQL `search_vector` directly;
   current name search uses portable JPA criteria.
9. Consolidate API detail rating statistics into one aggregate query if detail
   endpoint traffic becomes significant.
10. Add actor identity and correlation IDs to audit logs.

## Migration Integrity

No Flyway migration or database model was changed during this audit.
