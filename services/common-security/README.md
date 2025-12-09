# JWT Security Library (common-security)
**Author of service:** Bykov Lev  
**Documentation author:** Bykov Lev  

---

# Table of Contents
- [1. Overview](#1-overview)
- [2. Responsibilities](#2-responsibilities)
- [3. Architecture & Components](#3-architecture--components)
  - [3.1. Structure](#31-structure)
  - [3.2. Main Classes](#32-main-classes)
- [4. Usage in a Service](#4-usage-in-a-service)
  - [4.1. Add Dependency](#41-add-dependency)
  - [4.2. Configure JWT Properties](#42-configure-jwt-properties)
  - [4.3. SecurityConfig Example](#43-securityconfig-example)
  - [4.4. Using SecurityUtils](#44-using-securityutils)
- [5. Data Models](#5-data-models)
- [6. Security](#6-security)
- [7. Database](#7-database)
- [8. Dependencies](#8-dependencies)
- [9. Error Handling](#9-error-handling)
- [10. Building & Publishing](#10-building--publishing)
- [11. Common Use Cases](#11-common-use-cases)
- [12. Testing](#12-testing)
- [13. Troubleshooting](#13-troubleshooting)
- [14. Notes & Limitations](#14-notes--limitations)

---

# 1. Overview

`common-security` is a **shared JWT security library** for all LMS microservices.

It:

- Parses and validates **JWT access tokens**.
- Puts user data into Spring Security **SecurityContext**.
- Provides helper methods to read current user info.
- Helps protect controllers using `@PreAuthorize` and roles.

This library is used in resource services (users, courses, etc.), **not** in authservice for token generation.

---

# 2. Responsibilities

`common-security`:

- Reads JWT from `Authorization: Bearer <token>`.
- Validates token (signature and claims).
- Creates a `UserPrincipal` with user ID and roles.
- Sets `Authentication` in `SecurityContext`.
- Provides helper methods:
  - get current user ID
  - get roles
  - check if user is admin
  - check if user is authenticated
  - compare current user with given ID
- Defines common JWT-related exceptions.

---

# 3. Architecture & Components

## 3.1. Structure

```text
common-security/
└── src/main/java/ru/lms_project/common/security/
    ├── JwtAuthenticationFilter.java
    ├── JwtTokenProvider.java
    ├── ParsedToken.java
    ├── SecurityUtils.java
    ├── UserPrincipal.java
    └── exceptions/
        ├── JwtTokenException.java
        ├── TokenValidationException.java
        └── UnauthorizedException.java
````

## 3.2. Main Classes

### ParsedToken

* Simple data class.
* Fields:

   * `UUID userId`
   * `List<String> roles`
   * raw access token string
* Used as result of parsing.

### JwtTokenProvider

* Main class for **parsing access tokens**.
* Method: `ParsedToken parseAccessToken(String token)`

   * validates token (signature, issuer, audience, expiration, etc.)
   * returns `ParsedToken` with user data and roles
   * throws JWT-related exceptions if token is invalid

### UserPrincipal

* Implements `UserDetails`.
* Used inside Spring Security `Authentication`.
* Behavior:

   * `getUsername()` → userId as String
   * `getAuthorities()` → roles with `ROLE_` prefix
   * `getPassword()` → `null` (we do not use password here)
   * `isAccountNonExpired/NonLocked/CredentialsNonExpired/Enabled()` → `true`
     (real checks are done in authservice on login)

### JwtAuthenticationFilter

* Servlet filter (`OncePerRequestFilter`).
* Runs for every HTTP request.
* Steps:

   1. Reads `Authorization` header.
   2. If header is like `Bearer <token>` → parse via `JwtTokenProvider`.
   3. If token is valid:

      * creates `UserPrincipal`
      * creates `Authentication`
      * puts it into `SecurityContextHolder`.
   4. If no token or invalid token → **does not throw**, just continues chain.
* This allows:

   * Some endpoints to be public (`permitAll`).
   * Other endpoints to require auth (`authenticated()` / `hasRole()`).

### SecurityUtils

Static helper methods:

* `UUID getCurrentUserId()`
* `List<String> getCurrentUserRoles()`
* `boolean isAdmin()`
* `boolean isAuthenticated()`
* `boolean isCurrentUser(UUID userId)`

Used in controllers and services to get current user info.

### Exceptions

* `JwtTokenException` — base JWT-related exception.
* `TokenValidationException` — for validation problems (issuer, audience, expired, etc.).
* `UnauthorizedException` — for explicit 401 cases if needed by services.

---

# 4. Usage in a Service

## 4.1. Add Dependency

Gradle example (multi-module):

```gradle
dependencies {
    implementation project(":services:common-security")
    // other dependencies...
}
```

Or, if published to Maven Local, use group/name/version from your build.

---

## 4.2. Configure JWT Properties

In `application.yml` of your service:

```yaml
auth:
  tokens:
    secret: ${JWT_SECRET:your-secret-key-min-32-bytes}
    issuer: http://localhost:8084
    audience: lms-api
```

**Important:**

* **All services must use the same:**

   * `secret`
   * `issuer`
   * `audience`
* Values must match what **authservice** uses to issue tokens.

---

## 4.3. SecurityConfig Example

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/public/**").permitAll()

                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}
```

If your service is in a different package, make sure that `@ComponentScan` can see `ru.lms_project.common.security`.

---

## 4.4. Using SecurityUtils

Example controller:

```java
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public UserDTO getMyProfile() {
        UUID userId = SecurityUtils.getCurrentUserId();
        return userService.findById(userId);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public UserDTO createUser(@RequestBody UserDTO dto) {
        return userService.create(dto);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable UUID id) {
        if (!SecurityUtils.isAdmin() && !SecurityUtils.isCurrentUser(id)) {
            throw new UnauthorizedException("Cannot delete other users");
        }
        userService.delete(id);
    }
}
```

---

# 5. Data Models

### ParsedToken

Concept:

```java
public class ParsedToken {
    private UUID userId;
    private List<String> roles;
    private String rawToken;
    // getters...
}
```

Data in the token (claims):

* `sub` — user ID
* `roles` — list of roles (e.g. `["USER","ADMIN"]`)
* Standard JWT fields:

   * `iss`, `aud`, `exp`, `iat`, `jti`

This library only **reads** these fields, it does not create tokens.

---

# 6. Security

* Uses **JWT access tokens** from authservice.

* Tokens come from header:

  ```text
  Authorization: Bearer <accessToken>
  ```

* `JwtTokenProvider` validates:

   * signature with shared secret
   * `issuer`
   * `audience`
   * `expiration`
   * other required claims

* Roles from token are mapped to Spring authorities:

   * Role `ADMIN` → authority `ROLE_ADMIN`
   * Role `USER` → authority `ROLE_USER`

Controllers can use:

* `@PreAuthorize("hasRole('ADMIN')")`
* `@PreAuthorize("isAuthenticated()")`
* `@PreAuthorize("hasAnyRole('USER','ADMIN')")`

---

# 7. Database

This library does **not** work with any database directly.

All refresh token storage and sessions are handled in **authservice**, not here.

---

# 8. Dependencies

Main runtime dependencies (conceptual):

* Spring Security
* Spring Web (for filters)
* JWT library used in `JwtTokenProvider` (e.g. `jjwt`)
* Lombok (optional, if used in model classes)

Project will also depend on:

* The same JWT settings as authservice (`secret`, `issuer`, `audience`).

---

# 9. Error Handling

This library:

* Throws custom exceptions (`JwtTokenException`, `TokenValidationException`) when parsing fails.
* `JwtAuthenticationFilter` **does not** throw for every invalid token by default:

   * Often it just does not set authentication and lets security rules handle it.
* Your service can decide:

   * to treat missing authentication as 401 / 403 via Spring Security config,
   * or to throw `UnauthorizedException` in code when `SecurityUtils` is used without a user.

Typical HTTP results (from resource service):

* 401 — no token or invalid token when endpoint requires authentication.
* 403 — token valid but user has no required role.

---

# 10. Building & Publishing

From monorepo root:

```bash
# Build library
./gradlew :services:common-security:build

# Publish to local Maven
./gradlew :services:common-security:publishToMavenLocal
```

After that other services can depend on it via:

```gradle
implementation "ru.lms_project:common-security:<version>"
```

(or via `project(...)` in multi-module setup).

---

# 11. Common Use Cases

### Get current user ID

```java
UUID currentUserId = SecurityUtils.getCurrentUserId();
```

### Check if user is admin

```java
if (SecurityUtils.isAdmin()) {
    // admin-only logic
}
```

### Check access to resource

```java
public void updateProfile(UUID userId, ProfileDTO dto) {
    if (!SecurityUtils.isCurrentUser(userId) && !SecurityUtils.isAdmin()) {
        throw new UnauthorizedException("Cannot update other user's profile");
    }
    // update logic...
}
```

### Use annotations in service methods

```java
@PreAuthorize("hasRole('ADMIN')")
public void adminOnlyMethod() { }

@PreAuthorize("isAuthenticated()")
public void authRequiredMethod() { }
```

---

# 12. Testing

### Manual test with cURL

```bash
# 1. Login via authservice
curl -X POST http://localhost:8084/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"password"}'

# Response:
# { "accessToken": "eyJhbGc...", "refreshToken": "..." }

TOKEN="eyJhbGc..."

# 2. Call protected endpoint on some service
curl http://localhost:8082/api/v1/users/me \
  -H "Authorization: Bearer $TOKEN"
```

Check cases:

* No token to public endpoint → should work.
* No token to protected endpoint → should return 401.
* Valid token → should work.
* Expired token → should return 401.
* Token with wrong signature/secret → should return 401.

---

# 13. Troubleshooting

### Problem: "User not authenticated"

**Cause:** `SecurityUtils.getCurrentUserId()` called but there is no authenticated user.

**Check:**

* Request sends header `Authorization: Bearer <token>`.
* Token is valid and not expired.
* `JwtAuthenticationFilter` is registered in `SecurityConfig`.
* Path is not marked as `permitAll` if you expect authentication.

---

### Problem: "Invalid token"

**Possible reasons:**

* Wrong **secret** in this service.
* Wrong **issuer** or **audience** config.
* Token is expired.
* Token is not an access token issued by authservice.

**Fix:**

* Make sure `auth.tokens.secret`, `issuer`, `audience` are the same as in authservice.
* Check time on server.
* Get a new token from authservice.

---

### Problem: Filter is not called

**Cause:** Spring cannot see `JwtAuthenticationFilter` bean.

**Fix:**

* Ensure `@Component` is present on filter class.
* Ensure component scan includes package:

  ```java
  @Configuration
  @ComponentScan(basePackages = {
      "ru.lms_project.common.security",
      "your.service.package"
  })
  public class SecurityConfig {
  }
  ```

---

### Problem: 401 on public endpoints

**Cause:** Security configuration does not have `permitAll` for these paths.

**Fix:**

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/auth/**", "/public/**").permitAll()
    .anyRequest().authenticated()
)
```

---

# 14. Notes & Limitations

This library **does NOT**:

* generate JWT tokens (only parses and validates),
* handle user registration or login,
* manage refresh tokens or refresh sessions,
* store anything in DB.

It only:

* validates **existing** access tokens,
* sets security context,
* provides helpers to read current user and roles.

---

Architecture idea:

```text
Request → JwtAuthenticationFilter → SecurityContext → Controller
                ↓
                JwtTokenProvider
                ↓
                ParsedToken
                ↓
                UserPrincipal
```

The goal is to follow:

* **DRY** — do not repeat JWT logic in each service.
* **Single source of truth** — fix bug once, all services are updated.
* **Consistent security** — same validation everywhere.

---