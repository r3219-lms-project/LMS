# Group Service

Микросервис для управления группами в LMS (Learning Management System).

## Описание

Group Service предоставляет REST API для управления учебными группами, включая создание, обновление, удаление и поиск групп.

## Технологии

- Java 21
- Spring Boot 3.2.0
- Spring Data JPA
- PostgreSQL
- Lombok
- OpenAPI/Swagger UI

## Конфигурация

Сервис использует PostgreSQL базу данных. Основные настройки находятся в `application.properties`:

- **Порт**: 8083
- **База данных**: lms_db
- **Пользователь**: lms_user

## API Endpoints

### Основные операции

- `GET /api/v1/groups` - Получить все группы
- `GET /api/v1/groups/{id}` - Получить группу по ID
- `GET /api/v1/groups/by-teacher/{teacherId}` - Получить группы преподавателя
- `GET /api/v1/groups/by-course/{courseId}` - Получить группы курса
- `GET /api/v1/groups/active` - Получить активные группы
- `POST /api/v1/groups` - Создать новую группу
- `PUT /api/v1/groups/{id}` - Обновить группу
- `DELETE /api/v1/groups/{id}` - Удалить группу

### Примеры запросов

#### Создание группы

```bash
POST /api/v1/groups
Content-Type: application/json

{
  "name": "Java Backend Development",
  "description": "Advanced Java programming course",
  "teacherId": "550e8400-e29b-41d4-a716-446655440000",
  "courseId": "660e8400-e29b-41d4-a716-446655440000",
  "active": true
}
```

#### Обновление группы

```bash
PUT /api/v1/groups/{id}
Content-Type: application/json

{
  "name": "Updated Group Name",
  "description": "Updated description",
  "active": false
}
```

## Модель данных

### Group Entity

- `id` (UUID) - Уникальный идентификатор
- `name` (String) - Название группы (обязательно)
- `description` (String) - Описание группы
- `teacherId` (UUID) - ID преподавателя
- `courseId` (UUID) - ID курса
- `createdAt` (LocalDateTime) - Дата создания
- `updatedAt` (LocalDateTime) - Дата обновления
- `active` (Boolean) - Статус активности

## Запуск

### Локально

```bash
./gradlew bootRun
```

### Сборка

```bash
./gradlew build
```

## Swagger UI

После запуска сервиса документация API доступна по адресу:
```
http://localhost:8083/swagger-ui.html
```

## Здоровье сервиса

Проверка состояния сервиса:
```
http://localhost:8083/actuator/health
```

