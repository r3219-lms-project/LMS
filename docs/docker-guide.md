# ГАЙД
Пишу очень подробно, потому что хз кто и что знает о докере в принципе.
## Установка Docker

### Windows:
1. Скачайте [Docker Desktop](https://www.docker.com/products/docker-desktop)
2. Установите WSL и обновите до последней версии(тут чисто по моему опыту, потому что официально к докеру идет WSL)
3. Перепустите систему
4. Откройте приложение докера

### Arch Linux:
Тут спросил у гпт, потому что сам хз. Так что Лев - держись!
```bash
# Установка Docker
sudo pacman -S docker docker-compose

# Запуск Docker сервиса
sudo systemctl enable docker
sudo systemctl start docker

# Добавление пользователя в группу docker
sudo usermod -aG docker $USER

# Перелогиньтесь или выполните:
newgrp docker
```

## Начало работы

### 1. Клонирование репозитория
```bash
git clone https://github.com/Blackcaf/LMS.git
cd LMS
```

### 2. Создание необходимых директорий(я сейчас закинул все, но в будущем эти папки будут для каждого свои)
```bash
# Windows (PowerShell)
New-Item -ItemType Directory -Force -Path logs, uploads, init-scripts, mongo-init

# Arch Linux
mkdir -p logs uploads init-scripts mongo-init
```

### 3. Запуск проекта
```bash
# Тут только бдшки
docker-compose up -d postgres mongodb redis

# Это полноценный запуск
docker-compose up -d

# Запуск с админ-панелями (РЕКОМЕНДУЮ)
docker-compose --profile admin up -d
```

## Пока вот так реализовал

| Сервис | URL | Логин | Пароль | Описание |
|--------|-----|-------|--------|----------|
| **LMS App** | http://localhost:8080 | - | - | Основное приложение |
| **pgAdmin** | http://localhost:5050 | admin@lms.com | admin_password | Управление PostgreSQL |
| **Mongo Express** | http://localhost:8081 | admin | admin_password | Управление MongoDB |

## Настройка pgAdmin(я вроде настроил, но проверьте и вот если что инструкция)

После входа в pgAdmin (http://localhost:5050):

1. **Добавить новый сервер:**
   - Правый клик на "Servers" → "Create" → "Server"

2. **Вкладка General:**
   - Name: `LMS PostgreSQL`

3. **Вкладка Connection:**
   - Host name/address: `lms-postgres`
   - Port: `5432`
   - Maintenance database: `lms_db`
   - Username: `lms_user`
   - Password: `lms_password`
   - Save password: галочка

### Управление сервисами:
```bash
# Остановка всех сервисов
docker-compose down

# Остановка с удалением данных (это если вы мазохист)
docker-compose down -v

# Перезапуск конкретного сервиса
docker-compose restart lms-app
```

### Пересборка после изменений:
```bash
# Пересборка приложения
docker-compose build lms-app
docker-compose up -d lms-app

# Полная пересборка всех сервисов
docker-compose build --no-cache
docker-compose up -d
```

## На всякий вот структура

```
LMS/
├── docker-compose.yml          # Основная конфигурация Docker
├── Dockerfile                  # Образ для Spring Boot приложения
├── pom.xml                     # Maven конфигурация
├── src/                        # Исходный код Java
│   └── main/
│       ├── java/
│       │   └── com/lms/
│       │       ├── LmsApplication.java
│       │       └── controller/
│       │           └── HomeController.java
│       └── resources/
│           └── application-docker.yml
├── logs/                       # Логи приложения
├── uploads/                    # Загруженные файлы
├── init-scripts/               # SQL скрипты для PostgreSQL
├── mongo-init/                 # JS скрипты для MongoDB
└── README.md                   # Документация
```

## Разработка

### Подключение к базам данных:

#### PostgreSQL (через командную строку):
```bash
docker-compose exec postgres psql -U lms_user -d lms_db
```

#### MongoDB (через командную строку):
```bash
docker-compose exec mongodb mongosh --username admin --password admin_password --authenticationDatabase admin
```

#### Redis (через командную строку):
```bash
docker-compose exec redis redis-cli -a redis_password
```

### Подключение к контейнерам:
```bash
# Bash в контейнере приложения
docker-compose exec lms-app bash

# Если bash недоступен, то sh
docker-compose exec lms-app sh
```

### Мониторинг ресурсов:
```bash
# Использование ресурсов контейнерами
docker stats

# Подробная информация о контейнере
docker inspect lms-application
```

## Устранение проблем

### Проблема: "Порты заняты"
```bash
# Windows - найти процесс на порту
netstat -ano | findstr :8080
taskkill /F /PID <PID>

# Arch Linux - найти и убить процесс
sudo lsof -i :8080
sudo kill -9 <PID>

# Или изменить порты в docker-compose.yml(это крайний случай)
```

### Проблема: "MongoDB authentication failed"
```bash
# Пересоздать контейнер MongoDB
docker-compose down mongodb
docker volume rm lms_mongodb_data
docker-compose up -d mongodb
```

### Проблема: "pgadmin_data volume not found"
```bash
# Убедитесь что в docker-compose.yml есть в секции volumes:(а то это рили важно)
# pgadmin_data:
```

### Проблема: "Application not responding on port 8080"
```bash
# Проверить статус приложения
docker-compose logs lms-app

# Пересобрать приложение
docker-compose build lms-app --no-cache
docker-compose up -d lms-app
```

### Полная очистка Docker (крайняя мера):
```bash
# Остановить все контейнеры
docker stop $(docker ps -aq)

# Удалить все контейнеры
docker rm $(docker ps -aq)

# Очистить систему
docker system prune -a

# Удалить volumes (УДАЛИТ ВСЕ ДАННЫЕ!)
docker volume prune
```

## 🏗️ Архитектура микросервисов

### Структура проекта:
```
LMS/
├── services/                    # Микросервисы
│   ├── user-service/           # Управление пользователями
│   ├── auth-service/           # Аутентификация
│   ├── course-service/         # Курсы и контент
│   └── gateway-service/        # API Gateway
├── shared/                     # Общие компоненты
│   ├── common-lib/            # Общие утилиты
│   └── database-models/       # Общие модели БД
├── infrastructure/            # Инфраструктура
│   ├── docker-compose.yml     # Основной файл
│   ├── docker/               # Docker конфигурации
│   └── mongo-init/           # Инициализация MongoDB
└── README.md
```

### Запуск проекта:

1. **Установить Docker** (см. раздел выше)
2. **Клонировать репозиторий:**
   ```bash
   git clone https://github.com/Blackcaf/LMS.git
   cd LMS
   ```
3. **Создать директории:**
   ```bash
   mkdir -p logs uploads
   ```
4. **Запустить проект:**
   ```bash
   docker-compose -f infrastructure/docker-compose.yml --profile admin up -d
   ```
5. **Проверить работу:**
   - http://localhost:5050 - pgAdmin (admin@lms.com / admin_password)
   - http://localhost:8081 - Mongo Express (admin / admin_password)
   
   **Примечание:** Микросервисы будут добавлены в папку `services/` по мере разработки

### Синхронизация с командой:
```bash
# Получить последние изменения
git pull origin main

# Обновить Docker образы
docker-compose pull

# Перезапустить с обновлениями
docker-compose up -d --build
```

## Тестирование

### Проверка работы сервисов:
```bash
# Проверка Spring Boot приложения
curl http://localhost:8080
curl http://localhost:8080/health

```

### Health Checks:
```bash
# Проверка здоровья всех сервисов
docker-compose ps

# Подробные health checks
curl http://localhost:8080/actuator/health
```

## Backup и восстановление

### Backup PostgreSQL:
```bash
docker-compose exec postgres pg_dump -U lms_user lms_db > backup_postgres.sql
```

### Backup MongoDB:
```bash
docker-compose exec mongodb mongodump --uri="mongodb://admin:admin_password@localhost:27017/lms_mongo_db?authSource=admin" --out /tmp/backup
docker cp lms-mongodb:/tmp/backup ./backup_mongodb
```

### Восстановление PostgreSQL:
```bash
docker-compose exec -T postgres psql -U lms_user -d lms_db < backup_postgres.sql
```

### Восстановление MongoDB:
```bash
docker cp ./backup_mongodb lms-mongodb:/tmp/restore
docker-compose exec mongodb mongorestore --uri="mongodb://admin:admin_password@localhost:27017/lms_mongo_db?authSource=admin" /tmp/restore
```

## Продакшн

### Переменные окружения для продакшена:
```bash
# Создайте файл .env.production
POSTGRES_PASSWORD=secure_postgres_password
MONGO_ROOT_PASSWORD=secure_mongo_password
REDIS_PASSWORD=secure_redis_password
PGLADMIN_PASSWORD=secure_pgladmin_password
```

### Запуск в продакшене:
```bash
# Используйте внешние базы данных
# Настройте SSL сертификаты
# Используйте Docker Swarm или Kubernetes
```



