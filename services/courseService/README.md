# LMS Course Service API

REST API for managing courses in the Learning Management System.

---

## API Documentation

- **Swagger UI:** `http://localhost:8081/swagger-ui`
- **OpenAPI JSON:** `http://localhost:8081/v3/api-docs`
- **OpenAPI YAML:** `http://localhost:8081/v3/api-docs.yaml`

---

## Endpoints Overview

Base path: `/api/v1/courses`

| Method | Path                                 | Description                     | Success Codes |
|-------:|--------------------------------------|---------------------------------|---------------|
| GET    | `/api/v1/courses`                    | Get all courses                 | `200`         |
| POST   | `/api/v1/courses`                    | Create a new course             | `201`         |
| GET    | `/api/v1/courses/{id}`               | Get a course by ID              | `200`, `404`  |
| PUT    | `/api/v1/courses/{id}`               | Update an existing course       | `200`, `400`, `404` |
| DELETE | `/api/v1/courses/{id}`               | Delete a course by ID           | `204`, `404`  |
| PATCH  | `/api/v1/courses/{id}/status`        | Change course status            | `200`, `400`, `404` |