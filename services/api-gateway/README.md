# API Gateway Service
**Author of service:** Bykov Lev  
**Documentation author:** Bykov Lev  

---

# Table of Contents
- [1. Overview](#1-overview)
- [2. Responsibilities](#2-responsibilities)
- [3. Architecture & Components](#3-architecture--components)
  - [3.1. High-level Architecture](#31-high-level-architecture)
  - [3.2. Internal Components](#32-internal-components)
- [4. Routes](#4-routes)
- [5. Data Models](#5-data-models)
- [6. Security](#6-security)
  - [6.1. JWT Validation](#61-jwt-validation)
  - [6.2. Gateway Filters](#62-gateway-filters)
  - [6.3. Spring WebFlux Security](#63-spring-webflux-security)
- [7. Database](#7-database)
- [8. Dependencies](#8-dependencies)
- [9. Error Handling](#9-error-handling)
- [10. Running the Service](#10-running-the-service)
- [11. Examples](#11-examples)

---

# 1. Overview
API Gateway is the main entry point of the LMS system.  
It receives all client requests, checks authentication if needed, and forwards requests to internal microservices.

The gateway does not contain business logic.  
Its main job is routing and simple security.

---

# 2. Responsibilities
- Route requests to microservices.  
- Validate JWT tokens for protected routes.  
- Add `X-User-Id` and `X-User-Roles` headers.  
- Apply CORS rules.  
- Log requests and errors.

---

# 3. Architecture & Components

## 3.1. High-level Architecture
```

Client → API Gateway → Users Service
→ Courses Service
→ Auth Service
→ Review Service
→ Modules Service
→ Lessons Service
→ Progress Service
→ Notification Service

````

Uses WebFlux + Spring Cloud Gateway.  
No database.

---

## 3.2. Internal Components

### **AuthGatewayFilterFactory**
- Requires JWT for all requests.
- Validates token.
- Adds user headers.

### **ConditionalAuthGatewayFilterFactory**
- GET → allowed without token.  
- Other methods → require JWT.

### **SecurityConfig**
- CSRF disabled.  
- All routes permitted (authorization handled by filters).

### **application.yml**
- Route definitions.  
- Filter bindings (`Auth`, `ConditionalAuth`).  
- Global CORS settings.  
- Logging config.

---

# 4. Routes

| Service | Path | Target | Filter |
|--------|------|--------|--------|
| userservice | `/api/v1/users/**` | `http://localhost:8082` | Auth |
| courseservice | `/api/v1/courses/**` | `http://localhost:8083` | ConditionalAuth |
| authservice | `/api/v1/auth/**` | `http://localhost:8084` | none |
| reviewservice | `/api/v1/reviews` | `http://localhost:8085` | none |
| modulesservice | `/api/v1/modules` | `http://localhost:8087` | ConditionalAuth |
| lessonsservice | `/api/v1/lessons` | `http://localhost:8087` | ConditionalAuth |
| progressservice | `/api/v1/` | `http://localhost:8088` | Auth |
| notificationservice | `/api/v1/notifications` | `http://localhost:8089` | Auth |

---

# 5. Data Models
The gateway has no DTOs or entities.

It uses **ParsedToken** from common-security:

```json
{
  "userId": "uuid-string",
  "roles": ["USER", "ADMIN"]
}
````

---

# 6. Security

## 6.1. JWT Validation

Token is taken from:

```
Authorization: Bearer <token>
```

If valid:

* request continues
* user headers added

If invalid or missing:

* `401 Unauthorized`

Headers added:

```
X-User-Id
X-User-Roles
```

---

## 6.2. Gateway Filters

### **Auth**

Always requires token.

### **ConditionalAuth**

Requires token only for:

* POST
* PUT
* DELETE

GET is always allowed.

---

## 6.3. Spring WebFlux Security

* CSRF disabled.
* All routes permitted.
* Real security handled in filters.

---

# 7. Database

No database used.

---

# 8. Dependencies

* Spring Cloud Gateway
* Spring WebFlux
* Spring Security WebFlux
* Reactor Netty
* common-security module (JwtTokenProvider, ParsedToken)

---

# 9. Error Handling

Gateway returns:

```
401 Unauthorized
```

Reasons:

* Missing token
* Invalid token
* Expired token

Routing problems are handled by Spring Cloud Gateway.

---

# 10. Running the Service

```bash
gradle :services:api-gateway:build
gradle :services:api-gateway:bootRun
```

### Environment variables:

```
auth.tokens.secret=mysuperlongsecretkey_for_dev_1234567890
auth.tokens.issuer=http://localhost:8084
auth.tokens.audience=lms-api
```

### Port:

```
8080
```

---

# 11. Examples

### Authenticated request:

```bash
curl -H "Authorization: Bearer <token>" \
     http://localhost:8080/api/v1/users/me
```

### Unauthorized request:

```bash
curl http://localhost:8080/api/v1/users/me
```

Response:

```
401 Unauthorized
```

---