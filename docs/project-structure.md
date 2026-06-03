# FindApi project structure

## Objective

This structure creates the initial package layout for FindApi as a modular monolith. It is intentionally minimal: no business rules, no functional controllers, no complete DTOs, no repository queries, and no JPA entity mappings are implemented yet.

## Why Modular Monolith

FindApi can start as one deployable Spring Boot application while keeping clear business boundaries inside the codebase. This keeps the MVP simple to run, test, and deploy, while making future extraction or deeper module isolation easier if the product grows.

## Why Centralized Entities

Entities are kept under `entity` for the first phase because the database model is shared by several catalog features. Centralizing them avoids premature duplication while the domain model is still stabilizing. Module packages can still own controllers, services, repositories, mappers, and DTOs.

## Module Responsibilities

- `common`: shared configuration, exception types, response envelopes, pagination helpers, and utilities.
- `security`: future authentication, authorization, and JWT integration.
- `entity`: future JPA entity classes that map to the FindApi database schema.
- `enums`: shared enum values aligned with database constraints and domain language.
- `apiCatalog`: API catalog search, filtering, details, and comparison use cases.
- `category`: category taxonomy for organizing APIs.
- `tag`: flexible labels used for API discovery and filtering.
- `authenticationMethod`: authentication method catalog used by APIs.
- `pricing`: pricing plan information for APIs.
- `codeExample`: integration examples by API and language.
- `review`: user reviews and ratings for APIs.
- `collection`: user-created groups of APIs.
- `user`: user profile and account-related application boundaries.

## Next Steps

1. Add JPA mappings for entities, aligned with the existing Flyway schema.
2. Decide module package conventions and optional Spring Modulith annotations.
3. Add repositories extending Spring Data only after entities are mapped.
4. Define DTO fields per use case.
5. Add service contracts and business rules incrementally.
6. Add controllers only when endpoint behavior is specified.
7. Add tests per module as behavior is introduced.
