# Progress Service

Микросервис для отслеживания прогресса пользователей в курсах LMS системы.

## Описание

Progress Service предназначен для управления и отслеживания прогресса изучения уроков пользователями. Сервис обеспечивает автоматический расчет процента завершения курсов и предоставляет API для работы с прогрессом.

## Технические требования

- **Порт**: 8088
- **База данных**: PostgreSQL
- **Framework**: Spring Boot 3.5.6
- **Java**: 17+

## Основная сущность

### LessonProgress
- `id` - UUID, первичный ключ
- `userId` - UUID, идентификатор пользователя
- `lessonId` - UUID, идентификатор урока
- `courseId` - UUID, идентификатор курса
- `completed` - Boolean, статус завершения урока
- `completedAt` - LocalDateTime, время завершения урока
- `lastAccessedAt` - LocalDateTime, время последнего доступа к уроку

## API Endpoints

### POST /progress/lessons/{lessonId}/complete
Отметить урок как завершенный.

**Параметры:**
- `lessonId` - UUID урока (path parameter)
- `courseId` - UUID курса (query parameter)
- `X-User-Id` - UUID пользователя (header)

**Требует авторизации**: @RequireAuth

### GET /progress/courses/{courseId}
Получить прогресс пользователя по конкретному курсу.

**Параметры:**
- `courseId` - UUID курса (path parameter)
- `X-User-Id` - UUID пользователя (header)

**Требует авторизации**: @RequireAuth

### GET /progress/users/me
Получить весь прогресс текущего пользователя.

**Параметры:**
- `X-User-Id` - UUID пользователя (header)

**Требует авторизации**: @RequireAuth

### GET /progress/courses/{courseId}/stats
Получить статистику завершения курса (процент выполнения).

**Параметры:**
- `courseId` - UUID курса (path parameter)
- `X-User-Id` - UUID пользователя (header)

**Требует авторизации**: @RequireAuth

## Запуск сервиса

### Локальная разработка

1. Убедитесь, что PostgreSQL запущен и доступен на localhost:5432
2. База данных: `lms_db`
3. Пользователь: `lms_user`
4. Пароль: `lms_password`

```bash
./gradlew bootRun
```

### Тестирование

```bash
./gradlew test
```

## Интеграция с common-security

Сервис готов к интеграции с общим модулем безопасности. После создания common-security модуля необходимо:

1. Раскомментировать зависимость в build.gradle
2. Заменить аннотацию @RequireAuth на реальную реализацию из common-security

## Архитектура

Сервис следует стандартной архитектуре Spring Boot:

- **Controller Layer** - REST контроллеры
- **Service Layer** - бизнес-логика
- **Repository Layer** - доступ к данным
- **Model Layer** - JPA сущности
- **DTO Layer** - объекты передачи данных

## Автоматические функции

- Автоматическое обновление времени последнего доступа при каждом обращении к уроку
- Автоматическая установка времени завершения при отметке урока как завершенного
- Автоматический расчет процента завершения курса
