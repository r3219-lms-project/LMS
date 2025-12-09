# Auth Service
**Author of service:** Bykov Lev  
**Documentation author:** Bykov Lev  


# Table of Contents
- [1. Overview](#1-overview)
- [2. Responsibilities](#2-responsibilities)
- [3. Architecture & Components](#3-architecture--components)
  - [3.1. High-level Architecture](#31-high-level-architecture)
  - [3.2. Internal Components](#32-internal-components)
- [4. API](#4-api)
  - [4.1. POST /login](#41-post-login)
  - [4.2. POST /refresh](#42-post-refresh)
  - [4.3. POST /logout](#43-post-logout)
  - [4.4. POST /logout-all](#44-post-logout-all)
- [5. Data Models](#5-data-models)
  - [5.1. JWT Access Token](#51-jwt-access-token)
  - [5.2. JWT Refresh Token](#52-jwt-refresh-token)
  - [5.3. Refresh Session Table](#53-refresh-session-table)
- [6. Security](#6-security)
- [7. Database](#7-database)
- [8. Dependencies](#8-dependencies)
- [9. Error Handling](#9-error-handling)
- [10. Running the Service](#10-running-the-service)
  - [10.1. Profiles](#101-profiles)
  - [10.2. Configuration Properties](#102-configuration-properties)
- [11. Examples](#11-examples)

---

# 1. Overview

Auth Service is responsible for **authentication** and **token issuing** in the LMS platform.

It works with two types of tokens:

- **Access token**  
  Short-lived, stateless, used by resource services for authorization.

- **Refresh token**  
  Longer-lived, stored only as a **hash** in DB in a **refresh session**.  
  On each refresh call, the old refresh is **rotated** and marked as used.

Main flows:

- **Login** — check credentials using `userservice`, create refresh session, return `{accessToken, refreshToken}`.
- **Refresh** — validate old refresh token, mark its session as used, create a new session, return new pair.
- **Logout** — close one refresh session.
- **Logout all** — close all active sessions for a user.

Base URL (example):  
`http://localhost:8084/api/v1/auth`

---

# 2. Responsibilities

Auth Service:

- Validates user credentials via **userservice**.
- Generates **JWT access** and **JWT refresh** tokens.
- Stores **refresh sessions** in the database (only **hash** of token).
- Rotates refresh tokens in a safe way.
- Expires a single session or all sessions for a user.
- Validates refresh tokens (claims, audience, issuer, session status, hash).
- Exposes simple HTTP API for login/refresh/logout.

It does **not** validate access tokens of other services.  
Resource services must validate access tokens themselves.

---

# 3. Architecture & Components

## 3.1. High-level Architecture

```text
[client] → /auth/login  → [authservice] → /users/check-credentials (userservice)
                                       ↘ DB: refresh_token_session

[client] → /auth/refresh → [authservice] (parse + rotate) ↔ DB
[client] → /auth/logout  → [authservice] (expire one)     ↔ DB
[client] → /auth/logout-all → [authservice] (revoke all)  ↔ DB
````

**Services involved:**

* **authservice** (this service):
  issues and rotates tokens, stores refresh sessions in DB.

* **userservice** (external):

  * `POST /users/check-credentials` → `{ userId, roles[], valid }`
  * `GET /users/{id}` → `{ id, roles[], active }` (used during refresh to re-check user status and roles)

---

## 3.2. Internal Components

High-level internal parts (names may vary in code):

* **Controllers**

  * `AuthController` — exposes `/login`, `/refresh`, `/logout`, `/logout-all`.

* **Services**

  * `AuthService` / `TokenService` — main logic for issuing and parsing tokens.
  * `RefreshTokenSessionService` — works with DB table `refresh_token_session`.
  * `UserClient` or similar — HTTP client for `userservice`.

* **Repositories**

  * `RefreshTokenSessionRepository` — JPA repository for refresh sessions.

* **Config**

  * `SecurityConfig` — stateless security, permit `/api/v1/auth/**` and Swagger.
  * `AuthProperties` — issuer, audience, TTL for access/refresh, secret key.

---

# 4. API

Base path:
`/api/v1/auth`

All endpoints are **permitAll** inside Auth Service.
Resource services will protect their own endpoints using access tokens.

---

## 4.1. POST `/login`

Validate user credentials, create a refresh session, return token pair.

**Request:**

```json
{
  "email": "user@example.com",
  "password": "P@ssw0rd!"
}
```

**Response 200:**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
}
```

**Error 401 examples:**

* `invalid_credentials`
* `user_inactive`
* `user_service_empty_response`

---

## 4.2. POST `/refresh`

Rotate refresh token.
Steps:

1. Parse old refresh token.
2. Validate claims (`iss`, `aud`, `sub`, `jti`, `sid`, `exp`).
3. Check DB session by `sid`.
4. Mark old session as used (only if status = ACTIVE).
5. Create new session.
6. Return new access and refresh tokens.

**Request:**

```json
{
  "oldRefreshToken": "eyJhbGciOiJIUzI1NiIs..."
}
```

**Response 200:**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...new...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...new..."
}
```

**Error 401 examples:**

* `invalid_token`, `expired_token`
* `invalid_audience`, `invalid_subject`, `invalid_jti`, `invalid_expiration`
* `invalid_sid`, `sid_user_mismatch`
* `invalid_status`, `refresh_reuse_detected`
* `invalid_refresh_hash`
* `user_inactive`

---

## 4.3. POST `/logout`

Close **one** refresh session (for given refresh token).

**Request:**

```json
{
  "oldRefreshToken": "eyJhbGciOiJIUzI1NiIs..."
}
```

**Response:**
`204 No Content`

The call is safe to repeat: if the session is already closed, nothing breaks.

**Error 401 examples:**

* `invalid_sid` (no session found for `sid`)

---

## 4.4. POST `/logout-all`

Close **all active sessions** for a user.

**Request:**

```json
{
  "userId": "7d91b4f5-1a7a-4b71-9b4b-9a1c1b7b4a11"
}
```

**Response:**
`204 No Content`

---

# 5. Data Models

## 5.1. JWT Access Token

Library: **jjwt** (`io.jsonwebtoken`).
Algorithm: **HS256** with secret length **≥ 32 bytes**.
Clock skew tolerance: about **60 seconds**.

**Required checks:**

* `iss`, `aud`, `sub`, `exp`, `jti`
* `roles` claim (list of roles)

**Example payload:**

```json
{
  "iss": "lms-auth",
  "aud": ["lms-api"],
  "sub": "7d91b4f5-1a7a-4b71-9b4b-9a1c1b7b4a11",
  "iat": 1730400000,
  "exp": 1730400900,
  "jti": "f9a1b2c3-d4e5-6789-abcd-ef0123456789",
  "roles": ["USER","ADMIN"]
}
```

---

## 5.2. JWT Refresh Token

Refresh token also uses **HS256** and same base claims.
It adds a `sid` claim — ID of the refresh session in DB.

**Example payload:**

```json
{
  "iss": "lms-auth",
  "aud": ["lms-api"],
  "sub": "7d91b4f5-1a7a-4b71-9b4b-9a1c1b7b4a11",
  "iat": 1730400000,
  "exp": 1731600000,
  "jti": "1a2b3c4d-5e6f-7890-abcd-ef0123456789",
  "sid": "c3b5b6c2-7e01-4f9a-9fd5-1e8bca8468a0"
}
```

**Claims summary:**

* Common: `iss`, `aud`, `iat`, `exp`, `sub`, `jti`
* Access-only: `roles`
* Refresh-only: `sid`

---

## 5.3. Refresh Session Table

Table: `refresh_token_session`

Stores refresh **sessions**, not raw tokens.

| Column       | Type        | Description                                       |
| ------------ | ----------- | ------------------------------------------------- |
| `id`         | `uuid` (PK) | equals `sid` in refresh token                     |
| `user_id`    | `uuid`      | owner of the session                              |
| `token_hash` | `text`      | Base64(SHA-256(rawRefreshJWT))                    |
| `status`     | `varchar`   | `ACTIVE` / `ALREADY_USED` / `EXPIRED` / `REVOKED` |
| `created_at` | `timestamp` | created time (UTC, auditing)                      |
| `expires_at` | `timestamp` | when session should expire                        |

**Important:** raw refresh token must **never** be stored in DB.

Key repository methods (conceptual):

* `markActiveAsAlreadyUsed(sid)` — set `status=ALREADY_USED` only if `status=ACTIVE`.
* `expireIfActive(sid)` — set `status=EXPIRED` only if `status=ACTIVE`.
* `revokeAllActiveByUserId(userId)` — set `status=REVOKED` for all active sessions.

---

# 6. Security

Auth Service:

* Issues access and refresh tokens.
* Does **not** check access tokens on resource services.
* Works in **stateless** mode.

Security configuration:

* CSRF disabled.
* HTTP session is not used.
* Form login and basic auth are disabled.
* These paths are allowed for all:

  * `/api/v1/auth/**`
  * `/v3/api-docs/**`
  * `/swagger-ui/**`
  * `/swagger-ui.html`

Resource services should:

* read `Authorization: Bearer <accessToken>` header,
* parse access token with the same secret and expected `issuer` and `audience`,
* put user data and roles into security context.

---

# 7. Database

* **Database:** PostgreSQL (for refresh sessions).
* **Main table:** `refresh_token_session` (described above).
* In dev you can use `ddl-auto=update`, but in production use migrations (Flyway/Liquibase).

---

# 8. Dependencies

Main libraries (conceptual):

* Spring Boot (Web / WebMVC)
* Spring Security
* Spring Data JPA
* PostgreSQL driver
* `jjwt` (JWT library)
* `springdoc-openapi` (for Swagger/OpenAPI UI)
* H2 (for tests, optional)

---

# 9. Error Handling

Errors are returned in a simple JSON format, for example:

```json
{
  "error": "invalid_token",
  "message": "Optional human-readable message",
  "timestamp": "2025-10-01T18:25:43Z"
}
```

**HTTP 401 Unauthorized** — any token/session/authentication problem:

* `invalid_token`, `expired_token`
* `invalid_audience`, `invalid_subject`, `invalid_jti`, `invalid_expiration`
* `invalid_sid`, `sid_user_mismatch`
* `invalid_status`, `refresh_reuse_detected`
* `invalid_refresh_hash`, `expired_refresh`
* `invalid_credentials`, `bad_credentials`, `user_inactive`, `logout_reuse_detected`

**HTTP 400 Bad Request** — validation errors for request DTOs.

**HTTP 500 Internal Server Error** — unexpected errors.

---

# 10. Running the Service

Typical commands (Gradle):

```bash
./gradlew clean build
./gradlew bootRun
```

Service port example:

```yaml
server:
  port: 8084
```

---

## 10.1. Profiles

* **Dev profile**

  * Uses PostgreSQL.
  * `ddl-auto=update` (for local development).
* **Test profile**

  * Uses in-memory H2 DB.
  * `ddl-auto=create-drop`.

Example test properties:

```properties
spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=false
```

---

## 10.2. Configuration Properties

Example `application.yml`:

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
      ddl-auto: update     # dev only
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
      ttl: PT15M           # 15 minutes
    refresh:
      ttl: P14D            # 14 days
    secret: "change-me-please-32bytes-min-or-base64"
```

Notes:

* `secret` must be at least **32 bytes** (raw) or a strong Base64 string.
* All time handling is in **UTC**.
* `refresh.ttl` should match how you set `expires_at` in DB.

---

# 11. Examples

### Login

```bash
curl -X POST http://localhost:8084/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"P@ssw0rd!"}'
```

### Refresh

```bash
curl -X POST http://localhost:8084/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"oldRefreshToken":"<refresh>"}'
```

### Logout (one session)

```bash
curl -X POST http://localhost:8084/api/v1/auth/logout \
  -H "Content-Type: application/json" \
  -d '{"oldRefreshToken":"<refresh>"}'
```

### Logout all sessions

```bash
curl -X POST http://localhost:8084/api/v1/auth/logout-all \
  -H "Content-Type: application/json" \
  -d '{"userId":"<userId>"}'
```

---