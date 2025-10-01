# Auth Service (LMS Project)

Authentication & token service for the LMS platform.
Issues **JWT access** and **JWT refresh** tokens, stores refresh *sessions* in DB, and integrates with `userservice` to verify credentials and fetch roles.

---

## Table of contents

* [High-level overview](#high-level-overview)
* [Architecture](#architecture)
* [JWT model](#jwt-model)
* [API](#api)
* [Error model](#error-model)
* [Persistence model](#persistence-model)
* [Security](#security)
* [Configuration](#configuration)
* [Run & profiles](#run--profiles)
* [Swagger/OpenAPI](#swaggeropenapi)
* [Testing](#testing)
* [Troubleshooting](#troubleshooting)
* [Future work / nice to have](#future-work--nice-to-have)

---

## High-level overview

* **Access token** (short-lived, stateless) is used by resource services to authorize requests.
* **Refresh token** (longer-lived) is *rotated* on every refresh; the service stores a DB record per refresh *session* and keeps only a **hash** of the refresh JWT.
* **Login** verifies credentials via `userservice`, creates a refresh session, returns `{access, refresh}`.
* **Refresh** validates old refresh, *atomically* marks its session as used, creates a new session and returns a new pair.
* **Logout** expires one session; **LogoutAll** revokes all active sessions for a user.

---

## Architecture

```
[client] → /auth/login → [authservice] → /users/check-credentials (userservice)
                                     ↘ DB: refresh_token_session
[client] → /auth/refresh → [authservice] (parse + rotate) ↔ DB
[client] → /auth/logout  → [authservice] (expire one)     ↔ DB
[client] → /auth/logout-all → [authservice] (revoke all)  ↔ DB
```

### Services involved

* **authservice** (this repo): issues/rotates tokens, tracks refresh sessions.
* **userservice** (external dependency):

    * `POST /users/check-credentials` → `{ userId, roles[], valid }`
    * `GET  /users/{id}` → `{ id, roles[], active }` (used in refresh to re-check roles/active flag)

---

## JWT model

### Algorithms & validation

* Library: **jjwt** (io.jsonwebtoken)
* Signing: **HS256** (HMAC with a shared secret ≥ 32 bytes)
* Clock skew tolerance: **60s**
* Mandatory checks: `iss`, `aud`, `sub`, `exp`, `jti`
* Access-specific: `roles` claim (array or comma-separated string)
* Refresh-specific: `sid` claim (UUID of refresh session, equals DB `id`)

### Access token (example payload)

```json
{
  "iss": "lms-auth",
  "aud": ["lms-api"],
  "sub": "7d91b4f5-1a7a-4b71-9b4b-9a1c1b7b4a11",   // userId
  "iat": 1730400000,
  "exp": 1730400900,
  "jti": "f9a1b2c3-d4e5-6789-abcd-ef0123456789",
  "roles": ["USER","ADMIN"]
}
```

### Refresh token (example payload)

```json
{
  "iss": "lms-auth",
  "aud": ["lms-api"],
  "sub": "7d91b4f5-1a7a-4b71-9b4b-9a1c1b7b4a11",   // userId
  "iat": 1730400000,
  "exp": 1731600000,
  "jti": "1a2b3c4d-5e6f-7890-abcd-ef0123456789",
  "sid": "c3b5b6c2-7e01-4f9a-9fd5-1e8bca8468a0"    // refresh session id (DB id)
}
```

### Claims quick reference

* **Common**: `iss`, `aud`, `iat`, `exp`, `sub`, `jti`
* **Access-only**: `roles: string[] | string`
* **Refresh-only**: `sid: string (uuid)`

---

## API

Base path: `http://localhost:8081/api/v1/auth`

> All endpoints below are **permitAll** in `authservice` (no JWT filter here). Resource services will enforce access token checks.

### POST `/login`

Validate credentials with `userservice`, create a refresh session, return tokens.

**Request**

```json
{
  "email": "user@example.com",
  "password": "P@ssw0rd!"
}
```

**Response 200**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
}
```

**Errors (401)**

* `invalid_credentials`
* `user_inactive`
* `user_service_empty_response`

---

### POST `/refresh`

Rotate refresh token: validate old refresh, mark its session as used (atomic), create a new session and return a new pair.

**Request**

```json
{
  "oldRefreshToken": "eyJhbGciOiJIUzI1NiIs..."
}
```

**Response 200**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...new...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...new..."
}
```

**Errors (401)**

* `invalid_token`, `expired_token`
* `invalid_audience`, `invalid_subject`, `invalid_jti`, `invalid_expiration`
* `invalid_sid`, `sid_user_mismatch`
* `invalid_status`, `refresh_reuse_detected`
* `invalid_refresh_hash`
* `user_inactive`

---

### POST `/logout`

Expire **one** refresh session.

**Request**

```json
{
  "oldRefreshToken": "eyJhbGciOiJIUzI1NiIs..."
}
```

**Response**: `204 No Content` (idempotent)

**Errors (401)**

* `invalid_sid` (if no session for the token's `sid`)

---

### POST `/logout-all`

Revoke all **ACTIVE** sessions for a user.

**Request**

```json
{
  "userId": "7d91b4f5-1a7a-4b71-9b4b-9a1c1b7b4a11"
}
```

**Response**: `204 No Content`

---

### cURL examples

```bash
# Login
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"P@ssw0rd!"}'

# Refresh
curl -X POST http://localhost:8081/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"oldRefreshToken":"<refresh>"}'

# Logout (one session)
curl -X POST http://localhost:8081/api/v1/auth/logout \
  -H "Content-Type: application/json" \
  -d '{"oldRefreshToken":"<refresh>"}'

# Logout all
curl -X POST http://localhost:8081/api/v1/auth/logout-all \
  -H "Content-Type: application/json" \
  -d '{"userId":"<userId>"}'
```

---

## Error model

All errors are returned by a global `@RestControllerAdvice` as:

```json
{
  "error": "invalid_token",
  "message": "Optional human-readable details",
  "timestamp": "2025-10-01T18:25:43Z"
}
```

**401 Unauthorized** for any token/session/auth related problem:

* `invalid_token`, `expired_token`, `invalid_audience`, `invalid_subject`,
  `invalid_jti`, `invalid_expiration`
* `invalid_sid`, `sid_user_mismatch`, `invalid_status`, `refresh_reuse_detected`
* `invalid_refresh_hash`, `expired_refresh`
* `invalid_credentials`, `bad_credentials`, `user_inactive`, `logout_reuse_detected`

**400 Bad Request** for validation errors (if DTO validation is enabled).

**500 Internal Server Error** for unexpected failures.

---

## Persistence model

### Table: `refresh_token_session`

Tracks refresh sessions. Only **hash** of the refresh token is stored.

| Column       | Type        | Notes                                               |
| ------------ | ----------- | --------------------------------------------------- |
| `id`         | `uuid` (PK) | **sid**; also embedded in refresh JWT (`sid` claim) |
| `user_id`    | `uuid`      | user owner                                          |
| `token_hash` | `text`      | Base64(SHA-256(rawRefreshJWT))                      |
| `status`     | `varchar`   | `ACTIVE` / `ALREADY_USED` / `EXPIRED` / `REVOKED`   |
| `created_at` | `timestamp` | set by `@CreatedDate` (auditing)                    |
| `expires_at` | `timestamp` | session expiry (should match refresh TTL)           |

> **DO NOT** store raw refresh JWT.

### Repository methods (key ones)

* `int markActiveAsAlreadyUsed(UUID sid)` — atomic rotation guard
  Updates `status=ALREADY_USED` **only if** current status is `ACTIVE`.
  Returns `1` on success, `0` if someone already rotated.
* `int expireIfActive(UUID sid)` — idempotent logout of one session.
  Updates `status=EXPIRED` **only if** current is `ACTIVE`.
* `void revokeAllActiveByUserId(UUID userId)` — logout all devices.

---

## Security

`authservice` itself **does not** validate access tokens (it issues them).
Resource services must implement JWT validation (parse access, put Authentication in context).

Authservice SecurityConfig:

* Stateless: `SessionCreationPolicy.STATELESS`
* CSRF disabled, form/basic login disabled
* Permit Swagger & `/api/v1/auth/**`
* (Optionally) add CORS when frontend is ready

Permit list:

```
/api/v1/auth/**
/v3/api-docs/**
/swagger-ui/**
/swagger-ui.html
```

---

## Configuration

### Required properties (typical `application.yaml`)

```yaml
server:
  port: 8084

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/lms_auth
    username: postgres
    password: postgres
  jpa:
    hibernate:
      ddl-auto: update         # dev only; use migrations in prod
    properties:
      hibernate:
        jdbc:
          time_zone: UTC
  jackson:
    time-zone: UTC

auth:
  userservice:
    base-url: http://localhost:8082        # change to your userservice host
  tokens:
    issuer: lms-auth
    audience: lms-api
    access:
      ttl: PT15M                            # ISO-8601 duration (15 minutes)
    refresh:
      ttl: P14D                             # 14 days
    # secret must be >= 32 bytes raw OR base64-encoded
    secret: "change-me-please-32bytes-min-or-base64"
```

### Important notes

* **Secret**: must be at least **32 bytes** (256 bits) raw or Base64-encoded. If you provide Base64, service decodes it automatically; else uses raw UTF-8 bytes.
* **Clock**: server uses UTC (`Instant`) for timestamps.
* **TTL parity**: `auth.tokens.refresh.ttl` must match DB `expires_at` you set when creating sessions.

---

## Run & profiles

### Dev (Postgres local)

1. Start docker-compose from infrastructure/.
2. Set properties as above (or via env vars).
3. Run:

   ```bash
   ./gradlew bootRun
   ```
4. Base URL: `http://localhost:8081`

### Test profile (H2)

Use H2 in-memory for tests:
`src/test/resources/application-test.properties`:

```properties
spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=false
```

Activate with `@ActiveProfiles("test")` in tests (or Gradle env).

---

## Swagger/OpenAPI

Dependency (Gradle):

```gradle
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0'
```

* UI: `http://localhost:8081/swagger-ui/index.html`
* Docs: `http://localhost:8081/v3/api-docs`

OpenAPI config registers a **Bearer** scheme, so resource services can later authorize with access tokens inside the UI.

---

## Testing

### Run all tests

```bash
./gradlew test
```

### What is covered

* **Unit**: `TokenServiceTest`

    * generate/parse access & refresh (positive)
    * (add) negative cases: invalid audience, expired, missing roles, wrong signature, refresh-as-access, access-as-refresh
* **Repository**: `RefreshTokenSessionRepositoryTest`

    * atomic rotation: first update=1, second=0
    * (add) `expireIfActive` idempotency
* **Web / Controller**:

    * Either `@SpringBootTest(webEnvironment = MOCK)` + `@AutoConfigureMockMvc`
    * Or (faster) `@WebMvcTest(AuthController.class)` with:

        * `@MockitoBean AuthService`
        * exclude SecurityConfig & JPA autoconfig if needed

### Common pitfalls

* `@WebMvcTest` must point to the **controller**, not the test class.
* If MVC slice accidentally loads Security/JPA, exclude with:

    * `excludeFilters = @Filter(ASSIGNABLE_TYPE, classes = SecurityConfig.class)`
    * `@ImportAutoConfiguration(exclude = { DataSourceAutoConfiguration, HibernateJpaAutoConfiguration, JpaRepositoriesAutoConfiguration })`
* For integration tests, prefer `@SpringBootTest + H2` and mock `userservice` via `MockRestServiceServer`.

---

## Troubleshooting

* **`invalid_audience`** during parse: ensure `auth.tokens.audience` matches what you set into tokens and what services expect.
* **`secret length less than 32 bytes`**: set a stronger secret (≥ 32 bytes raw or Base64 43+ chars).
* **Refresh reuse accepted**: ensure repository method `markActiveAsAlreadyUsed` is used (not simple `save()`), and check it returns `1`.
* **401 in Swagger** (within authservice): ensure `SecurityConfig` has `permitAll` for `/api/v1/auth/**` and Swagger endpoints.
* **DB stores raw refresh**: it must store **SHA-256(Base64)** *hash* only (never raw JWT).

---

## Future work / nice to have

* Add **CORS** once frontend is ready (allow dev origins).
* Add **rate limiting** for `/login`.
* Add **pepper** for refresh hash (env secret concatenated before SHA-256).
* Add **session metadata** (ip, userAgent) for admin UI.
* Resource services: implement **JwtAuthFilter** to verify access tokens and put `Authentication` with roles → enable `@PreAuthorize`.
* Migrations (Flyway/Liquibase) instead of `ddl-auto`.
* More comprehensive negative tests.

---

## Appendix — Userservice contracts (assumed)

* `POST /users/check-credentials`

  ```json
  // Request:
  { "email": "user@example.com", "password": "P@ssw0rd!" }

  // Response 200:
  { "userId": "<uuid>", "roles": ["USER","ADMIN"], "valid": true }

  // Response 401:
  { "error": "invalid_credentials" }
  ```
* `GET /users/{id}`

  ```json
  // Response 200:
  { "id": "<uuid>", "roles": ["USER","ADMIN"], "active": true }
  ```

---

**Owner:** LMS Team
**Service:** `authservice`
**Default port:** `8088`
**Contact:** drop issues in the repository
--------------------------------------------------------------
# Auth Service (LMS Project)

Сервис аутентификации и выдачи токенов для платформы LMS.
Выдаёт **JWT access** и **JWT refresh** токены, хранит **сессии refresh** в БД (только хэш токена), интегрируется с `userservice` для проверки логина/пароля и получения ролей.

---

## Содержание

* [Общее описание](#общее-описание)
* [Архитектура](#архитектура)
* [Модель JWT](#модель-jwt)
* [API](#api)
* [Модель ошибок](#модель-ошибок)
* [Модель данных](#модель-данных)
* [Безопасность](#безопасность)
* [Конфигурация](#конфигурация)
* [Запуск и профили](#запуск-и-профили)
* [Swagger / OpenAPI](#swagger--openapi)
* [Тестирование](#тестирование)
* [Разбор проблем](#разбор-проблем)
* [Планы на будущее](#планы-на-будущее)
* [Приложение — контракты userservice](#приложение--контракты-userservice)

---

## Общее описание

* **Access-токен** (короткоживущий, stateless) — проверяется ресурс-сервисами.
* **Refresh-токен** (длинный) — **ротируется** при каждом обновлении, в БД хранится запись о сессии (только **хэш** refresh JWT).
* **/login** — проверяет креды через `userservice`, создаёт refresh-сессию, возвращает пару `{access, refresh}`.
* **/refresh** — валидирует старый refresh, **атомарно** помечает его как использованный, создаёт новую сессию, возвращает новую пару.
* **/logout** — завершает одну refresh-сессию; **/logout-all** — отзывает все активные сессии пользователя.

---

## Архитектура

```
[client] → /auth/login  → [authservice] → /users/check-credentials (userservice)
                                       ↘ БД: refresh_token_session
[client] → /auth/refresh → [authservice] (parse + rotate) ↔ БД
[client] → /auth/logout  → [authservice] (expire one)     ↔ БД
[client] → /auth/logout-all → [authservice] (revoke all)  ↔ БД
```

### Сервисы

* **authservice** (этот репозиторий): выдаёт/ротирует токены, ведёт учёт сессий refresh.
* **userservice** (внешний сервис):

    * `POST /users/check-credentials` → `{ userId, roles[], valid }`
    * `GET  /users/{id}` → `{ id, roles[], active }` (пере-проверка в `/refresh`)

---

## Модель JWT

### Алгоритм и проверки

* Библиотека: **jjwt** (io.jsonwebtoken)
* Подпись: **HS256** (секрет ≥ 32 байт)
* Допуск рассинхрона часов: **60s**
* Обязательные проверки: `iss`, `aud`, `sub`, `exp`, `jti`
* Для access: обязательный `roles` (массив строк или CSV-строка)
* Для refresh: обязательный `sid` (UUID сессии, равен `id` в БД)

### Пример payload access-токена

```json
{
  "iss": "lms-auth",
  "aud": ["lms-api"],
  "sub": "7d91b4f5-1a7a-4b71-9b4b-9a1c1b7b4a11",   // userId
  "iat": 1730400000,
  "exp": 1730400900,
  "jti": "f9a1b2c3-d4e5-6789-abcd-ef0123456789",
  "roles": ["USER","ADMIN"]
}
```

### Пример payload refresh-токена

```json
{
  "iss": "lms-auth",
  "aud": ["lms-api"],
  "sub": "7d91b4f5-1a7a-4b71-9b4b-9a1c1b7b4a11",   // userId
  "iat": 1730400000,
  "exp": 1731600000,
  "jti": "1a2b3c4d-5e6f-7890-abcd-ef0123456789",
  "sid": "c3b5b6c2-7e01-4f9a-9fd5-1e8bca8468a0"    // id сессии refresh в БД
}
```

### Клеймы кратко

* **Общие:** `iss`, `aud`, `iat`, `exp`, `sub`, `jti`
* **Только access:** `roles: string[] | string`
* **Только refresh:** `sid: string (uuid)`

---

## API

База: `http://localhost:8084/api/v1/auth`

> В самом **authservice** все эндпоинты `/auth/**` открыты (permitAll).
> Проверку access-токена делают **ресурс-сервисы** через свой JWT-фильтр.

### POST `/login`

Проверка кредов в `userservice`, создание refresh-сессии, выдача пары токенов.

**Запрос**

```json
{
  "email": "user@example.com",
  "password": "P@ssw0rd!"
}
```

**Ответ 200**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
}
```

**Ошибки (401)**

* `invalid_credentials`
* `user_inactive`
* `user_service_empty_response`

---

### POST `/refresh`

Ротация refresh-токена: валидация старого, атомарная пометка как использованного, создание новой сессии и выдача новой пары.

**Запрос**

```json
{
  "oldRefreshToken": "eyJhbGciOiJIUzI1NiIs..."
}
```

**Ответ 200**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...new...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...new..."
}
```

**Ошибки (401)**

* `invalid_token`, `expired_token`
* `invalid_audience`, `invalid_subject`, `invalid_jti`, `invalid_expiration`
* `invalid_sid`, `sid_user_mismatch`
* `invalid_status`, `refresh_reuse_detected`
* `invalid_refresh_hash`
* `user_inactive`

---

### POST `/logout`

Завершение **одной** refresh-сессии (идемпотентно).

**Запрос**

```json
{
  "oldRefreshToken": "eyJhbGciOiJIUzI1NiIs..."
}
```

**Ответ**: `204 No Content`

**Ошибки (401)**

* `invalid_sid` (если по `sid` сессия не найдена)

---

### POST `/logout-all`

Отзыв **всех ACTIVE** сессий пользователя.

**Запрос**

```json
{
  "userId": "7d91b4f5-1a7a-4b71-9b4b-9a1c1b7b4a11"
}
```

**Ответ**: `204 No Content`

---

### Примеры cURL

```bash
# Login
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"P@ssw0rd!"}'

# Refresh
curl -X POST http://localhost:8081/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"oldRefreshToken":"<refresh>"}'

# Logout (одна сессия)
curl -X POST http://localhost:8081/api/v1/auth/logout \
  -H "Content-Type: application/json" \
  -d '{"oldRefreshToken":"<refresh>"}'

# Logout all
curl -X POST http://localhost:8081/api/v1/auth/logout-all \
  -H "Content-Type: application/json" \
  -d '{"userId":"<userId>"}'
```

---

## Модель ошибок

Глобальный `@RestControllerAdvice` возвращает ошибки в формате:

```json
{
  "error": "invalid_token",
  "message": "Опционально: человекочитаемая детализация",
  "timestamp": "2025-10-01T18:25:43Z"
}
```

**401 Unauthorized** — всё, что связано с токенами/сессиями/аутентификацией:

* `invalid_token`, `expired_token`, `invalid_audience`, `invalid_subject`,
  `invalid_jti`, `invalid_expiration`
* `invalid_sid`, `sid_user_mismatch`, `invalid_status`, `refresh_reuse_detected`
* `invalid_refresh_hash`, `expired_refresh`
* `invalid_credentials`, `bad_credentials`, `user_inactive`, `logout_reuse_detected`

**400 Bad Request** — ошибки валидации входных DTO (если включишь `@Valid`).

**500 Internal Server Error** — неожиданные ошибки.

---

## Модель данных

### Таблица: `refresh_token_session`

Учёт refresh-сессий. Хранится **только хэш** refresh-токена.

| Колонка      | Тип         | Примечание                                         |
| ------------ | ----------- | -------------------------------------------------- |
| `id`         | `uuid` (PK) | **sid**; так же лежит в refresh JWT (`sid` claim)  |
| `user_id`    | `uuid`      | пользователь-владелец                              |
| `token_hash` | `text`      | Base64(SHA-256(rawRefreshJWT))                     |
| `status`     | `varchar`   | `ACTIVE` / `ALREADY_USED` / `EXPIRED` / `REVOKED`  |
| `created_at` | `timestamp` | `@CreatedDate` (auditing)                          |
| `expires_at` | `timestamp` | время истечения сессии (соответствует refresh TTL) |

> Никогда не храни «сырой» refresh JWT — только хэш.

### Ключевые методы репозитория

* `int markActiveAsAlreadyUsed(UUID sid)` — защита ротации
  Обновляет `status=ALREADY_USED` **только если** текущий статус `ACTIVE`.
  Возвращает `1`, если обновлена одна строка; `0`, если кто-то уже успел.
* `int expireIfActive(UUID sid)` — идемпотентный logout одной сессии.
  Обновляет `status=EXPIRED` **только если** текущий статус `ACTIVE`.
* `void revokeAllActiveByUserId(UUID userId)` — отзыв всех активных сессий.

---

## Безопасность

Сам **authservice** **не** проверяет access-токены (он их **выдаёт**).
Проверку access выполняют **ресурс-сервисы** (через свой `OncePerRequestFilter`: читать `Authorization: Bearer ...`, `TokenService.parseAccess(...)`, класть `Authentication` с ролями).

SecurityConfig в authservice:

* Stateless: `SessionCreationPolicy.STATELESS`
* CSRF выключен, form/basic выключены
* Swagger и `/api/v1/auth/**` — `permitAll`
* (Позже) добавить CORS для фронта

Разрешённые пути:

```
/api/v1/auth/**
/v3/api-docs/**
/swagger-ui/**
/swagger-ui.html
```

---

## Конфигурация

### Основные свойства (`application.yml`)

```yaml
server:
  port: 8084

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/lms_auth
    username: postgres
    password: postgres
  jpa:
    hibernate:
      ddl-auto: update          # только для dev; в prod использовать миграции
    properties:
      hibernate:
        jdbc:
          time_zone: UTC
  jackson:
    time-zone: UTC

auth:
  userservice:
    base-url: http://localhost:8082
  tokens:
    issuer: lms-auth
    audience: lms-api
    access:
      ttl: PT15M                # ISO-8601 (15 минут)
    refresh:
      ttl: P14D                 # 14 дней
    # секрет должен быть ≥ 32 байт сырых или Base64 (декодируется автоматически)
    secret: "change-me-please-32bytes-min-or-base64"
```

### Важные замечания

* **Secret**: минимум **32 байта** (256 бит) — сырые UTF-8 байты или Base64.
* **Время**: `Instant`/UTC.
* **TTL**: `auth.tokens.refresh.ttl` должен соответствовать `expires_at`, которое выставляете при создании записи в БД.

---

## Запуск и профили

### Dev (локальный Postgres)

1. Запусти docker-compose из infrastructure/.
2. Заполни свойства (или ENV-переменные).
3. Запуск:

   ```bash
   ./gradlew bootRun
   ```
4. База URL: `http://localhost:8081`

### Test (H2)

`src/test/resources/application-test.properties`:

```properties
spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=false
```

Активируй `@ActiveProfiles("test")` в тестах (или через Gradle ENV).

---

## Swagger / OpenAPI

Зависимость (Gradle):

```gradle
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0'
```

* UI: `http://localhost:8081/swagger-ui/index.html`
* JSON: `http://localhost:8081/v3/api-docs`

В конфиге OpenAPI объявлена схема **Bearer** — пригодится, когда ресурс-сервисы начнут авторизовываться access-токеном в Swagger UI.

---

## Тестирование

### Запуск тестов

```bash
./gradlew test
```

### Что покрыто (и что стоит добавить)

* **Unit (TokenServiceTest)**
  ✔ позитив: generate/parse access и refresh
  ➕ негатив: `invalid_audience`, `expired_token`, `missing_roles`, `wrong_signature`, «refresh как access» и наоборот

* **Repository (RefreshTokenSessionRepositoryTest)**
  ✔ двойной апдейт ротации: первый раз `1`, второй раз `0`
  ➕ тест на `expireIfActive` (второй вызов `0`)

* **Web / Controller**
  ✔ сейчас — через `@SpringBootTest(webEnvironment = MOCK) + @AutoConfigureMockMvc`
  ➕ ускоренный вариант через `@WebMvcTest(AuthController.class)` с:

    * `@MockitoBean AuthService`
    * исключение `SecurityConfig` и JPA автоконфигов при необходимости

### Частые ошибки

* `@WebMvcTest` должен указывать на **контроллер**, а не на класс теста.
* Если MVC-слайс случайно подхватывает Security/JPA — исключай:

    * `excludeFilters = @Filter(ASSIGNABLE_TYPE, classes = SecurityConfig.class)`
    * `@ImportAutoConfiguration(exclude = { DataSourceAutoConfiguration, HibernateJpaAutoConfiguration, JpaRepositoriesAutoConfiguration })`

---

## Разбор проблем

* **`invalid_audience` при парсинге** — проверь `auth.tokens.audience` в конфиге и в токенах.
* **`secret length less than 32 bytes`** — увеличь секрет (≥ 32 байт сырых или Base64).
* **Рефреш «переиспользуется»** — используй `markActiveAsAlreadyUsed(sid)` и проверяй, что вернулось `1`.
* **401 в Swagger (внутри authservice)** — в `SecurityConfig` должны быть `permitAll` на `/api/v1/auth/**` и Swagger пути.
* **В БД лежит сырой refresh** — это нарушение: должен лежать **только хэш** (Base64(SHA-256)).

---

## Планы на будущее

* Включить **CORS** для фронта (когда появится): `http://localhost:3000`, `http://localhost:5173`.
* **Rate limiting** на `/login`.
* **Pepper** для хэша refresh (секрет из ENV, добавляем перед SHA-256).
* **Метаданные сессии**: IP, userAgent — удобно для админки/аналитики.
* В ресурс-сервисах — **JwtAuthFilter**: читать `Authorization`, парсить access, класть `Authentication` с ролями → `@PreAuthorize` заработает.
* Миграции схемы (Flyway/Liquibase) вместо `ddl-auto`.
* Больше негативных тестов.

---

## Приложение — контракты userservice

* `POST /users/check-credentials`

  ```json
  // Request:
  { "email": "user@example.com", "password": "P@ssw0rd!" }

  // Response 200:
  { "userId": "<uuid>", "roles": ["USER","ADMIN"], "valid": true }

  // Response 401:
  { "error": "invalid_credentials" }
  ```

* `GET /users/{id}`

  ```json
  // Response 200:
  { "id": "<uuid>", "roles": ["USER","ADMIN"], "active": true }
  ```

---

**Владелец:** LMS Team
**Сервис:** `authservice`
**Порт по умолчанию:** `8084`
**Вопросы/багрепорты:** создавайте issue в репозитории
