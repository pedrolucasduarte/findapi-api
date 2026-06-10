# FindApi Project Structure

## Overview

FindApi is organized as a modular monolith. Each business capability owns its
controllers, services, repositories, specifications, mappers, and DTOs, while
JPA entities and shared enums remain centralized.

```text
src/main/java/com/findapi/api
|-- Application.java
|-- apiCatalog/
|-- authenticationMethod/
|-- category/
|-- codeExample/
|-- collection/
|-- common/
|-- dashboard/
|-- entity/
|-- enums/
|-- pricing/
|-- rankings/
|-- review/
|-- search/
|-- security/
|-- tag/
`-- user/
```

## Package Responsibilities

- `apiCatalog`: API catalog CRUD, filtering, details, review statistics, and
  category/tag relationships.
- `authenticationMethod`: authentication method catalog management.
- `category`: category taxonomy management.
- `codeExample`: JPA foundation and placeholder structure for a future REST
  API. This module does not expose functional endpoints yet.
- `collection`: user-owned collections and collection/API relationships.
- `common`: shared configuration, exceptions, pagination, and utilities.
- `dashboard`: aggregate catalog statistics.
- `entity`: centralized JPA mappings aligned with the Flyway schema.
- `enums`: enum values aligned with database constraints.
- `pricing`: pricing plans associated with APIs.
- `rankings`: public API rankings.
- `review`: user reviews, ratings, and aggregate statistics.
- `search`: paginated API discovery with composed specifications.
- `security`: JWT conversion, role authorities, ownership checks, and HTTP
  authorization.
- `tag`: tag catalog management.
- `user`: authenticated profile and administrative user queries.

## Conventions

- Controllers expose versioned REST endpoints and never return JPA entities.
- Services contain transactional application behavior.
- Repositories use Spring Data JPA and specifications.
- MapStruct maps entities to response DTOs.
- Flyway owns schema evolution; Hibernate validates the resulting schema.
- Soft deletion is represented by `deleted_at`.
- Tests use JUnit 5, Mockito, Spring Security Test, and PostgreSQL
  Testcontainers.

For deeper details, see [architecture.md](architecture.md),
[modules.md](modules.md), and [security.md](security.md).
