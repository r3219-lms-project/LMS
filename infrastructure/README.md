# 🏗️ LMS Infrastructure

Инфраструктурные файлы для LMS микросервисов.

## 📁 Структура

```
infrastructure/
├── docker-compose.yml          # Основной файл для запуска всех сервисов
├── Dockerfile.template         # Шаблон Dockerfile для микросервисов
├── pom-template.xml            # Шаблон pom.xml для микросервисов
├── application-template.yml    # Шаблон конфигурации для микросервисов
├── docker/                     # Конфигурации баз данных
│   ├── postgres/init/         # SQL скрипты для PostgreSQL
│   └── mongodb/init/          # JS скрипты для MongoDB
└── mongo-init/                # Дополнительные скрипты MongoDB
```

## 🚀 Запуск

### Только базы данных:
```bash
docker-compose up -d postgres mongodb redis
```

### С админ панелями:
```bash
docker-compose --profile admin up -d
```

## 🔧 Создание нового микросервиса

1. **Создайте директорию сервиса:**
   ```bash
   mkdir -p ../../services/your-service
   ```

2. **Скопируйте шаблоны:**
   ```bash
   cp Dockerfile.template ../../services/your-service/Dockerfile
   cp pom-template.xml ../../services/your-service/pom.xml
   cp application-template.yml ../../services/your-service/application.yml
   ```

3. **Отредактируйте файлы:**
   - Замените `SERVICE-NAME` на название вашего сервиса
   - Настройте порт в `application.yml`
   - Добавьте зависимости в `pom.xml`

4. **Добавьте сервис в docker-compose.yml:**
   ```yaml
   your-service:
     build:
       context: ../../services/your-service
       dockerfile: Dockerfile
     container_name: lms-your-service
     ports:
       - "8080:8080"
     # ... остальная конфигурация
   ```

## 📊 Доступные сервисы

- **PostgreSQL**: localhost:5432
- **MongoDB**: localhost:27017
- **Redis**: localhost:6379
- **pgAdmin**: http://localhost:5050 (admin@lms.com / admin_password)
- **Mongo Express**: http://localhost:8081 (admin / admin_password)



