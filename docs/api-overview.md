# API Overview

Base path: `/api/v1`

## Catalog

- `/apis`: API CRUD and filtering
- `/categories`: category CRUD and filtering
- `/tags`: tag CRUD and filtering
- `/authentication-methods`: authentication method CRUD
- `/pricing-plans`: pricing plan queries and mutations
- `/reviews`: review queries and mutations
- `/collections`: collection CRUD
- `/users/me`: authenticated user profile
- `/users`: administrative user queries

## Relationships

- `POST|DELETE /apis/{apiId}/categories/{categoryId}`
- `GET /apis/{apiId}/categories`
- `POST|DELETE /apis/{apiId}/tags/{tagId}`
- `GET /apis/{apiId}/tags`
- `POST|DELETE /collections/{collectionId}/apis/{apiId}`
- `GET /collections/{collectionId}/apis`

## Discovery

- `GET /search/apis`: advanced paginated search
- `GET /dashboard`: totals, latest, top-rated and Brazilian APIs
- `GET /rankings/top-rated`
- `GET /rankings/free`
- `GET /rankings/open-source`
- `GET /rankings/brazilian`
- `GET /rankings/newest`

Search supports `name`, `categoryId`, `tagId`, `apiType`,
`authenticationMethodId`, capability flags, `integrationDifficulty` and `status`.
Page size is limited to 100.

Detailed API responses include `ratingAverage`, `ratingCount` and the rating
distribution from one to five stars.

Interactive documentation: `/swagger-ui/index.html`.
