## Documentation for JWT Security Library (common-security)

### What is this?
When we want to create a request where the user must be authorized or must have admin role - we need to check their JWT access token. This library helps you with that.

This is a **shared library** that all microservices use for JWT authentication. 
----

### Structure
```
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
```

----

### How it works?

1. **ParsedToken** - Simple data class that contains `userId`, list of `roles`, and the access token string.

2. **JwtTokenProvider** - Main class for JWT parsing. Method `parseAccessToken(token)` reads the token and returns `ParsedToken` with user data.

3. **UserPrincipal** - Implements Spring Security's `UserDetails` interface.
    - `getPassword()` returns `null` (JWT doesn't need password)
    - `isAccountNonExpired/Locked/etc` return `true` (we check this in auth service)
    - `getUsername()` returns userId as String
    - `getAuthorities()` returns user roles with "ROLE_" prefix

4. **JwtAuthenticationFilter** - Servlet filter that intercepts every request:
    - Extracts JWT from `Authorization: Bearer <token>` header
    - Validates token using `JwtTokenProvider`
    - Sets authentication in `SecurityContext` if token is valid
    - **Important:** If no token or invalid token - just passes request further (doesn't throw error!)

5. **SecurityUtils** - Helper class with static methods to get current user info:
    - `getCurrentUserId()` - returns current user's UUID
    - `getCurrentUserRoles()` - returns list of user roles
    - `isAdmin()` - checks if current user is admin
    - `isAuthenticated()` - checks if user is logged in
    - `isCurrentUser(UUID userId)` - checks if given userId is current user

----

### How to use in your service?

#### Step 1: Add dependency to your service

In your service's `build.gradle`:
```gradle
dependencies {
    implementation project(':services:common-security')
    // ... other dependencies
}
```

#### Step 2: Configure application.yml

Add these properties (MUST be same in all services!):
```yaml
auth:
  tokens:
    secret: ${JWT_SECRET:your-secret-key-min-32-bytes}
    issuer: http://localhost:8084
    audience: lms-api
```

⚠️ **Important:** All services must use the SAME secret, issuer, and audience!

#### Step 3: Create SecurityConfig in your service
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                // Public endpoints (no auth required)
                .requestMatchers("/public/**").permitAll()
                
                // Protected endpoints (auth required)
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, 
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

#### Step 4: Use SecurityUtils in your controllers
```java
@RestController
@RequestMapping("/users")
public class UserController {

    @GetMapping("/me")
    public UserDTO getMyProfile() {
        UUID userId = SecurityUtils.getCurrentUserId();
        return userService.findById(userId);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")  // Only admins
    public UserDTO createUser(@RequestBody UserDTO dto) {
        // ...
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable UUID id) {
        // Check if user deletes themselves or is admin
        if (!SecurityUtils.isAdmin() && !SecurityUtils.isCurrentUser(id)) {
            throw new ForbiddenException("Cannot delete other users");
        }
        userService.delete(id);
    }
}
```

----

### How to build?

From the main repo root:
```bash
# Build library
gradle :services:common-security:build

# Publish to local Maven (so other services can use it)
gradle :services:common-security:publishToMavenLocal
```

After this, your services can use the library!

----

### Common Use Cases

#### Case 1: Get current user ID
```java
UUID currentUserId = SecurityUtils.getCurrentUserId();
```

#### Case 2: Check if user is admin
```java
if (SecurityUtils.isAdmin()) {
    // Admin-only logic
}
```

#### Case 3: Check if user can access resource
```java
public void updateProfile(UUID userId, ProfileDTO dto) {
    if (!SecurityUtils.isCurrentUser(userId) && !SecurityUtils.isAdmin()) {
        throw new ForbiddenException("Cannot update other user's profile");
    }
    // ...
}
```

#### Case 4: Using annotations
```java
@PreAuthorize("hasRole('ADMIN')")
public void adminOnlyMethod() { }

@PreAuthorize("isAuthenticated()")
public void authRequiredMethod() { }
```

----

### Testing

#### Test with cURL:
```bash
# 1. Login to get token
curl -X POST http://localhost:8084/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"password"}'

# Response: {"accessToken": "eyJhbGc...", ...}

# 2. Use token in request
TOKEN="eyJhbGc..."
curl http://localhost:8082/api/v1/users/me \
  -H "Authorization: Bearer $TOKEN"
```

#### Test endpoints:

✅ Request without token to public endpoint → should work  
✅ Request without token to protected endpoint → should get 401  
✅ Request with valid token → should work  
✅ Request with expired token → should get 401  
✅ Request with invalid token → should get 401

----

### Troubleshooting

#### Problem: "User not authenticated" exception
**Cause:** Calling `SecurityUtils.getCurrentUserId()` but no JWT in request  
**Solution:**
- Check JWT token is sent in `Authorization: Bearer <token>` header
- Check token is valid (not expired)
- Make sure `JwtAuthenticationFilter` is registered in SecurityConfig

#### Problem: "Invalid token" exception
**Cause:** JWT validation failed  
**Solution:**
- Check that all services use the SAME `auth.tokens.secret`
- Verify token is not expired (15 min default)
- Check issuer and audience match in all services

#### Problem: Filter not applied
**Cause:** Spring can't find the filter bean  
**Solution:** Add component scan:
```java
@Configuration
@ComponentScan(basePackages = {"ru.lms_project.common.security"})
public class SecurityConfig { }
```

#### Problem: 401 on public endpoints
**Cause:** SecurityConfig doesn't have `.permitAll()` for public paths  
**Solution:** Add public endpoints to SecurityConfig:
```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/auth/**", "/public/**").permitAll()
    .anyRequest().authenticated()
)
```

----

### For Developers: How to modify library

1. Make changes in `common-security` code
2. Build: `gradle :services:common-security:build`
3. Publish: `gradle :services:common-security:publishToMavenLocal`
4. Restart services that use it
5. Test changes

**Note:** If you change the library, ALL services that use it will need to rebuild!

----

### Security Notes

⚠️ **Never log JWT tokens!** They contain sensitive data  
⚠️ **Keep secret key safe** - use environment variables in production  
⚠️ **All services MUST use same secret** - otherwise tokens won't work  
⚠️ **Token lifetime is 15 minutes** - use refresh token to get new one

----

### What's NOT in this library?

❌ JWT generation (only in authservice)  
❌ User registration/login  
❌ Refresh token logic  
❌ Password hashing

This library only **validates** existing tokens!

----

### Questions?

Write me in telegram, @undndnwnkk

----

### Architecture Diagram
```
Request → JwtAuthenticationFilter → SecurityContext → Controller
                ↓
                JwtTokenProvider
                ↓
                ParsedToken
                ↓
                UserPrincipal
```
Why we need this library?

DRY principle - Don't Repeat Yourself
Single source of truth - fix bug once, all services fixed
Consistent security - same JWT validation everywhere
Easy to update - change library, all services get updated

Library created by: Bykov Lev, undndnwnkk
Author of documentation: Bykov Lev, undndnwnkk