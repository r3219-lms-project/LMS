# Notification Service

In-app notifications for LMS users.

## Endpoints

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| GET | `/api/v1/notifications` | Get all my notifications | Required |
| GET | `/api/v1/notifications/unread` | Get unread notifications | Required |
| GET | `/api/v1/notifications/count` | Get unread count | Required |
| PUT | `/api/v1/notifications/{id}/read` | Mark as read | Required |
| PUT | `/api/v1/notifications/read-all` | Mark all as read | Required |
| DELETE | `/api/v1/notifications/{id}` | Delete notification | Required |

## RequireAdmin endpoints

| Method | Path | Description | 
|--------|------|-------------|
| POST   | `/api/v1/notifications` | Create new Notification |
| GET | `/api/v1/notifications/users/{userId}` | Get all notifications which user have |

## Notification Types

- `WELCOME` - Welcome message for new users
- `COURSE_ENROLLMENT` - User enrolled in course
- `COURSE_COMPLETED` - User completed course
- `NEW_MODULE` - New module added to enrolled course
- `ADMIN_MESSAGE` - Message from admin
- `SYSTEM` - System notification

## Database

MongoDB database: our mongodb db

Table: `notifications`
- `id` - Primary key
- `user_id` - UUID of user
- `type` - Notification type (enum)
- `title` - Notification title
- `message` - Notification message (optional)
- `is_read` - Read status (boolean)
- `created_at` - Creation timestamp

## Configuration
```yaml
server:
  port: 8089

spring:
  application:
    name: notificationservice

  data:
    mongodb:
      uri: mongodb://admin:admin_password@localhost:27017/lms_mongo_db?authSource=admin

auth:
  tokens:
    secret: mysuperlongsecretkey_for_dev_1234567890
    issuer: http://localhost:8084
    audience: lms-api

logging:
  level:
    ru.lms_project.notificationservice: DEBUG
    org.springframework.data.mongodb: DEBUG

springdoc:
  swagger-ui:
    url: /openapi.yaml
```

## How to Run
```bash
# Run service
gradle :services:notificationservice:bootRun
```

Service author: Bykov Lev
Documentation written by: Bykov Lev