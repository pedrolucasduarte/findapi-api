# FindApi Architecture

## Overview

FindApi is a modular monolith built with Spring Boot, Spring Data JPA, Hibernate,
PostgreSQL and Flyway. Modules are organized by business capability, while JPA
entities remain centralized under `com.findapi.api.entity`.

## Layers

- Controllers expose versioned REST resources under `/api/v1`.
- Services define transaction boundaries and authorization rules tied to ownership.
- Repositories use Spring Data JPA, Specifications and JPQL aggregate queries.
- MapStruct converts entities to response DTOs.
- Flyway is the only owner of schema creation and seed data.

## Data Rules

- UUID primary keys.
- `timestamptz` mapped as `OffsetDateTime`.
- Soft delete through `deleted_at`.
- Association tables are mapped as entities with composite embedded IDs.
- Hibernate runs with `ddl-auto=validate`; it never creates or changes the schema.

## Cross-Cutting Concerns

- Stateless JWT resource-server security.
- Bean Validation on request DTOs.
- Central exception handling.
- Structured audit log messages for relationship and profile mutations.
- In-memory Spring Cache for read-heavy aggregate endpoints.
- Actuator health, info and metrics.
- OpenAPI 3 and Swagger UI.
