# LMS Platform - Microservices Documentation

**Documentation Author:** Bykov Lev  
**Last Updated:** November 15, 2025  
**Version:** 1.0.0

---

## Table of Contents

1. [System Overview](#system-overview)
2. [Architecture](#architecture)
3. [Services](#services)
4. [Authentication & Security](#authentication--security)
5. [API Gateway](#api-gateway)
6. [Database Schema](#database-schema)
7. [Deployment](#deployment)
8. [Development Guide](#development-guide)

---

## System Overview

The LMS (Learning Management System) is a microservices-based platform designed for managing online courses, users, notifications, and student progress. The system uses Spring Boot for backend services, PostgreSQL and MongoDB for data storage, and implements JWT-based authentication.

### Key Features

- User management with role-based access control
- Course and lesson management
- Student progress tracking
- Real-time notifications
- Group management for organizing students
- Secure authentication with JWT tokens

### Technology Stack

- **Backend Framework:** Spring Boot 3.x
- **Databases:** PostgreSQL (relational data), MongoDB (courses, notifications)
- **API Gateway:** Spring Cloud Gateway
- **Security:** Spring Security + JWT
- **Documentation:** OpenAPI/Swagger
- **Build Tool:** Gradle/Maven

---

## Architecture

### Microservices Architecture Diagram

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │
       ▼
┌─────────────────┐
│  API Gateway    │ (Port 8080)
│  - Routing      │
│  - Auth Filter  │
└────────┬────────┘
         │
    ┌────┴─────────────────────────────────┐
    │                                       │
    ▼                                       ▼
┌─────────┐  ┌──────────┐  ┌────────────┐  ┌─────────────┐
│  Auth   │  │  User    │  │  Course    │  │ Notification│
│ Service │  │ Service  │  │  Service   │  │  Service    │
│  8084   │  │  8082    │  │   8083     │  │    8089     │
└────┬────┘  └────┬─────┘  └─────┬──────┘  └──────┬──────┘
     │            │               │                 │
     ▼            ▼               ▼                 ▼
┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐
│PostgreSQL│  │PostgreSQL│  │ MongoDB  │  │ MongoDB  │
└──────────┘  └──────────┘  └──────────┘  └──────────┘

    ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
    │   Progress   │  │    Group     │  │   Modules/   │
    │   Service    │  │   Service    │  │   Lessons    │
    │    8088      │  │    8086      │  │    8087      │
    └──────┬───────┘  └──────┬───────┘  └──────┬───────┘
           │                 │                  │
           ▼                 ▼                  ▼
      ┌──────────┐      ┌──────────┐      ┌──────────┐
      │PostgreSQL│      │PostgreSQL│      │PostgreSQL│
      └──────────┘      └──────────┘      └──────────┘
```

### Service Communication

- **Synchronous:** REST API calls between services
- **Gateway Pattern:** All client requests go through API Gateway
- **Security:** JWT tokens passed via headers

---

## Services

### 1. API Gateway (Port 8080)

**Service Author:** _[To be filled]_

The API Gateway is the single entry point for all client requests. It handles routing, authentication, and CORS.

#### Key Responsibilities
- Route requests to appropriate microservices
- Validate JWT tokens
- Add user context headers (X-User-Id, X-User-Roles)
- Handle CORS for frontend

#### Configuration

**Routes:**
- `/api/v1/users/**` → User Service (requires auth)
- `/api/v1/courses/**` → Course Service (conditional auth)
- `/api/v1/auth/**` → Auth Service (public)
- `/api/v1/reviews` → Review Service
- `/api/v1/modules` → Modules Service (conditional auth)
- `/api/v1/lessons` → Lessons Service (conditional auth)
- `/api/v1/` → Progress Service (requires auth)
- `/api/v1/notifications` → Notification Service (requires auth)

#### Authentication Filters

**AuthGatewayFilterFactory**
- Requires valid JWT token
- Blocks requests without proper authorization

**ConditionalAuthGatewayFilterFactory**
- Allows GET requests without auth
- Requires auth for POST, PUT, DELETE

#### Environment Variables
```properties
auth.tokens.secret=<jwt-secret>
auth.tokens.issuer=http://localhost:8084
auth.tokens.audience=lms-api
```

---

### 2. Auth Service (Port 8084)

**Service Author:** _[To be filled]_

Manages user authentication, JWT token generation, and refresh token sessions.

#### Features
- User login with email/password
- JWT access token generation (15 min TTL)
- Refresh token rotation (14 day TTL)
- Session management
- Logout and logout-all functionality
- User registration

#### API Endpoints

**POST /api/v1/auth/login**
```json
Request:
{
  "email": "user@example.com",
  "password": "password123"
}

Response:
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "eyJhbGc..."
}
```

**POST /api/v1/auth/refresh**
```json
Request:
{
  "oldRefreshToken": "eyJhbGc..."
}

Response:
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "eyJhbGc..."
}
```

**POST /api/v1/auth/logout**
- Invalidates single refresh token

**POST /api/v1/auth/logout-all**
- Revokes all user sessions

**POST /api/v1/auth/register**
- Creates new user account

#### Security Features
- Passwords hashed with BCrypt (strength 12)
- Refresh tokens stored as SHA-256 hashes
- Session status tracking (ACTIVE, USED, EXPIRED, REVOKED)
- Refresh token reuse detection

#### Database Tables
- `refresh_token_session` - Stores refresh token metadata

---

### 3. User Service (Port 8082)

**Service Author:** _[To be filled]_

Manages user accounts and profiles.

#### Features
- CRUD operations for users
- Email-based user lookup
- Role management (USER, ADMIN, TEACHER)
- User activation/deactivation

#### API Endpoints

**GET /api/v1/users**
- Returns list of all users

**GET /api/v1/users/{id}**
- Get user by UUID

**GET /api/v1/users/by-email?email={email}**
- Find user by email (case-insensitive)

**GET /api/v1/users/me**
- Get current user profile (requires X-User-Id header)

**POST /api/v1/users**
```json
{
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "passwordHash": "$2a$12$...",
  "role": "USER",
  "active": true
}
```

#### User Roles
- **USER** - Standard student account
- **TEACHER** - Can create and manage courses
- **ADMIN** - Full system access

#### Database Schema
```sql
Table: users
- id (UUID, PK)
- first_name (VARCHAR)
- last_name (VARCHAR)
- email (VARCHAR, UNIQUE)
- password_hash (VARCHAR)
- role (ENUM)
- active (BOOLEAN)
```

---

### 4. Course Service (Port 8083)

**Service Author:** _[To be filled]_

Manages courses and their lifecycle.

#### Features
- Create, read, update, delete courses
- Course status management
- Student enrollment tracking
- Duration management

#### API Endpoints

**GET /api/v1/courses**
- List all courses

**GET /api/v1/courses/{id}**
- Get course details

**POST /api/v1/courses**
```json
{
  "name": "Introduction to Spring Boot",
  "description": "Learn Spring Boot basics",
  "students": ["student1", "student2"],
  "duration": 30,
  "status": "CREATED"
}
```

**PUT /api/v1/courses/{id}**
- Full course update

**PATCH /api/v1/courses/{id}/status**
```json
{
  "status": "IN_PROGRESS"
}
```

**DELETE /api/v1/courses/{id}**
- Remove course

#### Course Status
- **CREATED** - Course draft
- **IN_PROGRESS** - Active course
- **IN_ARCHIVE** - Completed course

#### Database
- Uses MongoDB
- Collection: `courses`

---

### 5. Notification Service (Port 8089)

**Service Author:** _[To be filled]_

Handles user notifications and messaging.

#### Features
- Create notifications for users
- Mark notifications as read
- Count unread messages
- Delete notifications
- Admin notification management

#### API Endpoints

**GET /api/v1/notifications/users/me** (Auth required)
- Get all my notifications

**GET /api/v1/notifications/unread** (Auth required)
- Get unread notifications only

**GET /api/v1/notifications/count** (Auth required)
```json
Response:
{
  "unreadCount": 5
}
```

**PUT /api/v1/notifications/{id}/read** (Auth required)
- Mark single notification as read

**PUT /api/v1/notifications/read-all** (Auth required)
- Mark all notifications as read

**DELETE /api/v1/notifications/{id}** (Auth required)
- Delete notification

**POST /api/v1/notifications** (Admin only)
```json
{
  "userId": "uuid",
  "notificationType": "WELCOME",
  "title": "Welcome to LMS",
  "message": "Get started with your first course"
}
```

#### Notification Types
- **WELCOME** - Welcome message for new users
- **COURSE_ENROLLMENT** - Enrolled in course
- **COURSE_COMPLETED** - Finished course
- **NEW_MODULE** - New module available
- **ADMIN_MESSAGE** - Message from admin
- **SYSTEM** - System notification

#### Database
- Uses MongoDB
- Collection: `notifications`

---

### 6. Progress Service (Port 8088)

**Service Author:** _[To be filled]_

Tracks student progress through courses and lessons.

#### Features
- Mark lessons as complete
- Track course completion percentage
- View all user progress
- Calculate course statistics

#### API Endpoints

**POST /progress/lessons/{lessonId}/complete** (Auth required)
```
Query params: courseId
Header: X-User-Id
```

**GET /progress/courses/{courseId}** (Auth required)
- Get user progress for specific course

**GET /progress/users/me** (Auth required)
- Get all progress for current user

**GET /progress/courses/{courseId}/stats** (Auth required)
```json
Response:
{
  "courseId": "uuid",
  "totalLessons": 10,
  "completedLessons": 6,
  "completionPercentage": 60.0
}
```

#### Database Schema
```sql
Table: lesson_progress
- id (UUID, PK)
- user_id (UUID)
- lesson_id (UUID)
- course_id (UUID)
- completed (BOOLEAN)
- completed_at (TIMESTAMP)
- last_accessed_at (TIMESTAMP)
```

---

### 7. Group Service (Port 8086)

**Service Author:** _[To be filled]_

Manages student groups for courses.

#### Features
- Create and manage groups
- Assign teachers to groups
- Link groups to courses
- Activate/deactivate groups

#### API Endpoints

**GET /api/v1/groups**
- List all groups

**GET /api/v1/groups/{id}**
- Get group details

**GET /api/v1/groups/by-teacher/{teacherId}**
- Get groups by teacher

**GET /api/v1/groups/by-course/{courseId}**
- Get groups for course

**GET /api/v1/groups/active**
- Get active groups only

**POST /api/v1/groups**
```json
{
  "name": "Spring 2025 Class A",
  "description": "Advanced Java course",
  "teacherId": "uuid",
  "courseId": "uuid",
  "active": true
}
```

**PUT /api/v1/groups/{id}**
- Update group

**DELETE /api/v1/groups/{id}**
- Delete group

#### Database Schema
```sql
Table: groups
- id (UUID, PK)
- name (VARCHAR, UNIQUE)
- description (TEXT)
- teacher_id (UUID)
- course_id (UUID)
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)
- active (BOOLEAN)
```

---

## Authentication & Security

### JWT Token Structure

#### Access Token
```json
{
  "iss": "http://localhost:8084",
  "aud": "lms-api",
  "sub": "user-uuid",
  "iat": 1234567890,
  "exp": 1234568790,
  "jti": "token-uuid",
  "roles": ["USER"]
}
```
- **TTL:** 15 minutes
- **Purpose:** API authentication

#### Refresh Token
```json
{
  "iss": "http://localhost:8084",
  "aud": "lms-api",
  "sub": "user-uuid",
  "iat": 1234567890,
  "exp": 1235777890,
  "jti": "token-uuid",
  "sid": "session-uuid"
}
```
- **TTL:** 14 days
- **Purpose:** Generate new access tokens

### Security Headers

Services receive authenticated user info via headers:
- `X-User-Id` - Current user UUID
- `X-User-Roles` - Comma-separated roles

### Role-Based Access

**Annotations in common-security module:**
- `@RequireAuth` - Any authenticated user
- `@RequireAdmin` - Admin or Teacher only
- `@RequireAuthenticated` - Custom auth check

### Password Security
- BCrypt hashing with strength 12
- Minimum 8 characters required
- Email format validation

---

## API Gateway

### Filter Configuration

#### Auth Filter
Applied to protected routes. Validates JWT and adds user headers.

```yaml
filters:
  - Auth
```

#### ConditionalAuth Filter
Allows public GET, requires auth for mutations.

```yaml
filters:
  - ConditionalAuth
```

### CORS Configuration

```yaml
globalcors:
  cors-configurations:
    '[/**]':
      allowed-origins: "http://localhost:3000"
      allowed-methods: [GET, POST, PUT, DELETE, OPTIONS]
      allowed-headers: "*"
      allow-credentials: true
      max-age: 3600
```

### Error Handling

Gateway returns standard error responses:
```json
{
  "error": "invalid_token",
  "message": "JWT validation failed",
  "timestamp": "2025-11-15T10:30:00Z"
}
```

---

## Database Schema

### PostgreSQL (Auth, User, Progress, Group)

**users**
```sql
CREATE TABLE users (
    id UUID PRIMARY KEY,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    active BOOLEAN DEFAULT true
);
```

**refresh_token_session**
```sql
CREATE TABLE refresh_token_session (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    token_hash VARCHAR(512) UNIQUE NOT NULL,
    created_date TIMESTAMP NOT NULL,
    expires TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL
);
```

**groups**
```sql
CREATE TABLE groups (
    id UUID PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
    teacher_id UUID,
    course_id UUID,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    active BOOLEAN DEFAULT true
);
```

**lesson_progress**
```sql
CREATE TABLE lesson_progress (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    lesson_id UUID NOT NULL,
    course_id UUID NOT NULL,
    completed BOOLEAN DEFAULT false,
    completed_at TIMESTAMP,
    last_accessed_at TIMESTAMP
);
```

### MongoDB (Courses, Notifications)

**courses collection**
```json
{
  "_id": "ObjectId",
  "name": "String",
  "description": "String",
  "students": ["String"],
  "duration": "Number",
  "status": "String (CREATED|IN_PROGRESS|IN_ARCHIVE)"
}
```

**notifications collection**
```json
{
  "_id": "ObjectId",
  "userId": "UUID",
  "type": "String",
  "title": "String",
  "message": "String",
  "read": "Boolean",
  "createdAt": "ISODate"
}
```

---

## Deployment

### Environment Variables

Each service requires:
```properties
# Database
spring.datasource.url=jdbc:postgresql://host:5432/db
spring.datasource.username=user
spring.datasource.password=pass

# MongoDB (for applicable services)
spring.data.mongodb.uri=mongodb://user:pass@host:27017/db

# Auth configuration
auth.tokens.secret=<base64-secret>
auth.tokens.issuer=http://auth-service:8084
auth.tokens.audience=lms-api
```

### Service Ports

| Service | Port |
|---------|------|
| API Gateway | 8080 |
| User Service | 8082 |
| Course Service | 8083 |
| Auth Service | 8084 |
| Review Service | 8085 |
| Group Service | 8086 |
| Modules/Lessons | 8087 |
| Progress Service | 8088 |
| Notification Service | 8089 |

### Docker Compose Example

```yaml
version: '3.8'
services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: lms_db
      POSTGRES_USER: lms_user
      POSTGRES_PASSWORD: lms_password
    ports:
      - "5432:5432"

  mongodb:
    image: mongo:6
    environment:
      MONGO_INITDB_ROOT_USERNAME: admin
      MONGO_INITDB_ROOT_PASSWORD: admin_password
    ports:
      - "27017:27017"

  api-gateway:
    build: ./api-gateway
    ports:
      - "8080:8080"
    environment:
      AUTH_TOKENS_SECRET: mysecret
    depends_on:
      - auth-service
      - user-service

  # Add other services...
```

---

## Development Guide

### Prerequisites

- Java 17+
- PostgreSQL 15+
- MongoDB 6+
- Gradle 8+ or Maven 3.8+

### Running Locally

1. **Start databases:**
```bash
docker-compose up postgres mongodb
```

2. **Configure application.properties:**
```properties
spring.profiles.active=dev
```

3. **Run services in order:**
```bash
# Terminal 1 - Auth Service
cd services/authservice
./gradlew bootRun

# Terminal 2 - User Service
cd services/userservice
./gradlew bootRun

# Terminal 3 - API Gateway
cd services/api-gateway
./gradlew bootRun
```

### Testing

Each service includes unit tests:
```bash
./gradlew test
```

### API Documentation

Each service provides Swagger UI:
- Auth Service: http://localhost:8084/swagger-ui.html
- User Service: http://localhost:8082/swagger-ui.html
- Course Service: http://localhost:8083/swagger-ui.html

### Common Issues

**Issue:** JWT validation fails
- Check token secret matches across services
- Verify token hasn't expired
- Ensure correct issuer and audience

**Issue:** CORS errors
- Verify allowed origins in gateway config
- Check credentials are allowed

**Issue:** Database connection fails
- Verify PostgreSQL/MongoDB are running
- Check connection strings and credentials

---

## Appendix

### Error Codes

| Code | Description |
|------|-------------|
| invalid_token | JWT token is malformed or invalid |
| expired_token | JWT token has expired |
| invalid_credentials | Login credentials are incorrect |
| user_not_found | User does not exist |
| email_already_exists | Email is already registered |
| user_inactive | User account is deactivated |
| refresh_reuse_detected | Refresh token was already used |

### Health Checks

All services expose actuator endpoints:
- `/actuator/health` - Service health status
- `/actuator/info` - Service information

### Logging

Services use SLF4J with configurable levels:
```properties
logging.level.ru.lms_project=DEBUG
logging.level.org.springframework.security=INFO
```

---

**End of Documentation**

For questions or issues, please contact the development team.