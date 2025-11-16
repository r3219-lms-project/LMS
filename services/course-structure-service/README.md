# Course Structure Service
**Author of service:** Danil Mantush  
**Documentation author:** Bykov Lev  

---

# Table of Contents
- [1. Overview](#1-overview)
- [2. Responsibilities](#2-responsibilities)
- [3. Architecture & Components](#3-architecture--components)
  - [3.1. High-level Architecture](#31-high-level-architecture)
  - [3.2. Internal Components](#32-internal-components)
- [4. API](#4-api)
  - [4.1. Public Endpoints (no auth)](#41-public-endpoints-no-auth)
  - [4.2. Protected Endpoints (admin/teacher)](#42-protected-endpoints-adminteacher)
  - [4.3. Course Structure Stats Endpoint](#43-course-structure-stats-endpoint)
- [5. Data Models](#5-data-models)
  - [5.1. Module](#51-module)
  - [5.2. Lesson](#52-lesson)
  - [5.3. Course Structure Stats DTO](#53-course-structure-stats-dto)
- [6. Security](#6-security)
- [7. Database](#7-database)
- [8. Dependencies](#8-dependencies)
- [9. Error Handling](#9-error-handling)
- [10. Running the Service](#10-running-the-service)
  - [10.1. Prerequisites](#101-prerequisites)
  - [10.2. Quick Start](#102-quick-start)
  - [10.3. Testing](#103-testing)
  - [10.4. Build JAR](#104-build-jar)
  - [10.5. Spring Profiles](#105-spring-profiles)
  - [10.6. Logs](#106-logs)
- [11. Integration with Other Services](#11-integration-with-other-services)
- [12. Troubleshooting](#12-troubleshooting)
- [13. Status](#13-status)

---

# 1. Overview

Course Structure Service is a microservice that manages:

- **Course modules**
- **Lessons inside modules**
- **Course structure statistics** (modules count, lessons count, total duration)

It uses **MongoDB** to store data and exposes a REST API with:

- Public read endpoints (GET)
- Protected write endpoints (create/update/delete)
- One endpoint for course structure statistics

Service port (default): **8087**

---

# 2. Responsibilities

Course Structure Service is responsible for:

- Storing and managing course **modules** and **lessons**.
- Keeping the **order** of modules and lessons (`orderIndex`).
- Cascading deletion of lessons when a module is deleted.
- Providing a **read-only public API** for course structure (GET endpoints).
- Providing a **protected API** (JWT + roles) for modifying modules and lessons.
- Providing **course structure statistics** for a given course.
- Exposing Swagger/OpenAPI documentation.

---

# 3. Architecture & Components

## 3.1. High-level Architecture

```text
Client
  ↓
Course Structure Service (port 8087)
  ↓
MongoDB (modules, lessons collections)
````

Main flows:

* Public read:

    * `/api/v1/courses/{courseId}/modules`
    * `/api/v1/modules/{moduleId}/lessons`
* Protected write:

    * create/update/delete modules and lessons (requires roles `ADMIN` or `TEACHER`)
* Stats:

    * `/api/v1/courses/{courseId}/structure-stats` (for authenticated users)

## 3.2. Internal Components

Conceptual components (names may differ in code):

* **Controllers**

    * Modules controller (`/api/v1/modules`, `/api/v1/courses/{courseId}/modules`)
    * Lessons controller (`/api/v1/lessons`, `/api/v1/modules/{moduleId}/lessons`)
    * CourseStructureStatsController (`/api/v1/courses/{courseId}/structure-stats`)

* **Services**

    * Module service — business logic for modules, sorting, cascade delete.
    * Lesson service — business logic for lessons.
    * CourseStructureStatsService — calculates:

        * `totalModules`
        * `totalLessons`
        * `totalDurationMinutes`

* **Repositories**

    * `ModuleRepository`

        * e.g. `List<Module> findByCourseIdOrderByOrderIndex(...)`
        * `long countByCourseId(String courseId)`
    * `LessonRepository`

        * e.g. `List<Lesson> findByModuleIdIn(...)`
        * `long countByModuleIdIn(Collection<String> moduleIds)`

* **Security / Access**

    * Integration with **common-security** (JWT parsing).
    * `@RequireAdmin` annotation — requires `ADMIN` or `TEACHER`.
    * `@RequireAuthenticated` annotation — requires any authenticated user.
    * `AuthenticationAccessAspect` — aspect that enforces these annotations.

---

# 4. API

Base URL:

```text
http://localhost:8087
```

API prefix:

```text
/api/v1
```

## 4.1. Public Endpoints (no auth)

These endpoints are public (any user can access).

```bash
# Get modules of a course
GET /api/v1/courses/{courseId}/modules

# Get module by ID
GET /api/v1/modules/{moduleId}

# Get lessons of a module
GET /api/v1/modules/{moduleId}/lessons

# Get lesson by ID
GET /api/v1/lessons/{lessonId}
```

Example:

```bash
curl http://localhost:8087/api/v1/courses/course123/modules
```

---

## 4.2. Protected Endpoints (admin/teacher)

These endpoints require a valid **JWT access token** with role `ADMIN` or `TEACHER`.

All examples assume:

```text
Authorization: Bearer {JWT_TOKEN}
```

### Create module

```bash
POST /api/v1/courses/{courseId}/modules
```

```bash
curl -X POST http://localhost:8087/api/v1/courses/{courseId}/modules \
  -H "Authorization: Bearer {JWT_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Introduction to Java",
    "description": "Java basics",
    "orderIndex": 1
  }'
```

### Create lesson

```bash
POST /api/v1/modules/{moduleId}/lessons
```

```bash
curl -X POST http://localhost:8087/api/v1/modules/{moduleId}/lessons \
  -H "Authorization: Bearer {JWT_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "First program",
    "content": "Hello World in Java",
    "type": "VIDEO",
    "duration": 15,
    "orderIndex": 1,
    "videoUrl": "https://example.com/video1.mp4"
  }'
```

### Update module

```bash
PUT /api/v1/modules/{moduleId}
```

```bash
curl -X PUT http://localhost:8087/api/v1/modules/{moduleId} \
  -H "Authorization: Bearer {JWT_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Updated title",
    "orderIndex": 2
  }'
```

### Delete module

```bash
DELETE /api/v1/modules/{moduleId}
```

```bash
curl -X DELETE http://localhost:8087/api/v1/modules/{moduleId} \
  -H "Authorization: Bearer {JWT_TOKEN}"
```

When a module is deleted, all its lessons are also deleted (cascade delete).

---

## 4.3. Course Structure Stats Endpoint

This endpoint returns statistics for a course.

```text
GET /api/v1/courses/{courseId}/structure-stats
```

**Access:** any authenticated user (JWT required).

**Path parameter:**

* `courseId` — ID of the course.

**Response example:**

```json
{
  "courseId": "course123",
  "totalModules": 5,
  "totalLessons": 23,
  "totalDurationMinutes": 450
}
```

**Fields:**

* `courseId` — ID of the course.
* `totalModules` — number of modules in the course.
* `totalLessons` — number of lessons in all modules of the course.
* `totalDurationMinutes` — total duration of all lessons.

**Status codes:**

* `200 OK` — stats returned.
* `401 Unauthorized` — user is not authenticated.
* `403 Forbidden` — user is not allowed (if additional rules are added).
* `404 Not Found` — course not found (if implemented).

Example:

```bash
curl -X GET "http://localhost:8087/api/v1/courses/course123/structure-stats" \
  -H "Authorization: Bearer <your-jwt-token>"
```

---

# 5. Data Models

## 5.1. Module

MongoDB collection: `modules`

Example document:

```json
{
  "_id": "ObjectId",
  "courseId": "string",
  "title": "string",
  "description": "string",
  "orderIndex": 1,
  "createdAt": "2025-11-04T10:00:00Z"
}
```

Fields:

* `_id` — MongoDB ObjectId.
* `courseId` — ID of the course this module belongs to.
* `title` — module title.
* `description` — module description.
* `orderIndex` — order in the course (used for sorting).
* `createdAt` — creation time.

Modules are automatically sorted by `orderIndex`.

---

## 5.2. Lesson

MongoDB collection: `lessons`

Example document:

```json
{
  "_id": "ObjectId",
  "moduleId": "string",
  "title": "string",
  "content": "string",
  "type": "VIDEO",
  "duration": 30,
  "orderIndex": 1,
  "videoUrl": "https://example.com/video1.mp4",
  "createdAt": "2025-11-04T10:00:00Z"
}
```

Fields:

* `_id` — MongoDB ObjectId.
* `moduleId` — ID of the module this lesson belongs to.
* `title` — lesson title.
* `content` — main content (text, description, etc.).
* `type` — `VIDEO` | `TEXT` | `QUIZ`.
* `duration` — duration in minutes.
* `orderIndex` — position in module.
* `videoUrl` — optional field for video lessons.
* `createdAt` — creation time.

Lessons are also sorted by `orderIndex`.

---

## 5.3. Course Structure Stats DTO

DTO: `CourseStructureStatsDto` (concept)

```json
{
  "courseId": "string",
  "totalModules": 0,
  "totalLessons": 0,
  "totalDurationMinutes": 0
}
```

Used as response for `/api/v1/courses/{courseId}/structure-stats`.

---

# 6. Security

* Authentication and authorization are based on **JWT access tokens**.
* Integration with `common-security` module:

    * `JwtAuthenticationFilter`
    * `SecurityUtils`
* Annotations:

    * `@RequireAdmin` — checks if user has `ADMIN` or `TEACHER` role.
    * `@RequireAuthenticated` — checks that user is logged in.

Security rules:

* Public GET endpoints:

    * course modules list
    * module details
    * lessons list
    * lesson details
* Protected endpoints:

    * create/update/delete modules and lessons: only `ADMIN` or `TEACHER`.
    * structure stats: any authenticated user.

JWT token is passed in:

```text
Authorization: Bearer <accessToken>
```

---

# 7. Database

* **Database type:** MongoDB.
* **Default host:** `localhost:27017`.

Collections:

* `modules`
* `lessons`

For tests, there is a **test profile** that can use embedded MongoDB.

---

# 8. Dependencies

Main dependencies (conceptual):

* Java 21
* Spring Boot (Web, Data MongoDB)
* Spring Security
* `common-security` (shared JWT library)
* Springdoc / OpenAPI for Swagger
* MongoDB driver

Build tool: **Gradle** (wrapper included in repo).

---

# 9. Error Handling

The service uses:

* Input validation for request bodies (Bean Validation).
* Unified error responses (for example, JSON with error message).
* Typical HTTP codes:

    * `400 Bad Request` — invalid input.
    * `401 Unauthorized` — missing/invalid JWT for protected endpoints.
    * `403 Forbidden` — user has no required role.
    * `404 Not Found` — module/lesson/course not found.
    * `500 Internal Server Error` — unexpected errors.

---

# 10. Running the Service

## 10.1. Prerequisites

* Java **21** installed.
* MongoDB running on `localhost:27017`.
* Gradle wrapper available in project (`gradlew` / `gradlew.bat`).

## 10.2. Quick Start

1. Start MongoDB (examples for Windows):

   ```bash
   # If MongoDB is installed as service
   net start MongoDB

   # Or run manually
   mongod --dbpath="C:\data\db"

   # Check connection
   mongo
   ```

2. Start service (example path):

   ```bash
   cd C:\Users\NLSHAKAL\Documents\LMS-main
   .\gradlew.bat :services:course-structure-service:bootRun
   ```

3. Service will start on port **8087**.

4. Check:

    * Swagger UI: `http://localhost:8087/swagger-ui.html`
    * Health: `http://localhost:8087/actuator/health`
    * OpenAPI JSON: `http://localhost:8087/v3/api-docs`

## 10.3. Testing

Run all tests:

```bash
.\gradlew.bat :services:course-structure-service:test
```

Run only unit tests:

```bash
.\gradlew.bat :services:course-structure-service:test --tests "*ServiceTest"
```

Run only integration tests:

```bash
.\gradlew.bat :services:course-structure-service:test --tests "*IntegrationTest"
```

## 10.4. Build JAR

```bash
.\gradlew.bat :services:course-structure-service:bootJar
```

JAR path:

```text
services/course-structure-service/build/libs/course-structure-service-0.0.1-SNAPSHOT.jar
```

## 10.5. Spring Profiles

Available profiles:

* `default` — MongoDB on localhost:27017.
* `dev` — development DB.
* `docker` — for Docker (Mongo host: `mongodb`).
* `test` — embedded MongoDB for tests.

Change profile:

```bash
.\gradlew.bat :services:course-structure-service:bootRun -Dspring.profiles.active=dev
```

## 10.6. Logs

Logs are written to:

* Console (DEBUG level for `ru.lms_project.coursestructure` package).
* File: `logs/lms-application.log`.

---

# 11. Integration with Other Services

Course Structure Service works together with:

* **authservice (8081)** — login and JWT token issuing.
* **userservice (8082)** — user info and roles (indirectly via auth).
* **courseService** — main course information (titles, metadata).
* **groupservice (8083)** — student groups and enrollments.

To call protected endpoints, you need an access token from **authservice**:

```bash
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@example.com",
    "password": "password"
  }'
```

Use `accessToken` from response in `Authorization: Bearer ...` header.

---

# 12. Troubleshooting

### MongoDB does not start

```bash
# Check service status (Windows)
sc query MongoDB

# Create data directory if missing
mkdir C:\data\db
```

### Port 8087 is already in use

Change port in `application.properties` or `application.yml`:

```properties
server.port=8088
```

### JWT token does not work

Check:

1. `authservice` is running.
2. Token is not expired.
3. JWT secret, issuer, and audience are the same in all services.

---

# 13. Status

* **Build status:** ✅ SUCCESS
* **Tests status:** ✅ ALL PASSED (21 tests)
* **Created at:** 04.11.2025

Swagger / docs:

* README in repo: full API description.
* Swagger UI: `http://localhost:8087/swagger-ui.html`
* OpenAPI JSON: `http://localhost:8087/v3/api-docs`

---

