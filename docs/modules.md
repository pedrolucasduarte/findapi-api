# Modules

| Module | Responsibility |
|---|---|
| `apiCatalog` | API catalog CRUD, filters, details and API relationships |
| `authenticationMethod` | Authentication method catalog |
| `category` | Category CRUD and filtering |
| `tag` | Tag CRUD and filtering |
| `pricing` | Pricing plans associated with APIs |
| `review` | User reviews and API rating aggregates |
| `collection` | User collections and collection/API relationships |
| `user` | Authenticated profile and administrative user queries |
| `search` | Advanced API search by attributes, category and tag |
| `dashboard` | Public catalog totals and highlighted APIs |
| `rankings` | Public ranked API views |
| `security` | JWT conversion, authorities and HTTP authorization |
| `common` | Exceptions, pagination, configuration and shared utilities |

Association modules preserve the Flyway composite keys:

- `ApiCategoryEntity`: `api_id + category_id`
- `ApiTagEntity`: `api_id + tag_id`
- `CollectionApiEntity`: `collection_id + api_id`

Removing a relationship sets `deleted_at`. Adding the same relationship again
reactivates the existing row when it was previously soft deleted.
