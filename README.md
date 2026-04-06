# Money Manager Backend - Assignment Coverage

This backend now covers the finance dashboard assignment requirements with role-based access control, record management, filtering, dashboard analytics, validation, and structured error handling.

## Tech Stack

- Java 21
- Spring Boot 4
- Spring Security (JWT)
- Spring Data JPA
- MySQL/PostgreSQL (runtime driver based on env)
- Maven

## Implemented Features

### 1) User and Role Management

- Users persist with role and status in `tbl_profiles`:
  - `VIEWER`
  - `ANALYST`
  - `ADMIN`
- Default role is `ANALYST` for new registrations.
- Active/inactive status is enforced during authentication.
- Admin APIs:
  - `GET /api/v1.0/admin/users` -> list users
  - `PUT /api/v1.0/admin/users/{userId}/access` -> update `role` and `isActive`

## 2) Financial Records Management

### Income APIs
- `POST /api/v1.0/incomes`
- `GET /api/v1.0/incomes`
- `GET /api/v1.0/incomes/{id}`
- `PUT /api/v1.0/incomes/{id}`
- `DELETE /api/v1.0/incomes/{id}`

### Expense APIs
- `POST /api/v1.0/expenses`
- `GET /api/v1.0/expenses`
- `GET /api/v1.0/expenses/{id}`
- `PUT /api/v1.0/expenses/{id}`
- `DELETE /api/v1.0/expenses/{id}`

### Category APIs
- `POST /api/v1.0/categories`
- `GET /api/v1.0/categories`
- `GET /api/v1.0/categories/{type}`
- `PUT /api/v1.0/categories/{categoryId}`
- `DELETE /api/v1.0/categories/{categoryId}`

All update/delete/read-by-id operations are ownership scoped to the authenticated user.

## 3) Dashboard Summary APIs

- `GET /api/v1.0/dashboard`

Returns:
- total income
- total expense
- total/net balance
- recent incomes and expenses
- merged recent transactions
- category-wise totals
- monthly trend summary (last 6 months)

## 4) Access Control Logic

Configured in `SecurityConfig`:

- Public: `/status`, `/health`, `/register`, `/activate`, `/login`
- `ADMIN` only: `/admin/**`
- `VIEWER`, `ANALYST`, `ADMIN`: `/dashboard/**`
- `ANALYST`, `ADMIN`: `/filter/**`, `/profile`
- `ANALYST`, `ADMIN`: `/expenses/**`, `/incomes/**`, `/categories/**`

Role authorities are loaded from persisted user role (`ROLE_VIEWER`, `ROLE_ANALYST`, `ROLE_ADMIN`).

## 5) Validation and Error Handling

Validation annotations added on request DTOs:
- `AuthDTO`, `ProfileDTO`, `IncomeDTO`, `ExpenseDTO`, `CategoryDTO`, `FilterDTO`

Global error contract via `@RestControllerAdvice`:
- validation errors (`400`) with field-level details
- access denied (`403`)
- status-driven service errors
- fallback server errors (`500`)

## 6) Data Persistence

JPA entities + repositories are used for persistence.
Current `application.properties` points to MySQL; PostgreSQL driver is also available as runtime dependency.

## Optional Enhancements Included

- JWT authentication
- Filtering with date range, keyword, category, sort field/order
- Admin user access management
- Pagination + search support:
  - `GET /api/v1.0/incomes/search?page=0&size=10&keyword=salary&sortField=date&sortDirection=desc`
  - `GET /api/v1.0/expenses/search?page=0&size=10&keyword=food&sortField=date&sortDirection=desc`
- Soft delete for financial records (`IncomeEntity` and `ExpenseEntity` now use `deletedAt`)
- Expanded unit tests (`ProfileServiceTest`, `IncomeServiceTest`, `ExpenseServiceTest`)
- Swagger/OpenAPI docs:
  - OpenAPI JSON: `http://localhost:8081/api/v1.0/v3/api-docs`
  - Swagger UI: `http://localhost:8081/api/v1.0/swagger-ui/index.html`

## Run

```powershell
Set-Location "D:\Springprojects\moneymanager"
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

## Test

Current tests include:
- `ProfileServiceTest` (`src/test/java/com/bhavaniprasad/moneymanager/service/ProfileServiceTest.java`)
- `IncomeServiceTest` (`src/test/java/com/bhavaniprasad/moneymanager/service/IncomeServiceTest.java`)
- `ExpenseServiceTest` (`src/test/java/com/bhavaniprasad/moneymanager/service/ExpenseServiceTest.java`)

```powershell
Set-Location "D:\Springprojects\moneymanager"
.\mvnw.cmd test
```

## Notes

- If your IDE still shows `jakarta.validation` unresolved after pulling dependencies, refresh/reimport Maven project.
- `target/` changes are generated build artifacts.

