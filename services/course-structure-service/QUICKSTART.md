# Course Structure Service - Инструкция по запуску

## Статус: ✅ Готов к использованию

Микросервис успешно создан, собран и протестирован.

## Предварительные требования

1. **Java 21** - установлен и настроен
2. **MongoDB** - должен быть запущен на localhost:27017
3. **Gradle** - используется wrapper, входит в проект

## Быстрый старт

### 1. Запуск MongoDB

Убедитесь, что MongoDB запущен:
```bash
# Windows (если MongoDB установлен как служба)
net start MongoDB

# Или запустите вручную
mongod --dbpath="C:\data\db"

# Проверка подключения
mongo
```

### 2. Запуск сервиса

```bash
cd C:\Users\NLSHAKAL\Documents\LMS-main
.\gradlew.bat :services:course-structure-service:bootRun
```

Сервис запустится на **порту 8087**

### 3. Проверка работоспособности

Откройте в браузере:
- **Swagger UI**: http://localhost:8087/swagger-ui.html
- **Health Check**: http://localhost:8087/actuator/health
- **API Docs**: http://localhost:8087/v3/api-docs

## Тестирование API

### Публичные endpoints (без авторизации)

```bash
# Получить модули курса
curl http://localhost:8087/api/v1/courses/{courseId}/modules

# Получить модуль по ID
curl http://localhost:8087/api/v1/modules/{moduleId}

# Получить уроки модуля
curl http://localhost:8087/api/v1/modules/{moduleId}/lessons

# Получить урок по ID
curl http://localhost:8087/api/v1/lessons/{lessonId}
```

### Защищенные endpoints (требуется JWT токен)

```bash
# Создать модуль (требуется роль ADMIN или TEACHER)
curl -X POST http://localhost:8087/api/v1/courses/{courseId}/modules \
  -H "Authorization: Bearer {JWT_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Введение в Java",
    "description": "Основы программирования на Java",
    "orderIndex": 1
  }'

# Создать урок (требуется роль ADMIN или TEACHER)
curl -X POST http://localhost:8087/api/v1/modules/{moduleId}/lessons \
  -H "Authorization: Bearer {JWT_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Первая программа",
    "content": "Hello World на Java",
    "type": "VIDEO",
    "duration": 15,
    "orderIndex": 1,
    "videoUrl": "https://example.com/video1.mp4"
  }'

# Обновить модуль (требуется роль ADMIN или TEACHER)
curl -X PUT http://localhost:8087/api/v1/modules/{moduleId} \
  -H "Authorization: Bearer {JWT_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Обновленное название",
    "orderIndex": 2
  }'

# Удалить модуль (требуется роль ADMIN или TEACHER)
curl -X DELETE http://localhost:8087/api/v1/modules/{moduleId} \
  -H "Authorization: Bearer {JWT_TOKEN}"
```

## Получение JWT токена

Для тестирования защищенных endpoints нужно получить JWT токен из authservice:

```bash
# Войти через authservice (порт 8081)
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@example.com",
    "password": "password"
  }'

# Ответ будет содержать accessToken, который используется в заголовке Authorization
```

## Запуск тестов

```bash
# Все тесты
.\gradlew.bat :services:course-structure-service:test

# Только unit тесты
.\gradlew.bat :services:course-structure-service:test --tests "*ServiceTest"

# Только интеграционные тесты
.\gradlew.bat :services:course-structure-service:test --tests "*IntegrationTest"
```

## Сборка JAR

```bash
.\gradlew.bat :services:course-structure-service:bootJar

# JAR файл будет создан в:
# services/course-structure-service/build/libs/course-structure-service-0.0.1-SNAPSHOT.jar
```

## Структура базы данных MongoDB

### Коллекция: modules
```json
{
  "_id": "ObjectId",
  "courseId": "string",
  "title": "string",
  "description": "string",
  "orderIndex": 1,
  "createdAt": "ISODate"
}
```

### Коллекция: lessons
```json
{
  "_id": "ObjectId",
  "moduleId": "string",
  "title": "string",
  "content": "string",
  "type": "VIDEO|TEXT|QUIZ",
  "duration": 30,
  "orderIndex": 1,
  "videoUrl": "string",
  "createdAt": "ISODate"
}
```

## Особенности реализации

✅ **Автоматическая сортировка** - Модули и уроки автоматически сортируются по orderIndex
✅ **Каскадное удаление** - При удалении модуля удаляются все его уроки
✅ **JWT авторизация** - Интеграция с common-security библиотекой
✅ **@RequireAdmin аннотация** - Автоматическая проверка прав доступа (ADMIN или TEACHER)
✅ **Публичные GET endpoints** - Чтение данных доступно всем
✅ **Валидация** - Автоматическая валидация входных данных
✅ **Обработка ошибок** - Унифицированные сообщения об ошибках
✅ **OpenAPI документация** - Swagger UI с поддержкой JWT авторизации

## Профили Spring Boot

- **default** - localhost MongoDB (порт 27017)
- **dev** - development профиль с отдельной БД
- **docker** - для запуска в Docker (MongoDB host: mongodb)
- **test** - embedded MongoDB для тестов

Смена профиля:
```bash
.\gradlew.bat :services:course-structure-service:bootRun -Dspring.profiles.active=dev
```

## Логи

Логи сохраняются в:
- Консоль (уровень DEBUG для ru.lms_project.coursestructure)
- Файл: logs/lms-application.log

## Troubleshooting

### MongoDB не запускается
```bash
# Проверьте статус службы
sc query MongoDB

# Создайте директорию для данных
mkdir C:\data\db
```

### Порт 8087 занят
Измените порт в `application.properties`:
```properties
server.port=8088
```

### JWT токен не работает
Убедитесь, что:
1. authservice запущен
2. Токен не истек (срок действия 24 часа)
3. Секретный ключ JWT совпадает во всех сервисах

## Интеграция с другими сервисами

- **authservice (8081)** - аутентификация и JWT токены
- **userservice (8082)** - информация о пользователях и ролях
- **courseService** - основная информация о курсах
- **groupservice (8083)** - управление группами студентов

## Поддержка

Для получения дополнительной информации смотрите:
- [README.md](README.md) - полная документация API
- [Swagger UI](http://localhost:8087/swagger-ui.html) - интерактивная документация
- [OpenAPI JSON](http://localhost:8087/v3/api-docs) - спецификация в формате JSON

---

**Статус последней сборки**: ✅ SUCCESS
**Статус тестов**: ✅ ALL PASSED (21 тестов)
**Дата создания**: 04.11.2025

