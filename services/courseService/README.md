# Course Service
**Author of service:** Hristo Darlyanov  
**Documentation author:** Bykov Lev  

---

# Table of Contents
- [1. Overview](#1-overview)
- [2. Responsibilities](#2-responsibilities)
- [3. Architecture & Components](#3-architecture--components)
  - [3.1. High-level Architecture](#31-high-level-architecture)
  - [3.2. Internal Components](#32-internal-components)
- [4. API](#4-api)
  - [4.1. GET /api/v1/courses](#41-get-apiv1courses)
  - [4.2. POST /api/v1/courses](#42-post-apiv1courses)
  - [4.3. GET /api/v1/courses{id}](#43-get-apiv1coursesid)
  - [4.4. DELETE /api/v1/courses{id}](#44-delete-apiv1coursesid)
  - [4.5. PUT /api/v1/courses{id}](#45-put-apiv1coursesid)
  - [4.6. PATCH /api/v1/courses{id}status](#46-patch-apiv1coursesidstatus)
- [5. Data Models](#5-data-models)
  - [5.1. Course](#51-course)
  - [5.2. CreateCourseRequest](#52-createcourserequest)
  - [5.3. CreateCourseResponse](#53-createcourseresponse)
  - [5.4. UpdateCourseRequest](#54-updatecourserequest)
  - [5.5. ChangeStatusRequest](#55-changestatusrequest)
  - [5.6. CourseStatus](#56-coursestatus)
- [6. Security](#6-security)
- [7. Database](#7-database)
- [8. Dependencies](#8-dependencies)
- [9. Error Handling](#9-error-handling)
- [10. Running the Service](#10-running-the-service)
  - [10.1. Prerequisites](#101-prerequisites)
  - [10.2. Quick Start](#102-quick-start)
  - [10.3. Swagger & API Docs](#103-swagger--api-docs)
- [11. Testing](#11-testing)
- [12. Build JAR](#12-build-jar)
- [13. Troubleshooting](#13-troubleshooting)
- [14. Status](#14-status)

---

# 1. Overview

Course Service is a microservice that manages **courses** in the LMS.

It provides REST API to:

- create courses,
- list all courses,
- get course by ID,
- update a course,
- delete a course,
- change only the course **status**.

Service runs on **port 8083** by default and uses **MongoDB** to store courses.

---

# 2. Responsibilities

Course Service is responsible for:

- Storing **course metadata**:
  - name
  - description
  - list of students (as string IDs for now)
  - duration (in minutes)
  - status (CREATED / IN_PROGRESS / IN_ARCHIVE)
- Exposing CRUD endpoints for courses.
- Exposing a separate endpoint to change only the **status** of a course.
- Validating incoming requests (basic Bean Validation).
- Providing OpenAPI/Swagger documentation.

It does **not** manage modules or lessons (this is done by the Course Structure Service).

---

# 3. Architecture & Components

## 3.1. High-level Architecture

```text
Client
  ↓
Course Service (port 8083)
  ↓
MongoDB (courses collection)
````

## 3.2. Internal Components

Package: `com.lms.courseService`

* **CourseServiceApplication**
  Main Spring Boot entry point.

* **Model**

    * `Course` — MongoDB document for a course.
    * `CourseStatus` — enum with course status values.

* **DTO**

    * `CreateCourseRequest`
    * `CreateCourseResponse`
    * `UpdateCourseRequest`
    * `ChangeStatusRequest`

* **Repository**

    * `CourseRepository extends MongoRepository<Course, String>`

* **Service**

    * `CourseService`

        * `create(Course c)` — create course.
        * `findAll()` — list courses.
        * `findById(String id)` — get course.
        * `delete(String id)` — delete by ID (returns boolean).
        * `changeStatus(String id, CourseStatus status)` — update only status.
        * `update(String id, UpdateCourseRequest req)` — full update.

* **Mapper**

    * `CourseMapper`

        * `Course toEntity(CreateCourseRequest req)`
        * `CreateCourseResponse toResponse(Course c)`

* **Controller**

    * `CourseController` at `/api/v1/courses`

        * all REST endpoints for courses.

* **Config**

    * `SecurityConfig`

        * configures Spring Security and CORS.

---

# 4. API

Base URL (service):

```text
http://localhost:8083
```

Base path:

```text
/api/v1/courses
```

All examples use JSON.

---

## 4.1. GET `/api/v1/courses`

Get list of all courses.

**Request:**

```http
GET /api/v1/courses
```

**Response 200 (example):**

```json
[
  {
    "id": "64f0c1a2e4b0c12345678901",
    "name": "Java Basics",
    "description": "Intro to Java",
    "students": ["user1", "user2"],
    "duration": 120,
    "status": "CREATED"
  }
]
```

---

## 4.2. POST `/api/v1/courses`

Create a new course.

**Request:**

```http
POST /api/v1/courses
Content-Type: application/json
```

Body:

```json
{
  "name": "Java Basics",
  "description": "Intro to Java",
  "students": ["user1", "user2"],
  "duration": 120,
  "status": "CREATED"
}
```

**Response 201 Created:**

Headers:

```http
Location: /api/v1/courses/{id}
```

Body (`CreateCourseResponse`):

```json
{
  "id": "64f0c1a2e4b0c12345678901",
  "name": "Java Basics",
  "description": "Intro to Java",
  "students": ["user1", "user2"],
  "duration": 120,
  "status": "CREATED"
}
```

---

## 4.3. GET `/api/v1/courses/{id}`

Get a course by ID.

**Request:**

```http
GET /api/v1/courses/{id}
```

**Response 200 (example):**

```json
{
  "id": "64f0c1a2e4b0c12345678901",
  "name": "Java Basics",
  "description": "Intro to Java",
  "students": ["user1", "user2"],
  "duration": 120,
  "status": "IN_PROGRESS"
}
```

**Response 404:**

Course not found.

---

## 4.4. DELETE `/api/v1/courses/{id}`

Delete a course by ID.

**Request:**

```http
DELETE /api/v1/courses/{id}
```

**Response 204 No Content:**

Course deleted.

**Response 404:**

Course not found.

---

## 4.5. PUT `/api/v1/courses/{id}`

Update all main fields of a course.

**Request:**

```http
PUT /api/v1/courses/{id}
Content-Type: application/json
```

Body (`UpdateCourseRequest`):

```json
{
  "name": "Updated name",
  "description": "Updated description",
  "students": ["user3", "user4"],
  "duration": 150,
  "courseStatus": "IN_PROGRESS"
}
```

**Response 200:**

```json
{
  "id": "64f0c1a2e4b0c12345678901",
  "name": "Updated name",
  "description": "Updated description",
  "students": ["user3", "user4"],
  "duration": 150,
  "status": "IN_PROGRESS"
}
```

**Response 404:**

Course not found.

---

## 4.6. PATCH `/api/v1/courses/{id}/status`

Change only the status of a course.

**Request:**

```http
PATCH /api/v1/courses/{id}/status
Content-Type: application/json
```

Body (`ChangeStatusRequest`):

```json
{
  "status": "IN_ARCHIVE"
}
```

**Response 200:**

Returns the updated `Course`:

```json
{
  "id": "64f0c1a2e4b0c12345678901",
  "name": "Java Basics",
  "description": "Intro to Java",
  "students": ["user1", "user2"],
  "duration": 120,
  "status": "IN_ARCHIVE"
}
```

**Response 404:**

Course not found.

---

# 5. Data Models

## 5.1. Course

Mongo document: `Course`

```java
@Document(collection = "courses")
public class Course {
    @Id
    private String id;

    @NotBlank
    private String name;

    private String description;

    // For now: list of user IDs as strings
    private List<String> students;

    @NotNull
    private Integer duration;

    @NotNull
    private CourseStatus status = CourseStatus.CREATED;
}
```

---

## 5.2. CreateCourseRequest

```java
public record CreateCourseRequest(
        @NotBlank String name,
        String description,
        List<String> students,
        @NotNull Integer duration,
        CourseStatus status
) {}
```

Example JSON:

```json
{
  "name": "Java Basics",
  "description": "Intro to Java",
  "students": ["user1"],
  "duration": 120,
  "status": "CREATED"
}
```

---

## 5.3. CreateCourseResponse

```java
public record CreateCourseResponse(
        String id,
        String name,
        String description,
        List<String> students,
        Integer duration,
        CourseStatus status
) {}
```

Example JSON:

```json
{
  "id": "64f0c1a2e4b0c12345678901",
  "name": "Java Basics",
  "description": "Intro to Java",
  "students": ["user1"],
  "duration": 120,
  "status": "CREATED"
}
```

---

## 5.4. UpdateCourseRequest

```java
public class UpdateCourseRequest {
    @NotBlank
    private String name;
    private String description;
    private List<String> students;
    @NotNull
    private Integer duration;
    private CourseStatus courseStatus;
    // getters/setters...
}
```

JSON:

```json
{
  "name": "New name",
  "description": "New description",
  "students": ["user1", "user2"],
  "duration": 100,
  "courseStatus": "IN_PROGRESS"
}
```

---

## 5.5. ChangeStatusRequest

```java
public class ChangeStatusRequest {
    @NotNull
    private CourseStatus status;
    // getter/setter
}
```

JSON:

```json
{
  "status": "IN_ARCHIVE"
}
```

---

## 5.6. CourseStatus

```java
public enum CourseStatus {
    CREATED, IN_PROGRESS, IN_ARCHIVE
}
```

---

# 6. Security

* The service has a custom **SecurityConfig** with Spring Security.
* In this version, there is **no role-based logic in the controller**.
* It is expected that:

    * API Gateway or other layer will handle JWT checks, or
    * further integration with `common-security` will be added.

CORS is configured:

* Allowed origin: `http://localhost:3000`
* Methods: `GET, POST, PUT, PATCH, DELETE, OPTIONS`
* Headers: `*`
* Credentials allowed.

So a React frontend on `http://localhost:3000` can call this API directly in dev.

---

# 7. Database

* **Type:** MongoDB
* **Entity:** `Course`
* **Repository:** `CourseRepository extends MongoRepository<Course, String>`

Connection (dev profile) in `application-dev.properties`:

```properties
spring.data.mongodb.uri=mongodb://admin:admin_password@localhost:27017/lms_mongo_db?authSource=admin
```

---

# 8. Dependencies

Main dependencies (from `build.gradle`):

* `spring-boot-starter-web`
* `spring-boot-starter-validation`
* `spring-boot-starter-data-mongodb`
* `spring-boot-starter-security`
* `mapstruct` (for mapping)
* `springdoc-openapi-starter-webmvc-ui` (Swagger / OpenAPI)
* JUnit 5, Mockito (tests)

---

# 9. Error Handling

Typical HTTP codes:

* `200 OK` — successful GET / PATCH / PUT.
* `201 Created` — course created.
* `204 No Content` — course deleted.
* `400 Bad Request` — validation errors (e.g. empty `name`, missing `duration`).
* `404 Not Found` — course with given ID does not exist.
* `500 Internal Server Error` — unexpected errors.

Validation is done with Jakarta Bean Validation annotations:

* `@NotBlank` on `name`
* `@NotNull` on `duration` and `status`

---

# 10. Running the Service

## 10.1. Prerequisites

* Java **21**
* MongoDB running on `localhost:27017`
* Gradle wrapper (included in project)

## 10.2. Quick Start

1. Start MongoDB (example for local):

   ```bash
   mongod --dbpath="C:\data\db"
   ```

2. Run the service from project root:

   ```bash
   ./gradlew :services:course-service:bootRun
   ```

   or on Windows:

   ```bash
   .\gradlew.bat :services:course-service:bootRun
   ```

3. The service will start on:

   ```text
   http://localhost:8083
   ```

## 10.3. Swagger & API Docs

From `application-dev.properties`:

```properties
springdoc.api-docs.path=/v1/api-docs
springdoc.api-docs.enabled=true
springdoc.swagger-ui.url=/openapi.yaml
```

* OpenAPI YAML: static file at `/openapi.yaml`
* Swagger UI (typical Springdoc default):
  `http://localhost:8083/swagger-ui/index.html`
  (UI is configured to load `/openapi.yaml`)

---

# 11. Testing

Basic tests:

```bash
./gradlew :services:course-service:test
```

Test class:

* `CourseServiceApplicationTests` (smoke / context test)

You can add more unit tests and integration tests for:

* `CourseService`
* `CourseController`

---

# 12. Build JAR

Build runnable JAR:

```bash
./gradlew :services:course-service:bootJar
```

JAR will be in:

```text
build/libs/
```

(e.g. `course-service-0.0.1-SNAPSHOT.jar` depending on your Gradle config)

---

# 13. Troubleshooting

### MongoDB connection errors

* Check that MongoDB is running on `localhost:27017`.
* Check `spring.data.mongodb.uri` in `application-dev.properties`.
* Ensure `admin/admin_password` user exists in MongoDB (or adjust URI).

### Port 8083 already in use

Change port in `application.properties`:

```properties
server.port=8084
```

### Swagger UI not loading schema

Check `springdoc.swagger-ui.url=/openapi.yaml` and that the file exists at:

```text
src/main/resources/static/openapi.yaml
```

---

# 14. Status

* **Build status:** ✅ Service compiles
* **Tests:** ✅ Basic tests pass (according to provided project)
* **Default port:** `8083`
* **Scope:** Courses only (no modules / lessons here)

---
