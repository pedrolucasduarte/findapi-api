# FindApi database model

## Conceptual model

FindApi is centered on a catalog of APIs. Each API belongs to many categories, has many tags, uses one authentication method, and can expose pricing plans, code examples, and user reviews. Users authenticate in the platform, write reviews, and create collections that group APIs for later consultation or comparison.

Core concepts:

- User: platform account with role and credential hash.
- API: catalog item with business flags, URLs, status, type, difficulty, and searchable descriptions.
- Category: primary taxonomy for filtering APIs.
- Tag: flexible labels for technologies, domains, and capabilities.
- AuthenticationMethod: controlled list of supported authentication styles.
- PricingPlan: commercial options for one API.
- CodeExample: integration examples per API and language.
- Review: user feedback for one API.
- Collection: user-owned group of APIs.

## Logical model

- `app_users`
  - PK: `id`
  - Unique active email: `lower(email)` where `deleted_at is null`
  - One user can own many `collections` and write many `reviews`.

- `apis`
  - PK: `id`
  - FK: `authentication_method_id -> authentication_methods.id`
  - Unique active slug: `slug` where `deleted_at is null`
  - One API has many `pricing_plans`, `code_examples`, and `reviews`.
  - Many-to-many with `categories` through `api_categories`.
  - Many-to-many with `tags` through `api_tags`.
  - Many-to-many with `collections` through `collection_apis`.

- `categories`
  - PK: `id`
  - Unique active slug.

- `tags`
  - PK: `id`
  - Unique active slug.

- `authentication_methods`
  - PK: `id`
  - Unique active name.

- `pricing_plans`
  - PK: `id`
  - FK: `api_id -> apis.id`

- `code_examples`
  - PK: `id`
  - FK: `api_id -> apis.id`

- `reviews`
  - PK: `id`
  - FK: `api_id -> apis.id`
  - FK: `user_id -> app_users.id`
  - Unique active review per user/API: `(api_id, user_id)` where `deleted_at is null`

- `collections`
  - PK: `id`
  - FK: `user_id -> app_users.id`
  - Unique active slug per user: `(user_id, slug)` where `deleted_at is null`

## Design notes

- UUID primary keys use `gen_random_uuid()` from `pgcrypto`.
- Soft delete uses nullable `deleted_at` on domain tables and relationship tables.
- Basic auditing uses `created_at`, `updated_at`, and database triggers to maintain `updated_at`.
- Enums are represented with `varchar` plus `CHECK` constraints for simpler JPA/Hibernate mapping and easier Flyway evolution.
- API search is supported by a generated `tsvector` column plus GIN indexes for full-text and trigram name search.
