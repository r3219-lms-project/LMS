# Эндпоинт для получения статистики по структуре курса

## Описание
Добавлен новый эндпоинт для получения статистики по структуре курса.

## Эндпоинт
```
GET /api/v1/courses/{courseId}/structure-stats
```

## Параметры
- `courseId` (path parameter) - ID курса

## Ответ
```json
{
  "courseId": "string",
  "totalModules": 0,
  "totalLessons": 0,
  "totalDurationMinutes": 0
}
```

## Описание полей ответа
- `courseId` - ID курса
- `totalModules` - общее количество модулей в курсе
- `totalLessons` - общее количество уроков во всех модулях курса
- `totalDurationMinutes` - общая продолжительность всех уроков в минутах

## Безопасность
- Доступ: все аутентифицированные пользователи
- Требуется JWT токен в заголовке `Authorization: Bearer <token>`

## Статус коды
- `200 OK` - статистика успешно получена
- `401 Unauthorized` - не аутентифицирован
- `403 Forbidden` - доступ запрещен

## Пример запроса

### cURL
```bash
curl -X GET "http://localhost:8087/api/v1/courses/course123/structure-stats" \
  -H "Authorization: Bearer <your-jwt-token>"
```

### Response
```json
{
  "courseId": "course123",
  "totalModules": 5,
  "totalLessons": 23,
  "totalDurationMinutes": 450
}
```

## Реализация

### Созданные компоненты:

1. **DTO**: `CourseStructureStatsDto` - объект передачи данных для статистики
2. **Service**: `CourseStructureStatsService` - бизнес-логика для расчета статистики
3. **Controller**: `CourseStructureStatsController` - REST контроллер
4. **Annotation**: `@RequireAuthenticated` - аннотация для проверки аутентификации
5. **Aspect**: `AuthenticationAccessAspect` - аспект для обработки аннотации

### Обновленные компоненты:

1. **ModuleRepository** - добавлен метод `countByCourseId`
2. **LessonRepository** - добавлены методы `countByModuleIdIn` и `findByModuleIdIn`

## Тестирование

После запуска приложения эндпоинт будет доступен по адресу:
```
http://localhost:8087/api/v1/courses/{courseId}/structure-stats
```

Swagger UI доступен по адресу:
```
http://localhost:8087/swagger-ui.html
```

